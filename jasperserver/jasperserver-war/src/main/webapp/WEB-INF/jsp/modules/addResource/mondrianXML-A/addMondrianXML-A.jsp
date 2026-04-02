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
            <c:when test="${mondrianXmlaSource.aloneEditMode}"><spring:message code="resource.mondrianxmla.page.title.edit"/></c:when>
            <c:otherwise><spring:message code="resource.mondrianxmla.page.title.add"/></c:otherwise>
         </c:choose>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_mondrianSource"/>
    <t:putAttribute name="bodyClass" value="oneColumn mondrian flow wizard oneStep"/>
    <t:putAttribute name="moduleName" value="addResource/mondrianXml/addMondrianXmlMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="addMondrianXML-AState.jsp"/>
    </t:putAttribute>

    <t:putAttribute name="bodyContent">
        <form method="post">
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="column decorated primary"/>

             <t:putAttribute name="containerTitle">
                <c:choose>
                    <c:when test="${mondrianXmlaSource.aloneEditMode}"><spring:message code='resource.mondrianxmla.title.edit'/></c:when>
                    <c:otherwise><spring:message code='resource.mondrianxmla.title.add'/></c:otherwise>
                </c:choose>
            </t:putAttribute>

            <t:putAttribute name="columnTitle"><spring:message code="jsp.editMondrianXmlaSourceForm.title" /></t:putAttribute>

            <t:putAttribute name="swipeScroll" value="${isIPad}"/>

            <t:putAttribute name="bodyContent">
                <div id="flowControls"></div>
                <div id="stepDisplay">
                    <fieldset class="row instructions">
                        <legend class="offLeft"><span><spring:message code="jsp.editMondrianXmlaSourceForm.instructions" javaScriptEscape="true"/></span></legend>
                        <h2 class="textAccent02"><spring:message code="jsp.editMondrianXmlaSourceForm.instructions1" javaScriptEscape="true"/></h2>
                        <h4></h4>
                    </fieldset>

                    <fieldset class="row inputs oneColumn">
                        <legend class="offLeft"><span><spring:message code="jsp.editMondrianXmlaSourceForm.inputs" javaScriptEscape="true"/></span></legend>

                        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                            <t:putAttribute name="containerClass" value="column noHeader primary"/>

                            <t:putAttribute name="bodyContent">
                                <fieldset>

                                    <legend class="offLeft"><span><spring:message code="jsp.editMondrianXmlaSourceForm.nameAndDescription" javaScriptEscape="true"/></span></legend>

                                    <spring:bind path="mondrianXmlaSource.mondrianXmlaDefinition.label">
                                        <label class="control input text <c:if test="${status.error}"> error </c:if>"
                                               class="required" for="label"
                                               title="<spring:message code="jsp.editMondrianXmlaSourceForm.labelTitle" javaScriptEscape="true"/>">
                                            <span class="wrap"><spring:message code="jsp.editMondrianXmlaSourceForm.label" javaScriptEscape="true"/>(<spring:message code='required.field'/>):</span>
                                            <input class="" id="label" type="text" name="${status.expression}"
                                                   value="${status.value}"/>
                                            <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                        </label>
                                    </spring:bind>

                                    <spring:bind path="mondrianXmlaSource.mondrianXmlaDefinition.name">
                                        <label class="control input text <c:if test="${status.error}"> error </c:if>"
                                               for="resourceID"
                                               title="<spring:message code="jsp.userManager.userCreator.userId.title" javaScriptEscape="true"/>">
                                            <span class="wrap"><spring:message code="jsp.editMondrianXmlaSourceForm.name" javaScriptEscape="true"/>
                                                <c:choose>
                                                    <c:when test="${mondrianXmlaSource.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                    <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <input id="resourceID" maxlength="100" type="text"
                                                   name="${status.expression}" value="${status.value}"
                                                   <c:if test="${mondrianXmlaSource.editMode}">tabindex="-1" readonly="readonly"</c:if>/>
                                            <span class="hint"><spring:message
                                                    code="jsp.userManager.userCreator.userId.hint"
                                                    javaScriptEscape="true"/></span>
                                            <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                        </label>
                                    </spring:bind>

                                    <spring:bind path="mondrianXmlaSource.mondrianXmlaDefinition.description">
                                        <label class="control textArea <c:if test="${status.error}"> error </c:if>"
                                               for="description">
                                            <span class="wrap"><spring:message code="jsp.editMondrianXmlaSourceForm.description" javaScriptEscape="true"/>:</span>
                                            <textarea id="description" type="text" name="${status.expression}">${status.value}</textarea>
                                            <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                        </label>
                                    </spring:bind>

                                    <spring:bind path="mondrianXmlaSource.mondrianXmlaDefinition.catalog">
                                        <label class="control input text <c:if test="${status.error}"> error </c:if>"
                                               class="required" for="catalog"
                                               title="<spring:message code="jsp.editMondrianXmlaSourceForm.catalog.title"/>">
                                            <span class="wrap"><spring:message code="jsp.editMondrianXmlaSourceForm.catalog" javaScriptEscape="true"/>(<spring:message code='required.field'/>):</span>
                                            <input class="catalog" id="catalog" type="text" name="${status.expression}"
                                                   value="${status.value}"/>
                                            <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            <span class="message hint"><spring:message code="jsp.editMondrianXmlaSourceForm.hint" javaScriptEscape="true"/></span>
                                        </label>
                                    </spring:bind>

                                    <spring:bind path="mondrianXmlaSource.connectionUri">
                                        <label class="control browser<c:if test="${status.error}"> error</c:if>" for="mondrianConnectionReference" title="<spring:message code="jsp.editMondrianXmlaSourceForm.connection_reference_title"/>">
                                            <span class="wrap"><spring:message code="jsp.editMondrianXmlaSourceForm.connection_reference"/></span>
                                            <input id="mondrianConnectionReference" type="text" name="${status.expression}" value="${status.value}" />
                                            <button type="button" id="browser_button" class="button action up"><span class="wrap"><spring:message code="button.browse"/><span class="icon"></span></span></button>
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
                                    <button id="previous" type="submit" class="button action up"><span class="wrap"><spring:message
                                            code='button.previous'/></span><span class="icon"></span></button>
                                    <button id="next" type="submit" class="button action up"><span class="wrap"><spring:message
                                            code='button.next'/></span><span class="icon"></span></button>
                                    <button id="save" type="submit" class="button primary action up" name="_eventId_save"><span
                                            class="wrap"><spring:message code='button.submit'/></span><span
                                            class="icon"></span></button>
                                    <button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span class="wrap"><spring:message
                                            code='button.cancel'/></span><span class="icon"></span></button>
                                </fieldset>

                            </t:putAttribute>
            </t:putAttribute>
                        </t:insertTemplate>

                    <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
                    <div id="ajaxbuffer" style="display:none"></div>
                </form>

                <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
                    <t:putAttribute name="containerClass">hidden</t:putAttribute>
                    <t:putAttribute name="bodyContent">
                        <ul id="resourceTreeRepoLocation"></ul>
                    </t:putAttribute>
                </t:insertTemplate>

            </t:putAttribute>
        </t:insertTemplate>
