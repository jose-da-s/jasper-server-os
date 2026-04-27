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

package com.jaspersoft.jasperserver.dto.job;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.connection.FtpConnection;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p/>
 * <p/>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
@XmlRootElement(name = "outputFTPInfo")
public class ClientJobFtpInfo implements DeepCloneable<ClientJobFtpInfo>{

    private String userName;
    private String password;
    private String folderPath;
    private String serverName;
    private FtpConnection.FtpType type;
    private String protocol;
    private Integer port;
    private Boolean implicit;
    private Long pbsz;
    private String prot;
    private Map<String, String> propertiesMap;
    private String sshKey;
    private String sshPassphrase;

    public ClientJobFtpInfo() {
    }

    public ClientJobFtpInfo(ClientJobFtpInfo other) {
        checkNotNull(other);

        this.folderPath = other.getFolderPath();
        this.implicit = other.getImplicit();
        this.password = other.getPassword();
        this.pbsz = other.getPbsz();
        this.port = other.getPort();
        this.propertiesMap = copyOf(other.getPropertiesMap());
        this.prot = other.getProt();
        this.protocol = other.getProtocol();
        this.serverName = other.getServerName();
        this.type = other.getType();
        this.userName = other.getUserName();
        this.sshKey = other.getSshKey();
        this.sshPassphrase = other.getSshPassphrase();
    }

    public String getUserName() {
        return userName;
    }

    public ClientJobFtpInfo setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public Boolean getImplicit() {
        return implicit;
    }

    public String getPassword() {
        return password;
    }

    public ClientJobFtpInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public ClientJobFtpInfo setFolderPath(String folderPath) {
        this.folderPath = folderPath;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public ClientJobFtpInfo setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public FtpConnection.FtpType getType() {
        return type;
    }

    public ClientJobFtpInfo setType(FtpConnection.FtpType type) {
        this.type = type;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public ClientJobFtpInfo setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ClientJobFtpInfo setPort(Integer port) {
        this.port = port;
        return this;
    }

    public ClientJobFtpInfo setImplicit(Boolean implicit) {
        this.implicit = implicit;
        return this;
    }

    public Long getPbsz() {
        return pbsz;
    }

    public ClientJobFtpInfo setPbsz(Long pbsz) {
        this.pbsz = pbsz;
        return this;
    }

    public String getProt() {
        return prot;
    }

    public ClientJobFtpInfo setProt(String prot) {
        this.prot = prot;
        return this;
    }

    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    public ClientJobFtpInfo setPropertiesMap(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
        return this;
    }


    public String getSshKey() {
        return sshKey;
    }

    public ClientJobFtpInfo setSshKey(String sshKey) {
        this.sshKey = sshKey;
        return this;
    }

    public String getSshPassphrase() {
        return sshPassphrase;
    }

    public ClientJobFtpInfo setSshPassphrase(String sshPassphrase) {
        this.sshPassphrase = sshPassphrase;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientJobFtpInfo)) return false;

        ClientJobFtpInfo that = (ClientJobFtpInfo) o;

        if (getUserName() != null ? !getUserName().equals(that.getUserName()) : that.getUserName() != null)
            return false;
        if (getPassword() != null ? !getPassword().equals(that.getPassword()) : that.getPassword() != null)
            return false;

        if (getFolderPath() != null ? !getFolderPath().equals(that.getFolderPath()) : that.getFolderPath() != null)
            return false;
        if (getServerName() != null ? !getServerName().equals(that.getServerName()) : that.getServerName() != null)
            return false;
        if (getType() != that.getType()) return false;
        if (getProtocol() != null ? !getProtocol().equals(that.getProtocol()) : that.getProtocol() != null)
            return false;
        if (getPort() != null ? !getPort().equals(that.getPort()) : that.getPort() != null) return false;
        if (getImplicit() != null ? !getImplicit().equals(that.getImplicit()) : that.getImplicit() != null)
            return false;
        if (getPbsz() != null ? !getPbsz().equals(that.getPbsz()) : that.getPbsz() != null) return false;
        if (getProt() != null ? !getProt().equals(that.getProt()) : that.getProt() != null) return false;
        if (getPropertiesMap() != null ? !getPropertiesMap().equals(that.getPropertiesMap()) : that.getPropertiesMap() != null)
            return false;
        if (getSshKey() != null ? !getSshKey().equals(that.getSshKey()) : that.getSshKey() != null) return false;
        return !(getSshPassphrase() != null ? !getSshPassphrase().equals(that.getSshPassphrase()) : that.getSshPassphrase() != null);

    }

    @Override
    public int hashCode() {
        int result = getUserName() != null ? getUserName().hashCode() : 0;
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        result = 31 * result + (getFolderPath() != null ? getFolderPath().hashCode() : 0);
        result = 31 * result + (getServerName() != null ? getServerName().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getProtocol() != null ? getProtocol().hashCode() : 0);
        result = 31 * result + (getPort() != null ? getPort().hashCode() : 0);
        result = 31 * result + (getImplicit() != null ? getImplicit().hashCode() : 0);
        result = 31 * result + (getPbsz() != null ? getPbsz().hashCode() : 0);
        result = 31 * result + (getProt() != null ? getProt().hashCode() : 0);
        result = 31 * result + (getPropertiesMap() != null ? getPropertiesMap().hashCode() : 0);
        result = 31 * result + (getSshKey() != null ? getSshKey().hashCode() : 0);
        result = 31 * result + (getSshPassphrase() != null ? getSshPassphrase().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobFtpInfo{" +
                "folderPath='" + folderPath + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", serverName='" + serverName + '\'' +
                ", type=" + type +
                ", protocol='" + protocol + '\'' +
                ", port=" + port +
                ", implicit=" + implicit +
                ", pbsz=" + pbsz +
                ", prot='" + prot + '\'' +
                ", propertiesMap=" + propertiesMap +
                ", sshKey='" + sshKey + '\'' +
                ", sshPassphrase='" + sshPassphrase + '\'' +
                '}';
    }

    @Override
    public ClientJobFtpInfo deepClone() {
        return new ClientJobFtpInfo(this);
    }
}
