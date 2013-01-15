package ch.cern.dss.teamcity.server;


import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.BuildTypeDescriptor;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AbiCheckerRunType extends RunType {

    private PluginDescriptor pluginDescriptor;
    private ProjectManager projectManager;
    private BuildTypeDescriptor buildType;

    public AbiCheckerRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                             @NotNull final PluginDescriptor pluginDescriptor,
                             @NotNull final ProjectManager projectManager,
                             @NotNull final WebControllerManager webControllerManager) {
        this.pluginDescriptor = pluginDescriptor;
        this.projectManager = projectManager;
        this.buildType = buildType;
        runTypeRegistry.registerRunType(this);

        for (RunType r : runTypeRegistry.getRegisteredRunTypes()) {
            Loggers.SERVER.info(r.getDisplayName());
        }
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
        return new AbiCheckerPropertiesProcessor();
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
        Map defaults = new HashMap<String, String>();
        return defaults;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();

        sb.append("Reference build type: ").append(parameters.get(AbiCheckerConstants.UI_BUILD_TYPE)).append("\n");
        sb.append("Reference tag: ").append(parameters.get(AbiCheckerConstants.UI_REFERENCE_TAG)).append("\n");
        sb.append("Executable path: ").append(
                parameters.get(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH)).append("\n");

        if (!PropertiesUtil.isEmptyOrNull(parameters.get(AbiCheckerConstants.UI_ARTIFACT_PATH))) {
            sb.append("Artifact path: ").append(parameters.get(AbiCheckerConstants.UI_ARTIFACT_PATH)).append("\n");
        }
        return sb.toString();
    }

}
