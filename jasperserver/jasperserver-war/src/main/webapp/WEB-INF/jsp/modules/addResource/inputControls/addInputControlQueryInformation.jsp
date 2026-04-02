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
     <t:putAttribute name="pageTitle"><spring:message code="addinputcontrol.queryextra.page.title"/></t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_inputControl_information"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard lastStep"/>
    <t:putAttribute name="moduleName" value="addResource/inputControls/addInputControlQueryInformationMain"/>

    <t:putAttribute name="bodyContent" >
    	<form method="post" id="extra">
		<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
			<t:putAttribute name="containerClass" value="column decorated primary"/>
		    <t:putAttribute name="containerTitle"><spring:message code="addResources.addInputControleQueryInformation.title" javaScriptEscape="true"/></t:putAttribute>
            <t:putAttribute name="swipeScroll" value="${isIPad}"/>

		    <t:putAttribute name="bodyContent">
			<div id="flowControls"></div>
				<div id="stepDisplay">
					<fieldset class="row instructions">
						<legend class="offLeft"><span><spring:message code="addResources.addInputControleQueryInformation.instructions" javaScriptEscape="true"/> </span></legend>
						<h2 class="textAccent02"><spring:message code="addResources.addInputControleQueryInformation.textAccent02" javaScriptEscape="true"/> </h2>
						<h4><spring:message code="addResources.addInputControleQueryInformation.subHeader" javaScriptEscape="true"/> </h4>
					</fieldset>

					<fieldset class="row inputs oneColumn">
						<legend class="offLeft"><span><spring:message code="addResources.addInputControleQueryInformation.userInputs" javaScriptEscape="true"/> </span></legend>

							<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
							    <t:putAttribute name="containerClass" value="column noHeader primary"/>

							    <t:putAttribute name="bodyContent">
									<fieldset>
										<legend class="label offLeft"><span class="wrap"><spring:message code="addResources.addInputControleQueryInformation.columnParameters" javaScriptEscape="true"/> </span></legend>
					                    <ul class="list tabular linkedResources twoColumn">
											<li id="control" class="node">
												<div class="wrap header"><p class="column one"><spring:message code="addResources.addInputControleQueryInformation.valueColumn" javaScriptEscape="true"/> </p><p class="column two"></p></div>
						                    	<ul class="list tabular linkedResources twoColumn">

                                                    <spring:bind path="control.inputControl.queryValueColumn">
													<li class="leaf"><!-- NOTE: this row ALWAYS visible, always last -->
														<div class="wrap"><p class="column one"><input type="text" id="labelID" name="${status.expression}"
                                                                               value="${status.value}"></p><p class="column two">(<spring:message code='required.field'/>)</p></div>
													</li>
													<li class="leaf errorMessage">
                                                        <c:if test="${status.error}"><div class="wrap <c:if test="${status.error}"> error </c:if>"><p class="message warning">${status.errorMessage}</div> </c:if>
													</li>
                                                    </spring:bind>
						                    	</ul>

											</li>
											<li id="controls" class="node">
												<div class="wrap header"><p class="column one"><spring:message code="addResources.addInputControleQueryInformation.visibleColumns" javaScriptEscape="true "/> </p><p class="column two"></p></div>
						                    	<ul  class="list tabular linkedResources twoColumn">

						                    		<c:forEach items="${control.inputControl.queryVisibleColumnsAsList}" var="column">
						                    		<li class="leaf"><!-- NOTE: This leaf is an example of what an added value looks like -->
														<div class="wrap">
															<p class="column one">${column}</p>
															<p class="column two"><a id="${column}" class="launcher"><spring:message code="button.remove"/></a></p>
														</div>
													</li>
                                                    </c:forEach>
                                                    <spring:bind path="control.newVisibleColumn">
													<li class="leaf">
														<div class="wrap"><p class="column one"><input type="text" id="value" name="${status.expression}"
                                                                               value="${status.value}"></p><p class="column two"><a class="launcher" id="add"><spring:message code="addResources.addInputControleQueryInformation.add" javaScriptEscape="true"/> </a></p></div>
													</li>
													<li class="leaf errorMessage">
														<c:if test="${status.error}"><div class="wrap <c:if test="${status.error}"> error </c:if>"><p class="message warning">${status.errorMessage}</div> </c:if>
													</li>
                                                    </spring:bind>
						                    	</ul>

											</li>
					                    </ul>


					                </fieldset>
							    </t:putAttribute>

					</t:insertTemplate>
				</fieldset><!--/.row.inputs-->
				</div>
				<t:putAttribute name="footerContent">
					<fieldset id="wizardNav" >
						<button id="previous" type="submit" class="button action up" name="_eventId_back"><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
						<button id="next" type="submit" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
						<button id="save" type="submit" class="button primary action up" name="_eventId_save"><span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span></button>
						<button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
				    </fieldset>
				</t:putAttribute>

			</t:putAttribute>
		</t:insertTemplate>
        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
        <input type="hidden" id="ar"/>
        <input type="hidden" id="itemToDelete" name="itemToDelete" value=""/>

    </t:putAttribute>

</t:insertTemplate>
