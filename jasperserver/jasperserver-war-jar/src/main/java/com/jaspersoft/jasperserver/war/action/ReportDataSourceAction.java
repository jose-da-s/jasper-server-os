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

package com.jaspersoft.jasperserver.war.action;

import com.jaspersoft.jasperserver.api.JSDuplicateResourceException;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.CustomReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.TibcoDriverManagerImpl;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDataSourceDefinition;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.*;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.war.common.DataSourceConfiguration;
import com.jaspersoft.jasperserver.war.common.JasperServerConstImpl;
import com.jaspersoft.jasperserver.war.common.JdkTimeZonesList;
import com.jaspersoft.jasperserver.war.dto.BaseDTO;
import com.jaspersoft.jasperserver.war.dto.ReportDataSourceWrapper;
import com.jaspersoft.jasperserver.war.dto.StringOption;
import com.jaspersoft.jasperserver.war.model.TreeDataProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

public class ReportDataSourceAction extends FormAction implements ApplicationContextAware {
    private static final String ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS = "resourceIdNotSupportedSymbols";

    protected final Log log = LogFactory.getLog(this.getClass());

    public static final String FORM_OBJECT_KEY = "dataResource";
    public static final String DATASOURCEURI_PARAM = "resource";
    public static final String PARENT_FOLDER_ATTR = "parentFolder";
    private static final String DATASOURCE_TREE_DATA_PROVIDER = "dsTreeDataProvider";

    private RepositoryService repository;
    private ResourceFactory dataSourceMappings;
    private JdkTimeZonesList timezones;
    private RepositoryConfiguration repositoryConfiguration;
    private DataSourceConfiguration dataSourceConfiguration;

    private JasperServerConstImpl constants = new JasperServerConstImpl();

    private EngineService engine;
    private String queryLanguageFlowAttribute;

    private MessageSource messageSource;
    private CustomReportDataSourceServiceFactory customDataSourceFactory;

    ApplicationContext ctx;

    private TreeDataProvider typedTreeDataProvider;

    public ReportDataSourceAction() {
        setFormObjectClass(ReportDataSourceWrapper.class); //custom form backing object class
        setFormObjectName(FORM_OBJECT_KEY);
        setFormObjectScope(ScopeType.FLOW);        //this is a multi-page wizard!
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        ctx = arg0;
    }

    public Event initAction(RequestContext context) throws Exception {
        // Look for any supplied ResourceDTO by any parent flows
        ReportDataSourceWrapper formObject = (ReportDataSourceWrapper) getFormObject(context);

        // Check for any request parameters sent along
        // If there is no parent flow, start here **For testing as a stand alone flow

        if (formObject.isSubflowMode() && formObject.getAllDatasources() == null) {

            List allDataSources = null;

            String dataSourceType = getQueryLanguage(context);


            // get a list of all datasources in repo and set in the formObject
            ResourceLookup[] lookups = engine.getDataSources(StaticExecutionContextProvider.getExecutionContext(), dataSourceType);

            if (lookups != null && lookups.length != 0) {
                allDataSources = new ArrayList(lookups.length);

                log("Found ReportDataSource lookups size=" + lookups.length);

                for (ResourceLookup dr : lookups) {
                    allDataSources.add(dr.getURIString());
                }
            }


            formObject.setAllDatasources(allDataSources);

            // TODO get this from main flow
            getAllFolders(formObject);

            String folderURI = (String) context.getFlowScope().get(PARENT_FOLDER_ATTR);
            if (folderURI == null) {
                folderURI = "/";
            }

            if (formObject.getReportDataSource() != null) { // TODO put parent folder in flow scope in main flow
                formObject.getReportDataSource().setParentFolder(
                        (String) context.getFlowScope().get(PARENT_FOLDER_ATTR));
            }
        }

        log("Type of datasource=" + formObject.getType() + " Mode=" + formObject.getMode());
        //	context.getFlowScope().put(FORM_OBJECT_KEY, formObject);
        context.getFlowScope().put("constants", constants);
        context.getExternalContext().getSessionMap().put(DATASOURCE_TREE_DATA_PROVIDER, typedTreeDataProvider);
        context.getFlowScope().put(ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS,
                repositoryConfiguration.getResourceIdNotSupportedSymbols());

        return success();
    }

