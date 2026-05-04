/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
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

import com.jaspersoft.jasperserver.api.common.util.LocaleHelper;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import net.sf.jasperreports.engine.JRParameter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import jakarta.servlet.ServletContext;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */
class HeartbeatBeanTest {

    private static final String TEST_URL = "https://www.google.com";
    private static final int TEST_MAX_CACHE_SIZE = 100;
    private static final long TEST_CACHE_SAVE_INTERVAL = 300L;
    private static final String TEST_PRODUCT_NAME = "TEST_PRODUCT_NAME";
    private static final String TEST_PRODUCT_VERSION = "TEST_PRODUCT_VERSION";
    private static final String TEST_CUSTOM_SD_CLASS_PATTERN = "TEST_CUSTOM_SD_CLASS_PATTERN";
    private static final String TEST_CUSTOM_SD_CLASS_PATTERN_ALT = "TEST_CUSTOM_SD_CLASS_PATTERN_ALT";
    private static final List<String> TEST_CUSTOM_DS_CLASS_PATTERNS = asList(TEST_CUSTOM_SD_CLASS_PATTERN, TEST_CUSTOM_SD_CLASS_PATTERN_ALT);
    private static final String TEST_SERVER_INFO = "TEST_SERVER_INFO";
    private static final String TEST_REAL_PATH = "TEST_REAL_PATH";
    private static final String TEST_DB_NAME = "TEST_DB_NAME";
    private static final String TEST_DB_VERSION = "TEST_DB_VERSION";
    private static final Integer TEST_NUMBER_OF_TENANTS = 200;

    private static final LocaleHelper LOCALE_HELPER = LocaleHelper.getInstance();
    private static final Locale TEST_LOCALE = Locale.CANADA;
    private static final Locale TEST_DISPLAY_LOCALE = Locale.CHINA;
    private static final UserLocale TEST_USER_LOCALE = new UserLocale(
            LOCALE_HELPER.getCode(TEST_LOCALE),
            TEST_LOCALE.getDisplayName(TEST_DISPLAY_LOCALE)
    );

    private static final ReportDataSource TEST_JDBC_DATASOURCE = mock(ReportDataSource.class);
    private static final ReportDataSource TEST_JDBC_DATASOURCE_ALT = mock(ReportDataSource.class);
    private static final List TEST_JDBC_DATASOURCES = asList(TEST_JDBC_DATASOURCE, TEST_JDBC_DATASOURCE_ALT);

    private static final ReportDataSource TEST_JNDI_DATASOURCE = mock(ReportDataSource.class);
    private static final ReportDataSource TEST_JNDI_DATASOURCE_ALT = mock(ReportDataSource.class);
    private static final List TEST_JNDI_DATASOURCES = asList(TEST_JNDI_DATASOURCE, TEST_JNDI_DATASOURCE_ALT);

    private static final CustomReportDataSource TEST_CUSTOM_REPORT_DATA_SOURCE = mock(CustomReportDataSource.class);
    private static final CustomReportDataSource TEST_CUSTOM_REPORT_DATA_SOURCE_ALT = mock(CustomReportDataSource.class);
    private static final List TEST_CUSTOM_REPORT_DATA_SOURCES = asList(TEST_CUSTOM_REPORT_DATA_SOURCE, TEST_CUSTOM_REPORT_DATA_SOURCE_ALT);

    private HeartbeatBean objectUnderTest = new HeartbeatBean();

    private ServletContext mock_servlet_context = mock(ServletContext.class);
    private DataSource mock_data_source = mock(DataSource.class);
    private Connection mock_connection = mock(Connection.class);
    private DatabaseMetaData mock_database_metadata = mock(DatabaseMetaData.class);
    private HeartbeatContributor mock_aws_ec2_contributor = mock(HeartbeatContributor.class);
    private HeartbeatContributor mock_heartbeat_contributor = mock(HeartbeatContributor.class);
    private TenantService mock_tenant_service = mock(TenantService.class);
    private LocalesList mock_locales_list = mock(LocalesList.class);
    private RepositoryService mock_repository_service = mock(RepositoryService.class);
    private EngineService mock_engine_service = mock(EngineService.class);
    private ReportDataSourceService mock_report_datasource_service = mock(ReportDataSourceService.class);

    private ReportDataSourceService report_datasource_service = new ReportDataSourceService() {
        @Override
        public void setReportParameterValues(Map parameterValues) {
            parameterValues.put(JRParameter.REPORT_CONNECTION, mock_connection);
        }

        @Override
        public void closeConnection() {

        }
    };

