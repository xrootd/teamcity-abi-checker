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

package ch.cern.dss.teamcity.agent;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class BuildInfoXmlResponseHandler extends DefaultHandler {

    private Map<String, String> buildInfo = new HashMap<String, String>();

    public Map<String, String> getBuildInfo() {
        return this.buildInfo;
    }

    public String getArtifactDownloadUrl() {
        return "/guestAuth/repository/downloadAll/" + buildInfo.get("buildTypeId") + "/" + buildInfo.get("id") + ":id"
                + "/artifacts.zip";
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("build")) {
            buildInfo.put("id", attributes.getValue("id"));
            buildInfo.put("webUrl", attributes.getValue("webUrl"));
        }

        if (qName.equalsIgnoreCase("buildType")) {
            buildInfo.put("buildTypeId", attributes.getValue("id"));
        }
    }
}
