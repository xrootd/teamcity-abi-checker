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

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.BuildRunnerContext;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AbiCheckerContext {

    private final Map<String, String> runnerParameters;
    private final BuildParametersMap buildParameters;
    private final BuildRunnerContext runnerContext;
    private final File buildTempDirectory;
    private final File workingDirectory;
    private List<String> matchedLibraryFiles;

    public AbiCheckerContext(final Map<String, String> runnerParameters, final BuildParametersMap buildParameters,
                             final BuildRunnerContext runnerContext, final File buildTempDirectory,
                             final File workingDirectory) {
        this.runnerParameters = runnerParameters;
        this.buildParameters = buildParameters;
        this.buildTempDirectory = buildTempDirectory;
        this.workingDirectory = workingDirectory;
        this.runnerContext = runnerContext;
    }

    public String getReferenceBuildType() {
        return runnerParameters.get(AbiCheckerConstants.REFERENCE_BUILD_TYPE);
    }

    public String getReferenceArtifactsDirectory() {
        return buildTempDirectory.getAbsolutePath() + "/artifacts-" + getReferenceTag();
    }

    public String getReferenceTag() {
        return runnerParameters.get(AbiCheckerConstants.REFERENCE_TAG);
    }

    public String getReferenceBuildTypeName() {
        return runnerParameters.get(AbiCheckerConstants.REFERENCE_BUILD_TYPE_NAME);
    }

    public String getReferenceXmlFilename() {
        return buildTempDirectory + File.separator + getReferenceTag() + ".xml";
    }

    public String getReferenceXmlFilename(String chroot) {
        return buildTempDirectory + File.separator + getReferenceTag() + "-" + chroot + ".xml";
    }

    public String getReferenceXmlVersion() {
        return getReferenceBuildTypeName() + " " + getReferenceTag();
    }

    public String getNewArtifactsDirectory() {
        return runnerContext.getBuild().getArtifactsPaths();
    }

    public String getNewExtractedArtifactsDirectory() {
        return new File(getReferenceArtifactsDirectory()).getParent() + "/new-artifacts-extracted";
    }

    public String getNewXmlFilename() {
        return buildTempDirectory + File.separator + runnerContext.getBuild().getBuildNumber() + ".xml";
    }

    public String getNewXmlFilename(String chroot) {
        return buildTempDirectory + File.separator + runnerContext.getBuild().getBuildNumber() + "-" + chroot + ".xml";
    }

    public String getNewXmlVersion() {
        return runnerContext.getBuild().getBuildTypeName()
                + " build #" + runnerContext.getBuild().getBuildNumber();
    }

    public String getArtifactFilePattern() {
        return runnerParameters.get(AbiCheckerConstants.ARTIFACT_FILE_PATTERN);
    }

    public String getArtifactType() {
        return runnerParameters.get(AbiCheckerConstants.ARTIFACT_TYPE);
    }

    public String getHeaderFilePattern() {
        return runnerParameters.get(AbiCheckerConstants.HEADER_FILE_PATTERN);
    }

    public String getLibraryFilePattern() {
        return runnerParameters.get(AbiCheckerConstants.LIBRARY_FILE_PATTERN);
    }

    public String getGccOptions() {
        return runnerParameters.get(AbiCheckerConstants.GCC_OPTIONS);
    }

    public String getBuildMode() {
        return runnerParameters.get(AbiCheckerConstants.BUILD_MODE);
    }

    public String getBuildTempDirectory() {
        return buildTempDirectory.getAbsolutePath();
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public BuildParametersMap getBuildParameters() {
        return buildParameters;
    }

    public Map<String, String> getEnvironment() {
        return getBuildParameters().getEnvironmentVariables();
    }

    public String getAbiCheckerExecutablePath() {
        return runnerParameters.get(AbiCheckerConstants.EXECUTABLE_PATH);
    }

    public void setMatchedLibraryFiles(List<String> matchedReferenceLibraryFiles) {
        this.matchedLibraryFiles = matchedReferenceLibraryFiles;
    }

    public List<String> getMatchedLibraryFiles() {
        return matchedLibraryFiles;
    }

    public Set<String> getLibNames() {
        Set<String> libNames = new HashSet<String>();
        for (String libName : getMatchedLibraryFiles()) {
            libNames.add(new File(libName).getName());
        }
        return libNames;
    }
}
