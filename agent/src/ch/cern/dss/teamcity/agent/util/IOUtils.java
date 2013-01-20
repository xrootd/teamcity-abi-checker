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
