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

import ch.cern.dss.teamcity.agent.util.IOUtils;
import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MockCommandLine implements ProgramCommandLine {

    private final SimpleLogger logger;
    private final AbiCheckerContext context;
    private final MockEnvironmentBuilder mockEnvironmentBuilder;

    public MockCommandLine(AbiCheckerContext context, SimpleLogger logger) throws RunBuildException {
        this.context = context;
        this.logger = logger;

        logger.message("Setting up mock context");

        File mockMetaDirectory = new File(context.getNewArtifactsDirectory(), AbiCheckerConstants.MOCK_META_DIRECTORY);
        if (!mockMetaDirectory.exists() || !mockMetaDirectory.isDirectory()) {
            throw new RunBuildException("Cannot setup mock context: directory not found: "
                    + mockMetaDirectory);
        }

        mockEnvironmentBuilder = new MockEnvironmentBuilder(mockMetaDirectory, logger);
        mockEnvironmentBuilder.setup();
    }

    @NotNull
    @Override
    public String getExecutablePath() throws RunBuildException {
        return null;
    }

    @NotNull
    @Override
    public String getWorkingDirectory() throws RunBuildException {
        return null;
    }

    @NotNull
    @Override
    public List<String> getArguments() throws RunBuildException {
        List<String> arguments = new Vector<String>();

        StringBuilder command = new StringBuilder();

        for (String chroot : mockEnvironmentBuilder.getChroots()) {
            command.append(AbiCheckerConstants.MOCK_EXECUTABLE
                    + " -r " + chroot + " --install abi-compliance-checker\n");
        }

        File mockScriptFile = new File(context.getWorkingDirectory(), "mock-install.sh");
        try {
            IOUtils.writeFile(mockScriptFile.getAbsolutePath(), command.toString());
        } catch (IOException e) {
            throw new RunBuildException("Error writing mock script", e);
        }

        arguments.add(mockScriptFile.getAbsolutePath());
        logger.message("Arguments: " + StringUtil.join(arguments, " "));
        return arguments;
    }

    @NotNull
    @Override
    public Map<String, String> getEnvironment() throws RunBuildException {
        return null;
    }
}
