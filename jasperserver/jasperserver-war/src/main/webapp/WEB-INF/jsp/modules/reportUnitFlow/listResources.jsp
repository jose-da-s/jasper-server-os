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

  <title><spring:message code="jsp.listResources.title"/></title>
  <meta name="pageHeading" content="<c:choose><c:when test='${wrapper.aloneEditMode}'><spring:message code='jsp.pageHeading.editReportWizard'/></c:when><c:otherwise><spring:message code='jsp.pageHeading.createReportWizard'/></c:otherwise></c:choose>"/>
  <script>
            function jumpTo(pageTo){
//FIXME not used
            document.forms['fmCRExtRsrCh'].jumpToPage.value=pageTo;
            document.forms['fmCRExtRsrCh'].jumpButton.click();
            }
  </script>
  <script>
        	function editResource(resName){
				document.forms['fmCRExtRsrCh'].resourceName.value=resName;
				document.forms['fmCRExtRsrCh'].editResourceButton.click();
        	}
			function editControl(resName){
				document.forms['fmCRExtRsrCh'].resourceName.value=resName;
				document.forms['fmCRExtRsrCh'].editControlButton.click();
        	}        	
  </script>
</head>

<body>

<FORM name="fmCRExtRsrCh" action="" method="post">
<table width="100%" border="0" cellpadding="20" cellspacing="0">
  <tr valign="top">
    <td width="1">
<table width="100%" border="0" cellpading="0" cellspacing="0">
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:document.forms['fmCRExtRsrCh'].reportNaming.click();"><spring:message code="jsp.reportWizard.naming"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:document.forms['fmCRExtRsrCh'].jrxmlUpload.click();"><spring:message code="jsp.reportWizard.jrxml"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu_current" href="javascript:document.forms['fmCRExtRsrCh']._eventId_resources.click();"><spring:message code="jsp.reportWizard.resources"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:document.forms['fmCRExtRsrCh'].dataSource.click();"><spring:message code="jsp.reportWizard.dataSource"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:document.forms['fmCRExtRsrCh'].query.click();"><spring:message code="jsp.reportWizard.query"/></a></td></tr>
  <tr><td nowrap="true"><a class="wizard_menu" href="javascript:document.forms['fmCRExtRsrCh'].customization.click();"><spring:message code="jsp.reportWizard.customization"/></a></td></tr>
