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

    public void extract(String archivePath, String outputFolder, String archiveType)
            throws CompressorException, ArchiveException, IOException {

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
            Loggers.AGENT.info("Extracting: " + newFileName);

            File newFile = new File(outputFolder + File.separator + newFileName);
            newFile.getParentFile().mkdirs();
            newFile.createNewFile();

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

    }

    public String decompress(String archivePath)
            throws ArchiveException, IOException, CompressorException {

        Loggers.AGENT.info("Decompressing: " + archivePath);
        String tarPath = FilenameUtils.removeExtension(archivePath);

        final BufferedInputStream is = new BufferedInputStream(new FileInputStream(archivePath));
        CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(is);
        IOUtils.copy(in, new FileOutputStream(tarPath));
        in.close();

        return tarPath;
    }

    public String rpm2cpio(String archivePath) throws IOException {
        System.out.println("Converting to cpio: " + archivePath);
        String cpioPath = FilenameUtils.removeExtension(archivePath) + ".cpio";

        String envp[] = new String[1];
        envp[0] = "PATH=" + System.getProperty("java.library.path");
        Runtime.getRuntime().exec("/usr/bin/rpm2cpio", envp);

        return cpioPath;
    }
}