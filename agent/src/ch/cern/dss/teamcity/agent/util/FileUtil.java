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

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.util.AntPatternFileFinder;
import jetbrains.buildServer.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A set useful methods for operating on files.
 */
public class FileUtil {

    /**
     * Find files in the given directory, based upon the specified Ant-like wildcard string.
     *
     * @param searchDirectory the directory to look for files.
     * @param filePattern     the Ant-like wildcard pattern.
     *
     * @return list of absolute paths to the files that were found.
     * @throws RunBuildException to break the build.
     */
    public static List<String> findFiles(String searchDirectory, String filePattern) throws RunBuildException {
        List<String> matchedFiles;
        try {
            matchedFiles = matchFiles(searchDirectory, filePattern);
            if (matchedFiles.size() == 0) {
                throw new RunBuildException("No files matched the pattern: " + filePattern
                        + " in directory " + searchDirectory);
            }
        } catch (IOException e) {
            throw new RunBuildException("I/O error while collecting files", e);
        }
        return matchedFiles;
    }

    /**
     * Find files in the given directory, based upon the specified Ant-like wildcard string.
     *
     * @param searchDirectory the directory to look for files.
     * @param filePattern     the Ant-like wildcard pattern.
     *
     * @return list of absolute paths to the files that were found.
     * @throws RunBuildException to break the build.
     */
    private static List<String> matchFiles(String searchDirectory, String filePattern) throws IOException {
        final AntPatternFileFinder finder = new AntPatternFileFinder(splitFileWildcards(filePattern),
                new String[]{},
                SystemInfo.isFileSystemCaseSensitive);
        final File[] files = finder.findFiles(new File(searchDirectory));

        final List<String> result = new ArrayList<String>(files.length);
        for (File file : files) {
            result.add(file.getAbsolutePath());
        }

        return result;
    }

    /**
     * Split a string of files on their wildcards.
     *
     * @param string the file pattern string to split.
     *
     * @return array of split string pieces.
     */
    private static String[] splitFileWildcards(final String string) {
        if (string != null) {
            final String filesStringWithSpaces = string.replace('\n', ' ').replace('\r', ' ').replace('\\', '/');
            final List<String> split = StringUtil.splitCommandArgumentsAndUnquote(filesStringWithSpaces);
            return split.toArray(new String[split.size()]);
        }
        return new String[0];
    }
}
