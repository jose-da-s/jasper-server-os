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
package com.jaspersoft.jasperserver.dto.common;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Volodya Sabadosh
 * @version $Id: $
 */
@XmlRootElement(name = "warning")
public class WarningDescriptor implements DeepCloneable<WarningDescriptor> {
    private String code;
    private String[] parameters;
    private String message;

    public WarningDescriptor(String code, String[] parameters, String message) {
        this.code = code;
        this.parameters = parameters;
        this.message = message;
    }

    public WarningDescriptor() {
    }

    public WarningDescriptor(WarningDescriptor warningDescriptor) {
        checkNotNull(warningDescriptor);

        this.code = warningDescriptor.getCode();
        this.parameters = copyOf(warningDescriptor.getParameters());
        this.message = warningDescriptor.getMessage();
    }

    @Override
    public WarningDescriptor deepClone() {
        return new WarningDescriptor(this);
    }

    @XmlElement(name = "code")
    public String getCode() {
        return code;
    }

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    public String[] getParameters() {
        return parameters;
    }

    @XmlElement(name = "message")
    public String getMessage() {
        return message;
    }

    public WarningDescriptor setCode(String code) {
        this.code = code;
        return this;
    }

    public WarningDescriptor setParameters(String[] parameters) {
        this.parameters = parameters;
        return this;
    }

    public WarningDescriptor setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarningDescriptor that = (WarningDescriptor) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (!Arrays.equals(parameters, that.parameters)) return false;
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(parameters);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WarningDescriptor{" +
                "code='" + code + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                ", message='" + message + '\'' +
                '}';
    }
}
