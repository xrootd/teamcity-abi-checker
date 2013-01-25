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

import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 */
public class AbiCheckerCommandLine implements ProgramCommandLine {

    private final SimpleLogger logger;
    private final AbiCheckerContext context;

    /**
     * @param context
     * @param logger
     */
    public AbiCheckerCommandLine(AbiCheckerContext context, SimpleLogger logger) {
        this.context = context;
        this.logger = logger;
    }

    /**
     * @return
     * @throws RunBuildException
     */
    @NotNull
    public List<String> getArguments() throws RunBuildException {
        List<String> arguments = new Vector<String>();
        arguments.add("-show-retval");

        List<String> libNames = new ArrayList<String>();
        for (String libName : context.getMatchedReferenceLibraryFiles()) {
            libNames.add(new File(libName).getName());
        }
        arguments.add("-lib");
        arguments.add(StringUtil.join(libNames, ", "));

        arguments.add("-component");
        arguments.add(libNames.size() > 1 ? "libraries" : "library");

        arguments.add("-old");
        arguments.add(context.getReferenceXmlFilename());

        arguments.add("-new");
        arguments.add(context.getNewXmlFilename());

        arguments.add("-report-path");
        arguments.add(context.getCompatibilityReportFile());

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
}
