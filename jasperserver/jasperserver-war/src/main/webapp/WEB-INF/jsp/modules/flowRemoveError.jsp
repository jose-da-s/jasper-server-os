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

<%@ page isErrorPage="true"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<html>

<head>
  <js:xssNonce/>

  <title><spring:message code='jsp.flowRemoveError.title'/></title>
  <LINK href="/stylesheets/styles.css" type="text/css" rel="stylesheet">
  <LINK href="/stylesheets/base.css" type="text/css" rel="stylesheet">
  <meta name="pageHeading" content='<spring:message code="jsp.flowRemoveError.pageHeading"/>'/>
  <meta name="noMenu" content="true">
</head>

<body>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<form name="fmErrorPage" method="post">
<table width="100%" cellpadding="0" cellspacing="0" border="0">
<tr><td align="center" class="ferror"><spring:message code='jsp.flowRemoveError.errorMsg1'/></td></tr>
<tr><td>&nbsp;</td></tr>
<c:forEach items="${requestScope.failedResources}" var="resource">
<tr><td align="center">${resource}</td></tr>
</c:forEach>
<tr><td>&nbsp;</td></tr>
<tr><td>&nbsp;</td></tr>
<tr><td>&nbsp;</td></tr>
<tr><td width="100%" align="center">
<input type="submit" class="fnormal" name="_eventId_back" id="back" value="edit" style="visibility:hidden;"/>
<a href="javascript:document.fmErrorPage.back.click()"/><spring:message code='jsp.flowError.errorMsg2'/></a>
</td></tr>


</table>
</form>

</body>

</html>


