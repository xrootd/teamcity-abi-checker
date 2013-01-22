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
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import jetbrains.buildServer.web.reportTabs.ReportTabUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class CompatibilityReportTab extends ViewLogTab {

    public CompatibilityReportTab(@NotNull PagePlaces pagePlaces,
                                  @NotNull SBuildServer server,
                                  @NotNull PluginDescriptor pluginDescriptor) {
        super(AbiCheckerConstants.TAB_TITLE, AbiCheckerConstants.TAB_ID, pagePlaces, server);
        setIncludeUrl(pluginDescriptor.getPluginResourcesPath() + "compatibilityReport.jsp");
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model,
                          @NotNull HttpServletRequest request,
                          @NotNull SBuild build) {
        try {
            model.put("compatibilityReport", getReportPage(build));
        } catch (IOException e) {
            Loggers.SERVER.error("Error filling report tab model: " + e.getMessage());
        }
    }

    private String getReportPage(SBuild build) throws IOException {
        BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT);
        BuildArtifact reportPage = buildArtifacts.getArtifact(AbiCheckerConstants.REPORT_FILE);
        String s = IOUtils.toString(reportPage.getInputStream(), "UTF-8");
        return s;
    }

    @Override
    protected boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
        return super.isAvailable(request, build) && ReportTabUtil.isAvailable(build, AbiCheckerConstants.REPORT_FILE);
    }

}
