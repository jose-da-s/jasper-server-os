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
package com.jaspersoft.jasperserver.dto.resources;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = ResourceMediaType.AZURE_SQL_DATA_SOURCE_CLIENT_TYPE)
public class ClientAzureSqlDataSource extends AbstractClientJdbcDataSource<ClientAzureSqlDataSource> {
    private String subscriptionId;
    private String keyStoreUri;
    private String keyStorePassword;
    private String serverName;
    private String dbName;

    public ClientAzureSqlDataSource() {
    }

    public ClientAzureSqlDataSource(ClientAzureSqlDataSource source) {
        super(source);
        subscriptionId = source.getSubscriptionId();
        keyStoreUri = source.getKeyStoreUri();
        keyStorePassword = source.getKeyStorePassword();
        serverName = source.getServerName();
        dbName = source.getDbName();
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public ClientAzureSqlDataSource setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getKeyStoreUri() {
        return keyStoreUri;
    }

    public ClientAzureSqlDataSource setKeyStoreUri(String keyStoreUri) {
        this.keyStoreUri = keyStoreUri;
        return this;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public ClientAzureSqlDataSource setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public ClientAzureSqlDataSource setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public ClientAzureSqlDataSource setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    @Override
    public String toString() {
        return "ClientAzureSqlDataSource{" + "subscriptionId='" + subscriptionId + "', serverName='" + serverName + "', dbName='" + dbName + "'} "
                + super.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dbName == null) ? 0 : dbName.hashCode());
        result = prime * result + ((keyStorePassword == null) ? 0 : keyStorePassword.hashCode());
        result = prime * result + ((keyStoreUri == null) ? 0 : keyStoreUri.hashCode());
        result = prime * result + ((serverName == null) ? 0 : serverName.hashCode());
        result = prime * result + ((subscriptionId == null) ? 0 : subscriptionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;

        ClientAzureSqlDataSource other = (ClientAzureSqlDataSource) obj;
        if (dbName == null) {
            if (other.dbName != null) {
                return false;
            }
        } else if (!dbName.equals(other.dbName)) {
            return false;
        }
        if (keyStorePassword == null) {
            if (other.keyStorePassword != null) {
                return false;
            }
        } else if (!keyStorePassword.equals(other.keyStorePassword)) {
            return false;
        }
        if (keyStoreUri == null) {
            if (other.keyStoreUri != null) {
                return false;
            }
        } else if (!keyStoreUri.equals(other.keyStoreUri)) {
            return false;
        }
        if (serverName == null) {
            if (other.serverName != null) {
                return false;
            }
        } else if (!serverName.equals(other.serverName)) {
            return false;
        }
        if (subscriptionId == null) {
            if (other.subscriptionId != null) {
                return false;
            }
        } else if (!subscriptionId.equals(other.subscriptionId)) {
            return false;
        }
        return true;
    }

    @Override
    public ClientAzureSqlDataSource deepClone() {
        return new ClientAzureSqlDataSource(this);
    }
}
