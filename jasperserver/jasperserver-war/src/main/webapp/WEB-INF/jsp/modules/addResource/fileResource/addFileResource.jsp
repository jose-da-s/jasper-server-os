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
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="openWithJrws" value="${openInEditor &&
    (fileResource.fileResource.fileType=='jrxml' || fileResource.fileResource.fileType=='xml' ||
    fileResource.fileResource.fileType=='dataAdapter' || fileResource.fileResource.fileType=='jrtx' ||
    fileResource.fileResource.fileType=='json' || fileResource.fileResource.fileType=='css' ||
    fileResource.fileResource.fileType=='html' || fileResource.fileResource.fileType=='properties' ||
    fileResource.fileResource.fileType=='csv' || fileResource.fileResource.fileType=='txt' ||
    fileResource.fileResource.fileType=='prop' ||
    expectedFileType=='jrxml' || expectedFileType=='xml' || expectedFileType=='dataAdapter' ||
    expectedFileType=='jrtx' || expectedFileType=='json' || expectedFileType=='css' ||
    expectedFileType=='html' || expectedFileType=='properties' || expectedFileType=='csv' ||
    expectedFileType=='txt' || expectedFileType=='prop')}"/>

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle"><c:if test='${not fileResource.editMode}'><spring:message
            code='resource.file.pageTitle'/></c:if><c:if test='${fileResource.editMode}'><spring:message
            code='resource.file.pageTitleEdit'/></c:if></t:putAttribute>
    <t:putAttribute name="bodyID" value="addResourceFile"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard oneStep"/>
    <c:choose>
        <c:when test="${openWithJrws}">
            <t:putAttribute name="moduleName" value="jrws/jrsResourcesMain"/>
        </c:when>
        <c:otherwise>
            <t:putAttribute name="moduleName" value="addResource/fileResource/addFileResourceMain"/>
        </c:otherwise>
    </c:choose>
    <t:putAttribute name="headerContent">
        <jsp:include page="addFileResourceState.jsp"/>
    </t:putAttribute>

    <t:putAttribute name="bodyContent">
        <c:choose>
            <c:when test="${openWithJrws}">
                <div id="root" style="display: flex; width: 100%; height: 100%; position: absolute;" path=<js:out>"${fileResource.fileResource.parentFolder}/${fileResource.fileResource.name}"</js:out> editMode=<js:out>"${fileResource.editMode}"</js:out>
                     <c:if test='${not fileResource.editMode}'>type=<js:out>"${expectedFileType}"</js:out> template=<js:out>"${not empty defaultTemplateUri ? defaultTemplateUri : templateProperties.defaultTemplateUri}"</js:out>
                </c:if>
                     <c:if test='${fileResource.editMode}'>type=<js:out>"${fileResource.fileResource.fileType}"</js:out></c:if>
                >
                </div>
                <form method="post" enctype="multipart/form-data" id="addFileResourceForm">
                    <input type="hidden" name="_eventId_cancel" id="cancel" />
                    <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
                </form>
                <script src="./jrws/main.js"></script>
            </c:when>
            <c:otherwise>
                <form method="post" enctype="multipart/form-data" id="addFileResourceForm">
                    <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                        <t:putAttribute name="containerClass" value="column decorated primary"/>
                        <t:putAttribute name="containerTitle"><c:if test="${not fileResource.editMode}"><spring:message
                                code="resource.file.add"/></c:if><c:if test="${fileResource.editMode}"><spring:message
                                code="resource.file.edit"/></c:if></t:putAttribute>

                        <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                        <t:putAttribute name="bodyContent">
                            <div id="flowControls"></div>
                            <div id="stepDisplay">
                                <fieldset class="row instructions">
                                    <legend class="offLeft"><span><spring:message code="resource.file.instructions"/></span>
                                    </legend>
                                    <h2 class="textAccent02"><c:if test="${not fileResource.editMode}"><spring:message
                                            code="resource.file.local"/></c:if><c:if test="${fileResource.editMode}"><spring:message
                                            code="resource.file.localEdit"/></c:if></h2>
                                    <h4><spring:message code="resource.file.uploadInstruction"/></h4>
                                </fieldset>

                                <fieldset class="row inputs oneColumn">
                                    <legend class="offLeft"><span><spring:message code="resource.file.label1"/></span></legend>

                                    <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                        <t:putAttribute name="containerClass" value="column primary"/>
                                        <t:putAttribute name="containerTitle"><spring:message
                                                code="resource.file.type"/></t:putAttribute>
                                        <t:putAttribute name="headerContent">
                                            <label id="fileTypeLabel" class="control inline select" for="fileType">
                                                <span class="wrap offLeft"><spring:message code="dialog.file.type"/>:</span>
                                                <spring:bind path="fileResource.fileResource.fileType">
                                                    <select name="${status.expression}" class="select"
                                                            id="fileType"
                                                            <c:if test='${fileResource.editMode}'>disabled="disabled"</c:if>>
                                                        <c:forEach items="${allTypes}" var="type">
                                                            <option value="${type.key}"
                                                                    <c:if test='${type.key==expectedFileType}'>selected="true"</c:if>>${type.value}</option>
                                                        </c:forEach>
                                                    </select>
                                                    <span class="message warning"><spring:message
                                                            code="resource.file.extension"/></span>
                                                </spring:bind>
                                            </label>
                                        </t:putAttribute>

                                        <t:putAttribute name="bodyClass" value="oneColumn"/>
                                        <t:putAttribute name="bodyContent">
                                            <fieldset class="group">
                                                <legend class="offLeft">
                                                    <span><spring:message code="dialog.file.locationAndType"/></span>
                                                    <span class="icon"></span>
                                                </legend>
                                                <spring:bind path="fileResource.newData">
                                                <label id="fileUpload"
                                                       class="control input file <c:if test="${status.error}"> error </c:if>"
                                                       for="filePath"
                                                       title="<spring:message code="resource.file.uploadTitle"/>">
                                                    <span class="wrap"><spring:message
                                                            code="dialog.file.path"/> (<spring:message
                                                            code='required.field'/>):</span>

                                                    <input id="filePath" type="file" name="${status.expression}"
                                                           value="${status.value}"/>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                    </spring:bind>
                                                </label>
                                            </fieldset>
                                            <fieldset class="group">
                                                <legend class="offLeft">
                                                    <span><spring:message code="dialog.file.nameAndDescription"/></span>
                                                    <span class="icon"></span>
                                                </legend>
                                                <spring:bind path="fileResource.fileResource.label">
                                                    <label class="control input text <c:if test="${status.error}"> error </c:if>"
                                                           class="required" for="addFileInputResourceLabelID"
                                                           title="<spring:message code="resource.file.label"/>">
                                                    <span class="wrap"><spring:message
                                                            code="dialog.file.name"/> (<spring:message
                                                            code='required.field'/>):</span>
                                                        <input id="addFileInputResourceLabelID" class="" type="text"
                                                               value="${status.value}"
                                                               name="${status.expression}"/>
                                                        <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                    </label>
                                                </spring:bind>
                                                <spring:bind path="fileResource.fileResource.name">
                                                <label class="control input text <c:if test="${status.error}"> error </c:if>"
                                                       for="addFileInputResourceID"
                                                       title="<spring:message code="resource.file.name"/>">
                                                    <span class="wrap"><spring:message code="dialog.file.id"/>
                                                        <c:choose>
                                                            <c:when test="${fileResource.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                            <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                        </c:choose>
                                                    </span>

                                                    <input class="" id="addFileInputResourceID" type="text"
                                                           value="${status.value}" name="${status.expression}"
                                                           <c:if test="${fileResource.editMode}">tabindex="-1" readonly="readonly"</c:if>/>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                    </spring:bind>
                                                </label>
                                                <spring:bind path="fileResource.fileResource.description">
                                                <label class="control textArea <c:if test="${status.error}"> error </c:if>"
                                                       for="addFileInputDescription">
                                                    <span class="wrap"><spring:message code="dialog.file.description"/>:</span>
                                                    <textarea
                                                            <c:if test="${fileResource.subflowMode && (fileResource.fileResource.fileType=='olapMondrianSchema' || fileResource.fileResource.fileType=='accessGrantSchema') && fileResource.source=='CONTENT_REPOSITORY'}">disabled</c:if>
                                                            name="${status.expression}"
                                                            type="text" id="addFileInputDescription">${status.value}</textarea>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                    </spring:bind>
                                                </label>
                                            </fieldset>
                                            <fieldset class="group">
                                                <spring:bind path="fileResource.fileResource.parentFolder">
                                                    <label class="control browser<c:if test="${status.error}"> error</c:if>" for="folderUri" title="">
                                                        <span class="wrap"><spring:message code="dialog.file.destination"/>:</span>
                                                        <input id="folderUri" type="text" name="${status.expression}" value="${status.value}" <c:if test="${fileResource.editMode}">disabled="disabled"</c:if>/>
                                                        <button id="browser_button" type="button" type="button" class="button action up" <c:if test="${fileResource.editMode}">disabled="disabled"</c:if>><span class="wrap"><spring:message code="button.browse"/><span class="icon"></span></span></button>
                                                        <c:if test="${status.error}">
                                                            <span class="message warning">${status.errorMessage}</span>
                                                        </c:if>
                                                    </label>
                                                </spring:bind>
                                            </fieldset>

                                        </t:putAttribute>
                                    </t:insertTemplate>
                                </fieldset>
                            </div>

                            <t:putAttribute name="footerContent">
                                <fieldset id="wizardNav">
                                    <button id="previous" type="submit" class="button action up">
                                        <span><spring:message code='button.previous'/></span><span class="icon"></span>
                                    </button>
                                    <button id="next" type="submit" class="button action up">
                                        <span><spring:message code='button.next'/></span>
                                        <span class="icon"></span></button>
                                    <button id="save" type="submit" class="button action primary up"
                                            name="_eventId_save">
                                        <span class="wrap"><spring:message code='button.submit'/></span>
                                        <span class="icon"></span>
                                    </button>
                                    <button id="cancel" type="submit" class="button action up" name="_eventId_cancel">
                                        <span class="wrap"><spring:message code='button.cancel'/></span>
                                        <span class="icon"></span>
                                    </button>
                                </fieldset>
                            </t:putAttribute>
                        </t:putAttribute>
                    </t:insertTemplate>
                    <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
                </form>
            </c:otherwise>
        </c:choose>
        <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="addFileTreeRepoLocation"> </ul>
            </t:putAttribute>
        </t:insertTemplate>

        <div id="ajaxbuffer" class="hidden" ></div>

    </t:putAttribute>
</t:insertTemplate>
