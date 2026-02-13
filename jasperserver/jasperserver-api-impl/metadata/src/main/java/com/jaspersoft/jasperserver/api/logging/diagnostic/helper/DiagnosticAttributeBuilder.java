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
package com.jaspersoft.jasperserver.api.logging.diagnostic.helper;

import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttribute;
import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttributeImpl;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Diagnostic params builder which will be used in services which implements <code>Diagnostic</code> interface for
 * easier diagnostic data collecting.
 *
 * @author vsabadosh
 */
public class DiagnosticAttributeBuilder {
    private static final Log log = LogFactory.getLog(DiagnosticAttributeBuilder.class);

    
    //Diagnostic sessions attributes section
    /* Currently logged in users */
    public final static String TOTAL_LOGGED_IN_USERS = "LoggedInUsersCount";
    /* Currently active sessions by users */
    public final static String TOTAL_SESSIONS_BY_USER = "LoggedInUsersList";

    //Diagnostic reports attributes section
    public final static String RUNNING_REPORT_COUNT = "RunningReportsCount";
    public final static String RUNNING_REPORTS_LIST = "RunningReportsList";
    public final static String RUNNING_REPORT_ASYNCTASKCOUNT ="RunningAsyncReportsCountCuml";
    public final static String RUNNING_REPORT_ASYNCPOOLSIZE ="RunningAsyncReportsPoolSize";
    public final static String RUNNING_REPORT_ASYNCACTIVETASKCOUNT ="RunningAsyncReportsActiveTaskCount";
    public final static String RUNNING_REPORT_SYNCTASKCOUNT ="RunningSyncReportsCountCuml";
    public final static String RUNNING_REPORT_ERRORCOUNT ="RunningReportsErrorsCuml";
    public final static String RUNNING_REPORT_COUNT_CUML ="RunningReportsCountCuml";

    //Diagnostic scheduler attributes section
    /* Total scheduled jobs */
    public final static String TOTAL_SCHEDULED_JOBS = "ScheduledJobsCount";
    /* Running jobs */
    public final static String TOTAL_RUNNING_JOBS = "RunningJobsCount";
    public final static String RUNNING_JOBS_LIST = "RunningJobsList";

    //Diagnostic JS About section
    public final static String VERSION = "Version";
    public final static String SOURCE_EDITION = "SourceEdition";
    public final static String BUILD = "Build";
    public final static String IS_LICENSE_VALID = "IsLicenseValid";
    public final static String SUPPORTED_FEATURES = "SupportedFeatures";
    public final static String LICENSE_EXPIRATION_DATE = "LicenseExpirationDate";
    public final static String PRODUCT_EDITION_NAME = "ProductEditionName";
    public final static String LICENSE_TYPE = "LicenseType";
    public final static String LICENSE_USER_COUNT = "LicenseUsersCount";
    public final static String LICENSE_CPUS_COUNT = "LicenseCpusCount";
    public final static String LICENSE_CORES_COUNT = "LicenseCoresCount";
    public final static String LICENSE_ENVIRONMENT_TYPE = "LicenseEnvironmentType";
    public final static String LICENSE_USER_COUNT_EXCEEDED = "LicenseUserCountExceeded";
    public final static String LICENSE_ABOUT_TO_EXPIRE = "LicenseAboutToExpire";
    public final static String PRODUCT_TYPE_NAME = "ProductTypeName";
    // Report run (execution) license specific diagnostic data
    public final static String TOTAL_EXECUTIONS_DURING_RPOT_COUNT = "ReportRunsTotalCount";
    public final static String EXECUTIONS_DURING_LAST_DAY_COUNT = "ReportRunsTodayCount";
    public final static String EXECUTIONS_BY_DAY_FOR_RPOT_LIST = "ReportRunsByDayList";

    //Diagnostic JS settings section
    public final static String AWS_SETTINGS = "AwsSettings";
    public final static String ADHOC_SETTINGS = "AdhocSettings";
    public final static String LOG_SETTINGS = "LogSettings";
    public final static String GLOBAL_PROPERTIES_LIST = "GlobalPropertiesList";

    //Diagnostic repository section
    public final static String TOTAL_USERS_COUNT = "TotalUsersCount";
    public final static String TOTAL_ENABLED_USERS_COUNT = "TotalEnabledUsersCount";
    public final static String TOTAL_ROLES_COUNT = "TotalRolesCount";

    //Repository  section
    public final static String TOTAL_REPORTS_COUNT = "TotalReportsCount";
    public final static String TOTAL_FOLDERS_COUNT = "TotalFoldersCount";
    public final static String TOTAL_DASHBOARDS_COUNT = "TotalDashboardsCount";
    public final static String TOTAL_ADHOC_VIEWS_COUNT = "TotalAdhocViewsCount";
    public final static String TOTAL_REPORT_OUTPUTS_COUNT = "TotalReportOutputsCount";
    public final static String TOTAL_DOMAINS_COUNT = "TotalDomainsCount";
    public final static String TOTAL_DATA_SOURCES_COUNT = "TotalDataSourcesCount";
    public final static String TOTAL_ORGANIZATIONS_COUNT = "TotalOrganizationsCount";

