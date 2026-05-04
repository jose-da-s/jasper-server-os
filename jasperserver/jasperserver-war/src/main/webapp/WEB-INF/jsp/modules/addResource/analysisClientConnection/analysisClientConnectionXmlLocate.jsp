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
    <t:putAttribute name="pageTitle"><spring:message
            code="resource.analysisConnection.xmla.locate.page.title"/></t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_locateDatatype"/>

    <t:putAttribute name="bodyClass" value="oneColumn flow wizard"/>
    <t:putAttribute name="moduleName" value="addResource/analysisClientConnection/locateXmlConnectionSourceMain"/>

    <t:putAttribute name="bodyContent">
        <form method="post" id="xml" >
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="column decorated primary"/>
            <t:putAttribute name="containerTitle"><spring:message code='addResources.analysisClient.ConectionXmlLocate.title' htmlEscape='true'/></t:putAttribute>

            <t:putAttribute name="headerContent">
                <jsp:include page="analysisClientConnectionXmlLocateState.jsp"/>
            </t:putAttribute>

            <t:putAttribute name="swipeScroll" value="${isIPad}"/>

            <t:putAttribute name="bodyContent">
                <div id="flowControls">
                </div>
                <div id="stepDisplay">
                    <fieldset class="row instructions">
                        <legend class="offLeft"><span><spring:message code='addResources.analysisClient.ConectionXmlLocate.instructions' htmlEscape='true'/></span></legend>
                        <h2 class="textAccent02"><spring:message code='addResources.analysisClient.ConectionXmlLocate.locateXmlConSource' htmlEscape='true'/></h2>
                        <h4><!--NOTE: keep h4 markup, but leave empty --></h4>

                        <p class="warning">Error or warning here.</p>
                    </fieldset>

                    <fieldset class="row inputs oneColumn">
                        <legend class="offLeft"><span><spring:message code='addResources.analysisClient.ConectionXmlLocate.userInputs' htmlEscape='true'/></span></legend>

                        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                            <t:putAttribute name="containerClass" value="column primary"/>
                            <t:putAttribute name="containerTitle"><spring:message code='addResources.analysisClient.ConectionXmlLocate.conectionType' htmlEscape='true'/></t:putAttribute>
                            <t:putAttribute name="headerContent">
                                <spring:bind path="connectionWrapper.type">
                                    <label class="control select inline" for="type" title="Connection type">
                                        <span class="wrap offLeft"><spring:message code='addResources.analysisClient.ConectionXmlLocate.conectionType' htmlEscape='true'/></span>
                                        <select id="type" name="${status.expression}">
                                            <option
                                                    <c:if test="${connectionWrapper.type=='olapMondrianCon'}">selected="selected"</c:if>
                                                    value="olapMondrianCon" name="${status.value}"><spring:message code="resource.com.jaspersoft.jasperserver.api.metadata.olap.domain.MondrianConnection.label"/>
                                            </option>
                                            <option
                                                    <c:if test="${connectionWrapper.type=='olapXmlaCon'}">selected="selected"</c:if>
                                                    value="olapXmlaCon" name="${status.value}"><spring:message code="resource.com.jaspersoft.jasperserver.api.metadata.olap.domain.XMLAConnection.label"/>
                                            </option>
                                        </select>
                                    </label>
                                </spring:bind>
                            </t:putAttribute>
                            <t:putAttribute name="bodyContent">
                                <fieldset class="group locationSelector">
                                    <legend class="offLeft"><span><spring:message code='addResources.analysisClient.ConectionXmlLocate.locateResource' htmlEscape='true'/></span></legend>
                                    <ul class="list locations">
                                        <spring:bind path="connectionWrapper.source">
                                            <c:if test="${not connectionWrapper.subEditMode}">
                                                <li id="create" class="leaf">
													<div class="control radio<c:if test="${status.error and status.value == 'LOCAL'}"> error</c:if>">
														<label class="wrap" for="LOCAL" title="<spring:message code='addResources.analysisClient.defineDataSource'/>">
															<spring:message code='addResources.analysisClient.ConectionXmlLocate.defineXmlConection' htmlEscape='true'/>
														</label>
														<input class="" id="LOCAL" type="radio" name="${status.expression}"
                                                               value="LOCAL"
                                                               <c:if test='${status.value=="LOCAL"}'>checked="true"</c:if>/>
                                                        <c:if test="${status.error and status.value == 'LOCAL'}">
                                                            <span class="message warning">${status.errorMessage}</span>
                                                        </c:if>
													</div>
                                                </li>
                                            </c:if>
                                            <li id="fromRepo" class="leaf">
												<div class="control radio complex">
													<label class="wrap" for="CONTENT_REPOSITORY"
                                                       title="<spring:message code='resource.report.repository'/>">
														<spring:message code='addResources.analysisClient.ConectionXmlLocate.selectXmlConection' htmlEscape='true'/>
													</label>
													<input id='CONTENT_REPOSITORY' name="${status.expression}" type="radio" value="CONTENT_REPOSITORY"
                                                           <c:if test='${status.value!="LOCAL"}'>checked="true"</c:if> />
												</div>
                                                </spring:bind>
                                                <spring:bind path="connectionWrapper.connectionUri">
                                                    <label class="control browser<c:if test="${status.error and connectionWrapper.source != 'LOCAL'}"> error</c:if>"
                                                           for="resourceUri">
                                                        <input id="resourceUri" type="text" name="${status.expression}"
                                                               value="${status.value}"
                                                               <c:if test="${empty status.value && connectionWrapper.source == 'LOCAL'}">disabled="disabled"</c:if>/>
                                                        <button id="browser_button" type="button" class="button action up"
                                                                <c:if test="${empty status.value && connectionWrapper.source == 'LOCAL'}">disabled="disabled"</c:if>>
                                                            <span class="wrap">
                                                                <spring:message code="button.browse"/>
                                                                <span class="icon"></span>
                                                            </span>
                                                        </button>
                                                        <c:if test="${status.error and connectionWrapper.source != 'LOCAL'}">
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
                    <!--/.row.inputs-->
                </div>

            </t:putAttribute>
            <t:putAttribute name="footerContent">
                    <fieldset id="wizardNav">
                        <button id="previous" type="submit" class="button action up" name="_eventId_back"><span
                                class="wrap"><spring:message
                                code='button.previous'/></span><span class="icon"></span></button>
                        <button id="next" type="submit" class="button action up" name="_eventId_next"><span
                                class="wrap"><spring:message
                                code='button.next'/></span><span class="icon"></span></button>
                        <button id="submit" type="submit" class="button primary action up"><span
                                class="wrap"><spring:message code='button.submit'/></span><span
                                class="icon"></span></button>
                        <button id="cancel" type="submit" name="_eventId_cancel" class="button action up"><span
                                class="wrap"><spring:message
                                code='button.cancel'/></span><span class="icon"></span></button>
                        <button id="chooseType" type="submit" class="hidden" name="_eventId_changeCombo"><span
                            class="wrap"><spring:message
                            code='button.next'/></span><span class="icon"></span></button>
                    </fieldset>
                </t:putAttribute>
        </t:insertTemplate>

        <!--/.row.inputs-->
        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
        <!--/#stepDisplay-->
        <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="OLAPTreeRepoLocation"></ul>
            </t:putAttribute>
        </t:insertTemplate>

        <div id="ajaxbuffer" class="hidden"></div>
        </form>
    </t:putAttribute>
</t:insertTemplate>
