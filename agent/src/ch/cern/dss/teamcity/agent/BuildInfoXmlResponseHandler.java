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
