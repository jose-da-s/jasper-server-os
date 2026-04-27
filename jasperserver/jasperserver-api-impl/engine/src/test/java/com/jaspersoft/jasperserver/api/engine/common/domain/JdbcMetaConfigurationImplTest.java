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
package com.jaspersoft.jasperserver.api.engine.common.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import jakarta.annotation.Resource;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"default","engine","jrs"})
@ContextConfiguration(locations = {
        "classpath:applicationContext-jdbc-metadata.xml"
})
public class JdbcMetaConfigurationImplTest {
    @Resource(name = "jdbcMetaConfiguration")
    JdbcMetaConfiguration jdbcMetaConfiguration;

    @Test
    public void getJdbcTypeName_withGenericSqlType(){
        String jdbcType = jdbcMetaConfiguration.getJdbcTypeName(4);
        assertEquals("INTEGER", jdbcType);
    }

    @Test
    public void getJdbcTypeName_withTimestampSqlType(){
        String jdbcType = jdbcMetaConfiguration.getJdbcTypeName(-102);
        assertEquals("TIMESTAMP", jdbcType);
    }
    @Test
    public void getJdbcTypeName_withUnknownSqlType(){
        String jdbcType = jdbcMetaConfiguration.getJdbcTypeName(999);
        assertEquals(null, jdbcType);
    }

    @Test
    public void getJavaType_withDefinedSqlType() throws SQLException {
        String javaType = jdbcMetaConfiguration.getJavaType("INTEGER",4);
        assertEquals("java.lang.Integer", javaType);
    }

    @Test
    public void getJavaType_withKnownSqlType_UnknownTypeCode() throws SQLException {
        String javaType = jdbcMetaConfiguration.getJavaType("INTEGER",999);
        assertEquals(null, javaType);
    }

    @Test
    public void getJavaType_withUserDefinedSqlType_UnknownTypeCode() throws SQLException {
        String javaType = jdbcMetaConfiguration.getJavaType("UUID",999);
        assertEquals("java.lang.String", javaType);
    }

    @Test
    public void getJavaType_withUserDefinedSqlType_OtherTypeCode() throws SQLException {
        String javaType = jdbcMetaConfiguration.getJavaType("UUID",1111);
        assertEquals("java.lang.String", javaType);
    }

    @Test
    public void getJavaType_msSqlDateTimeOffsetType_returnTimestamp() throws SQLException {
        String javaType = jdbcMetaConfiguration.getJavaType("TIMESTAMP",-155);
        assertEquals(Timestamp.class.getName(), javaType);
    }

    @Test
    public void getJavaType_withNullSqlType_UnknownTypeCode() throws SQLException {
        String javaType = jdbcMetaConfiguration.getJavaType(null,999);
        assertEquals(null, javaType);
    }
}

