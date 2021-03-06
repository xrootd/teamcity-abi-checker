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
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Class to build command-line parameters when running in normal build mode.
 */
public class NormalModeCommandLine extends AbiCheckerCommandLine {

    /**
     * @param context the context parameters object.
     * @param logger  the build progress logger to use.
     *
     * @throws RunBuildException to break the build.
     */
    public NormalModeCommandLine(AbiCheckerContext context, SimpleLogger logger) throws RunBuildException {
        super(context, logger);

        // Find the header and library files
        List<String> matchedReferenceHeaderFiles
                = FileUtil.findFiles(context.getReferenceArtifactsDirectory(), context.getHeaderFilePattern());
        List<String> matchedReferenceLibraryFiles
                = FileUtil.findFiles(context.getReferenceArtifactsDirectory(), context.getLibraryFilePattern());
        List<String> matchedNewHeaderFiles
                = FileUtil.findFiles(context.getNewExtractedArtifactsDirectory(), context.getHeaderFilePattern());
        List<String> matchedNewLibraryFiles
                = FileUtil.findFiles(context.getNewExtractedArtifactsDirectory(), context.getLibraryFilePattern());

        // Write the XML files
        writeXmlDescriptor(context.getReferenceXmlFilename(), context.getReferenceXmlVersion(),
                matchedReferenceHeaderFiles, matchedReferenceLibraryFiles, context.getGccOptions());

        writeXmlDescriptor(context.getNewXmlFilename(), context.getNewXmlVersion(), matchedNewHeaderFiles,
                matchedNewLibraryFiles, context.getGccOptions());

        context.setMatchedLibraryFiles(matchedReferenceLibraryFiles);
    }

    /**
     * @return the path to the executable to run in this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    public String getExecutablePath() throws RunBuildException {
        return context.getAbiCheckerExecutablePath();
    }

    /**
     * @return the directory to work in in this mode.
     * @throws RunBuildException to break the build.
     */
    public String getWorkingDirectory() throws RunBuildException {
        return context.getWorkingDirectory().getAbsolutePath();
    }

    /**
     * @return the command-line arguments to use for this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    public List<String> getArguments() throws RunBuildException {
        List<String> arguments = new Vector<String>();
        arguments.add("-show-retval");

        arguments.add("-lib");
        arguments.add(StringUtil.join(context.getLibNames(), ", "));

        arguments.add("-component");
        arguments.add(context.getLibNames().size() > 1 ? "libraries" : "library");

        arguments.add("-old");
        arguments.add(context.getReferenceXmlFilename());

        arguments.add("-new");
        arguments.add(context.getNewXmlFilename());

        arguments.add("-binary");
        arguments.add("-bin-report-path");
        arguments.add(context.getNewArtifactsDirectory()
                + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.ABI_REPORT);

        arguments.add("-source");
        arguments.add("-src-report-path");
        arguments.add(context.getNewArtifactsDirectory()
                + AbiCheckerConstants.REPORT_DIRECTORY + AbiCheckerConstants.SRC_REPORT);

        arguments.add("-log-path");
        arguments.add(context.getNewArtifactsDirectory()
                + AbiCheckerConstants.REPORT_DIRECTORY + "log.txt");
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
