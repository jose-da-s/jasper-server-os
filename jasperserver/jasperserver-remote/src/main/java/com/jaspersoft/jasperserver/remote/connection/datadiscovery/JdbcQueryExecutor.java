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
package com.jaspersoft.jasperserver.remote.connection.datadiscovery;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.engine.common.domain.JdbcMetaConfiguration;
import com.jaspersoft.jasperserver.dto.common.JavaAliasConverter;
import com.jaspersoft.jasperserver.dto.connection.datadiscovery.FlatDataSet;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceGroupElement;
import com.jaspersoft.jasperserver.dto.resources.domain.ResourceSingleElement;
import com.jaspersoft.jasperserver.dto.resources.domain.SchemaElement;
import com.jaspersoft.jasperserver.remote.exception.OperationCancelledException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import static com.jaspersoft.jasperserver.remote.common.ThreadInterruptionHelper.checkInterrupted;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Component
public class JdbcQueryExecutor implements QueryExecutor<String, Connection, FlatDataSet, ResourceGroupElement> {
    protected final Log log = LogFactory.getLog(getClass());
    private ExecutorService executor = Executors.newCachedThreadPool();
    protected SQLQueryValidator validator;

    @Resource(name="jdbcMetaConfiguration")
    private JdbcMetaConfiguration jdbcMetaConfiguration;

    @Override
    public FlatDataSet executeQuery(String query, Connection connection) {
        return executeQuery(query, connection, false);
    }

    @Override
    public ResourceGroupElement executeQueryForMetadata(String query, Connection connection) {
        return executeQuery(query, connection, true).getMetadata();
    }

    protected FlatDataSet executeQuery(final String query, Connection connection, boolean skipData) {
        validator.validate(query, connection);
        final List<String[]> result = new ArrayList<String[]>();

        final ArrayList<SchemaElement> columnElements = new ArrayList<SchemaElement>();
        final ResourceGroupElement metadata = new ResourceGroupElement().setKind("resultSet").setElements(columnElements);
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = connection.createStatement();
            final Statement statementClosure = stmt;
            log.debug("JDBC Query Executor SQL[" + query + "] skipData = " + skipData);
            if (skipData) stmt.setMaxRows(1);
            final Future<ResultSet> future = executor.submit(new Callable<ResultSet>() {
                @Override
                public ResultSet call() throws Exception {
                    return statementClosure.executeQuery(query);
                }
            });
            try {
                resultSet = future.get();
            } catch (CancellationException e){
                if(log.isDebugEnabled()) log.debug("Query execution cancelled.");
                throw new OperationCancelledException(e);
            } catch (ExecutionException e){
                if(log.isDebugEnabled()) log.debug("Query execution failed.");
                final Throwable cause = e.getCause();
                if(cause instanceof RuntimeException){
                    throw (RuntimeException)cause;
                } else {
                    throw new JSExceptionWrapper((Exception) cause);
                }
            } catch (Exception e) {
                if(log.isDebugEnabled()) log.debug("Query execution failed.");
                throw new JSExceptionWrapper(e);
            }
            final Set<String> columns = new HashSet<String>();
            final ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                final String javaType = jdbcMetaConfiguration.getJavaType(metaData.getColumnTypeName(i), metaData.getColumnType(i));
                final String columnName = metaData.getColumnLabel(i);
                if (columns.contains(columnName)) {
                    throw new QueryExecutionException("The query returned more than one field with the same name: "
                            + columnName
                            + " To use the fields, rename them with unique aliases. For example, you could " +
                            "rename a.account_id as my_account_a_id. ", query, null);
                }
                final String type = javaType != null ? JavaAliasConverter.toAlias(javaType) : null;
                columnElements.add(new ResourceSingleElement().setName(columnName).setType(type));
                columns.add(columnName);
            }
            if(!skipData) {
                while (resultSet.next()) {
                    checkInterrupted();
                    final int size = columnElements.size();
                    String[] row = new String[size];
                    for (int i = 0; i < size; i++) {
                        SchemaElement column = columnElements.get(i);
                        row[i] = resultSet.getString(column.getName());
                    }
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            throw new QueryExecutionException(query, e);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new QueryExecutionException("Query parsing exception", query, null);
        } finally {
           if (resultSet != null) {
               try {
                   resultSet.close();
               } catch (SQLException e) {
                   log.error(e);
               }
           }
            if(stmt != null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
        return new FlatDataSet().setData(result).setMetadata(metadata);
    }

    public void setValidator(SQLQueryValidator validator) {
        this.validator = validator;
    }
}

