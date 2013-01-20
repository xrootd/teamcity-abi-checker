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

import org.jetbrains.annotations.NotNull;

public class LoggerAdapter implements Logger {
  public void message(@NotNull final String message) {
  }

  public void error(@NotNull final String message) {
  }

  public void warning(@NotNull final String message) {
  }

  public void blockStart(@NotNull final String name) {
  }

  public void blockFinish(@NotNull final String name) {
  }

}
