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

<html>
<head>
  <js:xssNonce/>

  <title><spring:message code="jsp.chooseSource.title"/></title>
<c:if test='${dataResource.subflowMode}'>
    <meta name="pageHeading" content="<c:choose><c:when test='${dataResource.subEditMode}'><spring:message code="jsp.reportWizard.pageHeading_edit"/></c:when><c:otherwise><spring:message code="jsp.reportWizard.pageHeading_new"/></c:otherwise></c:choose>"/>
</c:if>
  <script>
    function jumpTo(pageTo){
    document.forms['fmCRContQd'].jumpToPage.value=pageTo;
    document.forms['fmCRContQd'].jumpButton.click();
    }
  </script>
</head>

<body>


<FORM name="fmCRContQd" action="" method="post">
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
<input type="hidden" name="mainFlow" id="mainFlow" value="mainFlow"/>
    </td>
</c:when>
<c:when test='${masterFlowStep == "query"}'>
    <td width="1">
<table width="100%" border="0" cellpading="0" cellspacing="0">
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('reportNaming');"><spring:message code="jsp.reportWizard.naming"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('jrxmlUpload');"><spring:message code="jsp.reportWizard.jrxml"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('resources');"><spring:message code="jsp.reportWizard.resources"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:jumpTo('dataSource');"><spring:message code="jsp.reportWizard.dataSource"/></span></td></tr>
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
    <a class="wizard_menu_current" href="javascript:document.forms['fmCRContQd']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.resources"/></a>  
  </c:when>
  <c:otherwise>
    <span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.resources"/></span>  
  </c:otherwise>
  </c:choose>
  </td></tr>
  <tr><td nowrap="true">
  <c:choose>
  <c:when test='${masterFlowStep == "dataSource"}'>
    <a class="wizard_menu_current" href="javascript:document.forms['fmCRContQd']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.dataSource"/></a>  
  </c:when>
  <c:otherwise>
    <span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.dataSource"/></span>  
  </c:otherwise>
  </c:choose>
  </td></tr>
  <tr><td nowrap="true">
  <c:choose>
  <c:when test='${masterFlowStep == "query"}'>
    <a class="wizard_menu_current" href="javascript:document.forms['fmCRContQd']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.query"/></a>  
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
                <tr>
                    <td>&nbsp;</td>
                    <td colspan="2"><span class="fsection">
                    	<c:choose>
                    		<c:when test="${dataResource.subflowMode}">
                    			<spring:message code="data.source.title.${dataResource.parentType}"/>
                    		</c:when>
                    		<c:otherwise>
                    			<spring:message code="data.source.title"/>
                    		</c:otherwise>
                    	</c:choose>
                    </span></td>
                </tr>
                <tr>
                    <td colspan="3">&nbsp;</td>
                </tr>
                <spring:bind path="dataResource.source">
                    <tr>
                        <td>&nbsp;</td>
                        <td><input name="${status.expression}" type="radio" id="CONTENT_REPOSITORY" value="CONTENT_REPOSITORY" <c:if test='${status.value=="CONTENT_REPOSITORY"}'>checked="true"</c:if>></td>
                        <td><spring:message code="label.fromRepository"/></td>
                    </tr>
                    <c:if test="${status.error}">
                    <tr>
                        <td>&nbsp;</td>
                        <td collspan="2"><span class="ferror">${status.errorMessage}</span></td>
                    </tr>
                    </c:if>
                </spring:bind>
                <spring:bind path="dataResource.selectedUri">
                    <tr>
                        <td colspan="2">&nbsp;</td>
                        <td>
                            <select name="${status.expression}" size="1" title="<spring:message code="jsp.chooseSource.fromRepository.title"/>" class="fnormal" onClick="document.forms['fmCRContQd'].CONTENT_REPOSITORY.click();">
                            <c:forEach items='${dataResource.allDatasources}' var='path'>
                                <option value="${path}" <c:if test='${path==status.value}'>selected="true"</c:if>>${path}</option>   
                            </c:forEach>
                        </td>
                    </tr>
                    <c:if test="${status.error}">
                    <tr>
                        <td colspan="2">&nbsp;</td>
                        <td><span class="ferror">${status.errorMessage}</span></td>
                    </tr>
                    </c:if>
                </spring:bind>
                <spring:bind path="dataResource.source">
	                <tr>
	                    <td colspan="3">&nbsp;</td>
	                </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td><input type="radio" name="${status.expression}" id="LOCAL" value="LOCAL" <c:if test='${status.value=="LOCAL"}'>checked="true"</c:if>></td><!--onClick="document.forms['fmCRContQd']._eventId_Next.click();"-->
                        <td><a href="javascript:document.forms['fmCRContQd'].LOCAL.click();"><spring:message code="label.locallyDefined"/></a></td>
                    </tr>
                </spring:bind>
				<%-- bug 8325: missing datasource for olap connection, i.e., none --%>
				<%-- if olap, don't display this; i.e., parentType is "olapMondrianSchema" --%>
				<c:if test="${dataResource.parentType != 'olapMondrianSchema'}">
					<spring:bind path="dataResource.source">
						<tr>
							<td colspan="3">&nbsp;</td>
						</tr>
						<tr>
							<td>&nbsp;</td>
							<td><input type="radio" name="${status.expression}" id="NONE" value="NONE" <c:if test='${status.value=="NONE"}'>checked="true"</c:if>></td><!--onClick="document.forms['fmCRContQd']._eventId_Next.click();"-->
							<td><a href="javascript:document.forms['fmCRContQd'].NONE.click();"><spring:message code="label.none"/></a></td>
						</tr>
					</spring:bind>
				</c:if>
                <tr><td colspan="3">&nbsp;</td></tr>
                <tr>
                    <td>&nbsp;</td>
                    <td colspan="2">
                    <input type="submit" class="fnormal" name="_eventId_Cancel" value="<spring:message code="button.cancel"/>">
                    <input type="submit" class="fnormal" name="_eventId_Back" value="<spring:message code="button.back"/>">&nbsp;
                    <input type="submit" class="fnormal" name="_eventId_Next" value="<spring:message code="button.next"/>">
                    <c:if test='${dataResource.parentType == "reportUnit" || masterFlowStep == "query"}'><input type="submit" class="fnormal" name="_eventId_Finish" value="<spring:message code="button.finish"/>"></c:if>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="jumpToPage">
            <input type="submit" class="fnormal" style="visibility:hidden;" value="" name="_eventId_Jump" id="jumpButton">   
            <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}">
    </td>
  </tr>
</table>
</FORM>

</body>

</html>
