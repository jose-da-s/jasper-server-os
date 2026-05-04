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
            <c:when test="${listOfValuesDTO.editMode}"><spring:message code="resource.listofvalues.page.title.edit"/></c:when>
            <c:otherwise><spring:message code="resource.listofvalues.page.title.add"/></c:otherwise>
        </c:choose>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_listOfValues"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard oneStep"/>
    <t:putAttribute name="moduleName" value="addResource/listOfValues/addListOfValuesMain"/>


    <t:putAttribute name="headerContent">
        <jsp:include page="addListOfValuesState.jsp"/>
    </t:putAttribute>

    <t:putAttribute name="bodyContent">
        <form method="post" id="lofForm" action="flow.html">
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="column decorated primary"/>
            <t:putAttribute name="containerTitle">
                <c:choose>
                    <c:when test="${listOfValuesDTO.editMode}"><spring:message code='resource.lov.title.edit'/></c:when>
                    <c:otherwise><spring:message code='resource.lov.title.add'/></c:otherwise>
                </c:choose>
            </t:putAttribute>

            <t:putAttribute name="swipeScroll" value="${isIPad}"/>

            <t:putAttribute name="bodyContent">
                <div id="flowControls">

                </div>
                <div id="stepDisplay">
                    <fieldset class="row instructions">
                        <legend class="offLeft"><span><spring:message code='resource.lov.instructions'/></span></legend>
                        <h2 class="textAccent02">
                            <c:choose>
                                <c:when test="${listOfValuesDTO.editMode}"><spring:message code='resource.lov.title.edit'/></c:when>
                                <c:otherwise><spring:message code='resource.lov.title.add'/></c:otherwise>
                            </c:choose>
                        </h2>
                        <h4><spring:message code='resource.lov.description'/></h4>
                    </fieldset>

                    <fieldset class="row inputs oneColumn">
                        <legend class="offLeft"><span><spring:message code='resource.lov.inputs'/></span></legend>

                        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                            <t:putAttribute name="containerClass" value="column primary noHeader"/>

                            <t:putAttribute name="bodyContent">
                                <t:putAttribute name="bodyClass" value="twoColumn_equal"/>
                                <!-- start two columns -->

                                <div class="column simple primary">

                                    <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                        <t:putAttribute name="containerClass" value="control groupBox scrollable"/>

                                        <t:putAttribute name="bodyContent">

                                            <ul class="list setLeft tabular name-value threeColumn attributesTable">
                                                <li class="node">
                                                    <div class="wrap header"><b class="icon" title=""></b>

                                                        <p class="column one"><spring:message
                                                                code='resource.lov.name'/></p>

                                                        <p class="column two"><spring:message
                                                                code='resource.lov.value'/></p>

                                                        <p class="column three"></p></div>
                                                    <ul class="" id="listOfValues">
                                                        <c:forEach
                                                                items="${requestScope.listOfValuesDTO.listOfValues.values}"
                                                                var="item">
                                                            <li class="leaf">
                                                                <div class="wrap">
                                                                    <b class="icon" title=""></b>

                                                                    <p class="column one" title="${item.label}">${item.label}</p>

                                                                    <p class="column two" title="${item.value}">${item.value}</p>

                                                                    <p class="column three"><a class="launcher"
                                                                                               id="${item.label}"><spring:message
                                                                            code='resource.lov.remove'/></a></p>
                                                                </div>
                                                            </li>
                                                        </c:forEach>
                                                        <li class="leaf">
                                                            <div class="wrap">
                                                                <b class="icon" title=""></b>

                                                                <spring:bind path="listOfValuesDTO.newLabel">
                                                                    <p class="column one <c:if test="${status.error}"> error </c:if>">
                                                                        <input id="name" name="${status.expression}"
                                                                               value="${status.value}" type="text"/>
                                                                        <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                                    </p>
                                                                </spring:bind>

                                                                <spring:bind path="listOfValuesDTO.newValue">
                                                                    <p class="column two <c:if test="${status.error}"> error </c:if>">
                                                                        <input id="value" name="${status.expression}"
                                                                               value="${status.value}" type="text"/>
                                                                        <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                                    </p>
                                                                </spring:bind>

                                                                <p class="column three">
                                                                    <a class="launcher" id="add">
                                                                        <spring:message code='resource.lov.add'/>
                                                                    </a>
                                                                </p>
                                                            </div>

                                                        </li>
                                                    </ul>
                                                </li>
                                            </ul>
                                        </t:putAttribute>
                                    </t:insertTemplate>
                                </div>
                                <div class="column simple secondary">


                                    <fieldset class="nameAndDescription">
                                        <legend class="offLeft"><span><spring:message
                                                code='resource.lov.nameanddescription'/></span></legend>

                                        <spring:bind path="listOfValuesDTO.listOfValues.label">
                                            <label class="control input text <c:if test="${status.error}"> error </c:if>"
                                                   class="required" for="labelID"
                                                   title="<spring:message code='resource.lov.labelTitle'/>">
                                                    <span class="wrap"><spring:message
                                                            code='resource.lov.name'/> (<spring:message
                                                            code='required.field'/>):</span>
                                                <input class="" id="labelID" type="text"
                                                       name="${status.expression}" value="${status.value}"/>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>

                                        <spring:bind path="listOfValuesDTO.listOfValues.name">
                                            <label class="control input text <c:if test="${status.error}"> error </c:if>"
                                                   for="resourceID"
                                                   title="<spring:message code='resource.lov.resourceTitle'/>">
                                                    <span class="wrap"><spring:message code='resource.lov.resource'/>
                                                        <c:choose>
                                                            <c:when test="${listOfValuesDTO.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                            <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                <input class="" id="resourceID" type="text" name="${status.expression}"
                                                       value="${status.value}"
                                                       <c:if test="${listOfValuesDTO.editMode}">readonly="readonly"</c:if>/>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>

                                        <spring:bind path="listOfValuesDTO.listOfValues.description">
                                            <label class="control textArea  <c:if test="${status.error}"> error </c:if>"
                                                   for="description">
                                                <span class="wrap"><spring:message code='resource.lov.desc'/></span>
                                                <textarea id="description" type="text"
                                                          name="${status.expression}">${status.value}</textarea>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>
                                    </fieldset>
                                </div>
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
                        <button id="save" type="button" class="button primary action up" name="save">
                            <span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span>
                        </button>
                        <button id="cancel" type="button" class="button action up" name="cancel"><span
                                class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span>
                        </button>
                    </fieldset>
                </t:putAttribute>

            </t:putAttribute>
        </t:insertTemplate>
        <div id="ajaxbuffer" style="display:none"></div>
        <input type="hidden" id="_flowExecutionKey" name="_flowExecutionKey" value="${flowExecutionKey}"/>
        <input type="hidden" name="_eventId_addItem" id="submitEvent"/>
        <input type="hidden" id="itemToDelete" name="itemToDelete" value=""/>
        </form>

    </t:putAttribute>
</t:insertTemplate>
