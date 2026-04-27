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
<!--
*** DEVELOPMENT NOTES ***

1. This mock used to define the location step for flows involving several different resources:
	 - Datatype
	 - Data Source
	 - Query
	 - OLAP Schema
	 - Access Grant
	In this mock, the token [resourceType] is used to represent the specific type in the page title, page ID and some of the visible strings.

	The developer should replace this token with the appropriate string when implementing the page,
	or, if this page used as a basis for creating a template, when calling the template.

	The pageID value is used in pageSpecific.css to control display and position of location options, so it must be assigned correctly.

2. Depending upon what flow this step is inserted into the developer must insert the correct flow navigation or step indicator into #flowControls.

PROVIDING FEEDBACK TO THE USER

The interactive elements associated with #fromLocal and #fromRepo should be set to disabled="disabled" until their associated radio button is selected.  If the radio button is again toggled off, then the element should be reset to disabled.


FINALLY
 Do not include these notes, or any HTML comment below that begins 'NOTE: ...' in the production page
-->
<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle">
        <c:choose>
            <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/></c:when>
            <c:otherwise><spring:message code="resource.report.title"/></c:otherwise>
        </c:choose>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_locateQuery"/>
    <!-- NOTE:

    -->
    <t:putAttribute name="bodyClass" value="oneColumn flow"/>
    <t:putAttribute name="moduleName" value="addResource/query/locateQueryMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="reportStep5State.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <form method="post" action="flow.html">
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
                                <button class="button up" id="steps1_2"><span class="wrap"><spring:message code="resource.report.setup"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab">
                                <!-- NOTE: tabs below are disabled until required information is entered on this page -->
                                <button class="button up" id="step3"><span class="wrap"><spring:message code="resource.report.controlsAndReources"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab">
                                <button class="button up" id="step4"><span class="wrap"><spring:message code="resource.report.dataSource"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab selected">
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
                    <input type="submit" style="visibility:hidden;" value="" name="_eventId_jump" id="jumpButton"/>
                    <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
                    <input type="hidden" id="ParentFolderUri" value='${param.ParentFolderUri}'>
                    <div id="stepDisplay">
                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.report.instructions"/></span></legend>
                            <h2 class="textAccent02"><spring:message code="resource.report.locateQuery"/></h2>
                            <h4><!--NOTE: keep h4 markup, but leave empty --></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.report.inputs"/></span></legend>

                                <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                    <t:putAttribute name="containerClass" value="column noHeader primary"/>

                                    <t:putAttribute name="bodyContent">
                                        <fieldset class="locationSelector">
                                                <legend class="offLeft"><span><spring:message code="resource.report.locateResource"/></span></legend>
                                                <ul class="list locations">
                                                    <spring:bind path="queryReference.source">
                                                        <c:if test="${allowNone}">
                                                            <li id="noLink" class="leaf">
                                                                <div class="control radio">
                                                                    <label class="wrap" for="NONE" title="<spring:message code="resource.report.noLinkQuery"/>">
                                                                        <spring:message code="resource.report.noLinkQuery"/>
                                                                    </label>
                                                                    <input id="NONE" name="${status.expression}" type="radio" value="NONE" <c:if test='${status.value=="NONE"}'>checked="checked"</c:if>/>
                                                                </div>
                                                            </li>
                                                        </c:if>
                                                        <li id="create" class="leaf">
                                                            <div class="control radio <c:if test="${queryReference.source == 'LOCAL' and status.error}"> error</c:if>">
                                                                <label class="wrap" for="LOCAL" title="<spring:message code="resource.report.defineQuery"/>">
                                                                    <a id="newQueryLink" href="#" class="<c:if test="${status.value == 'LOCAL'}">launcher</c:if>">
                                                                    <spring:message code="resource.report.defineQuery"/></a>
                                                                </label>
                                                                <input class="" id="LOCAL" name="${status.expression}" type="radio" value="LOCAL"
                                                                    <c:if test='${status.value=="LOCAL" or (not allowNone and status.value == "NONE" and empty queryLookups)}'>checked="checked"</c:if>
                                                                />
                                                                <c:if test="${queryReference.source == 'LOCAL' and status.error}">
                                                                    <span class="message warning">${status.errorMessage}</span>
                                                                </c:if>
                                                            </div>
                                                        </li>
                                                        <li id="fromRepo" class="leaf">
                                                            <div class="control radio complex">
                                                                <label class="wrap" for="CONTENT_REPOSITORY" title="<spring:message code="resource.report.repository"/>">
                                                                    <spring:message code="resource.report.selectQuery"/>
                                                                </label>
                                                                <input class="" id="CONTENT_REPOSITORY" name="${status.expression}" type="radio" title="<spring:message code="resource.report.repository"/>" value="CONTENT_REPOSITORY"
                                                                    <c:if test='${status.value == "CONTENT_REPOSITORY" or (not allowNone and status.value == "NONE" and not empty queryLookups)}'>checked="checked"</c:if>
                                                                    <c:if test="${empty queryLookups}">disabled</c:if>
                                                                />
                                                            </div>
                                                    </spring:bind>
                                                            <spring:bind path="queryReference.referenceURI">
                                                                <label  for="resourceUri" class="control browser <c:if test="${status.error}"> error</c:if>">
                                                                    <input id="resourceUri" type="text" name="${status.expression}" value="${queryReference.source == 'CONTENT_REPOSITORY' ? status.value : ''}" title="<spring:message code="resource.report.repository"/>" <c:if test="${empty status.value || queryReference.source != 'CONTENT_REPOSITORY'}">disabled="disabled"</c:if>/>
                                                                    <button id="browser_button" type="button" class="button action up" <c:if test="${empty status.value and queryReference.source != 'CONTENT_REPOSITORY'}">disabled="disabled"</c:if>><span class="wrap"><spring:message code="button.browse"/><span class="icon"></span></span></button>
                                                                    <c:if test="${queryReference.source == 'CONTENT_REPOSITORY' and status.error}">
                                                                        <span class="message warning">${status.errorMessage}</span>
                                                                    </c:if>
                                                                </label>
                                                            </spring:bind>
                                                        </li>
                                                </ul>
                                            </fieldset>
                                    </t:putAttribute>
                                </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="previous" name="_eventId_previous" type="submit" class="button action up"><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
                            <button id="next" name="_eventId_next" type="submit" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" name="_eventId_finish" type="submit" class="button primary action up"><span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span></button>
                            <button id="cancel" name="_eventId_cancel" type="submit" class="button action up"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>

                    <%--<jsp:include page="reportStep2State.jsp"/>--%>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
        <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="queryTreeRepoLocation"> </ul>
            </t:putAttribute>
        </t:insertTemplate>

        <div id="ajaxbuffer" class="hidden" ></div>
    </t:putAttribute>
</t:insertTemplate>