    @Nested
    @DisplayName("accessors and mutators")
    class GetAndSet {
        @Test
        void getAndSet_instanceWithDefaultValues() {
            final HeartbeatBean instance = new HeartbeatBean();

            assertAll("an instance with default values",
                    new Executable() {
                        @Override
                        public void execute() {
                            assertFalse(instance.getEnabled());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertFalse(instance.getAskForPermission());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertFalse(instance.getPermissionGranted());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertNull(instance.getUrl());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertEquals(0, instance.getMaxCacheSize());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertEquals(0, instance.getCacheSaveInterval());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertNull(instance.getProductVersion());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertNull(instance.getCustomDSClassPatterns());
                        }
                    }
            );
        }

        @Test
        void getAndSet_fullyConfiguredInstance() {
            final HeartbeatBean instance = new HeartbeatBean();
            instance.setEnabled(true);
            instance.setAskForPermission(true);
            instance.setPermissionGranted(true);
            instance.setUrl(TEST_URL);
            instance.setMaxCacheSize(TEST_MAX_CACHE_SIZE);
            instance.setCacheSaveInterval(TEST_CACHE_SAVE_INTERVAL);
            instance.setProductVersion(TEST_PRODUCT_VERSION);
            instance.setCustomDSClassPatterns(TEST_CUSTOM_DS_CLASS_PATTERNS);

            assertAll("a fully configured instance",
                    new Executable() {
                        @Override
                        public void execute() {
                            assertTrue(instance.getEnabled());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertTrue(instance.getAskForPermission());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertTrue(instance.getPermissionGranted());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertEquals(TEST_URL, instance.getUrl());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertEquals(TEST_MAX_CACHE_SIZE, instance.getMaxCacheSize());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertEquals(TEST_CACHE_SAVE_INTERVAL, instance.getCacheSaveInterval());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertEquals(TEST_PRODUCT_VERSION, instance.getProductVersion());
                        }
                    },
                    new Executable() {
                        @Override
                        public void execute() {
                            assertEquals(TEST_CUSTOM_DS_CLASS_PATTERNS, instance.getCustomDSClassPatterns());
                        }
                    }
            );
        }
    }

    @Nested
    @DisplayName("init")
    class Init {
        @Test
        @Disabled("need refactor to verify test result")
        void init_notEnabled() {
            objectUnderTest.init();

            // check steps:
            // initHeartbeat wasn't called
        }

        @Test
        @Disabled("need refactor to create mocks and verify result")
        void init_enabled() {
            prepareForInit();

            objectUnderTest.init();

            // check steps:
            // initHeartbeat was called
        }
    }

    @Nested
    @DisplayName("haveToAskForPermissionNow")
    class HaveToAskForPermissionNow {
        @Test
        void haveToAskForPermissionNow_enabledIsFalse_false() {
            boolean actual = objectUnderTest.haveToAskForPermissionNow();
            assertFalse(actual);
        }

        @Test
        void haveToAskForPermissionNow_enabledIsTrueAndAskForPermissionIsFalse_false() {
            objectUnderTest.setEnabled(true);
            boolean actual = objectUnderTest.haveToAskForPermissionNow();
            assertFalse(actual);
        }

