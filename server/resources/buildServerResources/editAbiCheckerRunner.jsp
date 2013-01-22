<%--
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
--%>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:useBean id="abiCheckerBean" class="ch.cern.dss.teamcity.server.AbiCheckerBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<c:set var="project" value="${buildForm.settingsBuildType.project}"/>
<c:set var="changeTags">
    var TagHandler = {
    requestTags: function () {
    var buildType = jQuery('#${abiCheckerBean.buildTypeKey} option:selected').val();

    BS.ajaxRequest('/requestTags.html', {
    parameters: 'buildTypeId=' + buildType,
    onComplete: function (transport) {
    if (transport.responseXML) {
    BS.XMLResponse.processErrors(transport.responseXML, {
    onAbiCheckerProblemError: function (elem) {
    alert(elem.firstChild.nodeValue);
    }
    });
    TagHandler.fillTags(transport.responseText);
    }
    }
    });
    return false;
    },

    fillTags: function (tags) {
    var xmlDoc = jQuery.parseXML(tags);
    var xml = jQuery(xmlDoc);

    jQuery('#${abiCheckerBean.referenceTagKey}').empty();

    xml.find('tag').each(function () {
    tag = jQuery(this).text();
    option = '<option value=&quot;' + tag + '&quot;>' + tag + '</option>';
    jQuery('#${abiCheckerBean.referenceTagKey}').append(option);
    });

    jQuery('#${abiCheckerBean.referenceTagKey} option:first-child').attr('selected', true);
    }
    };
    TagHandler.requestTags();
</c:set>

<layout:settingsGroup title="Reference Build Settings">
    <tr>
        <th><label for="${abiCheckerBean.buildTypeKey}">Reference build type: </label></th>
        <td>
            <props:selectProperty name="${abiCheckerBean.buildTypeKey}"
                                  onchange="${changeTags}">
                <c:forEach var="item" items="${project.buildTypes}">
                    <props:option value="${item.id}"><c:out value="${item.name}"/></props:option>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_${abiCheckerBean.buildTypeKey}"></span>
            <span class="smallNote">Select the build type which contains the artifacts you wish to check
            ABI compatibility with.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${abiCheckerBean.referenceTagKey}">Reference tag: </label></th>
        <td>
            <props:selectProperty name="${abiCheckerBean.referenceTagKey}">
                <c:set var="firstBuildType" value="${project.buildTypes[0]}"/>
                <c:forEach var="tag" items="${firstBuildType.tags}">
                    <props:option value="${tag}"><c:out value="${tag}"/></props:option>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_${abiCheckerBean.referenceTagKey}"></span>
            <span class="smallNote">Select the tagged build you wish to compare ABI compatibility with.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${abiCheckerBean.artifactFilesKey}">Artifact files: </label></th>
        <td>
            <props:multilineProperty name="${abiCheckerBean.artifactFilesKey}"
                                     className="longField"
                                     linkTitle="Type artifact files or wildcards"
                                     cols="55" rows="5"
                                     expanded="true"/>
            <span class="error" id="error_${abiCheckerBean.artifactFilesKey}"></span>
        <span class="smallNote">Enter paths to the artifacts which contain the source headers and
        shared object libraries,<br/> separated by newlines. Wildcards accepted (e.g. x86_64/foo-*.rpm)</span>
        </td>
    </tr>
    <tr>
        <th><label>Artifact type:</label></th>
        <td>
            <props:radioButtonProperty name="${abiCheckerBean.artifactTypeKey}"
                                       value="${abiCheckerBean.artifactTypeRpmKey}"
                                       checked="true"/>
            <label for="${abiCheckerBean.artifactTypeRpmKey}">${abiCheckerBean.artifactTypeRpmKey}</label><br/>

            <props:radioButtonProperty name="${abiCheckerBean.artifactTypeKey}"
                                       value="${abiCheckerBean.artifactTypeArchiveKey}"/>
            <label for="${abiCheckerBean.artifactTypeArchiveKey}">${abiCheckerBean.artifactTypeArchiveKey}</label><br/>

            <props:radioButtonProperty name="${abiCheckerBean.artifactTypeKey}"
                                       value="${abiCheckerBean.artifactTypeFolderKey}"/>
            <label for="${abiCheckerBean.artifactTypeFolderKey}">${abiCheckerBean.artifactTypeFolderKey}</label><br/>
            <span class="smallNote">Select which type of artifact this is.</span>
        </td>
    </tr>
</layout:settingsGroup>

<layout:settingsGroup title="ABI Compatibility Checker Settings">
    <tr>
        <th><label for="${abiCheckerBean.abiCheckerExecutablePathKey}">Executable path: </label></th>
        <td>
            <props:textProperty name="${abiCheckerBean.abiCheckerExecutablePathKey}" className="longField"
                                maxlength="256"/>
            <span class="error" id="error_${abiCheckerBean.abiCheckerExecutablePathKey}"></span>
            <span class="smallNote">Specify the path (on the build agent) to the <b>abi-compliance-checker</b>
                executable.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${abiCheckerBean.artifactHeaderFilesKey}">Header files: </label></th>
        <td>
            <props:multilineProperty name="${abiCheckerBean.artifactHeaderFilesKey}"
                                     className="longField"
                                     linkTitle="Type header files or wildcards"
                                     cols="55" rows="5"
                                     expanded="true"/>
            <span class="error" id="error_${abiCheckerBean.artifactHeaderFilesKey}"></span>
        <span class="smallNote">Specify the header files <b>(inside the artifact)</b> to be checked. Wildcards accepted
            (e.g. include/foo/*.h).</span>
        </td>
    </tr>
    <tr>
        <th><label for="${abiCheckerBean.artifactLibraryFilesKey}">Shared library files: </label></th>
        <td>
            <props:multilineProperty name="${abiCheckerBean.artifactLibraryFilesKey}"
                                     className="longField"
                                     linkTitle="Type shared library files or wildcards"
                                     cols="55" rows="5"
                                     expanded="true"/>
            <span class="error" id="error_${abiCheckerBean.artifactLibraryFilesKey}"></span>
        <span class="smallNote">Specify the shared library files <b>(inside the artifact)</b> to be checked. Wildcards
            accepted (e.g. lib64/*.so)</span>
        </td>
    </tr>
    <tr>
        <th><label for="${abiCheckerBean.gccOptionsKey}">GCC options: </label></th>
        <td>
            <props:multilineProperty name="${abiCheckerBean.gccOptionsKey}"
                                     className="longField"
                                     linkTitle="Type gcc options (one per line)"
                                     cols="55" rows="5"
                                     expanded="true"/>
            <span class="error" id="error_${abiCheckerBean.gccOptionsKey}"></span>
        <span class="smallNote">Specify any additional GCC compiler options <b>(one per line)</b>, e.g.
        <em>-D_LARGEFILE_SOURCE</em></span>
        </td>
    </tr>
</layout:settingsGroup>