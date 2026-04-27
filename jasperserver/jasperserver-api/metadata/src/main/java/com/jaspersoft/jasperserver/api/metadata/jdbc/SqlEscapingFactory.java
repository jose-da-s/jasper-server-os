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

package com.jaspersoft.jasperserver.api.metadata.jdbc;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author serhii.blazhyievskyi
 * @version $Id: SqlEscapingFactory.java 61657 2016-12-27 16:42:27Z ykovalch $
 */
@Component
public class SqlEscapingFactory {
    @Resource(name = "${bean.dbTypesToStrategyMapping}")
    private Map<String, SqlEscapingStrategy> dbTypes;
    private SqlEscapingStrategy defaultEscapingStrategy = new GenericSqlEscapingStrategy();

    public SqlEscapingStrategy getSlqEscapingStrategy(DatabaseMetaData dbMetadata) throws SQLException {
        return getSlqEscapingStrategy(dbMetadata.getDatabaseProductName());
    }

    public SqlEscapingStrategy getSlqEscapingStrategy(String databaseProductName){
        if (dbTypes.containsKey(databaseProductName)) {
            return dbTypes.get(databaseProductName);
        } else {
            return defaultEscapingStrategy;
        }
    }
}

