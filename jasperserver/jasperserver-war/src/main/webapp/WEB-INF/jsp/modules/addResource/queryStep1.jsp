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

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!--
*** DEVELOPMENT NOTES ***

*** CHANGES FROM 3.7 FUNCTIONALITY ***

1). A progress indicator has been added, see object .list.stepIndicator
    - implementing this feature is P2; only do so if will not impact current schedule
    - in the case that a wizard is created as a sub-wizard, then the sub-wizard steps are included as a nested
      list under appropriate entry in primary wizard stepIndicator; see lists.jsp for example

PROVIDING FEEDBACK TO USER
  - The advance button in #wizardNav has attribute disabled="disabled" until all required information values are set for the step
  - See notes in source for /samples/wizard regarding setting appropriate styles for controlling buttons that display in #wizardNav
  - Upon reaching end of wizard and successful submit,
    - user is returned to repository view from which add resource action was initiated
    - success message displayed in #systemConfirm on repository view page
      - '[query name] added to [folder name]'

FINALLY
 Do not include these notes, or any HTML comment below that begins 'NOTE: ...' in the production page
-->

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle"><c:choose><c:when test="${query.editMode=='true'}"><spring:message code="resource.query.titleEdit"/></c:when><c:otherwise><spring:message code="resource.query.title"/></c:otherwise></c:choose></t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_query_step1"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard firstStep"/>
    <t:putAttribute name="moduleName" value="addResource/query/addQueryMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="queryStep1State.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <form method="post" action="flow.html">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="containerTitle">
                    <c:choose><c:when test="${query.editMode=='true'}"><spring:message code="resource.query.titleEdit"/></c:when><c:otherwise><spring:message code="resource.query.title"/></c:otherwise></c:choose>:
                    ${query.query.label}
                </t:putAttribute>

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                <div id="flowControls">
                    <ul class="list stepIndicator">
                        <li class="leaf selected"><p class="wrap" href="#"><b class="icon"></b><spring:message code="resource.query.nameQuery"/></p></li>
                        <li class="leaf"><p class="wrap" href="#"><b class="icon"></b><spring:message code="resource.query.linkDataSource"/></p></li>
                        <li class="leaf"><p class="wrap" href="#"><b class="icon"></b><spring:message code="resource.query.defineQuery"/></p></li>
                    </ul>
                </div>
                    <div id="stepDisplay">
                        <input type="hidden" id="ParentFolderUri" value='${param.ParentFolderUri}'>
                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.query.instructions"/></span></legend>
                            <h2 class="textAccent02"><spring:message code="resource.query.nameQuery"/></h2>
                            <h4><spring:message code="resource.query.requiredValues"/></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.query.inputs"/></span></legend>

                                <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                    <t:putAttribute name="containerClass" value="column noHeader primary"/>

                                    <t:putAttribute name="bodyContent">
                                        <fieldset>
                                            <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>

                                            <!-- NOTE: This fieldset appears identically for all add resource data source types -->
                                            <legend class="offLeft"><span><spring:message code="resource.query.nameAndDescription"/></span></legend>
                                            <spring:bind path="query.query.label">
                                                <label class="control input text <c:if test="${status.error}">error</c:if>" class="required" for="${status.expression}" title="<spring:message code='resource.query.visibleResourceName'/>">
                                                    <span class="wrap"><spring:message code="resource.query.name"/> (<spring:message code='required.field'/>):</span>
                                                    <input class="" id="query.label" name="${status.expression}" type="text" value="${status.value}" size="40"/>
                                                    <span class="message warning">
                                                        <c:if test="${status.error}">${status.errorMessage}</c:if>
                                                    </span>
                                                </label>
                                            </spring:bind>

                                            <spring:bind path="query.query.name">
                                                <input type="hidden" id="editMode" value="${query.editMode}">
                                                <label class="control input text <c:if test="${status.error}">error</c:if>" class="required" for="${status.expression}" title="<spring:message code='resource.query.visibleResourceID'/>">
                                                    <span class="wrap"><spring:message code="resource.query.resourceID"/>
                                                        <c:choose>
                                                            <c:when test="${query.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                            <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <input class="" id="query.name" name="${status.expression}" type="text" value="${status.value}" <c:if test="${query.editMode}">readonly="readonly"</c:if>/>
                                                    <span class="message warning">
                                                        <c:if test="${status.error}">${status.errorMessage}</c:if>
                                                    </span>
                                                </label>
                                            </spring:bind>

                                            <spring:bind path="query.query.description">
                                                <label class="control textArea <c:if test="${status.error}">error</c:if>" for="${status.expression}">
                                                    <span class="wrap"><spring:message code="label.description"/>:</span>
                                                    <textarea id="query.description" name="${status.expression}" type="text">${status.value}</textarea>
                                                    <span class="message warning">
                                                        <c:if test="${status.error}">${status.errorMessage}</c:if>
                                                    </span>
                                                </label>
                                            </spring:bind>
                                        </fieldset>
                                    </t:putAttribute>
                                </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="previous" type="submit" class="button action up" disabled="disabled"><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
                            <button id="next" type="submit" class="button action up" name="_eventId_next"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" type="submit" class="button primary action up" name="_eventId_next" disabled="disabled"><span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span></button>
                            <button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>


    </t:putAttribute>
</t:insertTemplate>
