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
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>
<!--
*** DEVELOPMENT NOTES ***

MANAGING THE LIST OF RESOURCES AND CONTROLS
 - Basic interaction is that, initially, there are 4 rows in list
   -- for resources, a header row and an Add row
   -- for input controls, a header row and an Add row
 - If a resource is added, then a row for that file is added ABOVE the Add row
 - If an input control is added, then a row for this control is added ABOVE of the Add row
 - The Add rows are ALWAYS present for both types of objects
 - Clicking the name of an added object embeds the wizard for that object within the current flow
   - the add report navigation is retained at left
   - the input UI is replaced by appropriate one for the object
   - the footer buttons become previous/cancel/next to permit user to step through wizard-within-the-flow
   - if cancel is invoked on a wizard-within-the-flow, then the wizard is cancelled but the flow is not
 - Clicking Remove link immediately removes file, does NOT ask for confirmation
   - use #systemConfirm to message that change has occurred
   - message of the form '[resource name] removed.'

PROVIDING FEEDBACK TO USER

DIFFERENCES BETWEEN ADD AND EDIT VERSIONS OF THIS PAGE

FINALLY
 Do not include these notes, or any HTML comment below that begins 'NOTE: ...' in the production page
-->
<c:set var="canChangeResources" value="${wrapper.reportUnit.mainReport.localResource.name != 'adhocJRXML'}"/>
<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle">
        <c:choose>
            <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/></c:when>
            <c:otherwise><spring:message code="resource.report.title"/></c:otherwise>
        </c:choose>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="addReport_Controls"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow"/>
    <t:putAttribute name="moduleName" value="addResource/jasperReport/addJasperReportResourcesAndControlsMain"/>

    <t:putAttribute name="headerContent">

        <jsp:include page="reportStep3State.jsp"/>

        <jsp:include page="/WEB-INF/jsp/templates/addJasperReportResourcesAndControls.jsp"/>

        <script type="text/javascript">
            <js:out javaScriptEscape="true">
            if (typeof __jrsConfigs__.addJasperReport === "undefined") {
                __jrsConfigs__.addJasperReport = {};
            }

            __jrsConfigs__.addJasperReport.resource = resource;

            <c:if test="${canChangeResources}">
            __jrsConfigs__.addJasperReport.canChangeResources = true;
            </c:if>

            var nonSuggestedResources = [];
            <c:forEach items="${wrapper.reportUnit.resources}" var="res">
                <c:set var="suggested" value="false"/>
                <c:forEach items="${wrapper.suggestedResources}" var="resWrap">
                    <c:if test="${resWrap.fileResource.name==res.localResource.name}">
                        <c:set var="suggested" value="true"/>
                    </c:if>
                </c:forEach>
                <c:if test="${!suggested}">
                    nonSuggestedResources.push({
                        name: "${res.localResource.name}",
                        canChangeResources: __jrsConfigs__.addJasperReport.canChangeResources,
                        fileType: "<c:choose><c:when test='${res.localResource.fileType!=null}'>${allTypes[res.localResource.fileType]}</c:when><c:otherwise><spring:message code="jsp.listResources.resource"/></c:otherwise></c:choose>"
                    });
                </c:if>
            </c:forEach>
            __jrsConfigs__.addJasperReport.nonSuggestedResources = nonSuggestedResources;

            var suggestedResources = [];
            <c:forEach items="${wrapper.suggestedResources}" var="resWrap">
                suggestedResources.push({
                    name: "${resWrap.fileResource.name}",
                    label: "${resWrap.fileResource.label}",
                    located: ${resWrap.located}
                });
            </c:forEach>
            __jrsConfigs__.addJasperReport.suggestedResources = suggestedResources;

            var nonSuggestedControls = [];
            <c:forEach items="${wrapper.reportUnit.inputControls}" var="control">
                <c:set var="suggested" value="false"/>
                <c:forEach items="${wrapper.suggestedControls}" var="contWrap">
                    <c:if test="${contWrap.inputControl.name==control.localResource.name}">
                        <c:set var="suggested" value="true"/>
                    </c:if>
                </c:forEach>
                <c:if test="${!suggested}">
                    var nonSuggestedControl = {
                        local: ${control.local},
                        canChangeResources: __jrsConfigs__.addJasperReport.canChangeResources,
                        type: "${inputControlWrapper.supportedControlTypes[control.localResource.type]}"
                    };

                    <c:if test="${control.local}">
                        nonSuggestedControl.name = "${control.localResource.name}";
                        nonSuggestedControl.label = "${control.localResource.label}";
                    </c:if>

                    <c:if test="${!control.local}">
                        nonSuggestedControl.referenceURI = "${control.referenceURI}";
                    </c:if>

                    nonSuggestedControls.push(nonSuggestedControl);
                </c:if>
            </c:forEach>
            __jrsConfigs__.addJasperReport.nonSuggestedControls = nonSuggestedControls;

        var suggestedControls = [];
        <c:forEach items="${wrapper.suggestedControls}" var="contWrap">
            suggestedControls.push({
                name: "${contWrap.inputControl.name}",
                label: "${contWrap.inputControl.label}",
                type: "${contWrap.supportedControlTypes[contWrap.inputControl.inputControlType]}",
                located: ${contWrap.located}
            });
        </c:forEach>
        __jrsConfigs__.addJasperReport.suggestedControls = suggestedControls;

            </js:out>
        </script>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <form method="post" action="flow.html">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="containerTitle">
                    <c:choose>
                        <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/>:</c:when>
                        <c:otherwise><spring:message code="resource.report.title"/>:</c:otherwise>
                    </c:choose>
                    ${wrapper.reportUnit.label}
                </t:putAttribute>

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                    <div id="flowControls">
                        <ul class="control tabSet buttons vertical">
                            <li class="tab first">
                                <button name="_eventId_reportNaming" type="submit" class="button up" id="steps1_2" ><span class="wrap"><spring:message code="resource.report.setup"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab selected">
                                <!-- NOTE: tabs below are disabled until required information is entered on this page -->
                                <button name="_eventId_resources" type="submit" class="button up" id="step3"><span class="wrap"><spring:message code="resource.report.controlsAndReources"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab">
                                <button name="_eventId_dataSource" type="submit" class="button up" id="step4"><span class="wrap"><spring:message code="resource.report.dataSource"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab">
                                <button name="_eventId_query" type="submit" class="button up" id="step5"><span class="wrap"><spring:message code="resource.report.query"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab last">
                                <button name="_eventId_customization" type="submit" class="button up" id="step6"><span class="wrap"><spring:message code="resource.report.customization"/></span></button>
                            </li>
                            <!--/.tab-->
                        </ul>
                        <!--/.control-->
                    </div>

                    <div id="stepDisplay">
                        <input type="text" style="visibility:hidden;" name="resourceName" id="resourceName">

                        <input type="submit" style="visibility:hidden;" value="EditResource" name="_eventId_EditResource" id="editResourceButton">
                        <input type="submit" style="visibility:hidden;" value="RemoveResource" name="_eventId_RemoveResource" id="removeResourceButton">
                        <input type="submit" style="display:none;" value="AddResource" name="_eventId_AddResource" id="addResourceButton"/>

                        <input type="submit" style="visibility:hidden;" value="EditControl" name="_eventId_EditControl" id="editControlButton">
                        <input type="submit" style="visibility:hidden;" value="RemoveControl" name="_eventId_RemoveControl" id="removeControlButton">
                        <input type="submit" style="display:none;" value="AddControl" name="_eventId_AddControl" id="addControlButton">
                        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>

                        <spring:bind path="wrapper.validationMessage">
                            <fieldset class="row instructions<c:if test='${status.error}'> error</c:if>">
                                <legend class="offLeft"><span><spring:message code="resource.report.instructions"/></span></legend>
                                <h2 class="textAccent02"><spring:message code="resource.report.controlsAndReources"/></h2>
                                <h4><spring:message code="resource.report.locateControlsAndResources"/></h4>
                                    <c:if test="${status.error}">
                                        <p class="message warning">${status.errorMessage}</p>
                                    </c:if>
                            </fieldset>
                        </spring:bind>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.report.inputs"/></span></legend>
                                <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                    <t:putAttribute name="containerClass" value="column noHeader primary"/>

                                    <t:putAttribute name="bodyContent">
                                        <ul class="list setLeft tabular linkedResources threeColumn">
                                            <li id="resources" class="node">
                                                <div class="wrap header"><b class="icon" title=""></b><p class="column one"><spring:message code="resource.report.resources"/></p><p class="column two"></p><p class="column three"></p></div>
                                                <ul></ul>
                                            </li>
                                            <li id="controls" class="node">
                                                <div class="wrap header"><b class="icon" title=""></b>
                                                    <p class="column one"><spring:message code="resource.report.inputControls"/></p>
                                                    <p class="column two"></p>
                                                    <p class="column three"></p>
                                                </div>
                                                <ul></ul>
                                            </li>

                                            <li id="controlOptions" class="node">
                                            <!-- NOTE: this node hidden, unless an input control has been linked to the report -->
                                                <div class="wrap header"><b class="icon" title=""></b><p class="column one"><spring:message code="resource.report.options"/></p><p class="column two"></p><p class="column three"></p></div>
                                                <ul class="">
                                                    <li class="leaf">
                                                        <div class="wrap">
                                                            <spring:bind path="wrapper.reportUnit.controlsLayout">
                                                                <label class="control select" for="${status.expression}" title="<spring:message code='resource.report.mode'/>">
                                                                    <span class="wrap"><spring:message code="resource.report.mode"/>:</span>
                                                                    <select id="${status.expression}" name="${status.expression}">
                                                                        <option value="1" <c:if test="${status.value==1}">selected</c:if>><spring:message code="jsp.listResources.popupScreen"/></option><!--NOTE: this option is default -->
                                                                        <option value="2" <c:if test="${status.value==2}">selected</c:if>><spring:message code="jsp.listResources.separatePage"/></option>
                                                                        <option value="3" <c:if test="${status.value==3}">selected</c:if>><spring:message code="jsp.listResources.topOfPage"/></option>
                                                                        <option value="4" <c:if test="${status.value==4}">selected</c:if>><spring:message code="jsp.listResources.inPage"/></option>
                                                                    </select>
                                                                    <%--<span class="message warning">error message here</span>--%>
                                                                </label>
                                                            </spring:bind>

                                                            <spring:bind path="wrapper.reportUnit.alwaysPromptControls">
                                                                <input name="_${status.expression}" type="hidden"/>
                                                                <div class="control checkBox">
                                                                    <label class="wrap" for="${status.expression}" title="<spring:message code='resource.report.mode'/>">
                                                                        <spring:message code="jsp.listResources.alwaysPrompt"/>
                                                                    </label>
                                                                    <input class="" id="${status.expression}" name="${status.expression}" type="checkbox" <c:if test="${status.value}">checked</c:if>/><!--NOTE: default is NOT selected -->
                                                                </div>
                                                            </spring:bind>
                                                        </div>
                                                    </li>
                                                    <li class="leaf"><!-- NOTE:  -->
                                                        <div class="wrap">
                                                            <spring:bind path="wrapper.reportUnit.inputControlRenderingView">
                                                                <label class="control input text <c:if test="${status.error}">error</c:if>" for="${status.expression}" title="<spring:message code='resource.report.optionalJSPLocation'/>">
                                                                    <span class="wrap"><spring:message code="resource.report.optionalJSPLocation"/>:</span>
                                                                    <input name="${status.expression}" type="text" value="${status.value}"/>
                                                                    <p class="hint">(<spring:message code="resource.report.JSPLocationPath"/>)</p>

                                                                    <c:if test="${status.error}">
                                                                        <span class="message warning">${status.errorMessage}</span>
                                                                    </c:if>
                                                                </label>
                                                            </spring:bind>
                                                        </div>
                                                    </li>
                                                </ul>
                                            </li>
                                        </ul>
                                    </t:putAttribute>
                                </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="next" type="submit" name="_eventId_Next" class="button action up"><span class="wrap"><spring:message code="button.next" javaScriptEscape="true"/></span><span class="icon"></span></button>
                            <button id="done" type="submit" name="_eventId_finish" class="button primary action up"><span class="wrap"><spring:message code="button.submit" javaScriptEscape="true"/></span><span class="icon"></span></button>
                            <button id="cancel" type="submit" name="_eventId_Cancel" class="button action up"><span class="wrap"><spring:message code="button.cancel" javaScriptEscape="true"/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
    </t:putAttribute>
</t:insertTemplate>
