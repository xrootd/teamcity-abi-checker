<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:useBean id="abiCheckerBean" class="ch.cern.dss.teamcity.server.AbiCheckerBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>


<tr>
    <th>
        <label for="${abiCheckerBean.projectNameKey}">Project: </label>
    </th>
    <td>
        <props:selectProperty name="${abiCheckerBean.projectNameKey}">
            <c:forEach var="item" items="${propertiesBean.getProperties()}">
                <c:set var="requiredKey" value="${abiCheckerBean.projectNameKey}${item.value}" />
                <c:if test="${item.key == requiredKey}" >
                    <props:option value="${item.value}"><c:out value="${item.value}"/></props:option>
                </c:if>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${abiCheckerBean.projectNameKey}"></span>
        <span class="smallNote">Select the project which contains the artifacts you wish to compare against.</span>
    </td>
</tr>
<tr>
    <th>
        <label for="${abiCheckerBean.buildTypeKey}">Build type: </label>
    </th>
    <td>
        <props:selectProperty name="${abiCheckerBean.buildTypeKey}">
            <c:forEach var="item" items="${propertiesBean.getProperties()}">
                <c:set var="requiredKey" value="${abiCheckerBean.buildTypeKey}${item.value}" />
                <c:if test="${item.key == requiredKey}" >
                    <props:option value="${item.value}"><c:out value="${item.value}"/></props:option>
                </c:if>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${abiCheckerBean.buildTypeKey}"></span>
        <span class="smallNote">Select the build type which contains the artifacts you wish to compare against.</span>
    </td>
</tr>
<tr>
    <th>
        <label for="${abiCheckerBean.referenceTagKey}">Reference tag: </label>
    </th>
    <td>
        <props:selectProperty name="${abiCheckerBean.referenceTagKey}">
            <c:forEach var="item" items="${propertiesBean.getProperties()}">
                <c:set var="requiredKey" value="${abiCheckerBean.referenceTagKey}${item.value}" />
                <c:if test="${item.key == requiredKey}" >
                    <props:option value="${item.value}"><c:out value="${item.value}"/></props:option>
                </c:if>
            </c:forEach>
        </props:selectProperty>
        <span class="error" id="error_${abiCheckerBean.referenceTagKey}"></span>
        <span class="smallNote">Select the reference tag you wish to compare against.</span>
    </td>
</tr>