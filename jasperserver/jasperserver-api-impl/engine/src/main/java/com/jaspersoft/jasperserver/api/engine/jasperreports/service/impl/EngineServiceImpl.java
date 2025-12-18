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
package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;

import com.google.common.base.Optional;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.ValidationResult;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationDetailImpl;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationResultImpl;
import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import com.jaspersoft.jasperserver.api.common.util.diagnostic.DiagnosticSnapshotPropertyHelper;
import com.jaspersoft.jasperserver.api.common.util.diagnostic.FilterBy;
import com.jaspersoft.jasperserver.api.engine.common.domain.Request;
import com.jaspersoft.jasperserver.api.engine.common.domain.Result;
import com.jaspersoft.jasperserver.api.engine.common.service.BuiltInParameterProvider;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.IDataSourceAwareQueryManipulator;
import com.jaspersoft.jasperserver.api.engine.common.service.IQueryManipulator;
import com.jaspersoft.jasperserver.api.engine.common.service.JRXMLFixer;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportExecutionStatusSearchCriteria;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlValuesInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlsInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.SchedulerReportExecutionStatusSearchCriteria;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.JSReportExecutionRequestCancelledException;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.ReportExecuter;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.CompositeReportExecutionListener;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportExecutionListener;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportExecutionListenerFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitRequest;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitRequestBase;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.TrialReportUnitRequest;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.DataCacheProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.InputControlsInfoExtractor;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDataSourceDefinition;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataAdapterDefinition;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DefaultProtectionDomainProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.InputControlLabelResolver;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.JRQueryExecuterAdapter;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.JarsClassLoader;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.MaterializedDataParameter;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.MessageSourceLoader;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.ProtectionDomainProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.ReportInputControlValuesInformationLoader;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryCacheMap;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryCacheMap.CacheObject;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryCacheMap.ObjectCache;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryResourceClassLoader;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryResourceKey;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryUtil;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.ResourceCollector;
import com.jaspersoft.jasperserver.api.engine.scheduling.quartz.ReportExecutionJob;
import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEvent;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType;
import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttribute;
import com.jaspersoft.jasperserver.api.logging.diagnostic.helper.DiagnosticAttributeBuilder;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.Diagnostic;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControlsContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValues;
import com.jaspersoft.jasperserver.api.metadata.common.domain.MemoryDataContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.*;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.AsyncThumbnailCreator;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.ExecutorServiceWrapper;
import com.jaspersoft.jasperserver.api.metadata.data.cache.DataCacheSnapshot;
import com.jaspersoft.jasperserver.api.metadata.data.cache.DataSnapshotPersistentMetadata;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaDataProvider;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomJdbcReportDataSourceProvider;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.DataView;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Tenant;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.dto.reports.ReportQueryDetails;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.extension.annotations.WithSpan;
import net.sf.ehcache.pool.SizeOfEngine;
import net.sf.ehcache.pool.impl.DefaultSizeOfEngine;
import net.sf.jasperreports.data.cache.DataSnapshotException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.export.ExporterInputItem;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.engine.fill.*;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRResourcesUtil;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlTemplateLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import net.sf.jasperreports.extensions.DefaultExtensionsRegistry;
import net.sf.jasperreports.pdf.SimplePdfReportConfiguration;
import net.sf.jasperreports.repo.JasperDesignCache;
import net.sf.jasperreports.repo.ReportCompiler;
import net.sf.jasperreports.web.servlets.AsyncJasperPrintAccessor;
import net.sf.jasperreports.web.servlets.JasperPrintAccessor;
import net.sf.jasperreports.web.servlets.SimpleJasperPrintAccessor;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarFile;

import static com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.ErrorTemplateReportService.ERROR_PARAM;
import static com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType.EXECUTION_QUERY_DETAILS;

/**
 *
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id$
 */
