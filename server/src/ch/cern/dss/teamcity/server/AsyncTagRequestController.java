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
     * @param e
     *
     * @return
     */
    static private String getMessageWithNested(Throwable e) {
        String result = e.getMessage();
        Throwable cause = e.getCause();
        if (cause != null) {
            result += " Caused by: " + getMessageWithNested(cause);
        }
        return result;
    }

    /**
     * @param request
     * @param response
     *
     * @return
     *
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
                            errors.addError("abiCheckerProblem", getMessageWithNested(e));
                            errors.serialize(xmlResponse);
                        }
                    }
                });
        return null;
    }

    /**
     * @param request
     * @param xmlResponse
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
