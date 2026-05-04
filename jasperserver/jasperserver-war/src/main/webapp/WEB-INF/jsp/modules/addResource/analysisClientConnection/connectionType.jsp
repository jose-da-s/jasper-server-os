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
        <c:set var="editMode" value='${param.isEdit}'/>
        <c:if test="${editMode==null}"><spring:message code='resource.analysisConnection.title'/></c:if>
        <c:if test="${editMode!=null}"><spring:message code='resource.analysisConnection.edit.title'/></c:if>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_analysisConnection"/>
    <c:if test="${connectionWrapper.type=='olapMondrianCon'}">
        <c:if test="${connectionWrapper.mode == 3}">
        <t:putAttribute name="bodyClass" value="oneColumn mondrian flow wizard"/>
        </c:if>
        <c:if test="${connectionWrapper.mode != 3}">
        <t:putAttribute name="bodyClass" value="oneColumn mondrian flow wizard firstStep"/>
        </c:if>
    </c:if>
    <c:if test="${connectionWrapper.type=='olapXmlaCon'}">
        <c:if test="${connectionWrapper.mode == 3}">
        <t:putAttribute name="bodyClass" value="oneColumn mondrian flow wizard"/>
        </c:if>
        <c:if test="${connectionWrapper.mode != 3}">
        <t:putAttribute name="bodyClass" value="oneColumn mondrian flow wizard oneStep"/>
        </c:if>
    </c:if>
    <t:putAttribute name="moduleName" value="addResource/analysisClientConnection/addAnalysisClientConnectionMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="connectionTypeState.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <form method="post" action="">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="containerTitle">
                    <c:set var="editMode" value='${param.isEdit}'/>
                    <c:if test="${editMode==null}"><spring:message code='resource.analysisConnection.title'/></c:if>
                    <c:if test="${editMode!=null}"><spring:message code='resource.analysisConnection.edit.title'/></c:if>
                </t:putAttribute>

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                    <div id="flowControls"></div>
                    <div id="stepDisplay">
                        <input type="hidden" id="ParentFolderUri" value='${param.ParentFolderUri}'/>
                        <input type="submit" name="_eventId_changeCombo" id="changeCombo" style="visibility:hidden;"/>
                        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
                        <input type="hidden" id="editMode" value='${param.isEdit}'/>

                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.analysisConnection.instructions"/></span></legend>
                            <h2 class="textAccent02"><spring:message code="resource.analysisConnection.setConnectionTypeAndProperties"/></h2>
                            <h4><spring:message code="resource.analysisConnection.first"/></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.analysisConnection.inputs"/></span></legend>
                            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                <t:putAttribute name="containerClass" value="column primary"/>
                                <t:putAttribute name="containerTitle">
                                    <%--
                                    <spring:message code="resource.analysisConnection.type"/>:
                                    --%>
                                    <label class="wrap" for="analysisConnection.type" title="<spring:message code="resource.analysisConnection.connectionType"/>">
                                        <spring:message code="resource.analysisConnection.connectionType"/>:
                                    </label>
                                </t:putAttribute>
                                <t:putAttribute name="headerContent">
                                    <spring:bind path="connectionWrapper.type">
										<div class="control select inline">
											<select id="analysisConnection.type" name="type">
												<option <c:if test="${connectionWrapper.type=='olapMondrianCon'}">selected="selected"</c:if> value="olapMondrianCon"><spring:message code="resource.analysisConnection.mondrian"/></option>
												<option <c:if test="${connectionWrapper.type=='olapXmlaCon'}">selected="selected"</c:if> value="olapXmlaCon"><spring:message code="resource.analysisConnection.xmla"/></option>
                                            </select>
                                            <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
										</div>
                                    </spring:bind>
                                </t:putAttribute>

                                <t:putAttribute name="bodyContent">
                                    <fieldset class="group">
                                        <!-- NOTE: This fieldset appears identically for all connection types -->
                                        <legend class="offLeft"><span><spring:message code="resource.analysisConnection.nameAndDescription"/></span></legend>

                                        <spring:bind path="connectionWrapper.connectionLabel">
											<div class="control input text <c:if test="${status.error}">error</c:if>">
												<label class="wrap" class="required" for="${status.expression}" title="<spring:message code="resource.analysisConnection.visibleName"/>">
													<spring:message code="resource.analysisConnection.name"/> (<spring:message code='required.field'/>):
												</label>
												<input class="" id="${status.expression}" name="${status.expression}" type="text" value="${status.value}" title="<spring:message code="resource.analysisConnection.visibleName"/>"/>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
											</div>
                                        </spring:bind>

                                        <spring:bind path="connectionWrapper.connectionName">
											<div class="control input text <c:if test="${status.error}">error</c:if>">
												<label class="wrap" for="connectionName" title="<spring:message code="resource.analysisConnection.permanentID"/>">
													<spring:message code="resource.analysisConnection.resourceID"/>
                                                    <c:choose>
                                                        <c:when test="${connectionWrapper.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                        <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                    </c:choose>
												</label>
												<input class="" id="connectionName" name="${status.expression}" type="text" value="${status.value}" <c:if test="${connectionWrapper.editMode}">readonly="readonly"</c:if> title="<spring:message code="resource.analysisConnection.permanentID"/>"/>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
											</div>
                                        </spring:bind>

                                        <spring:bind path="connectionWrapper.connectionDescription">
											<div class="control textArea <c:if test="${status.error}">error</c:if>">
												<label class="wrap" for="${status.expression}">
													<spring:message code="resource.analysisConnection.description"/>:
												</label>
												<textarea id="${status.expression}" name="${status.expression}" type="text">${status.value}</textarea>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
											</div>
                                        </spring:bind>
                                    </fieldset>

                                    <c:if test="${connectionWrapper.type=='olapXmlaCon'}">
                                        <fieldset  class="group" id="xmlA">
                                            <!-- NOTE: this fieldset only appears if type == XML/A -->
                                            <legend class="offLeft"><span><spring:message code="resource.analysisConnection.properties"/></span></legend>

                                            <spring:bind path="connectionWrapper.xmlaCatalog">
												<div class="control input text <c:if test="${status.error}">error</c:if>">
													<label class="wrap" class="required" for="${status.expression}" title="<spring:message code="resource.analysisConnection.catalog"/>">
														<spring:message code="resource.analysisConnection.catalog"/> (<spring:message code='required.field'/>):
													</label>
													<input class="" id="${status.expression}" name="${status.expression}" type="text" value="${status.value}"/>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                    <span class="message hint"><spring:message code="resource.analysisConnection.foodmart"/></span>
												</div>
                                            </spring:bind>

                                            <spring:bind path="connectionWrapper.xmlaDatasource">
												<div class="control input text <c:if test="${status.error}">error</c:if>">
													<label class="wrap" class="required" for="${status.expression}" title="<spring:message code="resource.analysisConnection.dataSource"/>">
														<spring:message code="resource.analysisConnection.dataSource"/> (<spring:message code='required.field'/>):
													</label>
													<input class="" id="${status.expression}" name="${status.expression}" type="text" value="${status.value}"/>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                    <span class="message hint"><spring:message code="resource.analysisConnection.providerDataSource"/></span>
												</div>
                                            </spring:bind>

                                            <spring:bind path="connectionWrapper.xmlaConnectionUri">
												<div class="control input text <c:if test="${status.error}">error</c:if>">
													<label class="wrap" class="required" for="${status.expression}" title="<spring:message code="resource.analysisConnection.uri"/>">
														<spring:message code="resource.analysisConnection.uri"/> (<spring:message code='required.field'/>):
													</label>
													<input class="" id="${status.expression}" name="${status.expression}" type="text" value="${status.value}"/>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                    <span class="message hint"><spring:message code="resource.analysisConnection.xmlaHint"/></span>
												</div>
                                            </spring:bind>

                                            <spring:bind path="connectionWrapper.username">
												<div class="control input text <c:if test="${status.error}">error</c:if>">
													<label class="wrap" class="required" for="${status.expression}" title="<spring:message code="resource.analysisConnection.userName"/>">
														<spring:message code="resource.analysisConnection.userName"/>:
													</label>
													<input class="" id="${status.expression}" name="${status.expression}" type="text" value="${status.value}"/>
                                                    <c:if test="${status.error}">
                                                        <span class="message warning">${status.errorMessage}</span>
                                                    </c:if>
                                                    <span class="message hint"></span>
												</div>
                                            </spring:bind>

                                            <spring:bind path="connectionWrapper.password">
												<div class="control input password <c:if test="${status.error}">error</c:if>">
													<label class="wrap" class="required" for="${status.expression}" title="<spring:message code="resource.analysisConnection.password"/>">
														<spring:message code="resource.analysisConnection.password"/>:
													</label>
													<input class="" id="${status.expression}" name="${status.expression}" type="password" value="${not connectionWrapper.editMode ? null : passwordSubstitution}"/>
                                                    <c:if test="${status.error}">
                                                        <span class="message warning">${status.errorMessage}</span>
                                                    </c:if>
                                                    <span class="message hint"></span>
												</div>
                                            </spring:bind>
                                        </fieldset>
                                    </c:if>
                                    <fieldset  class="group">
                                        <spring:bind path="connectionWrapper.parentFolder">
											<div class="control browser <c:if test="${status.error}"> error</c:if>">
												<label class="wrap" for="resourceUri" title="<spring:message code="resource.analysisConnection.parentFolder"/>">
													<spring:message code="resource.analysisConnection.parentFolder"/>
												</label>
												<input id="resourceUri" type="text" name="${status.expression}" value="${status.value}" <c:if test="${connectionWrapper.editMode}">disabled="disabled"</c:if> title="<spring:message code="resource.analysisConnection.parentFolder"/>"/>
                                                <button id="browser_button" type="button" class="button action up" <c:if test="${connectionWrapper.editMode}">disabled="disabled"</c:if>><span class="wrap"><spring:message code="button.browse"/><span class="icon"></span></span></button>
                                                <c:if test="${status.error}">
                                                    <span class="message warning">${status.errorMessage}</span>
                                                </c:if>
											</div>
                                        </spring:bind>
                                    </fieldset>
                                    <c:if test="${connectionWrapper.type=='olapXmlaCon'}">
                                        <fieldset class="group">
                                            <span id="testXMLAConnection" type="submit" class="button action up"
                                                  name="_eventId_testXMLAConnection"><span
                                                    class="wrap"><spring:message code='button.testConnection'/></span>
                                            </span>
                                            <!-- NOTE: for test we are passing username and password parameters in form - new Spring trying to re-authentificate flow - so we must pass disable Flag -->
                                            <input id="disable-re-authentication-flag" class="hidden" type="hidden" name="disable-re-authentication-flag" value="1"/>
                                            <div class="message warning">
                                                <span >error message here</span>
                                                <a href="#" class="details"><spring:message code='button.details'/></a>
                                            </div>
                                        </fieldset>
                                    </c:if>
                                </t:putAttribute>
                            </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="previous" type="submit" name="_eventId_Back" class="button action up" <c:if test="${not connectionWrapper.subflowMode}">disabled="disabled"</c:if> ><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
                            <button id="next" type="submit" name="<c:if test="${connectionWrapper.type=='olapMondrianCon'}">_eventId_NextMondrian</c:if><c:if test="${connectionWrapper.type=='olapXmlaCon'}">_eventId_NextXmla</c:if>" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" type="submit" name="<c:if test="${connectionWrapper.type=='olapMondrianCon'}">_eventId_NextMondrian</c:if><c:if test="${connectionWrapper.type=='olapXmlaCon'}">_eventId_NextXmla</c:if>" class="button action primary up"><span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span></button>
                            <%--<button id="done" type="submit" class="button primary action up"><span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span></button>--%>
                            <button id="cancel" type="submit" name="_eventId_Cancel" type="button" class="button action up"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
        <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="folderTreeRepoLocation"> </ul>
            </t:putAttribute>
        </t:insertTemplate>

        <div id="ajaxbuffer" class="hidden" ></div>
    </t:putAttribute>
</t:insertTemplate>
