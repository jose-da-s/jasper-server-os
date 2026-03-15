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
 
package example.cdspro;

import net.sf.jasperreports.dataadapters.AbstractClasspathAwareDataAdapterService;
import net.sf.jasperreports.data.jdbc.JdbcDataAdapter;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Create JDBC connection from data adapter properties
 */
public class JDBCQueryDataSourceService extends AbstractClasspathAwareDataAdapterService {

    private static final Log log = LogFactory.getLog(JDBCQueryDataSourceService.class);

    private Connection connection = null;

    public JDBCQueryDataSourceService(JasperReportsContext jasperReportsContext, JdbcDataAdapter jdbcDataAdapter)
    {
        super(new ParameterContributorContext(jasperReportsContext, null, null), jdbcDataAdapter);
    }

    public JDBCQueryDataSourceService(JdbcDataAdapter jdbcDataAdapter)
    {
        this(DefaultJasperReportsContext.getInstance(), jdbcDataAdapter);
    }

    public JdbcDataAdapter getJdbcDataAdapter() {
        return (JdbcDataAdapter) getDataAdapter();
    }

    @Override
    public void contributeParameters(Map<String, Object> parameters) throws JRException
    {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new JRException(e);
        }
        parameters.put(JRParameter.REPORT_CONNECTION, connection);
    }

    public Connection getConnection() throws SQLException{
        JdbcDataAdapter jdbcDataAdapter = getJdbcDataAdapter();
        if (jdbcDataAdapter != null)
        {
            ClassLoader oldThreadClassLoader = Thread.currentThread().getContextClassLoader();

            try
            {
                Thread.currentThread().setContextClassLoader(getClassLoader(oldThreadClassLoader));

                Class<?> clazz = JRClassLoader.loadClassForRealName(jdbcDataAdapter.getDriver());
                Driver driver = (Driver) clazz.newInstance();

                Properties connectProps = new Properties();
                Map<String, String> map = jdbcDataAdapter.getProperties();
                if(map != null)
                    for(String key: map.keySet())
                        connectProps.setProperty(key, map.get(key));

                connectProps.setProperty("user", jdbcDataAdapter.getUsername());
                connectProps.setProperty("password", jdbcDataAdapter.getPassword());

                connection = driver.connect(jdbcDataAdapter.getUrl(), connectProps);
                if(connection == null)
                    throw new SQLException("No suitable driver found for "+ jdbcDataAdapter.getUrl());
            }
            catch (ClassNotFoundException ex){
                throw new JRRuntimeException(ex);
            } catch (InstantiationException e) {
                throw new JRRuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new JRRuntimeException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldThreadClassLoader);
            }
            return connection;
        }
        return null;
    }


    public String getPassword() throws JRException {
        throw new JRException(
                "This service implementation needs the password to be saved in the data adapter.");
    }

    @Override
    public void dispose()
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (Exception ex)
            {
                if (log.isErrorEnabled())
                    log.error("Error while closing the connection.", ex);
            }
        }
    }



}
