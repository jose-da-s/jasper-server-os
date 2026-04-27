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

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
	<t:putAttribute name="pageTitle"><spring:message code="report.scheduling.list.title"/></t:putAttribute>
	<t:putAttribute name="bodyID" value="scheduler_jobSummary"/>
	<t:putAttribute name="bodyClass" value="oneColumn scheduler_jobSummary"/>

	<t:putAttribute name="moduleName" value="scheduler/schedulerMain"/>

	<t:putAttribute name="headerContent">

		<jsp:include page="../inputControls/commonInputControlsImports.jsp" />

		<js:out javaScriptEscape="true">
		<script type="text/javascript">
			__jrsConfigs__.usersTimeZone = "${timezone}";
			__jrsConfigs__.enableSaveToHostFS = "${enableSaveToHostFS}";
			__jrsConfigs__.reportJobEditorDefaults = JSON.parse('${reportJobDefaults}');
			__jrsConfigs__.availableReportJobOutputFormats = JSON.parse('${availableReportJobOutputFormats}');
			__jrsConfigs__.availableDashboardJobOutputFormats = JSON.parse('${availableDashboardJobOutputFormats}');
			__jrsConfigs__.VALUE_SUBSTITUTION = "<spring:message code="input.password.substitution"/>";

			__jrsConfigs__.timeZones = [];
            <c:forEach items="${userTimezones}" var="timezone">
                __jrsConfigs__.timeZones.push({value: "${timezone.code}", title: "<spring:message code="timezone.option" arguments='${timezone.code},${timezone.description}'/>"});
            </c:forEach>
		</script>
		</js:out>
	</t:putAttribute>

	<t:putAttribute name="bodyContent" >
		<div id="saveValues" class="panel dialog saveValues overlay moveable centered_horz centered_vert hidden" >
			<div  class="content hasFooter " >
				<div  class="header mover" >
					<div class="title">
						<spring:message code='dialog.saveValues.title'/>
					</div>
				</div>
				<div  class="body  " >
					<label class="control input text" accesskey="o" for="savedValuesName" title="<spring:message code="dialog.saveValues.visibleResourceName" javaScriptEscape="true"/>">
						<span class="wrap"><spring:message code='dialog.saveValues.nameForSavedValues'/> (<spring:message code='required.field' javaScriptEscape="true"/>): </span>
						<input class="" id="savedValuesName" type="text" value=""/>
						<span class="message warning">error message here</span>
					</label>
				</div>
				<div  class="footer " >
					<button id="saveAsBtnSave" class="button action primary up"><span class="wrap"><spring:message code='dialog.saveValues.save'/><span class="icon"></span></button>
					<button id="saveAsBtnCancel" class="button action up"><span class="wrap"><spring:message code='dialog.saveValues.cancel'/><span class="icon"></span></button>
				</div>
			</div>
		</div>

		<jsp:include page="../inputControls/InputControlTemplates.jsp" />

	</t:putAttribute>

</t:insertTemplate>
