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

<%@ page language="java" %>

<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/WEB-INF/jasperserver.tld" prefix="js"%>

<%--TODO The page is broken and is not used anywhere.  REMOVE with repoAdminFlow.xml  --%>

<html>
<head>
  <js:xssNonce/>
  <script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/scripts/components.checkbox-utils.js"></script>
  <script type="text/javascript" language="JavaScript">
  	function removeRepositoryItems() {
  		if (checkboxListAnySelected('repositoryItems')) {
	  		if (confirm('<spring:message code="jsp.repoAdmin.defaultView.confirmRemove" javaScriptEscape="true"/>')) {
  				document.frm.remove.click();
  			}
  		} else {
  			alert('<spring:message code="jsp.repoAdmin.defaultView.nothing.to.remove" javaScriptEscape="true"/>');
  		}
  	}
  </script>
</head>

<body>

<table width="100%" border="0" cellpadding="20" cellspacing="0">
  <tr>
    <td>

<span class="fsection"><spring:message code="jsp.repoAdmin.defaultView.header"/></span>
<br/>
<br/>
<form name="frm" action="" method="post">
<table border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td>
<spring:message code="jsp.repoAdmin.defaultView.path"/>: <a href="<c:url value="flow.html"><c:param name="_flowId" value="repoAdminFlow"/></c:url>"><spring:message code="jsp.repoAdmin.defaultView.root"/></a>
<c:set var="lastFolder" value="/"/>
<c:forEach items="${requestScope.pathFolders}" var="folder">
/<a href="<c:url value="flow.html"><c:param name="_flowId" value="repoAdminFlow"/><c:param name="folder" value="${folder.URIString}"/></c:url>">${folder.name}</a>
   <c:set var="lastFolder" value="${folder.URIString}"/>
</c:forEach>
    </td>
    <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td><a href="javascript:document.frm.resource.value='${lastFolder}';document.frm.assign.click();"><spring:message code="jsp.repoAdmin.defaultView.assign.permissions"/></a></td>
  </tr>
</table>

  <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
  <input type="hidden" name="resource"/>
  <input type="hidden" name="resourceType"/>
  <input type="submit" class="fnormal" name="_eventId_Edit" id="edit" value="edit" style="visibility:hidden;"/>
  <input type="submit" class="fnormal" name="_eventId_ViewReport" id="viewReport" value="view" style="visibility:hidden;"/>
  <input type="submit" class="fnormal" name="_eventId_ViewOlapModel" id="viewOlapModel" value="view" style="visibility:hidden;"/>
  <input type="submit" class="fnormal" name="_eventId_ScheduleReport" id="scheduleReport" value="schedule" style="visibility:hidden;"/>
  <input type="submit" class="fnormal" name="_eventId_runReportInBackground" id="runReportInBackground" value="runReportInBackground" style="visibility:hidden;"/>
  <input type="submit" name="_eventId_Remove" id="remove" value="remove" style="visibility:hidden;"/>
  <input type="submit" name="_eventId_Assign" id="assign" value="assign" style="visibility:hidden;"/>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr bgcolor="#c2c4b6" class="fheader">
    <td class="paddedcell" width="20%"><spring:message code="label.name"/></td>
    <td class="paddedcell"><spring:message code="label.label"/></td>
    <td class="paddedcell" width="20%"><spring:message code="label.type"/></td>
    <td class="paddedcell" width="12%"><spring:message code="label.date"/></td>
    <td class="paddedcell" width="12%" align="center"><spring:message code="jsp.repoAdmin.defaultView.capitalEdit"/></td>
    <td class="paddedcell" width="12%" align="center"><spring:message code="jsp.repoAdmin.defaultView.permissions"/></td>
    <td class="paddedcell" width="10">
		<input type="checkbox" name="selectAll" class="fnormal" 
			onclick="checkboxListAllClicked('repositoryItems', this)"
			title="<spring:message code="list.checkbox.select.all.hint"/>"/>
    </td>
  </tr>
