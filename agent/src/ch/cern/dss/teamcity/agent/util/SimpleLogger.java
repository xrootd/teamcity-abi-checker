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

package ch.cern.dss.teamcity.agent.util;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Simple logging class to wrap around a BuildProgressLogger.
 */
public class SimpleLogger {
    @NotNull
    private final BuildProgressLogger logger;

    /**
     * @param buildLogger the BuildProgressLogger to wrap.
     */
    public SimpleLogger(@NotNull final BuildProgressLogger buildLogger) {
        logger = buildLogger;
    }

    /**
     * Log an informational message to the TeamCity build log.
     *
     * @param message the message to log.
     */
    public void message(@NotNull final String message) {
        logger.message(message);
    }

    /**
     * Log an error message to the TeamCity build log.
     *
     * @param message the message to log.
     */
    public void error(@NotNull final String message) {
        logger.error(message);
    }

    /**
     * Log a warning message to the TeamCity build log.
     *
     * @param message the message to log.
     */
    public void warning(@NotNull final String message) {
        logger.warning(message);
    }
}
