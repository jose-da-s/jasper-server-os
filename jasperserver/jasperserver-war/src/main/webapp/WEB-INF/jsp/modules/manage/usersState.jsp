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
<%@ page contentType="text/html; charset=utf-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>
<%@ page import="com.jaspersoft.jasperserver.api.security.SecurityConfiguration" %>

<js:out javaScriptEscape="true">
<script type="text/javascript">
    // ${sessionScope.XSS_NONCE} don't remove

    if (typeof orgModule === "undefined") {
        orgModule = {};
    }

    if (typeof orgModule.messages === "undefined") {
        orgModule.messages = {};
    }

    <%--orgModule.messages[""]  = '<spring:message code="" javaScriptEscape="true"/>';--%>
    orgModule.messages['userNameIsAlreadyInUse'] = '<spring:message code="jsp.userManager.userCreator.validation.userNameIsAlreadyInUse" javaScriptEscape="true"/>';
    orgModule.messages['invalidEmail'] = '<spring:message code="jsp.userManager.userCreator.validation.invalidEmail" javaScriptEscape="true"/>';
    orgModule.messages['invalidConfirmPassword'] = '<spring:message code="jsp.userManager.userCreator.validation.invalidConfirmPassword" javaScriptEscape="true"/>';
    orgModule.messages['userNameIsEmpty'] = '<spring:message code="jsp.userManager.userCreator.validation.userNameIsEmpty" javaScriptEscape="true"/>';
    orgModule.messages['passwordIsEmpty'] = '<spring:message code="jsp.userManager.userCreator.validation.passwordIsEmpty" javaScriptEscape="true"/>';
    orgModule.messages['passwordIsWeak'] = '<spring:message code="exception.remote.weak.password" javaScriptEscape="true"/>';
    orgModule.messages['addUser'] = '<spring:message code="jsp.userManager.userCreator.add" javaScriptEscape="true"/>';
    orgModule.messages['addUserTo'] = '<spring:message code="jsp.userManager.userCreator.addTo" javaScriptEscape="true"/>';
    orgModule.messages['deleteMessage'] = '<spring:message code="jsp.userManager.deleteUserMessage" javaScriptEscape="true"/>';
    orgModule.messages['deleteAllMessage'] = '<spring:message code="jsp.userManager.deleteAllUserMessage" javaScriptEscape="true"/>';
    orgModule.messages['cancelEdit'] = '<spring:message code="jsp.userManager.cancelUserEditMessage" javaScriptEscape="true"/>';
    orgModule.messages['unsupportedSymbols'] = '<spring:message code="jsp.userManager.userCreator.notSupportedSymbols" javaScriptEscape="true"/>';
    orgModule.messages['validationErrors'] = '<spring:message code="jsp.userAndRoleManager.validation.errors" javaScriptEscape="true"/>';
    orgModule.messages['attribute.name.empty'] = '<spring:message code="jsp.userAndRoleManager.attribute.name.empty" javaScriptEscape="true"/>';
    orgModule.messages['attribute.value.empty'] = '<spring:message code="jsp.userAndRoleManager.attribute.value.empty" javaScriptEscape="true"/>';
    orgModule.messages['attribute.name.too.long'] = '<spring:message code="jsp.userAndRoleManager.attribute.name.too.long" javaScriptEscape="true"/>';
    orgModule.messages['attribute.value.too.long'] = '<spring:message code="jsp.userAndRoleManager.attribute.value.too.long" javaScriptEscape="true"/>';
    orgModule.messages['attribute.name.already.exist'] = '<spring:message code="jsp.userAndRoleManager.attribute.name.already.exist" javaScriptEscape="true"/>';
    orgModule.messages['attribute.name.invalid'] = '<spring:message code="jsp.userManager.userCreator.notSupportedSymbols" javaScriptEscape="true"/>'.replace("\#{symbols}", "'/' , '\\'");

    if (typeof localContext === "undefined") {
        localContext = {};
    }

    // Initialization of repository search init object.
    localContext.flowExecutionKey = '${flowExecutionKey}';
    localContext.userMngInitOptions = {
        state: JSON.parse('${state}'),
        defaultUser: '${defaultUser}',
        defaultEntity: '${defaultEntity}',
        currentUser: '${signedUser}',
        currentUserRoles: JSON.parse('${currentUserRoles}')
    };

    orgModule.Configuration = JSON.parse('${configuration}');

    if (typeof __jrsConfigs__.userManagement === "undefined") {
        __jrsConfigs__.userManagement = {};
    }

    __jrsConfigs__.userManagement.orgModule = orgModule;
    __jrsConfigs__.userManagement.localContext = localContext;

    var isEncryptionOn = <%= SecurityConfiguration.isEncryptionOn()%>;

</script>
</js:out>
