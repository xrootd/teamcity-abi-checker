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

package ch.cern.dss.teamcity.agent;

import ch.cern.dss.teamcity.agent.util.FileUtil;
import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 */
public class NormalModeCommandLine extends AbiCheckerCommandLine implements ProgramCommandLine {

    /**
     * @param context
     * @param logger
     */
    public NormalModeCommandLine(AbiCheckerContext context, SimpleLogger logger) throws RunBuildException {
        super(context, logger);

        // Find the header and library files
        String headerFilePattern = context.getHeaderFilePattern();
        String libraryFilePattern = context.getLibraryFilePattern();
        String referenceArtifactsDirectory = context.getReferenceArtifactsDirectory();
        String newArtifactsDirectory = context.getNewArtifactsDirectory();

        List<String> matchedReferenceHeaderFiles = FileUtil.findFiles(referenceArtifactsDirectory, headerFilePattern);
        List<String> matchedReferenceLibraryFiles = FileUtil.findFiles(referenceArtifactsDirectory, libraryFilePattern);
        List<String> matchedNewHeaderFiles = FileUtil.findFiles(newArtifactsDirectory, headerFilePattern);
        List<String> matchedNewLibraryFiles = FileUtil.findFiles(newArtifactsDirectory, libraryFilePattern);

        // Write the XML files
        writeXmlDescriptor(context.getReferenceXmlFilename(), context.getReferenceXmlVersion(),
                matchedReferenceHeaderFiles, matchedReferenceLibraryFiles, context.getGccOptions());

        writeXmlDescriptor(context.getNewXmlFilename(), context.getNewXmlVersion(), matchedNewHeaderFiles,
                matchedNewLibraryFiles, context.getGccOptions());

        context.setMatchedFiles(matchedReferenceHeaderFiles, matchedReferenceLibraryFiles, matchedNewHeaderFiles,
                matchedNewLibraryFiles);
    }

    /**
     * @return
     * @throws RunBuildException
     */
    @NotNull
    public String getExecutablePath() throws RunBuildException {
        return context.getAbiCheckerExecutablePath();
    }

    /**
     * @return
     * @throws RunBuildException
     */
    public String getWorkingDirectory() throws RunBuildException {
        return context.getWorkingDirectory().getAbsolutePath();
    }

    /**
     * @return
     * @throws RunBuildException
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
     * @return
     * @throws RunBuildException
     */
    @NotNull
    @Override
    public Map<String, String> getEnvironment() throws RunBuildException {
        return context.getEnvironment();
    }
}
