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
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 */
public class AbiCheckerCommandLineBuilder {

    private final SimpleLogger logger;
    private final Map<String, String> runnerParameters;
    private final List<String> libNames;
    private final File referenceXmlFile;
    private final File newXmlFile;
    private final String artifactsPaths;

    /**
     *
     * @param logger
     * @param runnerParameters
     * @param libNames
     * @param referenceXmlFile
     * @param newXmlFile
     * @param artifactsPaths
     */
    public AbiCheckerCommandLineBuilder(SimpleLogger logger, final Map<String, String> runnerParameters,
                                        final List<String> libNames,
                                        final File referenceXmlFile,
                                        final File newXmlFile,
                                        final String artifactsPaths) {
        this.logger = logger;
        this.runnerParameters = runnerParameters;
        this.libNames = libNames;
        this.referenceXmlFile = referenceXmlFile;
        this.newXmlFile = newXmlFile;
        this.artifactsPaths = artifactsPaths;
    }

    /**
     *
     * @return
     */
    @NotNull
    public List<String> getArguments() throws RunBuildException {
        if (runnerParameters.get(AbiCheckerConstants.BUILD_MODE).equals(AbiCheckerConstants.BUILD_MODE_MOCK)) {
            setupMockEnvironment();
            return getMockModeArguments();
        } else {
            return getNormalModeArguments();
        }
    }

    private List<String> getNormalModeArguments() {
        List<String> arguments = new Vector<String>();
        arguments.add("-show-retval");

        arguments.add("-lib");
        arguments.add(StringUtil.join(libNames, ", "));

        arguments.add("-component");
        arguments.add(libNames.size() > 1 ? "libraries" : "library");

        arguments.add("-old");
        arguments.add(referenceXmlFile.getAbsolutePath());

        arguments.add("-new");
        arguments.add(newXmlFile.getAbsolutePath());

        arguments.add("-report-path");
        arguments.add(artifactsPaths + AbiCheckerConstants.REPORT_FILE);

        return arguments;
    }

    private List<String> getMockModeArguments() {
        List<String> arguments = new Vector<String>();

        

        return arguments;
    }

    /**
     *
     * @return
     * @throws RunBuildException
     */
    @NotNull
    public String getExecutablePath() throws RunBuildException {
        String executable;
        if (runnerParameters.get(AbiCheckerConstants.BUILD_MODE).equals(AbiCheckerConstants.BUILD_MODE_MOCK)) {
            executable = AbiCheckerConstants.MOCK_EXECUTABLE;
        } else if (runnerParameters.get(AbiCheckerConstants.BUILD_MODE).equals(AbiCheckerConstants.BUILD_MODE_NORMAL)) {
            executable = runnerParameters.get(AbiCheckerConstants.EXECUTABLE_PATH);
        } else {
            throw new RunBuildException("Unknown build mode: " + runnerParameters.get(AbiCheckerConstants.BUILD_MODE));
        }
        return executable;
    }

    /**
     * @throws RunBuildException
     */
    private void setupMockEnvironment() throws RunBuildException {
        logger.message("Setting up mock environment");

        File mockMetaDirectory = new File(artifactsPaths, AbiCheckerConstants.MOCK_META_DIRECTORY);
        if (!mockMetaDirectory.exists() || !mockMetaDirectory.isDirectory()) {
            throw new RunBuildException("Cannot setup mock environment: directory not found: "
                    + mockMetaDirectory);
        }

        new MockEnvironmentBuilder(mockMetaDirectory, logger).setup();
    }
}
