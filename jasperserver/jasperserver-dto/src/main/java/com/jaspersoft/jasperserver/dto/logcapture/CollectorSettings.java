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
package com.jaspersoft.jasperserver.dto.logcapture;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.io.File;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * DTO for Diagnostic Collector's settings.
 *
 * @author Yakiv Tymoshenko
 * @version $Id$
 * @since 11.08.14
 */
@XmlRootElement
@XmlType(propOrder = {"id", "name", "verbosity", "logFilterParameters", "status", "keyalias"})
@XmlAccessorType(XmlAccessType.PROPERTY)
public class CollectorSettings implements DeepCloneable<CollectorSettings> {

    public static void marshall(CollectorSettings collectorSettings, String filePath) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CollectorSettings.class);
        Marshaller xmlMarshaller = jaxbContext.createMarshaller();
        xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        xmlMarshaller.marshal(collectorSettings, new File(filePath));
    }

    public static CollectorSettings unMarshall(String filePath) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CollectorSettings.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (CollectorSettings) unmarshaller.unmarshal(new File(filePath));
    }

    private LogFilterParameters logFilterParameters;
    private String id;
    private String name;
    private String verbosity;
    private String status;
    private String keyalias;

    public CollectorSettings() {
        logFilterParameters = new LogFilterParameters();
    }

    // Cloning constructor required by Jaspersoft REST DTo convention.
    public CollectorSettings(CollectorSettings other) {
        checkNotNull(other);

        this.id = other.getId();
        this.name = other.getName();
        this.verbosity = other.getVerbosity();
        this.status = other.getStatus();
        this.logFilterParameters = copyOf(other.getLogFilterParameters());
        this.keyalias = other.getKeyalias();
    }

    @Override
    public CollectorSettings deepClone() {
        return new CollectorSettings(this);
    }

    /*
        Don't name it "isExportEnabled" because it would look like a property in resulting JSON/XML.
     */
    public boolean exportEnabled() {
        if (logFilterParameters == null) {
            return false;
        }
        ResourceAndSnapshotFilter resourceAndSnapshotFilter = logFilterParameters.getResourceAndSnapshotFilter();
        return resourceAndSnapshotFilter != null
                && resourceAndSnapshotFilter.exportDatasnapshotEnabled();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectorSettings)) return false;

        CollectorSettings that = (CollectorSettings) o;

        if (logFilterParameters != null ? !logFilterParameters.equals(that.logFilterParameters) : that.logFilterParameters != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (verbosity != null ? !verbosity.equals(that.verbosity) : that.verbosity != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        return keyalias != null ? keyalias.equals(that.keyalias) : that.keyalias == null;
    }

    @Override
    public int hashCode() {
        int result = logFilterParameters != null ? logFilterParameters.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (verbosity != null ? verbosity.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (keyalias != null ? keyalias.hashCode() : 0);
        return result;
    }

    @XmlElement(name = "filterBy")
    public LogFilterParameters getLogFilterParameters() {
        return logFilterParameters;
    }

    // Getter required for correct json/xml transformation by jaxb.
    @SuppressWarnings("unused")
    public CollectorSettings setLogFilterParameters(LogFilterParameters logFilterParameters) {
        this.logFilterParameters = logFilterParameters;
        return this;
    }

    @Override
    public String toString() {
        return "CollectorSettings{" +
                "logFilterParameters=" + logFilterParameters +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", verbosity='" + verbosity + '\'' +
                ", status='" + status + '\'' +
                ", keyalias='" + keyalias + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public CollectorSettings setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CollectorSettings setName(String name) {
        this.name = name;
        return this;
    }

    public String getVerbosity() {
        return verbosity;
    }

    // Getter required for correct json/xml transformation by jaxb
    @SuppressWarnings("unused")
    public CollectorSettings setVerbosity(String verbosity) {
        this.verbosity = verbosity;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public CollectorSettings setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getKeyalias() {
        return keyalias;
    }

    public CollectorSettings setKeyalias(String keyalias) {
        this.keyalias = keyalias;
        return this;
    }
}
