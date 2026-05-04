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

<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<% request.setAttribute("id", ((String) request.getAttribute("name")).replace(".", "_"));%>

<input type="text" name="${name}" id="${id}" value="${value}"
       onmousedown="cancelEventBubbling(event)"
       <c:if test="${not empty onchange}">onchange="${onchange}"</c:if>
       <c:if test="${readOnly}">disabled="disabled"</c:if>
        />
<c:if test="${not readOnly}">

    <script type="text/javascript">
        <js:xssNonce type="javascript"/>

        jQuery('#${id}').${(hasDate == true) ? 'date' : ''}${(hasTime == true) ? 'time' : ''}picker({
            <js:out javaScriptEscape="true">
            dateFormat:'${datePattern}',
            timeFormat:'${timePattern}',
            showSecond:'${showSecond}',
            </js:out>
            showOn:"button",
            buttonText:"",
            changeYear:true,
            changeMonth:true,
            showButtonPanel:true,
            onChangeMonthYear:null,
            beforeShow:jQuery.datepicker.movePickerRelativelyToTriggerIcon
        }).next().addClass('button').addClass('picker');
        // Prototype.js compatibility
        jQuery('#${id}')[0].getValue = function () {
            return jQuery(this).val()
        }
    </script>
</c:if>
