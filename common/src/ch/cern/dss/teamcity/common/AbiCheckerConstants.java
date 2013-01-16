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

    String UI_PREFIX = "ui-" + TYPE + "-";
    String UI_PROJECT_NAME = UI_PREFIX + "project-name";
    String UI_BUILD_TYPE = UI_PREFIX + "build-type";
    String UI_REFERENCE_TAG = UI_PREFIX + "reference-tag";
    String UI_ABI_CHECKER_EXECUTABLE_PATH = UI_PREFIX + "executable-path";
    String UI_ARTIFACT_FILES = UI_PREFIX + "artifact-files";
    String UI_ARTIFACT_TYPE = UI_PREFIX + "artifact-type";
    String UI_ARTIFACT_HEADER_PATH = UI_PREFIX + "artifact-header-path";
    String UI_ARTIFACT_LIBRARY_PATH = UI_PREFIX + "artifact-library-path";

    String UI_ARTIFACT_TYPE_RPM = UI_ARTIFACT_TYPE + "-rpm";
    String UI_ARTIFACT_TYPE_ARCHIVE = UI_ARTIFACT_TYPE + "-archive";
    String UI_ARTIFACT_TYPE_FOLDER = UI_ARTIFACT_TYPE + "-folder";
}