<js:paginator items="${resources}" page="${currentPage}" formName="frm">
  <script language="JavaScript">
	  checkboxListInit('repositoryItems', 'frm', 'selectAll', ['selectedFolders', 'selectedResources'], <%= ((java.util.Collection) pageContext.findAttribute("paginatedItems")).size() %>, 0);
  </script>
  
<c:forEach items="${paginatedItems}" var="resource" varStatus="itStatus">
<c:if test="${resource.resourceType == 'com.jaspersoft.jasperserver.api.metadata.common.domain.Folder'}">
  <tr <c:if test="${itStatus.count % 2 == 0}">class="list_alternate"</c:if>>
    <td class="paddedcell"><a href="<c:url value="flow.html"><c:param name="_flowId" value="repoAdminFlow"/><c:param name="folder" value="${resource.URIString}"/></c:url>">${resource.name}</a></td>
    <td class="paddedcell">${resource.label}</td>
    <td class="paddedcell"><spring:message code="label.folder"/></td>
    <td class="paddedcell" nowrap><js:formatDate value="${resource.creationDate}"/></td>
    <c:choose>
  		<c:when test="${not empty editableResources[resource.URIString]}">
    		   <td class="paddedcell" align="center"><a href="javascript:document.frm.resourceType.value='folder';document.frm.resource.value='${resource.URIString}';document.frm.edit.click();" value="Edit"><spring:message code="jsp.repoAdmin.defaultView.capitalEdit"/></a></td>
  		</c:when>
  		<c:otherwise>
  			<td class="paddedcell" align="center"><span disabled><spring:message code="jsp.repoAdmin.defaultView.capitalEdit"/></span></td>
  		</c:otherwise>
	</c:choose>
    <td class="paddedcell" align="center"><a href="javascript:document.frm.resource.value='${resource.URIString}';document.frm.assign.click();"><spring:message code="jsp.repoAdmin.defaultView.assign"/></a></td>
    <c:choose>
  		<c:when test="${not empty removableResources[resource.URIString]}">
    		   <td class="paddedcell" align="center"><input type="checkbox" name="selectedFolders" value="${resource.URIString}" class="fnormal" onclick="checkboxListCheckboxClicked('repositoryItems', this)"/></td>
  		</c:when>
  		<c:otherwise>
  			<td class="paddedcell" align="center"><input type="checkbox" class="fnormal"  disabled/></td>
  		</c:otherwise>
	</c:choose>
  </tr>
