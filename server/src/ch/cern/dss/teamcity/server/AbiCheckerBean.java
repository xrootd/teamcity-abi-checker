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

/**
 * Simple spring bean (defined in ../META-INF/build-server-plugin-abi-checker-xml) to provide identification constants
 * to the web UI forms.
 */
public class AbiCheckerBean {

    @NotNull
    public String getBuildTypeKey() {
        return AbiCheckerConstants.BUILD_TYPE;
    }

    @NotNull
    public String getBuildTypeNameKey() {
        return AbiCheckerConstants.BUILD_TYPE_NAME;
    }

    @NotNull
    public String getReferenceTagKey() {
        return AbiCheckerConstants.REFERENCE_TAG;
    }

    @NotNull
    public String getAbiCheckerExecutablePathKey() {
        return AbiCheckerConstants.EXECUTABLE_PATH;
    }

    @NotNull
    public String getArtifactFilesKey() {
        return AbiCheckerConstants.ARTIFACT_FILES;
    }

    @NotNull
    public String getArtifactTypeKey() {
        return AbiCheckerConstants.ARTIFACT_TYPE;
    }

    @NotNull
    public String getArtifactTypeRpmKey() {
        return AbiCheckerConstants.ARTIFACT_TYPE_RPM;
    }

    @NotNull
    public String getArtifactTypeArchiveKey() {
        return AbiCheckerConstants.ARTIFACT_TYPE_ARCHIVE;
    }

    @NotNull
    public String getArtifactTypeFolderKey() {
        return AbiCheckerConstants.ARTIFACT_TYPE_FOLDER;
    }

    @NotNull
    public String getArtifactHeaderFilesKey() {
        return AbiCheckerConstants.ARTIFACT_HEADER_FILES;
    }

    @NotNull
    public String getArtifactLibraryFilesKey() {
        return AbiCheckerConstants.ARTIFACT_LIBRARY_FILES;
    }

    @NotNull
    public String getGccOptionsKey() {
        return AbiCheckerConstants.GCC_OPTIONS;
    }
}
