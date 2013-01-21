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
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.util.AntPatternFileFinder;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AbiCheckerBuildService extends BuildServiceAdapter {

    private SimpleLogger logger;

    /**
     * @param string
     * @return
     */
    private static String[] splitFileWildcards(final String string) {
        if (string != null) {
            final String filesStringWithSpaces = string.replace('\n', ' ').replace('\r', ' ').replace('\\', '/');
            final List<String> split = StringUtil.splitCommandArgumentsAndUnquote(filesStringWithSpaces);
            return split.toArray(new String[split.size()]);
        }

        return new String[0];
    }

    /**
     * @return
     * @throws RunBuildException
     */
    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        this.logger = new SimpleLogger(getLogger());
        ArchiveExtractor archiveExtractor = new ArchiveExtractor(this.logger);

        final List<String> arguments = new ArrayList<String>();
        final Map<String, String> runnerParameters = getRunnerParameters();
        final Map<String, String> environment = new HashMap<String, String>(System.getenv());
        environment.putAll(getBuildParameters().getEnvironmentVariables());

        String referenceBuildType = runnerParameters.get(AbiCheckerConstants.UI_BUILD_TYPE);
        String referenceTag = runnerParameters.get(AbiCheckerConstants.UI_REFERENCE_TAG);
        String executablePath = runnerParameters.get(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH);
        String referenceArtifactFiles = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_FILES);
        String referenceArtifactType = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_TYPE);
        String headerPath = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_HEADER_FILES);
        String libraryPath = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_LIBRARY_FILES);
        String gccOptions = runnerParameters.get(AbiCheckerConstants.UI_GCC_OPTIONS);

        //--------------------------------------------------------------------------------------------------------------
        // Download reference artifact zip file
        //--------------------------------------------------------------------------------------------------------------
        String serverUrl = getAgentConfiguration().getServerUrl();
        String restUrl = serverUrl + "/guestAuth/app/rest/builds/buildType:" + referenceBuildType + ",tag:"
                + referenceTag + ",personal:false,count:1,status:SUCCESS";
        String artifactDownloadUrl;

        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            BuildInfoXmlResponseHandler handler = new BuildInfoXmlResponseHandler();
            saxParser.parse(new InputSource(new URL(restUrl).openStream()), handler);
            artifactDownloadUrl = serverUrl + handler.getArtifactDownloadUrl();
        } catch (Exception e) {
            throw new RunBuildException("Error determining build info XML", e);
        }

        String referenceArtifactFolder = getWorkingDirectory().getAbsolutePath() + "/artifacts-" + referenceTag;
        String referenceArtifactFilename = getWorkingDirectory().getAbsolutePath() + "/artifacts-" + referenceTag
                + ".zip";

        try {
            IOUtils.saveUrl(referenceArtifactFilename, artifactDownloadUrl);
        } catch (IOException e) {
            throw new RunBuildException("Error downloading artifacts", e);
        }

        //--------------------------------------------------------------------------------------------------------------
        // Extract downloaded artifact zip
        //--------------------------------------------------------------------------------------------------------------
        try {
            archiveExtractor.extract(referenceArtifactFilename, referenceArtifactFolder, "zip");
        } catch (Exception e) {
            throw new RunBuildException("Error extracting artifacts", e);
        }

        //--------------------------------------------------------------------------------------------------------------
        // Find the reference files
        //--------------------------------------------------------------------------------------------------------------
        List<String> referenceFiles;
        try {
            referenceFiles = matchFiles(referenceArtifactFolder, referenceArtifactFiles);
            if (referenceFiles.size() == 0) {
                throw new RunBuildException("No files matched the pattern");
            }
        } catch (IOException e) {
            throw new RunBuildException("I/O error while collecting files", e);
        }

        //--------------------------------------------------------------------------------------------------------------
        // Extract the reference files if necessary
        //--------------------------------------------------------------------------------------------------------------
        if (referenceArtifactType.equals(AbiCheckerConstants.UI_ARTIFACT_TYPE_RPM)
                || referenceArtifactType.equals(AbiCheckerConstants.UI_ARTIFACT_TYPE_ARCHIVE)) {

            logger.message("Extracting reference files");
            for (String file : referenceFiles) {
                String archivePath = referenceArtifactFolder + File.separator + file;
                try {
                    archiveExtractor.extract(archivePath, referenceArtifactFolder, referenceArtifactType);
                } catch (Exception e) {
                    throw new RunBuildException("Error extracting artifacts", e);
                }
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        // Find the newly built files
        //--------------------------------------------------------------------------------------------------------------
        String newArtifactFolder = getRunnerContext().getBuild().getArtifactsPaths();
        List<String> newFiles;

        try {
            newFiles = matchFiles(newArtifactFolder, referenceArtifactFiles);
            if (newFiles.size() == 0) {
                throw new RunBuildException("No files matched the pattern");
            }
        } catch (IOException e) {
            throw new RunBuildException("I/O error while collecting files", e);
        }

        //--------------------------------------------------------------------------------------------------------------
        // Extract the newly built files if necessary
        //--------------------------------------------------------------------------------------------------------------
        File newExtractedArtifactFolder = new File(newArtifactFolder + File.separator + "extracted");

        if (referenceArtifactType.equals(AbiCheckerConstants.UI_ARTIFACT_TYPE_RPM)
                || referenceArtifactType.equals(AbiCheckerConstants.UI_ARTIFACT_TYPE_ARCHIVE)) {

            logger.message("Extracting new files");
            newExtractedArtifactFolder.mkdirs();

            for (String file : newFiles) {
                String newArchivePath = getRunnerContext().getBuild().getArtifactsPaths() + File.separator + file;
                logger.message("New archive path: " + newArchivePath);
                try {
                    archiveExtractor.extract(newArchivePath, newExtractedArtifactFolder.getAbsolutePath(),
                            referenceArtifactType);
                } catch (Exception e) {
                    throw new RunBuildException("Error extracting artifacts", e);
                }
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        // Find the header and library files
        //--------------------------------------------------------------------------------------------------------------
        String referenceHeaderPath = referenceArtifactFolder + File.separator + headerPath;
        String referenceLibraryPath = referenceArtifactFolder + File.separator + libraryPath;

        String newHeaderPath = newExtractedArtifactFolder + File.separator + headerPath;
        String newLibraryPath = newExtractedArtifactFolder + File.separator + libraryPath;

        //--------------------------------------------------------------------------------------------------------------
        // Write the XML files
        //--------------------------------------------------------------------------------------------------------------
        String referenceXmlDescriptor = "" +
                "<version>" + referenceTag + "</version>" +
                "<headers>" + referenceHeaderPath + "</headers>" +
                "<libs>" + referenceLibraryPath + "</libs>" +
                "<gcc_options>" + gccOptions + "</gcc_options>";
        String newXmlDescriptor = "" +
                "<version>" + getRunnerContext().getBuild().getBuildNumber() + "</version>" +
                "<headers>" + newHeaderPath + "</headers>" +
                "<libs>" + newLibraryPath + "</libs>" +
                "<gcc_options>" + gccOptions + "</gcc_options>";

        logger.message("ref XML: " + referenceXmlDescriptor);
        logger.message("new XML: " + newXmlDescriptor);

        // Write these to files...
        File referenceXmlFile = new File(getWorkingDirectory() + File.separator + referenceTag + ".xml");
        File newXmlFile = new File(getWorkingDirectory() + File.separator +
                getRunnerContext().getBuild().getBuildNumber() + ".xml");
        try {
            IOUtils.writeFile(referenceXmlFile, referenceXmlDescriptor);
            IOUtils.writeFile(newXmlFile, newXmlDescriptor);
        } catch (IOException e) {
            throw new RunBuildException("Error writing XML descriptors", e);
        }

        //--------------------------------------------------------------------------------------------------------------
        // Add the arguments
        //--------------------------------------------------------------------------------------------------------------
        arguments.add("-show-retval");

        arguments.add("-lib");
        arguments.add(referenceBuildType);

        arguments.add("-old");
        arguments.add(referenceXmlFile.getAbsolutePath());

        arguments.add("-new");
        arguments.add(newXmlFile.getAbsolutePath());

        //--------------------------------------------------------------------------------------------------------------
        // Run the comparison
        //--------------------------------------------------------------------------------------------------------------
        final SimpleProgramCommandLine commandLine = new SimpleProgramCommandLine(environment,
                getWorkingDirectory().getAbsolutePath(), executablePath, arguments);
        return commandLine;
    }

    /**
     * @param filePath
     * @param fileString
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
            final String relativeName = FileUtil.getRelativePath(new File(filePath), file);

            result.add(relativeName);
            logger.message("  " + relativeName);
        }

        if (files.length == 0) {
            logger.message("  none");
        }

        return result;
    }
}
