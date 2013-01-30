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

import java.util.HashMap;
import java.util.Map;

public class CompatibilityReportPageBuilder {

    Map<String, String> reportPages;

    /**
     * @param reportPage
     */
    public CompatibilityReportPageBuilder(String reportPage) {
        reportPage = replaceCss(reportPage);
        this.reportPages = new HashMap<String, String>();
        this.reportPages.put("default", reportPage);
    }

    /**
     * @param reportPages
     */
    public CompatibilityReportPageBuilder(Map<String, String> reportPages) {
        Map<String, String> replacedPages = new HashMap<String, String>();

        for (Map.Entry e : reportPages.entrySet()) {
            replacedPages.put(e.getKey().toString(), replaceCss(e.getValue().toString()));
        }
        this.reportPages = replacedPages;
    }

    /**
     * @param reportPage
     *
     * @return
     */
    private String replaceCss(String reportPage) {
        return reportPage;
    }

    /**
     * @return
     */
    public Map<String, String> getReportPages() {
        return reportPages;
    }
}
