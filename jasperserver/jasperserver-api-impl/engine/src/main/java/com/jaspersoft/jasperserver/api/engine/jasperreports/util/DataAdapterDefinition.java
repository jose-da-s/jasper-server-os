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

package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EngineServiceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FileResourceImpl;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.RepositoryContextManager;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.connection.datadiscovery.CustomDataSourceMetadataBuilder;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.repo.RepositoryURLHandlerFactory;
import com.jaspersoft.jasperserver.api.metadata.common.service.JsonMarshaller;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaData;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomDomainMetaDataProvider;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataAdapterService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Tenant;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import net.sf.jasperreports.dataadapters.DataAdapter;
import net.sf.jasperreports.dataadapters.DataAdapterService;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JRDesignDataset;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRQueryExecuterUtils;
import net.sf.jasperreports.engine.util.LocalJasperReportsContext;
import net.sf.jasperreports.dataadapters.repo.DataAdapterResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.util.*;


/**
 * @author ichan
 * This data adapter definition takes any data adapter class and turns it into custom data source definition
 * This class is meant to be instantiated as a spring bean that registers a custom data source with the system.
 */
public abstract class DataAdapterDefinition extends CustomDataSourceDefinition implements CustomDomainMetaDataProvider {

    String dataAdapterClassName = null;
    String dataAdapterUri = null;
    DataAdapter dataAdapter = null;
    boolean enableMetadataCache = false;
    PropertyDescriptor[] propertyDescriptors = null;
    public static String DEFAULT_DATA_ADAPTER_DS = "dataAdapterDS";
    private Set<String> additionalPropertySet = new HashSet<String>();
    private Set<String> hiddenPropertySet = new HashSet<String>();         // a set of hidden properties
    private Map<String, String> propertyDefaultValueMap = new HashMap<String, String>();    // default value for properties
    public static String DATA_ADAPTER_SERVICE_CLASS = "com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataAdapterService";  // report data adapter service
    // repo custom data source property map value size limit is defined in RepoCustomDataSource.hbm.xml
    private static int REPOCUSTOMDATASOURCEPROPSIZELIMIT = 1000;
    @Resource(name = "concreteTenantService")
    private TenantService tenantService;
    @Autowired(required = false)
    @Qualifier("concreteRepositoryContextManager")
    private RepositoryContextManager repositoryContextManager;
    @Resource(name = "${bean.jasperReportsContext}")
    private JasperReportsContext jasperReportsContext;
    @Resource
    private JsonMarshaller jsonMarshaller;

    private EngineServiceImpl engineService;

    protected static final Log log = LogFactory.getLog(DataAdapterDefinition.class);


    public DataAdapterDefinition() {
        // set default data service for data adapter
        setServiceClassName(DATA_ADAPTER_SERVICE_CLASS);
        Set<String> hiddenPropertySet = getHiddenPropertySet();
    }

