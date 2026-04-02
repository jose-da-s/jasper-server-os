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
	This template used for a specific column title of table in manage page

Usage:
	 <t:insertTemplate template="/WEB-INF/jsp/modules/manage/manageColumnTitle.jsp">
	 	<t:putAttribute name="columnClass" value="twoColumn"/>
		<t:putAttribute name="firstColumnTitle">title1</t:putAttribute>
		<t:putAttribute name="secondColumnTitle">title2</t:putAttribute>
		<t:putAttribute name="thirdColumnTitle">title3</t:putAttribute>
	</t:insertTemplate>

--%>
<%@ taglib prefix="tx" uri="http://tiles.apache.org/tags-tiles-extras"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<tx:useAttribute id="columnClass" name="columnClass" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="firstColumnTitle" name="firstColumnTitle" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="secondColumnTitle" name="secondColumnTitle" classname="java.lang.String" ignore="true"/>
<tx:useAttribute id="thirdColumnTitle" name="thirdColumnTitle" classname="java.lang.String" ignore="true"/>

<ol class="list tabular collapsible ${columnClass} entities header">
	<li js-navtype="dynamiclist" class="leaf first" tabindex="-1">
		<div class="wrap">
			<div class="column one">
				<p class="ID"><a>${firstColumnTitle}</a></p>
			</div>
			<c:if test="${secondColumnTitle !=null}">
				<div class="column two">
					<p class="name">${secondColumnTitle}</p>
				</div>
			</c:if>
			<c:if test="${thirdColumnTitle !=null}">
				<div class="column three">
					<p class="organization">${thirdColumnTitle}</p>
				</div>
			</c:if>
			
		</div>
	</li>
</ol>