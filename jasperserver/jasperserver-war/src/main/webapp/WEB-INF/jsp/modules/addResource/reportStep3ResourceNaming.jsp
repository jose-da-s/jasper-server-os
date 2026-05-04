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
    <c:set var="editMode" value='${param.isEdit}'/>
    <t:putAttribute name="pageTitle"><c:choose><c:when test="${masterFlow=='olapClientConnection'}"><spring:message code="addResources.schemaDetails"/></c:when><c:when test="${masterFlow!='reportUnit'}"><spring:message code="addResources.accessGrantDefinition.details"/></c:when><c:when test="${masterFlow=='reportUnit' and fileResource.parentFlowObject.editMode}"><spring:message code="resource.report.titleEdit"/></c:when><c:otherwise><spring:message code="resource.report.title"/></c:otherwise></c:choose></t:putAttribute>
    <t:putAttribute name="bodyID" value="addResourceFile"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard ${lastSubflow?'lastStep':''}"/>
    <t:putAttribute name="moduleName" value="addResource/jasperReport/addJasperReportResourceNamingMain"/>
    <t:putAttribute name="headerContent">
        <jsp:include page="reportStep3ResourceNamingState.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <form action="flow.html" method="post">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <c:set var="isSchema"  value="${fileResource.fileResource.fileType=='olapMondrianSchema' || fileResource.fileResource.fileType=='accessGrantSchema'}"/>
                <c:set var="editMode" value='${param.isEdit}'/>
                <t:putAttribute name="containerTitle">
                    <c:choose>
                        <c:when test="${masterFlow=='olapClientConnection'}"><spring:message code="addResources.schemaDetails"/></c:when>
                        <c:when test="${masterFlow!='reportUnit'}"><spring:message code="addResources.accessGrantDefinition.details"/></c:when>
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
                    <div id="flowControls"></div>
                    <div id="stepDisplay">
                        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
                        <input type="hidden" id="editMode" value="${fileResource.editMode}">
                        <input type="hidden" id="ParentFolderUri" value='${param.ParentFolderUri}'>

                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.report.instructions"/></span></legend>
                            <h2 class="textAccent02"><c:choose><c:when test="${masterFlow=='olapClientConnection'}"><spring:message code="jsp.schemaResource"/></c:when><c:when test="${masterFlow!='reportUnit'}"><spring:message code="jsp.accessGrantResource"/></c:when><c:otherwise><spring:message code="resource.report.addReportResource"/></c:otherwise></c:choose></h2>
                            <h4><spring:message code="resource.report.setProperties"/></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.report.inputs"/></span></legend>

                                <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                    <t:putAttribute name="containerClass" value="column decorated primary"/>
                                    <t:putAttribute name="containerTitle"><spring:message code="resource.report.type"/>:</t:putAttribute>
                                    <t:putAttribute name="headerContent">
                                        <spring:bind path="fileResource.fileResource.fileType">
                                            <span>
                                                <c:choose>
                                                    <c:when test="${fileResource.fileResource.fileType!=null}">${allTypes[fileResource.fileResource.fileType]}</c:when>
                                                    <c:otherwise>
                                                        <label class="control select inline" for="${status.expression}" title="<spring:message code='resource.report.type'/>">
                                                            <select id="${status.expression}" name="${status.expression}">
                                                                <c:forEach items="${allTypes}" var="type">
                                                                    <option value="${type.key}">${type.value}</option>
                                                                </c:forEach>
                                                            </select>
                                                        </label>
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </spring:bind>
                                    </t:putAttribute>

                                    <t:putAttribute name="bodyClass" value="oneColumn"/>
                                    <t:putAttribute name="bodyContent">
                                        <fieldset class="group">
                                            <legend class="offLeft"><span><spring:message code="dialog.file.locationAndType"/></span><span class="icon"></span></legend>
                                            <label class="control input text" for="resourcePath" title="<c:choose><c:when test="${masterFlow!='reportUnit'}"></c:when><c:otherwise><spring:message code="resource.report.resourceTitle"/></c:otherwise></c:choose>">
                                                <span class="wrap"><spring:message code="resource.report.selectedResource"/>:</span>
                                                <input class="" id="resourcePath" type="text" value="<c:choose><c:when test='${uploadedFileName!=null || uploadedFileExt!=null}'>${uploadedFileName}.${uploadedFileExt}</c:when><c:otherwise>${fileResource.fileResource.referenceURI}</c:otherwise></c:choose>" readonly="readonly"/><!-- if user selected resource from local system, then display local path for confirmation-->
                                            </label>
                                        </fieldset>
                                        <fieldset class="group">
                                            <legend class="offLeft"><span><spring:message code="dialog.file.nameAndDescription"/></span><span class="icon"></span></legend>

                                            <spring:bind path="fileResource.fileResource.label">
                                                <label class="control input text <c:if test="${status.error}">error</c:if>" class="required" for="label" title="<spring:message code="resource.report.visibleName"/>">
                                                    <span class="wrap"><spring:message code="dialog.file.name"/> (<spring:message code='required.field'/>):</span>
                                                    <input <c:if test="${fileResource.subflowMode && isSchema && fileResource.source=='CONTENT_REPOSITORY'}">disabled</c:if> class="" id="label" name="${status.expression}" type="text" value="${status.value}"/>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                </label>
                                            </spring:bind>

                                            <spring:bind path="fileResource.fileResource.name">
                                                <label class="control input text <c:if test="${status.error}">error</c:if>" for="resourceID" title="<spring:message code="resource.report.permanentID"/>">
                                                    <span class="wrap"><spring:message code="dialog.file.id"/>
                                                        <c:choose>
                                                            <c:when test="${fileResource.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                            <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <input class="" id="resourceID" name="${status.expression}" type="text" value="${status.value}"
                                                            <%--<c:if test="${fileResource.editMode}">readonly="readonly"</c:if>--%>

                                                            <c:if test="${fileResource.editMode || (fileResource.suggested && fileResource.subflowMode)}">readonly="readonly"</c:if>
                                                            <c:if test="${fileResource.subflowMode && isSchema && fileResource.source=='CONTENT_REPOSITORY'}">disabled="disabled"</c:if>/>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                </label>
                                            </spring:bind>

                                            <spring:bind path="fileResource.fileResource.description">
                                                <label class="control textArea <c:if test="${status.error}">error</c:if>" for="${status.expression}">
                                                    <span class="wrap"><spring:message code="dialog.file.description"/>:</span>
                                                    <textarea <c:if test="${fileResource.subflowMode && isSchema && fileResource.source=='CONTENT_REPOSITORY'}">disabled</c:if> name="${status.expression}" id="${status.expression}" type="text">${status.value}</textarea>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                </label>
                                            </spring:bind>
                                        </fieldset>

                                        <c:if test="${fileResource.subflowMode && (fileResource.fileResource.fileType == 'olapMondrianSchema' || fileResource.fileResource.fileType == 'accessGrantSchema')}">
                                            <spring:bind path="fileResource.fileResource.parentFolder">
                                                <fieldset class="group">
                                                    <label title="<spring:message code='dialog.file.selectSaveLocation'/>" for="folderUri" class="control browser<c:if test="${status.error}"> error</c:if>">
                                                        <span class="wrap"><spring:message code="dialog.file.destination"/>:</span>
                                                        <input id="folderUri" type="text" name="${status.expression}" value="${status.value}" <c:if test="${editMode or fileResource.source=='CONTENT_REPOSITORY'}">disabled="disabled"</c:if> />
                                                        <button id="browser_button" type="button" class="button action up" <c:if test="${editMode or fileResource.source=='CONTENT_REPOSITORY'}">disabled="disabled"</c:if>><span class="wrap"><spring:message code="button.browse"/><span class="icon"></span></span></button>
                                                        <c:if test="${status.error}">
                                                            <span class="message warning">${status.errorMessage}</span>
                                                        </c:if>
                                                    </label>
                                                </fieldset>
                                            </spring:bind>
                                        </c:if>
                                    </t:putAttribute>
                                </t:insertTemplate>
                        </fieldset>
                    </div>
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav">
                            <button id="previous" type="submit" name="_eventId_Back" class="button action up"><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
                            <button id="next" type="submit" name="_eventId_Next" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" type="submit" name="_eventId_Save" class="button primary action up"><span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span></button>
                            <button id="cancel" type="submit" name="_eventId_Cancel" class="button action up"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
        <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="addFileTreeRepoLocation"> </ul>
            </t:putAttribute>
        </t:insertTemplate>

        <div id="ajaxbuffer" class="hidden" ></div>
    </t:putAttribute>
</t:insertTemplate>
