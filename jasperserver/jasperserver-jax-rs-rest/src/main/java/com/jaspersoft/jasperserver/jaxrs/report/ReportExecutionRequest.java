/*
 * Copyright (C) 2025 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.jaxrs.report;

import com.jaspersoft.jasperserver.dto.reports.ReportParameters;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: ReportExecutionRequest.java 26599 2012-12-10 13:04:23Z ykovalchyk $
 */
@XmlRootElement
@Schema(name = "reportExecutionRequest", description = "Descriptor for the report execution request.")
public class ReportExecutionRequest {
	
	public static final String MARKUP_TYPE_FULL = "full";
	public static final String MARKUP_TYPE_EMBEDDABLE = "embeddable";

    private String reportUnitUri;
    private Boolean freshData = false;
    private Boolean saveDataSnapshot = false;
    /* reports are interactive by default in v2 services*/
    private Boolean interactive = true;
    private Boolean ignorePagination;
    private Integer reportContainerWidth;
    private Boolean async = false;
    private String transformerKey;
    private String outputFormat;
    private String attachmentsPrefix;
    private Boolean allowInlineScripts = true;
    private String pages;
    private String baseUrl;
    private String markupType;
    private String anchor;
    private ReportParameters parameters;

    @Schema(description = "Specifies the report page anchor name.")
    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

	@Schema(
			description = "Specifies if the HTML output will be generated as full HTML document or as embeddable HTML content.", 
			allowableValues = {MARKUP_TYPE_FULL, MARKUP_TYPE_EMBEDDABLE},
			example = MARKUP_TYPE_FULL
			)
    public String getMarkupType() {
        return markupType;
    }

    public void setMarkupType(String markupType) {
        this.markupType = markupType;
    }

	@Schema(
			type="boolean", 
			description = "Affects HTML export only. If true, then inline scripts are allowed, otherwise no inline script is included in the HTML output.", 
			defaultValue = "true",
			example = "false")
    public Boolean isAllowInlineScripts() {
        return allowInlineScripts;
    }

    public void setAllowInlineScripts(Boolean allowInlineScripts) {
        this.allowInlineScripts = allowInlineScripts;
    }

	@Schema(
			description = "Specifies the base URL that the report will use to load static resources such as JavaScript files. "
					+ "You can also set the `deploy.base.url` property in the `.../WEB-INF/js.config.properties` file to set "
					+ "this value permanently. If both are set, the `baseUrl` parameter in this request takes precedence.",
			example = "http://localhost:8080/jasperserver-pro")
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

	@Schema(
			description = "Repository path (URI) of the report to run. For commercial editions with "
					+ "organizations, the URI is relative to the logged-in user’s organization.", 
			required=true,
			example = "/public/Samples/Reports/AllAccounts"
			)
   public String getReportUnitUri() {
        return reportUnitUri;
    }

    public void setReportUnitUri(String reportUnitUri) {
        this.reportUnitUri = reportUnitUri;
    }

	@Schema(
			type="boolean", 
			description = "When data snapshots are enabled, specifies whether the report should get fresh data by querying "
					+ "the data source or if false, use a previously saved data snapshot (if any). By default, if a saved "
					+ "data snapshot exists for the report it will be used when running the report.", 
			defaultValue = "false",
			example = "true"
			)
    public Boolean getFreshData() {
        return freshData;
    }

    public void setFreshData(Boolean freshData) {
        this.freshData = freshData;
    }

	@Schema(
			type="boolean", 
			description = "When data snapshots are enabled, specifies whether the data snapshot for the report should be "
					+ "written or overwritten with the new data from this execution of the report.", 
			defaultValue = "false",
			example = "true"
			)
    public Boolean getSaveDataSnapshot() {
        return saveDataSnapshot;
    }

    public void setSaveDataSnapshot(Boolean saveDataSnapshot) {
        this.saveDataSnapshot = saveDataSnapshot;
    }

