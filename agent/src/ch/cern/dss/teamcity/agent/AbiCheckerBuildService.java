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
import ch.cern.dss.teamcity.agent.util.IOUtils;
import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.AntPatternFileFinder;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides an environment in which to execute the abi-compatibility-checker program and associated command line
 * arguments. Also supports setting up mock environments and checking compatibility inside them, depending upon
 * runner parameters.
 */
public class AbiCheckerBuildService extends BuildServiceAdapter {

    private SimpleLogger logger;
    private ArchiveExtractor archiveExtractor;
    private String referenceArtifactsDirectory;
    private String newArtifactsDirectory;

    /**
     * This method is called before command line preparation has started.
     * <p/>
     * Here we download and extract the reference artifacts, extract the newly build artifacts (if necessary) and
     * resolve the wildcard paths to the artifacts that we actually want to use.
     *
     * @throws RunBuildException
     */
    @Override
    public void beforeProcessStarted() throws RunBuildException {
        super.beforeProcessStarted();

        this.logger = new SimpleLogger(getLogger());
        this.archiveExtractor = new ArchiveExtractor(this.logger);
        final Map<String, String> runnerParameters = getRunnerParameters();

        String referenceBuildType = runnerParameters.get(AbiCheckerConstants.REFERENCE_BUILD_TYPE);
        String referenceTag = runnerParameters.get(AbiCheckerConstants.REFERENCE_TAG);
        String referenceArtifactType = runnerParameters.get(AbiCheckerConstants.ARTIFACT_TYPE);

        // Download reference artifact zip
        String referenceArtifactZipFile = downloadReferenceArtifacts(referenceBuildType, referenceTag);

        // Extract downloaded artifact zip
        setReferenceArtifactsDirectory(getBuildTempDirectory().getAbsolutePath() + "/artifacts-" + referenceTag);
        extractArtifacts(referenceArtifactZipFile, referenceArtifactsDirectory);

        // Find the extracted reference files
        String referenceArtifactFilePattern = runnerParameters.get(AbiCheckerConstants.ARTIFACT_FILES);
        List<String> matchedReferenceArtifacts = findFiles(referenceArtifactsDirectory,
                referenceArtifactFilePattern);

        // Extract the reference files if necessary
        if (referenceArtifactType.equals(AbiCheckerConstants.ARTIFACT_TYPE_RPM)
                || referenceArtifactType.equals(AbiCheckerConstants.ARTIFACT_TYPE_ARCHIVE)) {

            logger.message("Extracting reference files");
            for (String artifact : matchedReferenceArtifacts) {
                extractArtifacts(artifact, referenceArtifactsDirectory);
            }
        }

        // Find the newly built files
        String newArtifactFolder = getRunnerContext().getBuild().getArtifactsPaths();
        List<String> matchedNewArtifacts = findFiles(newArtifactFolder, referenceArtifactFilePattern);

        // Extract the newly built files if necessary
        setNewArtifactsDirectory(getWorkingDirectory() + File.separator + "extracted");
        if (referenceArtifactType.equals(AbiCheckerConstants.ARTIFACT_TYPE_RPM)
                || referenceArtifactType.equals(AbiCheckerConstants.ARTIFACT_TYPE_ARCHIVE)) {

            logger.message("Extracting new files");
            for (String artifact : matchedNewArtifacts) {
                extractArtifacts(artifact, newArtifactsDirectory);
            }
        }
    }

    /**
     * @return
     * @throws RunBuildException
     */
    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final Map<String, String> runnerParameters = getRunnerParameters();

        String referenceTag = runnerParameters.get(AbiCheckerConstants.REFERENCE_TAG);
        String gccOptions = runnerParameters.get(AbiCheckerConstants.GCC_OPTIONS);

        // Find the header and library files
        String headerFiles = runnerParameters.get(AbiCheckerConstants.ARTIFACT_HEADER_FILES);
        String libraryFiles = runnerParameters.get(AbiCheckerConstants.ARTIFACT_LIBRARY_FILES);

        List<String> matchedReferenceHeaderFiles = findFiles(referenceArtifactsDirectory, headerFiles);
        List<String> matchedReferenceLibraryFiles = findFiles(referenceArtifactsDirectory, libraryFiles);
        List<String> matchedNewHeaderFiles = findFiles(newArtifactsDirectory, headerFiles);
        List<String> matchedNewLibraryFiles = findFiles(newArtifactsDirectory, libraryFiles);

        // Write the XML files
        String referenceXmlFileName = getBuildTempDirectory() + File.separator + referenceTag + ".xml";
        File referenceXmlFile = writeXmlDescriptor(referenceXmlFileName,
                runnerParameters.get(AbiCheckerConstants.REFERENCE_BUILD_TYPE_NAME) + " " + referenceTag,
                matchedReferenceHeaderFiles,
                matchedReferenceLibraryFiles,
                gccOptions);

        String newXmlFileName = getBuildTempDirectory() + File.separator
                + getRunnerContext().getBuild().getBuildNumber() + ".xml";
        File newXmlFile = writeXmlDescriptor(newXmlFileName,
                getRunnerContext().getBuild().getBuildTypeName() + " build #"
                        + getRunnerContext().getBuild().getBuildNumber(),
                matchedNewHeaderFiles,
                matchedNewLibraryFiles,
                gccOptions);

