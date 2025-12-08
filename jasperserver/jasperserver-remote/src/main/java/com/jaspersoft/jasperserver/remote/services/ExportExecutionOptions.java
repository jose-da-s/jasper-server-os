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
package com.jaspersoft.jasperserver.remote.services;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: ExportExecutionOptions.java 26599 2012-12-10 13:04:23Z ykovalchyk $
 */
@XmlRootElement(name = "export")
@Schema(name = "export")
public class ExportExecutionOptions {

	public static final String MARKUP_TYPE_FULL = "full";
	public static final String MARKUP_TYPE_EMBEDDABLE = "embeddable";
	
    private String outputFormat;
    private String attachmentsPrefix;
    private ReportOutputPages pages;
    private String baseUrl;
    private String anchor;
    private boolean allowInlineScripts = true;
    private String markupType;
    private Boolean ignorePagination;
    private Boolean clearContextCache = false;
    private Boolean ignoreCancelledReportExecution = false;

	@Schema(
			type="boolean", 
			description = "When set to true, the export output is generated as a single long page. This can be used with HTML output "
					+ "to avoid pagination. When omitted, the ignorePagination property on the JRXML, if any, is used. ", 
			example = "false"
			)
    public Boolean getIgnorePagination() {
        return ignorePagination;
    }


    /**
     * Setter for ignore pagination flag.
     * @param ignorePagination - if true, then one long page will be generated.
     * @return
     */
    public ExportExecutionOptions setIgnorePagination(Boolean ignorePagination) {
        this.ignorePagination = ignorePagination;
        return this;
    }

    public Boolean getClearContextCache() {
        return clearContextCache;
    }

    public ExportExecutionOptions setClearContextCache(Boolean clearContextCache) {
        this.clearContextCache = clearContextCache;
        return this;
    }

    public Boolean getIgnoreCancelledReportExecution() {
        return ignoreCancelledReportExecution;
    }

    public ExportExecutionOptions setIgnoreCancelledReportExecution(Boolean ignoreCancelledReportExecution) {
        this.ignoreCancelledReportExecution = ignoreCancelledReportExecution;
        return this;
    }

    @Schema(description = "Specifies the page anchor name.")
    public String getAnchor() {
        return anchor;
    }

    public ExportExecutionOptions setAnchor(String anchor) {
        this.anchor = anchor;
        return this;
    }

	@Schema(
		description = "Specifies if the HTML output will be generated as full HTML document or as embeddable HTML content.", 
		allowableValues = {MARKUP_TYPE_FULL, MARKUP_TYPE_EMBEDDABLE},
		example = MARKUP_TYPE_FULL
	)
    public String getMarkupType() {
        return markupType;
    }

    public ExportExecutionOptions setMarkupType(String markupType) {
        this.markupType = markupType;
        return this;
    }

	@Schema(
		type="boolean", 
		description = "Affects HTML export only. If true, then inline scripts are allowed, otherwise no inline script is included in the HTML output.", 
		defaultValue = "true",
		example = "false"
	)
    public boolean isAllowInlineScripts() {
        return allowInlineScripts;
    }

    public ExportExecutionOptions setAllowInlineScripts(boolean allowInlineScripts) {
        this.allowInlineScripts = allowInlineScripts;
        return this;
    }

	@Schema(
		description = "Specifies the base URL that the export will use to load static resources such as JavaScript files. "
				+ "You can also set the `deploy.base.url` property in the `.../WEB-INF/js.config.properties` file to set "
				+ "this value permanently. If both are set, the `baseUrl` parameter in this request takes precedence.",
		example = "http://localhost:8080/jasperserver-pro"
	)
	public String getBaseUrl() {
        return baseUrl;
    }

    public ExportExecutionOptions setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ExportExecutionOptions setPages(ReportOutputPages pages) {
        this.pages = pages;
        return this;
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

    public ExportExecutionOptions setAttachmentsPrefix(String attachmentsPrefix) {
        this.attachmentsPrefix = attachmentsPrefix;
        return this;
    }

    @XmlJavaTypeAdapter(ReportOutputPagesToStringXmlAdapter.class)
	@Schema(
		description = "Specifies a single page or page range to generate a partial report. The format is "
				+ "either `{singlePageNumber}` or `{startPageNumber}-{endPageNumber}`",
		implementation = ReportOutputPages.class
	)
    public ReportOutputPages getPages(){
        return pages;
    }

	@Schema(
		description = "Specifies the desired output format: `pdf`, `html`, `csv`, `xls`, `xlsx`, `docx`, `pptx`, `odt`, `ods`, "
				+ "`rtf`, `xml`, `jrprint`.\n\n"
				+ "As of Jasper Server OS 6.0, it is also possible to specify `json` if your reports are designed for data "
				+ "export. For more information, see the JasperReports Library samples documentation.", 
		required=true,
		allowableValues =  {"pdf", "html", "csv", "xls", "xlsx", "docx", "pptx", "odt", "ods", "rtf", "xml", "jrprint", "json", "data_csv", "data_xlsx"},
		example = "html"
	)
    public String getOutputFormat() {
        return outputFormat;
    }

    public ExportExecutionOptions setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExportExecutionOptions)) return false;

        ExportExecutionOptions that = (ExportExecutionOptions) o;

        if (allowInlineScripts != that.allowInlineScripts) return false;
        if (anchor != null ? !anchor.equals(that.anchor) : that.anchor != null) return false;
        if (attachmentsPrefix != null ? !attachmentsPrefix.equals(that.attachmentsPrefix) : that.attachmentsPrefix != null)
            return false;
        if (baseUrl != null ? !baseUrl.equals(that.baseUrl) : that.baseUrl != null) return false;
        if (ignorePagination != null ? !ignorePagination.equals(that.ignorePagination) : that.ignorePagination != null)
            return false;
        if (markupType != null ? !markupType.equals(that.markupType) : that.markupType != null) return false;
        if (outputFormat != null ? !outputFormat.equals(that.outputFormat) : that.outputFormat != null) return false;
        if (pages != null ? !pages.equals(that.pages) : that.pages != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = outputFormat != null ? outputFormat.hashCode() : 0;
        result = 31 * result + (attachmentsPrefix != null ? attachmentsPrefix.hashCode() : 0);
        result = 31 * result + (pages != null ? pages.hashCode() : 0);
        result = 31 * result + (baseUrl != null ? baseUrl.hashCode() : 0);
        result = 31 * result + (anchor != null ? anchor.hashCode() : 0);
        result = 31 * result + (allowInlineScripts ? 1 : 0);
        result = 31 * result + (markupType != null ? markupType.hashCode() : 0);
        result = 31 * result + (ignorePagination != null ? ignorePagination.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExportExecutionOptions{" +
                "outputFormat='" + outputFormat + '\'' +
                ", attachmentsPrefix='" + attachmentsPrefix + '\'' +
                ", pages=" + pages +
                ", baseUrl='" + baseUrl + '\'' +
                ", anchor='" + anchor + '\'' +
                ", allowInlineScripts=" + allowInlineScripts +
                ", markupType='" + markupType + '\'' +
                ", ignorePagination=" + ignorePagination +
                '}';
    }
}
