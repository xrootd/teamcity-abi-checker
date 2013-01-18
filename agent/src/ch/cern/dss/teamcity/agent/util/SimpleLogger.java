package ch.cern.dss.teamcity.agent.util;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

public class SimpleLogger extends LoggerAdapter {
    @NotNull
    private final BuildProgressLogger logger;

    public SimpleLogger(@NotNull final BuildProgressLogger buildLogger) {
        logger = buildLogger;
    }

    @Override
    public void message(@NotNull final String message) {
        logger.message(message);
    }

    @Override
    public void error(@NotNull final String message) {
        logger.error(message);
    }

    @Override
    public void warning(@NotNull final String message) {
        logger.warning(message);
    }

    @Override
    public void blockStart(@NotNull final String name) {
        logger.targetStarted(name);
    }

    @Override
    public void blockFinish(@NotNull final String name) {
        logger.targetFinished(name);
    }
}
