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

import ch.cern.dss.teamcity.common.AbiCheckerConstants;
import jetbrains.buildServer.web.openapi.CustomTab;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class CompatibilityReportPageExtension extends SimplePageExtension implements CustomTab {

    public CompatibilityReportPageExtension(@NotNull PagePlaces pagePlaces) {
        super(pagePlaces, PlaceId.BUILD_RESULTS_TAB, "abi-checker", "compatibilityReport.html");
        register();
    }

    @NotNull
    @Override
    public String getTabId() {
        return AbiCheckerConstants.TAB_ID;
    }

    @NotNull
    @Override
    public String getTabTitle() {
        return AbiCheckerConstants.TAB_TITLE;
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        super.fillModel(model, request);
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return true;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
