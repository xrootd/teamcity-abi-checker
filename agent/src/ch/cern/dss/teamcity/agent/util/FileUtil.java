package ch.cern.dss.teamcity.agent.util;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.util.AntPatternFileFinder;
import jetbrains.buildServer.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    /**
     * @param searchDirectory
     * @param filePattern
     *
     * @return
     * @throws jetbrains.buildServer.RunBuildException
     *
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
     * Returns *absolute* path
     *
     * @param filePath
     * @param fileString
     *
     * @return
     * @throws IOException
     */
    private static List<String> matchFiles(String filePath, String fileString) throws IOException {
        final AntPatternFileFinder finder = new AntPatternFileFinder(splitFileWildcards(fileString),
                new String[]{},
                SystemInfo.isFileSystemCaseSensitive);
        final File[] files = finder.findFiles(new File(filePath));

        final List<String> result = new ArrayList<String>(files.length);
        for (File file : files) {
            result.add(file.getAbsolutePath());
        }

        return result;
    }

    /**
     * @param string
     *
     * @return
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
