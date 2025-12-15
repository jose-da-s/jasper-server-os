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

package com.jaspersoft.jasperserver.war.validation;

import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.CustomReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDataSourceDefinition;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDataSourceValidator;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.RepositorySecurityChecker;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.*;
import com.jaspersoft.jasperserver.core.util.validators.ValidationUtil;
import com.jaspersoft.jasperserver.war.common.JasperServerConstImpl;
import com.jaspersoft.jasperserver.war.dto.ReportDataSourceWrapper;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ReportDataSourceValidator implements Validator {

	private JasperServerConstImpl constants=new JasperServerConstImpl();
	private RepositoryService repository;
	private CustomReportDataSourceServiceFactory customDataSourceFactory;
    public static final String DATASOURCE_JDBC = "jdbc";
	public static final String DATASOURCE_JNDI = "jndi";
	public static final String DATASOURCE_BEAN = "bean";
    public static final String DATASOURCE_VIRTUAL = "virtual";
    public static final String DATASOURCE_AWS = "aws";
    public static final String DATASOURCE_MONGO = "MongoDbDataSource";
    public static final String DATASOURCE_CUSTOM = "Custom Data Source";
		private RepositorySecurityChecker securityChecker;

		public void setSecurityChecker(RepositorySecurityChecker securityChecker) {
			this.securityChecker = securityChecker;
		}
    public CustomReportDataSourceServiceFactory getCustomDataSourceFactory() {
		return customDataSourceFactory;
	}

	public void setCustomDataSourceFactory(
			CustomReportDataSourceServiceFactory customDataSourceFactory) {
		this.customDataSourceFactory = customDataSourceFactory;
	}

	public RepositoryService getRepository()
	{
		return repository;
	}

	public void setRepository(RepositoryService repository)
	{
		this.repository = repository;
	}

	public boolean supports(Class clazz) {
		return ReportDataSourceWrapper.class.isAssignableFrom(clazz);
	}

	public void validate(Object obj, Errors errors)
	{
		ReportDataSourceWrapper wrapper = (ReportDataSourceWrapper)obj;
//		validateSource(wrapper, errors);
		validateType(wrapper, errors);
		if (!errors.hasErrors())
		{
			if (DATASOURCE_JDBC.equals(wrapper.getType()))
			{
				jdbcPropsForm(wrapper, errors);
			}
			else if (DATASOURCE_JNDI.equals(wrapper.getType()))
			{
				jndiPropsForm(wrapper, errors);
			}
			else if (DATASOURCE_BEAN.equals(wrapper.getType()))
			{
				beanPropsForm(wrapper, errors);
			}
            else if (DATASOURCE_VIRTUAL.equals(wrapper.getType()))
            {
                virtualPropsForm(wrapper, errors);
            }
            else if (DATASOURCE_AWS.equals(wrapper.getType()))
            {
                awsPropsForm(wrapper, errors);
            }
            else if (DATASOURCE_MONGO.equals(wrapper.getType()))
            {
                customPropsForm(wrapper, errors);
            }
            else if (DATASOURCE_CUSTOM.equals(wrapper.getType()))
            {
                customPropsForm(wrapper, errors);
            }
        }
	}


    public void validateSource(ReportDataSourceWrapper wrapper, Errors errors){
		if (wrapper.getSource() == null)
			errors.rejectValue("source", "ReportDataSourceValidator.error.not.empty");
		if (constants.getFieldChoiceRepo().equals(wrapper.getSource())
				&&( wrapper.getSelectedUri()==null || wrapper.getSelectedUri().trim().length()==0))
			errors.rejectValue("selectedUri", "ReportDataSourceValidator.error.not.empty");
	}

	public void validateType(ReportDataSourceWrapper wrapper, Errors errors){
		if (constants.getFieldChoiceLocal().equals(wrapper.getSource()) && wrapper.getType() == null)
		{
			errors.rejectValue("type", "ReportDataSourceValidator.error.not.empty");
		}
	}

	public void jndiPropsForm(ReportDataSourceWrapper wrapper, Errors errors){
		JndiJdbcReportDataSource jndiSource=(JndiJdbcReportDataSource)wrapper.getReportDataSource();
		if(jndiSource.getJndiName()==null || jndiSource.getJndiName().trim().length()==0) {
			errors.rejectValue("reportDataSource.jndiName", "ReportDataSourceValidator.error.not.empty");
		} else {
			if(!ValidationUtil.regExValidateJndiServiceName(jndiSource.getJndiName())) {
				errors.rejectValue("reportDataSource.jndiName", "ReportDataSourceValidator.error.invalid.chars");
			}
		}
		namingForm(wrapper, errors);
	}

	public void namingForm(ReportDataSourceWrapper wrapper, Errors errors){
		ReportDataSource ds=wrapper.getReportDataSource();
		if(ds.getName()==null || ds.getName().trim().length()==0) {
			errors.rejectValue("reportDataSource.name", "ReportDataSourceValidator.error.not.empty");
		} else {
			if(!ValidationUtil.regExValidateName(ds.getName())) {
				errors.rejectValue("reportDataSource.name", "ReportDataSourceValidator.error.invalid.chars");
			}
		}

		if(ds.getLabel()==null || ds.getLabel().trim().length()==0) {
			errors.rejectValue("reportDataSource.label", "ReportDataSourceValidator.error.not.empty");
		} else {
			if(!ValidationUtil.regExValidateLabel(ds.getLabel())) {
				errors.rejectValue("reportDataSource.label", "ReportDataSourceValidator.error.invalid.chars");
			}
		}

		if(ds.getDescription() != null && ds.getDescription().trim().length() > 250)
			errors.rejectValue("reportDataSource.description", "ReportDataSourceValidator.error.too.long");

        if (ds.getCreationDate() == null) {
            if (repository.repositoryPathExists(null, ds.getURIString())) {
                errors.rejectValue("reportDataSource.name", "ReportDataSourceValidator.error.duplicate");
            }
        }

        if (!repository.repositoryPathExists(null, ds.getParentFolder())) {
            errors.rejectValue("reportDataSource.parentFolder", "ReportDataSourceValidator.error.folder.not.found", new Object[] {ds.getParentFolder()}, null);
        }

	}

	public void jdbcPropsForm(ReportDataSourceWrapper wrapper, Errors errors){
		JdbcReportDataSource ds=(JdbcReportDataSource)wrapper.getReportDataSource();
		if(ds.getDriverClass()==null || ds.getDriverClass().trim().length()==0) {
			errors.rejectValue("reportDataSource.driverClass", "ReportDataSourceValidator.error.not.empty");
		} else {
			ds.setDriverClass(ds.getDriverClass().trim());
			if(!ValidationUtil.regExValidateDbDriver(ds.getDriverClass())) {
				errors.rejectValue("reportDataSource.driverClass", "ReportDataSourceValidator.error.invalid.chars");
			}
		}

		if(ds.getConnectionUrl()==null || ds.getConnectionUrl().trim().length()==0) {
			errors.rejectValue("reportDataSource.connectionUrl", "ReportDataSourceValidator.error.not.empty");
		} else {
			if(!ValidationUtil.regExValidateJdbcURL(ds.getConnectionUrl())) {
				errors.rejectValue("reportDataSource.connectionUrl", "ReportDataSourceValidator.error.invalid.chars");
			}
		}

		if(ds.getUsername()==null || ds.getUsername().trim().length()==0)
			errors.rejectValue("reportDataSource.username", "ReportDataSourceValidator.error.not.empty");

		namingForm(wrapper, errors);
	}

	public void beanPropsForm(ReportDataSourceWrapper wrapper, Errors errors){
		BeanReportDataSource beanSource=(BeanReportDataSource)wrapper.getReportDataSource();
		if(beanSource.getBeanName()==null || beanSource.getBeanName().trim().length()==0) {
			errors.rejectValue("reportDataSource.beanName", "ReportDataSourceValidator.error.not.empty");
		} else {
			// TODO Try and find the bean in the application context
			// If found, see if it has the given method
			// If it has the given method, does that method return the right thing
		}
		namingForm(wrapper, errors);
	}

    public void virtualPropsForm(ReportDataSourceWrapper wrapper, Errors errors){
        namingForm(wrapper, errors);
    }

    private void awsPropsForm(ReportDataSourceWrapper wrapper, Errors errors) {
        namingForm(wrapper, errors);
    }

    public void customPropsForm(ReportDataSourceWrapper wrapper, Errors errors){
		CustomReportDataSource cds = (CustomReportDataSource)wrapper.getReportDataSource();
		// see if there is a validator
		CustomDataSourceDefinition cdef = customDataSourceFactory.getDefinition(cds);
		CustomDataSourceValidator val = cdef.getValidator();
		if (val != null) {
			val.validatePropertyValues(cds, errors);
		}
		namingForm(wrapper, errors);
	}

    public void validateDataSourceExists(ReportDataSourceWrapper wrapper, Errors errors) {
        if (wrapper.getSource() != null && wrapper.getSource().equals(JasperServerConstImpl.getFieldChoiceRepo())
                && (wrapper.getSelectedUri() == null || wrapper.getSelectedUri().trim().length() == 0 ||
                    !repository.resourceExists(null, wrapper.getSelectedUri()) ||
									!this.securityChecker.isResourceReadable(wrapper.getSelectedUri()))) {
            errors.rejectValue("selectedUri", "ReportDataSourceValidator.error.no");
        } else if (wrapper.getSource() != null && wrapper.getSource().equals(JasperServerConstImpl.getFieldChoiceLocal())) {
            if (wrapper.getReportDataSource() == null || wrapper.getType() == null
                    || (JasperServerConstImpl.getJDBCDatasourceType().equals(wrapper.getType())
                        && ((JdbcReportDataSource)wrapper.getReportDataSource()).getDriverClass() == null)) {
                errors.rejectValue("source", "ReportDataSourceValidator.error.no");
            }
        }
    }
}