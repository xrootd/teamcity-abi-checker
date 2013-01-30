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
import ch.cern.dss.teamcity.common.IOUtil;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
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
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CompatibilityReportTab extends ViewLogTab {

    /**
     * @param pagePlaces
     * @param server
     * @param pluginDescriptor
     */
    public CompatibilityReportTab(@NotNull PagePlaces pagePlaces,
                                  @NotNull SBuildServer server,
                                  @NotNull PluginDescriptor pluginDescriptor) {
        super(AbiCheckerConstants.TAB_TITLE, AbiCheckerConstants.TAB_ID, pagePlaces, server);
        addJsFile(pluginDescriptor.getPluginResourcesPath("simpletabs-1.3.packed.js"));
        setIncludeUrl(pluginDescriptor.getPluginResourcesPath() + "compatibilityReport.jsp");
    }

    /**
     * @param model
     * @param request
     * @param build
     */
    @Override
    public void fillModel(@NotNull Map<String, Object> model,
                          @NotNull HttpServletRequest request,
                          @NotNull SBuild build) {
        try {
            model.put("reportPages", getReportPages(build));
        } catch (IOException e) {
            Loggers.SERVER.error("Error filling report tab model: " + e.getMessage());
        }
    }

    /**
     * @param build
     *
     * @return
     * @throws IOException
     */
    private Map<String, String> getReportPages(SBuild build) throws IOException {
        BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT);
        SBuildRunnerDescriptor descriptor = build.getBuildType().findBuildRunnerByType(AbiCheckerConstants.TYPE);
        Map<String, String> runnerParameters = descriptor.getParameters();

        CompatibilityReportPageBuilder builder;
        Map<String, String> reportPages;

        if (runnerParameters.get(AbiCheckerConstants.BUILD_MODE).equals(AbiCheckerConstants.BUILD_MODE_NORMAL)) {
            BuildArtifact reportPage = buildArtifacts.getArtifact(AbiCheckerConstants.REPORT_FILE);
            builder = new CompatibilityReportPageBuilder(IOUtils.toString(reportPage.getInputStream(), "UTF-8"));
            reportPages = builder.getReportPages();

        } else {
            Map<String, String> mockReportPages = getMockReportPages(build);
            builder = new CompatibilityReportPageBuilder(mockReportPages);
            reportPages = builder.getReportPages();
        }

        return reportPages;
    }

    /**
     * @param request
     * @param build
     *
     * @return
     */
    @Override
    protected boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
        SBuildRunnerDescriptor descriptor = build.getBuildType().findBuildRunnerByType(AbiCheckerConstants.TYPE);
        Map<String, String> runnerParameters = descriptor.getParameters();

        if (runnerParameters.get(AbiCheckerConstants.BUILD_MODE).equals(AbiCheckerConstants.BUILD_MODE_NORMAL)) {
            return super.isAvailable(request, build) && ReportTabUtil.isAvailable(build,
                    AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.REPORT_FILE);

        } else {

            try {
                for (String chroot : getMockReportPages(build).keySet()) {
                    if (!ReportTabUtil.isAvailable(build,
                            chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.REPORT_FILE)) {
                        Loggers.SERVER.info("Page not available: " + chroot + AbiCheckerConstants.REPORT_DIRECTORY
                                + AbiCheckerConstants.REPORT_FILE);
                        return false;
                    }
                }
            } catch (IOException e) {
                Loggers.SERVER.info("Page not available: " + e.getMessage());
                return false;
            }
        }

        // Nothing bad happened, we can show the page
        return true;
    }

    /**
     *
     * @param build
     * @return
     * @throws IOException
     */
    @Nullable
    private Map<String, String> getMockReportPages(SBuild build) throws IOException {
        Map<String, String> reportPages = new HashMap<String, String>();

        // Find the meta directory
        File mockMetaDirectory = new File(build.getArtifactsDirectory(), AbiCheckerConstants.MOCK_META_DIRECTORY);
        if (!mockMetaDirectory.exists() || !mockMetaDirectory.isDirectory()) {
            return null;
        }

        // Grab the chroot names
        File metaFile = new File(mockMetaDirectory, AbiCheckerConstants.MOCK_META_FILE);
        if (!metaFile.exists() || !metaFile.isFile()) {
            return null;
        }

        // Parse and verify the chroots
        List<String> chroots;
        if ((chroots = parseChroots(metaFile)) == null) {
            return null;
        }

        // Check if we have a folder with the chroot name and the report file is inside
        for (String chroot : chroots) {
            File reportPage = new File(build.getArtifactsDirectory(),
                    chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.REPORT_FILE);
            if (reportPage.exists()) {
                reportPages.put(chroot, IOUtil.readFile(reportPage.getAbsolutePath()));
            }
        }

        return reportPages;
    }

    /**
     * @param metaFile
     *
     * @return
     */
    @Nullable
    private List<String> parseChroots(File metaFile) {
        String metaFileContents;
        try {
            metaFileContents = IOUtil.readFile(metaFile.getAbsolutePath());
        } catch (IOException e) {
            return null;
        }

        if (!metaFileContents.startsWith("chroots=")) {
            return null;
        }

        return StringUtil.split(StringUtil.split(metaFileContents, "=").get(1), ",");
    }

}
