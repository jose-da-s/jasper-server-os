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
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle"><spring:message code="messages.messageDetail.page.title"/></t:putAttribute>
    <t:putAttribute name="bodyID" value="messageDetail"/>
    <t:putAttribute name="bodyClass" value="oneColumn"/>
    <t:putAttribute name="moduleName" value="messages/details/messageDetailsMain"/>
    <t:putAttribute name="headerContent" >
        <js:out javaScriptEscape="true">
        <script type="text/javascript">
            __jrsConfigs__.messageDetailsInitOptions = {
                flowExecutionKey: "${flowExecutionKey}",
                message: JSON.parse('${message}')
            };
        </js:out>
        </script>
    </t:putAttribute>

    <t:putAttribute name="bodyContent" >
        <!-- NEW STYLE PAGE TITLE -->
        <div class="pageHeader ">
            <div class="pageHeader-title">
                <div class="pageHeader-title-icon">
                    <span class="icon messages"></span>
                </div>
                <div class="pageHeader-title-text"><spring:message code="messages.messageDetail.page.title"/></div>
            </div>
        </div>
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerTitleEmpty" value="${true}"></t:putAttribute>
            <t:putAttribute name="containerClass" value="column decorated primary showingToolBar"/>
            <t:putAttribute name="subHeaderContent">
                <div id="toolbar" class="toolbar">
                    <ul class="list buttonSet">
                        <li class="leaf"><button id="back" class="button capsule text up"><span class="wrap"><spring:message code="messages.messageDetail.toolbar.button.back"/></span><span class="icon"></span></button></li>
                    </ul>
                </div>
            </t:putAttribute>

            <t:putAttribute name="bodyContent">
                <div class="message">
                    <div class="label subject"><spring:message code="messages.messageDetail.message.label.subject"/>:<p class="wrap" id="subject"></p></div>
                    <div class="label date"><spring:message code="messages.messageDetail.message.label.date"/>:<p class="wrap" id="date"></p></div>
                    <div class="label component"><spring:message code="messages.messageDetail.message.label.component"/>:<p class="wrap" id="component"></p></div>
                    <div class="label details"><spring:message code="messages.messageDetail.message.label.message"/>:<p class="wrap" id="message"></p></div>
				</div>
            </t:putAttribute>
        </t:insertTemplate>
    </t:putAttribute>
</t:insertTemplate>
