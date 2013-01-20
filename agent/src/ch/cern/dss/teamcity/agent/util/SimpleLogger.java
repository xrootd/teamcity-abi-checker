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

package ch.cern.dss.teamcity.agent.util;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class SimpleLogger extends LoggerAdapter {
    @NotNull
    private final BuildProgressLogger logger;

    /**
     * @param buildLogger
     */
    public SimpleLogger(@NotNull final BuildProgressLogger buildLogger) {
        logger = buildLogger;
    }

    @Override
    public void message(@NotNull final String message) {
        logger.message(message);
    }

    @Override
    public void error(@NotNull final String message) {
        logger.error(message);
    }

    @Override
    public void warning(@NotNull final String message) {
        logger.warning(message);
    }

    @Override
    public void blockStart(@NotNull final String name) {
        logger.targetStarted(name);
    }

    @Override
    public void blockFinish(@NotNull final String name) {
        logger.targetFinished(name);
    }
}
