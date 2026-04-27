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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="oDesc" value="${(param.oDesc == null || param.oDesc == '') ? param.oName : param.oDesc}"/>

<li class="leaf first">
    <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
        <t:putAttribute name="containerClass" value="panel pane settings"/>

        <t:putAttribute name="bodyClass" value="twoColumn"/>
        <t:putAttribute name="bodyContent">
            <div class="column simple primary">
                <div class="control checkBox">

                    <label class="wrap" for="input_${param.oName}" title="<spring:message code='${param.oLabelCode}' />">
                        <spring:message code="${param.oLabelCode}"/>
                    </label>
                    <c:if test="${param.oName=='showDistinctRowValues'}">
                        <input ${!param.oValue ? "checked=\"checked\"" : "" } id="input_${param.oName}" type="checkbox" value=""/>
                    </c:if>
                    <c:if test="${param.oName !='showDistinctRowValues'}">
                        <input ${param.oValue ? "checked=\"checked\"" : "" } id="input_${param.oName}" type="checkbox" value=""/>
                    </c:if>
                    <span class="message warning" id="error_${param.oName}">&nbsp;</span>
                </div>
				<div class="control description">
					<spring:message code="${param.oDesc}"/>
				</div>
            </div>
            <div class="column simple secondary">
                <fieldset class="actions">
                    <button id="save" name="${param.oName}" disabled="true" class="button action primary up"><span class="wrap"><spring:message code="button.change"/></span></button>
                    <button id="cancel" name="${param.oName}" value="${param.oValue}"  disabled="true" class="button action up"><span class="wrap"><spring:message code="button.cancel"/></span></button>
                </fieldset>
            </div>
        </t:putAttribute>
    </t:insertTemplate>
</li>
