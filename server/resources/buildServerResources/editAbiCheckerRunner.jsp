<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:useBean id="abiCheckerBean" class="ch.cern.dss.teamcity.server.AbiCheckerBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<c:set var="project" value="${buildForm.settingsBuildType.project}"/>

<bs:linkScript>
    ${propertiesBean.properties[abiCheckerBean.JSLocationKey]}
</bs:linkScript>

<tr>
    <th><label for="${abiCheckerBean.buildTypeKey}">Build type: </label></th>
    <td>
        <bs:refreshable containerId="abiCheckerComponent" pageUrl="${pageUrl}">

            <props:selectProperty name="${abiCheckerBean.buildTypeKey}"
                                  onchange="return TagRequester.requestTags();">
                <c:forEach var="item" items="${project.buildTypes}">
                    <props:option value="${item}"><c:out value="${item.name}"/></props:option>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_${abiCheckerBean.buildTypeKey}"></span>
            <span class="smallNote">Select the build type which contains the artifacts you wish to check
            ABI compatibility with.</span>

            <script>
                jQuery(document).ready(function () {
                    TagRequester.requestTags();
                });

                var TagRequester = {
                    requestTags: function () {
                        var buildType = jQuery("#${abiCheckerBean.buildTypeKey} option:selected").val();
                        var buildTypeId = buildType.match(/\{id=(bt.*)\}/)[1];

                        BS.ajaxRequest("/requestTags.html", {
                            parameters: 'buildTypeId=' + buildTypeId,
                            onComplete: function (transport) {
                                if (transport.responseXML) {
                                    BS.XMLResponse.processErrors(transport.responseXML, {
                                        onAbiCheckerProblemError: function (elem) {
                                            alert(elem);
                                        }
                                    });
                                    TagRequester.fillTags(transport.responseText);
                                }
                            }
                        });
                        return false;
                    },

                    fillTags: function (tags) {
                        var xmlDoc = jQuery.parseXML(tags);
                        var xml = jQuery(xmlDoc);

                        jQuery("#${abiCheckerBean.referenceTagKey}").empty();

                        xml.find("tag").each(function () {
                            tag = jQuery(this).text()
                            option = "<option value='" + tag + "'>" + tag + "</option>";
                            jQuery("#${abiCheckerBean.referenceTagKey}").append(option);
                        });

                        jQuery("#${abiCheckerBean.referenceTagKey} option:first-child").attr("selected", true);
                    }
                };
            </script>
        </bs:refreshable>
    </td>
</tr>
<tr>
    <th><label for="${abiCheckerBean.referenceTagKey}">Reference tag: </label></th>
    <td>
        <props:selectProperty name="${abiCheckerBean.referenceTagKey}"></props:selectProperty>
        <span class="error" id="error_${abiCheckerBean.referenceTagKey}"></span>
        <span class="smallNote">Available tags will appear once you select a build type.</span>
    </td>
</tr>
<tr>
    <th><label for="${abiCheckerBean.customArtifactPath}">Artifact path (optional): </label></th>
    <td>
        <props:textProperty name="${abiCheckerBean.customArtifactPath}" className="longField" maxlength="256"/>
        <span class="smallNote">Enter this build type's artifact path, or leave blank to use the same path
        as the reference.</span>
    </td>
</tr>