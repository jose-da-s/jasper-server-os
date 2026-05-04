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

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle"><c:choose><c:when test="${query.editMode=='true'}"><spring:message code="resource.query.titleEdit"/></c:when><c:otherwise><spring:message code="resource.query.title"/></c:otherwise></c:choose></t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_query_step3"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard lastStep"/>
    <t:putAttribute name="moduleName" value="addResource/query/addQueryMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="queryStep3State.jsp"/>
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
                        <li class="leaf"><p class="wrap" href="#"><b class="icon"></b><spring:message code="resource.query.nameQuery"/></p></li>
                        <li class="leaf"><p class="wrap" href="#"><b class="icon"></b><spring:message code="resource.query.linkDataSource"/></p></li>
                        <li class="leaf selected"><p class="wrap" href="#"><b class="icon"></b><spring:message code="resource.query.defineQuery"/></p></li>
                    </ul>
                </div>
                    <div id="stepDisplay">
                        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>

                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.query.instructions"/></span></legend>
                            <h2 class="textAccent02"><spring:message code="resource.query.defineQuery"/></h2>
                            <h4><spring:message code="resource.query.queryLanguage"/></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.query.inputs"/></span></legend>
                                <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                    <t:putAttribute name="containerClass" value="column primary"/>
                                    <t:putAttribute name="containerTitle"><spring:message code="resource.query.queryLanguage2"/>:</t:putAttribute>
                                    <t:putAttribute name="headerContent">
                                        <spring:bind path="query.query.language">
                                            <label class="control select inline" for="${status.expression}" title="<spring:message code='resource.query.queryLanguage2'/>">
                                                <span class="wrap offLeft"><spring:message code="jsp.editQueryTextForm.queryLanguage"/></span>
                                                <select id="${status.expression}" name="${status.expression}">
                                                    <c:forEach items="${requestScope.queryLanguages}" var="language">
                                                        <option value="${language}" <c:if test="${status.value == language}">selected</c:if>><spring:message code="query.language.${language}.label"/></option>
                                                    </c:forEach>
                                                </select>
                                                <c:if test="${status.error}">
                                                    <c:forEach items="${status.errorMessages}" var="error">
                                                        <span class="message warning">${error}</span>
                                                    </c:forEach>
                                                </c:if>
                                            </label>
                                        </spring:bind>
                                    </t:putAttribute>

                                    <t:putAttribute name="bodyContent">
                                        <spring:bind path="query.query.sql">
                                            <fieldset class="group">
                                                <label class="control textArea" for="${status.expression}">
                                                    <span class="wrap"><spring:message code="jsp.editQueryTextForm.queryString"/>:</span>
                                                    <textarea name="${status.expression}" id="${status.expression}" type="text">${status.value}</textarea>
                                                    <c:if test="${status.error}">
                                                        <c:forEach items="${status.errorMessages}" var="error">
                                                            <span class="message warning">${error}</span>
                                                        </c:forEach>
                                                    </c:if>
                                                </label>
                                            </fieldset>
                                        </spring:bind>
                                    </t:putAttribute>
                                </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="previous" type="submit" name="_eventId_back" class="button action up"><span class="wrap"><spring:message code="button.previous"/></span><span class="icon"></span></button>
                            <button id="next" type="submit" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" type="submit" name="_eventId_save" class="button primary action up"><span class="wrap"><spring:message code="button.save"/></span><span class="icon"></span></button>
                            <button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
    </t:putAttribute>
</t:insertTemplate>
