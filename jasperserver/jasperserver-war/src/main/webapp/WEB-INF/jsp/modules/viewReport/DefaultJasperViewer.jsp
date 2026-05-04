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
Default rendering HTML fragment for a JR report called from the JasperViewerTag.

 Expects attributes:
    pageIndex:          Integer              Current page in report
    lastPageIndex:      Integer              Greatest page number in report
    page:               String               URL for surrounding page
    exporter:           AbstractHtmlExporter The exporter implementation
    pageIndexParameter: String               parameter name in URL for paging
--%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="js" uri="/WEB-INF/jasperserver.tld" %>

<%@ page import="net.sf.jasperreports.engine.export.JsonExporter" %>
<%@ page import="net.sf.jasperreports.export.SimpleHtmlExporterOutput" %>
<%@ page import="net.sf.jasperreports.export.SimpleJsonExporterOutput" %>
<%@ page import="java.io.StringWriter " %>
<%@ page errorPage="/WEB-INF/jsp/modules/system/prepErrorPage.jsp" %>


<div id="reportOutput" class="hidden">
    <js:xssNonce/>
    <%@ include file="DefaultJasperViewerState.jsp" %>


    <%--  Optional Pagination  --%>
    <c:if test="${(innerPagination || param.frame == 0) and !emptyReport and (empty lastPageIndex or lastPageIndex > 0 or configurationBean.paginationForSinglePageReport)}">
        <div id="innerPagination">
            <button type="submit" title="<spring:message code="jasper.report.view.hint.first.page"/>" class="button toLeft" ${pageIndex > 0 ? '' : 'disabled="disabled"'} onclick="javascript:Report.navigateToReportPage(0)"><span class="wrap"><span class="icon"></span></span></button>
            <button type="submit" title="<spring:message code="jasper.report.view.hint.previous.page"/>" class="button left" ${pageIndex > 0 ? '' : 'disabled="disabled"'} onclick="javascript:Report.navigateToReportPage(${pageIndex-1})"><span class="wrap"><span class="icon"></span></span></button>

            <label class="control input text inline" for="currentPage" title="<spring:message code="REPORT_VIEWER_PAGINATION_CONTROLS_CURRENT_PAGE" javaScriptEscape="true"/>">
		        <spring:message code="jasper.report.view.page.intro"/>
			    <input type="text" name="currentPage" value="${pageIndex+1}" onchange="javascript:Report.goToPage(this.value);return false;"/>
			    <spring:message code="jasper.report.view.page.of"/>${lastPageIndex + 1}
            </label>

            <button type="submit" title="<spring:message code="jasper.report.view.hint.next.page"/>" class="button right" ${pageIndex < lastPageIndex ? '' : 'disabled="disabled"'} onclick="javascript:Report.navigateToReportPage(${pageIndex+1})"><span class="wrap"><span class="icon"></span></span></button>
            <button type="submit" title="<spring:message code="jasper.report.view.hint.last.page"/>" class="button toRight" ${pageIndex < lastPageIndex ? '' : 'disabled="disabled"'} onclick="javascript:Report.navigateToReportPage(${lastPageIndex})"><span class="wrap"><span class="icon"></span></span></button>
        </div>
    </c:if>


    <%--  Report Output  --%>

    <c:if test="${!emptyReport}">
        <jsp:useBean id="exporter" type="net.sf.jasperreports.engine.export.AbstractHtmlExporter" scope="request"/>
        <jsp:useBean id="exporterOutput" type="net.sf.jasperreports.export.HtmlExporterOutput" scope="request"/>
        <%
          SimpleHtmlExporterOutput htmlExporterOutput = new SimpleHtmlExporterOutput(out);
          htmlExporterOutput.setImageHandler(exporterOutput.getImageHandler());
          htmlExporterOutput.setResourceHandler(exporterOutput.getResourceHandler());
          exporter.setExporterOutput(htmlExporterOutput);

          exporter.exportReport();

          JsonExporter jsonExporter = (JsonExporter) request.getAttribute("jsonExporter");
          if (jsonExporter != null)
          {
              StringWriter sw = new StringWriter();
              SimpleJsonExporterOutput jsonExporterOutput = new SimpleJsonExporterOutput(sw);
              htmlExporterOutput.setFontHandler(exporterOutput.getFontHandler());
              jsonExporter.setExporterOutput(jsonExporterOutput);
              jsonExporter.exportReport();

              String serializedJson = sw.getBuffer().toString();
              serializedJson = serializedJson.replaceAll("\\s","");
        %>
              <c:set var="escapedComponentsJson" value="<%= serializedJson %>" />
              <span id="reportComponents" style="display:none">${escapedComponentsJson}</span>
        <%
          }
        %>

        <div id="paginationIndexHolder" class="hidden" data-lastPageIndex="${lastPageIndex}"></div>
    </c:if>
    <c:if test="${emptyReport}">
        <div id="emptyReportMessageHolder" class="hidden">${emptyReportMessage}</div>
    </c:if>
</div>