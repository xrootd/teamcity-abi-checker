package ch.cern.dss.teamcity.server;


import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AbiCheckerRunType extends RunType {

    private PluginDescriptor pluginDescriptor;

    public AbiCheckerRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                             @NotNull final PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
        runTypeRegistry.registerRunType(this);
    }

    @Override
    public String getType() {
        return AbiCheckerConstants.TYPE;
    }

    @Override
    public String getDisplayName() {
        return AbiCheckerConstants.DISPLAY_NAME;
    }

    @Override
    public String getDescription() {
        return AbiCheckerConstants.DESCRIPTION;
    }

    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return null;
    }

    @Override
    public String getEditRunnerParamsJspFilePath() {
        return this.pluginDescriptor.getPluginResourcesPath() + "editAbiCheckerRunner.jsp";
    }

    @Override
    public String getViewRunnerParamsJspFilePath() {
        return this.pluginDescriptor.getPluginResourcesPath() + "viewAbiCheckerRunner.jsp";
    }

    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }
}
