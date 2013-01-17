package ch.cern.dss.teamcity.agent;


import ch.cern.dss.teamcity.agent.util.ArchiveExtractor;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.util.AntPatternFileFinder;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AbiCheckerBuildService extends BuildServiceAdapter {

    private ArchiveExtractor archiveExtractor;

    public AbiCheckerBuildService() {
        archiveExtractor = new ArchiveExtractor();
    }

    private static String[] splitFileWildcards(final String string) {
        if (string != null) {
            final String filesStringWithSpaces = string.replace('\n', ' ').replace('\r', ' ').replace('\\', '/');
            final List<String> split = StringUtil.splitCommandArgumentsAndUnquote(filesStringWithSpaces);
            return split.toArray(new String[split.size()]);
        }

        return new String[0];
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final List<String> arguments = new ArrayList<String>();
        final Map<String, String> runnerParameters = getRunnerParameters();
        final Map<String, String> environment = new HashMap<String, String>(System.getenv());
        environment.putAll(getBuildParameters().getEnvironmentVariables());

        String referenceBuildType = runnerParameters.get(AbiCheckerConstants.UI_BUILD_TYPE);
        String referenceTag = runnerParameters.get(AbiCheckerConstants.UI_REFERENCE_TAG);
        String executablePath = runnerParameters.get(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH);
        String referenceArtifactFiles = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_FILES);
        String referenceArtifactType = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_TYPE);
        String headerPath = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_HEADER_PATH);
        String libraryPath = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_LIBRARY_PATH);

        //--------------------------------------------------------------------------------------------------------------
        // Download reference artifact zip file
        //--------------------------------------------------------------------------------------------------------------

        String serverUrl = getAgentConfiguration().getServerUrl();
        String restUrl = serverUrl + "/guestAuth/app/rest/builds/buildType:" + referenceBuildType + ",tag:"
                + referenceTag + ",personal:false,count:1,status:SUCCESS";
        Loggers.AGENT.info(">>>>>>> REST url: " + restUrl);

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        BuildInfoXmlResponseHandler handler;
        String artifactDownloadUrl = "";

        try {
            saxParser = saxParserFactory.newSAXParser();
            handler = new BuildInfoXmlResponseHandler();
            saxParser.parse(new InputSource(new URL(restUrl).openStream()), handler);
            artifactDownloadUrl = serverUrl + handler.getArtifactDownloadUrl();
        } catch (ParserConfigurationException e) {
            Loggers.AGENT.error("Error configuring SAX parser: " + e.getMessage());
        } catch (SAXException e) {
            Loggers.AGENT.error("Error parsing XML: " + e.getMessage());
        } catch (MalformedURLException e) {
            Loggers.AGENT.error("Error retrieving XML: " + e.getMessage());
        } catch (IOException e) {
            Loggers.AGENT.error("Error: " + e.getMessage());
        }

        Loggers.AGENT.info("Artifact download URL: " + artifactDownloadUrl);

        String referenceArtifactFolder = getWorkingDirectory().getAbsolutePath() + "/artifacts-" + referenceTag;
        String referenceArtifactFilename = getWorkingDirectory().getAbsolutePath() + "/artifacts-" + referenceTag + ".zip";
        Loggers.AGENT.info("Artifacts will be d/led to: " + referenceArtifactFilename);

        try {
            saveUrl(referenceArtifactFilename, artifactDownloadUrl);
        } catch (IOException e) {
            Loggers.AGENT.error("Error downloading artifacts: " + e.getMessage());
        }

        //--------------------------------------------------------------------------------------------------------------
        // Extract downloaded artifact zip
        //--------------------------------------------------------------------------------------------------------------
        archiveExtractor.extract(referenceArtifactFilename, referenceArtifactFolder, "zip");

        //--------------------------------------------------------------------------------------------------------------
        // Find the artifact files
        //--------------------------------------------------------------------------------------------------------------
        List<String> files = new ArrayList<String>();

        try {
            String matchFilePath = referenceArtifactFolder + File.separator + referenceArtifactFiles;
            Loggers.AGENT.info("Trying to match files: " + matchFilePath);
            files = matchFiles(referenceArtifactFolder + File.separator + referenceArtifactFiles);
        } catch (IOException e) {
            throw new RunBuildException("I/O error while collecting files", e);
        }

        if (files.size() == 0) {
            throw new RunBuildException("No files matched the pattern");
        }


        //--------------------------------------------------------------------------------------------------------------
        // Extract the artifact files if necessary
        //--------------------------------------------------------------------------------------------------------------



        //--------------------------------------------------------------------------------------------------------------
        // Find the header and library files
        //--------------------------------------------------------------------------------------------------------------



        //--------------------------------------------------------------------------------------------------------------
        // Write the XML files
        //--------------------------------------------------------------------------------------------------------------

        final SimpleProgramCommandLine commandLine = new SimpleProgramCommandLine(environment,
                getWorkingDirectory().getAbsolutePath(), executablePath, arguments);
        return commandLine;
    }

    public void saveUrl(String filename, String urlString) throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            out = new FileOutputStream(filename);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
            }
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    private List<String> matchFiles(String fileString) throws IOException {
        final Map<String, String> runParameters = getRunnerParameters();

        final AntPatternFileFinder finder = new AntPatternFileFinder(splitFileWildcards(fileString),
                new String[]{},
                SystemInfo.isFileSystemCaseSensitive);
        final File[] files = finder.findFiles(getCheckoutDirectory());

        getLogger().logMessage(DefaultMessagesInfo.createTextMessage("Matched artifact files:"));

        final List<String> result = new ArrayList<String>(files.length);
        for (File file : files) {
            final String relativeName = FileUtil.getRelativePath(getWorkingDirectory(), file);

            result.add(relativeName);
            getLogger().logMessage(DefaultMessagesInfo.createTextMessage("  " + relativeName));
        }

        if (files.length == 0) {
            getLogger().logMessage(DefaultMessagesInfo.createTextMessage("  none"));
        }

        return result;
    }
}
