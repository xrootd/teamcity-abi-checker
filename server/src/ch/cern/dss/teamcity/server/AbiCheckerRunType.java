package ch.cern.dss.teamcity.server;


import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.BuildTypeDescriptor;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.tags.TagsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbiCheckerRunType extends RunType {

    private PluginDescriptor pluginDescriptor;
    private ProjectManager projectManager;
    private BuildTypeDescriptor buildType;

    public AbiCheckerRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                             @NotNull final PluginDescriptor pluginDescriptor,
                             @NotNull final ProjectManager projectManager) {
        this.pluginDescriptor = pluginDescriptor;
        this.projectManager = projectManager;
        this.buildType = buildType;
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
        Map<String, String> defaults = new HashMap<String, String>();

        List<SProject> projectList = this.projectManager.getProjects();

        for (SProject project : projectList) {
            defaults.put(AbiCheckerConstants.UI_PROJECT_NAME + project.getName(), project.getName());

            for (SBuildType buildType : project.getBuildTypes()) {
                defaults.put(AbiCheckerConstants.UI_BUILD_TYPE + buildType.getName(), buildType.getName());

                for (String tag : buildType.getTags()) {
                    defaults.put(AbiCheckerConstants.UI_REFERENCE_TAG + tag, tag);
                }
            }
        }

        return defaults;
    }


}
