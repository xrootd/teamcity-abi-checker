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

package ch.cern.dss.teamcity.agent;

import ch.cern.dss.teamcity.agent.util.SimpleLogger;
import ch.cern.dss.teamcity.common.IOUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Acts as an entry point for the two modes of operation of this plugin (normal and mock).
 */
public abstract class AbiCheckerCommandLine implements ProgramCommandLine {

    protected final SimpleLogger logger;
    protected final AbiCheckerContext context;

    /**
     * @param context the context object which holds useful parameters.
     * @param logger  the build progress logger that we should use.
     */
    public AbiCheckerCommandLine(AbiCheckerContext context, SimpleLogger logger) {
        this.context = context;
        this.logger = logger;
    }

    /**
     * @return the path to the executable to run in this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    @Override
    public abstract String getExecutablePath() throws RunBuildException;

    /**
     * @return the directory to work in in this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    @Override
    public abstract String getWorkingDirectory() throws RunBuildException;

    /**
     * @return the command-line arguments to use for this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    @Override
    public abstract List<String> getArguments() throws RunBuildException;

    /**
     * @return the map of environment variables to use in this mode.
     * @throws RunBuildException to break the build.
     */
    @NotNull
    @Override
    public abstract Map<String, String> getEnvironment() throws RunBuildException;

    /**
     * Create an XML descriptor suitable for use with the abi-compliance-checker program based upon the specified
     * values.
     *
     * @param filename   the absolute filename of the XML descriptor to be created.
     * @param version    the version identifier for this descriptor.
     * @param headers    the list of header files to be checked.
     * @param libs       the list of shared libraries to be checked/
     * @param gccOptions the list of additional GCC options to be applied.
     *
     * @return the newly written XML descriptor file object.
     * @throws RunBuildException to break the build.
     */
    protected File writeXmlDescriptor(String filename, String version, List<String> headers, List<String> libs,
                                      String gccOptions) throws RunBuildException {
        String descriptor = "" +
                "<version>" + version + "</version>" +
                "<headers>" + StringUtil.join(headers, "\n") + "</headers>" +
                "<libs>" + StringUtil.join(libs, "\n") + "</libs>" +
                "<gcc_options>" + gccOptions + "</gcc_options>";

        File xmlFile;
        try {
            xmlFile = IOUtil.writeFile(filename, descriptor);
        } catch (IOException e) {
            throw new RunBuildException("Error writing XML descriptor", e);
        }
        return xmlFile;
    }
}