</table>
<input type="submit" class="fnormal" name="_eventId_reportNaming" id="reportNaming" value="reportNaming" style="visibility:hidden;"/>
<input type="submit" class="fnormal" name="_eventId_jrxmlUpload" id="jrxmlUpload" value="jrxmlUpload" style="visibility:hidden;"/>
<input type="submit" class="fnormal" name="_eventId_dataSource" id="dataSource" value="dataSource" style="visibility:hidden;"/>
<input type="submit" class="fnormal" name="_eventId_query" id="query" value="query" style="visibility:hidden;"/>
<input type="submit" class="fnormal" name="_eventId_customization" id="customization" value="customization" style="visibility:hidden;"/>
<input type="submit" class="fnormal" name="_eventId_resources" id="resources" value="resources" style="visibility:hidden;"/>
    </td>
    <td>
                <table border="0" cellpading="0" cellspacing="0" align="center">
                    <tr>
                        <td><span class="fsection"><spring:message code="jsp.reportUnitFlow.listResources.header"/></span></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td valign="top">
                            <table width="100%" cellpadding="0" cellspacing="0">
                                <tr bgcolor="#c2c4b6" class="fheader">
                                    <td><strong><spring:message code="jsp.listResources.resourceName"/></strong></td>
                                    <td><strong><spring:message code="jsp.listResources.resourceType"/></strong></td>
                                    <td><spring:message code="jsp.listResources.stateAction"/></td>
                                </tr>
                                <tr>
                                    <td colspan="3">&nbsp;</td>
                                </tr>
                                <%-- Create an instance of FileResourceWrapper, for getting the supported file type names--%>
								<jsp:useBean id="fileResourceWrapper" class="com.jaspersoft.jasperserver.war.dto.FileResourceWrapper" />
								
								<%-- Create an instance of InputControlWrapper, for getting the supported control type names--%>
								<jsp:useBean id="inputControlWrapper" class="com.jaspersoft.jasperserver.war.dto.InputControlWrapper" />
                                
                                <%-- Show added resources present in the ReportUnit--%>
                                <c:forEach items="${wrapper.reportUnit.resources}" var="res">
                                	<%-- Show only those resources which arent marked suggested --%>
                                	<c:set var="suggested" value="false"/>
                                	<c:forEach items="${wrapper.suggestedResources}" var="resWrap">
                                		<c:if test="${resWrap.fileResource.name==res.localResource.name}">
                                			<c:set var="suggested" value="true"/>
                                		</c:if>
                                	</c:forEach>
                                	<c:if test="${!suggested}">
	                                    <tr>
	                                        <td><a href="javascript:editResource('${res.localResource.name}')">${res.localResource.name}</a></td>
	                                        <td><c:choose><c:when test='${res.localResource.fileType!=null}'>${allTypes[res.localResource.fileType]}</c:when><c:otherwise><spring:message code="jsp.listResources.resource"/></c:otherwise></c:choose></td>
	                                        <td>
	                                        	<input type="image" name="_eventId_RemoveResource" title='<spring:message code="jsp.listResources.button.removeResource"/>' alt='<spring:message code="button.remove"/>'  src="images/delete1.gif" onclick="document.fmCRExtRsrCh.resourceName.value='${res.localResource.name}';">
	                                        </td>
	                                    </tr>
                                    </c:if>
                                </c:forEach>
								
								<%-- Show suggested resources, added or not --%>								                                                               
                               <c:if test="${wrapper.hasSuggestedResources}">
		                           <tr>
	                                    <td colspan="3" align="center"><strong><spring:message code="jsp.listResources.suggestedResources"/></strong></td>
	                               </tr>
                               </c:if>
                                <c:forEach items="${wrapper.suggestedResources}" var="resWrap">
                                	
	                                    <tr>
	                                        <td><a href="javascript:editResource('${resWrap.fileResource.name}')">${resWrap.fileResource.name}</a></td>
	                                        <td><c:choose><c:when test='${resWrap.fileResource.fileType!=null}'>${allTypes[resWrap.fileResource.fileType]}</c:when><c:otherwise><spring:message code="jsp.listResources.resource"/></c:otherwise></c:choose></td>
	                                        <td>
	                                        	<c:choose><c:when test="${resWrap.located}"><spring:message code="jsp.listResources.added"/></c:when>
	                                        	<c:otherwise><a href="javascript:editResource('${resWrap.fileResource.name}')"><spring:message code="jsp.listResources.addNow"/></a></c:otherwise></c:choose>
	                                        </td>
	                                    </tr>
                                    
                                </c:forEach>
                                
                                <%-- Show added controls --%>                       
                                <c:if test="${wrapper.hasNonSuggestedControls}">
		                           <tr>
	                                    <td colspan="3" align="center"><strong><spring:message code="jsp.listResources.addedControls"/></strong></td>
	                               </tr>
                               </c:if> 
                               <c:forEach items="${wrapper.reportUnit.inputControls}" var="control">
                                	<%-- Show only those controls which arent marked suggested --%>
                                	<c:set var="suggested" value="false"/>
                                	<c:forEach items="${wrapper.suggestedControls}" var="contWrap">
                                		<c:if test="${contWrap.inputControl.name==control.localResource.name}">
                                			<c:set var="suggested" value="true"/>
                                		</c:if>
                                	</c:forEach>
                                	<c:if test="${!suggested}">
										<tr>
											<c:if test="${control.local}">
												<td><strong><a href="javascript:editControl('${control.localResource.name}')">${control.localResource.name}</a></strong></td>
											</c:if>
								   			<c:if test="${!control.local}">
												   <td><strong><a href="javascript:editControl('${control.referenceURI}')">${control.referenceURI}</a></strong></td>
											</c:if>
											<td>${inputControlWrapper.supportedControlTypes[control.localResource.type]} <spring:message code="jsp.listResources.inputControl"/></td>
	                                        <td>
												<c:if test="${control.local}">
													<input type="image" name="_eventId_RemoveControl" title='<spring:message code="jsp.listResources.button.removeControl"/>' alt='<spring:message code="button.remove"/>' src="images/delete1.gif" onclick="document.fmCRExtRsrCh.resourceName.value='${control.localResource.name}';">
								   				</c:if>
												<c:if test="${!control.local}">
													<input type="image" name="_eventId_RemoveControl" title='<spring:message code="jsp.listResources.button.removeControl"/>' alt='<spring:message code="button.remove"/>' src="images/delete1.gif" onclick="document.fmCRExtRsrCh.resourceName.value='${control.referenceURI}';">
												</c:if>
											</td>
	                                    </tr>
                                    </c:if>
                                </c:forEach>
                               
                               <%-- Show suggested controls --%>
								<c:if test="${wrapper.hasSuggestedControls}">
		                           <tr>
	                                    <td colspan="3" align="left"><strong><spring:message code="jsp.listResources.suggestedControls"/></strong></td>
	                               </tr>
								</c:if>
								<c:forEach items="${wrapper.suggestedControls}" var="contWrap">
                               		    <tr>
	                                        <td><strong><a href="javascript:editControl('${contWrap.inputControl.name}')">${contWrap.inputControl.name}</a></strong></td>
	                                        <td><strong>${contWrap.supportedControlTypes[contWrap.inputControl.inputControlType]} <spring:message code="jsp.listResources.inputControl"/></strong></td>
	                                        <td>
	                                            <c:choose><c:when test="${contWrap.located}"><input type="image" name="_eventId_RemoveControl" title='<spring:message code="jsp.listResources.button.removeControl"/>' alt='<spring:message code="button.remove"/>'  src="images/delete1.gif" onclick="document.fmCRExtRsrCh.resourceName.value='${contWrap.inputControl.name}';"></c:when>
	                                            <c:otherwise><spring:message code="jsp.listResources.notAdded"/></c:otherwise></c:choose>
	                                        </td>
	                                    </tr>
                                </c:forEach>

                            </table>
                            <c:if test="${wrapper.hasNonSuggestedControls || wrapper.hasSuggestedControls}">
                            <table width="100%" cellpadding="0" cellspacing="5">
                                <tr>
                                    <td colspan="4">&nbsp;</td>
                                </tr>
                                <tr>
                                    <spring:bind path="wrapper.reportUnit.controlsLayout">
                                    <td width="15%" nowrap="nowrap" align="right">
                                        <spring:message code="jsp.listResources.controlsLayout"/>:
                                    </td>
                                    <td>
                                        <select name="${status.expression}" class="fnormal">
                                            <option value="1" <c:if test="${status.value==1}">selected</c:if>><spring:message code="jsp.listResources.popupScreen"/></option>
                                            <option value="2" <c:if test="${status.value==2}">selected</c:if>><spring:message code="jsp.listResources.separatePage"/></option>
                                            <option value="3" <c:if test="${status.value==3}">selected</c:if>><spring:message code="jsp.listResources.topOfPage"/></option>
                                        </select>
                                    </td>
                                    </spring:bind>
                                    <spring:bind path="wrapper.reportUnit.alwaysPromptControls">
                                    <td align="right">
                                        <input name="_${status.expression}" type="hidden"/>
                                        <input type="checkbox" name="${status.expression}"  class="fnormal" <c:if test="${status.value}">checked</c:if>/>
                                    </td>
                                    <td>
                                        <spring:message code="jsp.listResources.alwaysPrompt"/>
                                    </td>
                                    </spring:bind>
                                </tr>
                                <spring:bind path="wrapper.reportUnit.inputControlRenderingView">
                                <tr>
                                    <td width="15%" nowrap="nowrap" align="right">
                                        <spring:message code="jsp.listResources.jspLocation"/>:
                                    </td>
                                    <td colspan="3">
                                    	<input name="${status.expression}" type="hidden" value="${status.value}"/>
                                        <input style="width:100%"
                                       	<c:choose>
                                       		<c:when test="${empty status.value}">
                                       			class="ftooltip" 
                                       			value="<spring:message code="inline.hind.jsp.location"/>"
                                       		</c:when>
                                       		<c:otherwise>
                                       			class="fnormal" 
		                                        value="${status.value}"
                                       		</c:otherwise>
                                       	</c:choose>
                                       		onfocus="this.value = document.fmCRExtRsrCh['${status.expression}'].value;this.className = 'fnormal'"
                                       		onblur="document.fmCRExtRsrCh['${status.expression}'].value = this.value;if(this.value == ''){this.value='<spring:message code="inline.hind.jsp.location" javaScriptEscape="true"/>';this.className='ftooltip'}"
                                        />
                                    </td>
                                </tr>
                                	<c:if test="${status.error}">
                                <tr>
                                	<td></td>
                                	<td colspan="3"><span class="ferror">${status.errorMessage}</span></td>
                                </tr>
                                	</c:if>
                                </spring:bind>

                            </table>
                    </c:if>

                        </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td>
                        	<spring:bind path="wrapper.validationMessage">
                        		<input type="hidden" name="${status.expression}">
                        		<c:if test="${status.error}"><span class="ferror">${status.errorMessage}</span></c:if>
                        	</spring:bind>
                        </td>
                   </tr>
                    <tr>
                        <td>
                            <input type="button" class="fnormal" name="_eventId_Cancel" value='<spring:message code="button.cancel"/>' OnClick='javascript:gotoDefaultLocation()'/>&nbsp;
                            <input type="submit" class="fnormal" name="_eventId_Back" value='<spring:message code="button.back"/>'>&nbsp;
                            <input type="submit" name="_eventId_AddResource" class="fnormal"  value='<spring:message code="jsp.listResources.button.addResource"/>'>&nbsp;
                            <input type="submit" name="_eventId_AddControl" class="fnormal"  value='<spring:message code="jsp.listResources.button.addControl"/>'>
                            <input type="submit" class="fnormal" name="_eventId_Next" value='<spring:message code="button.next"/>'>
                            <input type="submit" class="fnormal" name="_eventId_Finish" value='<spring:message code="button.finish"/>'>
                        </td>
                    </tr>
                </table>
            <input type="hidden" name="resourceName">
            <input type="hidden" name="resourceType">
            <input type="hidden" name="jumpToPage">
            <input type="submit" style="visibility:hidden;" class="fnormal" value="EditControl" name="_eventId_EditControl" id="editControlButton">
            <input type="submit" style="visibility:hidden;" class="fnormal" value="EditResource" name="_eventId_EditResource" id="editResourceButton">
            <input type="submit" style="visibility:hidden;" class="fnormal" value="Jump" name="_eventId_Jump" id="jumpButton">
            <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
    </td>
  </tr>
</table>
</FORM>

</body>

</html>
