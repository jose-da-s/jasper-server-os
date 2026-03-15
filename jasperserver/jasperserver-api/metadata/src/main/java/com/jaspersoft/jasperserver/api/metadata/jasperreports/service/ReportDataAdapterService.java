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

package com.jaspersoft.jasperserver.api.metadata.jasperreports.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;

import net.sf.jasperreports.dataadapters.DataAdapter;
import net.sf.jasperreports.dataadapters.DataAdapterService;
import net.sf.jasperreports.dataadapters.DefaultDataAdapterServiceFactory;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ParameterContributorContext;


/**
 * @author ichan
 * this is an implementation of a report data adapter service that can set JRDataSource into report parameter through
 * contributeParameters()
 */
public class ReportDataAdapterService implements ReportDataSourceService {

	private RepositoryService repository;
	private Map propertyMap;
    private DataAdapter dataAdapter;
    private DataAdapterService dataAdapterService;
    private String sourceFileOrganizationUri;
    private static final Log log = LogFactory.getLog(ReportDataAdapterService.class);
     /* (non-Javadoc)
      * @see com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService#closeConnection()
      */
    public void closeConnection() {
        // Do nothing
    }

	/* (non-Javadoc)
	 * @see com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService#setReportParameterValues(java.util.Map)
	 */
	public void setReportParameterValues(Map parameterValues) {
        try {
            contributeParameters(parameterValues, dataAdapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        log.debug(parameterValues);
	}

    public RepositoryService getRepository() {
		return repository;
	}

	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

    public Map getPropertyMap() {
		return propertyMap;
	}

	public void setPropertyMap(Map propertyMap) {
		this.propertyMap = propertyMap;
	}

    public DataAdapter getDataAdapter() {
        return dataAdapter;
    }

    public void setDataAdapter(DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
    }


    /**
     * tenant information is needed for superuser to figure out which organization to use for locating data source file
     **/
    public String getSourceFileOrganizationUri() {
        return sourceFileOrganizationUri;
    }

    public void setSourceFileOrganizationUri(String sourceFileOrganizationUri) {
        this.sourceFileOrganizationUri = sourceFileOrganizationUri;
    }

    public DataAdapter customizeDataAdapter(DataAdapter dataAdapter) {
        // do nothing.  ppl can overwrite this function and customize data adapter before execute the data adapter service
        return dataAdapter;
    }

    //  set JRDataSource into report parameter through contributeParameters()
    public void contributeParameters(Map parameterValues, DataAdapter dataAdapter) throws JRException {
        JasperReportsContext jasperReportsContext = (JasperReportsContext) parameterValues.get("JRQueryDataSet_JasperReportsContext");
        if (dataAdapterService == null) {
            dataAdapterService = getDataAdapterService(jasperReportsContext, customizeDataAdapter(dataAdapter));
        }
        try {
            dataAdapterService.contributeParameters(parameterValues);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DataAdapterService getDataAdapterService() {
        return dataAdapterService;
    }

    public void setDataAdapterService(DataAdapterService dataAdapterService) {
        this.dataAdapterService = dataAdapterService;
    }

    public static DataAdapterService getDataAdapterService(JasperReportsContext jasperReportsContext, DataAdapter dataAdapter) {
        DataAdapterService dataAdapterService = 
            DefaultDataAdapterServiceFactory.getInstance().getDataAdapterService(
                new ParameterContributorContext(jasperReportsContext, null, null), 
                dataAdapter
                );
        return dataAdapterService;
    }

}
