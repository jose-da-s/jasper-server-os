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
package com.jaspersoft.jasperserver.api.engine.common.virtualdatasourcequery.teiid;

import bitronix.tm.BitronixTransactionManager;
import com.jaspersoft.jasperserver.api.common.virtualdatasourcequery.teiid.MemoryConfig;
import com.jaspersoft.jasperserver.api.common.virtualdatasourcequery.teiid.ServerConfig;
import com.jaspersoft.jasperserver.api.common.virtualdatasourcequery.teiid.TransactionManagerConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.dqp.internal.datamgr.ConnectorManagerRepository;
import org.teiid.dqp.service.BufferService;
import org.teiid.jdbc.ConnectionImpl;
import org.teiid.runtime.EmbeddedConfiguration;
import org.teiid.runtime.EmbeddedServer;

import javax.naming.InitialContext;
import jakarta.resource.spi.XATerminator;
import jakarta.transaction.TransactionManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings({"nls"})
public class TeiidEmbeddedServer extends EmbeddedServer {

    /**

	public static class DeployVDBParameter {
		public Map<String, Collection<FunctionMethod>> udfs;
		public MetadataRepository<?, ?> metadataRepo;
		public List<VDBImportMetadata> vdbImports;

		public DeployVDBParameter(Map<String, Collection<FunctionMethod>> udfs,
				MetadataRepository<?, ?> metadataRepo) {
			this.udfs = udfs;
			this.metadataRepo = metadataRepo;
		}
	}
    ***/


     private MemoryConfig memoryConfig;
     private static final Log log = LogFactory.getLog(TeiidEmbeddedServer.class);
     private ConcurrentHashMap<String, ConnectionFactoryProvider<?>> connectionFactoryProviders = new ConcurrentHashMap<String, ConnectionFactoryProvider<?>>();


	public TeiidEmbeddedServer(EmbeddedConfiguration config, MemoryConfig memoryConfig, ServerConfig serverConfig, TransactionManagerConfiguration transactionManagerConfiguration) {
        // allow user to plugin their own customizable for teiid embedded server
        if ((serverConfig != null) && (serverConfig.getServerInit() != null)) serverConfig.getServerInit().init(this);
        // transaction manager is required for derived table

        if (config.getTransactionManager() == null) {
            boolean useMockTranscationManager = true;
            String transactionManagerLookup = null;
            if (serverConfig != null) transactionManagerLookup = serverConfig.getTransactionManagerJNDILookup();
            if ((transactionManagerLookup != null) && (!transactionManagerLookup.trim().equals(""))) {
                try {
                    InitialContext ic = new InitialContext();
                    Object utm =  ic.lookup(transactionManagerLookup);
                    if ((utm != null) && (utm instanceof TransactionManager)) {
                        log.debug("Teiid Embedded Server: set transaction manager = " + transactionManagerLookup);
                        config.setTransactionManager((TransactionManager)utm);
                        useMockTranscationManager = false;
                    }
                } catch (Exception ex) {
                    log.debug("Teiid Embedded Server: fail to set transaction manager = " + transactionManagerLookup, ex);
                }
            }

            if (useMockTranscationManager) {
                log.debug("Teiid Embedded Server: use simple transaction manager");
                try {
                    config.setTransactionManager(createTransactionManager(transactionManagerConfiguration));
                } catch (Exception ex) {
                    log.debug("Fail to initialize transaction manager for teiid engine", ex);
            }
            }
            if ((serverConfig != null) && (serverConfig.getXaTerminator() != null)) {
                setXaTerminator(serverConfig.getXaTerminator());
            }
		}

        this.memoryConfig = memoryConfig;
		start(config);
	}

    private TransactionManager createTransactionManager(TransactionManagerConfiguration userConfig) {
        if (userConfig != null) {
            userConfig.setUpConfig();
        }
       return new BitronixTransactionManager();
    }


    public void addConnectionFactoryProvider(String name,
			ConnectionFactoryProvider<?> connectionFactoryProvider) {
		this.connectionFactoryProviders.put(name, connectionFactoryProvider);
	}

