/**
 * Copyright (c) 2012-2013 by European Organization for Nuclear Research (CERN)
 * Author: Justin Salmon <jsalmon@cern.ch>
 *
 * XRootD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * XRootD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with XRootD.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.cern.dss.teamcity.agent;

import ch.cern.dss.teamcity.agent.util.ArchiveExtractor;
import ch.cern.dss.teamcity.agent.util.FileUtil;
import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import ch.cern.dss.teamcity.common.IOUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an context in which to execute the abi-compatibility-checker program and associated command line
 * arguments. Also supports setting up mock environments and checking compatibility inside them, depending upon
 * runner parameters.
 */
public class AbiCheckerBuildService extends BuildServiceAdapter {

    private AbiCheckerContext context;
    private SimpleLogger logger;
    private ArchiveExtractor archiveExtractor;

    /**
     * This method is called before command line preparation has started.
     * <p/>
     * Here we download and extract the reference artifacts, extract the newly build artifacts (if necessary) and
     * resolve the wildcard paths to the artifacts that we actually want to use.
     *
     * @throws jetbrains.buildServer.RunBuildException
     *
     */
    @Override
    public void beforeProcessStarted() throws RunBuildException {
        super.beforeProcessStarted();
        this.context = new AbiCheckerContext(getRunnerParameters(), getBuildParameters(),
                getRunnerContext(), getBuildTempDirectory(),
                getWorkingDirectory());
        this.logger = new SimpleLogger(getLogger());
        this.archiveExtractor = new ArchiveExtractor(this.logger);

        logger.message("Downloading and extracting reference artifacts");
        String referenceArtifactZipFile = downloadReferenceArtifacts(context.getReferenceBuildType(),
                context.getReferenceTag());
        extractArtifacts(referenceArtifactZipFile, context.getReferenceArtifactsDirectory());

        logger.message("Finding reference files");
        logger.message("Matching pattern: " + context.getArtifactFilePattern());
        logger.message("    in directory: " + context.getReferenceArtifactsDirectory());
        List<String> matchedReferenceArtifacts = FileUtil.findFiles(context.getReferenceArtifactsDirectory(),
                context.getArtifactFilePattern());
        logger.message("Matched files: " + Arrays.toString(matchedReferenceArtifacts.toArray()));

        // Extract the reference files if necessary
        String artifactType = context.getArtifactType();
        if (artifactType.equals(AbiCheckerConstants.ARTIFACT_TYPE_RPM)
                || artifactType.equals(AbiCheckerConstants.ARTIFACT_TYPE_ARCHIVE)) {

            logger.message("Extracting reference files");
            for (String artifact : matchedReferenceArtifacts) {
                extractArtifacts(artifact, new File(artifact).getParent());
            }
        }

        logger.message("Finding newly built files");
        logger.message("Matching pattern: " + context.getArtifactFilePattern());
        logger.message("    in directory: " + context.getNewArtifactsDirectory());
        List<String> matchedNewArtifacts = FileUtil.findFiles(context.getNewArtifactsDirectory(),
                context.getArtifactFilePattern());
        logger.message("Matched files: " + Arrays.toString(matchedNewArtifacts.toArray()));

        // Extract the newly built files if necessary
        if (artifactType.equals(AbiCheckerConstants.ARTIFACT_TYPE_RPM)
                || artifactType.equals(AbiCheckerConstants.ARTIFACT_TYPE_ARCHIVE)) {

            logger.message("Extracting newly built files");
            for (String artifact : matchedNewArtifacts) {
                String relativePath = new File(new File(context.getNewArtifactsDirectory()).toURI()
                        .relativize(new File(artifact).toURI()).toString()).getParent();
                logger.message("relpath: " + relativePath);
                extractArtifacts(artifact, context.getNewExtractedArtifactsDirectory() + File.separator + relativePath);
            }
        }

        if (matchedReferenceArtifacts.size() != matchedNewArtifacts.size()) {
            logger.warning("Number of reference artifacts (" + matchedReferenceArtifacts.size() +
                    ") differs from number of new artifacts (" + matchedNewArtifacts.size() + ")");
        }
    }

    /**
     * @return
     * @throws RunBuildException
     */
    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        // Build the arguments, depending on the mode
        ProgramCommandLine commandLine;

        if (context.getBuildMode().equals(AbiCheckerConstants.BUILD_MODE_NORMAL)) {
            commandLine = new NormalModeCommandLine(context, logger);
        } else if (context.getBuildMode().equals(AbiCheckerConstants.BUILD_MODE_MOCK)) {
            commandLine = new MockModeCommandLine(context, logger);
        } else {
            throw new RunBuildException("Unknown build mode");
        }

        // Run the command
        return commandLine;
    }

    /**
     * Query the server for the build ID of the build we are referencing, then use this build ID do download a zip
     * file of the reference artifacts. We use the TeamCity REST API for convenience.
     *
     * @param buildType the reference build type ID, e.g. bt23
     * @param tag       the tagged reference build that we want to retrieve artifacts for.
     *
     * @return absolute path to the downloaded zip file.
     * @throws RunBuildException to break the build
     */
    private String downloadReferenceArtifacts(String buildType, String tag) throws RunBuildException {
        String serverUrl = getAgentConfiguration().getServerUrl();
        String restUrl = serverUrl
                + "/httpAuth/app/rest/builds/buildType:" + buildType
                + ",tag:" + tag
                + ",personal:false,count:1,status:SUCCESS";

        // Set the default authenticator
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        getSystemProperties().get("teamcity.auth.userId"),
                        getSystemProperties().get("teamcity.auth.password").toCharArray());
            }
        });

        BuildInfoXmlResponseHandler handler = new BuildInfoXmlResponseHandler();

        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(new InputSource(new URL(restUrl).openStream()), handler);
        } catch (Exception e) {
            throw new RunBuildException("Error determining build info XML", e);
        }

        String referenceArtifactDownloadUrl = serverUrl
                + "/httpAuth/repository/downloadAll/"
                + handler.getBuildTypeId() + "/" + handler.getBuildId() + ":id"
                + "/artifacts.zip";
        String referenceArtifactZipFile = getBuildTempDirectory().getAbsolutePath() + "/artifacts-" + tag + ".zip";

        try {
            IOUtil.saveUrl(referenceArtifactZipFile, referenceArtifactDownloadUrl);
        } catch (IOException e) {
            throw new RunBuildException("Error downloading artifacts", e);
        }

        return referenceArtifactZipFile;
    }

    /**
     * Extract an archive containing artifacts to the specified directory.
     *
     * @param artifactArchive     the archive to extract.
     * @param extractionDirectory the destination directory to extract to.
     *
     * @throws RunBuildException to break the build.
     */
    private void extractArtifacts(String artifactArchive, String extractionDirectory) throws RunBuildException {
        try {
            archiveExtractor.extract(artifactArchive, extractionDirectory);
        } catch (Exception e) {
            throw new RunBuildException("Error extracting artifacts", e);
        }
    }


}
