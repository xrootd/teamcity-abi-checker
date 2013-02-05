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

import ch.cern.dss.teamcity.common.IOUtil;
import ch.cern.dss.teamcity.common.SystemCommandResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.log.Loggers;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;

import java.io.*;

/**
 *
 */
public class ArchiveExtractor {

    private SimpleLogger logger;

    /**
     * @param logger
     */
    public ArchiveExtractor(SimpleLogger logger) {
        this.logger = logger;
    }

    /**
     * @param archivePath
     * @param outputFolder
     *
     * @throws CompressorException
     * @throws ArchiveException
     * @throws IOException
     * @throws InterruptedException
     */
    public void extract(String archivePath, String outputFolder)
            throws CompressorException, ArchiveException, IOException, InterruptedException, RunBuildException {
        logger.message("Extracting archive: " + archivePath);
        logger.message("    to directory: " + outputFolder);

        if (!new File(archivePath).exists()) {
            throw new FileNotFoundException("Archive not found: " + archivePath);
        }

        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (archivePath.endsWith(".gz") || archivePath.endsWith(".tgz") || archivePath.endsWith(".bz2")) {
            archivePath = decompress(archivePath);
            extractTar(archivePath, outputFolder);
        } else if (archivePath.endsWith(".rpm")) {
            archivePath = rpm2cpio(archivePath);
            extractCpio(archivePath, outputFolder);
        } else if (archivePath.endsWith(".cpio")) {
            extractCpio(archivePath, outputFolder);
        } else if (archivePath.endsWith(".zip")) {
            extractZip(archivePath, outputFolder);
        } else {
            throw new IOException("Unsupported archive type: " + archivePath);
        }
    }

    /**
     * @param archivePath
     *
     * @return
     * @throws ArchiveException
     * @throws IOException
     * @throws CompressorException
     */
    public String decompress(String archivePath)
            throws ArchiveException, IOException, CompressorException {

        Loggers.AGENT.debug("Decompressing: " + archivePath);
        String tarPath = FilenameUtils.removeExtension(archivePath);

        final BufferedInputStream is = new BufferedInputStream(new FileInputStream(archivePath));
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(is);
        org.apache.commons.compress.utils.IOUtils.copy(in, new FileOutputStream(tarPath));
        in.close();

        return tarPath;
    }

    /**
     * @param archivePath
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String rpm2cpio(String archivePath) throws IOException, InterruptedException {
        Loggers.AGENT.debug("Converting to cpio: " + archivePath);
        String cpioPath = FilenameUtils.removeExtension(archivePath) + ".cpio";
        String[] rpm2cpioCommand = {
                "/bin/sh",
                "-c",
                "/usr/bin/rpm2cpio " + archivePath + " > " + cpioPath};
        SystemCommandResult result = IOUtil.runSystemCommand(rpm2cpioCommand);
        Loggers.AGENT.debug("rpm2cpio returned with code " + result.getReturnCode());
        return cpioPath;
    }

    /**
     * <ugly_hack>The cpio command doesn't allow you to specify an output directory, and running commands with
     * ProcessBuilder in different directories is a pain, so we write a small script to a file and execute that
     * instead.</ugly_hack>
     *
     * @param archivePath
     * @param outputFolder
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void extractCpio(String archivePath, String outputFolder)
            throws IOException, InterruptedException, RunBuildException {

        String command = "#!/bin/sh\n" +
                "cd %working_directory%\n" +
                "cpio -idmv < %cpio_file%\n";

        command = command.replace("%cpio_file%", archivePath);
        command = command.replace("%working_directory%", outputFolder);
        IOUtil.writeFile("extract-cpio.sh", command);
        new File("extract-cpio.sh").setExecutable(true);

        SystemCommandResult result = IOUtil.runSystemCommand(new String[]{"./extract-cpio.sh", command});
        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Failed to extract cpio: " + result.getOutput());
        }
    }

    /**
     * @param archivePath
     * @param outputFolder
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void extractTar(String archivePath, String outputFolder)
            throws IOException, InterruptedException, RunBuildException {
        String[] command = {"tar", "-xf", archivePath, "-C", outputFolder};
        SystemCommandResult result = IOUtil.runSystemCommand(command);
        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Failed to extract tar: " + result.getOutput());
        }
    }

    /**
     * @param archivePath
     * @param outputFolder
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void extractZip(String archivePath, String outputFolder)
            throws IOException, InterruptedException, RunBuildException {
        String[] command = {"unzip", archivePath, "-d", outputFolder};
        SystemCommandResult result = IOUtil.runSystemCommand(command);
        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Failed to extract zip: " + result.getOutput());
        }
    }

}