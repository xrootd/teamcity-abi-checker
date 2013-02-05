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

package ch.cern.dss.teamcity.server;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.AjaxRequestProcessor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller class which registers a servlet, accessible via <teamcity_url>/requestTags.html. This servlet is used to
 * handle asynchronous AJAX requests from the build runner edit settings page.
 */
public class AsyncTagRequestController extends BaseController {

    private ProjectManager projectManager;

    /**
     * Constructor. Registers the controller with the WebControllerManager, acquired through spring constructor
     * autowiring.
     *
     * @param manager        the WebControllerManager that this controller will be registered to.
     * @param projectManager used to find build types.
     */
    public AsyncTagRequestController(WebControllerManager manager,
                                     ProjectManager projectManager) {
        this.projectManager = projectManager;
        manager.registerController("/requestTags.html", this);
    }

    /**
     * This method gets called when <teamcity_url>/requestTags.html is accessed. We handle the request asynchronously
     * and add any errors into the response, along with the requested information.
     *
     * @param request  the HTTP request object.
     * @param response the HTTP response object.
     *
     * @return null
     * @throws Exception
     */
    @Nullable
    protected ModelAndView doHandle(final HttpServletRequest request,
                                    final HttpServletResponse response) {
        new AjaxRequestProcessor().processRequest(request, response,
                new AjaxRequestProcessor.RequestHandler() {
                    public void handleRequest(final HttpServletRequest request,
                                              final HttpServletResponse response,
                                              final Element xmlResponse) {
                        try {
                            getTags(request, xmlResponse);
                        } catch (Exception e) {
                            Loggers.SERVER.warn(e);
                            ActionErrors errors = new ActionErrors();
                            errors.addError("abiCheckerProblem", e.getMessage());
                            errors.serialize(xmlResponse);
                        }
                    }
                });
        return null;
    }

    /**
     * Retrieve the list of tags from the build type, as specified by the build type ID passed in the request. The
     * tags are then serialized into an XML element.
     *
     * @param request     the HTTP request object.
     * @param xmlResponse the response element to write to.
     *
     * @throws Exception
     */
    private void getTags(final HttpServletRequest request, final Element xmlResponse) throws Exception {
        String buildTypeId = request.getParameter("buildTypeId");
        SBuildType buildType = projectManager.findBuildTypeById(buildTypeId);
        Element tags = new Element("tags");

        for (String tag : buildType.getTags()) {
            tags.addContent(new Element("tag").addContent(tag));
        }
        xmlResponse.addContent(tags);
    }
}
