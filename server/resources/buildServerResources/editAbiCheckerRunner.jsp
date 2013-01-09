<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="ccore" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="abiCheckerBean" class="ch.cern.dss.teamcity.server.AbiCheckerBean"/>

<tr>
    <th>
        <label for="${abiCheckerBean.referenceTagKey}">Reference: </label>
    </th>
    <td>
        <props:textProperty name="${abiCheckerBean.referenceTagKey}" className="longField" maxlength="256" />
        <span class="smallNote">Enter tag to compare against.</span>
    </td>
</tr>