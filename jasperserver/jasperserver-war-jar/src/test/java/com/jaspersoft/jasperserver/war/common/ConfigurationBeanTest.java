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

import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Andriy Tivodar <ativodar@tibco>
 */
class ConfigurationBeanTest {
    private static final int PAGINATOR_ITEMS_PER_PAGE = 21;
    private static final int PAGINATOR_PAGES_RANGE = 22;
    private static final String CALENDAR_INPUT_JSP = "calendarInputJsp";
    private static final int ROLE_ITEMS_PER_PAGE = 24;
    private static final int TENANT_ITEMS_PER_PAGE = 25;
    private static final String USER_NAME_SEPARATOR = "userNameSeparator";
    private static final String USER_NAME_NOT_SUPPORTED_SYMBOLS = "userNameNotSupportedSymbols";
    private static final String ROLE_NAME_NOT_SUPPORTED_SYMBOLS = "roleNameNotSupportedSymbols";
    private static final String DEFAULT_ROLE = "defaultRole";
    private static final String PASSWORD_MASK = "passwordMask";
    private static final String TENANT_NAME_NOT_SUPPORTED_SYMBOLS = "tenantNameNotSupportedSymbols";
    private static final String TENANT_ID_NOT_SUPPORTED_SYMBOLS = "tenantIdNotSupportedSymbols";
    private static final String RESOURCE_ID_NOT_SUPPORTED_SYMBOLS = "resourceIdNotSupportedSymbols";
    private static final String PUBLIC_FOLDER_URI = "publicFolderUri";
    private static final String THEME_DEFAULT_NAME = "themeDefaultName";
    private static final String THEME_FOLDER_NAME = "themeFolderName";
    private static final String THEME_SERVLET_PREFIX = "themeServletPrefix";
    private static final String DATE_FORMAT = "dateFormat";
    private static final String CURRENT_YEAR_DATE_FORMAT = "currentYearDateFormat";
    private static final String TIMESTAMP_FORMAT = "timestampFormat";
    private static final String TIME_FORMAT = "timeFormat";
    private static final String DATE = "date";
    private static final String CURRENT_YEAR_DATE = "currentYearDate";
    private static final String TIMESTAMP = "timestamp";
    private static final String TIME = "time";
    private static final int ENTITIES_PER_PAGE = 26;
    private static final String TEMP_FOLDER_URI = "tempFolderUri";
    private static final String ORGANIZATIONS_FOLDER_URI = "organizationsFolderUri";
    private static final String JDBC_DRIVERS_FOLDER_URI = "jdbcDriversFolderUri";
    private static final String EMAIL_REG_EXP_PATTERN = "emailRegExpPattern";
    private static final String CONTEXT_PATH = "contextPath";
    private static final int LOCAL_PORT = 27;
    private static final long MAX_FILE_SIZE = 28L;

    private final static String VALUE_ACCESS_GRANT_SCHEMA = "value1";
    private final static String VALUE_CSS = "value3";
    private final static String VALUE_MONGODB_JDBC_CONFIG = "value4";
    private final static String VALUE_AZURE_CERTIFICATE = "value5";
    private final static String VALUE_SECURE_FILE = "value6";
    private final static String VALUE_FONT = "value7";
    private final static String VALUE_IMAGE = "value8";
    private final static String VALUE_JAR = "value9";
    private final static String VALUE_JRXML = "value10";
    private final static String VALUE_RESOURCE_BUNDLE = "value12";
    private final static String VALUE_STYLE_TEMPLATE = "value13";
    private final static String VALUE_XML = "value14";
    private final static String VALUE_PPK = "value15";
    private final static String VALUE_PUB = "value16";
    private final static String VALUE_KEY = "value17";
    private final static String VALUE_MONGO_SCHEMA = "value18";
    private final static String VALUE_UNSPECIFIED = "value19";
    private final static String RESOURCE_LABEL_CONST = "resource.com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource.label";

