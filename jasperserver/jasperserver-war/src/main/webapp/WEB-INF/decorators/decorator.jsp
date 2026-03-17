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

<%
    response.setHeader("P3P","CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/WEB-INF/jasperserver.tld" prefix="js" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<%@ page import="com.jaspersoft.jasperserver.api.engine.common.service.impl.NavigationActionModelSupport" %>

<html lang="${fn:replace(userLocale, "_", "-")}">
<head>
    <title><spring:message code="company.name"/>: <decorator:title /></title>
        <meta http-equiv="Content-Type" content="text/html; charset=${requestScope['com.jaspersoft.ji.characterEncoding']}">

        <!--
        <meta http-equiv="X-UA-Compatible" content="IE=8"/>
        -->
        <link rel="shortcut icon" href='${pageContext.request.contextPath}/<spring:theme code="images/favicon.ico" />' />

        <!--
            <link rel="shortcut icon" href="favicon.ico" />
        -->
        <%@ include file="decoratorCommonImports.jsp" %>
        <decorator:head />
    <%@ include file="decoratorEndingImports.jsp" %>
    </head>

<%@ include file="../jsp/modules/common/jsEdition.jsp" %>
<%@ include file="../jsp/modules/common/jsEnvType.jsp" %>

<c:set var="decorator_body_id" scope="page">
    <decorator:getProperty property='body.id'/>
</c:set>
<body id="${decorator_body_id}" class="<decorator:getProperty property='body.class'/>" js-stdnav="true">

    <c:if test="${decorator_body_id != 'loginPage'}">
        <a href="#maincontent" class="skipLink" js-stdnav="false"><spring:message code="link.label.skipLink"/></a>
    </c:if>
    <div class="offLeft" id="stdnavAlert" role="alert" aria-live="assertive"></div>
    <div class="offLeft" id="stdnavInfo" role="region" aria-live="polite"></div>
    <!-- Internet Explorer Countermeasures.  This element is used to force DOM changes to cause user-agent logic to re-evaluate the DOM. -->
    <div id="IECM" aria-hidden="true" style="display:block; height:0px; background-color:transparent;">&nbsp;&nbsp;&nbsp;</div>
    <header id="banner" class="banner" style="<c:if test="${param['frame'] == 0}">display:none;</c:if>">
        <div id="systemMessageConsole" style="display:none">
            <p id="systemMessage"><spring:message code="button.close"/></p>
        </div>
        <div id="logo" class="sectionLeft" aria-label="<spring:message code="logo.label"/>" role="img"></div>
        <c:if test="${pageProperties['meta.noMenu']==null}">
            <nav class="sectionLeft" aria-label="<spring:message code='menu.name.main'/>">
                <ul
                        id="mainNavigation"
                        tabindex="0"
                        class="menuRoot menu horizontal primaryNav js-navigationOptions"
                        js-navtype="mainmenu"
                        role="menubar"
                        aria-orientation="horizontal"
                        aria-label="<spring:message code='menu.name.main'/>"
                >
                    <li id="main_home" role="none" class="leaf hidden" data-title="true" title="<spring:message code="menu.home"/>">
                        <p id="main_home_label" role="menuitem" aria-label="<spring:message code="menu.home"/>"
                           class="wrap button"><span class="icon"></span></p>
                    </li>
                    <c:if test='<%= !((NavigationActionModelSupport)application.getAttribute("concreteNavigationActionModelSupport")).banUserRole() %>'>
                        <li id="main_library" role="none" class="leaf hidden">
                            <p id="main_library_label" role="menuitem" class="wrap button"><span class="icon"></span><spring:message code="menu.library"/></p>
                        </li>
                    </c:if>
                </ul>
            </nav>
        </c:if>
        <div class="sectionRight">
            <div style="float:left;">
                <nav aria-label="<spring:message code='menu.name.user'/>">
                    <authz:authorize access="!hasRole('ROLE_ANONYMOUS')">
                    <ul
                            id="metaLinks"
                            class="horizontal"
                            role="menubar"
                            tabindex="0"
                            aria-label="<spring:message code='menu.name.user'/>"
                            aria-orientation="horizontal"
                    >
                        <li id="userID" role="menuitem" aria-disabled="true">
                            <span id="casted">
                                <c:if test="<%= com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserAuthorityServiceImpl.isUserSwitched() %>">
                                    <c:set var="principalUserFullName" value="<%= ((com.jaspersoft.jasperserver.api.metadata.user.domain.User)
                                            com.jaspersoft.jasperserver.api.metadata.user.service.impl.UserAuthorityServiceImpl.
                                                    getSourceAuthentication().getPrincipal()).getFullName() %>"/>
                                    ${principalUserFullName}
                                    <spring:message code="jsp.main.as"/>
                                </c:if>
                            </span>
                            <authz:authentication property="principal.fullName"/>
                        </li>
                    <c:set var="isShowHelp" scope="page"><%= WebHelpLookup.getInstance().isShowHelpTrue() %></c:set>
                        <c:if test="${isProVersion && isShowHelp}">
                            <li id="help" role="menuitem">
                                <a href="#" id="helpLink" tabindex="-1">
                                    <spring:message code="decorator.helpLink"/>
                                </a>
                            </li>
                        </c:if>
                        <li id="main_logOut" class="last" role="menuitem">
                            <a id="main_logOut_link" tabindex="-1">
                                <spring:message code="menu.logout"/>
                            </a>
                            <form id="exitUser" action="/exituser.html"></form>
                        </li>
                    </ul>
                    </authz:authorize>
                </nav>
            </div>
            <div class="searchContainer" style="float:left;">
                <!-- banner search -->
                <t:insertTemplate template="/WEB-INF/jsp/templates/control_searchLockup.jsp">
                    <t:putAttribute name="containerID" value="globalSearch"/>
                    <t:putAttribute name="inputID" value="searchInput"/>
                    <t:putAttribute name="label"><spring:message code='search.main.region' javaScriptEscape='true'/></t:putAttribute>
                </t:insertTemplate>
            </div>
        </div>
    </header>

    <main id="frame" style="<c:if test="${param['frame'] == 0}">top:0;bottom:0;</c:if>">
        <div class="content">
            <decorator:body />
        </div>
    </main>

    <footer id="frameFooter" style="<c:if test="${param['frame'] == 0}">display:none;</c:if>">
        <p id="about">
            <a href="#" role="button"><spring:message code="decorator.aboutLink"/></a>
            <c:if test="${isDevelopmentEnvironmentType}">
                    <span id="license">
                        (<spring:message code="LIC_023_license.envtype.development.label"/>)
                    </span>
            </c:if>
        </p>
        <p id="copyright"><spring:message code="decorators.main.copyright"/></p>
    </footer>

    <div id="templateElements" role="region" aria-label="<spring:message code='templates.region.name'/>">
        <%@ include file="decoratorMinimalComponents.jsp" %>
        <%@ include file="../jsp/modules/commonJSTLScripts.jsp" %>
        <div class="hidden">
            <p class="action">&nbsp;</p>
            <p class="action over">&nbsp;</p>
            <p class="action pressed">&nbsp;</p>
            <p class="action primary">&nbsp;</p>
            <p class="action primary over">&nbsp;</p>
            <p class="action primary pressed">&nbsp;</p>
        </div>
    </div>
    <div id="tooltipsContainer" role="region" aria-label="<spring:message code='tooltip.aria.label.tooltipContainer'/>"></div>
</body>

</html>
