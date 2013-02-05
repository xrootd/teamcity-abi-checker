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
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Generic class to extract archives (tar, bz2, gz, zip, cpio, rpm) based on the file extension.
 */
public class ArchiveExtractor {

    private SimpleLogger logger;

    /**
     * @param logger the build progress logger.
     */
    public ArchiveExtractor(SimpleLogger logger) {
        this.logger = logger;
    }

    /**
     * Extract the archive at the specified path to the specified directory. The method will attempt to discover the
     * type of the archive.
     *
     * @param archivePath     the path to the archive file.
     * @param outputDirectory the extraction target directory.
     *
     * @throws RunBuildException to break the build.
     */
    public void extract(String archivePath, String outputDirectory) throws RunBuildException {
        logger.message("Extracting archive: " + archivePath);

        if (!new File(archivePath).exists()) {
            throw new RunBuildException("Archive not found: " + archivePath);
        }

        File folder = new File(outputDirectory);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (archivePath.endsWith(".gz") || archivePath.endsWith(".tgz") || archivePath.endsWith(".bz2")) {
            archivePath = decompress(archivePath, outputDirectory);
            extractTar(archivePath, outputDirectory);
        } else if (archivePath.endsWith(".rpm")) {
            archivePath = rpm2cpio(archivePath, outputDirectory);
            extractCpio(archivePath, outputDirectory);
        } else if (archivePath.endsWith(".cpio")) {
            extractCpio(archivePath, outputDirectory);
        } else if (archivePath.endsWith(".zip")) {
            extractZip(archivePath, outputDirectory);
        } else {
            throw new RunBuildException("Unsupported archive type: " + archivePath);
        }
    }

    /**
     * Decompress a compressed archive (e.g. .tar.gz -> .tar).
     *
     * @param archivePath     the path to the archive file.
     * @param outputDirectory the decompression target directory.
     *
     * @return the path to the decompressed archive.
     * @throws RunBuildException to break the build.
     */
    public String decompress(String archivePath, String outputDirectory) throws RunBuildException {
        Loggers.AGENT.debug("Decompressing: " + archivePath);
        String tarPath = new File(outputDirectory, new File(FilenameUtils.removeExtension(archivePath))
                .getName()).getAbsolutePath();

        try {
            CompressorInputStream in = new CompressorStreamFactory()
                    .createCompressorInputStream(new BufferedInputStream(new FileInputStream(archivePath)));
            IOUtils.copy(in, new FileOutputStream(tarPath));
            in.close();
        } catch (Exception e) {
            throw new RunBuildException("Failed to decompress archive", e);
        }

        return tarPath;
    }

    /**
     * Convert an RPM to a cpio archive suitable for extraction.
     *
     * @param archivePath     the path to the archive file.
     * @param outputDirectory the decompression target directory.
     *
     * @return the path to the converted archive.
     * @throws RunBuildException to break the build.
     */
    public String rpm2cpio(String archivePath, String outputDirectory) throws RunBuildException {
        Loggers.AGENT.debug("Converting to cpio: " + archivePath);
        String cpioPath = new File(outputDirectory, new File(FilenameUtils.removeExtension(archivePath) + ".cpio")
                .getName()).getAbsolutePath();

        String[] rpm2cpioCommand = {
                "/bin/sh",
                "-c",
                "/usr/bin/rpm2cpio " + archivePath + " > " + cpioPath};

        SystemCommandResult result;
        try {
            result = IOUtil.runSystemCommand(rpm2cpioCommand);
        } catch (Exception e) {
            throw new RunBuildException("Failed to convert rpm to cpio", e);
        }

        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Failed to convert rpm to cpio: " + result.getOutput());
        }

        return cpioPath;
    }

    /**
     * Extract the specified cpio archive to the specified output directory.
     * <p/>
     * <ugly_hack>The cpio command doesn't allow you to specify an output directory, and running commands with
     * ProcessBuilder in different directories is a pain, so we write a small script to a file and execute that
     * instead.</ugly_hack>
     *
     * @param archivePath  the path to the archive file.
     * @param outputFolder the extraction target directory.
     *
     * @throws RunBuildException to break the build.
     */
    public void extractCpio(String archivePath, String outputFolder) throws RunBuildException {
        String command = "#!/bin/sh\n" +
                "cd %working_directory%\n" +
                "cpio -idmv < %cpio_file%\n";

        command = command.replace("%cpio_file%", archivePath);
        command = command.replace("%working_directory%", outputFolder);
        try {
            IOUtil.writeFile("extract-cpio.sh", command);
        } catch (IOException e) {
            throw new RunBuildException("Failed to write intermediate cpio extraction script", e);
        }

        new File("extract-cpio.sh").setExecutable(true);
        SystemCommandResult result;

        try {
            result = IOUtil.runSystemCommand(new String[]{"./extract-cpio.sh", command});
        } catch (Exception e) {
            throw new RunBuildException("Failed to extract cpio", e);
        }

        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Failed to extract cpio: " + result.getOutput());
        }
    }

    /**
     * Extract the specified tar archive to the specified output directory.
     *
     * @param archivePath  the path to the archive file.
     * @param outputFolder the extraction target directory.
     *
     * @throws RunBuildException to break the build.
     */
    public void extractTar(String archivePath, String outputFolder) throws RunBuildException {
        String[] command = {"tar", "-xf", archivePath, "-C", outputFolder};
        SystemCommandResult result;

        try {
            result = IOUtil.runSystemCommand(command);
        } catch (Exception e) {
            throw new RunBuildException("Failed to extract tar", e);
        }

        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Failed to extract tar: " + result.getOutput());
        }
    }

    /**
     * Extract the specified zip archive to the specified output directory.
     *
     * @param archivePath  the path to the archive file.
     * @param outputFolder the extraction target directory.
     *
     * @throws RunBuildException to break the build.
     */
    public void extractZip(String archivePath, String outputFolder) throws RunBuildException {
        String[] command = {"unzip", archivePath, "-d", outputFolder};
        SystemCommandResult result;

        try {
            result = IOUtil.runSystemCommand(command);
        } catch (Exception e) {
            throw new RunBuildException("Failed to extract zip", e);
        }

        if (result.getReturnCode() != 0) {
            throw new RunBuildException("Failed to extract zip: " + result.getOutput());
        }
    }
}