    private void getAllFolders(ReportDataSourceWrapper wrapper) {
        List allFolders = repository.getAllFolders(null);
        wrapper.setAllFolders(new ArrayList());
        for (int i = 0; i < allFolders.size(); i++) {
            String folderUri = ((Folder) allFolders.get(i)).getURIString();
            wrapper.getAllFolders().add(folderUri);
        }
    }

    public Event handleTypeSelection(RequestContext context) throws Exception {
        ReportDataSourceWrapper formObject = (ReportDataSourceWrapper) getFormObject(context);

        //If the object instance held by ReportDataSource is not the type selected copy common things

        log("Type=" + formObject.getType());

        String dsType = formObject.getType();
        ReportDataSource ds = formObject.getReportDataSource();

        if (ds == null) {
            // We are starting a new data source
            ds = newReportDataSource(dsType);
            formObject.setReportDataSource(ds);
//		} else {
//			formObject.setReportDataSource(newReportDataSource(dsType, ds));
        }
        if (JdbcReportDataSource.class.isAssignableFrom(ds.getClass())) {
            if (dsType.equals(JasperServerConstImpl.getJDBCDatasourceType())) {
                formObject.setReportDataSource(newReportDataSource(dsType, ds));
            } else
                formObject.setReportDataSource(newReportDataSource(dsType));
        } else if (JndiJdbcReportDataSource.class.isAssignableFrom(ds.getClass())) {
            // ReportDataSource holds an instance of JdbcReportDataSource
            if (dsType.equals(JasperServerConstImpl.getJNDIDatasourceType())) {
                formObject.setReportDataSource(newReportDataSource(dsType, ds));
            } else
                formObject.setReportDataSource(newReportDataSource(dsType));
        } else if (VirtualReportDataSource.class.isAssignableFrom(ds.getClass())) {
            // ReportDataSource holds an instance of JdbcReportDataSource
            if (dsType.equals(JasperServerConstImpl.getVirtualDatasourceType())) {
                formObject.setReportDataSource(newReportDataSource(dsType, ds));
            } else
                formObject.setReportDataSource(newReportDataSource(dsType));
        } else {
            // was BeanReportDataSource
            if (dsType.equals(JasperServerConstImpl.getBeanDatasourceType())) {
                formObject.setReportDataSource(newReportDataSource(dsType, ds));
            } else
                formObject.setReportDataSource(newReportDataSource(dsType));
        }
        formObject.getReportDataSource().setParentFolder((String) context.getFlowScope().get(PARENT_FOLDER_ATTR));
        return success();
    }


    public Event prepareChooseType(RequestContext context) throws Exception {
        List dataSourceTypes = dataSourceConfiguration.getDataSourceTypes();
        String queryLanguage = getQueryLanguage(context);
        if (queryLanguage != null) {
            Set supportedTypes = engine.getDataSourceTypes(StaticExecutionContextProvider.getExecutionContext(), queryLanguage);
            for (Iterator iter = dataSourceTypes.iterator(); iter.hasNext(); ) {
                DataSourceConfiguration.DataSourceType type = (DataSourceConfiguration.DataSourceType) iter.next();
                if (!supportedTypes.contains(type.getType())) {
                    iter.remove();
                }
            }
        }

        Map typeMap = new HashMap();
        for (Iterator it = dataSourceTypes.iterator(); it.hasNext(); ) {
            DataSourceConfiguration.DataSourceType type = (DataSourceConfiguration.DataSourceType) it.next();
            typeMap.put(type.getTypeValue(),
                    messageSource.getMessage(type.getLabelMessage(), null, type.getTypeValue(), LocaleContextHolder.getLocale()));
        }
        // add custom types
        Iterator cdsi = customDataSourceFactory.getDefinitions().iterator();
        while (cdsi.hasNext()) {
            CustomDataSourceDefinition cds = (CustomDataSourceDefinition) cdsi.next();
            typeMap.put(cds.getName(), messageSource.getMessage(cds.getLabelName(), null, LocaleContextHolder.getLocale()));
        }

        // NEXT
        // new spring file with cds def
        // see if it shows up on menu (try w/o msg cat first, then add msg cat)
        // figure out how to do param screen
        // then persist params back & forth

        context.getRequestScope().put("allTypes", typeMap);
        return success();
    }


