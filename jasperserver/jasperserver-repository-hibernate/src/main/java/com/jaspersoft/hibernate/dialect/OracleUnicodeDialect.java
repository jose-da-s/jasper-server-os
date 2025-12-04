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

package com.jaspersoft.hibernate.dialect;

import org.hibernate.dialect.Oracle10gDialect;
import java.sql.Types;

public class OracleUnicodeDialect extends Oracle10gDialect 
{

	public OracleUnicodeDialect()
	{
        super();
        registerColumnType( Types.CHAR, "nchar(1)" );
        registerColumnType( Types.VARCHAR, 2000, "nvarchar2($l)" );
        registerColumnType( Types.LONGVARCHAR, 500000, "nclob" );
        
		registerColumnType( Types.VARBINARY, "blob" );
        registerColumnType( Types.VARBINARY, 2000, "raw($l)" );
        
        registerColumnType( Types.CLOB, "nclob" );
        registerColumnType( Types.TIMESTAMP, "date");

	}
	
    @Override
    public String getSelectClauseNullString(int type) 
    {
        switch(type) 
        {
            case Types.CHAR:
            case Types.VARCHAR:
                return "to_nchar(null)";
            default:
                return super.getSelectClauseNullString(type);
        }
    }
}
