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
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<%@ include file="../common/jsEdition.jsp" %>
<c:choose>
    <c:when test="${isProVersion}">
        <c:set var="bodyColumnClass" value="threeColumn"/>
    </c:when>
    <c:otherwise>
        <c:set var="bodyColumnClass" value="twoColumn"/>
    </c:otherwise>
</c:choose>

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle">
        <spring:message code="jsp.roleManager.title" javaScriptEscape="true"/>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="manage_roles"/>
    <t:putAttribute name="bodyClass">${bodyColumnClass} manager</t:putAttribute>
    <t:putAttribute name="moduleName" value="manage/manageRolesMain"/>
    <t:putAttribute name="headerContent" >
        <jsp:include page="rolesState.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <t:insertTemplate template="/WEB-INF/jsp/templates/pageHeader.jsp">
            <t:putAttribute name="pageHeaderIconClass" value="manageRoles" cascade="false"/>
            <t:putAttribute name="pageHeaderText">
                <spring:message code="MT_MANAGE_ROLES" javaScriptEscape="true"/>
            </t:putAttribute>
        </t:insertTemplate>
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerID" value="roles"/>
            <t:putAttribute name="containerClass" value="column decorated primary showingToolBar"/>
            <t:putAttribute name="subHeaderContent" >
                <div class="toolbar">
                    <ul class="list buttonSet">
                        <li class="node open">
                            <ul class="list buttonSet">
                                <%--bug 18939: &#8230 changed to "..." --%>
                                <li class="leaf"><button id="addNewRoleBtn" class="button capsule text first up"><span class="wrap"><spring:message code="jsp.roleManager.addRole" javaScriptEscape="true"/>...</span><span class="icon"></span></button></li>
                            </ul>
                        </li>
                        <li class="node open">
                            <ul class="list buttonSet">
                                <li class="leaf"><button id="deleteAllRolesBtn" class="button capsule text up" disabled="disabled"><span class="wrap"><spring:message code="jsp.userAndRoleManager.deleteAll" javaScriptEscape="true"/></span><span class="icon"></span></button></li>
                            </ul>
                        </li>
                    </ul>
                <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
                    <t:putAttribute name="containerID" value="secondarySearchBox"/>
                    <t:putAttribute name="inputID" value="secondarySearchInput"/>
                </t:insertTemplate>
                </div>
                <t:insertTemplate template="/WEB-INF/jsp/modules/manage/manageColumnTitle.jsp">
                    <c:choose>
                        <c:when test="${isProVersion == true}">
                            <t:putAttribute name="columnClass" value="twoColumn"/>
                        </c:when>
                        <c:otherwise>
                            <t:putAttribute name="columnClass" value="oneColumn"/>
                        </c:otherwise>
                    </c:choose>

                    <t:putAttribute name="firstColumnTitle">
                        <spring:message code="jsp.roleManager.roleEditor.roleName" javaScriptEscape="true"/>
                    </t:putAttribute>
                    <c:if test="${isProVersion == true}">
                        <t:putAttribute name="secondColumnTitle">
                            <spring:message code="jsp.roleManager.roleEditor.organization" javaScriptEscape="true"/>
                        </t:putAttribute>
                    </c:if>
                </t:insertTemplate>
            </t:putAttribute>
            <t:putAttribute name="bodyID" value="listContainer"/>
            <t:putAttribute name="bodyContent">
                <ol id="entitiesList"></ol>
            </t:putAttribute>
            <t:putAttribute name="footerContent">
            </t:putAttribute>
        </t:insertTemplate>


        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerID" value="folders"/>
            <t:putAttribute name="containerClass" value="column decorated secondary sizeable"/>
            <t:putAttribute name="containerElements">
                <div class="sizer horizontal"></div>
                <button class="button minimize"></button>
                <div class="icon minimize"></div>
            </t:putAttribute>
            <t:putAttribute name="containerTitle">
                <spring:message code="jsp.roleManager.organizations" javaScriptEscape="true"/>
            </t:putAttribute>
            <t:putAttribute name="bodyClass" value=""/>
            <t:putAttribute name="bodyContent" >
                    <ul id="orgTree"></ul>
                    <div id="ajaxbuffer"></div>
            </t:putAttribute>
        </t:insertTemplate>

        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerID" value="properties"/>
            <t:putAttribute name="containerClass" value="column decorated tertiary sizeable"/>
            <t:putAttribute name="containerElements">
                <div class="sizer horizontal"></div>
                <button class="button minimize"></button>
                <div class="icon minimize"></div>
            </t:putAttribute>
            <t:putAttribute name="containerTitle"><spring:message code="jsp.userAndRoleManager.properties" javaScriptEscape="true"/></t:putAttribute>
            <t:putAttribute name="bodyClass" value=""/>

            <t:putAttribute name="bodyContent">
                <t:insertTemplate template="/WEB-INF/jsp/templates/nothingToDisplay.jsp">
                    <t:putAttribute name="bodyContent">
                        <div class="content ">
                        <div id="nothingToDisplayMessage" class="message">
                        <span class="jr-mInstructor-icon jr-mIcon jr-mIconXLarge jr-message jr"></span>
                        <span class="message-text crosstab chart"><spring:message code="jsp.roleManager.properties.nothingToDisplay" javaScriptEscape="true"/></span>
                        </div>
                        </div>
                    </t:putAttribute>
                </t:insertTemplate>
                <fieldset class="group">
                    <legend class="offLeft"><span><spring:message code="dialog.file.nameAndDescription" javaScriptEscape="true"/></span></legend>
                    <label class="control input text" class="required" for="roleName" title="<spring:message code="jsp.roleManager.roleCreator.roleName.title" javaScriptEscape="true"/>">
                        <span class="wrap"><spring:message code="jsp.roleManager.roleCreator.roleName" javaScriptEscape="true"/>:</span>
                        <input class="" id="roleName" type="text" maxlength="100" value="" readonly="readonly"/>
                        <span class="message warning"></span>
                    </label>
                </fieldset>
                <fieldset class="group">
                    <div class="control checkBox">
                        <label class="wrap" for="externalRole" title="<spring:message code="jsp.roleManager.externallyDefined" javaScriptEscape="true"/>">
                            <spring:message code="jsp.roleManager.externallyDefined" javaScriptEscape="true"/>
                        </label>
                        <input id="externalRole" type="checkbox" checked="checked" disabled="disabled"/>
                    </div>
                </fieldset>
                <fieldset id="attributes" class="oneColumn">
                    <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                        <t:putAttribute name="containerClass" value="column decorated primary showingSubHeader"/>
                        <t:putAttribute name="containerTitle"><spring:message code="jsp.roleManager.usersWithRole" javaScriptEscape="true"/></t:putAttribute>
                        <t:putAttribute name="containerID" value="assignedView"/>
                        <t:putAttribute name="headerContent" >
                            <div class="sub header">
                                <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
							        <t:putAttribute name="inputID" value="assignedViewSearchInput"/>
							    </t:insertTemplate>
                            </div>
                        </t:putAttribute>

                        <t:putAttribute name="bodyContent">
                            <ul id="assignedViewList"></ul>
                        </t:putAttribute>
                    </t:insertTemplate>
                </fieldset>
                <fieldset id="editUsers" class="row twoColumn_equal pickWells">
                    <!-- start two columns -->
                        <div id="moveButtons" class="centered_horz">
                            <button id="addToAssigned" class="button action square move right up"><span class="wrap"><b class="icon"></b></span></button>
                            <button id="removeFromAssigned" class="button action square move left up"><span class="wrap"><b class="icon"></b></span></button>
                        </div>

                        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                            <t:putAttribute name="containerClass" value="column decorated primary showingSubHeader"/>
                            <t:putAttribute name="containerTitle"><spring:message code="jsp.roleManager.roleEditor.userEditor.assigned" javaScriptEscape="true"/></t:putAttribute>
                            <t:putAttribute name="containerID" value="assigned"/>
                            <t:putAttribute name="headerContent" >
                                <div class="sub header">
                                <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
							        <t:putAttribute name="inputID" value="assignedSearchInput"/>
							    </t:insertTemplate>
                                </div>
                            </t:putAttribute>


                            <t:putAttribute name="bodyContent">
                                <ul id="assignedList"></ul>
                            </t:putAttribute>

                        </t:insertTemplate>

                        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                            <t:putAttribute name="containerClass" value="column decorated secondary sizeable showingSubHeader"/>
                            <t:putAttribute name="containerElements">
                                <div class="sizer horizontal"></div>
                                <button class="button minimize"></button>
                            </t:putAttribute>
                            <t:putAttribute name="containerTitle"><spring:message code="jsp.roleManager.roleEditor.userEditor.available" javaScriptEscape="true"/></t:putAttribute>
                            <t:putAttribute name="containerID" value="available"/>
                            <t:putAttribute name="headerContent" >
                                <div class="sub header">
                                    <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
							        	<t:putAttribute name="inputID" value="availableSearchInput"/>
							        </t:insertTemplate>
                                </div>
                            </t:putAttribute>

                            <t:putAttribute name="bodyContent">
                                <ul id="availableList"></ul>
                            </t:putAttribute>

                        </t:insertTemplate>
                    <!-- end two columns -->
                </fieldset>
            </t:putAttribute>
            <t:putAttribute name="footerID" value="propertiesButtons"/>
            <t:putAttribute name="footerContent">
                <button id="edit" type="submit" class="button action primary up"><span class="wrap"><spring:message code="form.edit" javaScriptEscape="true"/></span><span class="icon"></span></button>
                <button id="save" type="submit" class="button action primary up"><span class="wrap"><spring:message code="form.edit.save" javaScriptEscape="true"/></span><span class="icon"></span></button>
                <button id="delete" type="submit" class="button action up"><span class="wrap"><spring:message code="jsp.roleManager.deleteRoleOk" javaScriptEscape="true"/></span><span class="icon"></span></button>
                <button id="cancel" type="submit" class="button action up"><span class="wrap"><spring:message code="form.edit.cancel" javaScriptEscape="true"/></span><span class="icon"></span></button>
            </t:putAttribute>
        </t:insertTemplate>

        <t:insertTemplate template="/WEB-INF/jsp/templates/addRole.jsp">
            <t:putAttribute name="containerClass" value="hidden"/>
        </t:insertTemplate>


    </t:putAttribute>

</t:insertTemplate>
