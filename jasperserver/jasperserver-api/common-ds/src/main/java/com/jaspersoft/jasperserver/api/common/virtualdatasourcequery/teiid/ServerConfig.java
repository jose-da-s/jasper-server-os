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
import jakarta.resource.spi.XATerminator;

/**
 * @author Ivan Chan (ichan@jaspersoft.com)
 * @version $Id$
 */
@JasperServerAPI
public class ServerConfig {

    String transactionManagerJNDILookup;
    ServerInit serverInit;
    XATerminator xaTerminator;

    /*
	 * Set Transaction Manager Lookup for JNDI
	 * eg, java:/TransactionManager, java:/comp/env/TransactionManager, java:jboss/TransactionManager
	 */
    public String getTransactionManagerJNDILookup() {
        return transactionManagerJNDILookup;
    }

    /*
	 * Get Transaction Manager Lookup for JNDI
	 * eg, java:/TransactionManager, java:/comp/env/TransactionManager, java:jboss/TransactionManager
	 */
    public void setTransactionManagerJNDILookup(String transactionManagerJNDILookup) {
        this.transactionManagerJNDILookup = transactionManagerJNDILookup;
    }

    public ServerInit getServerInit() {
        return serverInit;
    }

    public void setServerInit(ServerInit serverInit) {
        this.serverInit = serverInit;
    }

    public XATerminator getXaTerminator() {
        return xaTerminator;
    }

    public void setXaTerminator(XATerminator xaTerminator) {
        this.xaTerminator = xaTerminator;
    }
}
