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

package ch.cern.dss.teamcity.common;

import java.io.*;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * Useful IO utilities.
 */
public class IOUtil {

    /**
     * Execute the specified system command.
     *
     * @param command array of commands, needed for pipes to work
     *
     * @return the process output and exit code.
     * @throws InterruptedException
     * @throws IOException
     */
    public static SystemCommandResult runSystemCommand(String[] command) throws InterruptedException, IOException {

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new BufferedInputStream(process.getInputStream())));

        StringBuffer buffer = new StringBuffer();

        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        try {
            process.waitFor();
        } finally {
            reader.close();
        }

        return new SystemCommandResult(process.exitValue(), buffer.toString());
    }

    /**
     * Download the file at the end of the specified URL and save it to the given filename.
     *
     * @param filename  the name of the file to save.
     * @param urlString the URL to retrieve.
     *
     * @throws IOException
     */
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

    /**
     * Read a file on the local filesystem into a string.
     *
     * @param path the path to the file to read
     *
     * @return the contents of the file, as a string.
     * @throws IOException
     */
    public static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel channel = stream.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            return Charset.defaultCharset().decode(buffer).toString();
        } finally {
            stream.close();
        }
    }

    /**
     * Write the contents of the given string to a file with the specified name.
     *
     * @param filename the path of the file to write.
     * @param text     the text to write to the file.
     *
     * @throws IOException
     */
    public static File writeFile(String filename, String text) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        out.write(text.getBytes("UTF-8"));
        out.close();
        return new File(filename);
    }
}
