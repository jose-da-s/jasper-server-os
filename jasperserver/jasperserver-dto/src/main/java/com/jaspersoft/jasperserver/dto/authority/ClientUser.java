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
package com.jaspersoft.jasperserver.dto.authority;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author: Zakhar.Tomchenco
 */

@XmlRootElement(name = "user")
public class ClientUser implements DeepCloneable<ClientUser> {
    private Set<ClientRole> roleSet;
    private String fullName;
    private String password;
    private String emailAddress;
    private Boolean externallyDefined;
    private Boolean enabled;
    private Date previousPasswordChangeTime;
    private String tenantId;
    private String username;
    private Integer numberOfFailedLoginAttempts;

    public ClientUser() {
    }

    public ClientUser(ClientUser other) {
        checkNotNull(other);

        this.roleSet = copyOf(other.getRoleSet());
        this.fullName = other.getFullName();
        this.password = other.getPassword();
        this.emailAddress = other.getEmailAddress();
        this.externallyDefined = other.isExternallyDefined();
        this.enabled = other.isEnabled();
        this.previousPasswordChangeTime = copyOf(other.getPreviousPasswordChangeTime());
        this.tenantId = other.getTenantId();
        this.username = other.getUsername();
        this.numberOfFailedLoginAttempts = other.getNumberOfFailedLoginAttempts();
    }

    @Override
    public ClientUser deepClone() {
        return new ClientUser(this);
    }

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    public Set<ClientRole> getRoleSet() {
        return roleSet;
    }

    public ClientUser setRoleSet(Set<ClientRole> roleSet) {
        this.roleSet = roleSet;
        return this;
    }

    @XmlElement(name = "fullName")
    public String getFullName() {
        return fullName;
    }

    public ClientUser setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    @XmlElement(name = "password")
    public String getPassword() {
        return password;
    }

    public ClientUser setPassword(String password) {
        this.password = password;
        return this;
    }

    @XmlElement(name = "emailAddress")
    public String getEmailAddress() {
        return emailAddress;
    }

    public ClientUser setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    @XmlElement(name = "externallyDefined")
    public Boolean isExternallyDefined() {
        return externallyDefined;
    }

    public ClientUser setExternallyDefined(Boolean externallyDefined) {
        this.externallyDefined = externallyDefined;
        return this;
    }

    @XmlElement(name = "enabled")
    public Boolean isEnabled() {
        return enabled;
    }

    public ClientUser setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @XmlElement(name = "previousPasswordChangeTime")
    public Date getPreviousPasswordChangeTime() {
        return previousPasswordChangeTime;
    }

    public ClientUser setPreviousPasswordChangeTime(Date previousPasswordChangeTime) {
        this.previousPasswordChangeTime = previousPasswordChangeTime;
        return this;
    }

    @XmlElement(name = "tenantId")
    public String getTenantId() {
        return tenantId;
    }

    public ClientUser setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @XmlElement(name = "username")
    public String getUsername() {
        return username;
    }

    public ClientUser setUsername(String username) {
        this.username = username;
        return this;
    }

    @XmlElement(name = "numberOfFailedLoginAttempts")
    public Integer getNumberOfFailedLoginAttempts() {
        return numberOfFailedLoginAttempts;
    }

    public ClientUser setNumberOfFailedLoginAttempts(Integer numberOfFailedLoginAttempts) {
        this.numberOfFailedLoginAttempts = numberOfFailedLoginAttempts;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientUser that = (ClientUser) o;
        return Objects.equals(roleSet, that.roleSet) && Objects.equals(fullName, that.fullName)
                && Objects.equals(password, that.password) && Objects.equals(emailAddress, that.emailAddress)
                && Objects.equals(externallyDefined, that.externallyDefined) && Objects.equals(enabled, that.enabled)
                && Objects.equals(previousPasswordChangeTime, that.previousPasswordChangeTime)
                && Objects.equals(tenantId, that.tenantId) && Objects.equals(username, that.username)
                && Objects.equals(numberOfFailedLoginAttempts, that.numberOfFailedLoginAttempts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleSet, fullName, password, emailAddress, externallyDefined, enabled,
                previousPasswordChangeTime, tenantId, username, numberOfFailedLoginAttempts);
    }

    @Override
    public String toString() {
        return "ClientUser{" +
                "roleSet=" + roleSet +
                ", fullName='" + fullName + '\'' +
                ", password='" + password + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", externallyDefined=" + externallyDefined +
                ", enabled=" + enabled +
                ", previousPasswordChangeTime=" + previousPasswordChangeTime +
                ", tenantId='" + tenantId + '\'' +
                ", username='" + username + '\'' +
                ", numberOfFailedLoginAttempts=" + numberOfFailedLoginAttempts +
                '}';
    }
}