    private static final LinkedHashMap<String, String> ALL_FILE_RESOURCE_TYPES = new LinkedHashMap<String, String>() {{
        put(ResourceDescriptor.TYPE_ACCESS_GRANT_SCHEMA, VALUE_ACCESS_GRANT_SCHEMA);
        put(FileResource.TYPE_CSS, VALUE_CSS);
        put(FileResource.TYPE_MONGODB_JDBC_CONFIG, VALUE_MONGODB_JDBC_CONFIG);
        put(FileResource.TYPE_AZURE_CERTIFICATE, VALUE_AZURE_CERTIFICATE);
        put(FileResource.TYPE_SECURE_FILE, VALUE_SECURE_FILE);
        put(FileResource.TYPE_FONT, VALUE_FONT);
        put(FileResource.TYPE_IMAGE, VALUE_IMAGE);
        put(FileResource.TYPE_JAR, VALUE_JAR);
        put(FileResource.TYPE_JRXML, VALUE_JRXML);
        put(FileResource.TYPE_RESOURCE_BUNDLE, VALUE_RESOURCE_BUNDLE);
        put(FileResource.TYPE_STYLE_TEMPLATE, VALUE_STYLE_TEMPLATE);
        put(FileResource.TYPE_PPK, VALUE_PPK);
        put(FileResource.TYPE_PUB, VALUE_PUB);
        put(FileResource.TYPE_KEY, VALUE_KEY);
        put(FileResource.TYPE_MONGODB_SCHEMA, VALUE_MONGO_SCHEMA);
        put(FileResource.TYPE_XML, VALUE_XML);
        put(ContentResource.TYPE_UNSPECIFIED, VALUE_UNSPECIFIED);
    }};
    private static final List<String> VIEW_REPORTS_FILTER_LIST = singletonList("viewReportsFilter");
    private static final List<String> OUTPUT_FOLDER_FILTER_LIST = singletonList("outputFolderFilter");
    private static final List OUTPUT_FOLDER_FILTER_PATTERNS_LIST = singletonList(Pattern.compile("outputFolderFilter"));

    private MessageSource messages = mock(MessageSource.class);

