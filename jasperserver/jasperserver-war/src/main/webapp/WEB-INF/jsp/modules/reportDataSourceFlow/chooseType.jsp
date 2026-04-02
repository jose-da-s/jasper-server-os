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

<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <js:xssNonce/>

<c:if test='${dataResource.subflowMode}'>
    <meta name="pageHeading" content="<c:choose><c:when test='${dataResource.subEditMode}'><spring:message code="jsp.reportWizard.pageHeading_edit"/></c:when><c:otherwise><spring:message code="jsp.reportWizard.pageHeading_new"/></c:otherwise></c:choose>"/>
</c:if>
  <title><spring:message code="jsp.chooseType.title"/></title>
  <script>
    function jumpTo(pageTo){
    document.forms['fmCRChTyp'].jumpToPage.value=pageTo;
    document.forms['fmCRChTyp'].jumpButton.click();
    }
  </script>
</head>

<body>

<FORM name="fmCRChTyp" action="" method="post">
<table width="100%" border="0" cellpadding="20" cellspacing="0">
  <tr valign="top">
<c:if test='${masterFlow == "reportUnit"}'>
<c:choose>
<c:when test='${dataResource.parentType == "reportUnit"}'>
    <td width="1">
<table width="100%" border="0" cellpading="0" cellspacing="0">
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('reportNaming');"><spring:message code="jsp.reportWizard.naming"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('jrxmlUpload');"><spring:message code="jsp.reportWizard.jrxml"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('resources');"><spring:message code="jsp.reportWizard.resources"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu_current" href="javascript:jumpTo('dataSource');"><spring:message code="jsp.reportWizard.dataSource"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('query');"><spring:message code="jsp.reportWizard.query"/></a></td></tr>
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
    <a class="wizard_menu_current" href="javascript:document.forms['fmCRChTyp']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.resources"/></a>  
  </c:when>
  <c:otherwise>
    <span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.resources"/></span>  
  </c:otherwise>
  </c:choose>
  </td></tr>
  <tr><td nowrap="true">
  <c:choose>
  <c:when test='${masterFlowStep == "dataSource"}'>
    <a class="wizard_menu_current" href="javascript:document.forms['fmCRChTyp']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.dataSource"/></a>  
  </c:when>
  <c:otherwise>
    <span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.dataSource"/></span>  
  </c:otherwise>
  </c:choose>
  </td></tr>
  <tr><td nowrap="true">
  <c:choose>
  <c:when test='${masterFlowStep == "query"}'>
    <a class="wizard_menu_current" href="javascript:document.forms['fmCRChTyp']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.query"/></a>  
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
            <table cellpading="1" cellspacing="0" border="0" align="center">
                <tr>
                    <td>&nbsp;</td>
                    <td><span class="fsection"><c:if test="${dataResource.subflowMode && dataResource.parentType != 'olapMondrianSchema'}"><spring:message code="jsp.reportWizard"/> - </c:if><spring:message code="jsp.chooseType.title"/></span></td>
                </tr>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td><spring:message code="jsp.chooseType.selectType"/></td>
                </tr>
                <spring:bind path="dataResource.type">
                	<c:forEach items="${allTypes}" var="type" varStatus="counter">
		                <tr>
		                    <td colspan="2">&nbsp;</td>
		                </tr>
                	    <tr>
                          <td>&nbsp;</td>
	                      <td><!--onClick="document.forms['fmCRChTyp']._eventId_Next.click();"--><input type="radio" id="radio${counter.index}" name="${status.expression}" <c:if test='${status.value==type.key}'>checked="true"</c:if>
	                        value="${type.key}" />&nbsp;${type.value}</td>
	                    </tr>
                	</c:forEach>
                    <c:if test="${status.error}">
                    <tr>
                        <td>&nbsp;</td>
                        <td><span class="ferror">${status.errorMessage}</span></td>
                    </tr>
                    </c:if>
                </spring:bind>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                  <td>&nbsp;</td>
                  <td>
                        <c:if test='${dataResource.subflowMode}'>
                         <input type="submit" class="fnormal" name="_eventId_Back" value="<spring:message code="button.cancel"/>" OnClick=''>&nbsp;
                        </c:if>
                        <c:if test='${!dataResource.subflowMode}'>
                         <input type="button" class="fnormal" name="_eventId_Cancel" value="<spring:message code="button.cancel"/>" OnClick='javascript:gotoDefaultLocation()'>&nbsp;
                        </c:if>
                        <c:if test='${dataResource.mode!=1 && dataResource.mode!=2}'>
                            <input type="submit" class="fnormal" name="_eventId_Back" value="<spring:message code="button.back"/>">&nbsp;
                        </c:if>
                        <input type="submit" class="fnormal" name="_eventId_Next" value="<spring:message code="button.next"/>">
                        <c:if test='${dataResource.parentType == "reportUnit"}'><input type="submit" class="fnormal" name="_eventId_Finish" value="<spring:message code="button.finish"/>"></c:if>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="jumpToPage">
            <input type="submit" class="fnormal" style="visibility:hidden;" value="" name="_eventId_Jump" id="jumpButton">
            <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
    </td>
  </tr>
</table>
</FORM>

</body>

</html>
