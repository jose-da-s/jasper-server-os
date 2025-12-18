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
package com.jaspersoft.jasperserver.war.common;

import com.jaspersoft.jasperserver.api.common.domain.ValidationConfiguration;
import com.jaspersoft.jasperserver.api.common.util.DateTimeConfiguration;
import com.jaspersoft.jasperserver.api.engine.common.domain.ReportEngineConfiguration;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.SchedulingConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantConfiguration;
import com.jaspersoft.jasperserver.api.metadata.user.domain.UserAndRoleConfiguration;
import com.jaspersoft.jasperserver.api.security.SecuritySettings;
import com.jaspersoft.jasperserver.core.util.XMLUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id
 */
public class ConfigurationBean implements DateTimeConfiguration, WebConfiguration,
        UserAndRoleConfiguration, TenantConfiguration, RepositoryConfiguration,
        SchedulingConfiguration, ReportEngineConfiguration, SecuritySettings,
        ValidationConfiguration
{
    @Autowired
    @Qualifier("messageSource")
	private MessageSource messages;

	private int paginatorItemsPerPage;
	private int paginatorPagesRange;
	private boolean reportLevelConfigurable;
	
	private boolean paginationForSinglePageReport;
	
	private String calendarInputJsp;

    private int roleItemsPerPage;

    private int tenantItemsPerPage;

    private String userNameNotSupportedSymbols;

    private String roleNameNotSupportedSymbols;

    private String userNameSeparator;

    private String defaultRole;

    private String passwordMask;

    private List viewReportsFilterList;
    
    private List outputFolderFilterList;
    private List outputFolderFilterPatterns;

	private String tenantNameNotSupportedSymbols;

    private String tenantIdNotSupportedSymbols;

    private String resourceIdNotSupportedSymbols;

    private String publicFolderUri;

    private String themeDefaultName;
    private String themeFolderName;
    private String themeServletPrefix;

    private String dateFormat;
    private String currentYearDateFormat;
    private String timestampFormat;
    private String timeFormat;

    private int entitiesPerPage;
    private String tempFolderUri;
    private String organizationsFolderUri;

    private String jdbcDriversFolderUri;
    private String emailRegExpPattern;

    private boolean enableSaveToHostFS;

    private Boolean optimizeJavaScript;

    private boolean enableAccessibility;

    
    private boolean defaultDomainDependentsUseACL;
    private boolean forceDomainDependentsUseACL;

    private boolean defaultDontUpdateDomainDependents;

    private String contextPath = null;
    private Integer localPort = null;
    

    private Long maxFileSize;

    private boolean skipXXECheck;

	private List<String> topicsURIParentSQLPatternList;

    /**
	 * @return Returns the reportLevelConfigurable.
	 */
	@Override
    public boolean isReportLevelConfigurable() {
		return reportLevelConfigurable;
	}

	/**
	 * @param reportLevelConfigurable The reportLevelConfigurable to set.
	 */
    public void setReportLevelConfigurable(boolean reportLevelConfigurable) {
		this.reportLevelConfigurable = reportLevelConfigurable;
	}

	@Override
    public Map getAllFileResourceTypes() {
		Map allTypes = new LinkedHashMap();
        allTypes.put(FileResource.TYPE_ACCESS_GRANT_SCHEMA,
				    messages.getMessage(JasperServerConst.TYPE_RSRC_ACCESS_GRANT_SCHEMA, null, "Access Grant Schema", LocaleContextHolder.getLocale())); // pro-only
        allTypes.put(ContentResource.TYPE_UNSPECIFIED,
                messages.getMessage(JasperServerConst.TYPE_RSRC_CONTENT_RESOURCE, null, "Content Resource", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_CSS,
                    messages.getMessage(JasperServerConst.TYPE_RSRC_CSS_FILE, null, "CSS File", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_MONGODB_JDBC_CONFIG,
                    messages.getMessage(JasperServerConst.TYPE_RSRC_MONGODB_JDBC_CONFIG, null, "MongoDB JDBC Schema", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_AZURE_CERTIFICATE,
                messages.getMessage(JasperServerConst.TYPE_RSRC_AZURE_CERTIFICATE, null, "Azure Certificate", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_SECURE_FILE,
                    messages.getMessage(JasperServerConst.TYPE_SECURE_FILE, null, "Secure File", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_FONT,
					messages.getMessage(JasperServerConst.TYPE_RSRC_FONT, null, "Font", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_IMAGE,
					messages.getMessage(JasperServerConst.TYPE_RSRC_IMAGE, null, "Image", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_JAR,
					messages.getMessage(JasperServerConst.TYPE_RSRC_CLASS_JAR, null, "Jar", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_JRXML,
					messages.getMessage(JasperServerConst.TYPE_RSRC_SUB_REPORT, null, "Jrxml", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_RESOURCE_BUNDLE,
					messages.getMessage(JasperServerConst.TYPE_RSRC_RESOURCE_BUNDLE, null, "Properties", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_STYLE_TEMPLATE,
				    messages.getMessage(JasperServerConst.TYPE_RSRC_STYLE_TEMPLATE, null, "Style Template", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_XML,
				    messages.getMessage(JasperServerConst.TYPE_RSRC_XML_FILE, null, "XML File", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_MONGODB_SCHEMA,
                messages.getMessage(JasperServerConst.TYPE_RSRC_MONGODB_SCHEMA, null, "MongoDB Schema File", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_KEY,
                messages.getMessage(JasperServerConst.TYPE_RSRC_KEY, null, "Secure Key File", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_PUB,
                messages.getMessage(JasperServerConst.TYPE_RSRC_PUB, null, "Public Key File", LocaleContextHolder.getLocale()));
        allTypes.put(FileResource.TYPE_PPK,
                messages.getMessage(JasperServerConst.TYPE_RSRC_PPK, null, "Private Key File", LocaleContextHolder.getLocale()));
        allTypes.put(ContentResource.TYPE_UNSPECIFIED,
				    messages.getMessage("resource.com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource.label", null, "Content Resource", LocaleContextHolder.getLocale()));
		return allTypes;
	}

	@Override
    public int getPaginatorItemsPerPage()
	{
		return paginatorItemsPerPage;
	}

    public void setPaginatorItemsPerPage(int paginatorItemsPerPage)
	{
		this.paginatorItemsPerPage = paginatorItemsPerPage;
	}

	@Override
    public int getPaginatorPagesRange()
	{
		return paginatorPagesRange;
	}

    public void setPaginatorPagesRange(int paginatorPagesRange)
	{
		this.paginatorPagesRange = paginatorPagesRange;
	}


	public MessageSource getMessages()
	{
		return messages;
	}

	public void setMessages(MessageSource messages)
	{
		this.messages = messages;
	}

	@Override
    public boolean isPaginationForSinglePageReport() {
		return paginationForSinglePageReport;
	}

    public void setPaginationForSinglePageReport(
            boolean paginationForSinglePageReport) {
		this.paginationForSinglePageReport = paginationForSinglePageReport;
	}

	@Override
    public String getCalendarInputJsp() {
		return calendarInputJsp;
	}

    public void setCalendarInputJsp(String calendarInputJsp) {
		this.calendarInputJsp = calendarInputJsp;
	}

    @Override
    public int getRoleItemsPerPage() {
        return roleItemsPerPage;
    }

    public void setRoleItemsPerPage(int roleItemsPerPage) {
        this.roleItemsPerPage = roleItemsPerPage;
    }

    @Override
    public int getTenantItemsPerPage() {
        return tenantItemsPerPage;
    }

    public void setTenantItemsPerPage(int tenantItemsPerPage) {
        this.tenantItemsPerPage = tenantItemsPerPage;
    }

    @Override
    public String getUserNameSeparator() {
        return userNameSeparator;
    }

    public void setUserNameSeparator(String userNameSeparator) {
        this.userNameSeparator = userNameSeparator;
    }

    @Override
    public String getUserNameNotSupportedSymbols() {
        return userNameNotSupportedSymbols;
    }

    public void setUserNameNotSupportedSymbols(String userNameNotSupportedSymbols) {
        this.userNameNotSupportedSymbols = userNameNotSupportedSymbols;
    }

    @Override
    public String getRoleNameNotSupportedSymbols() {
        return roleNameNotSupportedSymbols;
    }

    public void setRoleNameNotSupportedSymbols(String roleNameNotSupportedSymbols) {
        this.roleNameNotSupportedSymbols = roleNameNotSupportedSymbols;
    }

    @Override
    public String getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    @Override
    public String getPasswordMask() {
        return passwordMask;
    }

    public void setPasswordMask(String passwordMask) {
        this.passwordMask = passwordMask;
    }

    @Override
    public List getViewReportsFilterList() {
        return viewReportsFilterList;
    }

    public void setViewReportsFilterList(List viewReportsFilterList) {
        this.viewReportsFilterList = viewReportsFilterList;
    }

    public void setOutputFolderFilterList(List outputFolderFilterList) {
		this.outputFolderFilterList = outputFolderFilterList;
		compileOutputFolderFilterPatterns();
	}

	private void compileOutputFolderFilterPatterns() {
		if (outputFolderFilterList == null) {
			outputFolderFilterPatterns = new ArrayList(0);
		} else {
			outputFolderFilterPatterns = new ArrayList(outputFolderFilterList.size());
			for (Iterator it = outputFolderFilterList.iterator(); it.hasNext();) {
				String filter = (String) it.next();
				Pattern filterPattern = Pattern.compile(filter);
				outputFolderFilterPatterns.add(filterPattern);
			}
		}
	}

    public List getOutputFolderFilterPatterns() {
        return outputFolderFilterPatterns;
    }

    @Override
    public String getTenantNameNotSupportedSymbols() {
        return tenantNameNotSupportedSymbols;
    }

    public void setTenantNameNotSupportedSymbols(String tenantNameNotSupportedSymbols) {
        this.tenantNameNotSupportedSymbols = tenantNameNotSupportedSymbols;
    }

    @Override
    public String getTenantIdNotSupportedSymbols() {
        return tenantIdNotSupportedSymbols;
    }

    public void setTenantIdNotSupportedSymbols(String tenantIdNotSupportedSymbols) {
        this.tenantIdNotSupportedSymbols = tenantIdNotSupportedSymbols;
    }

    @Override
    public String getResourceIdNotSupportedSymbols() {
        return resourceIdNotSupportedSymbols;
    }

    public void setResourceIdNotSupportedSymbols(String resourceIdNotSupportedSymbols) {
        this.resourceIdNotSupportedSymbols = resourceIdNotSupportedSymbols;    
    }

    @Override
    public String getPublicFolderUri() {
        return publicFolderUri;
    }

    public void setPublicFolderUri(String publicFolderUri) {
        this.publicFolderUri = publicFolderUri;
    }

    @Override
    public String getThemeDefaultName() {
        return themeDefaultName;
    }

    public void setThemeDefaultName(String themeDefaultName) {
        this.themeDefaultName = themeDefaultName;
    }

    @Override
    public String getThemeFolderName() {
        return themeFolderName;
    }

    public void setThemeFolderName(String themeFolderName) {
        this.themeFolderName = themeFolderName;
    }

    @Override
    public String getThemeServletPrefix() {
        return themeServletPrefix;
    }

    public void setThemeServletPrefix(String themeServletPrefix) {
        this.themeServletPrefix = themeServletPrefix;
    }

    @Override
    public String getDateFormat() {
        return messages.getMessage(dateFormat, new Object[] {}, LocaleContextHolder.getLocale());
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public String getCurrentYearDateFormat() {
        return messages.getMessage(currentYearDateFormat, new Object[] {}, LocaleContextHolder.getLocale());
    }

    public void setCurrentYearDateFormat(String currentYearDateFormat) {
        this.currentYearDateFormat = currentYearDateFormat;
    }

    @Override
    public String getTimestampFormat() {
        return messages.getMessage(timestampFormat, new Object[] {}, LocaleContextHolder.getLocale());
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    @Override
    public String getTimeFormat() {
        return messages.getMessage(timeFormat, new Object[] {}, LocaleContextHolder.getLocale());
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    @Override
    public int getEntitiesPerPage() {
        return entitiesPerPage;
    }

    public void setEntitiesPerPage(int entitiesPerPage) {
        this.entitiesPerPage = entitiesPerPage;
    }

    @Override
    public String getTempFolderUri() {
        return tempFolderUri;
    }

    public void setTempFolderUri(String tempFolderUri) {
        this.tempFolderUri = tempFolderUri;
    }

    @Override
    public boolean getEnableAccessibility() {
        return enableAccessibility;
    }

    public void setEnableAccessibility(boolean enableAccessibility) {
        this.enableAccessibility = enableAccessibility;
    }

    @Override
    public String getOrganizationsFolderUri() {
        return organizationsFolderUri;
    }

    public void setOrganizationsFolderUri(String organizationsFolderUri) {
        this.organizationsFolderUri = organizationsFolderUri;
    }

    @Override
    public String getJdbcDriversFolderUri() {
        return jdbcDriversFolderUri;
    }

    public void setJdbcDriversFolderUri(String jdbcDriversFolderUri) {
        this.jdbcDriversFolderUri = jdbcDriversFolderUri;
    }

    @Override
    public String getEmailRegExpPattern() {
        return emailRegExpPattern;
    }

    public void setEmailRegExpPattern(String emailRegExpPattern) {
        this.emailRegExpPattern = emailRegExpPattern;
    }

    @Override
    public boolean isEnableSaveToHostFS() {
        return enableSaveToHostFS;
    }

    public void setEnableSaveToHostFS(boolean enableSaveToHostFS) {
        this.enableSaveToHostFS = enableSaveToHostFS;
    }

    @Override
    public Boolean isOptimizeJavaScript() {
        return optimizeJavaScript != null ? optimizeJavaScript : false;
    }

    public void setOptimizeJavaScript(Boolean optimizeJavaScript) {
        this.optimizeJavaScript = optimizeJavaScript;
    }
    

    public boolean getDefaultDomainDependentsUseACL() {
        return defaultDomainDependentsUseACL;
    }
    public void setDefaultDomainDependentsUseACL(boolean defaultDomainDependentsUseACL) {
        this.defaultDomainDependentsUseACL = defaultDomainDependentsUseACL;
    }
    public boolean getForceDomainDependentsUseACL() {
        return forceDomainDependentsUseACL;
    }
    public void setForceDomainDependentsUseACL(boolean forceDomainDependentsUseACL) {
        this.forceDomainDependentsUseACL = forceDomainDependentsUseACL;
    }

    public boolean getDefaultDontUpdateDomainDependents() {
        return defaultDontUpdateDomainDependents;
    }
    public void setDefaultDontUpdateDomainDependents(boolean defaultDontUpdateDomainDependents) {
        this.defaultDontUpdateDomainDependents = defaultDontUpdateDomainDependents;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public Integer getLocalPort() {
        return localPort;
    }

    @Override
    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }
    @Override
    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    @Override
    public boolean isSkipXXECheck() {
        return skipXXECheck;
    }

    public void setSkipXXECheck(boolean skipXXECheck) {
        this.skipXXECheck = skipXXECheck;
        XMLUtil.setSkipXXECheck(skipXXECheck);
    }

	@Override
    public List<String> getTopicsURIParentSQLPatternList() {
		return topicsURIParentSQLPatternList;
	}

    public void setTopicsURIParentSQLPatternList(List<String> topicsURIParentSQLPatternList) {
		this.topicsURIParentSQLPatternList = topicsURIParentSQLPatternList;
	}

    public String getServerTimezoneId() {
        return TimeZone.getDefault().getID();
    }

}
