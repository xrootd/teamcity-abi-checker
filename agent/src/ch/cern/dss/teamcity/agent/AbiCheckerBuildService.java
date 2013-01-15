package ch.cern.dss.teamcity.agent;


import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AbiCheckerBuildService extends BuildServiceAdapter {

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final Map<String, String> runnerParameters = getRunnerParameters();

        for (Map.Entry e : runnerParameters.entrySet()) {
            Loggers.AGENT.info(e.getKey() + ": " + e.getValue());
        }

        return null;
    }
}
