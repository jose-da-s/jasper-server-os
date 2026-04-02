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
    <t:putAttribute name="pageTitle">
        <spring:message code="jsp.inputControlSource.header"/>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_locateInputControl"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard"/>
    <t:putAttribute name="moduleName" value="addResource/jasperReport/addJasperReportLocateControlMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="locateControlResourceState.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
    	<form method="post" action="flow.html">
		<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
			<t:putAttribute name="containerClass" value="column decorated primary"/>
		    <t:putAttribute name="containerTitle"><spring:message code='addResources.locateControlResource.title' javaScriptEscape='true'/></t:putAttribute>

            <t:putAttribute name="swipeScroll" value="${isIPad}"/>

		    <t:putAttribute name="bodyContent">

                <div id="flowControls"></div>
				<div id="stepDisplay">
					<fieldset class="row instructions">
						<legend class="offLeft"><span><spring:message code='addResources.locateControlResource.instructions' javaScriptEscape='true'/></span></legend>
						<h2 class="textAccent02"><spring:message code="jsp.inputControlSource.header"/></h2>
						<h4><!--NOTE: keep h4 markup, but leave empty --></h4>
					</fieldset>

					<fieldset class="row inputs oneColumn">
						<legend class="offLeft"><span><spring:message code='addResources.locateControlResource.userInputs' javaScriptEscape='true'/></span></legend>

							<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
							    <t:putAttribute name="containerClass" value="column noHeader primary"/>

							    <t:putAttribute name="bodyContent">
                                    <fieldset class="locationSelector">
                                        <legend class="offLeft"><span><spring:message code='addResources.locateControlResource.locateResource' javaScriptEscape='true'/></span></legend>
                                        <ul class="list locations">
                                            <spring:bind path="wrapper.inputControlSource">
                                                <li id="create" class="leaf">
                                                    <div class="control radio">
                                                        <label class="wrap" for="LOCAL" title="<spring:message code='addResources.locateControlResource.defineInput'/>">
                                                            <spring:message code='addResources.locateControlResource.defineInput' javaScriptEscape='true'/>
                                                        </label>
                                                        <input id="LOCAL" type="radio"  name="${status.expression}"
                                                               value="LOCAL" <c:if test='${status.value=="LOCAL" || empty wrapper.inputControlList}'>checked="true"</c:if>/>
                                                    </div>
                                                </li>
                                            </spring:bind>
                                            <li id="fromRepo" class="leaf">
                                                <spring:bind path="wrapper.inputControlSource">
                                                    <div class="control radio complex">
                                                        <label class="wrap" for="CONTENT_REPOSITORY" title="<spring:message code='resource.report.repository'/>">
                                                            <spring:message code='addResources.locateControlResource.selectInput' javaScriptEscape='true'/>
                                                        </label>
                                                        <input id="CONTENT_REPOSITORY" name="${status.expression}" type="radio" value="CONTENT_REPOSITORY"
                                                                <c:if test='${status.value !="LOCAL"  && !empty wrapper.inputControlList}'>checked="true"</c:if>
                                                                <c:if test="${empty wrapper.inputControlList}">disabled</c:if>/>
                                                        <span class="message warning"></span>
                                                    </div>
                                                </spring:bind>
                                                <spring:bind path="wrapper.inputControlPath">
                                                    <label class="control browser<c:if test="${status.error and wrapper.inputControlSource != 'LOCAL'}"> error</c:if>" for="resourceUri">
                                                        <input id="resourceUri" type="text" name="${status.expression}" value="${status.value}"
                                                               <c:if test="${empty status.value && wrapper.inputControlSource == 'LOCAL'}">disabled="disabled"</c:if>/>
                                                        <button id="browser_button" type="button" class="button action up" <c:if test="${empty status.value && wrapper.inputControlSource == 'LOCAL'}">disabled="disabled"</c:if>>
                                                            <span class="wrap">
                                                                <spring:message code="button.browse"/>
                                                                <span class="icon"></span>
                                                            </span>
                                                        </button>
                                                        <c:if test="${status.error and wrapper.inputControlSource != 'LOCAL'}">
                                                            <span class="message warning">${status.errorMessage}</span>
                                                        </c:if>
                                                    </label>
                                                </spring:bind>
                                            </li>
                                        </ul>
                                    </fieldset>
							    </t:putAttribute>
                            </t:insertTemplate>
					</fieldset><!--/.row.inputs-->
					</div>
				<t:putAttribute name="footerContent">
					<fieldset id="wizardNav" >
						<button id="previous" type="submit" name="_eventId_Back" class="button action up"><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
						<button id="next" type="submit" name="_eventId_Next" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
						<button id="cancel" type="submit" name="_eventId_Cancel" class="button action up"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
				    </fieldset>
				</t:putAttribute>

			</t:putAttribute>
		</t:insertTemplate>
        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
    </form>
    <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
        <t:putAttribute name="containerClass">hidden</t:putAttribute>
        <t:putAttribute name="bodyContent">
            <ul id="inputControlTreeRepoLocation"> </ul>
        </t:putAttribute>
    </t:insertTemplate>
    <div id="ajaxbuffer" class="hidden" ></div>

    </t:putAttribute>

</t:insertTemplate>