    public void setXaTerminator(XATerminator xaTerminator) {
        transactionService.setXaTerminator(xaTerminator);
    }
 /**
    public void setWorkManager(WorkManager workManager) {
        transactionService.setWorkManager(workManager);
    }

    public void setDetectTransactions(boolean detectTransactions) {
        this.detectTransactions = detectTransactions;
    }
 **/
	@Override
    protected BufferService getBufferService() {
        if (memoryConfig != null) {
            if (memoryConfig.getDiskDirectory() != null) {
                bufferService.setDiskDirectory(memoryConfig.getDiskDirectory());
                log.debug("[Teiid Memory Setting] Disk Directory = " + memoryConfig.getDiskDirectory());
            }
            bufferService.setUseDisk(memoryConfig.isUseDisk());
            log.debug("[Teiid Memory Setting] Use Disk = " + memoryConfig.isUseDisk());
            bufferService.setProcessorBatchSize(memoryConfig.getProcessorBatchSize());
            log.debug("[Teiid Memory Setting] Processor Batch Size = " + memoryConfig.getProcessorBatchSize());
        //    bufferService.setConnectorBatchSize(memoryConfig.getConnectorBatchSize());
        //    log.debug("[Teiid Memory Setting] Connector Batch Size = " + memoryConfig.getConnectorBatchSize());
            bufferService.setMaxOpenFiles(memoryConfig.getMaxOpenFiles());
            log.debug("[Teiid Memory Setting] Max Open Files = " + memoryConfig.getMaxOpenFiles());
            bufferService.setMaxFileSize(memoryConfig.getMaxFileSize());
            log.debug("[Teiid Memory Setting] Max File Size = " + memoryConfig.getMaxFileSize());
            bufferService.setMaxProcessingKb(memoryConfig.getMaxProcessingKb());
            log.debug("[Teiid Memory Setting] Max Processing Kb = " + memoryConfig.getMaxProcessingKb());
            bufferService.setMaxReserveKb(memoryConfig.getMaxReserveKb());
            log.debug("[Teiid Memory Setting] Max Reserve Kb = " + memoryConfig.getMaxReserveKb());
            bufferService.setMaxBufferSpace(memoryConfig.getMaxBufferSpace());
            log.debug("[Teiid Memory Setting] Max Buffer Space = " + memoryConfig.getMaxBufferSpace());
            bufferService.setInlineLobs(memoryConfig.isInlineLobs());
            log.debug("[Teiid Memory Setting] Inline Lobs = " + memoryConfig.isInlineLobs());
            bufferService.setMemoryBufferSpace((int)memoryConfig.getMemoryBufferSpace());
            log.debug("[Teiid Memory Setting] Memory Buffer Space = " + memoryConfig.getMemoryBufferSpace());
            bufferService.setMaxStorageObjectSize(memoryConfig.getMaxStorageObjectSize());
            log.debug("[Teiid Memory Setting] Max Storage Object Size = " + memoryConfig.getMaxStorageObjectSize());
            bufferService.setMemoryBufferOffHeap(memoryConfig.isMemoryBufferOffHeap());
            log.debug("[Teiid Memory Setting] Memory Buffer Off Heap = " + memoryConfig.isMemoryBufferOffHeap());
        }
        return super.getBufferService();
    }

	public ConnectorManagerRepository getConnectorManagerRepository() {
		return cmr;
	}

	public void setConnectorManagerRepository(ConnectorManagerRepository cmr) {
		this.cmr = cmr;
	}


	public ConnectionImpl createConnection(String embeddedURL) throws SQLException {
		return getDriver().connect(embeddedURL, null);
	}

    public boolean isVDBExisted(String vdbName) {
        return (this.repo.getVDB(vdbName, 1) != null);
	}

    public String printVDB(String vdbName) {
        return this.repo.getVDB(vdbName, 1).toString();
    }

