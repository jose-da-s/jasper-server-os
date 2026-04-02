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
  --%><%--

    IMPORTANT: Do not remove the comments between the tags, they need to prevent unnecessary spaces in the output.

--%><%@ taglib prefix="c" uri="jakarta.tags.core" %><%--
--%><%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %><%--

--%><c:choose><%--
	--%><c:when test="${control.inputControl.mandatory
			and (control.inputControl.inputControlType == 2
				or control.inputControl.inputControlType == 3
				or control.inputControl.inputControlType == 4
				or control.inputControl.inputControlType == 6
				or control.inputControl.inputControlType == 7
				or control.inputControl.inputControlType == 8
				or control.inputControl.inputControlType == 9
				or control.inputControl.inputControlType == 10
				or control.inputControl.inputControlType == 11
				)}"><%--
        --%><spring:message code="jsp.defaultParametersForm.star.input.label" argumentSeparator="\\\\" arguments="${controlLabel}"/><%--
    --%></c:when><%--
	--%><c:otherwise><%--
		--%>${controlLabel}<%--
	--%></c:otherwise><%--
--%></c:choose>