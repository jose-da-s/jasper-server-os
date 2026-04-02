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

import com.jaspersoft.jasperserver.dto.common.ResourceLocation;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "ftp")
public class FtpConnection implements ResourceLocation {
    private String holder;
    private String host;
    private String userName;
    private String password;
    private String folderPath;
    private FtpType type;
    private String protocol;
    private Integer port;
    private Boolean implicit;
    private String sshKey;
    private String sshPassphrase;
    private String prot;
    private Long pbsz;

    public FtpConnection(){
        userName = "anonymous";
    }

    public FtpConnection(FtpConnection other) {
        checkNotNull(other);

        this.holder = other.getHolder();
        this.host = other.getHost();
        this.userName = other.getUserName();
        this.password = other.getPassword();
        this.folderPath = other.getFolderPath();
        this.type = other.getType();
        this.protocol = other.getProtocol();
        this.port = other.getPort();
        this.implicit = other.getImplicit();
        this.prot = other.getProt();
        this.pbsz = other.getPbsz();
        this.sshKey = other.getSshKey();
        this.sshPassphrase = other.getSshPassphrase();
    }

    public String getHolder() {
        return holder;
    }

    public FtpConnection setHolder(String holder) {
        this.holder = holder;
        return this;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public FtpConnection setFolderPath(String folderPath) {
        this.folderPath = folderPath;
        return this;
    }

    public FtpType getType() {
        return type;
    }

    public FtpConnection setType(FtpType type) {
        this.type = type;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public FtpConnection setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public FtpConnection setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Boolean getImplicit() {
        return implicit;
    }

    public FtpConnection setImplicit(Boolean implicit) {
        this.implicit = implicit;
        return this;
    }

    public String getProt() {
        return prot;
    }

    public FtpConnection setProt(String prot) {
        this.prot = prot;
        return this;
    }

    public Long getPbsz() {
        return pbsz;
    }

    public FtpConnection setPbsz(Long pbsz) {
        this.pbsz = pbsz;
        return this;
    }

    public String getHost() {
        return host;
    }

    public FtpConnection setHost(String host) {
        this.host = host;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public FtpConnection setUserName(String userName) {
        this.userName = (userName!=null && !userName.isEmpty()) ?  userName : "anonymous";
        return this;
    }

    public String getPassword() {
        return password;
    }

    public FtpConnection setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getSshKey() {
        return sshKey;
    }

    public FtpConnection setSshKey(String sshKey) {
        this.sshKey = sshKey;
        return this;
    }

    public String getSshPassphrase() {
        return sshPassphrase;
    }

    public FtpConnection setSshPassphrase(String sshPassphrase) {
        this.sshPassphrase = sshPassphrase;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FtpConnection that = (FtpConnection) o;

        if (holder != null ? !holder.equals(that.holder) : that.holder != null) return false;
        if (folderPath != null ? !folderPath.equals(that.folderPath) : that.folderPath != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (implicit != null ? !implicit.equals(that.implicit) : that.implicit != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (pbsz != null ? !pbsz.equals(that.pbsz) : that.pbsz != null) return false;
        if (port != null ? !port.equals(that.port) : that.port != null) return false;
        if (prot != null ? !prot.equals(that.prot) : that.prot != null) return false;
        if (protocol != null ? !protocol.equals(that.protocol) : that.protocol != null) return false;
        if (type != that.type) return false;
        if (!userName.equals(that.userName)) return false;
        if (sshKey != null ? !sshKey.equals(that.sshKey) : that.sshKey != null) return false;
        if (sshPassphrase != null ? !sshPassphrase.equals(that.sshPassphrase) : that.sshPassphrase != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (holder != null ? holder.hashCode() : 0);
        result = 31 * result + userName.hashCode();
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (folderPath != null ? folderPath.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (implicit != null ? implicit.hashCode() : 0);
        result = 31 * result + (prot != null ? prot.hashCode() : 0);
        result = 31 * result + (pbsz != null ? pbsz.hashCode() : 0);
        result = 31 * result + (sshKey != null ? sshKey.hashCode() : 0);
        result = 31 * result + (sshPassphrase != null ? sshPassphrase.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FtpConnection{" +
                "holder='" + holder + '\'' +
                ", host='" + host + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", folderPath='" + folderPath + '\'' +
                ", type=" + type +
                ", protocol='" + protocol + '\'' +
                ", port=" + port +
                ", implicit=" + implicit +
                ", prot='" + prot + '\'' +
                ", pbsz=" + pbsz +
                ", sshKey='" + sshKey + '\'' +
                ", sshPassphrase='" + sshPassphrase + '\'' +
                "} " + super.toString();
    }

    public enum FtpType{
        ftp, ftps, sftp
    }

    /*
     * DeepCloneable
     */

    @Override
    public FtpConnection deepClone() {
        return new FtpConnection(this);
    }
}
