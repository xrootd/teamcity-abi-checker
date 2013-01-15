package ch.cern.dss.teamcity.server;


import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.BuildTypeDescriptor;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.StatefulObject;
import jetbrains.buildServer.controllers.admin.projects.BuildTypeForm;
import jetbrains.buildServer.controllers.admin.projects.EditRunTypeControllerExtension;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.tags.TagsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
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
        Map defaults = new HashMap<String, String>();
        return defaults;
    }

}
