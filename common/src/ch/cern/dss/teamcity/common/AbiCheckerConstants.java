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

package ch.cern.dss.teamcity.common;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public interface AbiCheckerConstants {
    @NotNull
    @NonNls
    String TYPE = "abi-checker";
    @NotNull
    @NonNls
    String DESCRIPTION = "Checking binary compatibility of new shared libraries with previous versions";
    @NotNull
    @NonNls
    String DISPLAY_NAME = "ABI Compatibility Checker";
    String UI_PREFIX = "ui-" + TYPE + "-";
    String UI_PROJECT_NAME = UI_PREFIX + "project-name";
    String UI_BUILD_TYPE = UI_PREFIX + "build-type";
    String UI_REFERENCE_TAG = UI_PREFIX + "reference-tag";
    String UI_ABI_CHECKER_EXECUTABLE_PATH = UI_PREFIX + "executable-path";
    String UI_ARTIFACT_FILES = UI_PREFIX + "artifact-files";
    String UI_ARTIFACT_TYPE = UI_PREFIX + "artifact-type";
    String UI_ARTIFACT_HEADER_PATH = UI_PREFIX + "artifact-header-path";
    String UI_ARTIFACT_LIBRARY_PATH = UI_PREFIX + "artifact-library-path";
    String UI_ARTIFACT_TYPE_RPM = "RPM";
    String UI_ARTIFACT_TYPE_ARCHIVE = "Archive";
    String UI_ARTIFACT_TYPE_FOLDER = "Folder";
}