public class EngineServiceImpl implements EngineService, ReportExecuter,
		CompiledReportProvider, InternalReportCompiler, InitializingBean, Diagnostic
{
	protected static final Log log = LogFactory.getLog(EngineServiceImpl.class);
	protected static final Log valueQueryLog = LogFactory.getLog("valueQueryLog");

	public List<String> authorizedAllExecutionsRolesList;
	public List<String> authorizedOrgExecutionsRolesList;

	private DataSourceServiceFactory dataSourceServiceFactories;
	protected RepositoryUnsecure unsecureRepository;
	protected RepositoryService repository;
	protected TenantService tenantService;
	private SecurityContextProvider securityContextProvider;
	private ProtectionDomainProvider reportJarsProtectionDomainProvider = new DefaultProtectionDomainProvider();
	private String reportParameterLabelKeyPrefix;
	private IQueryManipulator queryManipulator = null;

	private boolean autoUpdateJRXMLs;
	private List<JRXMLFixer> jrxmlFixerList;
	
	private RepositoryCacheMap tempJarFiles;
	private RepositoryCache compiledReportsCache;
	private final ReferenceMap jarsClassLoaderCache;
	private final ReferenceMap resourcesClassLoaderCache;
	private RepositoryCacheableItem cacheableCompiledReports;
	private RepositoryContextManager repositoryContextManager;
    private List builtInParameterProviders = new ArrayList();

    private AuditContext auditContext;
    protected boolean recordSizeof=false;
    @javax.annotation.Resource
    protected GlobalDefaultValueProvider globalDefaultValueProvider;

    protected InputControlsInfoExtractor inputControlsInfoExtractor = new InputControlsInfoRoutingExtractor();
    //TODO consider injecting and using ReportLoadingService

    /*** THE FOLLOWING PARAMETER ARE USED FOR CANCEL EXECUTIONS **/
    private final ThreadLocal<ReportExecutionStatus> currentExecutionStatus = new ThreadLocal<ReportExecutionStatus>();

    @javax.annotation.Resource
    private Map<String, ReportExecutionStatus> engineExecutions;

    @javax.annotation.Resource
    private Map<String, ErrorTemplateReportService> errorReportDSFactory;

    @javax.annotation.Resource(name="monitoringExceptionMapping")
	private Map<String, List<String>> monitoringExceptionMapping;

    private Executor syncReportExecutorService =
			SynchronousExecutor.INSTANCE;//default value

	private Executor asyncReportExecutorService = Executors.newCachedThreadPool();
	
	private List<ReportExecutionListenerFactory> reportExecutionListenerFactories;

	private DataCacheProvider dataCacheProvider;
	
	private List<ReportDataParameterContributor> dataParameterContributors;
	
	private JasperReportsContext jasperReportsContext;

    private AsyncThumbnailCreator asyncThumbnailCreator;
    private boolean reportThumbnailServiceEnabled;

    private AtomicLong startSyncReportExecutionCount=new AtomicLong();
    private AtomicLong startAsyncReportExecutionCount=new AtomicLong();
    private AtomicLong endSyncReportExecutionCount=new AtomicLong();
    private AtomicLong endAsyncReportExecutionCount=new AtomicLong();
    private AtomicLong errorReportExecutionCount=new AtomicLong();
    
    private boolean reportUnitClassLoadingEnabled;

	@Value("${reportExecutions.otel.instrumentation.lib.name:io.opentelemetry.jaxrs-annotations-2.0}")
	private String otelInstrumentationLibName;
	@Value("${reportExecutions.otel.instrumentation.lib.version:null}")
	private String otelInstrumentationLibVersion;

	public EngineServiceImpl()
	{
		jarsClassLoaderCache = new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.SOFT);
		resourcesClassLoaderCache = new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.SOFT);
		cacheableCompiledReports = new CacheableCompiledReports(this);
	}

	public void setAuthorizedAllExecutionsRolesList(List<String> authorizedAllExecutionsRolesList) {
		this.authorizedAllExecutionsRolesList = authorizedAllExecutionsRolesList;
	}

	public void setAuthorizedOrgExecutionsRolesList(List<String> authorizedOrgExecutionsRolesList) {
		this.authorizedOrgExecutionsRolesList = authorizedOrgExecutionsRolesList;
	}

	public void afterPropertiesSet() throws Exception {
		if (getRepositoryContextManager() == null) {
			repositoryContextManager = new DefaultRepositoryContextManager(repository, this);
		}

		createJarFilesCache();
	}

	/**
	 * @return Returns the queryManipulator.
	 */
	public IQueryManipulator getQueryManipulator() {
		return queryManipulator;
	}


	/**
	 * @param queryManipulator The queryManipulator to set.
	 */
	public void setQueryManipulator(IQueryManipulator queryManipulator) {
		this.queryManipulator = queryManipulator;
	}

	/**
	 *
	 */
	public RepositoryService getRepositoryService()
	{
		return repository;
	}

	public void setRepositoryService(RepositoryService repository) {
		this.repository = repository;
	}

	public TenantService getTenantService() {
		return tenantService;
	}

	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	public RepositoryCache getCompiledReportsCache() {
		return compiledReportsCache;
	}

	public void setCompiledReportsCache(RepositoryCache compiledReportsCache) {
		this.compiledReportsCache = compiledReportsCache;
	}

	public RepositoryCacheableItem getCacheableCompiledReports() {
		return cacheableCompiledReports;
	}

	public void setCacheableCompiledReports(
			RepositoryCacheableItem cacheableCompiledReports) {
		this.cacheableCompiledReports = cacheableCompiledReports;
	}

	public RepositoryContextManager getRepositoryContextManager() {
		return repositoryContextManager;
	}

	public void setRepositoryContextManager(
			RepositoryContextManager repositoryContextManager) {
		this.repositoryContextManager = repositoryContextManager;
	}

    public AuditContext getAuditContext() {
        return auditContext;
    }

    public void setAuditContext(AuditContext auditContext) {
        this.auditContext = auditContext;
    }

    public List getBuiltInParameterProviders() {
        return builtInParameterProviders;
    }

    public void setBuiltInParameterProviders(List builtInParameterProviders) {
        this.builtInParameterProviders = builtInParameterProviders;
    }

    public void setInputControlsInfoExtractor(InputControlsInfoExtractor inputControlsInfoExtractor) {
        this.inputControlsInfoExtractor = inputControlsInfoExtractor;
    }
    public ProtectionDomainProvider getReportJarsProtectionDomainProvider() {
        return reportJarsProtectionDomainProvider;
    }

    public void setReportJarsProtectionDomainProvider(ProtectionDomainProvider protectionDomainProvider) {
        this.reportJarsProtectionDomainProvider = protectionDomainProvider;
    }

    /**
     * <code>Diagnostic</code> implementation - collecting info about currently executed reports.
     * @author Oleg Gavavka, vsabadosh
     */
    public Map<DiagnosticAttribute, DiagnosticCallback> getDiagnosticData() {
        return new DiagnosticAttributeBuilder()
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.RUNNING_REPORT_COUNT, new DiagnosticCallback<Long>() {
                    @Override
                    public Long getDiagnosticAttributeValue() {
                        return (startAsyncReportExecutionCount.get()+startSyncReportExecutionCount.get())-(endAsyncReportExecutionCount.get()+endSyncReportExecutionCount.get());
                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.RUNNING_REPORTS_LIST, new DiagnosticCallback<List<String>>() {
                    @Override
                    public List<String> getDiagnosticAttributeValue() {
                        List<String> reportUrisWithTimeExecutions = new ArrayList<String>();
                        for (ReportExecutionStatus status : engineExecutions.values()) {
                            Long executionTime = System.currentTimeMillis() - status.getStartTime().getTime();
                            reportUrisWithTimeExecutions.add(status.getReportURI()+"="+executionTime / 1000);
                        }
                        return reportUrisWithTimeExecutions;
                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.RUNNING_REPORT_ASYNCTASKCOUNT, new DiagnosticCallback<Long>() {
                    @Override
                    public Long getDiagnosticAttributeValue() {
                        if (asyncReportExecutorService instanceof ThreadPoolExecutor || hasWrappedThreadPoolExecutor(asyncReportExecutorService)) {
                            return startAsyncReportExecutionCount.get();
                        }
                        return 0L;
                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.RUNNING_REPORT_ASYNCPOOLSIZE, new DiagnosticCallback<Integer>() {
                    @Override
                    public Integer getDiagnosticAttributeValue() {
                        if (asyncReportExecutorService instanceof ThreadPoolExecutor) {
                            return ((ThreadPoolExecutor) (asyncReportExecutorService)).getPoolSize();
                        } else if (hasWrappedThreadPoolExecutor(asyncReportExecutorService)) {
                            ExecutorService wrappedExecutorService = ((ExecutorServiceWrapper) asyncReportExecutorService).getWrappedExecutorService();
                            return ((ThreadPoolExecutor) wrappedExecutorService).getPoolSize();
                        }
                        return 0;
                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.RUNNING_REPORT_ASYNCACTIVETASKCOUNT, new DiagnosticCallback<Long>() {
                    @Override
                    public Long getDiagnosticAttributeValue() {
                        return startAsyncReportExecutionCount.get()-endAsyncReportExecutionCount.get();

                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.RUNNING_REPORT_SYNCTASKCOUNT, new DiagnosticCallback<Long>() {
                    @Override
                    public Long getDiagnosticAttributeValue() {

                        return startSyncReportExecutionCount.get();
                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.RUNNING_REPORT_ERRORCOUNT, new DiagnosticCallback<Long>() {
                    @Override
                    public Long getDiagnosticAttributeValue() {

                        return errorReportExecutionCount.get();
                    }
                })
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.RUNNING_REPORT_COUNT_CUML, new DiagnosticCallback<Long>() {
                    @Override
                    public Long getDiagnosticAttributeValue() {

                        return startAsyncReportExecutionCount.get()+startSyncReportExecutionCount.get();
                    }
                }).build();
    }

    private boolean hasWrappedThreadPoolExecutor(Executor executor) {
        if (executor instanceof ExecutorServiceWrapper) {
            ExecutorService wrappedExecutorService = ((ExecutorServiceWrapper) executor).getWrappedExecutorService();
            return wrappedExecutorService instanceof ThreadPoolExecutor;
        }
        return false;
    }



    protected final class TempJarFileCacheObject implements ObjectCache {
		public boolean isValid(Object o) {
			return true;
		}

		public Object create(ExecutionContext context, FileResource res) {
			try {
				File tempFile = File.createTempFile("report_jar", ".jar");
				tempFile.deleteOnExit();

				if (log.isInfoEnabled()) {
					log.info("Created temp jar file \"" + tempFile.getPath() + "\" for resource \"" + res.getURIString() + "\"");
				}

				byte[] data = getFileResourceData(context, res);
				OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(tempFile));
				try {
					fileOut.write(data);
					fileOut.flush();
				} finally {
					fileOut.close();
				}

				JarFile jarFile = new JarFile(tempFile);

				return jarFile;
			} catch (IOException e) {
				log.error(e, e);
				throw new JSExceptionWrapper(e);
			}
		}

		public void release(Object o) {
			dispose((JarFile) o);
		}
	}

	protected void createJarFilesCache() {
		this.tempJarFiles = new RepositoryCacheMap(this.repository, 
				this.repositoryContextManager,
				new TempJarFileCacheObject());
	}

	protected InputStream getFileResourceDataStream(ExecutionContext context, FileResource fileResource) {
		InputStream data;
		if (fileResource.hasData()) {
			data = fileResource.getDataStream();
		} else {
			FileResourceData resourceData = repository.getResourceData(context, fileResource.getURIString());
			data = resourceData.getDataStream();
		}
		return data;
	}

	protected CacheObject getCacheJarFile(ExecutionContext context, FileResource jar, boolean cache) {
		return tempJarFiles.cache(context, jar, cache);
	}

	protected byte[] getFileResourceData(ExecutionContext context, FileResource fileResource) {
		byte[] data;
		if (fileResource.hasData()) {
			data = fileResource.getData();
		} else {
			FileResourceData resourceData = repository.getResourceData(context, fileResource.getURIString());
			data = resourceData.getData();
		}
		return data;
	}

	public DataSourceServiceFactory getDataSourceServiceFactories() {
		return dataSourceServiceFactories;
	}

	public void setDataSourceServiceFactories(DataSourceServiceFactory dataSourceServiceFactories) {
		this.dataSourceServiceFactories = dataSourceServiceFactories;
	}

	/**
	 * create a cancelable process
	 */
    public Result execute(ExecutionContext context, Request request)
    {
        ReportUnitRequestBase reportUnitRequest = (ReportUnitRequestBase) request;
        return reportUnitRequest.execute(context, this);
    }



	protected void startExecution(Request request) {
		if (log.isDebugEnabled()) {
			log.debug("Launching execution " + request.getId());
		}

		ReportExecutionStatus status;
        if (request instanceof ReportUnitRequest) {
            status = new ReportExecutionStatus(request, ((ReportUnitRequest)request).getPropertyMap());
            String reportUnitUri = ((ReportUnitRequest) request).getReportUnitUri();
            status.setReportURI(reportUnitUri);
			Map propertyMap = ((ReportUnitRequest) request).getPropertyMap();
			Object username;
			if (null != propertyMap && (username = propertyMap.get(ReportExecutionStatus.PROPERTY_USERNAME)) != null) {
				ThreadContext.put(FilterBy.USER_ID.name(), username.toString());
			}
            ThreadContext.put(FilterBy.RESOURCE_URI.name(), reportUnitUri);
            status.setOwner(securityContextProvider.getContextUser());
        } else {
            status = new ReportExecutionStatus(request);
        }
		log.debug("ENGINE SERVICE: start report execution: " + request.getId());
		engineExecutions.put(request.getId(), status);
        addPropertyToAuditEvent("executionID", request.getId());
		addPropertyToAuditEvent("executionIDS", request.getId(), true, String.class);
		currentExecutionStatus.set(status);
	}

	protected void endExecution(Request request) {
        engineExecutions.remove(request.getId());
		log.debug("ENGINE SERVICE: end report execution: " + request.getId());
		currentExecutionStatus.set(null);

		if (log.isDebugEnabled()) {
			log.debug("Ended execution " + request.getId());
		}
        ThreadContext.getContext().remove(FilterBy.RESOURCE_URI.name());
		ThreadContext.getContext().remove(FilterBy.USER_ID.name());
	}

	protected ReportExecutionStatus currentExecutionStatus() {
		return currentExecutionStatus.get();
	}

	protected String currentExecutionId() {
		ReportExecutionStatus status = currentExecutionStatus();
		return status == null ? "Not tracked" : status.getRequestId();
	}

	protected JasperReportsContext getEffectiveJasperReportsContext() {
		JasperReportsContext jrContext = getJasperReportsContext();
		if (jrContext == null) {
			jrContext = DefaultJasperReportsContext.getInstance();
		}
		return jrContext;
	}

    @Override
    public void exportToPdf(ExecutionContext context,
                            String reportUnitURI,
                            JasperPrint jasperPrint,
                            OutputStream outputStream,
                            Integer startPageIndex,
                            Integer endPageIndex,
                            Boolean overrideReportHints,
                            List<ExporterInputItem> inputItems,
                            Integer pageIndex
                            ) {
        ReportUnit reportUnit = getRepositoryResource(context, reportUnitURI, ReportUnit.class);
        RepositoryContextHandle repositoryContextHandle = setThreadRepositoryContext(context, null, reportUnitURI);

        try {
            OrigContextClassLoader origContext = setContextClassLoader(context, reportUnit, false, repositoryContextHandle);
            try {
                JRPdfExporter exporter = createPdfExporter(context);

                if (inputItems != null && !inputItems.isEmpty()) {
                    exporter.setExporterInput(new SimpleExporterInput(inputItems));
                } else {
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                }

                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

                SimplePdfReportConfiguration configuration = new SimplePdfReportConfiguration();
                if (overrideReportHints != null) {
                    configuration.setOverrideHints(overrideReportHints);
                }
                if(startPageIndex != null){
                    configuration.setStartPageIndex(startPageIndex);
                }
                if(endPageIndex != null){
                    configuration.setEndPageIndex(endPageIndex);
                }
                if(pageIndex != null){
                    configuration.setPageIndex(pageIndex);
                }

                exporter.setConfiguration(configuration);

                exporter.exportReport();

            } finally {
                revert(origContext);
            }
        } catch (JRException e) {
            log.error("Error while exporting report to PDF", e);
            throw new JSExceptionWrapper(e);
        } finally {
            resetThreadRepositoryContext(repositoryContextHandle);
        }
    }

	protected JRPdfExporter createPdfExporter(ExecutionContext context) {
		return new JRPdfExporter(getEffectiveJasperReportsContext());
	}

	public RepositoryContextHandle setThreadRepositoryContext(ExecutionContext context, ResourceContainer reportUnit, String reportUnitURI)
	{
		// resources loading in the repository context should only need execute permissions,
		// so make sure that the execution context has the magic attribute to signal that perms change
		context = getRuntimeExecutionContext(context);
        if (getRepositoryContextManager() == null) return null;
        if (reportUnitURI == null) return null;
        return getRepositoryContextManager().setRepositoryContext(context, reportUnitURI, reportUnit);
	}

	public void resetThreadRepositoryContext(RepositoryContextHandle repositoryContextHandle)
	{
		if (repositoryContextHandle != null)
		{
			repositoryContextHandle.unset();
		}
	}

	protected ReportUnitResult fillReport(ExecutionContext context, ReportUnitRequestBase request, ReportUnit reportUnit, boolean inMemoryUnit) {
		Span.current()
				.setAttribute("executionId", request.getId())
				.setAttribute("resourceUri", reportUnit.getURIString());
		ReportExecutionListener executionListener = createReportExecutionListener(request);
		
		boolean asynchronous = request.isAsynchronous();
		ReportFiller filler = createReportFiller(asynchronous);
		filler.setJasperReportsContext(request.getJasperReportsContext());
		ReportUnitResult result = new ReportUnitResult(reportUnit.getURIString(), request);
		filler.setReportResult(result);
		
		Executor executor = getReportExecutor(asynchronous);
		if (log.isDebugEnabled()) {
			log.debug("Running report " + reportUnit.getURIString() + " on " + executor);
		}

		executionListener.init();
		ReportFill reportFill = new ReportFill(context, request, reportUnit, inMemoryUnit, filler, executionListener);
		executor.execute(reportFill);

		// blocking for the result
		JasperPrintAccessor reportAccessor;
		try {
			reportAccessor = filler.getResult();
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new JSException(e);
		}

		if (log.isDebugEnabled()) {
			log.debug("Returning fill result " + reportAccessor);
		}

        if (reportThumbnailServiceEnabled && (reportAccessor.getJasperPrint().getPages().size() > 0)) {
            asyncThumbnailCreator.createAndStoreThumbnail(reportAccessor, jasperReportsContext, securityContextProvider.getContextUser(), reportUnit);
        }

		result.setJasperPrintAccessor(reportAccessor);
		result.setDataTimestamp(filler.getDataTimestamp());
		result.setPaginated(filler.isPaginated());
		return result;
	}

	protected ReportExecutionListener createReportExecutionListener(ReportUnitRequestBase request) {
		List<ReportExecutionListener> listeners = new LinkedList<ReportExecutionListener>();
		for (ReportExecutionListenerFactory listenerFactory : getReportExecutionListenerFactories()) {
			ReportExecutionListener listener = listenerFactory.createListener(request);
			if (listener != null) {
				listeners.add(listener);
			}
		}
		return CompositeReportExecutionListener.asListener(listeners);
	}
	
	protected ReportFiller createReportFiller(boolean asynchronous) {
		ReportFiller filler;
		if (asynchronous) {
			filler = new AsynchronousReportFiller();
		} else {
			filler = new SynchronousReportFiller();
		}
		return filler;
	}

	protected Executor getReportExecutor(boolean asynchronous) {
		Executor executor;
		if (asynchronous) {
			executor = asyncReportExecutorService;
		} else {
			executor = syncReportExecutorService;
		}
		return executor;
	}

	protected abstract class ReportFiller {

		private final Lock resultLock;
		private final Condition resultSync;
		private JasperReportsContext jasperReportsContext;
		private JasperPrintAccessor result;
		private Throwable error;
		private Date dataTimestamp;
		protected boolean asyncable;
		private boolean paginated = true;
		private ReportUnitResult reportResult;

		protected ReportFiller() {
			resultLock = new ReentrantLock();
			resultSync = resultLock.newCondition();
		}

		protected boolean hasResult() {
			resultLock.lock();
			try {
				return result != null || error != null;
			} finally {
				resultLock.unlock();
			}
		}
		
		protected void setResult(JasperPrintAccessor result, boolean paginated, boolean skipWhenSet) {
			resultLock.lock();
			try {
				if (this.result == null || !skipWhenSet) {
					if (log.isDebugEnabled()) {
						log.debug("Report fill result available " + result);
					}

					this.result = result;
					this.paginated = paginated;
					resultSync.signalAll();
				}
			} finally {
				resultLock.unlock();
			}
		}

		public void setError(Throwable error) {
			resultLock.lock();
			try {
				if (log.isDebugEnabled()) {
					log.debug("Report fill error", error);
				}

				this.error = error;
				resultSync.signalAll();
			} finally {
				resultLock.unlock();
			}
		}
		
		public boolean hasError() {
			return error != null;
		}

		public JasperPrintAccessor getResult() throws Throwable {
			resultLock.lock();
			try {
				while (result == null && error == null) {
					resultSync.await();
				}

				if (error != null) {
					throw error;
				}

				return result;
			} catch (InterruptedException e) {
				throw new JSException(e);
			} finally {
				resultLock.unlock();
			}
		}

		protected JasperReportsContext getFillJasperReportsContext() {
			JasperReportsContext fillContext = jasperReportsContext;
			if (fillContext == null) {
				fillContext = getEffectiveJasperReportsContext();
			}
			return fillContext;
		}
		
		public abstract void fillReport(JasperReport report, Map<String, Object> parameters, JRDataSource dataSource)
				throws JRException;

		public Date getDataTimestamp() {
			return dataTimestamp;
		}

		public void setDataTimestamp(Date dataTimestamp) {
			this.dataTimestamp = dataTimestamp;
		}

		public void setAsyncable(boolean asyncable) {
			this.asyncable = asyncable;
		}

		public JasperReportsContext getJasperReportsContext() {
			return jasperReportsContext;
		}

		public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
			this.jasperReportsContext = jasperReportsContext;
		}

		public boolean isPaginated() {
			return paginated;
		}

		public void setReportResult(ReportUnitResult reportResult) {
			this.reportResult = reportResult;
		}
		
		protected void setUsedPageWidth(int usedPageWidth) {
			if (reportResult != null) {
				reportResult.setUsedPageWidth(usedPageWidth);
			}
		}
	}

	protected class SynchronousReportFiller extends ReportFiller {

		public SynchronousReportFiller() {
			//NOP
		}

		public void fillReport(JasperReport report,
				Map<String, Object> parameters, JRDataSource dataSource) throws JRException {
			JasperReportsContext jasperReportsContext = getFillJasperReportsContext();
			FillResultListener fillResult = EngineServiceImpl.this.fillReport(jasperReportsContext, 
					report, parameters, dataSource);

			// set the result
			SimpleJasperPrintAccessor result = new SimpleJasperPrintAccessor(fillResult.getJasperPrint());
			setResult(result, fillResult.isPaginated(), false);
			setUsedPageWidth(fillResult.getUsedPageWidth());
		}
	}

	protected class AsynchronousReportFiller extends ReportFiller {

		public void fillReport(JasperReport report,
				Map<String, Object> parameters, JRDataSource dataSource) throws JRException {
			// creating a synchronous fill handler since the async fork already happened
			// we need a fill handler for AsyncJasperPrintAccessor
			checkRequestCanceled();

			if (log.isDebugEnabled()) {
				log.debug("Filling report for request " + currentExecutionId());
			}

			JasperReportsContext jasperReportsContext = getFillJasperReportsContext();
			final SynchronousFillHandle fillHandle = new SynchronousFillHandle(jasperReportsContext,
					report, parameters, dataSource);
			final AsyncJasperPrintAccessor result = new AsyncJasperPrintAccessor(fillHandle);
			
			if (asyncable) {
				// setting the result once we have the first page so that fillReport can return.
				// we are waiting for the first page in order to catch common exceptions that occur
				// while initializing the report and data source.
				fillHandle.addFillListener(new FillListener() {
					public void pageGenerated(JasperPrint jasperPrint, int pageIndex) {
						if (pageIndex == 0) {
							setResult(result, fillHandle.isPaginated(), false);
						}
					}
					
					public void pageUpdated(JasperPrint jasperPrint, int pageIndex) {
						// NOP
					}
				});
			}
			
			// adding this listener so that error results in the exception being thrown
			FillResultListener fillResult = new FillResultListener();
			fillHandle.addListener(fillResult);

			// set the cancelable
			FillHandleCancelable cancelable = new FillHandleCancelable(fillHandle);
			setExecutionCancelable(cancelable);
			try {
				// the fill will run on the same thread
				fillHandle.startFill();
			} finally {
				clearExecutionCancelable();
			}

			// set the result if not already set
			setResult(result, fillHandle.isPaginated(), true);
			setUsedPageWidth(fillHandle.getUsedPageWidth());
			
			// we don't need to do anything more here, AsyncJasperPrintAccessor gets the completed events
			if (log.isDebugEnabled()) {
				log.debug("Ended fill for request " + currentExecutionId());
			}
		}
	}

	protected static class SynchronousFillHandle extends BaseFillHandle {

		protected SynchronousFillHandle(JasperReportsContext jrContext, JasperReport jasperReport,
				Map<String, Object> parameters, JRDataSource dataSource) throws JRException {
			super(jrContext, jasperReport, parameters, dataSource, null);
		}

		@Override
		protected Executor getReportExecutor() {
			return SynchronousExecutor.INSTANCE;
		}
		
		protected boolean isPaginated() {
			JRFillContext fillContext = filler.getFillContext();
			return fillContext == null //conservatively considered paginated
					|| !fillContext.isIgnorePagination();
		}
		
		protected int getUsedPageWidth() {
			return ((BaseReportFiller) filler).getUsedPageWidth();
		}
	}

	protected static class SynchronousExecutor implements Executor {

		public static final SynchronousExecutor INSTANCE = new SynchronousExecutor();

		protected SynchronousExecutor() {
			//NOP
		}

		public void execute(Runnable command) {
			// run in the same thread
			command.run();
		}
	}

	protected abstract class ReportRunnable implements Runnable {
		protected final ReportUnitRequestBase request;
		protected final ReportFiller filler;
		private final ReportExecutionListener executionListener;
		private Tracer tracer;
		public ReportRunnable(ReportUnitRequestBase request, ReportFiller filler, 
				ReportExecutionListener executionListener) {
			this.request = request;
			this.filler = filler;
			this.executionListener = executionListener;
			tracer = GlobalOpenTelemetry.getTracer(otelInstrumentationLibName, otelInstrumentationLibVersion);
		}

		public final void run() {
			executionListener.start();
			Span span = tracer.spanBuilder("execution").startSpan();
			span.setAttribute("executionId", request.getId());
			try(Scope scope = span.makeCurrent())  {
                incrementStartReportExecutionCount(request.isAsynchronous());
				startExecution(request);
				try {
					runReport();
				} finally {
					endExecution(request);
                    incrementEndReportExecutionCount(request.isAsynchronous());
				}
				span.setStatus(StatusCode.OK);
			} catch (Throwable t) {
				span.setStatus(StatusCode.ERROR, t.getMessage());
				filler.setError(t);
                incrementErrorReportExecutionCount();
				addExceptionToAudit(t);
			} finally {
				span.end();
				long size = -1;
				if (recordSizeof) {
					try {
						size = getSizer().sizeOf(filler.getResult(), null, null).getCalculated();
					} catch (Throwable e) {}
					if (log.isDebugEnabled()) {
						log.debug("report result in memory size is "+size);
					}
				}
				executionListener.end(!filler.hasError(), size);
			}
		}

		SizeOfEngine getSizer() {
		    return new DefaultSizeOfEngine(1000, false);
		}

		protected abstract void runReport() throws Exception;
	}
	
	protected class ReportFill extends ReportRunnable {
		private final ExecutionContext context;
		private final ReportUnit reportUnit;
		private final ReportDataSource datasource;
		private final Query query;
		private final boolean inMemoryUnit;

		public ReportFill(ExecutionContext context, 
				ReportUnitRequestBase request, ReportUnit reportUnit,
				boolean inMemoryUnit, ReportFiller filler,
				ReportExecutionListener executionListener) {
			super(request, filler, executionListener);
			this.context = context;
			this.reportUnit = reportUnit;
			this.inMemoryUnit = inMemoryUnit;

			ExecutionContext runtimeContext = getRuntimeExecutionContext(context);
			ReportDataSource datasource = null;
			ResourceReference queryRef = reportUnit.getQuery();
			Query query = queryRef == null ? null : getFinalResource(runtimeContext, queryRef, Query.class);
			if (query != null && query.getDataSource() != null) {
				datasource = (ReportDataSource) getFinalResource(runtimeContext, query.getDataSource(), Resource.class);
			}

			ResourceReference dsRef = reportUnit.getDataSource();
			if (datasource == null && dsRef != null) {
				datasource = (ReportDataSource) getFinalResource(runtimeContext, dsRef, Resource.class);
			}
			this.datasource = datasource;
			this.query = query;
		}

		protected void runReport() {
			if (log.isDebugEnabled()) {
				log.debug("Initiating report execution for " + reportUnit.getURIString());
			}
			
			if (request.getReportContext() != null) {
				request.getReportContext().setParameterValue(
						ReportUnitRequestBase.REPORT_CONTEXT_PARAMETER_REPORT_UNIT, reportUnit);
			}
			
			// if we got here that means we have read perms for the report,
			// but access to other resources should only require execute perms
			ExecutionContext runtimeContext = getRuntimeExecutionContext(context);
			RepositoryContextHandle repositoryContextHandle = setThreadRepositoryContext(runtimeContext, reportUnit, reportUnit.getURIString());

            TimeZoneContextHolder.setTimeZone(context == null ? TimeZone.getDefault() : context.getTimeZone());
			try {
				// if we got here that means we have read perms for the report,
				// but access to other resources should only require execute perms
				Map unitResources = loadFinalResources(runtimeContext, reportUnit.getResources());
				OrigContextClassLoader origContext = setContextClassLoader(runtimeContext, unitResources, inMemoryUnit,
						repositoryContextHandle);
				try {
			        JasperDesignCache cache = JasperDesignCache.getInstance(DefaultJasperReportsContext.getInstance(), request.getReportContext());
			        setReportCompiler(cache, reportUnit, inMemoryUnit, repositoryContextHandle, origContext, unitResources);
					JasperReport report = getJasperReport(runtimeContext, cache, reportUnit, inMemoryUnit);
					
					// check if the report inhibits async viewing
					boolean reportAsyncable = JRPropertiesUtil.getInstance(
							DefaultJasperReportsContext.getInstance()).getBooleanProperty(
							report, AsynchronousFillHandle.PROPERTY_REPORT_ASYNC, true);
					filler.setAsyncable(reportAsyncable);
					
					@SuppressWarnings("unchecked")
					Map<String, Object> reportParameters = getReportParameters(runtimeContext, report, 
							request.getReportParameters());
					setReportTemplates(runtimeContext, unitResources, reportParameters);
					
					// parameter values used to match data snapshots
					Map<String, Object> dataParameters = new LinkedHashMap<String, Object>();
					addDataParameters(runtimeContext, request, reportUnit, report,
							reportParameters, dataParameters);
					
					boolean hasCachedData = false;
					if (dataCacheProvider != null) {
						DataCacheSnapshot snapshot = dataCacheProvider.setReportExecutionCache(
								runtimeContext, request, reportUnit, dataParameters);
						if (snapshot != null) {
							hasCachedData = true;
							filler.setDataTimestamp(snapshot.getMetadata().getSnapshotDate());
						}
					}

					// if we have cached data, run without data source and query
					if (hasCachedData) {
						runWithCachedData(runtimeContext, report, dataParameters, reportParameters);
					}
					
					// if we don't have a result yet (no cache or invalid cache), run with data source and query
					if (!filler.hasResult()) {
						runWithDataSource(runtimeContext, report, reportParameters);
					}
					/*if(request.getDashboardExecutionID() != null) {
						addPropertyToAuditEvent("dashboardExecutionID", request.getDashboardExecutionID());
					}*/
					addQueryDetailsToAudit(reportParameters, request);
				} finally {
					revert(origContext);
				}
			} finally {
                TimeZoneContextHolder.setTimeZone(null);
				resetThreadRepositoryContext(repositoryContextHandle);
			}
		}

		protected void runWithCachedData(ExecutionContext runtimeContext, JasperReport report, 
				Map<String, Object> dataParameters, Map<String, Object> reportParameters) {
			if (log.isDebugEnabled()) {
				log.debug("running report with cached data");
			}
			
			try {
				// run the report (note that it would run on the same thread even when async)
				Map<String, Object> paramsCopy = new HashMap<String, Object>(reportParameters);
				fillReport(runtimeContext, reportUnit, report, paramsCopy, datasource, null, filler, true);
				// success
			} catch (RuntimeException e) {
				// if a result has already been submitted, it's too late to do something
				if (filler.hasResult()) {
					throw e;
				}
				
				// if we have a data snapshot exception, rerun the report with the original data source.
				// otherwise rethrow the exception
				int snapshotExcIdx = ExceptionUtils.indexOfType(e, DataSnapshotException.class);
				if (snapshotExcIdx < 0) {
					throw e;
				}
				
				if (log.isDebugEnabled()) {
					log.debug("running report " + reportUnit.getURIString() 
							+ " with the data snapshot resulted in exception", e);
				}
				
				// remove the populated data snapshot and continue
				dataCacheProvider.handleInvalidSnapshot(runtimeContext, request, reportUnit, dataParameters);
			}
		}

		protected void runWithDataSource(ExecutionContext runtimeContext, JasperReport report, 
				Map<String, Object> reportParameters) {
			// setting the default data timestamp
			filler.setDataTimestamp(new Date());
			
			if (request.getReportContext() != null) {
				request.getReportContext().setParameterValue(
						ReportUnitRequestBase.REPORT_CONTEXT_PARAMETER_REPORT_UNIT_DATA_SOURCE, datasource);
			}
			
			if (log.isDebugEnabled()) {
				log.debug("running report with data source");
			}

			fillReport(runtimeContext, reportUnit, report, reportParameters, datasource, query, filler, false);
		}
	}

	protected static interface ReportExecutionCancelable {
		boolean cancel(String requestId);
	}

	protected static class QueryExecuterCancelable implements ReportExecutionCancelable {
		private final JRQueryExecuter queryExecuter;

		public QueryExecuterCancelable(JRQueryExecuter queryExecuter) {
			this.queryExecuter = queryExecuter;
		}

		public boolean cancel(String requestId) {
			if (log.isDebugEnabled()) {
				log.debug("Canceling query executer for request " + requestId);
			}

			try {
				return queryExecuter.cancelQuery();
			}
			catch (JRException e) {
				if (log.isWarnEnabled()) {
					log.warn("Error canceling query executer", e);
				}

				return false;
			}
		}
	}

	protected static class FillHandleCancelable implements ReportExecutionCancelable {
		private final FillHandle fillHandle;

		public FillHandleCancelable(FillHandle fillHandle) {
			this.fillHandle = fillHandle;
		}

		public boolean cancel(String requestId) {
			if (log.isDebugEnabled()) {
				log.debug("Canceling filler for request " + requestId);
			}

			try {
				fillHandle.cancellFill();
			}
			catch (JRException e) {
				if (log.isWarnEnabled()) {
					log.warn("Error canceling report filler", e);
				}

				return false;
			}
			return true;
		}
	}

    @XmlRootElement(name = "reportExecution")
    public static class ReportExecutionStatus implements ReportExecutionStatusInformation {

        public static String PROPERTY_REPORTURI = "REPORTEXECUTIONSTATUS_REPORTURI";
        public static String PROPERTY_JOBID = "REPORTEXECUTIONSTATUS_JOBID";
        public static String PROPERTY_JOBLABEL = "REPORTEXECUTIONSTATUS_JOBLABEL";
        public static String PROPERTY_FIRETIME = "REPORTEXECUTIONSTATUS_FIRETIME";
        public static String PROPERTY_USERNAME = "REPORTEXECUTIONSTATUS_USERNAME";
        public static String PROPERTY_QUARTZJOB = "REPORTEXECUTIONSTATUS_QUARTZJOB";

        private String requestId;
        private String reportURI;
        private User owner;
        private boolean cancelRequested;
        private Date startTime;
        private ReportExecutionCancelable cancelable;
        private Map<String, Object> propertyMap = null;

        public ReportExecutionStatus(){/* empty constructor is required for JAXB */}

        public ReportExecutionStatus(Request request) {
            this.requestId = request.getId();
            this.startTime = new Date();
        }

        public ReportExecutionStatus(Request request, Map<String, Object> propertyMap) {
            this.requestId = request.getId();
            this.propertyMap = propertyMap;
            this.startTime = new Date();
            if (propertyMap == null) propertyMap = new HashMap<String, Object>();
        }

        @XmlTransient
        public User getOwner() {
            return owner;
        }

        public void setOwner(User owner) {
            this.owner = owner;
        }

        @XmlElement
        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public synchronized void setCancelable(ReportExecutionCancelable cancelable) {
            this.cancelable = cancelable;
        }
        @XmlTransient
        public synchronized ReportExecutionCancelable getCancelable() {
            return cancelable;
        }

        public synchronized boolean cancel() {
            this.cancelRequested = true;
            if(propertyMap != null) {
                Object quartzJob = propertyMap.get(PROPERTY_QUARTZJOB);
                if ((quartzJob != null) && (quartzJob instanceof ReportExecutionJob)) {
                    ((ReportExecutionJob)quartzJob).cancelExecution();
                }
            }
            synchronized (this) {
                if (cancelable != null) {
                    return cancelable.cancel(requestId);
                }
                if (log.isDebugEnabled()) {
                    log.debug("No cancelable found for request " + requestId);
                }
                return true;
            }
        }
        @XmlTransient
        public Map<String, Object> getProperties() {
            return propertyMap;
        }

        @XmlTransient
        public synchronized boolean isCancelRequested() {
            return this.cancelRequested;
        }

        public String getReportURI() {
            return reportURI;
        }

        public void setReportURI(String reportURI) {
            this.reportURI = reportURI;
        }
        @XmlTransient
        public Date getStartTime() {
            return this.startTime;
        }
    }

	protected class FillResultListener implements AsynchronousFilllListener {

		private JasperPrint jasperPrint;
		private boolean paginated;
		private int usedPageWidth;

		public void reportFinished(JasperPrint jasperPrint) {
			this.jasperPrint = jasperPrint;
		}

		public void reportCancelled() {
			throw new JSReportExecutionRequestCancelledException("Report has been cancelled");
		}

		public void reportFillError(Throwable t) {
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}

			throw new JSException("Error filling report", t);
		}

		public JasperPrint getJasperPrint() {
			return jasperPrint;
		}

		public boolean isPaginated() {
			return paginated;
		}

		public void setPaginated(boolean paginated) {
			this.paginated = paginated;
		}

		public int getUsedPageWidth() {
			return usedPageWidth;
		}

		public void setUsedPageWidth(int usedPageWidth) {
			this.usedPageWidth = usedPageWidth;
		}
	}

	protected void checkRequestCanceled() {
		ReportExecutionStatus status = currentExecutionStatus();
		if (status != null && status.isCancelRequested()) {
			throw new JSReportExecutionRequestCancelledException();
		}
	}

	protected void setExecutionCancelable(ReportExecutionCancelable cancelable) {
		ReportExecutionStatus status = currentExecutionStatus();
		if (status == null) {
			if (log.isDebugEnabled()) {
				log.debug("Execution not tracked");
			}
		} else {
			status.setCancelable(cancelable);
		}
	}

	protected void clearExecutionCancelable() {
		setExecutionCancelable(null);
	}

	public boolean cancelExecution(String requestId) {
		ReportExecutionStatus status = engineExecutions.get(requestId);
		if (status == null) {
			if (log.isDebugEnabled()) {
				log.debug("No execution found for request " + requestId);
			}

			return false;
		}
		synchronized (status) {
			return status.cancel();
        }
	}

	protected void addDataParameters(ExecutionContext runtimeContext,
			ReportUnitRequestBase request, ReportUnit reportUnit, JasperReport report, 
			final Map<String, Object> reportParameters, final Map<String, Object> dataParameters) {
		ReportDataParameters parameters = new ReportDataParameters() {
			@Override
			public void addReportParameter(String name, Object value) {
				if (!reportParameters.containsKey(name)) {
					// also put additional data parameters into the report parameters map for data source services
					reportParameters.put(name, value);
				}
				dataParameters.put(name, value);
			}
			
			@Override
			public void addDataParameter(String name, Object value) {
				dataParameters.put(name, value);
			}
		};
		
		for (ReportDataParameterContributor contributor : getDataParameterContributors()) {
			contributor.addDataParameters(runtimeContext, request, reportUnit, report, parameters);
		}
	}

	protected Map getReportParameters(ExecutionContext context, JasperReport report, 
			Map requestParameters) {
		Map reportParameters = new HashMap();

		if (context != null && context.getLocale() != null
				&& reportParameters.get(JRParameter.REPORT_LOCALE) == null) {
			reportParameters.put(JRParameter.REPORT_LOCALE, context.getLocale());
		}

		if (context != null && context.getTimeZone() != null) {
			reportParameters.put(JRParameter.REPORT_TIME_ZONE, context.getTimeZone());
		}

		if (requestParameters != null) {
            reportParameters.putAll(requestParameters);
		}

        setBuiltinParameters(context, true, report.getParameters(), reportParameters, null);

		return reportParameters;
	}

	protected void setReportTemplates(ExecutionContext context, Map unitResources, Map reportParameters) {
		if (!reportParameters.containsKey(JRParameter.REPORT_TEMPLATES)) {
			List templates = new ArrayList();
			for (Iterator it = unitResources.values().iterator(); it.hasNext();) {
				FileResource resource = (FileResource) it.next();
				if (resource.getFileType().equals(FileResource.TYPE_STYLE_TEMPLATE)) {
					JRTemplate template = loadTemplate(context, resource);
					templates.add(template);
				}
			}
			reportParameters.put(JRParameter.REPORT_TEMPLATES, templates);
		}
	}

	protected JRTemplate loadTemplate(ExecutionContext context, FileResource resource) {
		InputStream templateDataStream = getFileResourceDataStream(context, resource);
		try {
			return JRXmlTemplateLoader.load(templateDataStream);
		} catch (JRRuntimeException e) {
			throw new JSExceptionWrapper(e);
		}
	}

	protected void revert(OrigContextClassLoader origContext) {
		if (origContext.set) {

			Thread.currentThread().setContextClassLoader(origContext.origClassLoader);

			for (Iterator it = origContext.jars.iterator(); it.hasNext();) {
				CacheObject cacheJarFile = (CacheObject) it.next();
				if (!cacheJarFile.isCached()) {
					JarFile jarFile = (JarFile) cacheJarFile.getObject();
					dispose(jarFile);
				}
			}
		}
	}

	protected static class OrigContextClassLoader {
		public final boolean set;
		public final ClassLoader origClassLoader;
		public final ClassLoader newClassLoader;
		public final List jars;

		public static final OrigContextClassLoader NOT_SET = new OrigContextClassLoader(false);

		private OrigContextClassLoader(boolean set) {
			this.set = set;
			this.origClassLoader = null;
			this.newClassLoader = null;
			this.jars = null;
		}

		public OrigContextClassLoader(ClassLoader origClassLoader, ClassLoader newClassLoader, 
				List jars) {
			this.set = true;
			this.origClassLoader = origClassLoader;
			this.newClassLoader = newClassLoader;
			this.jars = jars;
		}
	}

	protected OrigContextClassLoader setContextClassLoader(ExecutionContext context, ResourceContainer reportUnit, boolean inMemoryUnit) {
		return setContextClassLoader(context, reportUnit, inMemoryUnit, null);
	}

	protected OrigContextClassLoader setContextClassLoader(ExecutionContext context, ResourceContainer reportUnit, boolean inMemoryUnit,
			RepositoryContextHandle repositoryContextHandle) {
		Map unitResources = loadFinalResources(context, reportUnit.getResources());
		return setContextClassLoader(context, unitResources, inMemoryUnit, repositoryContextHandle);
	}

	protected Map loadFinalResources(ExecutionContext context, List<ResourceReference> resources) {
		Map finalResources = new LinkedHashMap();
		if (resources != null) {
			for (ResourceReference resRef : resources) {
				FileResource resource = getFinalFileResource(context, resRef);
				finalResources.put(resRef, resource);
			}
		}

		return finalResources;
	}

	protected OrigContextClassLoader setContextClassLoader(ExecutionContext context, Map unitResources, boolean inMemoryUnit,
			RepositoryContextHandle repositoryContextHandle) {
		Thread thread = Thread.currentThread();
		ClassLoader origClassLoader = thread.getContextClassLoader();
		ClassLoader jarsClassLoader;
		ClassLoader newClassLoader = null;

		List jarFiles = getJarFiles(context, unitResources, !inMemoryUnit);
		if (jarFiles.isEmpty()) {
			jarsClassLoader = origClassLoader;
		} else if (origClassLoader instanceof JarsClassLoader
				//check for reentrant classloader context
				&& ((JarsClassLoader) origClassLoader).hasSameJarFiles(jarFiles)) {
			jarsClassLoader = origClassLoader;
		} else {
			newClassLoader = jarsClassLoader = getJarsClassLoader(origClassLoader, jarFiles);
		}

		RepositoryContext repositoryContext = repositoryContextHandle == null ? null : repositoryContextHandle.getRepositoryContext();
		if (repositoryContext != null || RepositoryUtil.hasThreadRepositoryContext()) {
			//use the repository context for the keys? not required for now as for now the context is always freshly set on the thread.
			Map resourceBundleKeys = getResourceBundleKeys(context, unitResources);
			if (!resourceBundleKeys.isEmpty()) {
				newClassLoader = getResourcesClassLoader(jarsClassLoader, 
						resourceBundleKeys, inMemoryUnit, repositoryContext);
			}
		}

		OrigContextClassLoader origContext;
		if (newClassLoader == null) {
			origContext = OrigContextClassLoader.NOT_SET;
		} else {
			origContext = new OrigContextClassLoader(origClassLoader, newClassLoader, jarFiles);
			thread.setContextClassLoader(newClassLoader);
		}

		return origContext;
	}

	protected ClassLoader getJarsClassLoader(ClassLoader origClassLoader, List jarFiles) {
		boolean caching = true;
		for (Iterator it = jarFiles.iterator(); caching && it.hasNext();) {
			CacheObject cacheJarFile = (CacheObject) it.next();
			caching &= cacheJarFile.isCached();
		}

		ClassLoader classLoader;
		if (caching) {
			Map childrenClassLoaders;
			synchronized (jarsClassLoaderCache) {
				childrenClassLoaders = (Map) jarsClassLoaderCache.get(origClassLoader);
				if (childrenClassLoaders == null) {
					childrenClassLoaders = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT);
					jarsClassLoaderCache.put(origClassLoader, childrenClassLoaders);
				}
			}
			Object classLoaderKey = getJarFileNames(jarFiles);
			synchronized (childrenClassLoaders) {
				classLoader = (ClassLoader) childrenClassLoaders.get(classLoaderKey);
				if (classLoader == null) {
					if (log.isDebugEnabled()) {
						log.debug("Creating class loader for parent " + origClassLoader + " and jars " + classLoaderKey);
					}
					classLoader = createJarsClassLoader(origClassLoader, jarFiles);
					childrenClassLoaders.put(classLoaderKey, classLoader);
				}
			}
		} else {
			classLoader = createJarsClassLoader(origClassLoader, jarFiles);
		}
		return classLoader;
	}

	protected ClassLoader createJarsClassLoader(ClassLoader origClassLoader, List jarFiles) {
		JarFile[] jars = new JarFile[jarFiles.size()];
		int i = 0;
		for (Iterator it = jarFiles.iterator(); it.hasNext(); ++i) {
			jars[i] = (JarFile) ((CacheObject) it.next()).getObject();
		}

		return new JarsClassLoader(jars, origClassLoader,
				reportJarsProtectionDomainProvider.getProtectionDomain(), reportUnitClassLoadingEnabled);
	}

	private Object getJarFileNames(List jarFiles) {
		List jarFileNames = new ArrayList(jarFiles.size());
		for (Iterator it = jarFiles.iterator(); it.hasNext();) {
			JarFile jar = (JarFile) ((CacheObject) it.next()).getObject();
			jarFileNames.add(jar.getName());
		}
		return jarFileNames;
	}

	protected ClassLoader getResourcesClassLoader(
			ClassLoader parent, Map resourceBundleKeys, boolean inMemoryUnit,
			RepositoryContext repositoryContext) {
		ClassLoader repositoryResourceClassLoader;
		if (inMemoryUnit) {
			repositoryResourceClassLoader = new RepositoryResourceClassLoader(
					parent, resourceBundleKeys, true, repositoryContext);
		} else {
			Map childrenClassLoaders;
			synchronized (resourcesClassLoaderCache) {
				childrenClassLoaders = (Map) resourcesClassLoaderCache.get(parent);
				if (childrenClassLoaders == null) {
					childrenClassLoaders = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT);
					resourcesClassLoaderCache.put(parent, childrenClassLoaders);
				}
			}
			synchronized (childrenClassLoaders) {
				//put something from the repository context in the key?
				repositoryResourceClassLoader = (ClassLoader) childrenClassLoaders.get(resourceBundleKeys);
				if (repositoryResourceClassLoader == null) {
					if (log.isDebugEnabled()) {
						log.debug("Creating class loader for parent " + parent 
								+ " and resources " + resourceBundleKeys);
					}
					repositoryResourceClassLoader = new RepositoryResourceClassLoader(
							parent, resourceBundleKeys, false, repositoryContext);
					childrenClassLoaders.put(resourceBundleKeys, repositoryResourceClassLoader);
				}
			}
		}
		return repositoryResourceClassLoader;
	}

	protected List getJarFiles(ExecutionContext context, Map unitResources, boolean cache) {
		List jarFiles = new ArrayList();
		for (Iterator it = unitResources.values().iterator(); it.hasNext();) {
			FileResource resource = (FileResource) it.next();
			if (resource.getFileType().equals(FileResource.TYPE_JAR)) {
				CacheObject cacheJarFile = getCacheJarFile(context, resource, cache);
				jarFiles.add(cacheJarFile);
			}
		}
		return jarFiles;
	}

	protected Map getResourceBundleKeys(ExecutionContext context, Map unitResources) {
		Map resourceBundleKeys = new HashMap();
		for (Iterator it = unitResources.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			ResourceReference resRef = (ResourceReference) entry.getKey();
			FileResource finalResource = (FileResource) entry.getValue();
			if (finalResource.getFileType().equals(FileResource.TYPE_RESOURCE_BUNDLE)) {
				String resName;
				if (resRef.isLocal()) {
					resName = resRef.getLocalResource().getName();
				} else {
					resName = finalResource.getName();//TODO first reference name?
				}
				
				if (!reportUnitClassLoadingEnabled && (
						DefaultExtensionsRegistry.EXTENSION_RESOURCE_NAME.equals(resName)
						|| resName.endsWith(".class"))) {
					//avoid abuse
					continue;
				}
				
				String resPathKey = getRepositoryContextManager().getRepositoryPathKey(
						finalResource.getURIString());
				String uri = getRepositoryContextManager().getRepositoryUriForKey(resPathKey);
				RepositoryResourceKey resourceKey = new RepositoryResourceKey(resPathKey, 
						uri, finalResource.getVersion(), finalResource.getCreationDate());
				resourceBundleKeys.put(resName, resourceKey);
			}
		}
		return resourceBundleKeys;
	}

	protected JasperReport getJasperReport(ExecutionContext context, ReportUnitRequestBase request, ReportUnit reportUnit, boolean inMemoryUnit) {
        JasperDesignCache cache = JasperDesignCache.getInstance(DefaultJasperReportsContext.getInstance(), request.getReportContext());
        return getJasperReport(context, cache, reportUnit, inMemoryUnit);
    }
        
	protected JasperReport getJasperReport(ExecutionContext context, JasperDesignCache cache, ReportUnit reportUnit, boolean inMemoryUnit) {
        FileResource reportRes = getFinalResource(context, reportUnit.getMainReport(), FileResource.class);
        String location = reportUnit.getPath();
        return getJasperReport(context, cache, reportRes, location, inMemoryUnit);
    } 
    
	protected JasperReport getJasperReport(ExecutionContext context, JasperDesignCache cache, FileResource reportRes, String location, boolean inMemoryUnit)
	{
		JasperReport report = null;
		try {
			if (inMemoryUnit) {
				InputStream fileResourceData = getFileResourceDataStream(context, reportRes);
				report = compileReport(fileResourceData);
			} else {
				//TODO use an in-memory cache of compiled reports
				
				if (cache != null) {
					report = cache.getJasperReport(location);

					//JS-58747 - in the earlier version, getJasperReport() was implicitly setting jasperDesign in the cache,
					// but current jasperreports lib doesn't do this, so we are explicitly calling getJasperDesign().
					// here getJasperDesign() method will extract jasperDesign from reports if the jasperDesign in the cache is null.
					cache.getJasperDesign(location);
				}
				
				if (report == null) {
					InputStream compiledReport = getCompiledReport(context, reportRes);
					try {
						report = (JasperReport) JRLoader.loadObject(compiledReport);
						
						if (cache != null)
						{
							cache.set(location, report);
						}
						
					} catch (JRException e) {
						Throwable cause = e.getCause();
						if (cause == null || !(cause instanceof InvalidClassException)) {
							throw e;
						}
						
						if (log.isInfoEnabled()) {
							log.info("InvalidClassException caught while loading compiled report, clearing the compiled report cache");
						}
						clearCompiledReportCache();
						
						//recompiling the report
						compiledReport = getCompiledReport(context, reportRes);
						report = (JasperReport) JRLoader.loadObject(compiledReport);
					}
				}
				
				
			}
			return report;
		} catch (JRException e) {
			log.error(e, e);
			throw new JSExceptionWrapper(e);
		}
	}
	
	protected JasperReport getJasperReport(ExecutionContext context, ReportUnit reportUnit, boolean inMemoryUnit) {
		FileResource reportRes = getFinalResource(context, reportUnit.getMainReport(), FileResource.class);
		JasperReport report;
		try {
			if (inMemoryUnit) {
				InputStream fileResourceData = getFileResourceDataStream(context, reportRes);
				report = compileReport(fileResourceData);
			} else {
				//TODO use an in-memory cache of compiled reports
				InputStream compiledReport = getCompiledReport(context, reportRes);
				try {
					report = (JasperReport) JRLoader.loadObject(compiledReport);
				} catch (JRException e) {
					Throwable cause = e.getCause();
					if (cause == null || !(cause instanceof InvalidClassException)) {
						throw e;
					}
					
					if (log.isInfoEnabled()) {
						log.info("InvalidClassException caught while loading compiled report, clearing the compiled report cache");
					}
					clearCompiledReportCache();

					//recompiling the report
					compiledReport = getCompiledReport(context, reportRes);
					report = (JasperReport) JRLoader.loadObject(compiledReport);
				}
			}
			return report;
		} catch (JRException e) {
			log.error(e, e);
			throw new JSExceptionWrapper(e);
		}
	}

	protected void clearCompiledReportCache() {
		compiledReportsCache.clearCache(cacheableCompiledReports);
	}

    protected void addReportUnitToAuditEvent(final ReportUnit reportUnit) {
        getAuditContext().doInAuditContext(AuditEventType.RUN_REPORT.toString(),
                new AuditContext.AuditContextCallbackWithEvent() {
                    public void execute(AuditEvent auditEvent) {
                        auditEvent.setResourceUri(reportUnit.getURI());
                        auditContext.setResourceTypeToAuditEvent(reportUnit.getResourceType(), auditEvent);
                        String uri = reportUnit.getDataSource() != null ? reportUnit.getDataSource().getReferenceURI() : "";
                        getAuditContext().addPropertyToAuditEvent("dataSource", uri, auditEvent);
                        getAuditContext().addPropertyToAuditEvent("resourceName", reportUnit.getLabel(), auditEvent);
                    }
                });
    }

    protected void addPropertyToAuditEvent(final String propertyType, final Object param) {
        getAuditContext().doInAuditContext(AuditEventType.RUN_REPORT.toString(),
                new AuditContext.AuditContextCallbackWithEvent() {
                    public void execute(AuditEvent auditEvent) {
                        getAuditContext()
                                .addPropertyToAuditEvent(propertyType, param, auditEvent);
                    }
                });
    }

	protected void addPropertyToAuditEvent(final String propertyType, final Object param, boolean append, Class type) {
		getAuditContext().doInAuditContext(AuditEventType.RUN_REPORT.toString(),
				new AuditContext.AuditContextCallbackWithEvent() {
					public void execute(AuditEvent auditEvent) {
						getAuditContext()
								.addPropertyToAuditEvent(propertyType, param, auditEvent, append, type);
					}
				});
	}

	protected void fillReport(ExecutionContext context,
			ReportUnit reportUnit, JasperReport report, Map reportParameters, 
			ReportDataSource datasource, Query query, ReportFiller filler, boolean hasCachedData) {
		ReportDataSourceService dataSourceService = null;
		boolean dsClosing = false;
        addReportUnitToAuditEvent(reportUnit);
        RepositoryContextHandle repositoryContextHandle = null;
        String originalTopicDataSource = getOriginalDataAdapterTopicUri(datasource);
        if (originalTopicDataSource != null)
            repositoryContextHandle = setThreadRepositoryContext(context, reportUnit, originalTopicDataSource);
		try {
			if (datasource != null) {
				try {
					long startTime = System.currentTimeMillis();
					dataSourceService = createDataSourceService(datasource, hasCachedData);
					dataSourceService.setReportParameterValues(reportParameters, hasCachedData);
					addPropertyToAuditEvent("dataPrepTime",System.currentTimeMillis()-startTime, true, Long.class);
				} catch (RuntimeException e) {
					Optional<ErrorTemplateReportService> errorDSService = getErrorReportDataSourceService(e, reportParameters);

					if (errorDSService.isPresent()) {
						report = getJasperReport(context, null,
							errorDSService.get().getReportJrxml(context), errorDSService.get().getJrxmlTemplateRepoUri(), true);
						dataSourceService = errorDSService.get();
					} else {
						throw e;
					}
				}
			}

            long renderingStartTime = System.currentTimeMillis();
            addPropertyToAuditEvent("reportRenderingStartTime", new Date(renderingStartTime));
			addPropertyToAuditEvent("reportRenderingEpochStartTime", renderingStartTime);
			if (query == null) {
				filler.fillReport(report, reportParameters, null);
			} else {
				fillQueryReport(context, report, reportParameters, query, filler);
			}
            addPropertyToAuditEvent("reportRenderingTime", System.currentTimeMillis() - renderingStartTime, true, Long.class);

			dsClosing = true;
			if (dataSourceService != null) {
				dataSourceService.closeConnection();
				dataSourceService = null;
			}
		} catch (JRException e) {
			log.error("Error while filling report", e);
			throw new JSExceptionWrapper(e);
		} finally {
            resetThreadRepositoryContext(repositoryContextHandle);
			if (!dsClosing && dataSourceService != null) {
				//only exception cases
				try {
					dataSourceService.closeConnection();
				} catch (Exception e) {
					log.error("Error while closing data source connection", e);
				}
			}
		}
	}

	private Optional<ErrorTemplateReportService> getErrorReportDataSourceService(Exception e, Map reportParameters) {
		final String name = e.getClass().getName();
		final ErrorTemplateReportService service = errorReportDSFactory.get(name);

		if (service != null) {
			reportParameters.put(ERROR_PARAM, e);

			service.setReportParameterValues(reportParameters);

			return Optional.of(service);

		} else {
			return Optional.absent();
		}
	}

	protected JRDataSource createQueryDataSource(JRQueryExecuter queryExecuter)
			throws JRException {
		checkRequestCanceled();

		if (log.isDebugEnabled()) {
			log.debug("Executing query for request " + currentExecutionId());
		}

		QueryExecuterCancelable cancelable = new QueryExecuterCancelable(queryExecuter);
		setExecutionCancelable(cancelable);
		try {
			return queryExecuter.createDatasource();
		} finally {
			clearExecutionCancelable();

			if (log.isDebugEnabled()) {
				log.debug("Ended query for request " + currentExecutionId());
			}
		}
	}

	protected JasperPrint fillReport(JasperReport report, Map parameters,
			JRDataSource dataSource) throws JRException {
		FillResultListener result = fillReport(getEffectiveJasperReportsContext(), report, parameters, dataSource);
		return result.getJasperPrint();
	}

	protected FillResultListener fillReport(JasperReportsContext jasperReportsContext, 
			JasperReport report, Map parameters, JRDataSource dataSource) throws JRException {
		FillResultListener fillResult = new FillResultListener();
		checkRequestCanceled();

		if (log.isDebugEnabled()) {
			log.debug("Filling report for request " + currentExecutionId());
		}

		SynchronousFillHandle fillHandle = new SynchronousFillHandle(jasperReportsContext, 
				report, parameters, dataSource);
		// add the listener that will store the result
		fillHandle.addListener(fillResult);

		// set the cancelable
		FillHandleCancelable cancelable = new FillHandleCancelable(fillHandle);
		setExecutionCancelable(cancelable);
		try {
			// run the fill
			fillHandle.startFill();
		} finally {
			clearExecutionCancelable();
		}

		if (log.isDebugEnabled()) {
			log.debug("Ended fill for request " + currentExecutionId());
		}

		// the fill runs on the same thread, so we can set the following and return the result
		fillResult.setPaginated(fillHandle.isPaginated());
		fillResult.setUsedPageWidth(fillHandle.getUsedPageWidth());
		return fillResult;
	}

	private void setReportCompiler(JasperDesignCache cache, 
			final ReportUnit reportUnit, 
			final boolean inMemoryUnit, 
			final RepositoryContextHandle initialContextHandle, 
			final OrigContextClassLoader initialContextClassLoader, 
			final Map unitResources) {
		
		if (cache == null)
			return;
		
		cache.setReportCompiler(new ReportCompiler() {
			@Override
			public JasperReport compile(JasperDesign design) throws JRException {
				RepositoryContext initialContext = initialContextHandle.getRepositoryContext();
				RepositoryContext currentContext = RepositoryUtil.getThreadRepositoryContext();
				
				RepositoryContextHandle newContextHandle = null;
				if (currentContext == null || !currentContext.equals(initialContext)) {
					newContextHandle = setThreadRepositoryContext(
							initialContext.getExecutionContext(), 
							reportUnit, reportUnit.getURIString());
				}
				
				try {
					OrigContextClassLoader newContextClassLoader = null;
					ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
					if (initialContextClassLoader.set && (currentClassLoader == null
							|| !currentClassLoader.equals(initialContextClassLoader.newClassLoader))) {
						newContextClassLoader = setContextClassLoader(
								initialContext.getExecutionContext(), unitResources, inMemoryUnit, 
								newContextHandle == null ? initialContextHandle : newContextHandle);
					}
						
					try {
						return compileReport(design);
					} finally {
						if (newContextClassLoader != null) {
							revert(newContextClassLoader);
						}
					}
				} finally {
					if (newContextHandle != null) {
						resetThreadRepositoryContext(newContextHandle);
					}
				}
			}
		});
	}

    private String getOriginalDataAdapterTopicUri(ReportDataSource datasource) {
        if (datasource instanceof DataView) return ((DataView)datasource).getOriginalDataAdapterTopic();
        return null;
    }

	protected void fillQueryReport(ExecutionContext context, JasperReport report, Map reportParameters, Query query,
			ReportFiller filler) throws JRException {
		JRQueryExecuter queryExecuter = JRQueryExecuterAdapter.createQueryExecuter(report, reportParameters, query);
		boolean closing = false;
		try {
			JRDataSource reportDatasource = createQueryDataSource(queryExecuter);
			filler.fillReport(report, reportParameters, reportDatasource);

			closing = true;
			queryExecuter.close();
		} finally {
			if (!closing) {
				queryExecuter.close();
			}
		}
	}

	@WithSpan
	public ReportDataSourceService createDataSourceService(ReportDataSource dataSource) {
		java.util.Optional.ofNullable(dataSource).ifPresent(c -> Span.current().setAttribute("resourceUri", c.getURIString()));
		ReportDataSourceServiceFactory factory = (ReportDataSourceServiceFactory) getDataSourceServiceFactories().getBean(dataSource.getClass());
		return factory.createService(dataSource);
	}

	protected ReportDataSourceService createDataSourceService(ReportDataSource dataSource, boolean hasCachedData) {
		ReportDataSourceServiceFactory factory = (ReportDataSourceServiceFactory) getDataSourceServiceFactories().getBean(dataSource.getClass());
		return factory.createService(dataSource, hasCachedData);
	}

	/**
	 *
	 */
	public Resource[] getResources(ResourceReference jrxmlReference)
	{
		//TODO context?
		FileResource jrxml = getFinalResource(null, jrxmlReference, FileResource.class);
		return ResourceCollector.getResources(getFileResourceDataStream(null, jrxml));
	}

	protected <T extends Resource> T getRepositoryResource(ExecutionContext context, String uri, Class<T> type)
	{
		return (T) getRepositoryService().getResource(context, uri, type);
	}

    /**
	 * get an execution context containing a PermissionOverride which lets you access execute-only resources
	 * when you are running a report
	 * @return
	 */
    public ExecutionContext getRuntimeExecutionContext() {
    	return ExecutionContextImpl.getRuntimeExecutionContext();
    }

    public static ExecutionContext getRuntimeExecutionContext(ExecutionContext originalContext) {
    	return originalContext == null 
				? ExecutionContextImpl.getRuntimeExecutionContext() 
				: ExecutionContextImpl.getRuntimeExecutionContext(originalContext);
    }                                              

	public <T extends Resource> T getFinalResource(ExecutionContext context, ResourceReference res, Class<T> type) {
		T finalRes;
        if(res == null) {
            return null;
        }
		if (res.isLocal()) {
			finalRes = (T) res.getLocalResource();
		} else {
			finalRes = getRepositoryResource(context, res.getReferenceURI(), type);
		}
		return finalRes;
	}

	protected FileResource getFinalFileResource(ExecutionContext context, ResourceReference resRef) {
		ExecutionContext runtimeContext = getRuntimeExecutionContext(context);
		FileResource res = getFinalResource(runtimeContext, resRef, FileResource.class);
		while (res.isReference()) {
			res = getRepositoryResource(runtimeContext, res.getReferenceURI(), FileResource.class);
		}
		return res;
	}

	public ValidationResult validate(ExecutionContext context, ReportUnit reportUnit) {
		OrigContextClassLoader origContext = setContextClassLoader(context, reportUnit, true);
		ValidationResultImpl result = new ValidationResultImpl();
		try {
			ResourceReference mainReport = reportUnit.getMainReport();
			if (mainReport != null) {
				validateJRXML(context, result, mainReport);
			}

			List resources = reportUnit.getResources();
			if (resources != null && !resources.isEmpty()) {
				for (Iterator iter = resources.iterator(); iter.hasNext();) {
					ResourceReference resource = (ResourceReference) iter.next();
					validateJRXML(context, result, resource);
				}
			}
		} finally {
			revert(origContext);
		}
		return result;
	}

	protected void validateJRXML(ExecutionContext context, ValidationResultImpl result, ResourceReference resourceRef) {
		FileResource resource = getFinalFileResource(context, resourceRef);
		if (resource.getFileType().equals(FileResource.TYPE_JRXML)) {
			try {
				JasperCompileManager.compileReport(getFileResourceDataStream(context, resource));
			} catch (JRException e) {
				ValidationDetailImpl detail = new ValidationDetailImpl();
				detail.setValidationClass(FileResource.class);
				detail.setName(resource.getName());
				detail.setLabel(resource.getLabel());
				detail.setResult(ValidationResult.STATE_ERROR);
				detail.setException(e);
				detail.setMessage(e.getMessage());
				result.addValidationDetail(detail);
			}
		}
	}

	@WithSpan
	public ReportUnitResult executeReportUnitRequest(ExecutionContext context, ReportUnitRequest request) {
		ReportUnit reportUnit = getRepositoryResource(context, request.getReportUnitUri(), ReportUnit.class);
		return fillReport(context, request, reportUnit, false);
	}
	@WithSpan
	public ReportUnitResult executeTrialReportUnitRequest(ExecutionContext context, TrialReportUnitRequest request) {
		return fillReport(context, request, request.getReportUnit(), true);
	}

	public InputStream getCompiledReport(ExecutionContext context, InputStream jrxmlData) {
		JasperReport report = compileReport(jrxmlData);
		byte[] reportBytes = reportBytes(report);
		return new ByteArrayInputStream(reportBytes);
	}

	protected JasperReport compileReport(InputStream jrxmlData) {
		JasperDesign design = loadReportDesign(jrxmlData);
		JasperReport report = compileReport(design);
		return report;
	}

	protected JasperDesign loadReportDesign(InputStream jrxmlData) {
		JasperDesign design;
		try {
			design = JRXmlLoader.load(jrxmlData);
		} catch (JRException e) {
			log.error("error loading JRXML", e);
			throw new JSExceptionWrapper(e);
		}
		return design;
	}

	protected JasperReport compileReport(JasperDesign design) {
		if (log.isDebugEnabled()) {
			log.debug("compiling report " + design.getName());
		}
		
		try {
			JasperReport report = JasperCompileManager.compileReport(design);
			return report;
		} catch (JRException e) {
			log.error("error compiling report", e);
			java.util.Optional.ofNullable(e.getArgs()).ifPresent(args -> Arrays.stream(args).map(Objects::toString).forEach(log::error));
			throw new JSExceptionWrapper(e);
		}
	}

	protected InputStream getCompiledReport(ExecutionContext context, FileResource jrxml) {
		return compiledReportsCache.cache(context, jrxml, cacheableCompiledReports);
	}

	protected byte[] reportBytes(JasperReport report) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			JRSaver.saveObject(report, bout);
			byte[] reportBytes = bout.toByteArray();
			return reportBytes;
		} catch (JRException e) {
			log.error(e, e);
			throw new JSExceptionWrapper(e);
		}
	}

	public InputStream getCompiledReport(ExecutionContext context, String jrxmlURI) {
		return compiledReportsCache.cache(context, jrxmlURI, cacheableCompiledReports);
	}

    private void addReportUnitTypeToAudit(final Resource reportUnit) {
        auditContext.doInAuditContext(AuditEventType.RUN_REPORT.toString(), new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                if (reportUnit != null) {
                    if (auditEvent.getResourceType() == null) {
                        auditContext.setResourceTypeToAuditEvent(reportUnit.getResourceType(), auditEvent);
                    }
                    if (auditEvent.getResourceUri() == null) {
                        auditEvent.setResourceUri(reportUnit.getURI());
                    }
                }
            }
        });
    }

    public JasperReport getMainJasperReport(ExecutionContext context, String reportUnitURI) {
        JasperReport jasperReport = null;
        ReportUnit reportUnit = getRepositoryResource(context, reportUnitURI, ReportUnit.class);
        if (reportUnit != null) {
            addReportUnitTypeToAudit(reportUnit);
            
            RepositoryContextHandle repositoryContextHandle = setThreadRepositoryContext(context, reportUnit, reportUnit.getURIString());
            try {
                OrigContextClassLoader origContext = setContextClassLoader(context, reportUnit, false, repositoryContextHandle);
                ExecutionContext executionContext = getRuntimeExecutionContext(context);
                try {
                	jasperReport = getJasperReport(executionContext, reportUnit, false);
                } finally {
                    revert(origContext);
                }
            } finally {
            	resetThreadRepositoryContext(repositoryContextHandle);
            }
        }
        return jasperReport;
    }

    @Override
    public JasperReport getMainJasperReport(ExecutionContext context, InputControlsContainer container) {
        JasperReport jasperReport = null;
        if (container != null) {
            if (container instanceof ReportUnit) {
                addReportUnitTypeToAudit(container);
                
                ReportUnit reportUnit = (ReportUnit) container;
                RepositoryContextHandle repositoryContextHandle = setThreadRepositoryContext(context, reportUnit, reportUnit.getURIString());
                try {
    				OrigContextClassLoader origContext = setContextClassLoader(context, reportUnit, false, repositoryContextHandle);
    				ExecutionContext executionContext = getRuntimeExecutionContext(context);
                    try {
                        jasperReport = getJasperReport(executionContext, reportUnit, false);
                    } finally {
                        revert(origContext);
                    }
                } finally {
                	resetThreadRepositoryContext(repositoryContextHandle);
                }
            } else {
                throw new JSException("Unsupported report object " + container.getClass().getName());
            }
        }

        return jasperReport;
    }

    public void release() {
		tempJarFiles.release();
	}

	protected void dispose(JarFile jarFile) {
		try {
			jarFile.close();
		} catch (IOException e) {
			log.warn("Unable to close jar file \"" + jarFile.getName() + "\"", e);
		}
		File file = new File(jarFile.getName());
		if (file.exists() && !file.delete()) {
			log.warn("Unable to delete jar file \"" + jarFile.getName() + "\"");
		}
	}

	public void clearCaches(Class resourceItf, String resourceURI) {
		if (FileResource.class.isAssignableFrom(resourceItf)) {
			//TODO check JRXML type
			compiledReportsCache.clearCache(ExecutionContextImpl.getRuntimeExecutionContext(), resourceURI, cacheableCompiledReports);

			tempJarFiles.remove(resourceURI);
		}
	}

	public SecurityContextProvider getSecurityContextProvider() {
		return securityContextProvider;
	}

	public void setSecurityContextProvider(
			SecurityContextProvider securityContextProvider) {
		this.securityContextProvider = securityContextProvider;
	}


	public OrderedMap executeQuery(ExecutionContext context,
			ResourceReference queryReference, String keyColumn, String[] resultColumns,
			ResourceReference defaultDataSourceReference) {
		return executeQuery(context, 
				queryReference, keyColumn, resultColumns, 
				defaultDataSourceReference, 
				null);
	}


    protected void addReportUnitToAuditEvent(final String dataSourceUri) {
        getAuditContext().doInAuditContext(AuditEventType.INPUT_CONTROLS_QUERY.toString(),
                new AuditContext.AuditContextCallbackWithEvent() {
                    public void execute(AuditEvent auditEvent) {
                        getAuditContext().addPropertyToAuditEvent("dataSource", dataSourceUri, auditEvent);
                    }
                });
    }

    protected ReportDataSource getQueryDataSource(ExecutionContext context, ResourceReference dataSourceReference, ResourceReference defaultDataSourceReference) {

        if (dataSourceReference == null) {
            dataSourceReference = defaultDataSourceReference;
        }

        addReportUnitToAuditEvent(dataSourceReference != null ? dataSourceReference.getTargetURI() : null);

        return (ReportDataSource) getFinalResource(context, dataSourceReference, Resource.class);
    }

    public OrderedMap executeQuery(ExecutionContext context,
                                   ResourceReference queryReference, String keyColumn, String[] resultColumns,
                                   ResourceReference defaultDataSourceReference,
                                   Map parameterValues) {
        return executeQuery(context, queryReference, keyColumn, resultColumns, defaultDataSourceReference, parameterValues, null, true);
    }

	@WithSpan
	public OrderedMap executeQuery(ExecutionContext context,
			ResourceReference queryReference, String keyColumn, String[] resultColumns,
			ResourceReference defaultDataSourceReference,
			Map parameterValues, Map<String, Class<?>> parameterTypes, boolean formatValueColumns) {
		java.util.Optional.ofNullable(queryReference).ifPresent(c -> Span.current().setAttribute("resourceUri", c.getTargetURI()));
		// set exec-only context
		context = getRuntimeExecutionContext(context);
		
		Query query = getFinalResource(context, queryReference, Query.class);

		ReportDataSource dataSource = getQueryDataSource(context, query.getDataSource(), defaultDataSourceReference);

		ReportDataSourceService dataSourceService = createDataSourceService(dataSource);
		boolean dsClosing = false;
		try {
			Map parameters = new HashMap();

			if (parameterValues != null) {
                parameters.putAll(parameterValues);
			}

            List additionalParameters = new ArrayList();
			setBuiltinParameters(context, false, null, parameters, additionalParameters);

			dataSourceService.setReportParameterValues(parameters);
			if (queryManipulator != null) {
				String originalSQL = query.getSql();
				String newSQL;
				if (queryManipulator instanceof IDataSourceAwareQueryManipulator) {
					newSQL = ((IDataSourceAwareQueryManipulator) queryManipulator).updateQuery(originalSQL, parameters, dataSource);
				} else {
					newSQL = queryManipulator.updateQuery(query.getSql(), parameters);
				}
                query.setSql(newSQL);
			}
			long start = System.currentTimeMillis();
			OrderedMap result = JRQueryExecuterAdapter.executeQuery(query, 
					keyColumn, resultColumns, 
					parameters, parameterTypes, additionalParameters, formatValueColumns, dataSource);
			if (valueQueryLog.isDebugEnabled()) {
				valueQueryLog.debug("query took " + (System.currentTimeMillis() - start) + " ms: " + query.getSql());
				valueQueryLog.debug("params: " + parameters);
			}
			dsClosing = true;
			if (dataSourceService != null) {
				dataSourceService.closeConnection();
				dataSourceService = null;
			}

			return result;
		} finally {
			if (!dsClosing && dataSourceService != null) {
				//only exception cases
				try {
					dataSourceService.closeConnection();
				} catch (Exception e) {
					log.error("Error while closing data source connection", e);
				}
			}
		}

	}


	public void setBuiltinParameters(ExecutionContext context, boolean onlyExistingParameters, JRParameter[] existingJRParameters,
                Map parametersMap, List additionalParameters) {
            for (Object o : getBuiltInParameterProviders() ) {
                BuiltInParameterProvider pProvider = (BuiltInParameterProvider) o;

                // set standard parameters that will always be available

                List<Object[]> results = pProvider.getParameters(context, additionalParameters, parametersMap);

                for (Object[] aResult : results) {
                    setBuiltInParameter(context, onlyExistingParameters, existingJRParameters, parametersMap, additionalParameters, aResult);
                }

                // add parameters that are built in and requested
                // This is only needed when we are running a report.
                // Parameters in stand alone queries (engineService.executeQuery) are managed by FilterCore (cascading engine)
                if (onlyExistingParameters) {
                    for (JRParameter jrParameter : existingJRParameters) {
                        Object parameterMapValue = parametersMap.get(jrParameter.getName());
                        if (parameterMapValue == null) {
                            Object[] aResult = pProvider.getParameter(context, additionalParameters, parametersMap, jrParameter.getName());
                            if (aResult != null) {
                                setBuiltInParameter(context, onlyExistingParameters, existingJRParameters, parametersMap, additionalParameters, aResult);
                            }
                        }
                    }
                }
            }
	}

    public void setBuiltInParameter(ExecutionContext context, boolean onlyExistingParameters, JRParameter[] existingJRParameters,
                Map parametersMap, List additionalParameters, Object[] aResult) {

        JRParameter jrParameter = (JRParameter) aResult[0];
        Object aValue = aResult[1];
        boolean set = false;

        Object parameterFromMap = parametersMap.get(jrParameter.getName());

        JRParameter existingJRParameter = null;

        if (onlyExistingParameters) {
            for (JRParameter existingJRParameterFromArray : existingJRParameters) {
                if (existingJRParameterFromArray.getName().equals(jrParameter.getName())) {
                    existingJRParameter = existingJRParameterFromArray;
                    break;
                }
            }
        }

        if (onlyExistingParameters) {
            if (existingJRParameter == null) {
                // don't add: leave set = false
/* The default value expression has lower priority than user defined value. Bug #32316
            } else if (existingJRParameter.getDefaultValueExpression() != null) {
                //do not set if parameter has default value expression
                if (log.isDebugEnabled()) {
                    log.debug("Report parameter " + jrParameter.getName() + " has a default value expression, not setting value");
                }
*/
            } else if (!existingJRParameter.getValueClass().isAssignableFrom(jrParameter.getValueClass())) {
                //do not set if parameter has type incompatible with parameter
                if (log.isDebugEnabled()) {
                    log.debug("Report parameter " + jrParameter.getName() + " type " + jrParameter.getValueClassName() + " not compatible with java.lang.String, not setting value");
                }
            } else {
                //set the value
                set = true;
            }
        } else if (parameterFromMap == null) {
            //set the value anyway, it will be accessible through the parametersMap map
            set = true;
        }

        if (set) {
            if (log.isDebugEnabled()) {
                log.debug("Setting report parameter " + jrParameter.getName() + " to " + aValue);
            }

            if (!onlyExistingParameters) {
                additionalParameters.add(jrParameter);
            }

            parametersMap.put(jrParameter.getName(), aValue);
        }

    }

	public Map<String, ?> getResolvedBuiltinParameterValues(ExecutionContext ctx, JRParameter[] allJRParameters) {
		Map<String, ?> builtInParameters = new HashMap<String, Object>();

		setBuiltinParameters(ctx, true, allJRParameters, builtInParameters, null);

		return builtInParameters;
	}

	public ResourceLookup[] getDataSources(ExecutionContext context, String queryLanguage) {
		ResourceLookup[] datasources;
		if (queryLanguage == null) {
			datasources = repository.findResource(context, FilterCriteria.createFilter(ReportDataSource.class));
		} else {
			Set dataSourceTypes = dataSourceServiceFactories.getSupportingDataSourceTypes(queryLanguage);
			if (dataSourceTypes == null || dataSourceTypes.isEmpty()) {
				datasources = null;
			} else {
				FilterCriteria[] criteria = new FilterCriteria[dataSourceTypes.size()];
				int i = 0;
				for (Iterator it = dataSourceTypes.iterator(); it.hasNext(); ++i) {
					Class type = (Class) it.next();
					criteria[i] = FilterCriteria.createFilter(type);
				}
				datasources = repository.findResources(context, criteria);
			}
		}
		return datasources;
	}

	public String getQueryLanguage(ExecutionContext context, ResourceReference jrxmlResource) {
		JasperDesign jasperDesign = loadJRXML(context, jrxmlResource);
		JRQuery query = jasperDesign.getQuery();
		return query == null ? null : query.getLanguage();
	}

	protected JasperDesign loadJRXML(ExecutionContext context, ResourceReference jrxmlResource) {
		JasperDesign jasperDesign;
		FileResource jrxmlRes = getFinalFileResource(context, jrxmlResource);
		InputStream jrxmlData = getFileResourceDataStream(context, jrxmlRes);
		boolean close = true;
		try {
			jasperDesign = JRXmlLoader.load(jrxmlData);

			close = false;
			jrxmlData.close();
		} catch (JRException e) {
			log.error("Error parsing JRXML", e);
			throw new JSExceptionWrapper(e);
		} catch (IOException e) {
			log.error(e, e);
			throw new JSExceptionWrapper(e);
		} finally {
			if (close) {
				try {
					jrxmlData.close();
				} catch (IOException e) {
					log.error(e, e);
				}
			}
		}
		return jasperDesign;
	}

	public Set getDataSourceTypes(ExecutionContext context, String queryLanguage) {
		return dataSourceServiceFactories.getSupportingDataSourceTypes(queryLanguage);
	}

    /*
     * you should use the getReportInputControlsInformation method and get the values
     * from there: ReportInputControlsInformation#getDefaultValuesMap()
     */
    @Deprecated
    public Map getReportInputControlDefaultValues(ExecutionContext context, String reportURI, Map initialParameters) {
        ReportUnit reportUnit = getRepositoryResource(context, reportURI, ReportUnit.class);
        ReportInputControlsInformation infos = this.getReportInputControlsInformation(context, reportUnit, initialParameters);
        return infos.getDefaultValuesMap();
    }

	/*
	 *
	 */
	public ReportInputControlsInformation getReportInputControlsInformation(ExecutionContext context, String reportURI, Map initialParameters) {
        ReportUnit reportUnit = getRepositoryResource(context, reportURI, ReportUnit.class);

        return getReportInputControlsInformation(context, reportUnit, initialParameters);
    }

	protected void setSnapshotParameterValues(ExecutionContext context,
			ReportUnit reportUnit, ReportInputControlsInformation infos) {
		Set<String> controlNames = infos.getControlNames();
		if (controlNames.isEmpty()) {
			// nothing to do
			return;
		}
		
		DataSnapshotPersistentMetadata snapshot = dataCacheProvider.loadSavedDataSnapshot(context, reportUnit);
		if (snapshot == null) {
			return;
        }
		
		Map<String, Object> snapshotParams = snapshot.getSnapshotMetadata().getParameters();
		if (snapshotParams == null || snapshotParams.isEmpty()) {
			return;
		}

		if (DiagnosticSnapshotPropertyHelper.isDiagSnapshotSet(snapshotParams)) {
			infos.setDiagnosticProperty(true);
		}

		if (log.isDebugEnabled()) {
			log.debug("setting default param values for " + reportUnit.getURIString()
					+ " from data snapshot " + reportUnit.getDataSnapshotId());
		}
		
		for (String controlName : controlNames) {
			ReportInputControlInformation info = infos.getInputControlInformation(controlName);
			if (snapshotParams.containsKey(controlName)) {
				Object value = snapshotParams.get(controlName);
				
				// restore original relative date values
				if (value instanceof MaterializedDataParameter) {
					value = ((MaterializedDataParameter) value).getParameterValue();
				}
				
				// check if it matches the expected type
				if (matchesInputControlType(info, value)) {
					info.setDefaultValue(value);
				}
			}
		}
	}

	protected boolean matchesInputControlType(ReportInputControlInformation info, Object value) {
		// nulls are allowed
		if (value == null) {
			return true;
		}
		
		// check the base value type
		Class<?> controlType = info.getValueType();

        //TODO: need to correctly handle case when we have inputControl without JRParameter -
        //in this case valueType and nestedType will be null.
        if (controlType == null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Could not determine type of snapshot parameter %s", info.getParameterName()));
            }

            return false;
        }

		if (!controlType.isInstance(value)) {
			if (log.isDebugEnabled()) {
				log.debug("snapshot parameter " + info.getParameterName() + " of type " + value.getClass().getName()
						+ " does not match expected type " + controlType.getName());
			}

			return false;
		}
		
		if (Collection.class.isAssignableFrom(controlType)) {
			// for collection types, check the first value against the nested type
			Collection<?> collectionValues = (Collection<?>) value;
			Class nestedType = info.getNestedType();
			if (nestedType != null && !collectionValues.isEmpty()) {
				// we're assuming a homogeneous collection, only looking at first value
				Object firstValue = collectionValues.iterator().next();
				if (!nestedType.isInstance(firstValue)) {
					if (log.isDebugEnabled()) {
						log.debug("snapshot collection parameter " + info.getParameterName() 
								+ " with item type " + firstValue.getClass().getName()
								+ " does not match expected type " + nestedType.getName());
					}

					return false;
				}
			}
		}
		
		return true;
	}

	/*
	 *
	 */
	@WithSpan
	@Override
	public ReportInputControlsInformation getReportInputControlsInformation(ExecutionContext context, InputControlsContainer icContainer, Map initialParameters) {

        RepositoryContextHandle repositoryContextHandle = setThreadRepositoryContext(context, (ResourceContainer) icContainer, icContainer.getURIString());
        try {
            OrigContextClassLoader origContext = setContextClassLoader(context, (ResourceContainer) icContainer, false, 
            		repositoryContextHandle);

            try {

                List<ResourceReference> inputControls = icContainer.getInputControls();
                MessageSource messages;

                JasperReport report;
                if(icContainer instanceof ReportUnit) {
                	ExecutionContext executionContext = getRuntimeExecutionContext(context);
                	report = getJasperReport(executionContext, (ReportUnit) icContainer, false);
                    messages = MessageSourceLoader.loadMessageSource(context, (ResourceContainer) icContainer, repository);
                } else {
                    throw new JRException("Unsupported report object " + icContainer.getClass().getName());
                }

				ReportInputControlsInformation infos = getReportInputControlsInformation(context, 
						report, inputControls, initialParameters, messages);
				
				if (icContainer instanceof ReportUnit) {
			        // use parameters values from the data snapshot if any
					setSnapshotParameterValues(context, (ReportUnit) icContainer, infos);
				}
				
				return infos;
			} catch (JRException e) {
				throw new JSExceptionWrapper(e);
			} finally {
				revert(origContext);
			}
		} finally {
			resetThreadRepositoryContext(repositoryContextHandle);
		}
	}

	/* 
	 * 
	 */
	protected ReportInputControlsInformation getReportInputControlsInformation(ExecutionContext context, JasperReport report, 
                List<ResourceReference> inputControls, Map initialParameters, MessageSource serverMessageSource) throws JRException {
        ReportInputControlsInformationImpl result = new ReportInputControlsInformationImpl();
        if (inputControls == null) {
            return result;
        }

		// add Locale, Timezone and Built-In Parameters
        initialParameters = getReportParameters(context, report, initialParameters);

		// collect the JRParameter array to a Map
        Map<String,JRParameter> jrParametersMap = new HashMap();
		JRParameter[] params = report.getParameters();
		for (int i = 0; i < params.length; i++) {
            jrParametersMap.put(params[i].getName(), params[i]);
		}

        ResourceBundle reportBundle = loadResourceBundle(report);

        // evaluate the default values from Jasper Report
        Map jrDefaultValues = getDefaultValuesFromJasperReport(
				JRParameterDefaultValuesEvaluator.evaluateParameterDefaultValues(getEffectiveJasperReportsContext(),
        		report, initialParameters));

        // build the JasperReportInputControlInformation object for given input controls
        for (ResourceReference inputControlRef : inputControls) {
            // ExecutionContext may contain no attribute.  If so, add "execute override" attribute before calling getFinalResource()
            InputControl inputControl = getFinalResource(getRuntimeExecutionContext(context), inputControlRef, InputControl.class);

            String name = inputControl.getName();
            JRParameter param = jrParametersMap.get(name);

            // try to get i18n label for input control
            String label = InputControlLabelResolver.resolve(inputControl.getLabel(), reportBundle, serverMessageSource);
            String description = InputControlLabelResolver.resolve(inputControl.getDescription(), reportBundle, serverMessageSource);

            // try to get i18n labels for List Of Values input controls options
            ReportInputControlValuesInformation valuesInformation = null;
            ResourceReference listOfValuesResourceReference = inputControl.getListOfValues();
            if (listOfValuesResourceReference != null && (serverMessageSource != null || reportBundle != null)) {
                valuesInformation = getReportInputControlValuesInformation(context, reportBundle, serverMessageSource, listOfValuesResourceReference);
            }

            ReportInputControlInformation info;
            boolean useGlobalDefaultValue = true;
            if (param != null) {
                useGlobalDefaultValue = jrDefaultValues.get(name) == null && param.getDefaultValueExpression() == null;
                JasperReportInputControlInformation jrInfo = new JasperReportInputControlInformation();
                jrInfo.setReportParameter(param);
                jrInfo.setDefaultValue(jrDefaultValues.get(name));
                jrInfo.setPromptLabel(label);
				jrInfo.setDescription(description);
                jrInfo.setReportInputControlValuesInformation(valuesInformation);
                info = jrInfo;
            } else {
                // input control is not assigned to any report parameter.
                // Create an anonymous ReportInputControlInformation class which doesn't need the JRParameter.
                info = createReportInputControlInformationWithoutParameter(inputControl.getName(), label, description, valuesInformation);
            }
            if(useGlobalDefaultValue){
                final ResourceReference dataTypeReference = inputControl.getDataType();
                DataType dataType = null;
                if(dataTypeReference != null) {
                    dataType = (DataType) (dataTypeReference.isLocal() ? dataTypeReference.getLocalResource()
                            : repository.getResource(ExecutionContextImpl.getRuntimeExecutionContext(),
                            dataTypeReference.getReferenceURI()));
                }
                info.setDefaultValue(globalDefaultValueProvider.getDefaultValueForDataType(dataType));
            }
            result.setInputControlInformation(name, info);
        }

        return result;
	}

	protected Map<String, Object>  getDefaultValuesFromJasperReport(Map<String, Object>jrDefaultValues) throws JRException {
		// JasperReport uses "," to separate items in TOPIC JRXML
		// and it escapes "," to "\,", we need to replace "\," back to "," before using in adhoc / visiualize

		if (jrDefaultValues != null) {
			for (Map.Entry entry: jrDefaultValues.entrySet()) {
				Object defaultValue = entry.getValue();
				if ((defaultValue != null) && (defaultValue instanceof Collection)) {
					ArrayList newList = new ArrayList();
					for (Object val : (Collection) defaultValue) {
						if (val != null) {
							newList.add(val.toString().replace("\\,", ","));
						} else {
							newList.add(null);
						}
					}
					entry.setValue(newList);
				}
			}
		}
		return jrDefaultValues;
	}



    private ReportInputControlInformation createReportInputControlInformationWithoutParameter(final String name,
                                final String label, final String description, final ReportInputControlValuesInformation valuesInformation) {
        return new ReportInputControlWithoutParameterInformation(name, label, description, valuesInformation);
    }

    private ReportInputControlValuesInformation getReportInputControlValuesInformation(
            ExecutionContext context, ResourceBundle resourceBundle, MessageSource messageSource, ResourceReference listOfValuesResourceReference) {

        ListOfValues listOfValuesResource = null;
        if (listOfValuesResourceReference.isLocal()) {
            listOfValuesResource = (ListOfValues) listOfValuesResourceReference.getLocalResource();
        } else {
            listOfValuesResource = (ListOfValues) repository.getResource(context, listOfValuesResourceReference.getReferenceURI());
        }

        return ReportInputControlValuesInformationLoader.getReportInputControlValuesInformation(listOfValuesResource, resourceBundle, messageSource);
    }

    protected ResourceBundle loadResourceBundle(JasperReport report) {
		ResourceBundle bundle = null;
		String baseName = report.getResourceBundle();
		if (baseName != null) {
			Locale locale = LocaleContextHolder.getLocale();
			bundle = JRResourcesUtil.loadResourceBundle(getEffectiveJasperReportsContext(), baseName, locale);
		}
		return bundle;
	}
	
	public String getReportParameterLabelKeyPrefix() {
		return reportParameterLabelKeyPrefix;
	}

	public void setReportParameterLabelKeyPrefix(
			String reportParameterLabelKeyPrefix) {
		this.reportParameterLabelKeyPrefix = reportParameterLabelKeyPrefix;
	}

	public byte[] compileReport(ExecutionContext context, FileResource resource) {
		if (log.isDebugEnabled()) {
			log.debug("compiling report " + resource.getURIString());
		}
		
		InputStream jrxmlData = getFileResourceDataStream(context, resource);
		JasperDesign design = loadReportDesign(jrxmlData);
		JasperReport report = compileReport(design);
		byte[] reportBytes = reportBytes(report);
		
		boolean updateJRXML = false;
		if (!design.hasUUID()) {
			//FIXME refactor this into a JRXMLFixer
			if (log.isDebugEnabled()) {
				log.debug("JRXML at " + resource.getURIString() + " does not have UUID");
			}
			
			if (isAutoUpdateJRXMLs()) {
				updateJRXML = true;
			}
		}
		
		if (jrxmlFixerList != null) {
			for (JRXMLFixer fixer : jrxmlFixerList) {
				if (fixer.fix(design)) {
					if (log.isDebugEnabled()) {
						log.debug("JRXML at " + resource.getURIString() + " updated by " + fixer);
					}
					
					updateJRXML = true;
				}
			}
		}
		
		if (updateJRXML) {
			if (log.isDebugEnabled()) {
				log.debug("Auto updating JRXML resource at " + resource.getURIString());
			}
			
			autoUpdateJRXMLResource(resource, design);
		}
		
		return reportBytes;
	}
	
	protected void autoUpdateJRXMLResource(FileResource resource, JasperDesign report) {
		try {
			MemoryDataContainer jrxmlData = new MemoryDataContainer();
			OutputStream jrxmlStream = jrxmlData.getOutputStream();
			JRXmlWriter.writeReport(report, jrxmlStream, "UTF-8");//always writing UTF-8
			jrxmlStream.close();
			
			// skipping object security
			getUnsecureRepository().replaceFileResourceData(resource.getURIString(), jrxmlData);
			
			// also updating in the resource if it includes the original data
			if (resource.hasData()) {
				resource.setData(jrxmlData.getData());
			}
		} catch (Exception e) {
			// only log the error
			log.warn("Failed to auto update JRXML resource with missing UUID at " + resource.getURIString(), e);
		}
	}
	
	public Executor getSyncReportExecutorService() {
		return syncReportExecutorService;
	}

	public void setSyncReportExecutorService(Executor syncReportExecutorService) {
		this.syncReportExecutorService = syncReportExecutorService;
	}

	public Executor getAsyncReportExecutorService() {
		return asyncReportExecutorService;
	}

	public void setAsyncReportExecutorService(Executor asyncReportExecutorService) {
		this.asyncReportExecutorService = asyncReportExecutorService;
	}

    public List<ReportExecutionStatusInformation> getReportExecutionStatusList() {
        return new ArrayList<ReportExecutionStatusInformation>(engineExecutions.values());
    }

    public List<ReportExecutionStatusInformation> getReportExecutionStatusList(ReportExecutionStatusSearchCriteria searchCriteria) {
        List<ReportExecutionStatusInformation> executions = getReportExecutionStatusList();
        if (executions == null)return null;
        List<ReportExecutionStatusInformation> newSet = new ArrayList<ReportExecutionStatusInformation>();
        for (ReportExecutionStatusInformation entry : executions) {
             Map<String, Object>  entryProperties = entry.getProperties();
            if ((searchCriteria.getReportURI() != null) && !searchCriteria.getReportURI().equalsIgnoreCase((String) entryProperties.get(EngineServiceImpl.ReportExecutionStatus.PROPERTY_REPORTURI)))
                continue;
            newSet.add(entry);
        }
        return newSet;
    }

    public ReportExecutionStatusInformation getReportExecutionStatusByID(String reportExecutionStatusID) {
        return engineExecutions.get(reportExecutionStatusID);
    }

    public List<ReportExecutionStatusInformation> getSchedulerReportExecutionStatusList() {
        List<ReportExecutionStatusInformation> executions =  getReportExecutionStatusList();
        if (executions == null)return null;
        List<ReportExecutionStatusInformation> newSet = new ArrayList<ReportExecutionStatusInformation>();
        for (ReportExecutionStatusInformation entry : executions) {
             Map<String, Object>  entryProperties = entry.getProperties();
            if ((entryProperties != null) && (entryProperties.size() > 0)) newSet.add(entry);
        }
        return newSet;
    }

    public List<ReportExecutionStatusInformation> getSchedulerReportExecutionStatusList(SchedulerReportExecutionStatusSearchCriteria searchCriteria) {
        List<ReportExecutionStatusInformation> executions = getReportExecutionStatusList();
		User loginUser = null;
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication()!= null) {
			if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User) {
				loginUser = (MetadataUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			} else {
				if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null) {
					log.error("Report execution scheduler:  Fail to detect login user");
				} else {
					log.error("Report execution scheduler:  Cannot cast login user to User.  Actual class: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().toString());
				}
			}
		}

		if (executions == null)return null;
        List<ReportExecutionStatusInformation> newSet = new ArrayList<ReportExecutionStatusInformation>();

		for (ReportExecutionStatusInformation entry : executions) {
			// see JS-59924
			if (!includeUser(loginUser, entry.getOwner(), (searchCriteria != null ? searchCriteria.getUserName() : null))) continue;

			Map<String, Object>  entryProperties = entry.getProperties();
			if (searchCriteria != null) {
				if ((searchCriteria.getJobID() != null) && !equals(searchCriteria.getJobID(), (entryProperties != null ? entryProperties.get(EngineServiceImpl.ReportExecutionStatus.PROPERTY_JOBID) : null)))
					continue;
				if ((searchCriteria.getJobLabel() != null) && !equals(searchCriteria.getJobLabel(), (entryProperties != null ? entryProperties.get(EngineServiceImpl.ReportExecutionStatus.PROPERTY_JOBLABEL) : null)))
					continue;
				if ((searchCriteria.getReportURI() != null) && !equals(searchCriteria.getReportURI(), entry.getReportURI()))
					continue;
				if ((searchCriteria.getFireTimeFrom() != null || searchCriteria.getFireTimeTo() != null) && !isBetween((Date) (entryProperties != null ? entryProperties.get(ReportExecutionStatus.PROPERTY_FIRETIME) : null), searchCriteria.getFireTimeFrom(), searchCriteria.getFireTimeTo()))
					continue;
			}
			newSet.add(entry);
		}


        return newSet;
    }

	protected boolean includeUser(User loginUser, User currentOwner, String searchUser) {
		/***
		 * if login user is superuser, include everything
		 * if login user is jasperadmin, include everything within the same organization
		 * joeuser can only see his own stuffs
		 */
		if (includeUser(loginUser, currentOwner)) {
			/***
			 * if specific search is not null, only include if the owner is same as specific search
			 */
			if (searchUser != null) return isSameUser(currentOwner, searchUser);
			else return true;
		}
		return false;
	}



	protected boolean includeUser(User loginUser, User currentOwner) {
		if (isAuthorizedExecution(loginUser, authorizedAllExecutionsRolesList, true)) return true;
		if (isAuthorizedExecution(loginUser, authorizedOrgExecutionsRolesList, false)) {
			if (isAuthorizedExecution(currentOwner, authorizedAllExecutionsRolesList, true)) return false;
			return  (equals(loginUser.getTenantId(), currentOwner.getTenantId()));
		}
		if (equals(loginUser.getUsername(), currentOwner.getUsername()) && equals(loginUser.getTenantId(), currentOwner.getTenantId())) return true;
		return false;
	}


	private boolean isAuthorizedExecution(User user, List<String> authorizedRolesList, boolean requireToBeSuperUser) {
		/***
		 * login user has to be a super user to see all organizations and root executions.
		 */
		if (requireToBeSuperUser) {
			if (user.getTenantId() != null) return false;
		}
		Set<String> roleNamesSet = new HashSet<String>();
		if (user.getRoles() != null) {
			@SuppressWarnings("unchecked")
			Set<Role> userRoles = (Set<Role>)user.getRoles();
			for (Role role : userRoles) {
				roleNamesSet.add(role.getRoleName());
			}
		}
		if (authorizedRolesList == null) {
			log.error("Report execution scheduler:  authorized executions roles list is not set properly.  Please check authorizedOrgExecutionsRolesList and authorizedAllExecutionsRolesList values.");
			return false;
		}
		for (String role : authorizedRolesList) {
			if (roleNamesSet.contains(role)) return true;
		}
		return false;
	}

	private boolean isSameUser(User currentOwner, String searchUser) {
		String searchOrg = null;
		if (searchUser != null && searchUser.contains("|")) {
			String[] userOrg = searchUser.split("\\|");
			searchUser = userOrg[0];
			searchOrg = userOrg[1];
		}
		if (searchOrg == null) return equals(currentOwner.getUsername(), searchUser);
		else return equals(currentOwner.getUsername(), searchUser) && equals(currentOwner.getTenantId(), searchOrg);

	}

	/**
     * Compares if report execution fire time is in given range (inclusive). From and to time are both nullable, so following cases are processed:
     * from != null && to != null - true if fire time in range (inclusive)
     * from == null && to != null - fire time should be before or equals to "to"
     * from != null && to == null - fire time should be after or equals to "from"
     * from == null && to == null - result is always false
     *
     * @param fireTime - fire time
     * @param from - search criteria from time, nullable
     * @param to - search criteria to time, nullable
     * @return true if fire time is in range (inclusive)
     */
    private boolean isBetween(Date fireTime, Date from, Date to){
        boolean result = false;
        if(fireTime != null && (from != null || to != null)){
            if(from != null && to != null)
                result = (fireTime.after(from) || fireTime.equals(from)) && (fireTime.before(to) || fireTime.equals(to));
            else if(from != null && to == null)
                result = fireTime.after(from) || fireTime.equals(from);
            else if(from == null && to != null)
                result = fireTime.before(to) || fireTime.equals(to);
        }
        return result;
    }

    private boolean equals(Object obj1, Object obj2) {
        if (obj1 == obj2) return true;
        if ((obj1 == null) || (obj2 == null)) return false;
        if ((obj1 instanceof Date) && (obj2 instanceof Date)) {
            Calendar calendar1  = new GregorianCalendar();
            calendar1.setTime((Date)obj1);
            calendar1.set(Calendar.SECOND, 0);
            calendar1.set(Calendar.MILLISECOND, 0);
            Calendar calendar2  = new GregorianCalendar();
            calendar2.setTime((Date)obj2);
            calendar2.set(Calendar.SECOND, 0);
            calendar2.set(Calendar.MILLISECOND, 0);
            return (calendar1.compareTo(calendar2) == 0);
        } else if ((obj1 instanceof String) && (obj2 instanceof String)) {
            return ((String) obj1).equalsIgnoreCase((String)obj2);
        } else return obj1.equals(obj2);
    }

	public DataCacheProvider getDataCacheProvider() {
		return dataCacheProvider;
	}

	public void setDataCacheProvider(DataCacheProvider dataCacheProvider) {
		this.dataCacheProvider = dataCacheProvider;
	}

	public boolean isAutoUpdateJRXMLs() {
		return autoUpdateJRXMLs;
	}

	public void setAutoUpdateJRXMLs(boolean autoUpdateJRXMLs) {
		this.autoUpdateJRXMLs = autoUpdateJRXMLs;
	}

	public RepositoryUnsecure getUnsecureRepository() {
		return unsecureRepository;
	}

	public void setUnsecureRepository(RepositoryUnsecure unsecureRepository) {
		this.unsecureRepository = unsecureRepository;
	}

	public List<ReportExecutionListenerFactory> getReportExecutionListenerFactories() {
		return reportExecutionListenerFactories;
	}

	public void setReportExecutionListenerFactories(
			List<ReportExecutionListenerFactory> reportExecutionListenerFactories) {
		this.reportExecutionListenerFactories = reportExecutionListenerFactories;
	}

	public JasperReportsContext getJasperReportsContext() {
		return jasperReportsContext;
	}

	public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
		this.jasperReportsContext = jasperReportsContext;
	}

    private void incrementStartReportExecutionCount(Boolean asynchronous) {
        if (asynchronous)  {
            this.startAsyncReportExecutionCount.incrementAndGet();
        } else this.startSyncReportExecutionCount.incrementAndGet();
    }

    private void incrementEndReportExecutionCount(Boolean asynchronous) {
        if (asynchronous)  {
            this.endAsyncReportExecutionCount.incrementAndGet();
        } else this.endSyncReportExecutionCount.incrementAndGet();
    }

    private void incrementErrorReportExecutionCount() {
        this.errorReportExecutionCount.incrementAndGet();
    }

	public List<ReportDataParameterContributor> getDataParameterContributors() {
		return dataParameterContributors;
	}

	public void setDataParameterContributors(
			List<ReportDataParameterContributor> dataParameterContributors) {
		this.dataParameterContributors = dataParameterContributors;
	}

    public CustomDomainMetaData getMetaDataFromConnector(CustomReportDataSource customReportDataSource) throws Exception {
        // only supports data adapter CustomReportDataSource object
        CustomReportDataSourceServiceFactory factory =
                (CustomReportDataSourceServiceFactory) getDataSourceServiceFactories().getBean(customReportDataSource.getClass());
        CustomDataSourceDefinition dataSourceDefinition = factory.getDefinition(customReportDataSource);
        CustomDomainMetaDataProvider metaDataProvider = getCustomDomainMetaDataProvider(customReportDataSource, factory, dataSourceDefinition);

        if (metaDataProvider == null) {
            return null;
        }

        // discover metadata from connector
        CustomDomainMetaData metaData;
        if (metaDataProvider instanceof DataAdapterDefinition) {
            metaData = ((DataAdapterDefinition) metaDataProvider).retrieveCustomDomainMetaData(customReportDataSource, repository);
        } else {
            metaData = metaDataProvider.getCustomDomainMetaData(customReportDataSource);
        }
        if (log.isDebugEnabled()) {
            for (JRField field : metaData.getJRFieldList()) {
                log.debug("METADATA from CONNECTOR:  FIELD = " + field.getName() + "  TYPE = " + field.getValueClassName());
            }
        }

        return metaData;
    }



    public boolean isJDBCDiscoverySupported(CustomReportDataSource customReportDataSource) {
        CustomReportDataSourceServiceFactory factory =
                (CustomReportDataSourceServiceFactory) getDataSourceServiceFactories().getBean(customReportDataSource.getClass());
        CustomDataSourceDefinition dataSourceDefinition = factory.getDefinition(customReportDataSource);
        if  (dataSourceDefinition.getCustomFactory() != null) {
            Class<?>[] interfaces = dataSourceDefinition.getCustomFactory().getClass().getInterfaces();
            for (Class interfaceClass : interfaces) {
                if (interfaceClass == CustomJdbcReportDataSourceProvider.class) return true;
            }
        }
        return  false;
    }

    private CustomDomainMetaDataProvider getCustomDomainMetaDataProvider(CustomReportDataSource customReportDataSource,
                                                                         CustomReportDataSourceServiceFactory factory,
                                                                         CustomDataSourceDefinition dataSourceDefinition) {
        CustomDomainMetaDataProvider metaDataProvider = null;
        if (dataSourceDefinition instanceof CustomDomainMetaDataProvider) {
            metaDataProvider = (CustomDomainMetaDataProvider) dataSourceDefinition;
        } else {
            if (isCustomDomainMetadataProvider(customReportDataSource)) {
                ReportDataSourceService customReportDataSourceService = factory.createService(customReportDataSource);
                if (customReportDataSourceService instanceof CustomDomainMetaDataProvider) {
                    metaDataProvider = (CustomDomainMetaDataProvider) customReportDataSourceService;
                }
            }
        }

        return metaDataProvider;
    }

    @Override
    public boolean isCustomDomainMetadataProvider(CustomReportDataSource customReportDataSource) {
        CustomReportDataSourceServiceFactory factory =
                (CustomReportDataSourceServiceFactory) getDataSourceServiceFactories().getBean(customReportDataSource.getClass());
        CustomDataSourceDefinition dataSourceDefinition = factory.getDefinition(customReportDataSource);

        if (dataSourceDefinition instanceof CustomDomainMetaDataProvider) {
            return true;
        }
        
        try {
            if (CustomDomainMetaDataProvider.class.isAssignableFrom(Class.forName(dataSourceDefinition.getServiceClassName()))) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            log.error("Cannot load custom datasource service class: " + dataSourceDefinition.getServiceClassName()
                    + " for datasource: " + customReportDataSource.getURIString(), e);
        }

        return false;
    }
    
    

	public boolean isRecordSizeof() {
		return recordSizeof;
	}

	public void setRecordSizeof(boolean recordSizeof) {
		this.recordSizeof = recordSizeof;
	}

    public List<JRXMLFixer> getJrxmlFixerList() {
        return jrxmlFixerList;
    }

    public void setJrxmlFixerList(List<JRXMLFixer> jrxmlFixerList) {
        this.jrxmlFixerList = jrxmlFixerList;
    }
    
    public boolean isReportThumbnailServiceEnabled() {
        return reportThumbnailServiceEnabled;
    }

    public void setReportThumbnailServiceEnabled(boolean reportThumbnailServiceEnabled) {
        this.reportThumbnailServiceEnabled = reportThumbnailServiceEnabled;
    }

    public AsyncThumbnailCreator getAsyncThumbnailCreator() {
        return asyncThumbnailCreator;
    }

    public void setAsyncThumbnailCreator(AsyncThumbnailCreator asyncThumbnailCreator) {
        this.asyncThumbnailCreator = asyncThumbnailCreator;
    }

	public boolean isReportUnitClassLoadingEnabled() {
		return reportUnitClassLoadingEnabled;
	}

	public void setReportUnitClassLoadingEnabled(boolean reportUnitClassLoadingEnabled) {
		this.reportUnitClassLoadingEnabled = reportUnitClassLoadingEnabled;
	}

	private String getCategory(Throwable e) {
		AtomicReference<String> type = new AtomicReference<>();
		monitoringExceptionMapping.forEach( (key,value) -> {
			for(String exClass:value)	{
				try {
					if(Class.forName(exClass).isInstance(e))	{
						type.set(key);
					}
				} catch (ClassNotFoundException classNotFoundException) {
					log.error("Exception occured while trying to fetch category "+classNotFoundException);
				}

			}
		});
		return java.util.Optional.ofNullable(type.get()).orElse("Other");
	}
	private void addExceptionToAudit(Throwable e) {
    	Throwable suppressedException = e;
    	if (e.getSuppressed() != null && e.getSuppressed().length > 0)
    		suppressedException = e.getSuppressed()[0];
		addPropertyToAuditEvent("exceptionCategory", getCategory(suppressedException));
		addPropertyToAuditEvent("exception", suppressedException);
    }

	private void addQueryDetailsToAudit(ReportQueryDetails queryDetail, ReportUnitRequestBase request) {
		auditContext.doInAuditContext(EXECUTION_QUERY_DETAILS.toString(),
				new AuditContext.AuditContextCallbackWithEvent() {
					public void execute(AuditEvent auditEvent) {
						auditContext.addPropertyToAuditEvent("sqlQuery", queryDetail.getQuery(), auditEvent);
						auditContext.addPropertyToAuditEvent("executionID",request.getId(),auditEvent);
						auditContext.addPropertyToAuditEvent(ReportExecutionJob.REPORT_SCHEDULE_EXECUTION_ID,request.getScheduleExecutionID()
								,auditEvent);
						auditContext.addPropertyToAuditEvent(ReportExecutionJob.TENANT_URI,getTenantURI() ,auditEvent );
						auditContext.addPropertyToAuditEvent("queryExecutionEpochStartTime", queryDetail.getStarTime(), auditEvent);
						auditContext.addPropertyToAuditEvent("queryExecutionEpochEndTime", queryDetail.getEndTime(), auditEvent);
						auditContext.addPropertyToAuditEvent("queryExecutionTime", queryDetail.getExecutionTime(), auditEvent);
					}
				});
	}

	private void addQueryDetailsToAudit(Map<String, Object> parameters, ReportUnitRequestBase request) {
		if (request.getScheduleExecutionID() != null &&
				request.getScheduleResourceType().equals(ReportExecutionJob.RESOURCE_TYPE_REPORT)) {
			List<ReportQueryDetails> queryDetailsList = java.util.Optional.ofNullable(parameters.get(JRParameter.REPORT_CONTEXT)).filter(obj -> obj instanceof SimpleReportContext)
					.map(SimpleReportContext.class::cast)
					.map(reportContext -> reportContext.getParameterValue("reportQueries")).map(List.class::cast).orElse(new ArrayList<>());

			if(queryDetailsList!=null && queryDetailsList.size()>0)
				queryDetailsList.stream().forEach(detail -> {
					createQueryDetailsAudit();
					addQueryDetailsToAudit(detail, request);
					closeQueryDetailsAudit();

				});
		}
	}

	public void closeQueryDetailsAudit() {
		auditContext.doInAuditContext(EXECUTION_QUERY_DETAILS.toString(), new AuditContext.AuditContextCallbackWithEvent() {
			public void execute(AuditEvent auditEvent) {
				auditContext.closeAuditEvent(auditEvent);
			}
		});
	}

	public void createQueryDetailsAudit() {
		auditContext.doInAuditContext(new AuditContext.AuditContextCallback() {
			public void execute() {
				auditContext.createAuditEvent(EXECUTION_QUERY_DETAILS.toString());
			}
		});
	}

	private String getTenantURI() {
		Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();
		if (authenticationToken == null) {
			return null;
		}
		if (authenticationToken.getPrincipal() instanceof TenantQualified) {
			TenantQualified tenantQualified = (TenantQualified) authenticationToken.getPrincipal();
			String tenantID = java.util.Optional.ofNullable(tenantQualified.getTenantId()).orElse("organizations");
			Tenant tenant = getTenantService().getTenant(null,tenantID);
			return tenant.getTenantUri();
		} else {
			return null;
		}
	}


}
