/**
 * Copyright (c) 2012-2013 by European Organization for Nuclear Research (CERN)
 * Author: Justin Salmon <jsalmon@cern.ch>
 *
 * XRootD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * XRootD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with XRootD.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.cern.dss.teamcity.server;

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AbiCheckerRunType extends RunType {

    private PluginDescriptor pluginDescriptor;
    private ProjectManager projectManager;

    public AbiCheckerRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                             @NotNull final PluginDescriptor pluginDescriptor,
                             @NotNull final ProjectManager projectManager) {
        this.pluginDescriptor = pluginDescriptor;
        this.projectManager = projectManager;
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

        sb.append("Reference build type: ").append(projectManager.findBuildTypeById(
                parameters.get(AbiCheckerConstants.UI_BUILD_TYPE)).getName()).append("\n");
        sb.append("Reference tag: ").append(parameters.get(AbiCheckerConstants.UI_REFERENCE_TAG)).append("\n");
        sb.append("Executable path: ").append(
                parameters.get(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH)).append("\n");
        sb.append("Artifact files: ").append(parameters.get(AbiCheckerConstants.UI_ARTIFACT_FILES)).append("\n");
        sb.append("Artifact type: ").append(parameters.get(AbiCheckerConstants.UI_ARTIFACT_TYPE)).append("\n");
        sb.append("Header path: ").append(parameters.get(AbiCheckerConstants.UI_ARTIFACT_HEADER_PATH)).append("\n");
        sb.append("Shared library path: ")
                .append(parameters.get(AbiCheckerConstants.UI_ARTIFACT_LIBRARY_PATH)).append("\n");

        return sb.toString();
    }

}
