<%@ page contentType="text/html; charset=utf-8" %>
<%--
  ~ Copyright (C) 2025-2026 the Jasper Server OS Authors
  ~ SPDX-License-Identifier: AGPL-3.0-or-later
  ~ Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
  ~ http://www.jaspersoft.com.
  ~
  ~ Unless you have purchased a commercial license agreement from Jaspersoft,
  ~ the following license terms apply:
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program. If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<html>
<head>
  <js:xssNonce/>

  <title><spring:message code='jsp.editFolderForm.title'/></title>
  <meta name="pageHeading" content="<spring:message code='jsp.editFolderForm.pageHeading'/>"/>
</head>

<body>

<table width="100%" border="0" cellpadding="20" cellspacing="0">
  <tr>
    <td>

<form name="fmEditFolder" method="post" action="../">
<table border="0" cellpadding="1" cellspacing="0" align="center">
<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
  <tr>
    <td>&nbsp;</td>
    <td><span class="fsection"><spring:message code='jsp.editFolderForm.folder'/></span></td>
  </tr>
  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>
  <tr>
    <td align="right"><spring:message code='jsp.editFolderForm.parent'/></td>
    <td><input type="text" size="40" value="${requestScope.folderWrapper.actualFolder.parentFolder}" disabled="true" class="fnormal"/></td>
  </tr>

<spring:bind path="folderWrapper.actualFolder.name">
  <tr>
	<td align="right"><spring:message code='jsp.editFolderForm.name'/></td>
	<td>
  <c:if test="${folderWrapper.edit}">
      <input type="text" name="${status.expression}" value="${status.value}" size="40" disabled="true" class="fnormal"/>
  </c:if>
  <c:if test="${!folderWrapper.edit}">
      <input type="text" name="${status.expression}" value="${status.value}" size="40" class="fnormal"/>
  </c:if>
    </td>
  </tr>
  <c:if test="${status.error}">
    <c:forEach items="${status.errorMessages}" var="error">
  <tr>
    <td>&nbsp;</td>
	<td><span class="ferror">${error}</span></td>
  </tr>
    </c:forEach>
  </c:if>
</spring:bind>

<spring:bind path="folderWrapper.actualFolder.label">
  <tr>
    <td align="right"><spring:message code='jsp.editFolderForm.label'/></td>
    <td><input type="text" name="${status.expression}" value="${status.value}" size="40" class="fnormal"/></td>
  </tr>
  <c:if test="${status.error}">
    <c:forEach items="${status.errorMessages}" var="error">
  <tr>
    <td>&nbsp;</td>
    <td><span class="ferror">${error}</span></td>
  </tr>
    </c:forEach>
  </c:if>
</spring:bind>

<spring:bind path="folderWrapper.actualFolder.description">
  <tr>
    <td align="right" valign="top"><spring:message code='jsp.editFolderForm.description'/></td>
    <td><textarea name="${status.expression}" cols="37" rows="4" class="fnormal">${status.value}</textarea></td>
  </tr>
  <c:if test="${status.error}">
    <c:forEach items="${status.errorMessages}" var="error">
  <tr>
    <td>&nbsp;</td>
    <td><span class="ferror">${error}</span></td>
  </tr>
    </c:forEach>
  </c:if>
</spring:bind>

  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>
      <input type="submit" name="_eventId_save" value='<spring:message code="button.save"/>' class="fnormal"/>&nbsp;
      <input type="submit" name="_eventId_cancel" value='<spring:message code="button.cancel"/>' class="fnormal"/>
    </td>
  </tr>

</table>
</form>

    </td>
  </tr>
</table>

</body>

</html>
