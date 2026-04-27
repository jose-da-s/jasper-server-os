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
             <c:when test="${wrapper.editMode}"><spring:message code="resource.analysisView.page.title.edit"/></c:when>
             <c:otherwise><spring:message code="resource.analysisView.page.title.add"/></c:otherwise>
         </c:choose>
     </t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_AnalysisViewNaming"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard firstStep"/>
    <t:putAttribute name="moduleName" value="addResource/analysisView/addOLAPViewMain"/>

    <t:putAttribute name="headerContent">
         <jsp:include page="analysisViewNamingState.jsp"/>
    </t:putAttribute>

    <t:putAttribute name="bodyContent">
        <form method="post">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="containerTitle">
                    <c:choose>
                        <c:when test="${wrapper.editMode}"><spring:message code="resource.analysisView.title.edit"/></c:when>
                        <c:otherwise><spring:message code="resource.analysisView.title.add"/></c:otherwise>
                    </c:choose>
                </t:putAttribute>

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                    <div id="flowControls"></div>
                    <div id="stepDisplay">
                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message
                                                code="jsp.editMondrianXmlaSourceForm.instructions"/></span></legend>
                            <h2 class="textAccent02"><spring:message code="resource.analysisView.nametheview"/></h2>
                            <h4><spring:message code="resource.analysisView.description"/></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message
                                                code="jsp.editMondrianXmlaSourceForm.inputs"/></span></legend>

                            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                <t:putAttribute name="containerClass" value="column primary noHeader"/>

                                <t:putAttribute name="bodyClass" value="oneColumn"/>
                                <t:putAttribute name="bodyContent">
                                    <fieldset class="group">
                                        <legend class="offLeft"><span><spring:message
                                                code="dialog.file.nameAndDescription"/></span><span class="icon"></span>
                                        </legend>

                                        <spring:bind path="wrapper.olapUnitLabel">
                                            <label class="control input text <c:if test="${status.error}"> error </c:if>" class="required"
                                                   for="labelID"
                                                   title="<spring:message code="dialog.file.name.title"/>">
                                                <span class="wrap"><spring:message
                                                        code="dialog.file.name"/> (<spring:message
                                                        code='required.field'/>):</span>
                                                <input class="" id="labelID" type="text" value="${status.value}"
                                                       name="${status.expression}"/>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>

                                        <spring:bind path="wrapper.olapUnitName">
                                            <label class="control input text <c:if test="${status.error}"> error </c:if>" for="nameID"
                                                   title="<spring:message code="dialog.file.id.title"/>">
                                                <span class="wrap"><spring:message code="dialog.file.id"/>
                                                    <c:choose>
                                                        <c:when test="${wrapper.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                        <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <input class="" id="nameID" type="text" value="${status.value}" name="${status.expression}" <c:if test="${wrapper.editMode}">readonly="readonly"</c:if>/>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>

                                        <spring:bind path="wrapper.olapUnitDescription">
                                            <label class="control textArea <c:if test="${status.error}"> error </c:if>" for="addFileInputDescription">
                                                <span class="wrap"><spring:message
                                                        code="dialog.file.description"/>:</span>
                                                <textarea id="addFileInputDescription"
                                                          type="text" name="${status.expression}">${status.value}</textarea>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>
                                    </fieldset>

                                </t:putAttribute>
                                    <div id="ajaxbuffer" style="display:none">
                            </t:insertTemplate>
                        </fieldset>
                    </div>
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav">
                            <button id="previous" type="submit" class="button action up" disabled="disabled"><span class="wrap"><spring:message
                                    code='button.previous'/></span><span class="icon"></span></button>
                            <button id="next" type="submit" class="button action up" name="_eventId_next"><span class="wrap"><spring:message
                                    code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" type="submit" class="button primary action up"><span class="wrap"><spring:message
                                    code='button.submit'/></span><span class="icon"></span></button>
                            <button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span class="wrap"><spring:message
                                    code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>

                </t:putAttribute>
            </t:insertTemplate>
          <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>

        </form>

    </t:putAttribute>

</t:insertTemplate>
