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

package ch.cern.dss.teamcity.server;

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import org.jetbrains.annotations.NotNull;

public class AbiCheckerBean {
    @NotNull
    public String getProjectNameKey() {
        return AbiCheckerConstants.UI_PROJECT_NAME;
    }

    @NotNull
    public String getBuildTypeKey() {
        return AbiCheckerConstants.UI_BUILD_TYPE;
    }

    @NotNull
    public String getReferenceTagKey() {
        return AbiCheckerConstants.UI_REFERENCE_TAG;
    }

    @NotNull
    public String getAbiCheckerExecutablePathKey() {
        return AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH;
    }

    @NotNull
    public String getArtifactFilesKey() {
        return AbiCheckerConstants.UI_ARTIFACT_FILES;
    }

    @NotNull
    public String getArtifactTypeKey() {
        return AbiCheckerConstants.UI_ARTIFACT_TYPE;
    }

    @NotNull
    public String getArtifactTypeRpmKey() {
        return AbiCheckerConstants.UI_ARTIFACT_TYPE_RPM;
    }

    @NotNull
    public String getArtifactTypeArchiveKey() {
        return AbiCheckerConstants.UI_ARTIFACT_TYPE_ARCHIVE;
    }

    @NotNull
    public String getArtifactTypeFolderKey() {
        return AbiCheckerConstants.UI_ARTIFACT_TYPE_FOLDER;
    }

    @NotNull
    public String getArtifactHeaderPathKey() {
        return AbiCheckerConstants.UI_ARTIFACT_HEADER_PATH;
    }

    @NotNull
    public String getArtifactLibraryPathKey() {
        return AbiCheckerConstants.UI_ARTIFACT_LIBRARY_PATH;
    }


}
