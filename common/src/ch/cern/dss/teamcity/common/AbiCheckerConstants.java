package ch.cern.dss.teamcity.common;


import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public interface AbiCheckerConstants {
    @NotNull
    @NonNls
    String TYPE = "abi-checker";
    @NotNull
    @NonNls
    String DESCRIPTION = "Checking binary compatibility of new shared libraries with previous versions";
    @NotNull
    @NonNls
    String DISPLAY_NAME = "ABI Compatibility Checker";

    @NotNull
    @NonNls
    String UI_PREFIX = "ui-" + TYPE + "-";

    @NotNull
    @NonNls
    String UI_PROJECT_NAME = UI_PREFIX + "project-name";
    @NotNull
    @NonNls
    String UI_BUILD_TYPE = UI_PREFIX + "build-type";
    @NotNull
    @NonNls
    String UI_REFERENCE_TAG = UI_PREFIX + "reference-tag";
    @NotNull
    @NonNls
    String UI_ARTIFACT_PATH = TYPE + "-artifact-path";
}