    public void clearAllVDBs() {
        List<VDBMetaData> vdbMetaDataList = this.repo.getVDBs();
        if (vdbMetaDataList != null) {
            for (VDBMetaData vdbMetaData: vdbMetaDataList) {
                log.debug("Clear ALL VDBs - Undeploy VDB: " + vdbMetaData.getName());
                try {
                    undeployVDB(vdbMetaData.getName());
                } catch (Exception ex) {
                    log.debug("Fail to deploy " + vdbMetaData.getName(), ex);
                };
            }
        }
    }

    public int countVDBs() {
        List<VDBMetaData> vdbMetaDataList = this.repo.getVDBs();
        if (vdbMetaDataList != null) return vdbMetaDataList.size();
        else return 0;
    }


    /***

    public DQPCore getDqp() {
		return dqp;
	}

	public void setUseCallingThread(boolean useCallingThread) {
		this.useCallingThread = useCallingThread;
	}

	public void deployVDB(String vdbName, String vdbPath) throws Exception {
        deployVDB(vdbName, vdbPath, new DeployVDBParameter(null, null));		
	}	

	public void deployVDB(String vdbName, String vdbPath, DeployVDBParameter parameterObject) throws Exception {
		IndexMetadataStore imf = VDBMetadataFactory.loadMetadata(vdbName, new File(vdbPath).toURI().toURL());
        deployVDB(vdbName, imf, parameterObject);		
	}
	
	public void deployVDB(String vdbName, MetadataStore metadata) {
		deployVDB(vdbName, metadata, new DeployVDBParameter(null, null));
	}

	public void deployVDB(String vdbName, MetadataStore metadata, DeployVDBParameter parameterObject) {
		VDBMetaData vdbMetaData = new VDBMetaData();
        vdbMetaData.setName(vdbName);
        vdbMetaData.setStatus(VDB.Status.ACTIVE);
        
        for (Schema schema : metadata.getSchemas().values()) {
        	ModelMetaData model = addModel(vdbMetaData, schema);
        	if (parameterObject.metadataRepo != null) {
        		model.addAttchment(MetadataRepository.class, parameterObject.metadataRepo);
        	}
        }
                        
        try {
        	UDFMetaData udfMetaData = null;
        	if (parameterObject.udfs != null) {
        		udfMetaData = new UDFMetaData();
        		for (Map.Entry<String, Collection<FunctionMethod>> entry : parameterObject.udfs.entrySet()) {
        			udfMetaData.addFunctions(entry.getKey(), entry.getValue());
        		}
        	}
        	
        	if (parameterObject.vdbImports != null) {
        		for (VDBImportMetadata vdbImport : parameterObject.vdbImports) {
					vdbMetaData.getVDBImports().add(vdbImport);
				}
        	}
        	
			this.repo.addVDB(vdbMetaData, metadata, (metadata instanceof IndexMetadataStore)?((IndexMetadataStore)metadata).getEntriesPlusVisibilities():null, udfMetaData, cmr);
			this.repo.finishDeployment(vdbMetaData.getName(), vdbMetaData.getVersion());
			this.repo.getVDB(vdbMetaData.getName(), vdbMetaData.getVersion()).setStatus(VDB.Status.ACTIVE);
		} catch (VirtualDatabaseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void removeVDB(String vdbName) {
		this.repo.removeVDB(vdbName, 1);
	}

	private ModelMetaData addModel(VDBMetaData vdbMetaData, Schema schema) {
		ModelMetaData model = new ModelMetaData();
		model.setModelType(schema.isPhysical()?Type.PHYSICAL:Type.VIRTUAL);
		model.setName(schema.getName());
		vdbMetaData.addModel(model);
		model.addSourceMapping("source", "translator", "jndi:source");
		return model;
	}
	



	public void undeployVDB(String vdbName) {
		this.repo.removeVDB(vdbName, 1);
	}
	
	public ClientServiceRegistryImpl getClientServiceRegistry() {
		return services;
	}
	
	public void setThrowMetadataErrors(boolean throwMetadataErrors) {
		this.throwMetadataErrors = throwMetadataErrors;
	}

    ***/
}
