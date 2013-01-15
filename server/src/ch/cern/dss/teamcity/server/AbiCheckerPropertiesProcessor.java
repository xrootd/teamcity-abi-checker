package ch.cern.dss.teamcity.server;

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;

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

        if (PropertiesUtil.isEmptyOrNull(properties.get(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH))) {
            result.add(new InvalidProperty(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH,
                    "Path to abi-compliance-checker executable must be specified "));
        }

        return result;
    }
}
