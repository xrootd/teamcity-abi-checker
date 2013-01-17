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

public class ArchiveExtractor {

    public void extract(String archivePath, String outputFolder, String archiveType) {

        try {
            if (archiveType == "tar" && (archivePath.endsWith(".gz") || archivePath.endsWith(".bz2"))) {
                archivePath = decompress(archivePath, outputFolder);
            }

            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(archivePath));
            ArchiveInputStream input;

            input = new ArchiveStreamFactory().createArchiveInputStream(archiveType, in);

            final byte[] buffer = new byte[4096];
            ArchiveEntry archiveEntry;

            while ((archiveEntry = input.getNextEntry()) != null) {
                String newFileName = archiveEntry.getName();
                Loggers.AGENT.info("Extracting: " + archiveEntry);

                File newFile = new File(outputFolder + File.separator + newFileName);

                newFile.getParentFile().mkdirs();

                if (!archiveEntry.isDirectory()) {
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));
                    int numBytes;
                    while ((numBytes = input.read(buffer, 0, buffer.length)) != -1)
                        out.write(buffer, 0, numBytes);
                    out.flush();
                    out.close();
                }

            }
            input.close();

        } catch (FileNotFoundException e) {
            Loggers.AGENT.error("Error extracting: " + e.getMessage());
        } catch (ArchiveException e) {
            Loggers.AGENT.error("Error extracting: " + e.getMessage());
        } catch (IOException e) {
            Loggers.AGENT.error("Error extracting: " + e.getMessage());
        } catch (CompressorException e) {
            Loggers.AGENT.error("Error decompressing: " + e.getMessage());
        }

    }

    public String decompress(String archivePath, String outputFolder)
            throws ArchiveException, IOException, CompressorException {

        Loggers.AGENT.info("Decompressing: " + archivePath);
        String tarPath = FilenameUtils.removeExtension(archivePath);

        final BufferedInputStream is = new BufferedInputStream(new FileInputStream(archivePath));
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(is);
        IOUtils.copy(in, new FileOutputStream(tarPath));
        in.close();

        return tarPath;
    }
}