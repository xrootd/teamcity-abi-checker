package ch.cern.dss.teamcity.server;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.AjaxRequestProcessor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Example custom page controller
 */
public class AsyncTagRequestController extends BaseController {
    private PluginDescriptor pluginDescriptor;
    private ProjectManager projectManager;

    public AsyncTagRequestController(PluginDescriptor pluginDescriptor, WebControllerManager manager,
                                     ProjectManager projectManager) {
        this.pluginDescriptor = pluginDescriptor;
        this.projectManager = projectManager;
        // this will make the controller accessible via <teamcity_url>\abi-checker.html
        manager.registerController("/requestTags.html", this);
    }

    static private String getMessageWithNested(Throwable e) {
        String result = e.getMessage();
        Throwable cause = e.getCause();
        if (cause != null) {
            result += " Caused by: " + getMessageWithNested(cause);
        }
        return result;
    }

    @Nullable
    protected ModelAndView doHandle(final HttpServletRequest request,
                                    final HttpServletResponse response) throws Exception {
        new AjaxRequestProcessor().processRequest(request, response, new AjaxRequestProcessor.RequestHandler() {
            public void handleRequest(final HttpServletRequest request, final HttpServletResponse response,
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

    private void getTags(final HttpServletRequest request, final Element xmlResponse) throws Exception {
        String buildTypeId = request.getParameter("buildTypeId");
        Loggers.SERVER.info("buildTypeId: " + buildTypeId);

        SBuildType buildType = projectManager.findBuildTypeById(buildTypeId);
        Element tags = new Element("tags");

        for (String tag : buildType.getTags()) {
            tags.addContent(new Element("tag").addContent(tag));
        }

        xmlResponse.addContent(tags);
    }

    private void addMessage(HttpServletRequest request, String message) {
        if (message != null) {
            getOrCreateMessages(request).addMessage("abiCheckerMessage", message);
        }
    }

}
