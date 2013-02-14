<%--
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
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="constants" class="ch.cern.dss.teamcity.server.AbiCheckerConstantsBean"/>
<jsp:useBean id="reportPages" type="java.util.HashMap" scope="request"/>
<jsp:useBean id="buildMode" type="java.lang.String" scope="request"/>

<div id="outer-container" class="tab-container">
    
    <c:choose>
        <c:when test="${buildMode == constants.buildModeMockKey}">
            <ul class='tabs-nav'>
                <c:forEach var="item" items="${reportPages}">
                    <li class='tab'><a href="#${item.key}">${item.key}</a></li>
                </c:forEach>
            </ul>

            <c:forEach var="item" items="${reportPages}">
                <div id="${item.key}">
                    <div class="panel-container" id="${item.key}-inner">
                        <ul class="tabs-nav">
                            <li class="tab"><a href="#${item.key}-abi">Binary Compatibility</a></li>
                            <li class="tab"><a href="#${item.key}-src">Source Compatibility</a></li>
                        </ul>

                        <div id="${item.key}-abi">
                            ${item.value.key}
                        </div>
                        <div id="${item.key}-src">
                            ${item.value.value}
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:when>

        <c:otherwise>
            <ul class='tabs-nav'>
                <li class='tab'><a href="#abi">Binary Compatibility</a></li>
                <li class='tab'><a href="#src">Source Compatibility</a></li>
            </ul>

            <c:forEach var="item" items="${reportPages}">
                <div id="${item.key}">
                    <div id="abi">
                        ${item.value.key}
                    </div>
                    <div id="src">
                        ${item.value.value}
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<script>
    jQuery(document).ready(function () {
        jQuery('#outer-container').easytabs({animate: false});
        <c:forEach var="item" items="${reportPages}">
            jQuery('#${item.key}-inner').easytabs({animate: false});
        </c:forEach>
    });
</script>