    public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
        this.jasperReportsContext = jasperReportsContext;
    }

    protected JasperReportsContext getJasperReportsContext() {
        return jasperReportsContext;
    }

    // set custom data source property definitions from data adapter custom report data source object
    protected void setPropertyDefinitions(CustomReportDataSource customReportDataSource) throws Exception {
        String dataAdapterClassName = (String) customReportDataSource.getPropertyMap().get("dataAdapterClassName");
        if (dataAdapterClassName == null) throw new Exception("Cannot create data adapter definition from this custom data source");
        if (customReportDataSource.getDataSourceName() != null) setName(customReportDataSource.getDataSourceName());
        else setName(DATA_ADAPTER_SERVICE_CLASS);
        setDataAdapterClassName(dataAdapterClassName);
    }

    public String getDataAdapterClassName() {
        return dataAdapterClassName;
    }

    // set custom data source property definitions from data adapter class
    public void setDataAdapterClassName(String dataAdapterClassName) {
        this.dataAdapterClassName = dataAdapterClassName;
        if ((dataAdapterClassName != null) && !dataAdapterClassName.equals("")) {
            try {
                log.debug("DATA ADAPTER CLASS NAME = " + dataAdapterClassName);
                setPropertyDefinitions((DataAdapter) getDataAdapterInstance());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private DataAdapter getDataAdapterInstance() throws Exception {
            Class dataAdapterClass = Class.forName(dataAdapterClassName);
            return (DataAdapter) dataAdapterClass.newInstance();
    }

    public String getDataAdapterUri() {
        return dataAdapterClassName;
    }

    // set custom data source property definitions from data adapter uri
    public void setDataAdapterUri(String dataAdapterUri) {
        this.dataAdapterUri = dataAdapterUri;
        if ((dataAdapterUri != null) && !dataAdapterUri.equals("")) {
            try {
                log.debug("DATA ADAPTER URI = " + dataAdapterUri);
                DataAdapterResource dataAdapterResource = net.sf.jasperreports.repo.RepositoryUtil.getInstance(getLocalJasperReportsContext()).getResourceFromLocation(dataAdapterUri, DataAdapterResource.class);
                DataAdapter dataAdapter = dataAdapterResource.getDataAdapter();
                setPropertyDefinitions(dataAdapter);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // set custom data source property definitions from data adapter object
    protected void setPropertyDefinitions(DataAdapter dataAdapter) throws Exception {
        // setup data adapter
        this.dataAdapter = dataAdapter;
        BeanWrapperImpl bw = new BeanWrapperImpl(dataAdapter);
        propertyDescriptors = bw.getPropertyDescriptors();
        List<Map<String, Object>> propertyDefinitions = new ArrayList<Map<String, Object>>();
        // read data adapter property and translate it to custom report data source property definition
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals("class")) continue;
            Map<String, Object> propMap = new HashMap<String, Object>();
            propMap.put(CustomDataSourceDefinition.PARAM_NAME, propertyDescriptor.getName());
        //    propMap.put(CustomDataSourceDefinition.PARAM_LABEL, propertyDescriptor.getDisplayName());
            String defaultValue = DataAdapterDefinitionUtil.toString(bw.getPropertyValue(propertyDescriptor.getName()), propertyDescriptor.getPropertyType());
            String propertyDefaultValue = propertyDefaultValueMap.get(propertyDescriptor.getName());
            if (propertyDefaultValue != null) defaultValue = propertyDefaultValue;
            if (!defaultValue.equals("")) propMap.put(CustomDataSourceDefinition.PARAM_DEFAULT, defaultValue);
            if (hiddenPropertySet.contains(propertyDescriptor.getName())) propMap.put(CustomDataSourceDefinition.PARAM_HIDDEN, "true");
            log.debug(propMap + " TYPE = " + propertyDescriptor.getPropertyType());
            propertyDefinitions.add(propMap);

        }
        for (String propName : additionalPropertySet) {
            Map<String, Object> propMap = new HashMap<String, Object>();
            propMap.put(CustomDataSourceDefinition.PARAM_NAME, propName);
             String defaultValue = propertyDefaultValueMap.get(propName);
            if (defaultValue != null && !defaultValue.equals("")) propMap.put(CustomDataSourceDefinition.PARAM_DEFAULT, defaultValue);
            if (hiddenPropertySet.contains(propName)) propMap.put(CustomDataSourceDefinition.PARAM_HIDDEN, "true");
            propertyDefinitions.add(propMap);
        }


        Map<String, Object> propMap = new HashMap<String, Object>();
        propMap.put(CustomDataSourceDefinition.PARAM_NAME, "dataAdapterClassName");
        propMap.put(CustomDataSourceDefinition.PARAM_DEFAULT, dataAdapter.getClass().getName());
        log.debug("SET DATA ADAPTER CLASS NAME = " + dataAdapter.getClass().getName());
        propMap.put(CustomDataSourceDefinition.PARAM_HIDDEN, "true");
        propertyDefinitions.add(propMap);
        setPropertyDefinitions(propertyDefinitions);
    }

    @Override
    public void setDataSourceServiceProperties(
            CustomReportDataSource customDataSource,
            ReportDataSourceService service) {
        Map<String, Object> propMap = new HashMap<String, Object>();
        try {
            propMap = getDataSourceServicePropertyMap(customDataSource, propMap);
            // customize the value of data source definition property if needed
            propMap = customizePropertyValueMap(customDataSource, propMap);
            dataAdapter = setupDataAdapter(dataAdapter, propMap);
            if (service != null) {
                // set data adapter to the service
                ((ReportDataAdapterService) service).setDataAdapter(dataAdapter);
                ((ReportDataAdapterService) service).setDataAdapterService(getDataAdapterService(getLocalJasperReportsContext(), dataAdapter));
                // repository URI of data file is tenant dependent. E.g. for root organization is /organizations/organization_1/someFolder/dataFile.csv
                // and for jasperadmin on organization_1 it's /someFolder/dataFile.csv
                // let's find user's tenant to allow service to find a data file correctly
                String tenantUri = DataAdapterDefinitionUtil.getDataSourceUri(customDataSource, tenantService);
                SecurityContext securityContext = SecurityContextHolder.getContext();
                Authentication authentication = securityContext == null ? null : securityContext.getAuthentication();
                if(authentication != null) {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof MetadataUserDetails) {
                        MetadataUserDetails user = (MetadataUserDetails) principal;
                        Tenant tenant = null;
                        String tenantId = user.getTenantId();
                        if (tenantId != null) {
                            tenant = tenantService.getTenant(null, tenantId);
                        }
                        // if user has tenant, then take it's URI if no, then it's root organization's user
                        // If root organization user, then let's build dummy URI of any root folder. Folder name
                        // is taken away and just "/" is used internally
                        tenantUri = tenant != null ? tenant.getTenantUri() : "/dummyFolerNameToIgnore";
                    }
                }
                ((ReportDataAdapterService) service).setSourceFileOrganizationUri(tenantUri);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	public Map<String, Object> getDataSourceServicePropertyMap(
			CustomReportDataSource customDataSource, Map<String, Object> propMap) throws Exception {
            // use spring for introspection help
            // use "propertyMap" for passing params if you want that...
            // set params
            for (Map<String, ?> pd : getPropertyDefinitions()) {
                String name = (String) pd.get(CustomDataSourceDefinition.PARAM_NAME);
                Object deflt = pd.get(CustomDataSourceDefinition.PARAM_DEFAULT);
                Object value = customDataSource.getPropertyMap().get(name);
                if (value == null && deflt != null) value = deflt;
                // set prop if it's writable
                if ((value != null) && !value.equals("")) {
                    propMap.put(name, DataAdapterDefinitionUtil.toObject((String) value, DataAdapterDefinitionUtil.findType(name, propertyDescriptors)));
                }
            }
            // pass all prop values as map if available
            return propMap;
	}

    // set the given properties to data adapter
    public DataAdapter setupDataAdapter(DataAdapter dataAdapter, Map<String, Object> propMap) throws Exception {
        BeanWrapperImpl bw = new BeanWrapperImpl(dataAdapter);
        for (Map.Entry<String, Object> entry : propMap.entrySet()) {
            if (bw.isWritableProperty(entry.getKey())) {
                log.debug("SET DataAdapter Property: " + entry.getKey() + ", " + entry.getValue());
                bw.setPropertyValue(entry.getKey(), entry.getValue());
            }
        }
        return dataAdapter;
    }

    public abstract Map<String, Object>  customizePropertyValueMap(CustomReportDataSource customReportDataSource, Map<String, Object>  propertyValueMap);


    public DataAdapter getDataAdapter() {
        return dataAdapter;
    }

    public Set<String> getHiddenPropertySet() {
        return hiddenPropertySet;
    }

    public void setHiddenPropertySet(Set<String> hiddenPropertySet) {
        this.hiddenPropertySet = hiddenPropertySet;
    }

    public Map<String, String> getPropertyDefaultValueMap() {
        return propertyDefaultValueMap;
    }

    public void setPropertyDefaultValueMap(Map<String, String> propertyDefaultValueMap) {
        this.propertyDefaultValueMap = propertyDefaultValueMap;
    }

    public Set<String> getAdditionalPropertySet() {
        return additionalPropertySet;
    }

    public void setAdditionalPropertySet(Set<String> additionalPropertySet) {
        this.additionalPropertySet = additionalPropertySet;
    }

    @Override
    public boolean isHiddenProperty(Map<String, ?> dsProperty) {
        String name = (String) dsProperty.get(CustomDataSourceDefinition.PARAM_NAME);
        String hidden = (String) dsProperty.get(CustomDataSourceDefinition.PARAM_HIDDEN);
        // skip hidden ones, but not data adapter class name
        return (Boolean.parseBoolean(hidden) && (!name.equals("dataAdapterClassName")));
    }

    // return query executer
    public QueryExecuterFactory getQueryExecuterFactory() throws JRException {
        if (getQueryExecuterMap() != null) {
            for (Map.Entry<String, String> queryExecuterEntry : getQueryExecuterMap().entrySet()) {
                return JRQueryExecuterUtils.getInstance(getLocalJasperReportsContext()).getExecuterFactory(queryExecuterEntry.getKey());
            }
        }
        return null;
    }

    /*
     * Getting JRDataSource from data adapter
     */
    public JRDataSource getJRDataSource(DataAdapter dataAdapter) throws Exception {
        try {
            // setup thread repository context for looking up data source file from repo
            repositoryContextManager.setRepositoryContext(ExecutionContextImpl.getRuntimeExecutionContext(), "/ignore", null);
            DataAdapterService dataAdapterService = getDataAdapterService(getLocalJasperReportsContext(), dataAdapter);
            Map<String,Object> parameterValues = new HashMap<String, Object>();
            dataAdapterService.contributeParameters(parameterValues);
            JRDataSource dataSource = (JRDataSource) parameterValues.get(JRParameter.REPORT_DATA_SOURCE);
            if (dataSource  == null) {
                QueryExecuterFactory queryExecuterFactory = getQueryExecuterFactory();
                // convert your param map to fill params...
                Map<String, ? extends JRValueParameter>  fillParams = DataAdapterDefinitionUtil.convertToFillParameters(parameterValues, queryExecuterFactory.getBuiltinParameters());
                // and pass it all to the factory to get the queryExecuter
                JRQueryExecuter queryExecuter = queryExecuterFactory.createQueryExecuter(getLocalJasperReportsContext(), new JRDesignDataset(false), fillParams);
                // ok, now we're in the innermost Russian doll...let's get our JRDatasource!
                dataSource = queryExecuter.createDatasource();
            }
            return dataSource;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public DataAdapterService getDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter) {
            return ReportDataAdapterService.getDataAdapterService(getLocalJasperReportsContext(), dataAdapter);
    }

    public CustomDomainMetaData retrieveCustomDomainMetaData(CustomReportDataSource customReportDataSource, RepositoryService repository) throws Exception {
        // check whether it is ok to read metadata from data source object property map
        if (isEnableMetadataCache()) {
            boolean containsMetaData = CustomDataSourceMetadataBuilder.containsMetadata(customReportDataSource);
            if (containsMetaData) {
                // if metadata is already existed inside data source, load metadata directly from data source
                return loadCustomDomainMetaDataToDataSource(customReportDataSource, getQueryLanguage(), repository);
            }
            CustomDomainMetaData metaData = getCustomDomainMetaData(customReportDataSource);
            // set metadata properties inside data source object
            saveCustomDomainMetaDataToDataSource(metaData, customReportDataSource);
            // over-write data source object in repo with metadata properties
            if (!containsMetaData && CustomDataSourceMetadataBuilder.containsMetadata(customReportDataSource)) {
                String isWrappedInVDS = (String) customReportDataSource.getPropertyMap().get(CustomJDBCReportDataSourceServiceFactory.IS_WRAPPED_DATASOURCE);
                // remove temporary property, for example: IS_WRAPPED_DATASOURCE, before saving the resource
                if (isWrappedInVDS != null) customReportDataSource.getPropertyMap().remove(CustomJDBCReportDataSourceServiceFactory.IS_WRAPPED_DATASOURCE);
                try {
                    repository.saveResource(null, customReportDataSource);
                } catch (Exception ex) {
                    log.error("Fail to cache metadata info in custom data source: " + ex.getMessage(), ex);
                }
                customReportDataSource.getPropertyMap().put(CustomJDBCReportDataSourceServiceFactory.IS_WRAPPED_DATASOURCE, isWrappedInVDS);
            }
            return metaData;
        } else {
            // retrieving metadata layer from fresh
            return getCustomDomainMetaData(customReportDataSource);
        }
    }


    /*
     * This function is used for retrieving the metadata layer of the data connector in form of TableSourceMetadata
     * TableSourceMetadata contains information JRFields, query, query language and field name mapping (actual JRField name, name used in domain)
     *
     */
    public abstract CustomDomainMetaData getCustomDomainMetaData(CustomReportDataSource customReportDataSource) throws Exception;

    // return JRDataSource from CustomReportDataSource.  User can iterate the data from JRDataSource for previewing
    public JRDataSource getJRDataSource(CustomReportDataSource customDataSource) throws Exception {
        // METADATA DISCOVERY
        Map<String, Object> propertyValueMap = new HashMap<String, Object>();
        propertyValueMap = getDataSourceServicePropertyMap(customDataSource, propertyValueMap);
        propertyValueMap = customizePropertyValueMap(customDataSource, propertyValueMap);

        // GET CSV DATA ADAPTER from Custom Report Data Source
        // map custom report data source properties to data adapter
        DataAdapter dataAdapter = getDataAdapterInstance();
        dataAdapter = setupDataAdapter(dataAdapter, propertyValueMap);
        return getJRDataSource(dataAdapter);
    }

    // set metadata properties inside data source object
    private void saveCustomDomainMetaDataToDataSource(CustomDomainMetaData customDomainMetaData, CustomReportDataSource customDataSource) {
        if (!isEnableMetadataCache()) return;

        String jsonContent = CustomDataSourceMetadataBuilder.metadataToJsonContent(customDomainMetaData, null, jsonMarshaller);

        log.debug("jsonContent = " + jsonContent);

        FileResourceImpl fileResourceImpl = new FileResourceImpl();
        fileResourceImpl.setFileType(".json");
        fileResourceImpl.setName(customDataSource.getName() + "_METADATA");
        fileResourceImpl.setLabel(customDataSource.getName() + "_METADATA");
        fileResourceImpl.setParentFolder(customDataSource.getParentFolder());
        fileResourceImpl.setData(jsonContent.getBytes());
        ResourceReference resourceReference = new ResourceReference(fileResourceImpl);
        HashMap<String, ResourceReference> resources = new HashMap<String, ResourceReference>();
        resources.put(CustomDataSourceMetadataBuilder.METADATA_FILE_RESOURCE_ALIAS, resourceReference);
        customDataSource.setResources(resources);
    }

    // if metadata is already existed inside data source, load metadata directly from data source
    private CustomDomainMetaData loadCustomDomainMetaDataToDataSource(CustomReportDataSource customDataSource, String queryLanguage, RepositoryService repository) {
        String jsonContent = CustomDataSourceMetadataBuilder.getJsonMetadataContentFromDataSource(customDataSource, repository);
        return CustomDataSourceMetadataBuilder.jsonContentToMetadata(jsonContent, queryLanguage, null, jsonMarshaller);
    }

    // check whether it is ok to read metadata from data source object property map
    public boolean isEnableMetadataCache() {
        return enableMetadataCache;
    }

    public void setEnableMetadataCache(boolean enableMetadataCache) {
        this.enableMetadataCache = enableMetadataCache;
    }

    protected LocalJasperReportsContext getLocalJasperReportsContext() {
        LocalJasperReportsContext localJasperReportsContext = new LocalJasperReportsContext(jasperReportsContext);
        localJasperReportsContext.setURLStreamHandlerFactory(RepositoryURLHandlerFactory.getInstance());
        return localJasperReportsContext;
    }

    protected String getQueryLanguage() throws JRException {
        if (getQueryExecuterMap() != null) {
            for (Map.Entry<String, String> queryExecuterEntry : getQueryExecuterMap().entrySet()) {
                return queryExecuterEntry.getKey();
            }
        }
        throw new JRException("Please define query language or query excuter map!");
    }
}
