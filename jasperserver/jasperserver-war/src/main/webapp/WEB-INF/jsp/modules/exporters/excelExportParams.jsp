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
  <title><spring:message code='jsp.excelExportParams.title'/></title>
  <meta name="pageHeading" content="<spring:message code='jsp.excelExportParams.pageHeading'/>"/>
</head>

<body>
<table width="100%" border="0" cellpadding="20" cellspacing="0">
  <tr>
    <td>

<form name="excelExportParametersForm" method="post" action="">
<table border="0" cellpadding="1" cellspacing="0" align="center">
<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
  <tr>
    <td>&nbsp;</td>
    <td><span class="fsection"><spring:message code='jsp.excelExportParams.title'/></span></td>
  </tr>
  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>
   <spring:bind path="excelExportParams.onePagePerSheet">
	<tr>
		<td align="right"><spring:message code="jsp.excelExportParams.is.one.page.per.sheet"/>&nbsp;</td>
		<td align="left">
			<input name="_${status.expression}" type="hidden"/>
			<input name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if> size="30"/>
		</td>
	</tr>
   </spring:bind>
  
   <spring:bind path="excelExportParams.detectCellType">
	<tr>
		<td align="right"><spring:message code="jsp.excelExportParams.is.detect.cell.type"/>&nbsp;</td>
		<td align="left">
			<input name="_${status.expression}" type="hidden"/>
			<input name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if> size="30"/>
		</td>
	</tr>
   </spring:bind>
  
   <spring:bind path="excelExportParams.removeEmptySpaceBetweenRows">
	<tr>
		<td align="right"><spring:message code="jsp.excelExportParams.is.remove.blank.space"/>&nbsp;</td>
		<td align="left">
			<input name="_${status.expression}" type="hidden"/>
			<input name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if> size="30"/>
		</td>
	</tr>
   </spring:bind>
  
   <spring:bind path="excelExportParams.whitePageBackground">
    <tr>
        <td align="right"><spring:message code="jsp.excelExportParams.is.white.page.background"/>&nbsp;</td>
        <td align="left">
            <input name="_${status.expression}" type="hidden"/>
            <input name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if> size="30"/>
        </td>
    </tr>
   </spring:bind>

   <spring:bind path="excelExportParams.ignoreGraphics">
    <tr>
        <td align="right"><spring:message code="jsp.excelExportParams.is.ignore.graphics"/>&nbsp;</td>
        <td align="left">
            <input name="_${status.expression}" type="hidden"/>
            <input name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if> size="30"/>
        </td>
    </tr>
   </spring:bind>

   <spring:bind path="excelExportParams.collapseRowSpan">
    <tr>
        <td align="right"><spring:message code="jsp.excelExportParams.is.collapse.row.span"/>&nbsp;</td>
        <td align="left">
            <input name="_${status.expression}" type="hidden"/>
            <input name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if> size="30"/>
        </td>
    </tr>
   </spring:bind>

   <spring:bind path="excelExportParams.ignoreCellBorder">
    <tr>
        <td align="right"><spring:message code="jsp.excelExportParams.is.ignore.cell.border"/>&nbsp;</td>
        <td align="left">
            <input name="_${status.expression}" type="hidden"/>
            <input name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if> size="30"/>
        </td>
    </tr>
   </spring:bind>

   <spring:bind path="excelExportParams.fontSizeFixEnabled">
    <tr>
        <td align="right"><spring:message code="jsp.excelExportParams.is.font.size.fix.enabled"/>&nbsp;</td>
        <td align="left">
            <input name="_${status.expression}" type="hidden"/>
            <input name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if> size="30"/>
        </td>
    </tr>
   </spring:bind>

  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>
		<input type="submit" name="_eventId_submit" value='<spring:message code="button.submit"/>' class="fnormal"/>&nbsp;
		<input type="button" name="btnclose" value='<spring:message code="button.close"/>' class="fnormal" onclick="javascript:window.close()"/>
    </td>
  </tr>

</table>
</form>

    </td>
  </tr>
</table>
</body>

</html>