    //Repository Database Section
    public final static String DATABASE_PRODUCT_NAME="DatabaseProductName";
    public final static String DATABASE_PRODUCT_VERSION="DatabaseProductVersion";
    public final static String DRIVER_NAME="DriverName";
    public final static String SQL_KEYWORDS="SQLKeywords";
    public final static String DATABASE_URL="URL";
    public final static String DATABASE_USER_NAME="UserName";
    public final static String JDBC_MAJOR_VERSION="JDBCMajorVersion";
    public final static String JDBC_MINOR_VERSION="JDBCMinorVersion";
    public final static String DATABASE_MAX_ROW_SIZE="MaxRowSize";
    public final static String DATABASE_MAX_STATEMENT_LENGTH="MaxStatementLength";
    public final static String DATABASE_MAX_CONNECTIONS="MaxConnections";
    public final static String DATABASE_MAX_CHAR_LENGTH="MaxCharLiteralLength";
    public final static String DATABASE_MAX_COLUMNS_TABLE="MaxColumnsInTable";
    public final static String DATABASE_MAX_COLUMNS_SELECT="MaxColumnsInSelect";
    public final static String DATABASE_MAX_COLUMNS_GROUP="MaxColumnsInGroupBy";
    public final static String DATABASE_MAX_COLUMN_NAME_LENGTH="MaxColumnNameLength";

    //Diagnostic hibernate section
//    public final static String HIBERNATE_SESSION_FACTORY_JNDI_NAME = "SessionFactoryJNDIName";
    public final static String HIBERNATE_START_TIME = "StartTime";
    public final static String HIBERNATE_STATISTICS_ENABLED = "StatisticsEnabled";
    public final static String HIBERNATE_ENTITY_DELETE_COUNT = "EntityDeleteCount";
    public final static String HIBERNATE_ENTITY_INSERT_COUNT = "EntityInsertCount";
    public final static String HIBERNATE_ENTITY_LOAD_COUNT = "EntityLoadCount";
    public final static String HIBERNATE_ENTITY_FETCH_COUNT = "EntityFetchCount";
    public final static String HIBERNATE_ENTITY_UPDATE_COUNT = "EntityUpdateCount";
    public final static String HIBERNATE_QUERY_EXECUTION_COUNT = "QueryExecutionCount";
    public final static String HIBERNATE_QUERY_CACHE_HIT_COUNT = "QueryCacheHitCount";
    public final static String HIBERNATE_QUERY_EXECUTION_MAX_TIME = "QueryExecutionMaxTime";
    public final static String HIBERNATE_QUERY_CACHE_MIS_COUNT = "QueryCacheMissCount";
    public final static String HIBERNATE_QUERY_CACHE_PUT_COUNT = "QueryCachePutCount";
    public final static String HIBERNATE_FLUSH_COUNT = "FlushCount";
    public final static String HIBERNATE_CONNECT_COUNT = "ConnectCount";
    public final static String HIBERNATE_SECOND_LEVEL_CACHE_HIT_COUNT = "SecondLevelCacheHitCount";
    public final static String HIBERNATE_SECOND_LEVEL_CACHE_MISS_COUNT = "SecondLevelCacheMissCount";
    public final static String HIBERNATE_SECOND_LEVEL_CACHE_PUT_COUNT = "SecondLevelCachePutCount";
    public final static String HIBERNATE_SESSION_CLOSE_COUNT = "SessionCloseCount";
    public final static String HIBERNATE_SESSION_OPEN_COUNT = "SessionOpenCount";
    public final static String HIBERNATE_COLLECTION_LOAD_COUNT = "CollectionLoadCount";
    public final static String HIBERNATE_COLLECTION_FETCH_COUNT = "CollectionFetchCount";
    public final static String HIBERNATE_COLLECTION_UPDATE_COUNT = "CollectionUpdateCount";
    public final static String HIBERNATE_COLLECTION_REMOVE_COUNT = "CollectionRemoveCount";
    public final static String HIBERNATE_COLLECTION_RECREATE_COUNT = "CollectionRecreateCount";
    public final static String HIBERNATE_COLLECTION_ROLE_NAMES = "CollectionRoleNames";
    public final static String HIBERNATE_ENTITY_NAMES = "EntityNames";
    public final static String HIBERNATE_QUERIES = "Queries";
    public final static String HIBERNATE_SECOND_LEVEL_CACHE_REGION_NAMES = "SecondLevelCacheRegionNames";
    public final static String HIBERNATE_SUCCESSFUL_TRANSACTION_COUNT = "SuccessfulTransactionCount";
    public final static String HIBERNATE_TRANSACTION_COUNT = "TransactionCount";
    public final static String HIBERNATE_CLOSE_STATEMENT_COUNT = "CloseStatementCount";
    public final static String HIBERNATE_PREPARE_STATEMENT_COUNT = "PrepareStatementCount";
    public final static String HIBERNATE_OPTIMISTIC_FAILURE_COUNT = "OptimisticFailureCount";
    public final static String HIBERNATE_QUERY_EXECUTION_MAX_TIME_QUERY_STRING = "QueryExecutionMaxTimeQueryString";

