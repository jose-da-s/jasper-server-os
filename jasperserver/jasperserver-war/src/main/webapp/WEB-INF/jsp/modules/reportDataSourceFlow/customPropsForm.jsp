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

  <title><spring:message code="jsp.jndiPropsForm.title"/></title>
 <script>
    function jumpTo(pageTo){
    document.forms['fmJNDIProps'].jumpToPage.value=pageTo;
    document.forms['fmJNDIProps'].jumpButton.click();
    }
 </script>
</head>
    
<body>

<FORM name="fmJNDIProps" action="" method="post">
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
    <a class="wizard_menu_current" href="javascript:document.forms['fmJNDIProps']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.resources"/></a>  
  </c:when>
  <c:otherwise>
    <span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.resources"/></span>  
  </c:otherwise>
  </c:choose>
  </td></tr>
  <tr><td nowrap="true">
  <c:choose>
  <c:when test='${masterFlowStep == "dataSource"}'>
    <a class="wizard_menu_current" href="javascript:document.forms['fmJNDIProps']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.dataSource"/></a>  
  </c:when>
  <c:otherwise>
    <span class="wizard_menu_disabled"><spring:message code="jsp.reportWizard.dataSource"/></span>  
  </c:otherwise>
  </c:choose>
  </td></tr>
  <tr><td nowrap="true">
  <c:choose>
  <c:when test='${masterFlowStep == "query"}'>
    <a class="wizard_menu_current" href="javascript:document.forms['fmJNDIProps']._eventId_Cancel.click();"><spring:message code="jsp.reportWizard.query"/></a>  
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
                    <td><span class="fsection"><spring:message code="${dataResource.customDatasourceLabel}"/></span></td>
                </tr>
                <tr><td colspan="2">&nbsp;</td></tr>
                <spring:bind path="dataResource.reportDataSource.name">
                    <tr align="center">
                        <td align="right">* <spring:message code="label.name"/>&nbsp;</td>
                        <td align="left"><input maxlength="100" name="${status.expression}" type="text" class="fnormal" size="40" <c:if test='${dataResource.aloneEditMode}'>disabled="true"</c:if> value="${status.value}"></td>
                    </tr>
                    <c:if test="${status.error}">
                    <tr>
                        <td>&nbsp;</td>
                        <td><span class="ferror">${status.errorMessage}</span></td>
                    </tr>
                    </c:if>
                </spring:bind>
                <spring:bind path="dataResource.reportDataSource.label">
                    <tr align="center">
                        <td align="right">* <spring:message code="label.label"/>&nbsp;</td>
                        <td align="left"><input maxlength="100" name="${status.expression}" type="text" class="fnormal" size="40" value="${status.value}"></td>
                    </tr>
                    <c:if test="${status.error}">
                    <tr>
                        <td>&nbsp;</td>
                        <td><span class="ferror">${status.errorMessage}</span></td>
                    </tr>
                    </c:if>
                </spring:bind>
                <spring:bind path="dataResource.reportDataSource.description">
					<tr>
						<td align="right" valign="top"><spring:message code="label.description"/>&nbsp;</td>
						<td align="left"><textarea name="${status.expression}" rows="5" cols="28" class="fnormal">${status.value}</textarea></td>
					</tr>
					<c:if test="${status.error}">
					<tr>
						<td>&nbsp;</td>
						<td><span class="ferror">${status.errorMessage}</span></td>
					</tr>
					</c:if>
                </spring:bind>
                <tr><td colspan="2">&nbsp;</td></tr>
                <c:forEach var="prop" items="${dataResource.customProperties}">
	                <spring:bind path="dataResource.reportDataSource.propertyMap[${prop.name}]">
	                    <tr>
                            <td align="right"><c:if test="${prop.mandatory != null}">* </c:if><spring:message code="${prop.label}"/>&nbsp;</td>
                        <td>
                            <c:choose>
                                <c:when test="${prop.type eq 'boolean'}">
                                    <input type="hidden" name="_${status.expression}" value="visible" />
                                    <input name="${status.expression}" type="checkbox" <c:if test="${status.value eq 'on' || status.value eq 'true'}">checked="true"</c:if> value ="true" size="30">
                                </c:when>

                                <c:otherwise>
                                <c:choose>
                                    <c:when test="${prop.displayHeight != null}">
                                            <textarea name="${status.expression}" rows="${prop.displayHeight}" cols="<c:choose><c:when test="${prop.displayWidth != null}">${prop.displayWidth}</c:when><c:otherwise>40</c:otherwise></c:choose>"
                                                      class="fnormal">${status.value}</textarea>
                                    </c:when>
                                    <c:otherwise>
                                        <input name="${status.expression}" type="<c:choose><c:when test="${prop.name eq 'password'}">password</c:when><c:otherwise>text</c:otherwise></c:choose>" class="fnormal"
                                               size="<c:choose><c:when test="${prop.displayWidth != null}">${prop.displayWidth}</c:when><c:otherwise>40</c:otherwise></c:choose>" value="${status.value}">
                                    </c:otherwise>
                                </c:choose>
                                </c:otherwise>
                            </c:choose>
	                        </td>
	                    </tr>
	                    <c:if test="${status.error}">
	                    <tr>
	                        <td>&nbsp;</td>
	                        <td><span class="ferror">${status.errorMessage}</span></td>
	                    </tr>
	                    </c:if>
	                </spring:bind>
                </c:forEach>
                <spring:bind path="dataResource">
                    <c:forEach var="err" items="${status.errors.globalErrors}">
                        <tr>
                            <td>&nbsp;</td>
                            <td width="60"><span class="ferror"><spring:message code="${err.code}" arguments="${err.arguments}" text="${err.defaultMessage}"/></span></td>
                        </tr>
                    </c:forEach>
                </spring:bind>
