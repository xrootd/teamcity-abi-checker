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

package ch.cern.dss.teamcity.agent.util;

import jetbrains.buildServer.log.Loggers;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
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
     * @param archiveType
     *
     * @throws CompressorException
     * @throws ArchiveException
     * @throws IOException
     * @throws InterruptedException
     */
    public void extract(String archivePath, String outputFolder, String archiveType)
            throws CompressorException, ArchiveException, IOException, InterruptedException {
        logger.message("Extracting archive: " + archivePath);

        if (archiveType.equals("tar") && (archivePath.endsWith(".gz") || archivePath.endsWith(".bz2"))) {
            archivePath = decompress(archivePath);
        } else if (archiveType.equalsIgnoreCase("rpm") && archivePath.endsWith("rpm")) {
            archivePath = rpm2cpio(archivePath);
            archiveType = "cpio";
        }

        File folder = new File(outputFolder);
        folder.mkdirs();

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(archivePath));
        ArchiveInputStream input;

        input = new ArchiveStreamFactory().createArchiveInputStream(archiveType, in);

        final byte[] buffer = new byte[4096];
        ArchiveEntry archiveEntry;

        while ((archiveEntry = input.getNextEntry()) != null) {
            String newFileName = archiveEntry.getName();

            File newFile = new File(outputFolder + File.separator + newFileName);
            Loggers.AGENT.debug("Extracting: " + newFile);

            if (!archiveEntry.isDirectory()) {
                newFile.getParentFile().mkdirs();
                newFile.createNewFile();

                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));
                int numBytes;
                while ((numBytes = input.read(buffer, 0, buffer.length)) != -1)
                    out.write(buffer, 0, numBytes);
                out.flush();
                out.close();
            } else {
                newFile.mkdirs();
            }

        }
        input.close();

    }

    /**
     * @param archivePath
     *
     * @return
     *
     * @throws ArchiveException
     * @throws IOException
     * @throws CompressorException
     */
    public String decompress(String archivePath)
            throws ArchiveException, IOException, CompressorException {

        logger.message("Decompressing: " + archivePath);
        String tarPath = FilenameUtils.removeExtension(archivePath);

        final BufferedInputStream is = new BufferedInputStream(new FileInputStream(archivePath));
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(is);
        IOUtils.copy(in, new FileOutputStream(tarPath));
        in.close();

        return tarPath;
    }

    /**
     * @param archivePath
     *
     * @return
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public String rpm2cpio(String archivePath) throws IOException, InterruptedException {
        logger.message("Converting to cpio: " + archivePath);
        String cpioPath = FilenameUtils.removeExtension(archivePath) + ".cpio";
        String[] rpm2cpioCommand = {
                "/bin/sh",
                "-c",
                "/usr/bin/rpm2cpio " + archivePath + " > " + cpioPath};
        int returnCode = ch.cern.dss.teamcity.agent.util.IOUtils.runSystemCommand(rpm2cpioCommand);
        Loggers.AGENT.debug("rpm2cpio returned with code " + returnCode);
        return cpioPath;
    }

}