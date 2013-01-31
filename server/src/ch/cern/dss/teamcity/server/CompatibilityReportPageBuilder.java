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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompatibilityReportPageBuilder {

    Map<String, Map.Entry<String, String>> reportPages;

    /**
     * @param reportPages
     */
    public CompatibilityReportPageBuilder(Map.Entry<String, String> reportPages) {
        reportPages = new AbstractMap.SimpleEntry<String, String>
                (stripCss(reportPages.getKey()), stripCss(reportPages.getValue()));
        this.reportPages = new HashMap<String, Map.Entry<String, String>>();
        this.reportPages.put("default", reportPages);
    }

    /**
     * @param reportPages
     */
    public CompatibilityReportPageBuilder(Map<String, Map.Entry<String, String>> reportPages) {
        Map<String, Map.Entry<String, String>> replacedPages = new HashMap<String, Map.Entry<String, String>>();

        for (Map.Entry e : reportPages.entrySet()) {
            replacedPages.put(e.getKey().toString(), new AbstractMap.SimpleEntry<String, String>
                    (stripCss(((Map.Entry) e.getValue()).getKey().toString()),
                            stripCss(((Map.Entry) e.getValue()).getValue().toString())));
        }
        this.reportPages = replacedPages;
    }

    /**
     * @param reportPage
     *
     * @return
     */
    private String stripCss(String reportPage) {
        Pattern pattern = Pattern.compile("<style type=\"text/css\">.+</style>", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(reportPage);
        if (matcher.find()) {
            reportPage = reportPage.replace(matcher.group(0), "");
        }

        pattern = Pattern.compile("<div style='height:999px;'></div>");
        matcher = pattern.matcher(reportPage);
        if (matcher.find()) {
            reportPage = reportPage.replace(matcher.group(0), "");
        }

        return reportPage;
    }

    /**
     * @return
     */
    public Map<String, Map.Entry<String, String>> getReportPages() {
        return reportPages;
    }
}
