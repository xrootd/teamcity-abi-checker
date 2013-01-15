package ch.cern.dss.teamcity.agent;


import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbiCheckerBuildService extends BuildServiceAdapter {

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final List<String> arguments = new ArrayList<String>();
        final Map<String, String> runnerParameters = getRunnerParameters();
        final Map<String, String> environment = new HashMap<String, String>(System.getenv());
        environment.putAll(getBuildParameters().getEnvironmentVariables());

        for (Map.Entry e : runnerParameters.entrySet()) {
            Loggers.AGENT.info(e.getKey() + ": " + e.getValue());
        }

        final SimpleProgramCommandLine commandLine = new SimpleProgramCommandLine(environment,
                getWorkingDirectory().getAbsolutePath(), "abi-compliance-checker", arguments);
        return commandLine;
    }
}
