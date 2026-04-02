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

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle"><spring:message code="messages.messageList.page.title"/></t:putAttribute>
    <t:putAttribute name="bodyID" value="messages"/>
    <t:putAttribute name="bodyClass" value="oneColumn"/>
    <t:putAttribute name="moduleName" value="messages/list/messageListMain"/>

    <t:putAttribute name="headerContent" >

        <script type="text/javascript">

            __jrsConfigs__.messagesListInitOptions = {
                flowExecutionKey: "${flowExecutionKey}",
                systemConfirmMessages: {
                    "markAsRead": "<spring:message code="messages.messageList.systemConfirm.markAsRead"/>",
                    "markAsUnread": "<spring:message code="messages.messageList.systemConfirm.markAsUnread"/>",
                    "delete": "<spring:message code="messages.messageList.systemConfirm.delete"/>"
                }
            };

        </script>

    </t:putAttribute>
    <t:putAttribute name="bodyContent" >
        <!-- NEW STYLE PAGE TITLE -->
        <div class="pageHeader ">
            <div class="pageHeader-title">
                <div class="pageHeader-title-icon">
                    <span class="icon messages"></span>
                </div>
                <div class="pageHeader-title-text"><spring:message code="messages.messageList.page.title"/></div>
            </div>
        </div>

        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="column decorated primary showingToolBar"/>
            <t:putAttribute name="containerTitleEmpty" value="${true}"></t:putAttribute>
            <t:putAttribute name="subHeaderContent">
                <div id="toolbar" class="toolbar">
                    <ul class="list buttonSet">
						<li class="node open">
                            <ul class="list buttonSet">
                                <li class="leaf"><button id="markAsRead" class="button capsule text up first"><span class="wrap">
                                            <spring:message code="messages.messageList.toolbar.button.markAsRead" />
                                        </span><span class="icon"></span></button></li>
                                <li class="leaf"><button id="markAsUnread" class="button capsule text up last"><span class="wrap">
                                            <spring:message code="messages.messageList.toolbar.button.markAsUnread" />
                                        </span><span class="icon"></span></button></li>
                                <li class="leaf"><button id="delete" class="button capsule text up"><span class="wrap">
                                            <spring:message code="messages.messageList.toolbar.button.delete" />
                                        </span></button></li>
                            </ul>
						</li>
                    </ul>

                    <ul id="sortMode" class="list tabSet text control horizontal" tabindex="-1" js-navtype="none" js-stdnav="false">
                        <li js-navtype="dynamiclist" id="sortMode_item1" class="label first" tabindex="-1" disabled="disabled">
                            <p class="wrap">
                                <spring:message code="messages.messageList.filter.title.showMessage" />:
                            </p>
                        </li>
                        <li js-navtype="dynamiclist" id="sortMode_item2" class="tab selected cursor" tabindex="-1">
                            <p class="wrap button">
                                <spring:message code="messages.messageList.filter.all" />
                            </p>
                        </li>
                        <li js-navtype="dynamiclist" id="sortMode_item3" class="tab last" tabindex="-1">
                            <p class="wrap button">
                                <spring:message code="messages.messageList.filter.unread" />
                            </p>
                        </li>
                    </ul>
                </div>
                <ul id="messageListHeader" class="list collapsible tabular type_messages fourColumn header">
                <li id="messageListHeader_item1" class="leaf first">
                    <div class="wrap type_messages">
                        <div class="column one">
                            <p class="subject" title="Message subject">
                                <spring:message
                                    code="messages.messageList.message.header.subject" />
                            </p>
                        </div>
                        <div class="column two">
                            <p class="date" title="Event Date">
                                <spring:message code="messages.messageList.message.header.date" />
                            </p>
                        </div>
                        <div class="column three">
                            <p class="type" title="Event Type">
                                <spring:message code="messages.messageList.message.header.type" />
                            </p>
                        </div>
                        <div class="column four">
                            <p class="component" title="Affected Component">
                                <spring:message
                                    code="messages.messageList.message.header.component" />
                            </p>
                        </div>
                    </div>
                </li>
            </ul>
            </t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ol id="messageList" class=""></ol>
            </t:putAttribute>
        </t:insertTemplate>
    </t:putAttribute>
</t:insertTemplate>
