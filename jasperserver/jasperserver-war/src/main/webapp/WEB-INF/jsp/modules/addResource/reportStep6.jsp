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

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle">
        <c:choose>
            <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/></c:when>
            <c:otherwise><spring:message code="resource.report.title"/></c:otherwise>
        </c:choose>
    </t:putAttribute>
    <t:putAttribute name="moduleName" value="commons/commonsMain"/>
    <t:putAttribute name="bodyID" value="addReport_Naming"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow"/>
    <t:putAttribute name="bodyContent">
        <form action="flow.html" method="post">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="containerTitle">
                    <c:choose>
                        <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/>:</c:when>
                        <c:otherwise><spring:message code="resource.report.title"/>:</c:otherwise>
                    </c:choose>
                    ${wrapper.reportUnit.label}
                </t:putAttribute>

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                    <div id="flowControls">
                        <ul class="control tabSet buttons vertical">
                            <li class="tab first">
                                <button name="_eventId_reportNaming" type="submit" class="button up" id="steps1_2" ><span class="wrap"><spring:message code="resource.report.setup"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab">
                                <!-- NOTE: tabs below are disabled until required information is entered on this page -->
                                <button name="_eventId_resources" type="submit" class="button up" id="step3"><span class="wrap"><spring:message code="resource.report.controlsAndReources"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab">
                                <button name="_eventId_dataSource" type="submit" class="button up" id="step4"><span class="wrap"><spring:message code="resource.report.dataSource"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab">
                                <button name="_eventId_query" type="submit" class="button up" id="step5"><span class="wrap"><spring:message code="resource.report.query"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab selected last">
                                <button name="_eventId_customization" type="submit" class="button up" id="step6"><span class="wrap"><spring:message code="resource.report.customization"/></span></button>
                            </li>
                            <!--/.tab-->
                        </ul>
                        <!--/.control-->
                    </div>
                    <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>

                    <div id="stepDisplay">
                        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>

                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.report.instructions"/></span></legend>
                            <h2 class="textAccent02"><spring:message code="resource.report.customization"/></h2>
                            <h4><spring:message code="resource.report.locateView"/></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.report.inputs"/></span></legend>
                                <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                    <t:putAttribute name="containerClass" value="column primary noHeader"/>

                                    <t:putAttribute name="bodyContent">
                                        <spring:bind path="wrapper.reportUnit.reportRenderingView">
                                            <fieldset>
                                                <legend class="offLeft"><span><spring:message code="resource.report.nameAndDescription"/></span></legend>
                                                <label class="control input text required <c:if test="${status.error}">error</c:if>" for="${status.expression}" title="<spring:message code='resource.report.fileToRenderReport'/>">
                                                    <span class="wrap"><spring:message code="resource.report.JSPLocation"/>:</span>
                                                    <input id="${status.expression}" name="${status.expression}" type="text" value="${status.value}"/>
                                                    <span class="hint">(<spring:message code="resource.report.JSPLocationPath"/>)</span>
                                                    <c:if test="${status.error}">
                                                        <span class="message warning">${status.errorMessage}</span>
                                                    </c:if>
                                                </label>
                                            </fieldset>
                                        </spring:bind>
                                    </t:putAttribute>
                                </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="done" class="button action primary up" type="submit" name="_eventId_save"><span class="wrap"><spring:message code="button.submit" javaScriptEscape="true"/></span><span class="icon"></span></button>
                            <button id="cancel" class="button action up" type="submit" name="_eventId_cancel"><span class="wrap"><spring:message code="button.cancel" javaScriptEscape="true"/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
    </t:putAttribute>
</t:insertTemplate>
