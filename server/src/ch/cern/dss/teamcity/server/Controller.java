package ch.cern.dss.teamcity.server;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.cern.dss.teamcity.common.Util;
import jetbrains.buildServer.BuildServer;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.springframework.web.servlet.ModelAndView;

/**
 * Example custom page controller
 */
public class Controller extends BaseController {
  private PluginDescriptor myPluginDescriptor;

  public Controller(PluginDescriptor pluginDescriptor, WebControllerManager manager){
    myPluginDescriptor = pluginDescriptor;
    // this will make the controller accessible via <teamcity_url>\abi-checker.html
    manager.registerController("/abi-checker.html", this);
  }

  @Override
  protected ModelAndView doHandle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    ModelAndView view = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("abi-checker.jsp"));
    final Map model = view.getModel();
    model.put("name", Util.NAME);
    return view;
  }
}
