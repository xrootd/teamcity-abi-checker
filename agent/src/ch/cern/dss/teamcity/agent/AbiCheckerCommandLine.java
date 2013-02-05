package ch.cern.dss.teamcity.agent;

import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.IOUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbiCheckerCommandLine implements ProgramCommandLine {

    protected final SimpleLogger logger;
    protected final AbiCheckerContext context;

    public AbiCheckerCommandLine(AbiCheckerContext context, SimpleLogger logger) {
        this.context = context;
        this.logger = logger;
    }

    @NotNull
    @Override
    public abstract String getExecutablePath() throws RunBuildException;

    @NotNull
    @Override
    public abstract String getWorkingDirectory() throws RunBuildException;

    @NotNull
    @Override
    public abstract List<String> getArguments() throws RunBuildException;

    @NotNull
    @Override
    public abstract Map<String, String> getEnvironment() throws RunBuildException;

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
    protected File writeXmlDescriptor(String filename, String version, List<String> headers, List<String> libs,
                                      String gccOptions) throws RunBuildException {
        String descriptor = "" +
                "<version>" + version + "</version>" +
                "<headers>" + StringUtil.join(headers, "\n") + "</headers>" +
                "<libs>" + StringUtil.join(libs, "\n") + "</libs>" +
                "<gcc_options>" + gccOptions + "</gcc_options>";

        File xmlFile;
        try {
            xmlFile = IOUtil.writeFile(filename, descriptor);
        } catch (IOException e) {
            throw new RunBuildException("Error writing XML descriptor", e);
        }
        return xmlFile;
    }
}
