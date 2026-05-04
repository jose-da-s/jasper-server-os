<%@ page contentType="text/html; charset=utf-8" %>
    <%--
      ~ Copyright (C) 2005 - 2022 TIBCO Software Inc. All Rights Reserved. Confidential & Proprietary.
      ~ Licensed pursuant to commercial TIBCO End User License Agreement.
      --%>

    <%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

    <%@ page import="com.jaspersoft.commons.semantic.ConfigurationObject" %>

    <t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
		<t:putAttribute name="showServerSettingsHeader" value="true"/>
        <t:putAttribute name="pageTitle"><spring:message code="menu.general"/></div></t:putAttribute>
        <t:putAttribute name="bodyID" value="generalServerSettings"/>
        <t:putAttribute name="bodyClass" value="twoColumn"/>
        <t:putAttribute name="moduleName" value="administer/administerGeneralSettingsMain"/>
        <t:putAttribute name="headerContent">
            <%@ include file="administerState.jsp" %>
        </t:putAttribute>
        <t:putAttribute name="bodyContent">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerID" value="settings"/>
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="bodyClass" value=""/>
                <t:putAttribute name="bodyContent">
                    <div class="title"><spring:message code="menu.general"/></div>
                    <div class="generalSettings"></div>
                    <input type="hidden"  id="_flowExecutionKey" name="_flowExecutionKey" value="${flowExecutionKey}"/>
                    <input type="hidden"  id="_allowJarFileUpload" name="_allowJarFileUpload" value="${allowJarFileUpload}"/>
                </t:putAttribute>
                <t:putAttribute name="footerContent">
                </t:putAttribute>
            </t:insertTemplate>

            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerID" value="serverSettingsMenu"/>
                <t:putAttribute name="containerClass" value="column decorated secondary sizeable"/>
                <t:putAttribute name="containerElements">
                    <div class="sizer horizontal"></div>
                    <button class="button minimize"></button>
                </t:putAttribute>
                <t:putAttribute name="containerTitle"><spring:message code="menu.settings"/></t:putAttribute>
                <t:putAttribute name="bodyClass" value=""/>
                <t:putAttribute name="bodyContent">
                    <!--
                    NOTE: these objects serve as navigation links, load respective pages
                    -->
                    <ul class="list responsive filters">
						<li class="leaf selected"><p class="wrap button" id="navGeneralSettings"><b class="icon"></b><spring:message code="menu.general"/></p></li>
						<li class="leaf"><p class="wrap button" id="navLogSettings"><b class="icon"></b><spring:message code="menu.log.Settings"/></p></li>
						<li class="leaf"><p class="wrap button" id="logCollectors"><b class="icon"></b><spring:message code="logCollectors.title"/></p></li>
						<li class="leaf"><p class="wrap button" id="navDesignerOptions"><b class="icon"></b><spring:message code="menu.adhoc.options"/></p></li>
						<li class="leaf"><p class="wrap button" id="navDesignerCache"><b class="icon"></b><spring:message code="menu.adhoc.cache"/></p></li>
						<li class="leaf"><p class="wrap button" id="navAnalysisOptions"><b class="icon"></b><spring:message code="menu.mondrian.properties"/></p></li>
						<li class="leaf"><p class="wrap button" id="navAwsSettings"><b class="icon"></b><spring:message code="menu.aws.settings"/></p></li>
						<li class="leaf"><p class="wrap button" id="navCustomAttributes"><b class="icon"></b><spring:message code="menu.server.attributes"/></p></li>
						<li class="leaf"><p class="wrap button" id="navResetSettings"><b class="icon"></b><spring:message code="menu.edit.settings"/></p></li>
						<li class="leaf"><p class="wrap button" id="navImport"><b class="icon"></b><spring:message code="import.import"/></p></li>
						<li class="leaf"><p class="wrap button" id="navExport"><b class="icon"></b><spring:message code="export.export"/></p></li>
						<li class="leaf" disabled="disabled"><p class="wrap separator" href="#"><b class="icon"></b></p></li>
                    </ul>
                </t:putAttribute>
                <t:putAttribute name="footerContent">
                </t:putAttribute>
            </t:insertTemplate>

        </t:putAttribute>

    </t:insertTemplate>