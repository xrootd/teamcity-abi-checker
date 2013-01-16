package ch.cern.dss.teamcity.server;

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


public class AbiCheckerPropertiesProcessor implements PropertiesProcessor {
    @Override
    public Collection<InvalidProperty> process(Map<String, String> properties) {
        final Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();

        if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.UI_REFERENCE_TAG))) {
            result.add(new InvalidProperty(AbiCheckerConstants.UI_REFERENCE_TAG,
                    "Cannot reference a project with no tagged builds"));
        }

        String executablePath = properties.get(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH);
        if (PropertiesUtil.isEmptyOrNull(executablePath)) {
            result.add(new InvalidProperty(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH,
                    "Path to abi-compliance-checker executable must be specified"));
        } else {
            File executableFile = new File(executablePath);
            if (!executableFile.exists() || !executableFile.canExecute()) {
                result.add(new InvalidProperty(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH,
                        "The given path doesn't exist, or is not executable"));
            }
        }

        if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.UI_ARTIFACT_FILES))) {
            result.add(new InvalidProperty(AbiCheckerConstants.UI_ARTIFACT_FILES,
                    "At least one artifact must be specified"));
        }

        if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.UI_ARTIFACT_HEADER_PATH))) {
            result.add(new InvalidProperty(AbiCheckerConstants.UI_ARTIFACT_HEADER_PATH,
                    "Path to at least one header inside artifact must be specified"));
        }

        if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.UI_ARTIFACT_LIBRARY_PATH))) {
            result.add(new InvalidProperty(AbiCheckerConstants.UI_ARTIFACT_LIBRARY_PATH,
                    "Path to at least one library inside artifact must be specified"));
        }

        return result;
    }
}
