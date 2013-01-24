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

<jsp:useBean id="constants" class="ch.cern.dss.teamcity.server.AbiCheckerConstantsBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<c:set var="project" value="${buildForm.settingsBuildType.project}"/>
<c:set var="firstBuildType" value="${project.buildTypes[0]}"/>
<c:set var="changeTags">
    var TagHandler = {
    requestTags: function () {
    var buildType = jQuery('#${constants.referenceBuildTypeKey} option:selected').val();
    var buildTypeName = jQuery('#${constants.referenceBuildTypeKey} option:selected').text();
    jQuery('#${constants.referenceBuildTypeNameKey}').val(buildTypeName);

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

    jQuery('#${constants.referenceTagKey}').empty();

    xml.find('tag').each(function () {
    tag = jQuery(this).text();
    option = '<option value=&quot;' + tag + '&quot;>' + tag + '</option>';
    jQuery('#${constants.referenceTagKey}').append(option);
    });

    jQuery('#${constants.referenceTagKey} option:first-child').attr('selected', true);
    }
    };
    TagHandler.requestTags();
</c:set>

<layout:settingsGroup title="Reference Build Settings">
    <tr>
        <th><label for="${constants.referenceBuildTypeKey}">Reference build type: </label></th>
        <td>
            <props:selectProperty name="${constants.referenceBuildTypeKey}"
                                  onchange="${changeTags}">
                <c:forEach var="item" items="${project.buildTypes}">
                    <props:option value="${item.id}"><c:out value="${item.name}"/></props:option>
                </c:forEach>
            </props:selectProperty>
            <props:hiddenProperty name="${constants.referenceBuildTypeNameKey}"
                                  value="${firstBuildType.name}"/>
            <span class="error" id="error_${constants.referenceBuildTypeKey}"></span>
            <span class="smallNote">Select the build type which contains the artifacts you wish to check
            ABI compatibility with.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${constants.referenceTagKey}">Reference tag: </label></th>
        <td>
            <props:selectProperty name="${constants.referenceTagKey}">
                <c:forEach var="tag" items="${firstBuildType.tags}">
                    <props:option value="${tag}"><c:out value="${tag}"/></props:option>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_${constants.referenceTagKey}"></span>
            <span class="smallNote">Select the tagged build you wish to compare ABI compatibility with.</span>
        </td>
    </tr>
</layout:settingsGroup>
<layout:settingsGroup title="Reference Artifact Settings">
    <tr>
        <th><label>Build mode:</label></th>
        <td>
            <c:set var="onclick">
                BS.Util.show('artifactTypeSection');
            </c:set>
            <props:radioButtonProperty name="${constants.buildModeKey}"
                                       value="${constants.buildModeNormalKey}"
                                       onclick="${onclick}"
                                       checked="true"/>
            <label for="${constants.buildModeNormalKey}">Normal build</label>

            <c:set var="onclick">
                BS.Util.hide('artifactTypeSection');
            </c:set>
            <props:radioButtonProperty name="${constants.buildModeKey}"
                                       value="${constants.buildModeMockKey}"
                                       onclick="${onclick}"/>
            <label for="${constants.buildModeMockKey}">Mock build</label>

            <span class="smallNote">ABI Compatibility Checker supports the use of artifacts that were built for
            multiple architectures inside a <code>mock</code> environment. If you select the <code>mock</code>
            build mode, the plugin will search for the <code>meta</code> folder in the artifact root, and run the
            compatibility check inside mock for all listed architectures.</span>
        </td>
    </tr>
    <tr id="artifactTypeSection">
        <th><label>Artifact type:</label></th>
        <td>
            <props:radioButtonProperty name="${constants.artifactTypeKey}"
                                       value="${constants.artifactTypeRpmKey}"
                                       checked="true"/>
            <label for="${constants.artifactTypeRpmKey}">${constants.artifactTypeRpmKey}</label><br/>

            <props:radioButtonProperty name="${constants.artifactTypeKey}"
                                       value="${constants.artifactTypeArchiveKey}"/>
            <label for="${constants.artifactTypeArchiveKey}">${constants.artifactTypeArchiveKey}</label><br/>

            <props:radioButtonProperty name="${constants.artifactTypeKey}"
                                       value="${constants.artifactTypeFolderKey}"/>
            <label for="${constants.artifactTypeFolderKey}">${constants.artifactTypeFolderKey}</label><br/>

            <span class="smallNote">Select which type of artifact this is.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${constants.artifactFilesKey}">Artifact files: </label></th>
        <td>
            <props:multilineProperty name="${constants.artifactFilesKey}"
                                     className="longField"
                                     linkTitle="Type artifact files or wildcards"
                                     cols="55" rows="5"
                                     expanded="true"/>
            <span class="error" id="error_${constants.artifactFilesKey}"></span>
        <span class="smallNote">Enter paths to the artifacts which contain the source headers and shared object
            libraries, separated by newlines. Wildcards accepted (e.g. x86_64/foo-*.rpm)</span>
        </td>
    </tr>
</layout:settingsGroup>

<layout:settingsGroup title="ABI Compatibility Checker Settings">
    <tr>
        <th><label for="${constants.abiCheckerExecutablePathKey}">Executable path: </label></th>
        <td>
            <props:textProperty name="${constants.abiCheckerExecutablePathKey}" className="longField"
                                maxlength="256"/>
            <span class="error" id="error_${constants.abiCheckerExecutablePathKey}"></span>
            <span class="smallNote">Specify the path (on the build agent) to the <b>abi-compliance-checker</b>
                executable.</span>
        </td>
    </tr>
    <tr>
        <th><label for="${constants.artifactHeaderFilesKey}">Header files: </label></th>
        <td>
            <props:multilineProperty name="${constants.artifactHeaderFilesKey}"
                                     className="longField"
                                     linkTitle="Type header files or wildcards"
                                     cols="55" rows="5"
                                     expanded="true"/>
            <span class="error" id="error_${constants.artifactHeaderFilesKey}"></span>
        <span class="smallNote">Specify the header files <b>(relative to the extracted artifact)</b> to be checked.
            Wildcards accepted (e.g. include/foo/*.h).</span>
        </td>
    </tr>
    <tr>
        <th><label for="${constants.artifactLibraryFilesKey}">Shared library files: </label></th>
        <td>
            <props:multilineProperty name="${constants.artifactLibraryFilesKey}"
                                     className="longField"
                                     linkTitle="Type shared library files or wildcards"
                                     cols="55" rows="5"
                                     expanded="true"/>
            <span class="error" id="error_${constants.artifactLibraryFilesKey}"></span>
        <span class="smallNote">Specify the shared library files <b>(relative to the extracted artifact)</b> to be
            checked. Wildcards accepted (e.g. lib64/*.so)</span>
        </td>
    </tr>
    <tr>
        <th><label for="${constants.gccOptionsKey}">GCC options: </label></th>
        <td>
            <props:multilineProperty name="${constants.gccOptionsKey}"
                                     className="longField"
                                     linkTitle="Type gcc options (one per line)"
                                     cols="55" rows="5"
                                     expanded="true"/>
            <span class="error" id="error_${constants.gccOptionsKey}"></span>
        <span class="smallNote">Specify any additional GCC compiler options <b>(one per line)</b>, e.g.
        <em>-D_LARGEFILE_SOURCE</em></span>
        </td>
    </tr>
</layout:settingsGroup>