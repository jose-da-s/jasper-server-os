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
<%@ page contentType="text/html; charset=utf-8" %>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="/WEB-INF/jasperserver.tld" prefix="js" %>

<%@ page import="com.jaspersoft.jasperserver.search.model.SearchActionModelSupport" %>

<%@ include file="../common/jsEdition.jsp" %>

<%-- Setting bodyID, page title, primary container title, nothing to display message. --%>
<c:set var="bodyId" value="repoBrowse"/>
<c:set var="pageTitle"><spring:message code='SEARCH_BROWSE_REPOSITORY' javaScriptEscape='true'/></c:set>
<c:set var="primaryContainerTitle"><spring:message code='SEARCH_SEARCH' javaScriptEscape='true'/></c:set>
<c:set var="nothingToDisplayMsg"><spring:message code="repository.nothingToDisplay.browse" javaScriptEscape="true"/></c:set>
<c:set var="pageClass">twoColumn</c:set>

<c:choose>
    <c:when test="${mode == 'search'}">
        <c:set var="bodyId" value="repoSearch"/>
        <c:set var="pageTitle"><spring:message code='SEARCH_SEARCH_REPOSITORY' javaScriptEscape='true'/></c:set>
        <c:set var="primaryContainerTitle"><spring:message code='SEARCH_TITLE' javaScriptEscape='true'/></c:set>
        <c:set var="nothingToDisplayMsg"><spring:message code="repository.nothingToDisplay.search" javaScriptEscape="true"/></c:set>
        <c:set var="pageClass">twoColumn</c:set>
    </c:when>
    <c:when test="${mode == 'library'}">
        <c:set var="bodyId" value="repoLibrary"/>
        <c:set var="pageTitle"><spring:message code='SEARCH_LIBRARY' javaScriptEscape='true'/></c:set>
        <c:set var="primaryContainerTitle"><spring:message code='SEARCH_LIBRARY' javaScriptEscape='true'/></c:set>
        <c:set var="nothingToDisplayMsg"><spring:message code="repository.nothingToDisplay.library" javaScriptEscape="true"/></c:set>
        <c:set var="pageClass">oneColumn</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="bodyId" value="repoBrowse"/>
        <c:set var="pageTitle"><spring:message code='SEARCH_BROWSE_REPOSITORY' javaScriptEscape='true'/></c:set>
        <c:set var="primaryContainerTitle"><spring:message code='SEARCH_TITLE' javaScriptEscape='true'/></c:set>
        <c:set var="nothingToDisplayMsg"><spring:message code="repository.nothingToDisplay.browse" javaScriptEscape="true"/></c:set>
        <c:set var="pageClass">twoColumn</c:set>
    </c:otherwise>
</c:choose>
<c:set var="modelDocument">
    <%= SearchActionModelSupport.getInstance((String) request.getAttribute("mode")).getClientActionModelDocument(request) %>
</c:set>



