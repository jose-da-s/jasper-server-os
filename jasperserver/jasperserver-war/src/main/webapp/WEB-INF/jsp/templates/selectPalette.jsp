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
    Usage:permit user to add a file to the repository

Usage:

    <t:insertTemplate template="/WEB-INF/jsp/templates/selectPalette.jsp">
    </t:insertTemplate>

--%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<tx:useAttribute id="containerTitle" name="containerTitle" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="containerClass" name="containerClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="bodyContent" name="bodyContent" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="selectFileButtonId" name="selectFileButtonId" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="cancelButtonId" name="cancelButtonId" classname="java.lang.String" ignore="true"/>


<t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
    <t:putAttribute name="containerClass">panel dialog selectPalette overlay moveable sizeable centered_horz centered_vert ${containerClass}</t:putAttribute>
    <t:putAttribute name="containerElements"><div class="sizer diagonal"></div></t:putAttribute>
    <t:putAttribute name="headerClass" value="mover"/>
    <t:putAttribute name="containerID" value="selectPalette"/>
    <t:putAttribute name="containerTitle">Select Palette</t:putAttribute>
    <t:putAttribute name="bodyContent">
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="control groupBox fillParent"/>
            <t:putAttribute name="bodyContent">

                <ul class="list palette">
                    <li class="leaf selected button">
                        <p class="wrap">Default</p>
                        <ul class="list palette default">
                            <li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Olive</p>
                        <ul class="list palette olive"><li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Corporate</p>
                        <ul class="list palette corporate"><li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Pastel</p>
                        <ul class="list palette pastel">
                            <li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Fall</p>
                        <ul class="list palette fall"><li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Professional</p>
                        <ul class="list palette professional"><li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Forest</p>
                        <ul class="list palette forest">
                            <li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Slate</p>
                        <ul class="list palette slate"><li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Ocean</p>
                        <ul class="list palette ocean"><li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                    <li class="leaf button">
                        <p class="wrap">Wine</p>
                        <ul class="list palette wine"><li class="leaf chip color01"></li><li class="leaf chip color02"></li><li class="leaf chip color03"></li><li class="leaf chip color04"></li>
                        </ul>
                    </li>
                </ul>

            </t:putAttribute>
        </t:insertTemplate>
    </t:putAttribute>
    <t:putAttribute name="footerContent">
        <button id="themeDialogOk" class="button action primary up"><span class="wrap"><spring:message code="button.ok"/></span><span class="icon"></span></button>
        <button id="themeDialogCancel" class="button action up"><span class="wrap"><spring:message code="button.cancel"/></span><span class="icon"></span></button>
    </t:putAttribute>
</t:insertTemplate>