    public Event preparePropsForm(RequestContext context) throws Exception {
        ReportDataSourceWrapper formObject = (ReportDataSourceWrapper) getFormObject(context);
        ReportDataSource ds = formObject.getReportDataSource();

        Locale displayLocale = LocaleContextHolder.getLocale();
        String selectedTimezone = null;
        if (ds instanceof JdbcReportDataSource)
            selectedTimezone = ((JdbcReportDataSource) ds).getTimezone();
        if (ds instanceof JndiJdbcReportDataSource)
            selectedTimezone = ((JndiJdbcReportDataSource) ds).getTimezone();

        List timezoneList = timezones.getTimeZones(displayLocale);
        timezoneList = new ArrayList(timezoneList);
        if (selectedTimezone != null && selectedTimezone.length() > 0) {
            TimeZone zone = TimeZone.getTimeZone(selectedTimezone);
            StringOption option = new StringOption(selectedTimezone, zone.getDisplayName(displayLocale));
            if (!timezoneList.contains(option))
                timezoneList.add(0, option);
        }
        context.getFlowScope().put("timezones", timezoneList);
        context.getFlowScope().put("selectedTimezone", selectedTimezone);

        // init report data source; set props to defaults if not present
        if (ds instanceof CustomReportDataSource) {
            CustomReportDataSource cds = (CustomReportDataSource) ds;
            // look up definition & use it to init defaults & set prop defs
            CustomDataSourceDefinition customDef = customDataSourceFactory.getDefinition(cds);
            customDef.setDefaultValues(cds);
            formObject.setCustomProperties(customDef.getEditablePropertyDefinitions());
            formObject.setCustomDatasourceLabel(customDef.getLabelName());
        }

        return success();
    }

    public Event saveLookup(RequestContext context) throws Exception {
        ReportDataSourceWrapper wrapper = (ReportDataSourceWrapper) getFormObject(context);

        log("user selected a reusable ReportDataSource");

        String selectedUri = wrapper.getSelectedUri();

        if (selectedUri != null && !selectedUri.equals("")) {
            Resource resource = repository.getResource(null, selectedUri);

            wrapper.setReportDataSource((ReportDataSource) resource);
        }

        return success();
    }

    public Event saveNone(RequestContext context) throws Exception {
        ReportDataSourceWrapper wrapper = (ReportDataSourceWrapper) getFormObject(context);

        log("user selected no ReportDataSource");
        wrapper.setReportDataSource(null);

        return success();
    }

    public Event saveDatasource(RequestContext context) throws Exception {
        ReportDataSourceWrapper wrapper = (ReportDataSourceWrapper) getFormObject(context);

        log("Saving the datasource back ");

        if (wrapper.isStandAloneMode()) {

            if (wrapper.getType() != null) {

                ReportDataSource ds = wrapper.getReportDataSource();

                log("Saving DataSource name=" + ds.getName() +
                        " datasource desc=" + ds.getDescription() + " in folder=" + ds.getParentFolder());

                if (ds.getName() != null) {
                    try {
                        repository.saveResource(null, ds);
                    } catch (JSDuplicateResourceException e) {
                        getFormErrors(context).rejectValue("reportDataSource.name", "ReportDataSourceValidator.error.duplicate");
                        return error();
                    }
                }
            }
            return yes();
        }
        return success();
    }

    public Event testJdbcDataSource(RequestContext context) throws Exception {
        ReportDataSourceWrapper wrapper = (ReportDataSourceWrapper) getFormObject(context);
        JdbcReportDataSource ds = (JdbcReportDataSource) wrapper.getReportDataSource();
        Connection conn = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("CreateConnection about to load Driver class '" + ds.getDriverClass() + "' at com.jaspersoft.jasperserver.war.action.ReportDataSourceAction.testJdbcDataSource\n");
            }
            Class.forName(ds.getDriverClass());

