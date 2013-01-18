package ch.cern.dss.teamcity.agent.util;

import org.jetbrains.annotations.NotNull;

public interface Logger {

  void message(@NotNull String message);

  void error(@NotNull String message);

  void warning(@NotNull String message);

  void blockStart(@NotNull String name);

  void blockFinish(@NotNull String name);

}
