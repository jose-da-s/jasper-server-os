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
<%@ taglib uri="/WEB-INF/jasperserver.tld" prefix="js"%>
 
<html>
<head>
	<js:xssNonce/>

	<title><spring:message code='jsp.ListReports.title'/></title>
  <meta name="pageHeading" content="<spring:message code='jsp.ListReports.pageHeading'/>"/>
  </head>

<body>
<%-- "$Id$" --%>
<table width="100%" border="0" cellpadding="20" cellspacing="0">
  <tr>
    <td>

<span class="fsection"><spring:message code='jsp.ListReports.section'/></span>
<br/>
<br/>
<form name="fmLstRpts" method="post" action="../">
<table border="0" width="100%" cellpadding="0" cellspacing="0">
	<input type="submit" class="fnormal" name="_eventId_ScheduleReport" id="scheduleReport" value="schedule" style="visibility:hidden;"/>
	<input type="submit" class="fnormal" name="_eventId_runReportInBackground" id="runReportInBackground" value="runReportInBackground" style="visibility:hidden;"/>
	<input type="hidden" name="reportUnit"/>
	<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
  <tr bgcolor="#c2c4b6" class="fheader">
    <td class="paddedcell" width="20%"><spring:message code='jsp.ListReports.report_name'/></td>
    <td class="paddedcell" width="40%"><spring:message code='jsp.ListReports.description'/></td>
    <td class="paddedcell" width="10%"><spring:message code='jsp.ListReports.date'/></td>
    <td class="paddedcell" width="10%"><spring:message code='RM_CREATE_FOLDER_PARENT_FOLDER'/></td>
    
  </tr>
<js:paginator items="${reportUnits}" page="${currentPage}" formName="fmLstRpts">
<c:forEach var="reportUnit" items="${paginatedItems}" varStatus="itStatus">
  <tr heigth="18" <c:if test="${itStatus.count % 2 == 0}">class="list_alternate"</c:if>>
	<td class="paddedcell">
		<table width="100%" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td>
				<c:url var="reportExecutionURL" value="flow.html">
					<c:param name="_eventId" value="selectReport"/>
					<c:param name="reportUnit" value="${reportUnit.URIString}"/>
					<c:param name="_flowExecutionKey" value="${flowExecutionKey}"/>
				</c:url>
				<a href="${reportExecutionURL}">${reportUnit.label}</a>
				</td>
				<td align="right" valign="middle" nowrap>
					<a href="javascript:document.fmLstRpts.reportUnit.value='${reportUnit.URIString}';document.fmLstRpts.scheduleReport.click();" title="<spring:message code="repository.browser.schedule.hint"/>">
						<img border="0" src="images/schedule.gif" alt="<spring:message code="repository.browser.schedule.hint"/>"/>
					</a>
					<a href="javascript:document.fmLstRpts.reportUnit.value='${reportUnit.URIString}';document.fmLstRpts.runReportInBackground.click();" title="<spring:message code="repository.browser.run.in.background.hint"/>">
						<img border="0" src="images/runreport.gif" alt="<spring:message code="repository.browser.run.in.background.hint"/>"/>
					</a>
				</td>
			</tr>
		</table>
	</td>
    <td class="paddedcell">${reportUnit.description}</td>
    <td class="paddedcell" nowrap><js:formatDate value="${reportUnit.creationDate}"/></td>
    <td class="paddedcell" nowrap>   
     <a href="ListReports.jsp#" onclick="gotoFolderExplorer('${reportUnit.parentFolder}')">
      ${reportUnit.parentFolder}
     </a> 
    </td>
  </tr>
</c:forEach>
	<tr>
		<td class="paddedcell" colspan="4">
			<js:paginatorLinks/>
		</td>
	</tr>
</js:paginator>
</table>
</form>

    </td>
  </tr>
</table>

</body>

</html>


