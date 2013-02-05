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

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class AbiCheckerBuildServiceFactory implements CommandLineBuildServiceFactory, AgentBuildRunnerInfo {

    /**
     * @return
     */
    @NotNull
    @Override
    public CommandLineBuildService createService() {
        return new AbiCheckerBuildService();
    }

    /**
     * @return
     */
    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return this;
    }

    /**
     * @return
     */
    @NotNull
    @Override
    public String getType() {
        return AbiCheckerConstants.TYPE;
    }

    /**
     * @param buildAgentConfiguration
     *
     * @return
     */
    @Override
    public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfiguration) {

        // Check for existence of rpm2cpio
        return true;
    }
}