<c:if test="${dataResource.subflowMode}">
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
				<tr>
					<td>
						<c:choose>
							<c:when test="${dataResource.editMode || dataResource.source=='CONTENT_REPOSITORY' && dataResource.reportDataSource.parentFolder!=null}">${dataResource.reportDataSource.parentFolder}</c:when>
							<c:otherwise> 
								<spring:bind path="dataResource.reportDataSource.parentFolder">
									<select name="${status.expression}" class="fnormal" onClick="document.forms['fmJNDIProps'].CONTENT_REPOSITORY.click();">
					
					<%-- bug #9008: like 'jdbcpropsform.jsp', the following is needed to specify a resource folder --%>
					
										<c:forEach items="${dataResource.allFolders}" var="folder">
											<option name="${folder}" 
												<c:if test='${status.value==folder}'>selected="true"</c:if>>${folder}
											</option>
										</c:forEach>
						
									</select>	                        
								</spring:bind>
								<c:if test="${status.error}"><BR><span class="ferror">${status.errorMessage}</span></c:if>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
</c:if>
			  <!-- TODO test hook
                <tr>
                	<td colspan="2">&nbsp;</td>
                </tr>
				<tr>
					<td>&nbsp;</td>
					<td>
						<input type="submit" class="fnormal" name="_eventId_testDataSource" value="<spring:message code="jsp.testConnection"/>"/>
						<c:if test="${requestScope['connection.test'] != null}">
							<c:if test="${requestScope['connection.test'] == true}">
								<spring:message code="jsp.testConnection.successful"/>
							</c:if>
							<c:if test="${requestScope['connection.test'] == false}">
								<spring:message code="jsp.testConnection.failed"/>
							</c:if>
						</c:if>
					</td>
				</tr>
			  -->
				<tr><td colspan="2">&nbsp;</td></tr>
				<tr>
                    <td>&nbsp;</td>
                    <td>
                        <input type="submit" class="fnormal" name="_eventId_Cancel" value="<spring:message code="button.cancel"/>"/>&nbsp;
                        <c:if test="${!dataResource.aloneEditMode}">
                            <input type="submit" class="fnormal" name="_eventId_Back" value="<spring:message code="button.back"/>">&nbsp;
                        </c:if>
                        <c:choose>
                            <c:when test='${dataResource.subflowMode || dataResource.mode==0}'>
                                <input type="submit" class="fnormal" name="_eventId_Next" value="<spring:message code="button.next"/>">
                                <c:if test='${dataResource.parentType == "reportUnit"}'><input type="submit" class="fnormal" name="_eventId_Finish" value="<spring:message code="button.finish"/>"></c:if>
                            </c:when>
                            <c:otherwise>
                                <input type="submit" class="fnormal" name="_eventId_Save" value="<spring:message code="button.save"/>">
                            </c:otherwise>
                        </c:choose>
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
