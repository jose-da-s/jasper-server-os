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
     <t:putAttribute name="pageTitle"><spring:message code="resource.query.page.title"/></t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_query_step3"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard lastStep"/>
    <t:putAttribute name="moduleName" value="commons/commonsMain"/>
    <t:putAttribute name="bodyContent" >
		<form method="post" >
		<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
			<t:putAttribute name="containerClass" value="column decorated primary"/>
		    <t:putAttribute name="containerTitle">Add Query</t:putAttribute>

            <t:putAttribute name="swipeScroll" value="${isIPad}"/>

		    <t:putAttribute name="bodyContent">
				<div id="flowControls"></div>
				<div id="stepDisplay">
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
							    	<label class="control select inline" for="input_9" title="Query Language">
               							<span class="wrap offLeft"><spring:message code="resource.query.queryLanguage2"/></span>
                                        <select id="input_9" name="queryLanguage">
                                            <option selected="selected" value="MDX">MDX</option>
                                        </select>
                						<span class="message warning">error message here</span>
           							</label>

							    </t:putAttribute>
							    <t:putAttribute name="bodyContent">
									<fieldset  class="group">
                                        <spring:bind path="wrapper.olapUnitMdxQuery">
                                            <label class="control textArea <c:if test="${status.error}"> error </c:if>" for="queryString">
                                                <span class="wrap"><spring:message code="jsp.editQueryTextForm.queryString"/>:</span>
                                                <textarea id="queryString" type="text" name="${status.expression}">${status.value}</textarea>
                                                <c:if test="${status.error}"><span
                                                        class="message warning">${status.errorMessage}</span></c:if>
                                            </label>
                                        </spring:bind>
					                </fieldset>
							    </t:putAttribute>

							    		</t:insertTemplate>
					</fieldset><!--/.row.inputs-->
					</div>
				<t:putAttribute name="footerContent">
					<fieldset id="wizardNav">
						<button id="previous" type="submit" class="button action up" name="_eventId_back"><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
						<button id="next" type="submit" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
						<button id="done" type="submit" class="button primary action up" name="_eventId_save"><span class="wrap"><spring:message code='button.submit'/></span><span class="icon"></span></button>
						<button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
				    </fieldset>
				</t:putAttribute>

			</t:putAttribute>
		</t:insertTemplate>
        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
    </form>
    </t:putAttribute>

</t:insertTemplate>
