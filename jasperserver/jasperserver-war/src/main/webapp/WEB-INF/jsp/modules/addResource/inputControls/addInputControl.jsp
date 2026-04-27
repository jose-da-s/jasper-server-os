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
         <c:choose >
             <c:when test="${isEdit}">
                <spring:message code="editinputcontrol.page.title"/>
             </c:when>
             <c:otherwise>
                 <spring:message code="addinputcontrol.page.title"/>
             </c:otherwise>
         </c:choose>
     </t:putAttribute>

    <t:putAttribute name="bodyID" value="addResource_inputControl"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard firstStep"/>
    <t:putAttribute name="moduleName" value="addResource/inputControls/addInputControlMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="addInputControlState.jsp"/>
    </t:putAttribute>

    <t:putAttribute name="bodyContent">

        <form method="post" action="flow.html">
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="column decorated primary"/>
		    <t:putAttribute name="containerTitle">
                <c:choose>
                    <c:when test="${isEdit}">
                        <spring:message code="editResources.editInputControle.title" javaScriptEscape="true"/>
                    </c:when>
                    <c:otherwise>
                        <spring:message code="addResources.addInputControle.title" javaScriptEscape="true"/>
                    </c:otherwise>
                </c:choose>
            </t:putAttribute>

            <t:putAttribute name="swipeScroll" value="${isIPad}"/>

            <t:putAttribute name="bodyContent">
                <div id="flowControls"></div>
                <div id="stepDisplay">
                    <fieldset class="row instructions">
                        <legend class="offLeft"><span><spring:message code="addResources.addInputControle.instructions" javaScriptEscape="true"/></span></legend>
                        <c:choose>
                            <c:when test="${isEdit}">
                                <h2 class="textAccent02">
                                    <spring:message code="editResources.editInputControle.createInputControl" javaScriptEscape="true"/></h2>
                                <h4><spring:message code="editResources.editInputControle.textaccent2" javaScriptEscape="true"/></h4>
                            </c:when>
                            <c:otherwise>
                                <h2 class="textAccent02">
                                    <spring:message code="addResources.addInputControle.createInputControl" javaScriptEscape="true"/></h2>
                                <h4><spring:message code="addResources.addInputControle.textaccent2" javaScriptEscape="true"/></h4>
                            </c:otherwise>
                        </c:choose>
                    </fieldset>

                    <fieldset class="row inputs oneColumn">
                        <legend class="offLeft"><span><spring:message code="addResources.addInputControle.userInputs" javaScriptEscape="true"/></span></legend>

                        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                            <t:putAttribute name="containerClass" value="column primary"/>
                            <t:putAttribute name="containerTitle"><spring:message code="addResources.addInputControle.conteinerTitle" javaScriptEscape="true"/></t:putAttribute>
                            <t:putAttribute name="headerContent">

                                <spring:bind path="control.inputControl.inputControlType">
                                    <label class="control select inline<c:if test="${status.error}"> error</c:if>" for="dataTypeKind" title="<spring:message code='resource.dataType.dataTypeKind'/>">
                                        <span class="wrap offLeft"><spring:message code="addResources.addInputControle.inputControleType" javaScriptEscape="true"/></span>
                                        <select id="dataTypeKind" name="${status.expression}">
                                            <c:forEach items='${inputControlTypes}' var='type'>
                                                <option value="${type.key}"
                                                        <c:if test='${type.key==status.value}'>selected="true"</c:if>>
                                                    <spring:message code="${type.value}"/></option>
                                            </c:forEach>
                                        </select>
                                        <c:if test="${status.error}">
                                            <span class="message warning">${status.errorMessage}</span>
                                        </c:if>
                                    </label>
                                </spring:bind>

                            </t:putAttribute>
                            <t:putAttribute name="bodyContent">
                                <fieldset class="group">
                                    <legend class="offLeft"><span><spring:message code="addResources.addInputControle.nameAndDescriprion" javaScriptEscape="true"/> </span></legend>

                                    <spring:bind path="control.inputControl.label">
                                        <label class="control input text<c:if test="${status.error}"> error</c:if>" class="required" for="label"
                                               title="<spring:message code="addResources.addInputControl.name.hint" javaScriptEscape="true"/>">
                                            <span class="wrap"><spring:message code='addResources.addInputControle.promtText' javaScriptEscape="true"/> (<spring:message
                                                    code='required.field'/>):</span>
                                            <input class="" id="label" type="text" name="${status.expression}" value="${status.value}"/>
                                            <span class="hint"><spring:message code="addResources.addInputControl.name.hint" javaScriptEscape="true"/></span>
                                            <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                        </label>
                                    </spring:bind>

                                    <spring:bind path="control.inputControl.name">
                                        <label class="control input text<c:if test="${status.error}"> error</c:if>" class="required" for="name"
                                               title="<spring:message code="addResources.addInputControl.resourceId.hint" javaScriptEscape="true"/>">
                                            <span class="wrap"><spring:message code="addResources.addInputControle.parameterName" javaScriptEscape="true"/>
                                                <c:choose>
                                                    <c:when test="${control.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                    <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <input class="" id="name" type="text" name="${status.expression}" value="${status.value}"
                                                    <c:if test="${control.editMode}">readonly="readonly"</c:if>/>
                                            <span class="hint"><spring:message code="addResources.addInputControl.resourceId.hint" javaScriptEscape="true"/></span>
                                            <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                        </label>
                                    </spring:bind>

                                    <spring:bind path="control.inputControl.description">
                                        <label class="control textArea<c:if test="${status.error}"> error</c:if>" for="description">
                                            <span class="wrap"><spring:message code='addResources.addInputControle.descrioption' javaScriptEscape="true"/></span>
                                            <textarea id="description" type="text" name="${status.expression}" >${status.value}</textarea>
                                           <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                        </label>
                                    </spring:bind>

                                </fieldset>
                                <fieldset  class="group">
                                    <legend class="offLeft"><span><spring:message code='addResources.addInputControle.options' javaScriptEscape="true"/></span></legend>
                                    <ul class="list inputSet">
                                        <li class="leaf">
                                            <spring:bind path="control.inputControl.mandatory">
                                                <div class="control checkBox" title="<spring:message code='addResources.addInputControle.mandatory' javaScriptEscape="true"/>">
                                                    <label for="mandatory" class="wrap"><spring:message code='addResources.addInputControle.mandatory' javaScriptEscape="true"/></label>
                                                    <input name="_${status.expression}" type="hidden"/>
                                                    <input id="mandatory" name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if>/>
                                                </div>
                                            </spring:bind>
                                        </li>
                                        <li class="leaf">
                                            <spring:bind path="control.inputControl.readOnly">
                                                <div class="control checkBox" title="<spring:message code='addResources.addInputControle.readOnly' javaScriptEscape="true"/>">
                                                    <label  for="readOnly" class="wrap"><spring:message code='addResources.addInputControle.readOnly' javaScriptEscape="true"/></label>
                                                    <input name="_${status.expression}" type="hidden"/>
                                                    <input id="readOnly" name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if>/>
                                                </div>
                                            </spring:bind>
                                        </li>
                                        <li class="leaf">
                                            <spring:bind path="control.inputControl.visible">
                                                <div class="control checkBox"  title="<spring:message code='addResources.addInputControle.visible' javaScriptEscape="true"/>">
                                                    <label for="visible" class="wrap"><spring:message code='addResources.addInputControle.visible' javaScriptEscape="true"/></label>
                                                    <input name="_${status.expression}" type="hidden"/>
                                                    <input id="visible" name="${status.expression}" type="checkbox" <c:if test='${status.value}'>checked="true"</c:if>/><!--NOTE: default is checked -->
                                                </div>
                                            </spring:bind>
                                        </li>
                                    </ul>
                                </fieldset>
                            </t:putAttribute>
                            </t:insertTemplate>
                                               </fieldset></div>
                            <t:putAttribute name="footerContent">
                                <fieldset id="wizardNav">
                                    <button id="previous" type="submit" class="button action up" disabled="disabled"><span class="wrap"><spring:message
                                            code='button.previous'/></span><span class="icon"></span></button>
                                    <button id="next" type="submit" class="button action up" name="_eventId_Next"><span class="wrap"><spring:message
                                            code='button.next'/></span><span class="icon"></span></button>
                                    <button id="nextAndSubmit" type="submit" class="button primary action up hidden" name="_eventId_Next"><span class="wrap"><spring:message
                                            code='button.submit'/></span><span class="icon"></span></button>
                                    <button id="submit" type="submit" class="button primary action up" disabled="disabled" name="_eventId_save"><span
                                            class="wrap"><spring:message code='button.submit'/></span><span
                                            class="icon"></span></button>
                                    <button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span class="wrap"><spring:message
                                            code='button.cancel'/></span><span class="icon"></span></button>
                                </fieldset>
                            </t:putAttribute>
            </t:putAttribute>
                        </t:insertTemplate>

                    <div id="ajaxbuffer" style="display:none"></div>
                    <input type="hidden" id="_flowExecutionKey" name="_flowExecutionKey" value="${flowExecutionKey}"/>

                </form>
                <!--/#stepDisplay-->

            </t:putAttribute>
        </t:insertTemplate>