</c:if>
<c:if test="${resource.resourceType != 'com.jaspersoft.jasperserver.api.metadata.common.domain.Folder'}">
  <tr <c:if test="${itStatus.count % 2 == 0}">class="list_alternate"</c:if>>
    <td class="paddedcell">
		<c:choose>
		<c:when test="${resource.resourceType == 'com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit'}">
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<a href="javascript:document.frm.resource.value='${resource.URIString}';document.frm.viewReport.click();">
							${resource.name}
						</a>
					</td>
					<td align="right" valign="middle" nowrap>
						<a href="javascript:document.frm.resource.value='${resource.URIString}';document.frm.scheduleReport.click();" title="<spring:message code="repository.browser.schedule.hint"/>">
							<img border="0" src="images/schedule.gif" alt="<spring:message code="repository.browser.schedule.hint"/>"/>
						</a>
						<a href="javascript:document.frm.resource.value='${resource.URIString}';document.frm.runReportInBackground.click();" title="<spring:message code="repository.browser.run.in.background.hint"/>">
							<img border="0" src="images/runreport.gif" alt="<spring:message code="repository.browser.run.in.background.hint"/>"/>
						</a>
					</td>
				</tr>
			</table>
		</c:when>
		<%-- olap web flow --%>
		<c:when test="${resource.resourceType == 'com.jaspersoft.jasperserver.api.metadata.olap.domain.OlapUnit'}">
			<a href="javascript:disableLink('<c:url value="/olap/viewOlap.html"><c:param name="name" value="${resource.URIString}"/><c:param name="new" value="true"/><c:param name="parentFlow" value="repoAdminFlow"/><c:param name="folderPath" value="${lastFolder}"/></c:url>');" class="disLink" onclick="this.disabled='true'">
				${resource.name}</a>
		</c:when>
		<c:when test="${resource.resourceType == 'com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource'}">
			<a href="<c:url value="/fileview/fileview${resource.URIString}"/>" target="_new">
				${resource.name}</a>
		</c:when>
		<c:otherwise>
			${resource.name}
		</c:otherwise>
		</c:choose>
	</td>
    <td class="paddedcell">${resource.label}</td>
    <td class="paddedcell"><spring:message code="resource.${resource.resourceType}.label"/></td>
    <td class="paddedcell" nowrap><js:formatDate value="${resource.creationDate}"/></td>
    <c:choose>
  		<c:when test="${not empty editableResources[resource.URIString]}">
    		<td class="paddedcell" align="center"><a href="javascript:document.frm.resourceType.value='${resource.resourceType}';document.frm.resource.value='${resource.URIString}';document.frm.edit.click();"><spring:message code="jsp.repoAdmin.defaultView.capitalEdit"/></a></td>
  		</c:when>
  		<c:otherwise>
  			<td class="paddedcell" align="center"><span disabled><spring:message code="jsp.repoAdmin.defaultView.capitalEdit"/></span></td>
  		</c:otherwise>
	</c:choose>
    <td class="paddedcell" align="center"><a href="javascript:document.frm.resource.value='${resource.URIString}';document.frm.assign.click();"><spring:message code="jsp.repoAdmin.defaultView.assign"/></a></td>
    <c:choose>
  		<c:when test="${not empty removableResources[resource.URIString]}">
    		   <td class="paddedcell" align="center"><input type="checkbox" name="selectedResources" value="${resource.URIString}" class="fnormal" onclick="checkboxListCheckboxClicked('repositoryItems', this)"/></td>
  		</c:when>
  		<c:otherwise>
  			<td class="paddedcell" align="center"><input type="checkbox" class="fnormal"  disabled/></td>
  		</c:otherwise>
	</c:choose>
  </tr>
</c:if>
</c:forEach>
  <tr>
	  <td class="paddedcell" colspan="7">
			<js:paginatorLinks/>
	  </td>
  </tr>
  <tr>
    <td colspan="7">&nbsp;</td>
  </tr>
</js:paginator>
  </table>
  <table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td class="paddedcell">
      <input type="submit" class="fnormal" name="_eventId_Add" value="<spring:message code="jsp.repoAdmin.defaultView.button.addNew"/>" onClick="document.frm.resourceType.value=document.frm.cmbResourceType.value" class="fnormal"/>
      <select name="cmbResourceType" class="fnormal">
        <c:forEach items="${requestScope.resourceTypes}" var="resourceType">
        <option value="${resourceType.value}"/>"><spring:message code="${resourceType.key}</option>
        <%--
        <option value="com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit">Report Unit</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource">Data Source</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl">Input Control</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.DataType">Data Type</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValues">List of Values</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource">JRXML</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.olap.domain.OlapUnit">OLAP View</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.olap.domain.OlapClientConnection">OLAP Client Connection</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource">OLAP Schema</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.olap.domain.MondrianXMLADefinition">Mondrian XML/A Source</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource">Image</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource">Font</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource">JAR</option>
        <option value="com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource">Resource Bundle</option>
        --%>
        </c:forEach>
      </select>
    </td>
    <td align="right"><input type="button" name="" value='<spring:message code="button.remove"/>' class="fnormal" 
    	onclick="removeRepositoryItems()"/></td>
  </tr>
</table>
</form>
    </td>
  </tr>
</table>

<script type="text/javascript"> 
<!-- // bug #8381 disable olap link after first click
function disableLink(url) { 
	document.write("<style type='text/css'>.disLink{display:none;}</style>"); 
	location.href = url; 
} 
// -->
</script> 


</body>
</html>
