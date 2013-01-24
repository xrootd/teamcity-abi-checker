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

import ch.cern.dss.teamcity.agent.util.IOUtils;
import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.agent.util.SystemCommandResult;
import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import com.intellij.openapi.util.text.StringUtil;
import com.sun.security.auth.UnixNumericGroupPrincipal;
import com.sun.security.auth.module.UnixSystem;
import jetbrains.buildServer.RunBuildException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MockEnvironmentBuilder {

    private SimpleLogger logger;
    private File metaDirectory;

    /**
     * @param metaDirectory
     * @param logger
     */
    public MockEnvironmentBuilder(File metaDirectory, SimpleLogger logger) {
        this.logger = logger;
        this.metaDirectory = metaDirectory;
    }

    /**
     *
     */
    public void setup() throws RunBuildException {
//        if (!checkMockUser()) {
//            throw new RunBuildException("User not in 'mock' group, or group not found");
//        }

        File metaFile = new File(metaDirectory, AbiCheckerConstants.MOCK_META_FILE);
        if (!metaFile.exists() || !metaFile.isFile()) {
            throw new RunBuildException("No meta file found in directory: " + metaDirectory);
        }

        // Parse and verify the chroots
        List<String> chroots = parseChroots(metaFile);
        for (String chroot : chroots) {
            logger.message("chroot: " + chroot);
            if (!new File(AbiCheckerConstants.MOCK_CONFIG_DIRECTORY, chroot + ".cfg").exists()) {
                throw new RunBuildException("Unknown chroot environment: " + chroot);
            }
        }

        logger.message("Using the following chroots: " + StringUtil.join(chroots, ", "));

        // Verify that the chroot environments are initialized.
        for (String chroot : chroots) {
            File chrootDirectory = new File(AbiCheckerConstants.MOCK_CHROOT_DIRECTORY, chroot);
            if (!chrootDirectory.exists()) {
                initializeChrootEnvironment(chrootDirectory);
            }
        }

    }

    /**
     * @param chrootDirectory
     */
    private void initializeChrootEnvironment(File chrootDirectory) throws RunBuildException {
        logger.message("Initializing mock environment: " + chrootDirectory.getAbsolutePath());
        String[] command = {AbiCheckerConstants.MOCK_EXECUTABLE, "--init", "-r", chrootDirectory.getAbsolutePath()};
        SystemCommandResult result;

        try {
            result = IOUtils.runSystemCommand(command);
        } catch (Exception e) {
            throw new RunBuildException("Unable to initialize mock environment: " + e.getMessage());
        }

        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Unable to initialize mock environment: " + result.getOutput());
        }
    }

    /**
     * @param metaFile
     *
     * @return
     * @throws IOException
     */
    private List<String> parseChroots(File metaFile) throws RunBuildException {
        String metaFileContents;
        try {
            metaFileContents = IOUtils.readFile(metaFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RunBuildException("Unable to parse chroot metadata", e);
        }

        if (!metaFileContents.startsWith("chroots=")) {
            throw new RunBuildException("Unable to parse chroot metadata: invalid file format");
        }

        return StringUtil.split(StringUtil.split(metaFileContents, "=").get(1), ",");
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
}