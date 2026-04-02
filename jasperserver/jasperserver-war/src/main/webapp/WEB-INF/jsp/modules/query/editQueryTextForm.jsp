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

  <title><spring:message code="jsp.editQueryForm.title"/></title>
  <meta name="pageHeading" content="Edit Query"/>
<script>
function jumpTo(pageTo){
    document.forms['fmDataType'].jumpToPage.value=pageTo;
    document.forms['fmDataType'].jumpButton.click();
}
</script>
</head>

<body>

<form name="fmDataType" method="post" action="">
<table width="100%" border="0" cellpadding="20" cellspacing="0">
  <tr valign="top">
<c:if test='${masterFlow == "reportUnit"}'>
<c:choose>
<c:when test='${parentFlow == "reportUnit"}'>
    <td width="1">
<table width="100%" border="0" cellpading="0" cellspacing="0">
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('reportNaming');"><spring:message code="jsp.reportWizard.naming"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('jrxmlUpload');"><spring:message code="jsp.reportWizard.jrxml"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('resources');"><spring:message code="jsp.reportWizard.resources"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('dataSource');"><spring:message code="jsp.reportWizard.dataSource"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu_current" href="javascript:jumpTo('query');"><spring:message code="jsp.reportWizard.query"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('customization');"><spring:message code="jsp.reportWizard.customization"/></a></td></tr>
</table>
    </td>
</c:when>
<c:otherwise>
    <td width="1">
<table width="100%" border="0" cellpading="0" cellspacing="0">
  <tr><td nowrap="true"><span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.naming"/></span></td></tr>
  <tr><td nowrap="true"><span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.jrxml"/></span></td></tr>
  <tr><td nowrap="true">
  <c:choose>
  <c:when test='${masterFlowStep == "resources"}'>
    <a class="wizard_menu_current" href="javascript:document.forms['fmDataType']._eventId_cancel.click();"><spring:message code="jsp.reportWizard.resources"/></a>  
  </c:when>
  <c:otherwise>
    <span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.resources"/></span>  
  </c:otherwise>
  </c:choose>
  </td></tr>
  <tr><td nowrap="true"><span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.dataSource"/></span></td></tr>
  <tr><td nowrap="true">
  <c:choose>
  <c:when test='${masterFlowStep == "query"}'>
    <a class="wizard_menu_current" href="javascript:document.forms['fmDataType']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.query"/></a>  
  </c:when>
  <c:otherwise>
    <span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.query"/></span>  
  </c:otherwise>
  </c:choose>
  </td></tr>
  <tr><td nowrap="true"><span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.customization"/></span></td></tr>
</table>
    </td>
</c:otherwise>
</c:choose>
</c:if>
    <td>
<table border="0" cellpadding="1" cellspacing="0" align="center">
<input type="hidden" name="jumpToPage">
<input type="submit" class="fnormal" style="visibility:hidden;" value="" name="_eventId_Jump" id="jumpButton">   
<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
  <tr>
    <td>&nbsp;</td>
    <td><span class="fsection"><spring:message code="jsp.editQueryForm.pageHeading"/></span></td>
  </tr>
  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>


<spring:bind path="query.query.language">
  <tr>
    <td align="right"><spring:message code="jsp.editQueryTextForm.queryLanguage"/>&nbsp;</td>
    <td>
      <select name="${status.expression}" class="fnormal">
  <c:forEach items="${requestScope.queryLanguages}" var="language">
        <option value="${language}" <c:if test="${status.value == language}">selected</c:if>><spring:message code="query.language.${language}.label"/></option>
  </c:forEach>
      </select>
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


<spring:bind path="query.query.sql">
  <tr>
    <td align="right" valign="top"><spring:message code="jsp.editQueryTextForm.queryString"/>&nbsp;</td>
    <td><textarea name="${status.expression}" rows="5" cols="28" class="fnormal">${status.value}</textarea></td>
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
      <c:if test="${query.subflowMode == true}">
       <input type="submit" name="_eventId_back" value="<spring:message code="button.cancel" />" class="fnormal" />
	  </c:if>
      <c:if test="${query.subflowMode != true}">
       <input type="button" name="_eventId_cancel" value="<spring:message code="button.cancel" />" class="fnormal" OnClick='javascript:gotoDefaultLocation()'/>
	  </c:if>	  
	  <input type="submit" name="_eventId_back" value="<spring:message code="button.back"/>" class="fnormal"/>
		<c:if test="${query.standAloneMode == true}">
	  		<input type="submit" name="_eventId_save" value="<spring:message code="button.save"/>" class="fnormal"/>&nbsp;
		</c:if>
		<c:if test="${query.subflowMode == true}">
	  		<input type="submit" name="_eventId_save" value="<spring:message code="button.next"/>" class="fnormal"/>&nbsp;
	  		<c:if test='${parentFlow == "reportUnit"}'><input type="submit" name="_eventId_finish" value="<spring:message code="button.finish"/>" class="fnormal"/>&nbsp;</c:if>
		</c:if>
	</td>
  </tr>
</table>
    </td>
  </tr>
</table>
</form>

</body>

</html>
