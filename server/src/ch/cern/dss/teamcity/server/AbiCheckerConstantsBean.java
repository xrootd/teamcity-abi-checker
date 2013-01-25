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
public class AbiCheckerConstantsBean {

    @NotNull
    public String getReferenceBuildTypeKey() {
        return AbiCheckerConstants.REFERENCE_BUILD_TYPE;
    }

    @NotNull
    public String getReferenceBuildTypeNameKey() {
        return AbiCheckerConstants.REFERENCE_BUILD_TYPE_NAME;
    }

    @NotNull
    public String getReferenceTagKey() {
        return AbiCheckerConstants.REFERENCE_TAG;
    }

    @NotNull
    public String getBuildModeKey() {
        return AbiCheckerConstants.BUILD_MODE;
    }

    @NotNull
    public String getBuildModeNormalKey() {
        return AbiCheckerConstants.BUILD_MODE_NORMAL;
    }

    @NotNull
    public String getBuildModeMockKey() {
        return AbiCheckerConstants.BUILD_MODE_MOCK;
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
    public String getArtifactFilesKey() {
        return AbiCheckerConstants.ARTIFACT_FILE_PATTERN;
    }

    @NotNull
    public String getAbiCheckerExecutablePathKey() {
        return AbiCheckerConstants.EXECUTABLE_PATH;
    }

    @NotNull
    public String getArtifactHeaderFilesKey() {
        return AbiCheckerConstants.HEADER_FILE_PATTERN;
    }

    @NotNull
    public String getArtifactLibraryFilesKey() {
        return AbiCheckerConstants.LIBRARY_FILE_PATTERN;
    }

    @NotNull
    public String getGccOptionsKey() {
        return AbiCheckerConstants.GCC_OPTIONS;
    }
}
