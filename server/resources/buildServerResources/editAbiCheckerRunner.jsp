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
    var buildType = jQuery('#ui-abi-checker-build-type option:selected').val();

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

    jQuery('#ui-abi-checker-reference-tag').empty();

    xml.find('tag').each(function () {
    tag = jQuery(this).text();
    option = '<option value=&quot;' + tag + '&quot;>' + tag + '</option>';
    jQuery('#ui-abi-checker-reference-tag').append(option);
    });

    jQuery('#ui-abi-checker-reference-tag option:first-child').attr('selected', true);
    }
    };
    TagHandler.requestTags();
</c:set>

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
                <props:option value="tag"><c:out value="${tag}"/></props:option>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${abiCheckerBean.referenceTagKey}"></span>
        <span class="smallNote">Available tags will appear once you select a build type.</span>
    </td>
</tr>
<tr>
    <th><label for="${abiCheckerBean.abiCheckerExecutablePathKey}">Executable path: </label></th>
    <td>
        <props:textProperty name="${abiCheckerBean.abiCheckerExecutablePathKey}" className="longField" maxlength="256"/>
        <span class="error" id="error_${abiCheckerBean.abiCheckerExecutablePathKey}"></span>
        <span class="smallNote">Specify the path to the <b>abi-compliance-checker</b> executable.</span>
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
                                   id="${abiCheckerBean.artifactTypeRpmKey}"
                                   checked="true"/>
        <label for="${abiCheckerBean.artifactTypeRpmKey}">RPM</label><br/>

        <props:radioButtonProperty name="${abiCheckerBean.artifactTypeKey}"
                                   value="${abiCheckerBean.artifactTypeArchiveKey}"
                                   id="${abiCheckerBean.artifactTypeArchiveKey}"/>
        <label for="${abiCheckerBean.artifactTypeArchiveKey}">Archive</label><br/>

        <props:radioButtonProperty name="${abiCheckerBean.artifactTypeKey}"
                                   value="${abiCheckerBean.artifactTypeFolderKey}"
                                   id="${abiCheckerBean.artifactTypeFolderKey}"/>
        <label for="${abiCheckerBean.artifactTypeFolderKey}">Folder</label><br/>
        <span class="smallNote">Select which type of artifact this is.</span>
    </td>
</tr>
<tr>
    <th><label for="${abiCheckerBean.artifactHeaderPathKey}">Header path: </label></th>
    <td>
        <props:textProperty name="${abiCheckerBean.artifactHeaderPathKey}" className="longField" maxlength="256"/>
        <span class="error" id="error_${abiCheckerBean.artifactHeaderPathKey}"></span>
        <span class="smallNote">Specify the path <b>(inside the artifact)</b> where the header files are located
        (e.g. /usr/include/foo/bar.h).</span>
    </td>
</tr>
<tr>
    <th><label for="${abiCheckerBean.artifactLibraryPathKey}">Shared library path: </label></th>
    <td>
        <props:textProperty name="${abiCheckerBean.artifactLibraryPathKey}" className="longField" maxlength="256"/>
        <span class="error" id="error_${abiCheckerBean.artifactLibraryPathKey}"></span>
        <span class="smallNote">Specify the path <b>(inside the artifact)</b> where the shared library files are
        located (e.g. /usr/lib64/foo.so)</span>
    </td>
</tr>