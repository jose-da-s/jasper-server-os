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

<%--
Overview:
    Usage:permit user to add a system created object to the repository.

Usage:

    <t:insertTemplate template="/WEB-INF/jsp/templates/generatorSelect.jsp">
    </t:insertTemplate>

--%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<tx:useAttribute id="containerID" name="containerID" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>


<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <t:putAttribute name="containerID" value="${containerID}"/>
    <t:putAttribute name="containerClass" value="control ${containerClass}"/>
    <t:putAttribute name="bodyContent">
        <fieldset class="group locationSelector">
            <legend class=""><span><spring:message code="GENERATOR_SELECT_LEGEND"/></span></legend>
            <ul class="list locations">
                <li class="leaf">
                    <div class="control radio">
                        <label class="wrap" for="${containerID}DefaultTemplateRadio" title="<spring:message code="GENERATOR_SELECT_DEFAULT_TEMPLATE_LABEL"/>">
                            <spring:message code="GENERATOR_SELECT_DEFAULT_TEMPLATE"/>
                        </label>
                        <input tabindex="0" id="${containerID}DefaultTemplateRadio" type="radio" name="${containerID}" checked="checked" aria-label="<spring:message code="GENERATOR_SELECT_DEFAULT_TEMPLATE"/>" />
                        <span class="message warning">error message here</span>
                    </div>
                </li>
                <li class="leaf">
                    <div class="control radio complex">
                        <label class="wrap" for="${containerID}ReportTemplateRadio" title="<spring:message code="GENERATOR_SELECT_DEFAULT_CUSTOM_LABEL"/>">
                            <spring:message code="GENERATOR_SELECT_CUSTOM_TEMPLATE"/>
                        </label>
                        <!-- ID of radio should match to ID of input to without "Radio" prefix -->
                        <input tabindex="0" id="${containerID}ReportTemplateRadio" type="radio" name="${containerID}" aria-label="<spring:message code="GENERATOR_SELECT_CUSTOM_TEMPLATE"/>"/>
                        <span class="message warning">error message here</span>
                    </div>
                    <div id="browseTemplate" class="control input file" for="filePath" title="<spring:message code="GENERATOR_SELECT_DEFAULT_CUSTOM_LABEL"/>">
                        <label class="control browser" for="${containerID}ReportTemplate" title="Define a path in the repository">
                            <span class="wrap"></span>
                            <!-- ID of input should match to ID of radio without "Radio" prefix -->
                            <input class="" id="${containerID}ReportTemplate" type="text" value=""/>
                            <button class="button action up" id="customTemplateBrowser" type="button">
                                <span class="wrap"><spring:message code="button.browse"/></span>
                                <span class="icon"></span>
                            </button>
                            <span class="message warning">error message here</span>
                            <ul id="${containerID}DefaultTemplate" class="responsive collapsible folders hideRoot"></ul>
                        </label>
                    </div>
                </li>
                <li class="leaf">
                    <div class="control radio">
                        <label class="wrap" for="${containerID}ReportGeneratorRadio" title="<spring:message code="GENERATOR_SELECT_DEFAULT_CUSTOM_GENERATOR_LABEL"/>">
                            <spring:message code="GENERATOR_SELECT_CUSTOM_GENERATOR"/>
                        </label>
                        <!-- ID of radio should match to ID of select without "Radio" prefix -->
                        <input id="${containerID}ReportGeneratorRadio" type="radio" name="${containerID}" data-innput-control/>
                        <span class="message warning">error message here</span>
                    </div>
                    <div id="selectGeneratorControl" class="control input">
                        <label class="control browser" for="${containerID}ReportGenerator" title="<spring:message code="GENERATOR_SELECT_DEFAULT_CUSTOM_GENERATOR_LABEL"/>">
                            <span class="wrap"></span>
                            <!-- ID of select should match to ID of radio without "Radio" prefix -->
                            <select id="${containerID}ReportGenerator"></select>
                            <span class="message warning">error message here</span>
                        </label>
                    </div>
                </li>
            </ul>
        </fieldset>

    </t:putAttribute>
</t:insertTemplate>

<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <t:putAttribute name="containerAttributes" value="role='dialog' aria-labelledby='${containerID}TemplateDialogTitle' aria-modal='true'"/>
    <t:putAttribute name="containerClass">panel dialog overlay detail centered_horz centered_vert moveable hidden sizeable</t:putAttribute>
    <t:putAttribute name="containerID" value="${containerID}TemplateDialog"/>
    <t:putAttribute name="containerElements"><div class="sizer diagonal"></div></t:putAttribute>
    <t:putAttribute name="headerClass" value="mover"/>
    <t:putAttribute name="containerTitle">
        <div id="${containerID}TemplateDialogTitle"><spring:message code="ADH_1015_TEMPLATE_PROPERTIES_DIALOG_TITLE"/></div>
    </t:putAttribute>
    <t:putAttribute name="bodyContent" cascade="true">
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="control groupBox"/>
            <t:putAttribute name="bodyContent">
                <ul class="responsive collapsible folders hideRoot" id="${containerID}TemplateDialogTree"></ul>
            </t:putAttribute>
        </t:insertTemplate>
    </t:putAttribute>
    <t:putAttribute name="footerContent">
        <button id="${containerID}TemplateDialogOkTemplate" class="button action primary up"><span class="wrap"><spring:message code="button.ok" javaScriptEscape="true"/></span><span class="icon"></span></button>
        <button id="${containerID}TemplateDialogCloseTemplate" class="button action up"><span class="wrap"><spring:message code="button.close" javaScriptEscape="true"/></span><span class="icon"></span></button>
    </t:putAttribute>
</t:insertTemplate>