            if (log.isDebugEnabled()) {
                log.debug("CreateConnection loaded Driver class '" + ds.getDriverClass() + "' at com.jaspersoft.jasperserver.war.action.ReportDataSourceAction.testJdbcDataSource\n");
            }


            if (log.isDebugEnabled()) {
                log.debug("CreateConnection about to getConnection URL='" + ds.getConnectionUrl() + ", User Name='" + ds.getUsername() + "' Password='" + ds.getPassword() + "', at com.jaspersoft.jasperserver.war.action.ReportDataSourceAction.testJdbcDataSource\n");
            }
            conn = DriverManager.getConnection(ds.getConnectionUrl(), ds.getUsername(), ds.getPassword());


            context.getRequestScope().put("connection.test", Boolean.valueOf(conn != null));
            if (log.isDebugEnabled()) {
                log.debug("CreateConnection successful at com.jaspersoft.jasperserver.war.action.ReportDataSourceAction.testJdbcDataSource\n");
            }

        } catch (Exception e) {
            log.warn("exception testing jdbc data source", e);
            context.getRequestScope().put("connection.test", Boolean.FALSE);
        } finally {
            if (conn != null)
                conn.close();
        }

        return success();
    }

    public Event testJndiDataSource(RequestContext context) throws Exception {
        ReportDataSourceWrapper wrapper = (ReportDataSourceWrapper) getFormObject(context);
        JndiJdbcReportDataSource ds = (JndiJdbcReportDataSource) wrapper.getReportDataSource();
        Connection conn = null;
        try {
            Context ctx = new InitialContext();

            if (log.isDebugEnabled()) {
                log.debug("com.jaspersoft.jasperserver.war.action.ReportDataSourceAction.testJndiDataSource  About to look up DataSource at JNDI location: 'java:comp/env/" + ds.getJndiName() + "' \n");
            }
            DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/" + ds.getJndiName());

            if (log.isDebugEnabled()) {
                log.debug("com.jaspersoft.jasperserver.war.action.ReportDataSourceAction.testJndiDataSource  look up success: DataSource at JNDI location: 'java:comp/env/" + ds.getJndiName() + "' \n" +
                        " About to do:  DataSource.getConnection \n");
            }
            conn = TibcoDriverManagerImpl.getInstance().unlockConnection(dataSource);
            if (log.isDebugEnabled()) {
                log.debug("com.jaspersoft.jasperserver.war.action.ReportDataSourceAction.testJndiDataSource  DataSource.getConnection success. \n");
            }

            context.getRequestScope().put("connection.test", Boolean.valueOf(conn != null));
        } catch (Exception e) {
            context.getRequestScope().put("connection.test", Boolean.FALSE);
        } finally {
            if (conn != null)
                conn.close();
        }

        return success();
    }

    public Event testBeanDataSource(RequestContext context) throws Exception {
        ReportDataSourceWrapper wrapper = (ReportDataSourceWrapper) getFormObject(context);
        BeanReportDataSource ds = (BeanReportDataSource) wrapper.getReportDataSource();

        try {
            Object bean = ctx.getBean(ds.getBeanName());

            if (bean == null) {
                context.getRequestScope().put("connection.test", Boolean.FALSE);
                return success();
            }

            if (ds.getBeanMethod() == null) {
                // The bean had better be a ReportDataSourceService
                context.getRequestScope().put("connection.test", Boolean.valueOf(bean instanceof ReportDataSourceService));
            } else {
                // The method on this bean returns a ReportDataSourceService
                Method serviceMethod;
                try {
                    serviceMethod = bean.getClass().getMethod(ds.getBeanMethod(), null);
                    Object obj = serviceMethod.invoke(bean, null);
                    context.getRequestScope().put("connection.test", Boolean.valueOf(obj != null));
                } catch (SecurityException e) {
                    context.getRequestScope().put("connection.test", Boolean.FALSE);
                }
            }
        } catch (Exception e) {
            context.getRequestScope().put("connection.test", Boolean.FALSE);
        }
        return success();
    }

    public Event validateDataSource(RequestContext context) throws Exception {
        Errors errors = getFormErrors(context);

        ReportDataSourceWrapper wrapper = (ReportDataSourceWrapper) getFormObject(context);

        getValidator().validate(wrapper, errors);

        List fieldErrors = errors.getFieldErrors();
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            FieldError error = (FieldError) fieldErrors.get(0);
            String field = error.getField();

            if (
                    "source".equals(field)
                            || "selectedUri".equals(field)
            ) {
                return result("chooseSource");
            } else if ("type".equals(field)) {
                return result("chooseType");
            } else if (JasperServerConstImpl.getJDBCDatasourceType().equals(wrapper.getType())) {
                return result("jdbcPropsForm");
            } else if (JasperServerConstImpl.getJNDIDatasourceType().equals(wrapper.getType())) {
                return result("jndiPropsForm");
            } else if (JasperServerConstImpl.getVirtualDatasourceType().equals(wrapper.getType())) {
                return result("virtualPropsForm");
            } else if (JasperServerConstImpl.getBeanDatasourceType().equals(wrapper.getType())) {
                return result("beanPropsForm");
            } else {
                return result("customPropsForm");
            }
        }

        return success();
    }

    public Object createFormObject(RequestContext context) {
        ReportDataSourceWrapper formObject = new ReportDataSourceWrapper();
        String resourceUri = context.getRequestParameters().get(DATASOURCEURI_PARAM);

        if (resourceUri != null && resourceUri.trim().length() != 0) {
            Resource resource = (Resource) repository.getResource(null, resourceUri);

            if (resource == null)
                throw new JSException("jsexception.could.not.find.resource.with.uri", new Object[]{resourceUri});

            log("Found resource with uri=" + resourceUri);

            formObject.setMode(BaseDTO.MODE_STAND_ALONE_EDIT);

            ReportDataSource dataSource = (ReportDataSource) resource;

            formObject.setType(getDataSourceMappings().getIdForClass(dataSource.getClass()));
			/*
			if(JdbcReportDataSource.class.isAssignableFrom(dataSource.getClass())){
				formObject.setType(constants.getJDBCDatasourceType());
			}else if(JndiJdbcReportDataSource.class.isAssignableFrom(dataSource.getClass()))
					formObject.setType(constants.getJNDIDatasourceType());
			else
				formObject.setType(constants.getBeanDatasourceType());
			*/
            formObject.setReportDataSource(dataSource);
        }
        if (formObject.getReportDataSource() == null) {
            String parentFolder = (String) context.getFlowScope().get(PARENT_FOLDER_ATTR);
            if (parentFolder == null) {
                parentFolder = (String) context.getRequestParameters().get("ParentFolderUri");
                context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);
            }
            if (parentFolder == null || parentFolder.trim().length() == 0)
                parentFolder = "/";

            log("Datasource flow: Stand alone new mode");

            formObject.setMode(BaseDTO.MODE_STAND_ALONE_NEW);
            //	set default options for datasource type
            formObject.setType(JasperServerConstImpl.getJNDIDatasourceType());
            formObject.setSource(JasperServerConstImpl.getFieldChoiceLocal());
            ReportDataSource jndiSource = (ReportDataSource) newReportDataSource(JasperServerConstImpl.getJNDIDatasourceType());
            jndiSource.setParentFolder(parentFolder);
            jndiSource.setVersion(Resource.VERSION_NEW);
            formObject.setReportDataSource(jndiSource);
        }
        return formObject;
    }

    private ReportDataSource newReportDataSource(String dsType) {
        /**
         * if this is any of the custom data sources, create a custom data source and set it up
         */
        if (customDataSourceFactory != null) {
            CustomDataSourceDefinition cdsd = customDataSourceFactory.getDefinitionByName(dsType);
            if (cdsd != null) {
                return customDataSourceFactory.createDataSource(dsType);
            }
        }
        return (ReportDataSource) getDataSourceMappings().newResource(null, dsType);
    }

    private ReportDataSource newReportDataSource(String dsType, ReportDataSource oldDS) {
        ReportDataSource newDS = newReportDataSource(dsType);
        newDS.setParentFolder(oldDS.getParentFolder());
        newDS.setName(oldDS.getName());
        newDS.setLabel(oldDS.getLabel());
        newDS.setDescription(oldDS.getDescription());
        newDS.setVersion(oldDS.getVersion());

        if (oldDS instanceof JdbcReportDataSource) {
            JdbcReportDataSource oldJdbc = (JdbcReportDataSource) oldDS;
            JdbcReportDataSource newJdbc = (JdbcReportDataSource) newDS;
            newJdbc.setDriverClass(oldJdbc.getDriverClass());
            newJdbc.setConnectionUrl(oldJdbc.getConnectionUrl());
            newJdbc.setPassword(oldJdbc.getPassword());
            newJdbc.setTimezone(oldJdbc.getTimezone());
            newJdbc.setUsername(oldJdbc.getUsername());
        } else if (oldDS instanceof JndiJdbcReportDataSource) {
            JndiJdbcReportDataSource oldJndi = (JndiJdbcReportDataSource) oldDS;
            JndiJdbcReportDataSource newJndi = (JndiJdbcReportDataSource) newDS;
            newJndi.setJndiName(oldJndi.getJndiName());
            newJndi.setTimezone(oldJndi.getTimezone());
        } else if (oldDS instanceof BeanReportDataSource) {
            BeanReportDataSource oldBean = (BeanReportDataSource) oldDS;
            BeanReportDataSource newBean = (BeanReportDataSource) newDS;
            newBean.setBeanMethod(oldBean.getBeanMethod());
            newBean.setBeanName(oldBean.getBeanName());
        }
        return newDS;
    }

    public JdkTimeZonesList getTimezones() {
        return timezones;
    }

    public void setTimezones(JdkTimeZonesList timezones) {
        this.timezones = timezones;
    }

    public RepositoryService getRepository() {
        return repository;
    }

    public void setRepository(RepositoryService repository) {
        this.repository = repository;
    }

    public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
        this.repositoryConfiguration = repositoryConfiguration;
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public void setDataSourceConfiguration(DataSourceConfiguration dataSourceConfiguration) {
        this.dataSourceConfiguration = dataSourceConfiguration;
    }

    public DataSourceConfiguration getDataSourceConfiguration() {
        return dataSourceConfiguration;
    }

    /**
     * @return Returns the dataSourceMappings.
     */
    public ResourceFactory getDataSourceMappings() {
        return dataSourceMappings;
    }

    /**
     * @param dataSourceMappings The dataSourceMappings to set.
     */
    public void setDataSourceMappings(ResourceFactory dataSourceMappings) {
        this.dataSourceMappings = dataSourceMappings;
    }

    /**
     * Helper method to facilitate easy logging level change
     *
     * @param text
     */
    private void log(String text) {
        log.debug(text);
    }

    public static String getDATASOURCEURI_PARAM() {
        return DATASOURCEURI_PARAM;
    }

    public static String getFORM_OBJECT_KEY() {
        return FORM_OBJECT_KEY;
    }

    protected void initBinder(RequestContext requestContext, DataBinder binder) {
        super.initBinder(requestContext, binder);

        binder.registerCustomEditor(String.class, "reportDataSource.beanMethod", new StringTrimmerEditor(true));
        binder.registerCustomEditor(String.class, "reportDataSource.timezone", new StringTrimmerEditor(true));
    }

    protected String getQueryLanguage(RequestContext context) {
        return context.getFlowScope().getString(getQueryLanguageFlowAttribute());
    }

    public String getQueryLanguageFlowAttribute() {
        return queryLanguageFlowAttribute;
    }

    public void setQueryLanguageFlowAttribute(String queryLanguageFlowAttribute) {
        this.queryLanguageFlowAttribute = queryLanguageFlowAttribute;
    }

    public EngineService getEngine() {
        return engine;
    }

    public void setEngine(EngineService engine) {
        this.engine = engine;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public CustomReportDataSourceServiceFactory getCustomDataSourceFactory() {
        return customDataSourceFactory;
    }

    public void setCustomDataSourceFactory(CustomReportDataSourceServiceFactory customDataSourceFactory) {
        this.customDataSourceFactory = customDataSourceFactory;
    }

    public void setDataSourceTreeDataProvider(TreeDataProvider typedTreeDataProvider) {
        this.typedTreeDataProvider = typedTreeDataProvider;
    }

}