    //Diagnostic EhCache Statistics Section
    public final static String EHCACHE_STAT_CACHEHIT_PERCENTAGE = "CacheHitPercentage";
    public final static String EHCACHE_STAT_CACHEHITS = "CacheHits";
    public final static String EHCACHE_STAT_CACHEMISS_PERCENTAGE = "CacheMissPercentage";
    public final static String EHCACHE_STAT_CACHEMISSES = "CacheMisses";
    public final static String EHCACHE_STAT_DISKSTORECOUNT = "DiskStoreObjectCount";
    public final static String EHCACHE_STAT_DISKHITT_PERCENTAGE = "OnDiskHitPercentage";
    public final static String EHCACHE_STAT_DISKHITS = "OnDiskHits";
    public final static String EHCACHE_STAT_DISKMISSES = "OnDiskMisses";
    public final static String EHCACHE_STAT_MEMORYSTORECOUNT = "MemoryStoreObjectCount";
    public final static String EHCACHE_STAT_MEMORYHITS = "InMemoryHits";
    public final static String EHCACHE_STAT_MEMORYHITT_PERCENTAGE = "InMemoryHitPercentage";
    public final static String EHCACHE_STAT_MEMORYMISSES = "InMemoryMisses";
    public final static String EHCACHE_STAT_OFFHEAPSTORECOUNT = "OffHeapStoreObjectCount";
    public final static String EHCACHE_STAT_OFFHEAPHITS = "OffHeapHits";
    public final static String EHCACHE_STAT_OFFHEAPHITT_PERCENTAGE = "OffHeapHitPercentage";
    public final static String EHCACHE_STAT_OFFHEAPMISSES = "OffHeapMisses";
    public final static String EHCACHE_STAT_OBJECTCOUNT = "ObjectCount";
    public final static String EHCACHE_STAT_WRITEMAXQUEUE = "WriteMaxQueueSize";
    public final static String EHCACHE_STAT_WRITEQUEUELENGTH = "WriteQueueLength";
    public final static String EHCACHE_STAT_ACCURACY = "StatisticsAccuracy";
    public final static String EHCACHE_STAT_ACCURACYDESCR = "StatisticsAccuracyDescription";

    //Diagnostic EhCache Configuration Section
    public final static String EHCACHE_CONF_STATISTICS = "Conf.Statistics";
    public final static String EHCACHE_CONF_DISKEXPIRYTHREAD = "Conf.DiskExpiryThreadIntervalSeconds";
    public final static String EHCACHE_CONF_DISKEPERSISTENT = "DiskPersistent";
    public final static String EHCACHE_CONF_DISKSPOOL = "Conf.DiskSpoolBufferSizeMB";
    public final static String EHCACHE_CONF_ETERNAL = "Eternal";
    public final static String EHCACHE_CONF_LOGGING = "Conf.LoggingEnabled";
    public final static String EHCACHE_CONF_MBYTE_LOCALDISK = "Conf.MaxBytesLocalDisk";
    public final static String EHCACHE_CONF_MBYTE_LOCALHEAP = "Conf.MaxBytesLocalHeap";
    public final static String EHCACHE_CONF_MBYTE_LOCALOFFHEAP = "Conf.MaxBytesLocalOffHeap";
    public final static String EHCACHE_CONF_MELEMENTS_LOCALDISK = "Conf.MaxElementsOnDisk";
    public final static String EHCACHE_CONF_MELEMENTS_MEMORY = "Conf.MaxElementsInMemory";
    public final static String EHCACHE_CONF_MENTRYES_MEMORY = "Conf.MaxEntriesLocalDisk";
    public final static String EHCACHE_CONF_MENTRYES_LOCALHEAP = "Conf.MaxEntriesLocalHeap";
    public final static String EHCACHE_CONF_MEMORYSTORE_POLICY = "Conf.MemoryStoreEvictionPolicy";
    public final static String EHCACHE_CONF_OVERFLOW_DISK = "OverflowToDisk";
    public final static String EHCACHE_CONF_OVERFLOW_OFFHEAP = "OverflowToOffHeap";
    public final static String EHCACHE_CONF_TIME_IDLE = "Conf.TimeToIdleSeconds";
    public final static String EHCACHE_CONF_TIME_LIVE = "Conf.TimeToLiveSeconds";

