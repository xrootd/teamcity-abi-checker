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

    String BUILD_TYPE_NAME = "build-type-name";
    String BUILD_TYPE = "build-type";
    String REFERENCE_TAG = "reference-tag";
    String EXECUTABLE_PATH = "executable-path";
    String ARTIFACT_FILES = "artifact-files";
    String ARTIFACT_TYPE = "artifact-type";
    String ARTIFACT_HEADER_FILES = "artifact-header-files";
    String ARTIFACT_LIBRARY_FILES = "artifact-library-files";
    String GCC_OPTIONS = "gcc-options";

    String ARTIFACT_TYPE_RPM = "RPM";
    String ARTIFACT_TYPE_ARCHIVE = "Archive";
    String ARTIFACT_TYPE_FOLDER = "Folder";

    String TAB_ID = "abiCheckerReport";
    String TAB_TITLE = "ABI Compatibility Report";
    String REPORT_FILE = "/abi-checker-report/abi_compat_report.html";
}
