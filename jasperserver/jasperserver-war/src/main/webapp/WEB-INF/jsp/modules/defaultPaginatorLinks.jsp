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

<%@ page import="com.jaspersoft.jasperserver.war.tags.PaginatorLinksTag, com.jaspersoft.jasperserver.war.tags.PaginatorTag"%>

<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<%
PaginatorLinksTag.PaginatorInfo info = (PaginatorLinksTag.PaginatorInfo)request.getAttribute(PaginatorTag.PAGINATOR_INFO_REQUEST_PARAMETER);
%>

<table cellpadding="0" cellspacing="0" border="0">
    <js:xssNonce/>
    <tr valign="middle">
<c:if test="${paginatorInfo.currentPage > 1}">
  <td><a href="javascript:document.forms['${paginatorFormName}'].currentPage.value=1;document.forms['${paginatorFormName}'].goToPage.click();" title="<spring:message code="paginator.links.hint.first.page"/>"><img border="0" src="images/first.gif" class="imageborder" onMouseover="borderImage(this,'#C0C0C0')" onMouseout="borderImage(this,'white')"/></a></td>
  <td><a href="javascript:document.forms['${paginatorFormName}'].currentPage.value=${paginatorInfo.currentPage - 1};document.forms['${paginatorFormName}'].goToPage.click();" title="<spring:message code="paginator.links.hint.previous.page"/>"><img border="0" src="images/prev.gif" class="imageborder" onMouseover="borderImage(this,'#C0C0C0')" onMouseout="borderImage(this,'white')"/></a></td>
  <td>&nbsp;</td>
</c:if>
<c:if test="${paginatorInfo.pageCount > 1 && paginatorInfo.currentPage <= 1}">
  <td><img border="0" src="images/first-d.gif" class="imageborder"/></td>
  <td><img border="0" src="images/prev-d.gif" class="imageborder"/></td>
  <td>&nbsp;</td>
</c:if>
<%
			for (int i = info.firstPage; i < info.currentPage; i++) 
			{
%>
  <td><a href="javascript:document.forms['${paginatorFormName}'].currentPage.value=<%=i%>;document.forms['${paginatorFormName}'].goToPage.click();" title="<spring:message code="paginator.links.hint.go.to.page" arguments='<%= new Integer(i) %>'/>"><%=i%></a>&nbsp;</td>
<%
			}
%>
<c:if test="${paginatorInfo.pageCount > 1}">
  <td><%=info.currentPage%>&nbsp;</td>
</c:if>
<%
			for (int i = info.currentPage + 1; i <= info.lastPage; i++) 
			{
%>
  <td><a href="javascript:document.forms['${paginatorFormName}'].currentPage.value=<%=i%>;document.forms['${paginatorFormName}'].goToPage.click();" title="<spring:message code="paginator.links.hint.go.to.page" arguments='<%= new Integer(i) %>'/>"><%=i%></a>&nbsp;</td>
<%
			}
%>
<c:if test="${paginatorInfo.pageCount > paginatorInfo.currentPage}">
  <td><a href="javascript:document.forms['${paginatorFormName}'].currentPage.value=<%=info.currentPage + 1%>;document.forms['${paginatorFormName}'].goToPage.click();" title="<spring:message code="paginator.links.hint.next.page"/>"><img border="0" src="images/next.gif" class="imageborder" onMouseover="borderImage(this,'#C0C0C0')" onMouseout="borderImage(this,'white')"/></a></td>
  <td><a href="javascript:document.forms['${paginatorFormName}'].currentPage.value=<%=info.pageCount%>;document.forms['${paginatorFormName}'].goToPage.click();" title="<spring:message code="paginator.links.hint.last.page"/>"><img border="0" src="images/last.gif" class="imageborder" onMouseover="borderImage(this,'#C0C0C0')" onMouseout="borderImage(this,'white')"/></a></td>
</c:if>
<c:if test="${paginatorInfo.pageCount > 1 && paginatorInfo.pageCount <= paginatorInfo.currentPage}">
  <td><img border="0" src="images/next-d.gif" class="imageborder"/></td>
  <td><img border="0" src="images/last-d.gif" class="imageborder"/></td>
</c:if>
  </tr>
</table>
<input type="submit" name="_eventId_goToPage" id="goToPage" value="edit" style="visibility:hidden;" onClick="javascript:return (js_pagination_hookActions())"/>
<input type="hidden" name="currentPage" id="currentPage"/>

<script type="text/javascript">
   function js_pagination_hookActions() {
      if (window.js_hookActions) {
         return (js_hookActions());
      }
      return true;
   }

</script>
