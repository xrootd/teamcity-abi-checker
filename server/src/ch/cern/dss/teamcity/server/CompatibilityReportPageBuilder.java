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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompatibilityReportPageBuilder {

    Map<String, Map.Entry<String, String>> reportPages;

    /**
     * Constructor for normal build mode.
     *
     * @param reportPages the map of report pages.
     */
    public CompatibilityReportPageBuilder(Map.Entry<String, String> reportPages) {
        reportPages = new AbstractMap.SimpleEntry<String, String>
                (stripCss(reportPages.getKey()), stripCss(reportPages.getValue()));
        this.reportPages = new HashMap<String, Map.Entry<String, String>>();
        this.reportPages.put("default", reportPages);
    }

    /**
     * Constructor for mock build mode.
     *
     * @param reportPages the map of report pages.
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
     * Strip the default CSS from the generated abi-compliance-checker report.
     *
     * @param reportPage the page to strip CSS from.
     *
     * @return the stripped page.
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

        // Deal with other annoying bits of the generated output
        pattern = Pattern.compile("<h1>.+</h1>", Pattern.MULTILINE | Pattern.DOTALL);
        matcher = pattern.matcher(reportPage);
        if (matcher.find()) {
            reportPage = reportPage.replace(matcher.group(0), "");
        }

        return reportPage;
    }

    /**
     * @return the prepared report pages.
     */
    public Map<String, Map.Entry<String, String>> getReportPages() {
        return reportPages;
    }
}
