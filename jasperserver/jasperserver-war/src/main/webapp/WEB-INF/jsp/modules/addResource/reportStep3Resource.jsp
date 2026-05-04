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
    <c:set var="editMode" value='${param.isEdit}'/>
    <t:putAttribute name="pageTitle"><c:choose><c:when test="${masterFlow=='olapClientConnection' and editMode=='edit'}"><spring:message code="addResources.schemaResource.edit"/></c:when><c:when test="${masterFlow=='olapClientConnection'}"><spring:message code="addResources.schemaResource.add"/></c:when><c:when test="${masterFlow!='reportUnit'}"><spring:message code="addResources.accessGrantResource.add"/></c:when><c:when test="${masterFlow!='reportUnit' and editMode=='edit'}"><spring:message code="addResources.schemaResource.edit"/></c:when><c:when test="${masterFlow=='reportUnit' and fileResource.parentFlowObject.editMode}"><spring:message code="resource.report.titleEdit"/></c:when><c:otherwise><spring:message code="resource.report.title"/></c:otherwise></c:choose></t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_locateResource"/>
    <!-- NOTE:

    -->
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard"/>
    <t:putAttribute name="moduleName" value="addResource/jasperReport/addJasperReportMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="reportStep3ResourceState.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <form action="flow.html" method="post" enctype="multipart/form-data">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <c:set var="editMode" value='${param.isEdit}'/>
                <t:putAttribute name="containerTitle">
                    <c:choose>
                        <c:when test="${masterFlow!='reportUnit' and editMode=='edit'}"><spring:message code="addResources.schemaResource.edit"/></c:when>
                        <c:when test="${masterFlow=='olapClientConnection' and editMode=='edit'}"><spring:message code="addResources.schemaResource.edit"/></c:when>
                        <c:when test="${masterFlow=='olapClientConnection' and editMode=='edit'}"><spring:message code="addResources.schemaResource.edit"/></c:when>
                        <c:when test="${masterFlow=='olapClientConnection'}"><spring:message code="addResources.schemaResource.add"/></c:when>
                        <c:when test="${masterFlow!='reportUnit'}"><spring:message code="addResources.accessGrantResource.add"/></c:when>
                        <c:when test="${masterFlow=='reportUnit'}">
                            <c:choose>
                                <c:when test="${fileResource.parentFlowObject.editMode}"><spring:message code="resource.report.titleEdit"/>:</c:when>
                                <c:otherwise><spring:message code="resource.report.title"/>:</c:otherwise>
                            </c:choose>
                            ${fileResource.parentFlowObject.reportUnit.label}
                        </c:when>
                        <c:otherwise><spring:message code="resource.report.title"/></c:otherwise>
                    </c:choose>
                </t:putAttribute>

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                <div id="flowControls">
                    <!-- NOTE: insert appropriate navigation, if any, here -->
                </div>
                    <div id="stepDisplay">
                        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>

                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.report.instructions"/></span></legend>
                            <h2 class="textAccent02"><c:choose><c:when test="${masterFlow=='olapClientConnection'}"><spring:message code="jsp.resourceUploadForm.locateSchema"/> </c:when><c:when test="${masterFlow!='reportUnit'}"><spring:message code="jsp.resourceUploadForm.locateAccessGrant"/></c:when><c:otherwise><spring:message code="resource.report.locateFileResource"/></c:otherwise></c:choose></h2>
                            <h4><!--NOTE: keep h4 markup, but leave empty --></h4>
                            <p class="warning">Error or warning here.</p>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.report.inputs"/></span></legend>

                                <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                    <t:putAttribute name="containerClass" value="column noHeader primary"/>

                                    <t:putAttribute name="bodyContent">
                                        <fieldset class="locationSelector">
                                                <legend class="offLeft"><span><spring:message code="resource.report.locateResource"/></span></legend>
                                                <ul class="list locations">
                                                    <c:if test="${(fileResource.parentFlowObject != null && fileResource.parentFlowObject.reportUnit == null)}">
                                                        <c:if test="${fileResource.parentFlowObject.accessGrant}">
                                                            <spring:bind path="fileResource.source">
                                                                <li id="noLinkZ" class="leaf">
                                                                    <div class="control radio">
                                                                        <label class="wrap" for="NONE" title="<spring:message code="resource.report.noLinkAccessGrant"/>">
                                                                            <spring:message code="resource.report.noLinkAccessGrant"/>
                                                                        </label>
                                                                        <input id="NONE" type="radio" name="${status.expression}" value="NONE" <c:if test='${status.value=="NONE"}'>checked="checked"</c:if>/>
                                                                        <%--<span class="message warning">error message here</span>--%>
                                                                    </div>
                                                                </li>
                                                            </spring:bind>
                                                        </c:if>
                                                    </c:if>

                                                    <li id="fromLocal" class="leaf">
                                                        <c:if test='${fileResource.subflowMode}'><%--Show radio button and combo box in sub mode --%>
                                                            <spring:bind path="fileResource.source">
                                                                <c:set var="isFileSystem" value="${status.value=='FILE_SYSTEM'}"/>
                                                                <div  class="control radio <c:if test="${status.error}">error</c:if>">
                                                                    <label class="wrap" for="FILE_SYSTEM" title="<spring:message code="resource.report.upload"/>">
                                                                        <spring:message code="resource.report.upload"/>
                                                                    </label>
                                                                    <input class="" id="FILE_SYSTEM" type="radio" name="${status.expression}" value="FILE_SYSTEM" <c:if test="${status.value=='FILE_SYSTEM'}">checked="checked"</c:if>/>
                                                                    <c:if test="${status.error}">
                                                                        <span class="message warning">${status.errorMessage}</span>
                                                                    </c:if>
                                                                </div>
                                                            </spring:bind>
                                                        </c:if>
                                                        <spring:bind path="fileResource.newData">
                                                            <div id="fileUpload" class="control input file <c:if test="${status.error}">error</c:if>" for="filePath" title="<spring:message code="resource.report.locateFile"/>">
                                                                <input class="" id="filePath" name="${status.expression}" type="file" value="${status.value}" <c:if test="${!isFileSystem}">disabled="disabled"</c:if>/>
                                                                <span class="message warning">${status.errorMessage}</span>
                                                            </div>
                                                        </spring:bind>
                                                    </li>
                                                    <c:if test='${fileResource.subflowMode}'><%--Show radio button and combo box in sub mode --%>
                                                        <li id="fromRepo" class="leaf">
                                                            <spring:bind path="fileResource.source">
                                                                <div class="control radio complex <c:if test="${status.error}">error</c:if>">
                                                                    <label class="wrap" for="CONTENT_REPOSITORY" title="<spring:message code="resource.report.repository"/>">
                                                                        <spring:message code="resource.report.selectResource"/>
                                                                    </label>
                                                                    <input class="" id="CONTENT_REPOSITORY" type="radio" name="${status.expression}" value="CONTENT_REPOSITORY" <c:if test="${status.value=='CONTENT_REPOSITORY'}">checked="checked"</c:if>/>
                                                                    <c:if test="${status.error}">
                                                                        <span class="message warning">${status.errorMessage}</span>
                                                                    </c:if>
                                                                </div>
                                                            </spring:bind>
                                                            <spring:bind path="fileResource.newUri">
                                                                <label class="control browser<c:if test="${status.error}"> error</c:if>" for="resourceUri">
                                                                    <input id="resourceUri" type="text" name="${status.expression}" value="${status.value}"  title="<spring:message code="resource.report.repository"/>"
                                                                           <c:if test="${empty status.value}">disabled="disabled"</c:if>/>
                                                                    <button id="browser_button" type="button" class="button action up" <c:if test="${(empty status.value) and (!status.error)}">disabled="disabled"</c:if>>
                                                                        <span class="wrap">
                                                                            <spring:message code="button.browse"/>
                                                                            <span class="icon"></span>
                                                                        </span>
                                                                    </button>
                                                                    <c:if test="${status.error}">
                                                                        <span class="message warning">${status.errorMessage}</span>
                                                                    </c:if>
                                                                </label>
                                                            </spring:bind>
                                                        </li>
                                                    </c:if>
                                                </ul>
                                            </fieldset>
                                    </t:putAttribute>
                                </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav">
                            <button id="previous" type="submit" name="_eventId_Back" class="button action up"><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
                            <button id="next" type="submit" name="_eventId_Next" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" type="submit" class="button primary action up"><span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span></button>
                            <button id="cancel" type="submit" name="_eventId_cancel" class="button action up"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
        <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="resourceTreeRepoLocation"> </ul>
            </t:putAttribute>
        </t:insertTemplate>

        <div id="ajaxbuffer" class="hidden" ></div>
    </t:putAttribute>
</t:insertTemplate>
