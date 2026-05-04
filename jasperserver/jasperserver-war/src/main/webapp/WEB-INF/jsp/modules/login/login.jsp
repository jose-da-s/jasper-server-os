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

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<%@ include file="../common/jsEdition.jsp" %>
<%@ page import="java.util.Random" %>

<c:choose>
    <c:when test="${isAwsMpProduct}">
        <c:set var="jsEditionClass" value="amazon"/>
    </c:when>
    <c:when test="${isProVersion}">
        <c:set var="jsEditionClass" value="pro"/>
    </c:when>
    <c:otherwise>
        <c:set var="jsEditionClass" value="community"/>
    </c:otherwise>
</c:choose>

<%-- Rotating page count. --%>
<c:set var='rotatingPageCount' value='1'/>

<%-- Random rotating page number. --%>
<c:set var='randomRotatingPageNumber' value='<%=new Random().nextInt(Integer.parseInt(pageContext.getAttribute("rotatingPageCount").toString()))%>'/>
<% response.setHeader("LoginRequested","true");
    session.removeAttribute("js_uname");
    session.removeAttribute("js_upassword");
%>

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle"><spring:message code='jsp.Login.title'/></t:putAttribute>
    <t:putAttribute name="moduleName" value="login/loginMain"/>
    <t:putAttribute name="headerContent">

        <meta name="noMenu" content="true">
        <meta name="pageHeading" content="<spring:message code='jsp.Login.pageHeading'/>"/>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="loginPage"/>
    <t:putAttribute name="bodyClass">oneColumn ${jsEditionClass}</t:putAttribute>
    <t:putAttribute name="bodyContent" >

        <!--[if IE 6]>
        <script type="text/javascript">
        alert("<spring:message code="LOGIN_UNSUPPORTED_BROWSER" javaScriptEscape="true"/>");
        </script>
        <![endif]-->

        <c:set var='showPasswordChange' value='${param.showPasswordChange}'/>
        <c:set var='weakPassword' value='<%=request.getParameter("weakPassword")%>'/>

        <div class="wrapper">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="panel info"/>
            <t:putAttribute name="containerID" value="copy"/>
            <t:putAttribute name="bodyContent">
                <div id="welcome" class="row">
                    <h1>
                        <span class="logo" aria-label="Jaspersoft"></span>
                        <span class="text"><spring:message code='LOGIN_WELCOME_OS'/></span>
                    </h1>
                </div>
                <div  id="rotating" class="row">
                    <jsp:include page="rotating/login_rotating_${jsEditionClass}_${randomRotatingPageNumber}.jsp"/>
                </div>
                <ul
                        id="metaLinks"
                        class="horizontal"
                        role="menubar"
                        tabindex="0"
                        aria-label="<spring:message code='menu.name.user'/>"
                        aria-orientation="horizontal"
                >
                    <li id="help" role="menuitem">
                        <a href="#" id="helpLink" tabindex="-1">
                            <spring:message code="decorator.helpLink"/>
                        </a>
                    </li>
                </ul>
<%--
                    <div id="buttons" class="row">
                        <div class="primary">
                            <c:choose>
                                <c:when test="${isProVersion}">
                                    <button id="documentationButton" class="button action primary up"><span class="wrap"><spring:message code='BUTTON_DOCUMENTATION'/></span><span class="icon"></span></button>
                                </c:when>
                                <c:otherwise>
                                    <button id="gotoJasperForge" class="button action primary up"><span class="wrap"><spring:message code='BUTTON_GOTO_JASPERFORGE'/></span><span class="icon"></span></button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="secondary">
                            <button id="contactSalesButton" class="button action primary up"><span class="wrap"><spring:message code='BUTTON_CONTACT_SALES'/></span><span class="icon"></span></button>
                        </div>
                    </div>
