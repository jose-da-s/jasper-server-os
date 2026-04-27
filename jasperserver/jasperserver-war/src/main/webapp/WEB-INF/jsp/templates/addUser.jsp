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

<%--
Overview:
    Usage:permit user to add a system created object to the repository.

Usage:

    <t:insertTemplate template="/WEB-INF/jsp/templates/addUser.jsp">
    </t:insertTemplate>

--%>

<%@ page import="com.jaspersoft.jasperserver.api.JSException" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="bodyContent" name="bodyContent" classname="java.lang.String" ignore="true"/>


<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <t:putAttribute name="containerClass">panel dialog overlay addUser moveable centered_horz centered_vert ${containerClass}</t:putAttribute>
    <t:putAttribute name="containerID" value="addUser" />
    <t:putAttribute name="containerTitle">
        <spring:message code="jsp.userManager.userCreator.title" javaScriptEscape="true"/>
    </t:putAttribute>
    <t:putAttribute name="headerClass" value="mover"/>
    <t:putAttribute name="bodyContent">
            <fieldset class="group first">
                <legend class="offLeft"><span><spring:message code="DIALOG_PROPERTIES_RESOURCE_COLUMN_PRIMARY_LEGEND" javaScriptEscape="true"/></span></legend>
                <label class="control input text" for="addUserFullName" title="<spring:message code="jsp.userManager.userCreator.fullName.title" javaScriptEscape="true"/>">
                    <span class="wrap"><spring:message code="jsp.userManager.userCreator.fullName" javaScriptEscape="true"/>:</span>
                    <input id="addUserFullName" type="text" maxlength="100" value=""/>
                    <span class="message warning">error message here</span>
                </label>
                <label class="control input text" for="addUserID" title="<spring:message code="jsp.userManager.userCreator.userId.title" javaScriptEscape="true"/>">
                    <span class="wrap"><spring:message code="jsp.userManager.userCreator.userId" javaScriptEscape="true"/> (<spring:message code='required.field'/>):</span>
                    <input id="addUserID" type="text" maxlength="100" value=""/>
                    <span class="hint"><spring:message code="jsp.userManager.userCreator.userId.hint" javaScriptEscape="true"/></span>
                    <span class="message warning">error message here</span>
                </label>
                <label class="control input text" class="required" for="addUserEmail" title="<spring:message code="jsp.userManager.userCreator.emailAddress.title" javaScriptEscape="true"/>">
                    <span class="wrap"><spring:message code="jsp.userManager.userCreator.emailAddress" javaScriptEscape="true"/>:</span>
                    <input id="addUserEmail" type="text" maxlength="100" value=""/>
                    <span class="message warning">error message here</span>
                </label>
			</fieldset>
			<fieldset id="passwords" class="group">
				<legend class="offLeft"><span><spring:message code="jsp.userManager.userCreator.password.legend" javaScriptEscape="true"/></span></legend>
				<label class="control input password" class="required" for="addUserPassword" title="<spring:message code="jsp.userManager.userCreator.password.title" javaScriptEscape="true"/>">
				    <span class="wrap"><spring:message code="jsp.userManager.userCreator.password" javaScriptEscape="true"/> (<spring:message code='required.field'/>):</span>
                    <%-- Input password max length dependent on the length of encoded password. Currently it is 47 characters. --%>
				    <input class="" id="addUserPassword" type="password" maxlength="47" value=""/>
				    <span class="message warning">error message here</span>
				    <span class="message hint"></span>
				</label>
				<label class="control input password" class="required" for="addUserConfirmPassword" title="<spring:message code="jsp.userManager.userCreator.password.title" javaScriptEscape="true"/>">
				    <span class="wrap"><spring:message code="jsp.userManager.userCreator.confirmPassword" javaScriptEscape="true"/> (<spring:message code='required.field'/>):</span>
                    <%-- Input password max length dependent on the length of encoded password. Currently it is 47 characters. --%>
				    <input class="" id="addUserConfirmPassword" maxlength="47" type="password" value="" />
				    <span class="message warning">error message here</span>
				    <span class="message hint"></span>
				</label>
            </fieldset>
            <fieldset class="group">
                <div class="control checkBox">
                    <label class="wrap" for="addUserEnableUser" title="<spring:message code="jsp.userManager.userCreator.enableThisUser.title" javaScriptEscape="true"/>">
                        <spring:message code="jsp.userManager.userCreator.enableThisUser" javaScriptEscape="true"/>
                    </label>
                    <input id="addUserEnableUser" type="checkbox" checked="checked"/>
                </div>
			</fieldset>
    </t:putAttribute>
    <t:putAttribute name="footerContent">
             <!--NOTE: label of #addUserBtn is dynamic with [Organization Name] token replaced with name of org to which user will be added -->
            <button id="addUserBtn" class="button action primary up">
                <span class="wrap"><spring:message code="jsp.userManager.userCreator.add" javaScriptEscape="true"/></span><span class="icon"></span>
            </button>
            <button id="cancelUserBtn" class="button action up">
                <span class="wrap"><spring:message code="dialog.file.cancel" javaScriptEscape="true"/></span><span class="icon"></span>
            </button>
    </t:putAttribute>
</t:insertTemplate>