    public final static String DIAGNOSTIC_ATTRIBUTE_MESSAGES_PREFIX ="diagnosticAttribute.desc.";

    //Diagnostic external system section
    public final static String EXT_AVAILABLEPROCESSORS = "AvailableLogicalProcessors";
    public final static String EXT_FREEMEMORY = "FreeMemory";
    public final static String EXT_MAXMEMORY = "MaxMemory";
    public final static String EXT_TOTALMEMORY = "TotalMemory";
    public final static String EXT_ENVIRONMENT = "Environment";
    public final static String EXT_PROPERTIES = "Properties";
    public final static String EXT_JVMHEAPMEMORY = "JvmHeapMemory";
    public final static String EXT_JVMNONHEAPMEMORY = "JvmNonHeapMemory";
    public final static String EXT_OSNAME = "OsName";
    public final static String EXT_OSARCH = "OsArchitecture";
    public final static String EXT_OSVERSION = "OsVersion";
    public final static String EXT_OSSYSTEMLOADAVERAGE = "OsSystemLoadAverage";
    public final static String EXT_OSTOTALPHYSICALMEMORY = "OsTotalPhysicalMemorySize";
    public final static String EXT_OSTOTALSWAPSPACESIZE = "OsTotalSwapSpaceSize";
    public final static String EXT_OSFREESWAPSPACESIZE = "OsFreeSwapSpaceSize";
    public final static String EXT_OSFREEPHYSICALMEMORY = "OsFreePhysicalMemorySize";
    public final static String EXT_OSPROCESSORCPUTIME = "OsProcessCpuTime";
    public final static String EXT_OSCOMMITEDVIRTUALMEMORYSIZE = "OsCommittedVirtualMemorySize";
    public final static String EXT_CLASSLOADERTOTALLOADEDCLASSESCOUNT = "ClTotalLoadedClassCount";
    public final static String EXT_CLASSLOADERUNLOADEDCLASSCOUNT = "ClUnloadedClassCount";
    public final static String EXT_CLASSLOADERLOADEDCLASSCOUNT = "ClLoadedClassCount";
    public final static String EXT_RUNTIMEBOOTCLASSPATH = "RtBootClassPath";
    public final static String EXT_RUNTIMECLASSPATH = "RtClassPath";
    public final static String EXT_RUNTIMELIBRARYPATH = "RtLibraryPath";
    public final static String EXT_RUNTIMEUPTIME = "RtUpTime";
    public final static String EXT_RUNTIMEVMNAME = "RtVmName";
    public final static String EXT_RUNTIMEVMVENDOR = "RtVmVendor";
    public final static String EXT_RUNTIMEVMVERSION = "RtVmVersion";
    public final static String EXT_RUNTIMEINPUTARGUMENTS = "RtInputArguments";
    public final static String EXT_DISKSFREESPACE = "DisksFreeSpace";

    //Diagnostic data for target diagnostic section(session attributes, report attributes,..)
    private Map<DiagnosticAttribute, DiagnosticCallback> diagnosticData;

    private static MessageSource messageSource;

    public DiagnosticAttributeBuilder() {
        this.diagnosticData = new HashMap<DiagnosticAttribute, DiagnosticCallback>();
    }

    /**
     *
     * @param diagnosticAttribute diagnostic attribute.
     * @param callback diagnostic callback.
     *
     * @return <code>this Diagnostic Attributes Builder</code>
     */
    public DiagnosticAttributeBuilder addDiagnosticAttribute(String diagnosticAttribute, DiagnosticCallback callback) {
        String attributeDescription = "";
        try {
            attributeDescription = messageSource.getMessage(DIAGNOSTIC_ATTRIBUTE_MESSAGES_PREFIX + diagnosticAttribute, new Object[]{}, Locale.getDefault());
        } catch (Exception ex) {

        }
        DiagnosticAttribute attributeInfo = new DiagnosticAttributeImpl(diagnosticAttribute, getAttributeType(callback), attributeDescription);
        this.diagnosticData.put(attributeInfo, callback);
        return this;
    }

    private String getAttributeType(DiagnosticCallback callback) {
        final Type[] genericInterfaces = callback.getClass().getGenericInterfaces();
        for (Type type : genericInterfaces) {
            if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == DiagnosticCallback.class) {
                return (((ParameterizedType) type).getActualTypeArguments()[0]).toString();
            }
        }
        return "java.lang.Object";
    }

    public Map<DiagnosticAttribute, DiagnosticCallback> build() {
        return diagnosticData;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

}
