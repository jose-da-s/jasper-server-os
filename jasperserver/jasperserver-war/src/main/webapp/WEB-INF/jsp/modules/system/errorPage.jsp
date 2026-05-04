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

<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle"><spring:message code="jsp.JSErrorPage.title"/></t:putAttribute>
    <t:putAttribute name="bodyID" value="serverError"/>
    <t:putAttribute name="moduleName" value="system/errorMain"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow"/>
    <t:putAttribute name="bodyContent">
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="column decorated primary"/>
            <t:putAttribute name="containerTitle"><spring:message code="jsp.JSErrorPage.error"/></t:putAttribute>
            <t:putAttribute name="headerContent" cascade="false">
                <%-- this code allows to close error page in case it was embedded as an iframe --%>
                <c:if test="${not empty param.embeddedDesigner}">
                    <div id="closeErrorPage" class="closeIcon"></div>
                </c:if>
            </t:putAttribute>
            <t:putAttribute name="bodyID" value="errorPageContent"/>
            <t:putAttribute name="bodyContent">
                <c:if test="sessionAttributeMissingException">
                    <input type="hidden" id="sessionAttributeMissingException" value="true"/>
                </c:if>

                <div id="stepDisplay">
                    <fieldset class="row" style="margin-top: 10px;">
                        <h3>
                            <spring:message code="jsp.JSErrorPage.errorMsg"/>
                        </h3>
                        <div id="errorMessages">
                            <c:forEach var="msg" items="${errorMessages}">
                                <spring:message code="${msg}" arguments="${exceptionArgs}"/>
                            </c:forEach>
                        </div>
                    </fieldset>
                    <c:if test="${not empty stackTraces}">
                        <fieldset class="row oneColumn" style="margin-top: 10px;">
                            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                <t:putAttribute name="containerClass" value="column noHeader primary"/>
                                <t:putAttribute name="bodyContent">
                                    <c:forEach var="stackTrace" items="${stackTraces}" >
                                        <c:if test="${isIPad}"><div class="swipeScroll" style="height:480px;overflow:hidden;border-top:solid 1px #ccc;padding-top:12px;"></c:if>
                                        <div id="completeStackTrace" style="padding-bottom:350px;">
                                            <h3><spring:message code="jsp.JSErrorPage.errorTrace"/></h3>
                                            <div style="white-space:normal;">${stackTrace}</div>
                                        </div>
                                        <c:if test="${isIPad}"></div></c:if>
                                    </c:forEach>
                                </t:putAttribute>
                            </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </c:if>
                </div><!--/#stepDisplay-->
            </t:putAttribute>
        </t:insertTemplate>
    </t:putAttribute>
</t:insertTemplate>


