package ch.cern.dss.teamcity.agent.util;

import jetbrains.buildServer.log.Loggers;

import java.io.*;
import java.net.URL;

public class IOUtils {

    /**
     * @param command array of commands, needed for pipes to work
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public static int runSystemCommand(String[] command) throws InterruptedException, IOException {

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new BufferedInputStream(process.getInputStream())));

        while (reader.readLine() != null) ;

        try {
            if (process.waitFor() != 0) {
                Loggers.AGENT.error("Exit value: " + process.exitValue());
            }
        } finally {
            reader.close();
        }

        return process.exitValue();
    }

    public static void saveUrl(String filename, String urlString) throws IOException {
        BufferedInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            out = new FileOutputStream(filename);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
            }
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }
}
