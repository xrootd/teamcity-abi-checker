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

import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import ch.cern.dss.teamcity.common.IOUtil;
import ch.cern.dss.teamcity.common.SystemCommandResult;
import com.intellij.openapi.util.text.StringUtil;
import com.sun.security.auth.UnixNumericGroupPrincipal;
import com.sun.security.auth.module.UnixSystem;
import jetbrains.buildServer.RunBuildException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockEnvironmentBuilder {

    private SimpleLogger logger;
    private AbiCheckerContext context;
    private List<String> chroots;

    /**
     * @param context
     * @param logger  the build progress logger to use.
     */
    public MockEnvironmentBuilder(AbiCheckerContext context, SimpleLogger logger) {
        this.context = context;
        this.logger = logger;
    }

    /**
     * Initialize the mock environment.
     */
    public void setup() throws RunBuildException {
//        if (!checkMockUser()) {
//            throw new RunBuildException("User not in 'mock' group, or group not found");
//        }
//
//        File metaFile = new File(metaDirectory, AbiCheckerConstants.MOCK_META_FILE);
//        if (!metaFile.exists() || !metaFile.isFile()) {
//            throw new RunBuildException("No meta file found in directory: " + metaDirectory);
//        }

        // Parse and verify the chroots
        setChroots(parseChroots());
        for (String chroot : chroots) {
            if (!new File(AbiCheckerConstants.MOCK_CONFIG_DIRECTORY, chroot + ".cfg").exists()) {
                throw new RunBuildException("No matching config for chroot environment: " + chroot);
            }
        }

        logger.message("Using the following chroots: " + StringUtil.join(chroots, ", "));

        // Verify that the chroot environments are initialized.
        for (String chroot : chroots) {
            File chrootConfig = new File(AbiCheckerConstants.MOCK_CHROOT_DIRECTORY, chroot);
            if (!chrootConfig.exists()) {
                initializeChrootEnvironment(chrootConfig);
            }
        }
    }

    /**
     * Parse a list of chroots to use by finding the manifest files in the artifacts.
     *
     * @return a list of chroots to use.
     * @throws RunBuildException to break the build.
     */
    private List<String> parseChroots() throws RunBuildException {

        List<String> chroots = new ArrayList<String>();
        File artifactsDirectory = new File(context.getNewArtifactsDirectory());
        File[] artifacts = artifactsDirectory.listFiles();

        if (artifacts != null && artifacts.length > 0) {

            for (File file : artifacts) {
                if (file.isDirectory()) {
                    File[] manifestFile = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File directory, String name) {
                            return name.equals(AbiCheckerConstants.MOCK_META_FILE);
                        }
                    });

                    if (manifestFile != null && manifestFile.length == 1) {

                        String manifestFileContents;
                        try {
                            manifestFileContents = IOUtil.readFile(manifestFile[0].getAbsolutePath());
                        } catch (IOException e) {
                            throw new RunBuildException("Unable to parse chroot metadata", e);
                        }

                        if (!manifestFileContents.startsWith("chroot=")) {
                            throw new RunBuildException("Unable to parse chroot metadata: invalid file format");
                        }

                        chroots.add(StringUtil.split(manifestFileContents, "=").get(1));
                    }
                }
            }

        } else {
            throw new RunBuildException("Build contains no artifacts");
        }

        return chroots;
    }

    /**
     * Initialize an individual chroot environment with mock.
     *
     * @param chrootConfig the path to the mock config file to use for this chroot.
     */
    private void initializeChrootEnvironment(File chrootConfig) throws RunBuildException {
        logger.message("Initializing mock environment: " + chrootConfig.getAbsolutePath());
        String[] command = {AbiCheckerConstants.MOCK_EXECUTABLE, "--init", "-r", chrootConfig.getAbsolutePath()};
        SystemCommandResult result;

        try {
            result = IOUtil.runSystemCommand(command);
        } catch (Exception e) {
            throw new RunBuildException("Unable to initialize mock environment: " + e.getMessage());
        }

        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Unable to initialize mock environment: " + result.getOutput());
        }
    }

    /**
     * Function to check if the user under which the script is running is a member of the 'mock' unix group. If this is
     * not the case the mock command cannot be executed. This check is performed by newer versions of mock inside the
     * mock command itself.
     */
    private boolean checkMockUser() {
        UnixSystem unixSystem = new UnixSystem();
        logger.message("User: " + unixSystem.getUsername());

        long[] unixGroups;
        boolean groupFound = false;

        if (unixSystem.getGroups() != null && unixSystem.getGroups().length > 0) {
            unixGroups = unixSystem.getGroups();

            for (int i = 0; i < unixGroups.length; i++) {
                UnixNumericGroupPrincipal groupPrincipal = new UnixNumericGroupPrincipal(unixGroups[i], false);
                logger.message("Group: " + groupPrincipal.toString());

                if (groupPrincipal.getName().equals(AbiCheckerConstants.MOCK_GROUP)) {
                    groupFound = true;
                }
            }
        }
        return groupFound;
    }

    /**
     * @return the list of chroot names to be used.
     */
    public List<String> getChroots() {
        return chroots;
    }

    /**
     * @param chroots the chroots to be set.
     */
    public void setChroots(List<String> chroots) {
        this.chroots = chroots;
    }
}