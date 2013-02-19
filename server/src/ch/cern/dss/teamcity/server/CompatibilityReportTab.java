/**
 * Copyright (c) 2012-2013 by European Organization for Nuclear Research (CERN)
 * Author: Justin Salmon <jsalmon@cern.ch>
 *
 * This file is part of the ABI Compatibility Checker (ACC) TeamCity plugin.
 *
 * ACC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ACC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ACC.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.cern.dss.teamcity.server;

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import ch.cern.dss.teamcity.common.IOUtil;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import jetbrains.buildServer.web.reportTabs.ReportTabUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class CompatibilityReportTab extends ViewLogTab {

    /**
     * @param pagePlaces       the object with which we register this page extension.
     * @param server           the build server.
     * @param pluginDescriptor the plugin descriptor.
     */
    public CompatibilityReportTab(@NotNull PagePlaces pagePlaces,
                                  @NotNull SBuildServer server,
                                  @NotNull PluginDescriptor pluginDescriptor) {
        super(AbiCheckerConstants.TAB_TITLE, AbiCheckerConstants.TAB_ID, pagePlaces, server);
        addJsFile(pluginDescriptor.getPluginResourcesPath("js/jquery.easytabs.min.js"));
        addCssFile(pluginDescriptor.getPluginResourcesPath("css/custom.css"));
        setIncludeUrl(pluginDescriptor.getPluginResourcesPath() + "compatibilityReport.jsp");
    }

    /**
     * Called when the user clicks on the custom report tab.
     *
     * @param model   the map of data objects that will be passed to the JSP page.
     * @param request the HTTP request object.
     * @param build   the current build.
     */
    @Override
    public void fillModel(@NotNull Map<String, Object> model,
                          @NotNull HttpServletRequest request,
                          @NotNull SBuild build) {
        try {
            model.put("reportPages", getReportPages(build));
            model.put("buildMode", build.getBuildType().findBuildRunnerByType(AbiCheckerConstants.TYPE)
                    .getParameters().get(AbiCheckerConstants.BUILD_MODE));
        } catch (IOException e) {
            Loggers.SERVER.error("Error filling report tab model: " + e.getMessage());
        }
    }

    /**
     * Get the appropriate report pages based on the current build mode.
     *
     * @param build the current build.
     *
     * @return map of report pages.
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
     * Perform checks to see whether this page is available to be displayed or not.
     *
     * @param request the HTTP request object.
     * @param build   the current build.
     *
     * @return true if the page is available, false otherwise.
     */
    @Override
    protected boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
        SBuildRunnerDescriptor descriptor = build.getBuildType().findBuildRunnerByType(AbiCheckerConstants.TYPE);
        if (descriptor == null) return false;

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
                    Loggers.SERVER.error(">>>>>> report pages map null or empty");
                    return false;
                }

                for (String chroot : mockReportPages.keySet()) {
                    if (!ReportTabUtil.isAvailable(build,
                            chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.ABI_REPORT)
                            ||
                            !ReportTabUtil.isAvailable(build,
                                    chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.SRC_REPORT)) {
                        Loggers.SERVER.error(">>>>>> report pages not available");
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
     * Get report pages for mock build mode. These pages will need to go inside sub-tabs in the web UI, hence the nested
     * maps.
     *
     * @param build the current buiild.
     *
     * @return the map of mock report pages.
     * @throws IOException
     */
    @Nullable
    private Map<String, Map.Entry<String, String>> getMockReportPages(SBuild build) throws IOException {
        Map<String, Map.Entry<String, String>> reportPages = new HashMap<String, Map.Entry<String, String>>();

        // Grab the chroots
        List<String> chroots = getChroots(build.getArtifactsDirectory());
        Loggers.SERVER.info(">>>>> chroots: " + Arrays.toString(chroots.toArray()));

        // Check if we have a folder with the chroot name and the report file is inside
        for (String chroot : chroots) {
            File abiReportPage = new File(build.getArtifactsDirectory(),
                    chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.ABI_REPORT);
            File srcReportPage = new File(build.getArtifactsDirectory(),
                    chroot + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.SRC_REPORT);
            if (abiReportPage.exists() && srcReportPage.exists()) {
                Loggers.SERVER.info("+++++ pages exist for " + chroot);

                Map.Entry<String, String> pages = new AbstractMap.SimpleEntry<String, String>
                        (IOUtil.readFile(abiReportPage.getAbsolutePath()),
                                IOUtil.readFile(srcReportPage.getAbsolutePath()));
                reportPages.put(chroot, pages);
            }
        }

        return reportPages;
    }

    /**
     * Parse a list of chroots to use by finding the manifest files in the artifacts.
     *
     * @param artifactsDirectory
     *
     * @return a list of chroots to use.
     */
    private List<String> getChroots(File artifactsDirectory) {

        List<String> chroots = new ArrayList<String>();
        File[] artifacts = artifactsDirectory.listFiles();

        if (artifacts != null && artifacts.length > 0) {

            for (File file : artifacts) {
                Loggers.SERVER.info(">>>>> artifact: " + file.getAbsolutePath());
                if (file.isDirectory()) {
                    Loggers.SERVER.info(">>>>> directory: " + file.getAbsolutePath());
                    File[] manifestFile = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File directory, String name) {
                            return name.equals(AbiCheckerConstants.MOCK_META_FILE);
                        }
                    });

                    if (manifestFile != null && manifestFile.length == 1) {

                        Loggers.SERVER.info(">>>>> found: " + manifestFile[0]);

                        String manifestFileContents = "";
                        try {
                            manifestFileContents = IOUtil.readFile(manifestFile[0].getAbsolutePath());
                        } catch (IOException e) {
                            Loggers.SERVER.error("Unable to read manifest file: " + manifestFile[0].getAbsolutePath());
                        }

                        if (!manifestFileContents.startsWith("chroot=")) {
                            Loggers.SERVER.error("Unable to parse manifest file: invalid file format: "
                                    + manifestFileContents);
                        }

                        chroots.add(StringUtil.split(manifestFileContents, "=").get(1));
                    }
                }
            }

        } else {
            Loggers.SERVER.error("Build contains no artifacts");
        }

        return chroots;
    }

}