    @BeforeEach
    void setup() {
        doReturn(VALUE_ACCESS_GRANT_SCHEMA).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_ACCESS_GRANT_SCHEMA), eq(((Object[]) null)), eq("Access Grant Schema"), any(Locale.class));
        doReturn(VALUE_CSS).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_CSS_FILE), eq(((Object[]) null)), eq("CSS File"), any(Locale.class));
        doReturn(VALUE_MONGODB_JDBC_CONFIG).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_MONGODB_JDBC_CONFIG), eq(((Object[]) null)), eq("MongoDB JDBC Schema"), any(Locale.class));
        doReturn(VALUE_AZURE_CERTIFICATE).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_AZURE_CERTIFICATE), eq(((Object[]) null)), eq("Azure Certificate"), any(Locale.class));
        doReturn(VALUE_SECURE_FILE).when(messages).getMessage(eq(JasperServerConst.TYPE_SECURE_FILE), eq(((Object[]) null)), eq("Secure File"), any(Locale.class));
        doReturn(VALUE_FONT).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_FONT), eq(((Object[]) null)), eq("Font"), any(Locale.class));
        doReturn(VALUE_IMAGE).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_IMAGE), eq(((Object[]) null)), eq("Image"), any(Locale.class));
        doReturn(VALUE_JAR).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_CLASS_JAR), eq(((Object[]) null)), eq("Jar"), any(Locale.class));
        doReturn(VALUE_JRXML).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_SUB_REPORT), eq(((Object[]) null)), eq("Jrxml"), any(Locale.class));
        doReturn(VALUE_RESOURCE_BUNDLE).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_RESOURCE_BUNDLE), eq(((Object[]) null)), eq("Properties"), any(Locale.class));
        doReturn(VALUE_STYLE_TEMPLATE).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_STYLE_TEMPLATE), eq(((Object[]) null)), eq("Style Template"), any(Locale.class));
        doReturn(VALUE_XML).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_XML_FILE), eq(((Object[]) null)), eq("XML File"), any(Locale.class));
        doReturn(VALUE_PPK).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_PPK), eq(((Object[]) null)), eq("Private Key File"), any(Locale.class));
        doReturn(VALUE_PUB).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_PUB), eq(((Object[]) null)), eq("Public Key File"), any(Locale.class));
        doReturn(VALUE_MONGO_SCHEMA).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_MONGODB_SCHEMA), eq(((Object[]) null)), eq("MongoDB Schema File"), any(Locale.class));
        doReturn(VALUE_KEY).when(messages).getMessage(eq(JasperServerConst.TYPE_RSRC_KEY), eq(((Object[]) null)), eq("Secure Key File"), any(Locale.class));
        doReturn(VALUE_UNSPECIFIED).when(messages).getMessage(eq(RESOURCE_LABEL_CONST), eq(((Object[]) null)), eq("Content Resource"), any(Locale.class));

        doReturn(DATE).when(messages).getMessage(eq(DATE_FORMAT), eq(new Object[]{}), any(Locale.class));
        doReturn(CURRENT_YEAR_DATE).when(messages).getMessage(eq(CURRENT_YEAR_DATE_FORMAT), eq(new Object[]{}), any(Locale.class));
        doReturn(TIMESTAMP).when(messages).getMessage(eq(TIMESTAMP_FORMAT), eq(new Object[]{}), any(Locale.class));
        doReturn(TIME).when(messages).getMessage(eq(TIME_FORMAT), eq(new Object[]{}), any(Locale.class));
    }

    @Test
    void getAndSet_instanceWithDefaultValues() {
        final ConfigurationBean instance = new ConfigurationBean();

        assertAll("an instance with default values",
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.isReportLevelConfigurable());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(0, instance.getPaginatorItemsPerPage());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(0, instance.getPaginatorPagesRange());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getMessages());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.isPaginationForSinglePageReport());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getCalendarInputJsp());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(0, instance.getRoleItemsPerPage());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(0, instance.getTenantItemsPerPage());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getUserNameSeparator());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getUserNameNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getRoleNameNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getDefaultRole());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getPasswordMask());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getViewReportsFilterList());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getOutputFolderFilterPatterns());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getTenantNameNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getTenantIdNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getResourceIdNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getPublicFolderUri());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getThemeDefaultName());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getThemeFolderName());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getThemeServletPrefix());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(0, instance.getEntitiesPerPage());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getTempFolderUri());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.getEnableAccessibility());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getOrganizationsFolderUri());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getJdbcDriversFolderUri());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getEmailRegExpPattern());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.isEnableSaveToHostFS());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.isOptimizeJavaScript());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.getDefaultDomainDependentsUseACL());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.getForceDomainDependentsUseACL());
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.getDefaultDontUpdateDomainDependents());
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getContextPath());
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getLocalPort());
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertNull(instance.getMaxFileSize());
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertFalse(instance.isSkipXXECheck());
                    }
                }
        );
    }

    @Test
    void getAndSet_fullyConfiguredInstance() {
        final ConfigurationBean instance = new ConfigurationBean();
        instance.setOutputFolderFilterList(null);

        instance.setReportLevelConfigurable(true);
        instance.setPaginatorItemsPerPage(PAGINATOR_ITEMS_PER_PAGE);
        instance.setPaginatorPagesRange(PAGINATOR_PAGES_RANGE);
        instance.setMessages(messages);
        instance.setPaginationForSinglePageReport(true);
        instance.setCalendarInputJsp(CALENDAR_INPUT_JSP);
        instance.setRoleItemsPerPage(ROLE_ITEMS_PER_PAGE);
        instance.setTenantItemsPerPage(TENANT_ITEMS_PER_PAGE);
        instance.setUserNameSeparator(USER_NAME_SEPARATOR);
        instance.setUserNameNotSupportedSymbols(USER_NAME_NOT_SUPPORTED_SYMBOLS);
        instance.setRoleNameNotSupportedSymbols(ROLE_NAME_NOT_SUPPORTED_SYMBOLS);
        instance.setDefaultRole(DEFAULT_ROLE);
        instance.setPasswordMask(PASSWORD_MASK);
        instance.setViewReportsFilterList(VIEW_REPORTS_FILTER_LIST);
        instance.setOutputFolderFilterList(OUTPUT_FOLDER_FILTER_LIST);
        instance.setTenantNameNotSupportedSymbols(TENANT_NAME_NOT_SUPPORTED_SYMBOLS);
        instance.setTenantIdNotSupportedSymbols(TENANT_ID_NOT_SUPPORTED_SYMBOLS);
        instance.setResourceIdNotSupportedSymbols(RESOURCE_ID_NOT_SUPPORTED_SYMBOLS);
        instance.setPublicFolderUri(PUBLIC_FOLDER_URI);
        instance.setThemeDefaultName(THEME_DEFAULT_NAME);
        instance.setThemeFolderName(THEME_FOLDER_NAME);
        instance.setThemeServletPrefix(THEME_SERVLET_PREFIX);
        instance.setDateFormat(DATE_FORMAT);
        instance.setCurrentYearDateFormat(CURRENT_YEAR_DATE_FORMAT);
        instance.setTimestampFormat(TIMESTAMP_FORMAT);
        instance.setTimeFormat(TIME_FORMAT);
        instance.setEntitiesPerPage(ENTITIES_PER_PAGE);
        instance.setTempFolderUri(TEMP_FOLDER_URI);
        instance.setEnableAccessibility(true);
        instance.setOrganizationsFolderUri(ORGANIZATIONS_FOLDER_URI);
        instance.setJdbcDriversFolderUri(JDBC_DRIVERS_FOLDER_URI);
        instance.setEmailRegExpPattern(EMAIL_REG_EXP_PATTERN);
        instance.setEnableSaveToHostFS(true);
        instance.setOptimizeJavaScript(true);
        instance.setDefaultDomainDependentsUseACL(true);
        instance.setForceDomainDependentsUseACL(true);
        instance.setDefaultDontUpdateDomainDependents(true);
        instance.setContextPath(CONTEXT_PATH);
        instance.setLocalPort(LOCAL_PORT);
        instance.setMaxFileSize(MAX_FILE_SIZE);
        instance.setSkipXXECheck(true);

        assertAll("a fully configured instance",
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.isReportLevelConfigurable());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(ALL_FILE_RESOURCE_TYPES, instance.getAllFileResourceTypes());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(PAGINATOR_ITEMS_PER_PAGE, instance.getPaginatorItemsPerPage());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(PAGINATOR_PAGES_RANGE, instance.getPaginatorPagesRange());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(messages, instance.getMessages());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.isPaginationForSinglePageReport());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(CALENDAR_INPUT_JSP, instance.getCalendarInputJsp());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(ROLE_ITEMS_PER_PAGE, instance.getRoleItemsPerPage());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(TENANT_ITEMS_PER_PAGE, instance.getTenantItemsPerPage());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(USER_NAME_SEPARATOR, instance.getUserNameSeparator());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(USER_NAME_NOT_SUPPORTED_SYMBOLS, instance.getUserNameNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(ROLE_NAME_NOT_SUPPORTED_SYMBOLS, instance.getRoleNameNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(DEFAULT_ROLE, instance.getDefaultRole());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(PASSWORD_MASK, instance.getPasswordMask());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(VIEW_REPORTS_FILTER_LIST, instance.getViewReportsFilterList());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertPatternListEquals(OUTPUT_FOLDER_FILTER_PATTERNS_LIST, instance.getOutputFolderFilterPatterns());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(TENANT_NAME_NOT_SUPPORTED_SYMBOLS, instance.getTenantNameNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(TENANT_ID_NOT_SUPPORTED_SYMBOLS, instance.getTenantIdNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(RESOURCE_ID_NOT_SUPPORTED_SYMBOLS, instance.getResourceIdNotSupportedSymbols());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(PUBLIC_FOLDER_URI, instance.getPublicFolderUri());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(THEME_DEFAULT_NAME, instance.getThemeDefaultName());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(THEME_FOLDER_NAME, instance.getThemeFolderName());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(THEME_SERVLET_PREFIX, instance.getThemeServletPrefix());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(DATE, instance.getDateFormat());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(CURRENT_YEAR_DATE, instance.getCurrentYearDateFormat());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(TIMESTAMP, instance.getTimestampFormat());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(TIME, instance.getTimeFormat());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(ENTITIES_PER_PAGE, instance.getEntitiesPerPage());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(TEMP_FOLDER_URI, instance.getTempFolderUri());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.getEnableAccessibility());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(ORGANIZATIONS_FOLDER_URI, instance.getOrganizationsFolderUri());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(JDBC_DRIVERS_FOLDER_URI, instance.getJdbcDriversFolderUri());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(EMAIL_REG_EXP_PATTERN, instance.getEmailRegExpPattern());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.isEnableSaveToHostFS());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.isOptimizeJavaScript());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.getDefaultDomainDependentsUseACL());
                    }
                },
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.getForceDomainDependentsUseACL());
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.getDefaultDontUpdateDomainDependents());
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(CONTEXT_PATH, instance.getContextPath());
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(LOCAL_PORT, ((int) instance.getLocalPort()));
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertEquals(MAX_FILE_SIZE, ((long) instance.getMaxFileSize()));
                    }
                }
                ,
                new Executable() {
                    @Override
                    public void execute() {
                        assertTrue(instance.isSkipXXECheck());
                    }
                }
        );
    }

    private static void assertPatternListEquals(List<Pattern> expected, List<Pattern> result) {
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).pattern(), result.get(i).pattern());
        }
    }

}