--%>
                </t:putAttribute>
            </t:insertTemplate>

            <form id="loginForm" method="POST" action="j_spring_security_check" js-navtype="none"
                  <c:if test="${autoCompleteLoginForm == 'false'}">autocomplete="off"</c:if>>
                <t:insertTemplate template="/WEB-INF/jsp/templates/login.jsp">
                    <t:putAttribute name="jsEdition">${jsEditionClass}</t:putAttribute>
                    <t:putAttribute name="allowUserPasswordChange" value="${allowUserPasswordChange}"/>
                    <t:putAttribute name="showPasswordChange" value="${showPasswordChange}"/>
                    <t:putAttribute name="warningMessages">
                        <c:if test="${isDevelopmentEnvironmentType}">
                            <p class="warning"><b><spring:message code="LIC_023_license.envtype.development.message"/></b></p>
                        </c:if>
                    </t:putAttribute>
                    <t:putAttribute name="errorMessages">
                        <c:choose>
                            <c:when test="${isProVersion}">
                                <c:choose>
                                    <c:when test="${usersExceeded && banUser}">
                                        <p class="errorMessage">
                                            <spring:message code="LIC_017_license.block.user"/>
                                        </p>
                                    </c:when>
                                    <c:when test="${usersExceeded && !banUser}">
                                        <p class="errorMessage">
                                            <spring:message code="LIC_017_license.user.count.exceeded.login.box"/>
                                        </p>
                                    </c:when>
                                </c:choose>
                            </c:when>
                        </c:choose>
                        <c:if test="${paramValues.error != null && !usersExceeded && !banUser}">
                            <c:choose>
                                <c:when test="${exception!=null}">
                                    <p class="errorMessage">${exception}</p>
                                </c:when>
                                <c:otherwise>
                                    <p class="errorMessage"><spring:message code='jsp.loginError.invalidCredentials1'/></p>
                                    <p class="errorMessage"><spring:message code='jsp.loginError.invalidCredentials2'/></p>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <c:if test="${userPrincipal != null && isUserLockFeatureEnabled}">
                            <c:choose>
                                <c:when test="${isUserLocked}">
                                    <p class="errorMessage"><spring:message code='jsp.loginError.userLockedMessage'/></p>
                                </c:when>
                                <c:otherwise>
                                    <p class="errorMessage"><spring:message code='jsp.loginError.remainingLoginAttempts'/> : ${sessionScope.numberOfLoginAttemptsRemaining}</p>
                                </c:otherwise>
                            </c:choose>
                        </c:if>

                        <c:if test="${showPasswordChange eq 'true' and weakPassword != 'true'}">
                            <p class="errorMessage"><spring:message code='jsp.loginError.expiredPassword1'/></p>
                            <p class="errorMessage"><spring:message code='jsp.loginError.expiredPassword2'/></p>
                        </c:if>
                        <c:if test="${weakPassword eq 'true'}">
                             <p class="errorMessage"><spring:message code='exception.remote.weak.password'/></p>
                        </c:if>
                        <p id="customError" class="errorMessage hidden"></p>
                    </t:putAttribute>
                    <t:putAttribute name="localeOptions">
                        <c:forEach items="${userLocales}" var="locale">
                            <option value="${locale.code}" <c:if test="${preferredLocale == locale.code}">selected</c:if>>
                                <spring:message code="locale.option" arguments='${locale.code},${locale.description}'/>
                            </option>
                        </c:forEach>
                    </t:putAttribute>
                    <t:putAttribute name="timezoneOptions">
                        <c:forEach items="${userTimezones}" var="timezone">
                            <option value="${timezone.code}" <c:if test="${preferredTimezone == timezone.code}">selected</c:if>>
                                <spring:message code="timezone.option" arguments='${timezone.code},${timezone.description}'/>
                            </option>
                        </c:forEach>
                    </t:putAttribute>
                </t:insertTemplate>
                <%-- MOVED TO LOGIN.JSP TEMPLATE FILE
                    <c:if test="${isProVersion and isEC2}">
                        <div id="amazonLogo" class="row"></div>
                    </c:if>
                --%>
            </form>
        </div>

        <jsp:include page="${isProVersion and isAwsMpProduct ? 'loginHelpAws.jsp' : 'loginHelp.jsp'}" />

        <jsp:include page="loginState.jsp"/>
    </t:putAttribute>
</t:insertTemplate>
