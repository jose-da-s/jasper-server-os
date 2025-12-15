<%@ page contentType="text/html; charset=utf-8" %>
<%--
  ~ Copyright (C) 2025 the Jasper Server OS Authors
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
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ include file="../common/jsEdition.jsp" %>


<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
  <t:putAttribute name="showServerSettingsHeader" value="true"/>
  <t:putAttribute name="pageTitle"><spring:message code="export.export"/></t:putAttribute>
  <t:putAttribute name="bodyID" value="exportDataFile"/>
  <t:putAttribute name="bodyClass" value="twoColumn"/>
  <t:putAttribute name="moduleName" value="administer/administerExportMain"/>

  <t:putAttribute name="headerContent">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/<spring:theme code="importExport.css"/>" type="text/css" media="screen,print"/>
  </t:putAttribute>
  <t:putAttribute name="bodyContent">
    <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
      <t:putAttribute name="containerID" value="settings"/>
      <t:putAttribute name="containerClass" value="column decorated primary"/>

      <t:putAttribute name="bodyClass" value=""/>
      <t:putAttribute name="bodyContent">
        <div class="title"></b><spring:message code="export.export"/></div>
			</t:putAttribute>
      <t:putAttribute name="footerContent">
        <button id="exportButton" class="button action primary up">
          <span class="wrap"><spring:message code="export.export"/></span>
          <span class="icon"></span>
        </button>
      </t:putAttribute>
		</t:insertTemplate>

    <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
      <t:putAttribute name="containerID" value="serverSettingsMenu"/>
      <t:putAttribute name="containerClass">
        column decorated secondary sizeable
        <t:putAttribute name="containerElements">
          <div class="sizer horizontal"></div>
          <button class="button minimize"></button>
        </t:putAttribute>
      </t:putAttribute>
      <t:putAttribute name="containerTitle"><spring:message code="menu.settings"/></t:putAttribute>
      <t:putAttribute name="bodyClass" value=""/>
      <t:putAttribute name="bodyContent">
        <ul class="list responsive filters">
          <c:if test="${isProVersion}">
            <li class="leaf"><p class="wrap button" id="navGeneralSettings"><b class="icon"></b><spring:message code="menu.general"/></p></li>
          </c:if>
          <li class="leaf"><p class="wrap button" id="navLogSettings"><b class="icon"></b><spring:message code="menu.log.Settings"/></p></li>
          <c:if test="${isProVersion}">
            <li class="leaf"><p class="wrap button" id="logCollectors"><b class="icon"></b><spring:message code="logCollectors.title"/></p></li>
          </c:if>
          <c:if test="${isProVersion}">
            <li class="leaf"><p class="wrap button" id="navDesignerOptions"><b class="icon"></b><spring:message code="menu.adhoc.options"/></p></li>
            <li class="leaf"><p class="wrap button" id="navDesignerCache"><b class="icon"></b><spring:message code="menu.adhoc.cache"/></p></li>
          </c:if>
          <li class="leaf"><p class="wrap button" id="navAwsSettings"><b class="icon"></b><spring:message code="menu.aws.settings"/></p></li>
          <li class="leaf"><p class="wrap button" id="navCustomAttributes"><b class="icon"></b><spring:message code="menu.server.attributes"/></p></li>
          <li class="leaf"><p class="wrap button" id="navResetSettings"><b class="icon"></b><spring:message code="menu.edit.settings"/></p></li>
          <li class="leaf"><p class="wrap button" id="navImport"><b class="icon"></b><spring:message code="import.import"/></p></li>
          <li class="leaf selected"><p class="wrap button" id="navExport"><b class="icon"></b><spring:message code="export.export"/></p></li>
          <li class="leaf" disabled="disabled"><p class="wrap separator" href="#"><b class="icon"></b></p></li>
        </ul>
      </t:putAttribute>
      <t:putAttribute name="footerContent"></t:putAttribute>
    </t:insertTemplate>
  </t:putAttribute>
</t:insertTemplate>
