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
package com.jaspersoft.jasperserver.dto.connection;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "mongodb")
public class MongodbConnection implements DeepCloneable<MongodbConnection> {
    private String mongoURI;
    private String username;
    private String password;

    public MongodbConnection(){
    }

    public MongodbConnection(MongodbConnection source){
        checkNotNull(source);

        mongoURI = source.getMongoURI();
        username = source.getUsername();
        password = source.getPassword();
    }

    public String getMongoURI() {
        return mongoURI;
    }

    public MongodbConnection setMongoURI(String mongoURI) {
        this.mongoURI = mongoURI;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MongodbConnection setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public MongodbConnection setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MongodbConnection that = (MongodbConnection) o;

        if (mongoURI != null ? !mongoURI.equals(that.mongoURI) : that.mongoURI != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mongoURI != null ? mongoURI.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MongodbConnection{" +
                "mongoURI='" + mongoURI + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                "} " + super.toString();
    }

    /*
     * DeepCloneable
     */

    @Override
    public MongodbConnection deepClone() {
        return new MongodbConnection(this);
    }
}
