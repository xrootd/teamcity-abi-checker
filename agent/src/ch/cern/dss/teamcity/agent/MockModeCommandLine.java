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

package ch.cern.dss.teamcity.agent;

import ch.cern.dss.teamcity.agent.util.FileUtil;
import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import ch.cern.dss.teamcity.common.IOUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Class to build command-line parameters when running in mock build mode.
 */
public class MockModeCommandLine extends AbiCheckerCommandLine {

    private final MockEnvironmentBuilder mockEnvironmentBuilder;

    /**
     * @param context the context parameters object.
     * @param logger  the build progress logger to use.
     *
     * @throws RunBuildException to break the build.
     */
    public MockModeCommandLine(AbiCheckerContext context, SimpleLogger logger) throws RunBuildException {
        super(context, logger);

        logger.message("Setting up mock context");
        mockEnvironmentBuilder = new MockEnvironmentBuilder(context, logger);
        mockEnvironmentBuilder.setup();

        // Write xml descriptor for each chroot
        for (String chroot : mockEnvironmentBuilder.getChroots()) {

            List<String> matchedReferenceHeaderFiles = FileUtil.findFiles(
                    context.getReferenceArtifactsDirectory() + "/" + chroot, context.getHeaderFilePattern());
            List<String> matchedReferenceLibraryFiles = FileUtil.findFiles(
                    context.getReferenceArtifactsDirectory() + "/" + chroot, context.getLibraryFilePattern());
            List<String> matchedNewHeaderFiles = FileUtil.findFiles(
                    context.getNewExtractedArtifactsDirectory() + "/" + chroot, context.getHeaderFilePattern());
            List<String> matchedNewLibraryFiles = FileUtil.findFiles(
                    context.getNewExtractedArtifactsDirectory() + "/" + chroot, context.getLibraryFilePattern());

            // Write the XML files
            writeXmlDescriptor(context.getReferenceXmlFilename(chroot), context.getReferenceXmlVersion(),
                    matchedReferenceHeaderFiles, matchedReferenceLibraryFiles, context.getGccOptions());

            writeXmlDescriptor(context.getNewXmlFilename(chroot), context.getNewXmlVersion(), matchedNewHeaderFiles,
                    matchedNewLibraryFiles, context.getGccOptions());

            context.setMatchedLibraryFiles(matchedReferenceLibraryFiles);
        }
    }

    /**
     * @return the path to the executable to run in this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    @Override
    public String getExecutablePath() throws RunBuildException {
        return "/bin/bash";
    }

    /**
     * @return the directory to work in in this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    @Override
    public String getWorkingDirectory() throws RunBuildException {
        return context.getWorkingDirectory().getAbsolutePath();
    }

    /**
     * @return the command-line arguments to use for this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    @Override
    public List<String> getArguments() throws RunBuildException {
        List<String> arguments = new Vector<String>();
        StringBuilder command = new StringBuilder();

        try {
            FileUtils.copyFile(new File(AbiCheckerConstants.MOCK_CONFIG_DIRECTORY, "site-defaults.cfg"),
                    new File(context.getWorkingDirectory().getAbsolutePath(), "site-defaults.cfg"));
            FileUtils.copyFile(new File(AbiCheckerConstants.MOCK_CONFIG_DIRECTORY, "logging.ini"),
                    new File(context.getWorkingDirectory().getAbsolutePath(), "logging.ini"));
        } catch (IOException e) {
            throw new RunBuildException("Error reading mock site-defaults.cfg", e);
        }

        for (String chroot : mockEnvironmentBuilder.getChroots()) {
            String mockConfig;

            try {
                mockConfig = IOUtil.readFile(
                        new File(AbiCheckerConstants.MOCK_CONFIG_DIRECTORY, chroot + ".cfg").getAbsolutePath());
            } catch (IOException e) {
                throw new RunBuildException("Error reading mock config: " + chroot, e);
            }

            mockConfig += "\nconfig_opts['plugin_conf']['bind_mount_enable'] = True\n";
            mockConfig += "config_opts['plugin_conf']['root_cache_opts']['age_check'] = False\n";
            mockConfig += "config_opts['plugin_conf']['bind_mount_opts']['create_dirs'] = True\n";
            mockConfig += "config_opts['plugin_conf']['bind_mount_opts']['dirs'].append(('"
                    + context.getBuildTempDirectory() + "', '" + context.getBuildTempDirectory() + "' ))\n";

            try {
                IOUtil.writeFile(new File(context.getWorkingDirectory().getAbsolutePath(), chroot + ".cfg")
                        .getAbsolutePath(), mockConfig);
            } catch (IOException e) {
                throw new RunBuildException("Error writing mock config: " + chroot, e);
            }

            command.append(AbiCheckerConstants.MOCK_EXECUTABLE)
                    .append(" --configdir=").append(context.getWorkingDirectory().getAbsolutePath())
                    .append(" -r ").append(chroot)
                    .append(" -q ")
                    .append(" --cache-alterations ")
                    .append(" --install abi-compliance-checker ctags\n");

            command.append(AbiCheckerConstants.MOCK_EXECUTABLE)
                    .append(" --configdir=").append(context.getWorkingDirectory().getAbsolutePath())
                    .append(" -r ").append(chroot)
                    .append(" --chroot '")
                    .append(context.getAbiCheckerExecutablePath())
                    .append(" -show-retval")
                    .append(" -lib ").append(StringUtil.join(context.getLibNames(), ", "))
                    .append(" -component ").append(context.getLibNames().size() > 1 ? "libraries" : "library")
                    .append(" -old ").append(context.getReferenceXmlFilename(chroot))
                    .append(" -new ").append(context.getNewXmlFilename(chroot))
                    .append(" -binary -bin-report-path ").append(context.getNewArtifactsDirectory())
                    .append("/").append(chroot).append(AbiCheckerConstants.REPORT_DIRECTORY)
                    .append(AbiCheckerConstants.ABI_REPORT)
                    .append(" -source -src-report-path ").append(context.getNewArtifactsDirectory())
                    .append("/").append(chroot).append(AbiCheckerConstants.REPORT_DIRECTORY)
                    .append(AbiCheckerConstants.SRC_REPORT)
                    .append(" -log-path ").append(context.getNewArtifactsDirectory())
                    .append("/").append(chroot).append(AbiCheckerConstants.REPORT_DIRECTORY)
                    .append("log.txt")
                    .append(" -report-format html")
                    .append(" -logging-mode w'\n");
        }

        File mockScriptFile = new File(context.getWorkingDirectory(), "mock-install.sh");
        try {
            IOUtil.writeFile(mockScriptFile.getAbsolutePath(), command.toString());
        } catch (IOException e) {
            throw new RunBuildException("Error writing mock script", e);
        }

        arguments.add(mockScriptFile.getAbsolutePath());
        logger.message("Arguments: " + StringUtil.join(arguments, " "));
        return arguments;
    }

    /**
     * @return the map of environment variables to use in this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    @Override
    public Map<String, String> getEnvironment() throws RunBuildException {
        return context.getEnvironment();
    }
}
