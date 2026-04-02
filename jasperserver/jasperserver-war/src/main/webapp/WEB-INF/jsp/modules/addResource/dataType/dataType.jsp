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

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle">
        <c:choose>
            <c:when test="${dataType.editMode}"><spring:message code="resource.dataType.title.edit"/></c:when>
            <c:otherwise><spring:message code="resource.dataType.title.add"/></c:otherwise>
        </c:choose>
    </t:putAttribute>
    <t:putAttribute name="bodyID" value="addResource_dataType"/>
    <t:putAttribute name="bodyClass" value="oneColumn flow wizard oneStep"/>
    <t:putAttribute name="moduleName" value="addResource/dataType/addDataTypeMain"/>
    <t:putAttribute name="headerContent">
        <jsp:include page="dataTypeState.jsp"/>
    </t:putAttribute>
    <t:putAttribute name="bodyContent">
        <form method="post" action="flow.html">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                <t:putAttribute name="containerTitle">
                    <c:choose>
                        <c:when test="${dataType.editMode}"><spring:message code="resource.dataType.title.edit"/></c:when>
                        <c:otherwise><spring:message code="resource.dataType.title.add"/></c:otherwise>
                    </c:choose>
                </t:putAttribute>

                <t:putAttribute name="swipeScroll" value="${isIPad}"/>

                <t:putAttribute name="bodyContent">
                    <div id="flowControls"></div>
                    <div id="stepDisplay">
                        <input type="hidden" id="ParentFolderUri" value='${param.ParentFolderUri}'>
                        <input type="submit" name="_eventId_changeCombo" id="changeCombo" style="display:none;"/>
                        <input type="hidden" name="_flowExecutionKey" value="${flowExecutionKey}"/>

                        <fieldset class="row instructions">
                            <legend class="offLeft"><span><spring:message code="resource.dataType.instructions"/></span></legend>
                            <h2 class="textAccent02"><spring:message code="resource.dataType.setDataTypeKindAndProperties"/></h2>
                            <h4><spring:message code="resource.dataType.selectKindOfDataType"/></h4>
                        </fieldset>

                        <fieldset class="row inputs oneColumn">
                            <legend class="offLeft"><span><spring:message code="resource.dataType.userInputs"/></span></legend>
                                <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                                    <t:putAttribute name="containerClass" value="column primary"/>
                                    <t:putAttribute name="containerTitle"><spring:message code="jsp.editDataTypeForm.type"/>:</t:putAttribute>
                                    <t:putAttribute name="headerContent">
                                        <spring:bind path="dataType.dataType.dataTypeType">
                                            <label class="control select inline" for="dataTypeKind" title="<spring:message code='resource.dataType.dataTypeKind'/>">
                                                <span class="wrap offLeft"><spring:message code="resource.dataType.dataTypeKind"/></span>
                                                <select id="${status.expression}" name="${status.expression}" <%--onchange="javascript:$('changeCombo').click();"--%>>
                                                    <option value="1" <c:if test="${status.value == 1}">selected</c:if>><spring:message code="jsp.editDataTypeForm.text"/></option>
                                                    <option value="2" <c:if test="${status.value == 2}">selected</c:if>><spring:message code="jsp.editDataTypeForm.number"/></option>
                                                    <option value="3" <c:if test="${status.value == 3}">selected</c:if>><spring:message code="jsp.editDataTypeForm.date"/></option>
                                                    <option value="4" <c:if test="${status.value == 4}">selected</c:if>><spring:message code="jsp.editDataTypeForm.datetime"/></option>
                                                    <option value="5" <c:if test="${status.value == 5}">selected</c:if>><spring:message code="jsp.editDataTypeForm.time"/></option>
                                                </select>
                                            </label>
                                        </spring:bind>
                                    </t:putAttribute>

                                    <t:putAttribute name="bodyContent">
                                        <fieldset class="group">
                                            <!-- NOTE: This fieldset appears identically for all add resource data source types -->
                                            <legend class="offLeft"><span><spring:message code="resource.dataType.nameAndDescription"/></span></legend>
                                            <spring:bind path="dataType.dataType.label">
                                                <label class="control input text <c:if test="${status.error}">error</c:if>" class="required" for="${status.expression}" title="<spring:message code='resource.dataType.visibleResourceName'/>">
                                                    <span class="wrap"><spring:message code="resource.dataType.name"/> (<spring:message code='required.field'/>):</span>
                                                    <input class="" id="${status.expression}" name="${status.expression}" type="text" value="${status.value}" size="40"/>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                </label>
                                            </spring:bind>

                                            <spring:bind path="dataType.dataType.name">
                                                <input type="hidden" id="editMode" value="${dataType.editMode}">
                                                <label class="control input text <c:if test="${status.error}">error</c:if>" class="required" for="${status.expression}" title="<spring:message code='resource.dataType.visibleResourceID'/>">
                                                    <span class="wrap"><spring:message code="resource.dataType.resourceID"/>
                                                        <c:choose>
                                                            <c:when test="${dataType.editMode}"> (<spring:message code='dialog.value.readOnly'/>):</c:when>
                                                            <c:otherwise> (<spring:message code='required.field'/>):</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <input class="" id="${status.expression}" name="${status.expression}" type="text" value="${status.value}" <c:if test="${dataType.editMode}">readonly="readonly"</c:if>/>
                                                           <!-- NOTE: This inline javascript here just for demonstration purposes; in production use AJAX to validate and create resourceID on keyup -->
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                </label>
                                            </spring:bind>

                                            <spring:bind path="dataType.dataType.description">
                                                <label class="control textArea <c:if test="${status.error}">error</c:if>" for="${status.expression}">
                                                    <span class="wrap"><spring:message code="jsp.editDataTypeForm.description"/>:</span>
                                                    <textarea id="${status.expression}" name="${status.expression}" type="text">${status.value}</textarea>
                                                    <span class="message warning"><c:if test="${status.error}">${status.errorMessage}</c:if></span>
                                                </label>
                                            </spring:bind>
                                        </fieldset>
                                        <fieldset class="group">
                                            <legend class="offLeft"><span><spring:message code="resource.dataType.values"/></span></legend>
                                            <!-- NOTE: add value 'hidden' to class attribute for pattern label if (kind != 'text') -->
                                            <c:if test="${dataType.dataType.dataTypeType == 1}">
                                                <spring:bind path="dataType.dataType.regularExpr">
                                                    <label class="control input text <c:if test="${status.error}">error</c:if>" class="" for="${status.expression}" title="<spring:message code='resource.dataType.textPattern'/>">
                                                        <span class="wrap"><spring:message code="jsp.editDataTypeForm.pattern"/>:</span>
                                                        <input class="" id="${status.expression}" name="${status.expression}" type="text" value="${status.value}" size="40"/>
                                                        <c:if test="${status.error}">
                                                            <c:forEach items="${status.errorMessages}" var="error">
                                                                <span class="message warning">${error}</span>
                                                            </c:forEach>
                                                        </c:if>
                                                    </label>
                                                </spring:bind>
                                            </c:if>

                                            <c:choose>
                                                <c:when test="${dataType.dataType.dataTypeType == 3 || dataType.dataType.dataTypeType == 4 || dataType.dataType.dataTypeType == 5}">
                                                    <spring:bind path="dataType.minValueText">
                                                        <label class="control picker minPicker <c:if test="${status.error}">error</c:if>" class="" for="${status.expression}" title="<spring:message code='resource.dataType.minimumValue'/>">
                                                            <span class="wrap"><spring:message code="jsp.editDataTypeForm.minValue"/>:</span>
                                                            <c:choose>
                                                                 <c:when test="${dataType.dataType.dataTypeType == 3}">
                                                                    <c:set var="date" value="true"/>
                                                                    <c:set var="time" value="false"/>
                                                                 </c:when>
                                                                 <c:when test="${dataType.dataType.dataTypeType == 4}">
                                                                    <c:set var="date" value="true"/>
                                                                    <c:set var="time" value="true"/>
                                                                </c:when>
                                                                 <c:when test="${dataType.dataType.dataTypeType == 5}">
                                                                    <c:set var="date" value="false"/>
                                                                    <c:set var="time" value="true"/>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <c:set var="date" value="true"/>
                                                                    <c:set var="time" value="true"/>
                                                                </c:otherwise>
                                                            </c:choose>

                                                            <js:out javaScriptEscape="true">
                                                            <script type="text/javascript">
                                                                __jrsConfigs__.addDataType.minValueText = {
                                                                    name: "${status.expression}",
                                                                    value: "${status.value}",
                                                                    date: "${date}",
                                                                    time: "${time}"
                                                                };
                                                            </script>
                                                            </js:out>

                                                            <c:if test="${status.error}">
                                                                <c:forEach items="${status.errorMessages}" var="error">
                                                                    <span class="message warning">${error}</span>
                                                                </c:forEach>
                                                            </c:if>
                                                        </label>
                                                    </spring:bind>
                                                </c:when>
                                                <c:when test="${dataType.dataType.dataTypeType == 2}">
                                                    <spring:bind path="dataType.dataType.minValue">
                                                        <label class="control input text <c:if test="${status.error}">error</c:if>" class="" for="${status.expression}" title="<spring:message code='resource.dataType.minimumValue'/>">
                                                            <span class="wrap"><spring:message code="jsp.editDataTypeForm.minValue"/>:</span>
                                                            <input type="text" name="${status.expression}" value="${status.value}" size="40"/>
                                                            <c:if test="${status.error}">
                                                                <c:forEach items="${status.errorMessages}" var="error">
                                                                    <span class="message warning">${error}</span>
                                                                </c:forEach>
                                                            </c:if>
                                                        </label>
                                                    </spring:bind>
                                                </c:when>
                                            </c:choose>

                                            <c:choose>
                                                <c:when test="${dataType.dataType.dataTypeType == 3 || dataType.dataType.dataTypeType == 4 || dataType.dataType.dataTypeType == 5}">
                                                    <spring:bind path="dataType.maxValueText">
                                                        <label class="control picker maxPicker <c:if test="${status.error}">error</c:if>" class="" for="${status.expression}" title="<spring:message code='resource.dataType.maximumValue'/>">
                                                            <span class="wrap"><spring:message code="jsp.editDataTypeForm.maxValue"/></span>
                                                            <c:choose>
                                                                 <c:when test="${dataType.dataType.dataTypeType == 3}">
                                                                    <c:set var="date" value="true"/>
                                                                    <c:set var="time" value="false"/>
                                                                 </c:when>
                                                                 <c:when test="${dataType.dataType.dataTypeType == 4}">
                                                                    <c:set var="date" value="true"/>
                                                                    <c:set var="time" value="true"/>
                                                                </c:when>
                                                                 <c:when test="${dataType.dataType.dataTypeType == 5}">
                                                                    <c:set var="date" value="false"/>
                                                                    <c:set var="time" value="true"/>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <c:set var="date" value="true"/>
                                                                    <c:set var="time" value="true"/>
                                                                </c:otherwise>
                                                            </c:choose>

                                                            <js:out javaScriptEscape="true">
                                                            <script type="text/javascript">
                                                                __jrsConfigs__.addDataType.maxValueText = {
                                                                    name: "${status.expression}",
                                                                    value: "${status.value}",
                                                                    date: "${date}",
                                                                    time: "${time}"
                                                                };
                                                            </script>
                                                            </js:out>

                                                            <c:if test="${status.error}">
                                                                <c:forEach items="${status.errorMessages}" var="error"> <span class="message warning">${error}</span>
                                                                </c:forEach>
                                                            </c:if>
                                                        </label>
                                                    </spring:bind>
                                                </c:when>
                                                <c:otherwise>
                                                    <spring:bind path="dataType.dataType.maxValue">
                                                            <c:choose>
                                                                <c:when test="${dataType.dataType.dataTypeType == 3}">
                                                        <label class="control picker" class="" for="${status.expression}" title="<spring:message code='resource.dataType.maximumValue'/>">
                                                            <span class="wrap"><spring:message code="jsp.editDataTypeForm.maxValue"/></span>
                                                                    <js:calendarInput name="${status.expression}" value="${status.value}"
                                                                        time="false"
                                                                        imageTipMessage="jsp.defaultParametersForm.pickDate"/>
                                                                </c:when>
                                                                <c:when test="${dataType.dataType.dataTypeType == 4}">
                                                        <label class="control picker" class="" for="${status.expression}" title="<spring:message code='resource.dataType.maximumValue'/>">
                                                            <span class="wrap"><spring:message code="jsp.editDataTypeForm.maxValue"/></span>
                                                                    <js:calendarInput name="${status.expression}" value="${status.value}"
                                                                        imageTipMessage="jsp.defaultParametersForm.pickDate"/>
                                                                </c:when>
                                                                 <c:when test="${dataType.dataType.dataTypeType == 5}">
                                                        <label class="control picker" class="" for="${status.expression}" title="<spring:message code='resource.dataType.maximumValue'/>">
                                                            <span class="wrap"><spring:message code="jsp.editDataTypeForm.maxValue"/></span>
                                                                    <js:calendarInput name="${status.expression}" value="${status.value}"
                                                                        date="false" showSecond="${true}"
                                                                        imageTipMessage="jsp.defaultParametersForm.pickDate"/>
                                                                </c:when>
                                                                <c:when test="${dataType.dataType.dataTypeType == 2}">
                                                        <label class="control input text <c:if test="${status.error}">error</c:if>" class="" for="${status.expression}" title="<spring:message code='resource.dataType.maximumValue'/>">
                                                            <span class="wrap"><spring:message code="jsp.editDataTypeForm.maxValue"/></span>
                                                                    <input type="text" name="${status.expression}" value="${status.value}" size="40"/>
                                                                </c:when>
                                                            </c:choose>
                                                            <c:if test="${status.error}">
                                                                <c:forEach items="${status.errorMessages}" var="error">
                                                                    <span class="message warning">${error}</span>
                                                                </c:forEach>
                                                            </c:if>
                                                        </label>
                                                    </spring:bind>
                                                </c:otherwise>
                                            </c:choose>

                                            <c:if test="${dataType.dataType.dataTypeType != 1}">
                                            <ul class="list inputSet">
                                                <spring:bind path="dataType.dataType.strictMin">
                                                    <li class="leaf">
                                                        <div class="control checkBox">
                                                            <label class="wrap" for="${status.expression}" title="<spring:message code='resource.dataType.strictMinimum'/>">
                                                                <spring:message code="resource.dataType.strictMinimum"/>
                                                            </label>
                                                            <input name="_${status.expression}" type="hidden"/>
                                                            <c:if test="${status.value}">
                                                                <input type="checkbox" id="${status.expression}" name="${status.expression}" checked/>
                                                            </c:if>
                                                            <c:if test="${!status.value}">
                                                                <input type="checkbox" id="${status.expression}" name="${status.expression}"/>
                                                            </c:if>
                                                        </div>
                                                    </li>
                                                </spring:bind>

                                                <spring:bind path="dataType.dataType.strictMax">
                                                    <li class="leaf">
                                                        <div class="control checkBox">
                                                            <label class="wrap" for="${status.expression}" title="<spring:message code='resource.dataType.strictMaximum'/>">
                                                                <spring:message code="resource.dataType.strictMaximum"/>
                                                            </label>
                                                            <input name="_${status.expression}" type="hidden"/>
                                                            <c:if test="${status.value}">
                                                                <input type="checkbox" id="${status.expression}" name="${status.expression}" checked/>
                                                            </c:if>
                                                            <c:if test="${!status.value}">
                                                                <input type="checkbox" id="${status.expression}" name="${status.expression}"/>
                                                            </c:if>
                                                        </div>
                                                    </li>
                                                </spring:bind>
                                            </ul>
                                      </c:if>
                                    </t:putAttribute>
                                </t:insertTemplate>
                        </fieldset><!--/.row.inputs-->
                    </div><!--/#stepDisplay-->
                    <t:putAttribute name="footerContent">
                        <fieldset id="wizardNav" class="row actions">
                            <button id="previous" type="submit" class="button action up"><span class="wrap"><spring:message code='button.previous'/></span><span class="icon"></span></button>
                            <button id="next" type="submit" class="button action up"><span class="wrap"><spring:message code='button.next'/></span><span class="icon"></span></button>
                            <button id="done" type="submit" class="button primary action up" name="_eventId_save"><span class="wrap"><spring:message code='button.save'/></span><span class="icon"></span></button>
                            <button id="cancel" type="submit" class="button action up" name="_eventId_cancel"><span class="wrap"><spring:message code='button.cancel'/></span><span class="icon"></span></button>
                        </fieldset>
                    </t:putAttribute>
                </t:putAttribute>
            </t:insertTemplate>
        </form>
    </t:putAttribute>
</t:insertTemplate>
