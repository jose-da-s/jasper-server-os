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
    <t:putAttribute name="pageTitle">
        <c:choose>
            <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/></c:when>
            <c:otherwise><spring:message code="resource.report.title"/></c:otherwise>
        </c:choose>
    </t:putAttribute><!-- NOTE: If this page reached by selecting to edit an existing domain, this value should be 'Edit JasperReport' -->
    <t:putAttribute name="bodyID" value="addReport_SetUp"/><!-- NOTE: If this page reached by selecting to edit an existing domain, this value should be 'editJasperReport' -->
    <t:putAttribute name="bodyClass" value="oneColumn flow oneStep"/>
    <t:putAttribute name="moduleName" value="addResource/jasperReport/addJasperReportMain"/>

    <t:putAttribute name="headerContent">
        <jsp:include page="reportStep2State.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <form action="flow.html" method="post" enctype="multipart/form-data">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="containerTitle">
                    <c:choose>
                        <c:when test="${wrapper.editMode}"><spring:message code="resource.report.titleEdit"/>:</c:when>
                        <c:otherwise><spring:message code="resource.report.title"/>:</c:otherwise>
                    </c:choose>
                     ${wrapper.reportUnit.label}
                </t:putAttribute><!-- NOTE: If this page reached by selecting to edit an existing domain, this value should be 'Edit JasperReport' -->

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                    <div id="flowControls">
                        <ul class="control tabSet buttons vertical">
                            <li class="tab first selected">
                                <button class="button up" id="steps1_2" ><span class="wrap"><spring:message code="resource.report.setup"/></span></button>
                            </li>
                            <!--/.tab-->
                            <li class="tab">
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
                        <input type="hidden" id="ParentFolderUri" value='${param.ParentFolderUri}'>
                        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>
                        <input type="hidden" id="editMode" value="${wrapper.editMode}">

                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.report.instructions"/></span></legend>
                            <h2 class="textAccent02"><spring:message code="resource.report.reportSetup"/></h2>
                            <h4><spring:message code="resource.report.setValues"/></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.report.inputs"/></span></legend>

                            <!-- start two columns -->
                            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                <t:putAttribute name="containerClass" value="column primary noHeader"/>
                                <t:putAttribute name="bodyContent">

                                    <fieldset class="group">
                                        <legend class="offLeft"><span><spring:message code="resource.report.nameAndDescription"/></span></legend>

                                        <spring:bind path="wrapper.reportUnit.label">
                                            <label class="control input text <c:if test="${status.error}">error</c:if>" class="required" for="label" title="<spring:message code="resource.report.visibleName"/>">
                                                <span class="wrap"><spring:message code="resource.report.name"/>:</span>
                                                <input class="" id="label" name="${status.expression}" type="text" value="${status.value}"/>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>

                                        <spring:bind path="wrapper.reportUnit.name">
                                            <label class="control input text <c:if test="${status.error}">error</c:if>" for="resourceID" title="<spring:message code="resource.report.permanentID"/>">
                                                <span class="wrap"><spring:message code="resource.report.resourceID"/>
                                                    <c:choose>
                                                        <c:when test="${wrapper.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                        <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <input class="" id="resourceID" name="${status.expression}" type="text" value="${status.value}" <c:if test="${wrapper.editMode}">readonly="readonly"</c:if>/>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>

                                        <spring:bind path="wrapper.reportUnit.description">
                                            <label class="control textArea <c:if test="${status.error}">error</c:if>" for="${status.expression}">
                                                <span class="wrap"><spring:message code="resource.report.description"/>:</span>
                                                <textarea class="" id="${status.expression}" name="${status.expression}" type="text">${status.value}</textarea>
                                                <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                            </label>
                                        </spring:bind>
                                    </fieldset>
                                    <fieldset class="group">
                                        <legend class="label" style="overflow:visible;"><span><spring:message code="resource.report.locateJRXML"/></span></legend>
                                        <ul class="list locations">
                                            <li id="fromLocal" class="leaf">
                                                <spring:bind path="wrapper.source">
                                                    <div  class="control radio<c:if test='${status.error}'> error</c:if>">
                                                        <label class="wrap" for="FILE_SYSTEM" title="Upload a Local File">
                                                            <spring:message code="resource.report.upload"/>
                                                        </label>
                                                        <input class="" id="FILE_SYSTEM" type="radio" name="${status.expression}" value="FILE_SYSTEM" <c:if test="${status.value!='CONTENT_REPOSITORY'}">checked="checked"</c:if>/>
                                                        <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                    </div>
                                                </spring:bind>

                                                <spring:bind path="wrapper.jrxmlData">
                                                <div class="browser">
                                                    <label id="fileUpload" for="fileName" class="control browser <c:if test="${status.error || jrxmlUnparsable != null}">error</c:if> fakeFileUpload" for="filePath" title="<spring:message code="resource.report.locateFile"/>">
                                                       <!-- NOTE: If radio button for this option selected, remove disabled attribute on input -->
                                                        <input id="fileName" name="fileName" type="text" readonly="readonly"/>
                                                        <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                        <c:if test="${wrapper.source!='CONTENT_REPOSITORY'}">
                                                            <span class="message warning"><spring:message code="${jrxmlUnparsable}"/></span>
                                                        </c:if>
                                                        <button id="fake_upload_button" class="button action up">
                                                            <span class="wrap"><spring:message code="button.browse"/>
                                                                <span class="icon">
                                                                </span>
                                                            </span>
                                                        </button>
                                                        <input class="" id="filePath" name="${status.expression}" type="file" value="" tabindex="-1"/>
                                                      </label>
                                                </div>

                                                </spring:bind>
                                            </li>
                                            <spring:bind path="wrapper.source">
                                                <li id="browseRepo" class="leaf">
                                                    <div class="control radio complex">
                                                        <label class="wrap" for="CONTENT_REPOSITORY" title="<spring:message code='resource.report.repository'/>">
                                                            <spring:message code="resource.report.selectJRXML"/>
                                                        </label>
                                                        <input class="" id="CONTENT_REPOSITORY" type="radio" name="${status.expression}" title="<spring:message code="resource.report.repository"/>" value="CONTENT_REPOSITORY" <c:if test="${status.value=='CONTENT_REPOSITORY'}">checked="checked"</c:if>
                                                        <c:if test='${not wrapper.anyJrxmlAvailable}'>disabled="disabled"</c:if>/>
                                                    </div>
                                                    <div class="browser">
                                                    <label title="<spring:message code="resource.report.repository"/>" for="resourceUri"
                                                           class="control browser<c:if test="${status.error || wrapper.source=='CONTENT_REPOSITORY' && jrxmlUnparsable != null}"> error</c:if>">
                                            </spring:bind>
                                                        <spring:bind path="wrapper.jrxmlUri">
                                                            <input id="resourceUri" type="text" name="${status.expression}" title="<spring:message code="resource.report.repository"/>" value="${status.value}"/>
                                                        </spring:bind>
                                            <spring:bind path="wrapper.source">
                                                        <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                        <c:if test="${status.error || wrapper.source=='CONTENT_REPOSITORY' && jrxmlUnparsable != null}">
                                                            <span class="message warning">${status.errorMessage}<spring:message code="${jrxmlUnparsable}"/></span>
                                                        </c:if>
                                                        <button id="browser_button" type="button" class="button action up" <c:if test="${wrapper.source!='CONTENT_REPOSITORY'}">disabled="disabled"</c:if>>
                                                            <span class="wrap"><spring:message code="button.browse"/>
                                                                <span class="icon"></span>
                                                            </span>
                                                        </button>
                                                    </label>
                                                        </div>
                                                </li>
                                            </spring:bind>
                                        </ul>
                                    </fieldset>
                                </t:putAttribute>
                            </t:insertTemplate>

                            <!-- end two columns -->
                        </fieldset>
                        <!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="done" name="_eventId_finish" type="submit" class="button primary action up"><span class="wrap"><spring:message code="button.submit" javaScriptEscape="true"/></span><span class="icon"></span></button>
                            <button id="next" name="_eventId_Next" type="submit" class="button primary action up" disabled="disabled"><span class="wrap"><spring:message code="button.submit" javaScriptEscape="true"/></span><span class="icon"></span></button>
                            <button id="cancel" name="_eventId_Cancel" type="submit" class="button action up"><span class="wrap"><spring:message code="button.cancel" javaScriptEscape="true"/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
        <t:insertTemplate template="/WEB-INF/jsp/templates/selectFromRepository.jsp">
            <t:putAttribute name="containerClass">hidden</t:putAttribute>
            <t:putAttribute name="bodyContent">
                <ul id="resourceTreeRepoLocation"> </ul>
            </t:putAttribute>
        </t:insertTemplate>

        <div id="ajaxbuffer" class="hidden" ></div>
    </t:putAttribute>
</t:insertTemplate>