<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle" value="${pageTitle}"/>
    <t:putAttribute name="bodyID" value="${bodyId}"/>
    <t:putAttribute name="bodyClass" value="${pageClass}"/>
    <t:putAttribute name="moduleName" value="repository/repositoryMain"/>
    <t:putAttribute name="headerContent" >
        <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="importExport.css"/>" type="text/css" media="screen,print"/>
        <jsp:include page="resultsState.jsp"/>

        <%--get action model data for search menus--%>
        <script type="text/json" id="searchActionModel">
            <js:out escapeScript="false">${modelDocument}</js:out>
        </script>

    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <t:insertTemplate template="/WEB-INF/jsp/templates/pageHeader.jsp">
            <c:choose>
                <c:when test="${mode == 'library'}">
                    <t:putAttribute name="pageHeaderIconClass" value="repoLibrary" cascade="false"/>
                </c:when>
                <c:otherwise>
                    <t:putAttribute name="pageHeaderIconClass" value="repoBrowse" cascade="false"/>
                </c:otherwise>
            </c:choose>
            <t:putAttribute name="pageHeaderText" value="${primaryContainerTitle}" cascade="false"/>
        </t:insertTemplate>

        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerID" value="folders"/>
            <t:putAttribute name="containerClass" value="column decorated secondary sizeable ${mode == 'search' ? 'hidden' : ''}"/>
            <t:putAttribute name="containerElements">
                <div class="sizer horizontal"></div>
                <button class="button minimize"></button>
                <div class="icon minimize"></div>
            </t:putAttribute>
            <t:putAttribute name="containerTitle"><spring:message code="SEARCH_FOLDERS" javaScriptEscape="true"/></t:putAttribute>
            <t:putAttribute name="containerTitleEmpty" value="${false}"></t:putAttribute>
            <t:putAttribute name="swipeScroll" value="${isIPad}"/>
            <t:putAttribute name="bodyID">foldersPodContent</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="foldersTree" style="position:absolute;" class="list responsive collapsible folders" ${mode == "browse" ? "data-tab-index='3' data-component-type='tree'" : ""}></ul>
                <div id="ajaxbuffer" style="display:none"></div>
            </t:putAttribute>
        </t:insertTemplate>

        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerID" value="results"/>
            <t:putAttribute name="containerClass" value="column decorated primary"/>
            <%--
            <t:putAttribute name="headerContent">
                    <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
				        <t:putAttribute name="containerID" value="secondarySearchBox"/>
                        <t:putAttribute name="containerAttr">
                            <c:if test="${mode == 'search'}">data-tab-index="3" data-component-type="search"</c:if>
                        </t:putAttribute>
				        <t:putAttribute name="inputID" value="secondarySearchInput"/>
				    </t:insertTemplate>


                <form id="secondarySearchBox" class="searchLockup">
                    <label for="secondarySearchInput" class="offLeft"><spring:message code="button.search" javaScriptEscape="true"/></label>
                    <input class="" id="secondarySearchInput"/>
                    <b class="right"><button class="button searchClear"></button></b>
                    <button class="button search up"></button>
                </form>


                <ul id="sortMode" class=""></ul>
            </t:putAttribute> --%>
            <t:putAttribute name="subHeaderContent">
                <div class="toolbar">
                <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
                    <t:putAttribute name="containerID" value="secondarySearchBox"/>
                    <t:putAttribute name="inputID" value="secondarySearchInput"/>
                    <t:putAttribute name="label">${primaryContainerTitle}</t:putAttribute>
                </t:insertTemplate>
                    <c:if test="${mode == 'library'}">
                        <div class="control switch" data-name="showFavoritesOnlySwitch">
                            <input id="favorites-only" type="checkbox">
                            <label class="wrap" for="favorites-only">
                                <div class="label"><spring:message code="SEARCH_SORT_BY_FAVORITES" javaScriptEscape="true"/></div>
                                <div class="track"></div>
                            </label>
                        </div>
                    </c:if>
                    <div class="list buttonSet js-toolbar" role="toolbar" tabindex="0" js-navtype="toolbarV2" aria-controls="resultsList" aria-label="<spring:message code='toolbar.aria.label.toolbarContainer' />">
                        <div class="leaf"><button id="run" tabindex="-1" class="button capsule text up first" js-navtype="none"><span class="wrap"><spring:message code="RM_BUTTON_RUN" javaScriptEscape="true"/></span><span class="icon"></span></button></div>
                        <div class="leaf"><button id="edit" tabindex="-1" class="button capsule text up middle" js-navtype="none"><span class="wrap"><spring:message code="RM_BUTTON_WIZARD" javaScriptEscape="true"/></span><span class="icon"></span></button></div>
                        <div class="leaf"><button id="open" tabindex="-1" class="button capsule text up last" js-navtype="none"><span class="wrap"><spring:message code="RM_BUTTON_OPEN" javaScriptEscape="true"/></span><span class="icon"></span></button></div>
                        <div class="leaf"><button id="copy" tabindex="-1" class="button capsule text up first" js-navtype="none"><span class="wrap"><spring:message code="RM_BUTTON_COPY_RESOURCE" javaScriptEscape="true"/></span><span class="icon"></span></button></div>
                        <div class="leaf"><button id="cut" tabindex="-1" class="button capsule text up middle" js-navtype="none"><span class="wrap"><spring:message code="RM_BUTTON_MOVE_RESOURCE" javaScriptEscape="true"/></span><span class="icon"></span></button></div>
                        <div class="leaf"><button id="paste" tabindex="-1" class="button capsule text up last" js-navtype="none"><span class="wrap"><spring:message code="RM_BUTTON_COPY_HERE" javaScriptEscape="true"/></span><span class="icon"></span></button></div>
                        <div class="leaf"><button id="remove" tabindex="-1" class="button capsule text up" js-navtype="none"><span class="wrap"><spring:message code="SEARCH_BULK_DELETE" javaScriptEscape="true"/></span><span class="icon"></span></button></div>
                    </div>
                    <ul id="sortMode" class=""></ul>
                </div>
                <div class="sub header hidden">
                    <ul id="filterPath" class=""></ul>
                </div>
                <ul role="none" class="list collapsible tabular resources fourColumn header hidden" id="resultsListHeader">
                    <li role="none" class="resources first leaf scheduled" id="resultsListHeader_item1"> <!-- removed id -->
                        <div role="row" aria-selected="false" class="wrap draggable">
                            <div role="none" class="column one">
                                <div role="columnheader" aria-selected="false" class="favorite icon button"></div>
                                <div role="none" class="separator"></div>
                                <div role="columnheader" aria-selected="false" aria-label="<spring:message code="repository.resource.header.schedule" javaScriptEscape="true"/>" class="scheduled icon button"></div>
                                <div role="none" class="separator"></div>
                                <div role="none" class="disclosure icon button"></div>
                            </div>
                            <div role="none" class="column two">
                                <p role="columnheader" aria-selected="false" class="resourceName"><spring:message code="repository.resource.header.name" javaScriptEscape="true"/></p>
                                <p role="columnheader" aria-selected="false" class="resourceDescription"><spring:message code="repository.resource.header.description" javaScriptEscape="true"/></p>
                            </div>
                            <div role="columnheader" aria-selected="false" class="column three resourceType"><spring:message code="repository.resource.header.type" javaScriptEscape="true"/></div>
                                <div role="none" class="column four">
                                <p role="columnheader" aria-selected="false" class="createdDate"><spring:message code="repository.resource.header.createdDate" javaScriptEscape="true"/></p>
                                <p role="columnheader" aria-selected="false" class="modifiedDate"><spring:message code="repository.resource.header.modifiedDate" javaScriptEscape="true"/></p>
                            </div>
                        </div>
                    </li>
                </ul>
            </t:putAttribute>
            <t:putAttribute name="bodyID" value="resultsContainer"/>
            <t:putAttribute name="bodyAttributes">tabindex="-1"</t:putAttribute>
			<!-- Include swipeScroll here -->
            <t:putAttribute name="bodyContent">
                <ul id="resultsList" class="" tabIndex="0" data-tab-index="4" data-component-type="list" js-navtype="dynamiclist"></ul>
                <t:insertTemplate template="/WEB-INF/jsp/templates/nothingToDisplay.jsp">
                    <t:putAttribute name="bodyContent">
                        <div class="header ">
                        <div class="title"></div>
                        </div>
                        <p id="nothingToDisplayMessage" class="message">
                        <span class="jr-mInstructor-icon jr-mIcon jr-mIconXLarge jr-message jr"></span>
                        <span class="message-text">${nothingToDisplayMsg}</span>
                        </p>
                    </t:putAttribute>
                </t:insertTemplate>
            </t:putAttribute>
        </t:insertTemplate>

        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerID" value="searchFilters"/>
            <t:putAttribute name="containerClass" value="column decorated secondary sizeable ${mode == 'browse' ? 'hidden' : ''}"/>
            <t:putAttribute name="containerElements">
                <div class="sizer horizontal"></div>
                <button class="button minimize"></button>
                <div class="icon minimize"></div>
            </t:putAttribute>
            <t:putAttribute name="containerTitle"><spring:message code="SEARCH_FILTERS" javaScriptEscape="true"/></t:putAttribute>
            <%--
            <t:putAttribute name="swipeScroll" value="${isIPad}"/>
             --%>
            <t:putAttribute name="bodyID" value="filtersPanelContent"/>
            <t:putAttribute name="bodyContent">
           		<div id="filtersPanel">
                    <div class="panel open"> <!-- new element -->
                        <div class="header">
                            <button class="buttonIconToggle"></button>
                            <div class="title" role="heading" aria-level="2"><spring:message code="SEARCH_FAVORITE_HEADING" javaScriptEscape="true"/></div>
                        </div>
                        <div class="subcontainer ui-resizable"></div>
                    </div>
                    <div class="panel open"> <!-- new element -->
                        <div class="header">
                            <button class="buttonIconToggle"></button>
                            <div class="title"><spring:message code="SEARCH_ACCESS_HEADING" javaScriptEscape="true"/></div>
                        </div>
                            <div class="subcontainer ui-resizable"></div>
                    </div>

                    <div class="panel open"> <!-- new element -->
                        <div class="header">
                            <button class="buttonIconToggle"></button>
                            <div class="title"><spring:message code="SEARCH_FILE_TYPE_HEADING" javaScriptEscape="true"/></div>
                        </div>
                            <div class="subcontainer ui-resizable"></div>
                    </div>

                    <div class="panel open"> <!-- new element -->
                        <div class="header">
                            <button class="buttonIconToggle"></button>
                            <div class="title"><spring:message code="SEARCH_TIME_HEADING" javaScriptEscape="true"/></div>
                        </div>
                            <div class="subcontainer ui-resizable"></div>
                    </div>

                    <div class="panel open"> <!-- new element -->
                        <div class="header">
                            <button class="buttonIconToggle"></button>
                            <div class="title"><spring:message code="SEARCH_SCHEDULE_HEADING" javaScriptEscape="true"/></div>
                        </div>
                            <div class="subcontainer ui-resizable"></div>
                    </div>
                </div>
            </t:putAttribute>
        </t:insertTemplate>

        <jsp:include page="searchComponents.jsp"/>


        <t:insertTemplate template="/WEB-INF/jsp/templates/standardConfirm.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="leftButtonId" value="deleteResourceOK"/>
            <t:putAttribute name="rightButtonId" value="deleteResourceCancel"/>
        </t:insertTemplate>
    </t:putAttribute>
</t:insertTemplate>
