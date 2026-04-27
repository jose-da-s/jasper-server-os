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
package com.jaspersoft.jasperserver.api.common.virtualdatasourcequery.teiid;

import com.jaspersoft.jasperserver.api.JasperServerAPI;
import com.jaspersoft.jasperserver.api.common.virtualdatasourcequery.DataSource;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.dqp.internal.datamgr.ConnectorManager;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.TranslatorException;

import jakarta.resource.cci.ConnectionFactory;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Chan (ichan@jaspersoft.com)
 * @version $Id$
 */
@JasperServerAPI
public interface TeiidDataSource extends DataSource {

    /*
     * returns connector name which use for reference in teiid connector manager repository (sub data source id)
     */
    public String getConnectorName();

    /*
     * returns connector manager which contains the connection and translator information
     */
    public ConnectorManager getConnectorManager() throws Exception;

    /*
     * returns list of modelMetaData (schema) which is going to be available in virtual data source
     */
    public List<ModelMetaData> getModelMetaDataList();

    public Object getConnectionFactory() throws Exception;

    public String getTranslatorName();

    public ExecutionFactory getTranslatorFactory() throws TranslatorException;

    public Object getConnectionFactory(Map map) throws Exception;

    public TranslatorConfiguration getTranslator() throws Exception;

    public String getSchemaText(Map map) throws Exception;

    public String getSchemaSourceType() throws Exception;

}
