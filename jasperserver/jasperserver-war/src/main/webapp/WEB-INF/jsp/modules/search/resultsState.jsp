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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<js:xssNonce/>
<js:out javaScriptEscape="true">
<script type="text/javascript">

    __jrsConfigs__.repositorySearch = {};

    __jrsConfigs__.repositorySearch["i18n"] = (function(messages){

        messages["action.create.folder"]  = '<spring:message code="SEARCH_CREATE_FOLDER" javaScriptEscape="true"/>';
        messages["action.create.folder.name"]  = '<spring:message code="RM_NEW_FOLDER" javaScriptEscape="true"/>';
        messages["SEARCH_SORT_BY"] = '<spring:message code="SEARCH_SORT_BY" javaScriptEscape="true"/>';
        messages["RM_BUTTON_DELETE_RESOURCE"] = '<spring:message code="RM_BUTTON_DELETE_RESOURCE" javaScriptEscape="true"/>';
        messages["RM_BUTTON_CANCEL"] = '<spring:message code="RM_BUTTON_CANCEL" javaScriptEscape="true"/>';
        messages["SEARCH_DELETE_CONFIRM_MSG"] = '<spring:message code="SEARCH_DELETE_CONFIRM_MSG" javaScriptEscape="true"/>';
        messages["SEARCH_BULK_DELETE_CONFIRM_MSG"] = '<spring:message code="SEARCH_BULK_DELETE_CONFIRM_MSG" javaScriptEscape="true"/>';
        messages["SEARCH_DELETE_FOLDER_CONFIRM_MSG"] = '<spring:message code="SEARCH_DELETE_FOLDER_CONFIRM_MSG" javaScriptEscape="true"/>';
        messages["SEARCH_OVERWRITE_THEME_CONFIRM_MSG"] = '<spring:message code="SEARCH_OVERWRITE_THEME_CONFIRM_MSG" javaScriptEscape="true"/>';
        messages['permission.modify'] = '<spring:message code="RM_PERMISSION_MODIFY" javaScriptEscape="true"/>';
        messages['permission.readOnly'] = '<spring:message code="RM_PERMISSION_READ_ONLY" javaScriptEscape="true"/>';
        messages['permission.delete'] = '<spring:message code="RM_BUTTON_DELETE_RESOURCE" javaScriptEscape="true"/>';
        messages['permission.administer'] = '<spring:message code="RM_PERMISSION_ADMINISTRATE" javaScriptEscape="true"/>';
        messages['RE_INVALID_DESC_SIZE'] = '<spring:message code="RE_INVALID_DESC_SIZE" javaScriptEscape="true"/>';
        messages['RE_INVALID_NAME_SIZE'] = '<spring:message code="RE_INVALID_NAME_SIZE" javaScriptEscape="true"/>';
        messages['RE_INVALID_FILE_TYPE'] = '<spring:message code="RE_INVALID_FILE_TYPE" javaScriptEscape="true"/>';
        messages['RE_ENTER_FILE_NAME'] = '<spring:message code="RE_ENTER_FILE_NAME" javaScriptEscape="true"/>';
        messages["RM_NEW_THEME_NAME"]  = '<spring:message code="RM_NEW_THEME_NAME" javaScriptEscape="true"/>';
        messages["RM_UPLOAD_THEME_ERROR"]  = '<spring:message code="RM_UPLOAD_THEME_ERROR" javaScriptEscape="true"/>';
        messages["RM_REPORT_CREATED"]  = '<spring:message code="RM_REPORT_CREATED" javaScriptEscape="true"/>';

        messages['RM_CANCEL_EDIT_MESSAGE'] = '<spring:message code="RM_CANCEL_EDIT_MESSAGE" javaScriptEscape="true"/>';
        messages['dialog.generateResource.defaultNameSuffix'] = '<spring:message code="dialog.generateResource.defaultNameSuffix" javaScriptEscape="true"/>';
        messages['dialog.generateResource.own.datasource.overwrite'] = '<spring:message code="dialog.generateResource.own.datasource.overwrite" javaScriptEscape="true"/>';
        messages['jasper.report.view.save.missing.name'] = '<spring:message code="jasper.report.view.save.missing.name" javaScriptEscape="true"/>';
        messages['jasper.report.view.save.missing.folder'] = '<spring:message code="jasper.report.view.save.missing.folder" javaScriptEscape="true"/>';

        messages['dialog.dependencies.resources.message'] = '<spring:message code="dialog.dependencies.resources.message" javaScriptEscape="true"/>';
        messages['dialog.dependencies.resources.deleteMessage'] = '<spring:message code="dialog.dependencies.resources.deleteMessage" javaScriptEscape="true"/>';

        messages['dialog.resources.exist.title'] = '<spring:message code="dialog.resources.exist.title" javaScriptEscape="true"/>';
        messages['dialog.resources.exist'] = '<spring:message code="dialog.resources.exist" javaScriptEscape="true"/>';
        messages['dialog.resources.exist.confirm.skip'] = '<spring:message code="dialog.resources.exist.confirm.skip" javaScriptEscape="true"/>';

        messages['dialog.resources.all.exist'] = '<spring:message code="dialog.resources.all.exist" javaScriptEscape="true"/>';
        messages['dialog.resources.one.exists'] = '<spring:message code="dialog.resources.one.exists" javaScriptEscape="true"/>';

        messages['ADH_1001_ADHOC_SAVE_REPORT_DESCRIPTION_ERROR'] = "<spring:message code='ADH_1001_ADHOC_SAVE_REPORT_DESCRIPTION_ERROR' javaScriptEscape='true'/>";
        messages['ADH_1001_ADHOC_SAVE_REPORT_LABEL_ERROR'] = "<spring:message code='ADH_1001_ADHOC_SAVE_REPORT_LABEL_ERROR' javaScriptEscape='true'/>";
        messages['ADH_162_NULL_SAVE_REPORT_FOLDER_MESSAGE'] = "<spring:message code='ADH_162_NULL_SAVE_REPORT_FOLDER_MESSAGE' javaScriptEscape='true'/>";
        messages['ADH_1001_SERVER_REPOSITORY_ACCESS_DENIED'] = "<spring:message code='ADH_1001_SERVER_REPOSITORY_ACCESS_DENIED' javaScriptEscape='true'/>";

        messages['loading'] = "<spring:message code='jsp.loading' javaScriptEscape='true'/>";

        <c:forEach var="customFilter" items="${configuration.customFilters}">
            <c:forEach var="option" items="${customFilter.options}">
                messages["${option.labelId}"] = '<spring:message code="${option.labelId}" javaScriptEscape="true"/>';
            </c:forEach>
        </c:forEach>

        <c:forEach var="permission" items="${permissions}">
           messages["${permission.labelId}"] = '<spring:message code="${permission.labelId}" javaScriptEscape="true"/>';
        </c:forEach>

        <c:forEach var="sorter" items="${configuration.customSorters}">
            messages["${sorter.labelId}"] = '<spring:message code="${sorter.labelId}" javaScriptEscape="true"/>';
        </c:forEach>

        return messages;

    })({});

    __jrsConfigs__.repositorySearch["localContext"] = {};

    // Initialization of repository search init object.
    __jrsConfigs__.repositorySearch.localContext["rsInitOptions"] = {
        flowExecutionKey: '${flowExecutionKey}',
        state: JSON.parse('${state}'),
        configuration: JSON.parse('${jsonConfiguration}'),
        organizationId: "${organizationId}",
        publicFolderUri: "${publicFolderUri}",
        tempFolderUri: "${tempFolderUri}",
        rootFolderUri: "${rootFolderUri}",
        organizationsFolderUri: "${organizationsFolderUri}",
        folderSeparator: "${folderSeparator}",
        mode: "${mode}",
        systemConfirm: "${systemConfirm}",
        isAnalysisFeatureEnabled: ${isAnalysisFeatureEnabled},
        isDashboardFeatureEnabled: ${isDashboardFeatureEnabled},
        isAdHocFeatureEnabled: ${isAdHocFeatureEnabled},
        isAdministrator: false,
        errorPopupMessage: "${requestScope.errorPopupMessage}",
        isFolderSet: ${isFolderSet == 'true'},
        isAdHoReportDisabled: true
    };

    <authz:authorize access="hasRole('ROLE_ADMINISTRATOR')">
        __jrsConfigs__.repositorySearch.localContext.rsInitOptions.isAdministrator = true;
    </authz:authorize>

    //TODO: move to setup list component

    __jrsConfigs__.dynamicList = {};

    __jrsConfigs__.dynamicList["i18n"] = (function(messages){
        messages['listNItemsSelected'] = '<spring:message code="RM_N_RESOURCES_SELECTED" javaScriptEscape="true"/>';
        return messages;
    })({});
</script>
</js:out>
<%-- Insert CSRF script here, which is responsible for sending CSRF security token --%>
<%--<script src="<c:url value="/JavaScriptServlet"/>"></script>--%>