        // Build the arguments
        List<String> libNames = new ArrayList<String>();
        for (String libName : matchedReferenceLibraryFiles) {
            libNames.add(new File(libName).getName());
        }

        final AbiCheckerCommandLineBuilder commandLineBuilder = new AbiCheckerCommandLineBuilder(logger,
                runnerParameters, libNames, referenceXmlFile, newXmlFile, getBuild().getArtifactsPaths());

        // Run the command
        return new ProgramCommandLine() {
            @NotNull
            @Override
            public String getExecutablePath() throws RunBuildException {
                return commandLineBuilder.getExecutablePath();
            }

            @NotNull
            @Override
            public String getWorkingDirectory() throws RunBuildException {
                return getCheckoutDirectory().getPath();
            }

            @NotNull
            @Override
            public List<String> getArguments() throws RunBuildException {
                return commandLineBuilder.getArguments();
            }

            @NotNull
            @Override
            public Map<String, String> getEnvironment() throws RunBuildException {
                return getBuildParameters().getEnvironmentVariables();
            }
        };
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
        String restUrl = serverUrl + "/guestAuth/app/rest/builds/buildType:" + buildType + ",tag:"
                + tag + ",personal:false,count:1,status:SUCCESS";
        BuildInfoXmlResponseHandler handler = new BuildInfoXmlResponseHandler();

        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(new InputSource(new URL(restUrl).openStream()), handler);

        } catch (Exception e) {
            throw new RunBuildException("Error determining build info XML", e);
        }

        String referenceArtifactDownloadUrl = serverUrl + handler.getArtifactDownloadUrl();
        String referenceArtifactZipFile = getBuildTempDirectory().getAbsolutePath() + "/artifacts-" + tag + ".zip";

        try {
            IOUtils.saveUrl(referenceArtifactZipFile, referenceArtifactDownloadUrl);
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

    /**
     * @param searchDirectory
     * @param filePattern
     *
     * @return
     * @throws RunBuildException
     */
    private List<String> findFiles(String searchDirectory, String filePattern) throws RunBuildException {
        List<String> matchedFiles;
        try {
            matchedFiles = matchFiles(searchDirectory, filePattern);
            if (matchedFiles.size() == 0) {
                throw new RunBuildException("No files matched the pattern");
            }
        } catch (IOException e) {
            throw new RunBuildException("I/O error while collecting files", e);
        }
        return matchedFiles;
    }

    /**
     * @param filename
     * @param version
     * @param headers
     * @param libs
     * @param gccOptions
     *
     * @return
     * @throws RunBuildException
     */
    private File writeXmlDescriptor(String filename, String version, List<String> headers, List<String> libs,
                                    String gccOptions) throws RunBuildException {
        String descriptor = "" +
                "<version>" + version + "</version>" +
                "<headers>" + StringUtil.join(headers, "\n") + "</headers>" +
                "<libs>" + StringUtil.join(libs, "\n") + "</libs>" +
                "<gcc_options>" + gccOptions + "</gcc_options>";

        File xmlFile;
        try {
            xmlFile = IOUtils.writeFile(filename, descriptor);
        } catch (IOException e) {
            throw new RunBuildException("Error writing XML descriptor", e);
        }
        return xmlFile;
    }

    /**
     * Returns *absolute* path
     *
     * @param filePath
     * @param fileString
     *
     * @return
     * @throws IOException
     */
    private List<String> matchFiles(String filePath, String fileString) throws IOException {
        logger.message("Trying to match '" + fileString + "' in directory: " + filePath);

        final AntPatternFileFinder finder = new AntPatternFileFinder(splitFileWildcards(fileString),
                new String[]{},
                SystemInfo.isFileSystemCaseSensitive);
        final File[] files = finder.findFiles(new File(filePath));

        logger.message("Matched artifact files:");

        final List<String> result = new ArrayList<String>(files.length);
        for (File file : files) {
            result.add(file.getAbsolutePath());
            logger.message("  " + file);
        }

        if (files.length == 0) {
            logger.message("  none");
        }

        return result;
    }

    /**
     * @param string
     *
     * @return
     */
    private String[] splitFileWildcards(final String string) {
        if (string != null) {
            final String filesStringWithSpaces = string.replace('\n', ' ').replace('\r', ' ').replace('\\', '/');
            final List<String> split = StringUtil.splitCommandArgumentsAndUnquote(filesStringWithSpaces);
            return split.toArray(new String[split.size()]);
        }

        return new String[0];
    }

    /**
     * @param referenceArtifactsDirectory
     */
    private void setReferenceArtifactsDirectory(String referenceArtifactsDirectory) {
        this.referenceArtifactsDirectory = referenceArtifactsDirectory;
    }

    /**
     * @param newArtifactsDirectory
     */
    private void setNewArtifactsDirectory(String newArtifactsDirectory) {
        this.newArtifactsDirectory = newArtifactsDirectory;
    }
}
