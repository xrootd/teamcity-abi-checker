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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="ch.cern.dss.teamcity.server.AbiCheckerConstantsBean"/>

<div class="parameter">
    Reference build type: <strong><props:displayValue name="${constants.referenceBuildTypeNameKey}"/></strong>
</div>

<div class="parameter">
    Reference tag: <strong><props:displayValue name="${constants.referenceTagKey}"/></strong>
</div>

<div class="parameter">
    Artifact type: <strong><props:displayValue name="${constants.artifactTypeKey}"/></strong>
</div>

<div class="parameter">
    Artifact files: <strong><props:displayValue name="${constants.artifactFilesKey}"/></strong>
</div>

<div class="parameter">
    Executable path: <strong><props:displayValue name="${constants.abiCheckerExecutablePathKey}"/></strong>
</div>

<div class="parameter">
    Header files: <strong><props:displayValue name="${constants.artifactHeaderFilesKey}"/></strong>
</div>

<div class="parameter">
    Shared library files: <strong><props:displayValue name="${constants.artifactHeaderFilesKey}"/></strong>
</div>
<div class="parameter">
    GCC options: <strong><props:displayValue name="${constants.gccOptionsKey}" emptyValue="none specified"/></strong>
</div>

