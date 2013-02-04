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
import java.util.AbstractMap;
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
        addJsFile(pluginDescriptor.getPluginResourcesPath("js/jquery.easytabs.min.js"));
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
    private Map<String, Map.Entry<String, String>> getReportPages(SBuild build) throws IOException {
        SBuildRunnerDescriptor descriptor = build.getBuildType().findBuildRunnerByType(AbiCheckerConstants.TYPE);
        Map<String, String> runnerParameters = descriptor.getParameters();

        CompatibilityReportPageBuilder builder;
        Map<String, Map.Entry<String, String>> reportPages;

        if (runnerParameters.get(AbiCheckerConstants.BUILD_MODE).equals(AbiCheckerConstants.BUILD_MODE_NORMAL)) {
            File abiReportPage = new File(build.getArtifactsDirectory(),
                    AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.ABI_REPORT);
            File srcReportPage = new File(build.getArtifactsDirectory(),
                    AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.SRC_REPORT);

            builder = new CompatibilityReportPageBuilder(new AbstractMap.SimpleEntry<String, String>
                    (IOUtil.readFile(abiReportPage.getAbsolutePath()),
                            IOUtil.readFile(srcReportPage.getAbsolutePath())));
            reportPages = builder.getReportPages();

        } else {
            Map<String, Map.Entry<String, String>> mockReportPages = getMockReportPages(build);
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
            return super.isAvailable(request, build)
                    && ReportTabUtil.isAvailable(build,
                    AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.ABI_REPORT)
                    && ReportTabUtil.isAvailable(build,
                    AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.SRC_REPORT);
        } else {
            try {
                Map<String, Map.Entry<String, String>> mockReportPages = getMockReportPages(build);
                if (mockReportPages == null || mockReportPages.isEmpty()) {
                    return false;
                }

                for (String chroot : mockReportPages.keySet()) {
                    if (!ReportTabUtil.isAvailable(build,
                            chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.ABI_REPORT)
                            ||
                            !ReportTabUtil.isAvailable(build,
                                    chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.SRC_REPORT)) {
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
     * @param build
     *
     * @return
     * @throws IOException
     */
    @Nullable
    private Map<String, Map.Entry<String, String>> getMockReportPages(SBuild build) throws IOException {
        Map<String, Map.Entry<String, String>> reportPages = new HashMap<String, Map.Entry<String, String>>();

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
            File abiReportPage = new File(build.getArtifactsDirectory(),
                    chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.ABI_REPORT);
            File srcReportPage = new File(build.getArtifactsDirectory(),
                    chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.SRC_REPORT);
            if (abiReportPage.exists() && srcReportPage.exists()) {
                Map.Entry<String, String> pages = new AbstractMap.SimpleEntry<String, String>
                        (IOUtil.readFile(abiReportPage.getAbsolutePath()),
                                IOUtil.readFile(srcReportPage.getAbsolutePath()));
                reportPages.put(chroot, pages);
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
