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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses the response to the build info REST API query.
 */
public class BuildInfoXmlResponseHandler extends DefaultHandler {

    private Map<String, String> buildInfo = new HashMap<String, String>();

    /**
     * @return the build type ID extracted from the response XML.
     */
    public String getBuildTypeId() {
        return buildInfo.get("buildTypeId");
    }

    /**
     * @return the build ID extracted from the response XML.
     */
    public String getBuildId() {
        return buildInfo.get("id");
    }

    /**
     * Called when the parser encounters a new element.
     *
     * @param uri        the Namespace URI, or the empty string if the element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  the local name (without prefix), or the empty string if Namespace processing is not being
     *                   performed.
     * @param qName      the qualified name (with prefix), or the empty string if qualified names are not available.
     * @param attributes the attributes attached to the element. If there are no attributes, it shall be an empty
     *                   Attributes object. The value of this object after startElement returns is undefined.
     *
     * @throws SAXException any SAX exception, possibly wrapping another exception.
     */
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
