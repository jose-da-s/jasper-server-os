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
            <c:when test='${masterFlow == "reportUnit" && masterFlowStep != "query"}'>
                <c:choose>
                    <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/></c:when>
                    <c:otherwise><spring:message code="resource.report.title"/>:</c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${query.editMode}"><spring:message code="resource.query.titleEdit"/></c:when>
                    <c:otherwise><spring:message code="resource.query.title"/></c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_locateDataSource"/>
    <t:putAttribute name="bodyClass">
        oneColumn flow
        <c:choose>
            <c:when test='${masterFlow == "reportUnit" && masterFlowStep != "query"}'>
                oneStep
            </c:when>
            <c:otherwise>
                wizard
            </c:otherwise>
        </c:choose>
    </t:putAttribute>

    <t:putAttribute name="moduleName" value="addResource/query/addQueryWithResourceLocatorMain"/>
    <t:putAttribute name="headerContent">
        <jsp:include page="queryStep2State.jsp"/>
    </t:putAttribute>

    <t:putAttribute name="bodyContent">
        <form method="post"  action="flow.html">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="containerTitle">
                    <c:choose>
                        <c:when test='${masterFlow == "reportUnit" && masterFlowStep != "query"}'>
                            <c:choose>
                                <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/>:</c:when>
                                <c:otherwise><spring:message code="resource.report.title"/>:</c:otherwise>
                            </c:choose>
                            ${wrapper.reportUnit.label}
                        </c:when>
                        <c:otherwise>
                            <c:choose>
                                <c:when test="${query.editMode}"><spring:message code="resource.query.titleEdit"/>:</c:when>
                                <c:otherwise><spring:message code="resource.query.title"/>:</c:otherwise>
                            </c:choose>
                            ${query.query.label}
                        </c:otherwise>
                    </c:choose>
                </t:putAttribute>

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                    <c:choose>
                        <c:when test='${masterFlow == "reportUnit" && masterFlowStep != "query"}'>
                            <div id="flowControls">
                                <ul class="control tabSet buttons vertical">
                                    <li class="tab first">
                                        <button class="button up" id="steps1_2"><span class="wrap"><spring:message code="resource.report.setup"/></span></button>
                                    </li>
                                    <!--/.tab-->
                                    <li class="tab">
                                        <!-- NOTE: tabs below are disabled until required information is entered on this page -->
                                        <button class="button up" id="step3"><span class="wrap"><spring:message code="resource.report.controlsAndReources"/></span></button>
                                    </li>
                                    <!--/.tab-->
                                    <li class="tab selected">
                                        <button class="button up" id="step4"><span class="wrap"><spring:message code="resource.report.dataSource"/></span></button>
                                    </li>
                                    <!--/.tab-->
                                    <li class="tab">
                                        <button class="button up" id="step5"><span class="wrap"><spring:message code="resource.report.query"/></span></button>
                                    </li>
                                    <!--/.tab-->
                                    <li class="tab last">
                                        <button class="button up" id="step6"><span class="wrap"><spring:message code="resource.report.customization"/></span></button>
                                    </li>
                                    <!--/.tab-->
                                </ul>
                                <!--/.control-->
                            </div>
                            <input type="hidden" id="jumpToPage" name="jumpToPage"/>
                            <input type="submit" style="visibility:hidden;" value="" name="_eventId_jumpTo" id="jumpButton"/>
                            <%--<input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>--%>
                        </c:when>
                        <c:otherwise>
                            <div id="flowControls">
                                <!-- NOTE: insert appropriate navigation, if any, here -->
                                <ul class="list stepIndicator">
                                    <li class="leaf"><p class="wrap" href="#"><b class="icon"></b><spring:message
                                            code="resource.query.nameQuery"/></p></li>
                                    <li class="leaf selected"><p class="wrap" href="#"><b class="icon"></b><spring:message
                                            code="resource.query.linkDataSource"/></p></li>
                                    <li class="leaf"><p class="wrap" href="#"><b class="icon"></b><spring:message
                                            code="resource.query.defineQuery"/></p></li>
                                </ul>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div id="stepDisplay">
                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.query.instructions"/></span>
                            </legend>
                            <h2 class="textAccent02">
                                <c:choose>
                                    <c:when test='${masterFlow == "reportUnit" && masterFlowStep != "query"}'>
                                        <spring:message code="resource.report.linkDataSource"/>
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="resource.query.linkDataSource2"/>
                                    </c:otherwise>
                                </c:choose>
                            </h2>
                            <h4>
                                <c:choose>
                                    <c:when test='${masterFlow == "reportUnit" && masterFlowStep != "query"}'>
                                        <spring:message code="resource.report.linkDataSource2"/>
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="resource.query.linkDataSource3"/>
                                    </c:otherwise>
                                </c:choose>
                            </h4>

                            <p class="warning">Error or warning here.</p>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.query.inputs"/></span></legend>

                            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                <t:putAttribute name="containerClass" value="column noHeader primary"/>

                                <t:putAttribute name="bodyContent">
                                    <fieldset class="locationSelector">
                                        <legend class="offLeft"><span><spring:message
                                                code="resource.query.linkDataSource2"/></span></legend>
                                        <ul class="list locations">
                                            <spring:bind path="dataResource.source">
                                                <li id="noLink" class="leaf">
                                                    <div class="control radio">
                                                        <label class="wrap" for="NONE"
                                                           title="<spring:message code='resource.query.noLinkDataSource'/>">
                                                           <spring:message code="resource.query.noLinkDataSource"/>
                                                        </label>
                                                        <input type="radio" name="${status.expression}" value="NONE" id="NONE"
                                                                <c:if test='${status.value=="NONE"}'>checked="checked"</c:if>/>
                                                        <c:if test="${status.value == 'NONE' and status.error}">
                                                            <span class="message warning">${status.errorMessage}</span>
                                                        </c:if>
                                                    </div>
                                                </li>
                                                <li id="create" class="leaf">
                                                    <div class="control radio <c:if test="${status.value == 'LOCAL' and status.error}"> error</c:if>">
                                                        <label class="wrap" for="LOCAL" title="<spring:message code='resource.query.defineDataSource'/>">
                                                            <a id="newDataSourceLink" href="#" class="<c:if test='${status.value=="LOCAL"}'>launcher</c:if>">
                                                            <spring:message code="resource.query.defineDataSource"/></a>
                                                        </label>
                                                        <input type="radio" name="${status.expression}" value="LOCAL" id="LOCAL" <c:if test='${status.value=="LOCAL"}'>checked="checked"</c:if>/>
                                                        <c:if test="${status.value == 'LOCAL' and status.error}">
                                                            <span class="message warning">${status.errorMessage}</span>
                                                        </c:if>
                                                    </div>
                                                </li>
                                                <li id="fromRepo" class="leaf">
                                                    <div class="control radio complex">
                                                        <label class="wrap" for="CONTENT_REPOSITORY"
                                                           title="<spring:message code="resource.report.repository"/>">
                                                            <spring:message code='resource.query.repository'/>
                                                        </label>
                                                        <input name="${status.expression}" type="radio" value="CONTENT_REPOSITORY"
                                                           id="CONTENT_REPOSITORY" <c:if test='${status.value == "CONTENT_REPOSITORY"}'>checked="checked"</c:if> />
                                                    </div>
                                            </spring:bind>
                                            <spring:bind path="dataResource.selectedUri">
                                                        <label  for="resourceUri" class="control browser <c:if test="${status.error}"> error</c:if>">
                                                            <input id="resourceUri" type="text" name="${status.expression}" value="${status.value}" title="<spring:message code="resource.report.repository"/>" <c:if test="${empty status.value && !(dataResource.source == 'CONTENT_REPOSITORY' and status.error)}">disabled="disabled"</c:if>/>
                                                            <button id="browser_button" type="button" class="button action up" <c:if test="${empty status.value && !(dataResource.source == 'CONTENT_REPOSITORY' and status.error)}">disabled="disabled"</c:if>><span class="wrap"><spring:message code="button.browse"/><span class="icon"></span></span></button>
                                                            <c:if test="${dataResource.source == 'CONTENT_REPOSITORY' and status.error}">
                                                                <span class="message warning">${status.errorMessage}</span>
                                                            </c:if>
                                                        </label>
                                            </spring:bind>
                                                </li>
                                        </ul>
                                    </fieldset>
                                </t:putAttribute>
                            </t:insertTemplate>

                        </fieldset>
                    </div>
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="previous" type="submit" class="button action up" name="_eventId_back"><span class="wrap"><spring:message
                                    code='button.previous'/></span><span class="icon"></span></button>
                            <button id="next" type="submit" class="button action up" name="_eventId_next"><span
                                    class="wrap"><spring:message
                                    code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" type="submit" class="button primary action up" name="_eventId_save"><span
                                    class="wrap"><spring:message code='button.submit'/></span><span
                                    class="icon"></span></button>
                            <button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span
                                    class="wrap"><spring:message
                                    code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
            <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
            <input type="hidden" name="ParentFolderUri" value='${query.query.parentFolder}'>
        </form>

        <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="queryTreeRepoLocation"></ul>
            </t:putAttribute>
        </t:insertTemplate>

        <div id="ajaxbuffer" class="hidden"></div>

    </t:putAttribute>
</t:insertTemplate>