        @Test
        void haveToAskForPermissionNow_enabledIsTrueAndAskForPermissionIsTrueAndLocalIdPropertiesDoNotContainPermissionGranted_true() {
            objectUnderTest.setEnabled(true);
            objectUnderTest.setAskForPermission(true);
            boolean actual = objectUnderTest.haveToAskForPermissionNow();
            assertTrue(actual);
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void haveToAskForPermissionNow_enabledIsTrueAndAskForPermissionIsTrueAndLocalIdPropertiesContainPermissionGrantedAndPermissionGranted_false() {
            objectUnderTest.setAskForPermission(true);

            objectUnderTest.setProductVersion(TEST_PRODUCT_VERSION);
            objectUnderTest.setUrl(TEST_URL);
            prepareForInit();
            objectUnderTest.init();

            objectUnderTest.permitCall(true);

            boolean actual = objectUnderTest.haveToAskForPermissionNow();
            assertFalse(actual);
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void haveToAskForPermissionNow_enabledIsTrueAndAskForPermissionIsTrueAndLocalIdPropertiesContainPermissionGrantedAndPermissionNotGranted_false() {
            objectUnderTest.setAskForPermission(true);

            objectUnderTest.setProductVersion(TEST_PRODUCT_VERSION);
            objectUnderTest.setUrl(TEST_URL);
            prepareForInit();
            objectUnderTest.init();

            objectUnderTest.permitCall(false);

            boolean actual = objectUnderTest.haveToAskForPermissionNow();
            assertFalse(actual);
        }
    }

    @Nested
    @DisplayName("permitCall")
    class PermitCall {
        @Test
        @Disabled("need refactor to verify test result")
        void permitCall_enabledIsFalseAndIsCallPermittedIsTrue() {
            objectUnderTest.permitCall(true);

            // check steps:
            // localIdProperties was set property PROPERTY_HEARTBEAT_ID if heartbeatId was set

            // httpCall was called (we need be able to mock httpClient)
            // properties were not saved
        }

        @Test
        @Disabled("need refactor to verify test result")
        void permitCall_enabledIsFalseAndIsCallPermittedIsFalse() {
            objectUnderTest.permitCall(false);

            // check steps:
            // localIdProperties was set property PROPERTY_HEARTBEAT_ID if heartbeatId was set

            // httpCall was called (we need be able to mock httpClient)
            // properties were not saved
        }

        @Test
        @Disabled("need refactor to verify test result")
        void permitCall_enabledIsTrueAndIsCallPermittedIsTrue() {
            objectUnderTest.setUrl(TEST_URL);
            prepareForInit();
            objectUnderTest.init();

            objectUnderTest.permitCall(true);

            // check steps:
            // localIdProperties was set property PROPERTY_HEARTBEAT_ID if heartbeatId was set

            // httpCall was called (we need be able to mock httpClient)
            // properties were saved
        }

        @Test
        @Disabled("need refactor to verify test result")
        void permitCall_enabledIsTrueAndIsCallPermittedIsFalse() {
            objectUnderTest.setUrl(TEST_URL);
            prepareForInit();
            objectUnderTest.init();

            objectUnderTest.permitCall(false);

            // check steps:
            // localIdProperties was set property PROPERTY_HEARTBEAT_ID if heartbeatId was set

            // httpCall was called (we need be able to mock httpClient)
            // properties were saved
        }

        @Test
        void permitCall_enabledIsTrueAndUrlIsNull_throwsException() {
            objectUnderTest.setEnabled(true);
            objectUnderTest.setUrl(null);

            assertThrows(NullPointerException.class, new Executable() {
                @Override
                public void execute() {
                    objectUnderTest.permitCall(true);
                }
            });
        }
    }

    @Nested
    @DisplayName("isMakingCalls")
    class IsMakingCalls {
        @Test
        void isMakingCalls_enabledIsFalse_false() {
            // perform test action
            boolean actual = objectUnderTest.isMakingCalls();

            assertFalse(actual);
        }

        @Test
        void isMakingCalls_enabledIsTrueAndAskForPermissionIsFalse_false() {
            objectUnderTest.setEnabled(true);

            // perform test action
            boolean actual = objectUnderTest.isMakingCalls();

            assertFalse(actual);
        }

        @Test
        void isMakingCalls_enabledIsTrueAndAskForPermissionIsTrueAndLocalIdPropertiesDoNotContainPermissionGranted_false() {
            objectUnderTest.setEnabled(true);
            objectUnderTest.setAskForPermission(true);

            // perform test action
            boolean actual = objectUnderTest.isMakingCalls();

            assertFalse(actual);
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void isMakingCalls_enabledIsTrueAndAskForPermissionIsTrueAndLocalIdPropertiesContainPermissionGrantedAndPermissionGranted_true() {
            objectUnderTest.setAskForPermission(true);

            objectUnderTest.setProductVersion(TEST_PRODUCT_VERSION);
            objectUnderTest.setUrl(TEST_URL);
            prepareForInit();
            objectUnderTest.init();

            objectUnderTest.permitCall(true);

            // perform test action
            boolean actual = objectUnderTest.isMakingCalls();

            assertTrue(actual);
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void isMakingCalls_enabledIsTrueAndAskForPermissionIsTrueAndLocalIdPropertiesContainPermissionGrantedAndPermissionNotGranted_false() {
            objectUnderTest.setAskForPermission(true);

            objectUnderTest.setProductVersion(TEST_PRODUCT_VERSION);
            objectUnderTest.setUrl(TEST_URL);
            prepareForInit();
            objectUnderTest.init();

            objectUnderTest.permitCall(false);

            // perform test action
            boolean actual = objectUnderTest.isMakingCalls();

            assertFalse(actual);
        }

        @Test
        void isMakingCalls_enabledIsTrueAndAskForPermissionIsFalseAndLocalIdPropertiesDoNotContainPermissionGrantedAndPermissionGranted_true() {
            objectUnderTest.setEnabled(true);
            objectUnderTest.setPermissionGranted(true);

            // perform test action
            boolean actual = objectUnderTest.isMakingCalls();

            assertTrue(actual);
        }

        @Test
        void isMakingCalls_enabledIsTrueAndAskForPermissionIsFalseAndLocalIdPropertiesDoNotContainPermissionGrantedAndPermissionNotGranted_false() {
            objectUnderTest.setEnabled(true);
            objectUnderTest.setPermissionGranted(false);

            // perform test action
            boolean actual = objectUnderTest.isMakingCalls();

            assertFalse(actual);
        }
    }

    @Nested
    @DisplayName("updateClientInfo")
    class UpdateClientInfo {
        @Test
        @Disabled("need refactor to verify test result")
        void updateClientInfo_isMakingCallsIsFalse_heartbeatClientInfoIsNull() {
            // perform test action
            objectUnderTest.updateClientInfo(null);

            // Check steps:
            // clientInfoCache wasn't updated
            // httpCall wasn't called
            // saveLocalIdProperties wasn't called
        }

        @Test
        void updateClientInfo_isMakingCallsIsTrue_heartbeatClientInfoIsNull_throwsException() {
            objectUnderTest.setEnabled(true);
            objectUnderTest.setPermissionGranted(true);

            assertThrows(NullPointerException.class, new Executable() {
                @Override
                public void execute() {
                    objectUnderTest.updateClientInfo(null);
                }
            });
        }

        @Test
        @Disabled("need refactor to verify test result")
        void updateClientInfo_isMakingCallsIsTrue_heartbeatClientInfoIsSomeInstance() {
            objectUnderTest.setEnabled(true);
            objectUnderTest.setPermissionGranted(true);

            HeartbeatClientInfo mock_heartbeatClientInfo = mock(HeartbeatClientInfo.class);

            // perform test action
            objectUnderTest.updateClientInfo(mock_heartbeatClientInfo);

            // check steps:
            // clientInfoCache was updated
            // httpCall wasn't called
            // saveLocalIdProperties wasn't called
        }

        @Test
        @Disabled("need refactor to verify test result")
        void updateClientInfo_isMakingCallsIsTrueAndMaxCacheSizeIs1_heartbeatClientInfoIsSomeInstance() {
            objectUnderTest.setPermissionGranted(true);
            objectUnderTest.setMaxCacheSize(1);

            prepareForInit();
            objectUnderTest.init();

            HeartbeatClientInfo mock_heartbeatClientInfo = mock(HeartbeatClientInfo.class);

            // perform test action
            objectUnderTest.updateClientInfo(mock_heartbeatClientInfo);

            // check steps:
            // clientInfoCache was updated
            // httpCall wasn't called
            // saveLocalIdProperties wasn't called
        }

        @Test
        @Disabled("need refactor to verify test result")
        void updateClientInfo_isMakingCallsIsTrueAndMaxCacheSizeIs1AndClientInfoCacheSizeIs2_heartbeatClientInfoIsSomeInstance() {
            objectUnderTest.setPermissionGranted(true);
            objectUnderTest.setMaxCacheSize(1);

            objectUnderTest.setUrl(TEST_URL);

            prepareForInit();
            objectUnderTest.init();

            // refactor to be able fill clientInfoCache
            HeartbeatClientInfo mock_heartbeatClientInfo_alt = mock(HeartbeatClientInfo.class);
            objectUnderTest.updateClientInfo(mock_heartbeatClientInfo_alt);

            HeartbeatClientInfo mock_heartbeatClientInfo = mock(HeartbeatClientInfo.class);

            // perform test action
            objectUnderTest.updateClientInfo(mock_heartbeatClientInfo);

            // check steps:
            // clientInfoCache was updated
            // httpCall was called
            // saveLocalIdProperties wasn't called
        }

        @Test
        @Disabled("need refactor to verify test result")
        void updateClientInfo_isMakingCallsIsTrueAndMaxCacheSizeIs1AndCacheSaveIntervalIsExceeded_heartbeatClientInfoIsSomeInstance() throws InterruptedException {
            objectUnderTest.setPermissionGranted(true);
            objectUnderTest.setMaxCacheSize(1);
            objectUnderTest.setCacheSaveInterval(1);

            prepareForInit();
            objectUnderTest.init();

            HeartbeatClientInfo mock_heartbeatClientInfo = mock(HeartbeatClientInfo.class);

            // adding a delay to simulate that time interval for saving was exceeded.
            Thread.sleep(2);

            // perform test action
            objectUnderTest.updateClientInfo(mock_heartbeatClientInfo);

            // check steps:
            // clientInfoCache was updated
            // httpCall wasn't called
            // saveLocalIdProperties wasn't called
        }
    }

    @Nested
    @DisplayName("contributeToHttpCall")
    class ContributeToHttpCall {
        @Test
        @Disabled("need refactor to mock saving files")
        void contributeToHttpCall_fullSetup() {
            prepareHeartbeatContributor();
            prepareTenantService();
            prepareLocalesList(new UserLocale[]{TEST_USER_LOCALE});

            prepareForInit();
            objectUnderTest.init();

            // performing test action
            HeartbeatCall mock_heartbeatCall = mock(HeartbeatCall.class);
            objectUnderTest.contributeToHttpCall(mock_heartbeatCall);

            // verifying
            verifyHeartbeatCallAddedParameters(mock_heartbeatCall);

            verify(mock_heartbeatCall).addParameter(eq("tenants"), eq(String.valueOf(TEST_NUMBER_OF_TENANTS)));
            verify(mock_heartbeatCall).addParameter(eq("userLocales"), eq(TEST_LOCALE.toString()));

            verify(mock_aws_ec2_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
            verify(mock_heartbeat_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void contributeToHttpCall_localesListIsNull() {
            prepareHeartbeatContributor();
            prepareTenantService();

            prepareForInit();
            objectUnderTest.init();

            // performing test action
            HeartbeatCall mock_heartbeatCall = mock(HeartbeatCall.class);
            objectUnderTest.contributeToHttpCall(mock_heartbeatCall);

            // verifying
            verifyHeartbeatCallAddedParameters(mock_heartbeatCall);

            verify(mock_heartbeatCall).addParameter(eq("tenants"), eq(String.valueOf(TEST_NUMBER_OF_TENANTS)));
            verify(mock_heartbeatCall, never()).addParameter(eq("userLocales"), anyString());

            verify(mock_aws_ec2_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
            verify(mock_heartbeat_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void contributeToHttpCall_localesIsNull() {
            prepareHeartbeatContributor();
            prepareTenantService();
            prepareLocalesList(null);

            prepareForInit();
            objectUnderTest.init();

            // performing test action
            HeartbeatCall mock_heartbeatCall = mock(HeartbeatCall.class);
            objectUnderTest.contributeToHttpCall(mock_heartbeatCall);

            // verifying phase
            verifyHeartbeatCallAddedParameters(mock_heartbeatCall);

            verify(mock_heartbeatCall).addParameter(eq("tenants"), eq(String.valueOf(TEST_NUMBER_OF_TENANTS)));
            verify(mock_heartbeatCall, never()).addParameter(eq("userLocales"), anyString());

            verify(mock_aws_ec2_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
            verify(mock_heartbeat_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void contributeToHttpCall_localesIsEmpty() {
            prepareHeartbeatContributor();
            prepareTenantService();
            prepareLocalesList(new UserLocale[]{});

            prepareForInit();
            objectUnderTest.init();

            // performing test action
            HeartbeatCall mock_heartbeatCall = mock(HeartbeatCall.class);
            objectUnderTest.contributeToHttpCall(mock_heartbeatCall);

            // verifying
            verifyHeartbeatCallAddedParameters(mock_heartbeatCall);

            verify(mock_heartbeatCall).addParameter(eq("tenants"), eq(String.valueOf(TEST_NUMBER_OF_TENANTS)));
            verify(mock_heartbeatCall, never()).addParameter(eq("userLocales"), anyString());

            verify(mock_aws_ec2_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
            verify(mock_heartbeat_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void contributeToHttpCall_optionalContributorIsNull() {
            prepareTenantService();
            prepareLocalesList(new UserLocale[]{TEST_USER_LOCALE});

            prepareForInit();
            objectUnderTest.init();

            // performing test action
            HeartbeatCall mock_heartbeatCall = mock(HeartbeatCall.class);
            objectUnderTest.contributeToHttpCall(mock_heartbeatCall);

            // verifying
            verifyHeartbeatCallAddedParameters(mock_heartbeatCall);

            verify(mock_heartbeatCall).addParameter(eq("tenants"), eq(String.valueOf(TEST_NUMBER_OF_TENANTS)));
            verify(mock_heartbeatCall).addParameter(eq("userLocales"), eq(TEST_LOCALE.toString()));

            verify(mock_aws_ec2_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
            verify(mock_heartbeat_contributor, never()).contributeToHttpCall(eq(mock_heartbeatCall));
        }

        @Test
        @Disabled("need refactor to mock saving files")
        void contributeToHttpCall_tenantServiceIsNull() {
            prepareHeartbeatContributor();
            prepareLocalesList(new UserLocale[]{TEST_USER_LOCALE});

            prepareForInit();
            objectUnderTest.init();

            // performing test action
            HeartbeatCall mock_heartbeatCall = mock(HeartbeatCall.class);
            objectUnderTest.contributeToHttpCall(mock_heartbeatCall);

            // verifying
            verifyHeartbeatCallAddedParameters(mock_heartbeatCall);

            verify(mock_heartbeatCall, never()).addParameter(eq("tenants"), anyString());
            verify(mock_heartbeatCall).addParameter(eq("userLocales"), eq(TEST_LOCALE.toString()));

            verify(mock_aws_ec2_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
            verify(mock_heartbeat_contributor).contributeToHttpCall(eq(mock_heartbeatCall));
        }
    }

    @Nested
    @DisplayName("createDatabaseInfoCache")
    class CreateDatabaseInfoCache {
        @Test
        void createDatabaseInfoCache_fullSetup() {
            prepareRepositoryService();
            prepareEngineService();
            prepareDataSource();

            // performing test action
            objectUnderTest.createDatabaseInfoCache();

            // verifying
            verify(mock_repository_service, times(2)).loadClientResources(any(FilterCriteria.class));
            verify(mock_report_datasource_service, times(2)).setReportParameterValues(any(HashMap.class));
        }

        @Test
        void createDatabaseInfoCache_repositoryServiceIsNull() {
            prepareEngineService();
            prepareDataSource();

            // performing test action
            objectUnderTest.createDatabaseInfoCache();

            // verifying
            verify(mock_repository_service, never()).loadClientResources(any(FilterCriteria.class));
            verify(mock_report_datasource_service, never()).setReportParameterValues(any(HashMap.class));
        }

        @Test
        void createDatabaseInfoCache_engineServiceIsNull() {
            prepareRepositoryService();
            prepareDataSource();

            // performing test action
            objectUnderTest.createDatabaseInfoCache();

            // verifying
            verify(mock_repository_service, times(2)).loadClientResources(any(FilterCriteria.class));
            verify(mock_report_datasource_service, never()).setReportParameterValues(any(HashMap.class));
        }

        @Test
        void createDatabaseInfoCache_dataSourceThrowsExceptions() {
            prepareRepositoryService();
            prepareEngineService();
            prepareDataSourceWithThrowingExceptions();

            // performing test action
            objectUnderTest.createDatabaseInfoCache();

            // verifying
            verify(mock_repository_service, times(2)).loadClientResources(any(FilterCriteria.class));
            verify(mock_report_datasource_service, times(2)).setReportParameterValues(any(HashMap.class));
        }
    }

    @Nested
    @DisplayName("call")
    class Call {
        @Test
        @Disabled("need refactor to verify test result")
        void call_fullSetup() {
            objectUnderTest.setUrl(TEST_URL);

            prepareForInit();
            objectUnderTest.init();

            // perform test action
            objectUnderTest.call();

            // check steps:
            // callCount was increased
            // properties were saved
        }

        @Test
        @Disabled("need refactor to verify test result")
        void call_askForPermissionIsTrue() {
            objectUnderTest.setAskForPermission(true);

            prepareForInit();
            objectUnderTest.init();

            // perform test action
            objectUnderTest.call();

            // check steps:
            // callCount was increased
            // properties were not saved
        }

        @Test
        @Disabled("need refactor to verify test result")
        void call_permissionGrantedIsTrue() {
            objectUnderTest.setUrl(TEST_URL);
            objectUnderTest.setPermissionGranted(true);

            prepareForInit();
            objectUnderTest.init();

            // perform test action
            objectUnderTest.call();

            // check steps:
            // callCount was increased
            // properties were not saved
        }

        @Test
        @Disabled("need refactor to verify test result")
        void call_enabledIsTrue() {
            objectUnderTest.setEnabled(false);

            // perform test action
            objectUnderTest.call();

            // check steps:
            // callCount was increased
            // properties were not saved
        }
    }

    @Nested
    @DisplayName("createCustomDSInfoCache")
    class CreateCustomDSInfoCache {
        @Test
        @Disabled("need refactor to verify test result")
        void createCustomDSInfoCache_fullSetup() {
            objectUnderTest.setCustomDSClassPatterns(TEST_CUSTOM_DS_CLASS_PATTERNS);
            prepareRepositoryServiceWithCustomReportDataSources();

            // perform test action
            objectUnderTest.createCustomDSInfoCache();

            // check steps:
            // filters were prepared
            // datasources were found for filters
            // customDSInfoCache was updated
        }

        @Test
        @Disabled("need refactor to mock saving files and verify results")
        void createCustomDSInfoCache_dataSourcesIsNull() {
            objectUnderTest.setCustomDSClassPatterns(TEST_CUSTOM_DS_CLASS_PATTERNS);
            prepareRepositoryServiceWithNullDataSources();

            // perform test action
            objectUnderTest.createCustomDSInfoCache();

            // check steps:
            // filters were prepared
            // datasources were not found for filters
            // customDSInfoCache was not updated
        }

        @Test
        @Disabled("need refactor to verify test result")
        void createCustomDSInfoCache_dataSourcesIsEmpty() {
            objectUnderTest.setCustomDSClassPatterns(TEST_CUSTOM_DS_CLASS_PATTERNS);
            prepareRepositoryServiceWithEmptyDataSources();

            // perform test action
            objectUnderTest.createCustomDSInfoCache();

            // check steps:
            // filters were prepared
            // datasources were not found for filters
            // customDSInfoCache was not updated
        }

        @Test
        @Disabled("need refactor to verify test result")
        void createCustomDSInfoCache_repositoryServiceIsNull() {
            objectUnderTest.setCustomDSClassPatterns(TEST_CUSTOM_DS_CLASS_PATTERNS);

            // perform test action
            objectUnderTest.createCustomDSInfoCache();

            // check steps:
            // filters were prepared
            // datasources were not found for filters
            // customDSInfoCache was not updated
        }

        @Test
        @Disabled("need refactor to verify test result")
        void createCustomDSInfoCache_customDSClassPatternsIsNull() {
            objectUnderTest.setCustomDSClassPatterns(null);

            // perform test action
            objectUnderTest.createCustomDSInfoCache();

            // check steps:
            // filters were not prepared
            // datasources were not found for filters
            // customDSInfoCache was not updated
        }

        @Test
        @Disabled("need refactor to verify test result")
        void createCustomDSInfoCache_customDSClassPatternsIsEmpty() {
            objectUnderTest.setCustomDSClassPatterns(new ArrayList<String>());

            // perform test action
            objectUnderTest.createCustomDSInfoCache();

            // check steps:
            // filters were not prepared
            // datasources were not found for filters
            // customDSInfoCache was not updated
        }
    }

    /*
     * Custom verifying
     */

    private void verifyHeartbeatCallAddedParameters(HeartbeatCall call) {
        verify(call).addParameter(eq("callCount"), eq(String.valueOf(0)));
        verify(call).addParameter(eq("osName"), eq(System.getProperty("os.name")));
        verify(call).addParameter(eq("osVersion"), eq(System.getProperty("os.version")));
        verify(call).addParameter(eq("javaVendor"), eq(System.getProperty("java.vendor")));
        verify(call).addParameter(eq("javaVersion"), eq(System.getProperty("java.version")));
        verify(call).addParameter(eq("serverInfo"), eq(TEST_SERVER_INFO));
        verify(call).addParameter(eq("productName"), eq(TEST_PRODUCT_NAME));
        verify(call).addParameter(eq("productVersion"), eq(TEST_PRODUCT_VERSION));
        verify(call).addParameter(eq("dbName"), eq(TEST_DB_NAME));
        verify(call).addParameter(eq("dbVersion"), eq(TEST_DB_VERSION));
        verify(call).addParameter(eq("serverLocale"), eq(Locale.getDefault().toString()));
    }

    /*
     * Helpers
     */

    private void prepareForInit() {
        objectUnderTest.setEnabled(true);

        objectUnderTest.setProductVersion(TEST_PRODUCT_VERSION);
        objectUnderTest.setProductName(TEST_PRODUCT_NAME);

        prepareServletContext();
        prepareDataSource();
        prepareAwsEc2Contributor();
    }

    private void prepareServletContext() {
        when(mock_servlet_context.getServerInfo()).thenReturn(TEST_SERVER_INFO);
        when(mock_servlet_context.getRealPath("/")).thenReturn(TEST_REAL_PATH);

        objectUnderTest.setServletContext(mock_servlet_context);
    }

    private void prepareDataSource() {
        try {
            when(mock_database_metadata.getDatabaseProductName()).thenReturn(TEST_DB_NAME);
            when(mock_database_metadata.getDatabaseProductVersion()).thenReturn(TEST_DB_VERSION);

            when(mock_connection.getMetaData()).thenReturn(mock_database_metadata);

            when(mock_data_source.getConnection()).thenReturn(mock_connection);
        } catch (SQLException e) {
            // Should never happen
            throw new RuntimeException(e);
        }

        objectUnderTest.setDataSource(mock_data_source);
    }

    private void prepareDataSourceWithThrowingExceptions() {
        try {
            when(mock_database_metadata.getDatabaseProductName()).thenThrow(new SQLException());
            when(mock_database_metadata.getDatabaseProductVersion()).thenThrow(new SQLException());

            when(mock_connection.getMetaData()).thenThrow(new SQLException());

            when(mock_data_source.getConnection()).thenReturn(mock_connection);
        } catch (SQLException e) {
            // Should never happen
            throw new RuntimeException(e);
        }

        objectUnderTest.setDataSource(mock_data_source);
    }

    private void prepareAwsEc2Contributor() {
        objectUnderTest.setAwsEc2Contributor(mock_aws_ec2_contributor);
    }

    private void prepareTenantService() {
        when(mock_tenant_service.getNumberOfTenants(null)).thenReturn(TEST_NUMBER_OF_TENANTS);
        objectUnderTest.setTenantService(mock_tenant_service);
    }

    private void prepareLocalesList(UserLocale[] locales) {
        when(mock_locales_list.getUserLocales(Locale.getDefault())).thenReturn(locales);
        objectUnderTest.setLocalesList(mock_locales_list);
    }

    private void prepareHeartbeatContributor() {
        objectUnderTest.setContributor(mock_heartbeat_contributor);
    }

    private void prepareRepositoryService() {
        when(mock_repository_service.loadClientResources(
                any(FilterCriteria.class)
                )
        ).thenReturn(TEST_JDBC_DATASOURCES);

        when(mock_repository_service.loadClientResources(
                any(FilterCriteria.class)
                )
        ).thenReturn(TEST_JNDI_DATASOURCES);

        objectUnderTest.setRepositoryService(mock_repository_service);
    }

    private void prepareRepositoryServiceWithCustomReportDataSources() {
        when(mock_repository_service.loadClientResources(
                any(FilterCriteria.class)
                )
        ).thenReturn(TEST_CUSTOM_REPORT_DATA_SOURCES);

        when(TEST_CUSTOM_REPORT_DATA_SOURCE.getServiceClass()).thenReturn(CustomReportDataSource.class.getName());
        when(TEST_CUSTOM_REPORT_DATA_SOURCE_ALT.getServiceClass()).thenReturn(CustomReportDataSource.class.getName());

        objectUnderTest.setRepositoryService(mock_repository_service);
    }

    private void prepareRepositoryServiceWithNullDataSources() {
        when(mock_repository_service.loadClientResources(any(FilterCriteria.class))).thenReturn(null);
        objectUnderTest.setRepositoryService(mock_repository_service);
    }

    private void prepareRepositoryServiceWithEmptyDataSources() {
        when(mock_repository_service.loadClientResources(any(FilterCriteria.class))).thenReturn(new ArrayList());
        objectUnderTest.setRepositoryService(mock_repository_service);
    }

    private void prepareEngineService() {
        when(
                mock_engine_service.createDataSourceService(TEST_JDBC_DATASOURCE)
        ).thenReturn(report_datasource_service);
        when(
                mock_engine_service.createDataSourceService(TEST_JNDI_DATASOURCE)
        ).thenReturn(report_datasource_service);

        when(
                mock_engine_service.createDataSourceService(TEST_JDBC_DATASOURCE_ALT)
        ).thenReturn(mock_report_datasource_service);
        when(
                mock_engine_service.createDataSourceService(TEST_JNDI_DATASOURCE_ALT)
        ).thenReturn(mock_report_datasource_service);

        objectUnderTest.setEngineService(mock_engine_service);
    }

}