	@Schema(
			type="boolean", 
			description = "In a commercial editions of the server where HighCharts are used in the report, "
					+ "this property determines whether the JavaScript necessary for interaction is generated "
					+ "and returned as an attachment when exporting to HTML. If false, the chart is generated as "
					+ "a non-interactive image file (also as an attachment).", 
			defaultValue = "true",
			example = "false"
			)
    public Boolean getInteractive() {
        return interactive;
    }

    public void setInteractive(Boolean interactive) {
        this.interactive = interactive;
    }

	@Schema(
			type="boolean", 
			description = "When set to true, the report is generated as a single long page. This can be used with HTML output "
					+ "to avoid pagination. When omitted, the ignorePagination property on the JRXML, if any, is used. ", 
			example = "false"
			)
    public Boolean getIgnorePagination() {
        return ignorePagination;
    }

    public void setIgnorePagination(Boolean ignorePagination) {
        this.ignorePagination = ignorePagination;
    }

    @Schema(
            type="integer",
            description = "Reports that declare the REPORT_CONTAINER_WIDTH parameter will have this value injected " +
                    "into it. ",
            defaultValue = "null"
    )
    public Integer getReportContainerWidth() {
        return reportContainerWidth;
    }

    public void setReportContainerWidth(Integer reportContainerWidth) {
        this.reportContainerWidth = reportContainerWidth;
    }

	@Schema(
			type="boolean", 
			description = "Determines whether reportExecution is synchronous or asynchronous. When set to true, "
					+ "the response is sent immediately and the client must poll the report status and later "
					+ "download the result when ready. By default, this property is false and the operation will "
					+ "wait until the report execution is complete, forcing the client to wait as well, but allowing "
					+ "the client to download the report immediately after the response.", 
			defaultValue = "false",
			example = "true"
			)
    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

	@Schema(
			description = "Advanced property used when requesting a report as a JasperPrint object. This property can specify "
					+ "a JasperReports Library generic print element transformers of class "
					+ "`net.sf.jasperreports.engine.export.GenericElementTransformer`. These transformers are pluggable as "
					+ "JasperReports Library extensions."
			)
    public String getTransformerKey() {
        return transformerKey;
    }

    public void setTransformerKey(String transformerKey) {
        this.transformerKey = transformerKey;
    }

	@Schema(
			description = "Specifies the desired output format: `pdf`, `html`, `csv`, `xls`, `xlsx`, `docx`, `pptx`, `odt`, `ods`, `rtf`, `xml`, "
					+ "`jrprint`.\n\n"
					+ "As of Jasper Server OS 6.0, it is also possible to specify `json` if your reports are designed for data "
					+ "export. For more information, see the JasperReports Library samples documentation.", 
			required=true,
			allowableValues =  {"pdf", "html", "csv", "xls", "xlsx", "docx", "pptx", "odt", "ods", "rtf", "xml", "jrprint", "json", "data_csv", "data_xlsx"},
			example = "html"
			)
    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

	@Schema(
			description = "For HTML output, this property specifies the URL path to use for downloading the attachment files "
					+ "(JavaScript and images). The full path of the default value is:\n\n"
					+ "`{contextPath}/rest_v2/reportExecutions/{reportExecutionId}/exports/{exportExecutionId}/attachments/`\n\n" 
					+ "You can specify a different URL path using the placeholders `{contextPath}`, `{reportExecutionId}`, and "
					+ "`{exportExecutionId}`.", 
			defaultValue = "attachments",
			example = "attachments"
			)
    public String getAttachmentsPrefix() {
        return attachmentsPrefix;
    }

    public void setAttachmentsPrefix(String attachmentsPrefix) {
        this.attachmentsPrefix = attachmentsPrefix;
    }

	@Schema(
			description = "Specifies a single page or page range to generate a partial report. The format is "
					+ "either `{singlePageNumber}` or `{startPageNumber}-{endPageNumber}`",
			example = "2-3"
			)
   public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

	@Schema(description = "A list of parameters and their values.", implementation = ReportParameters.class)
    public ReportParameters getParameters() {
        return parameters;
    }

    public void setParameters(ReportParameters parameters) {
        this.parameters = parameters;
    }
}
