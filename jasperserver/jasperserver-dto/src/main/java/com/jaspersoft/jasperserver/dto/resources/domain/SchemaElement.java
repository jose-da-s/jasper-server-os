/*
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
package com.jaspersoft.jasperserver.dto.resources.domain;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.lang.reflect.Constructor;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class SchemaElement<T extends SchemaElement<T>> implements DeepCloneable<T>, Serializable {
    public static final int ELEMENT_NAME_MAX_LENGTH = 20000;
    private String name;

    public SchemaElement(){}
    public SchemaElement(SchemaElement source){
        checkNotNull(source);

        name = source.getName();
    }
    @NotNull
    @Size(min = 1, max = ELEMENT_NAME_MAX_LENGTH, message = "domain.schema.presentation.element.name.length.limit")
    public String getName() {
        return name;
    }

    public T setName (String name) {
        this.name = name;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SchemaElement)) return false;

        SchemaElement that = (SchemaElement) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SchemaElement{" +
                "name='" + name + '\'' +
                '}';
    }


    @Override
    public T deepClone() {
        Class<? extends SchemaElement> thisClass = this.getClass();

        SchemaElement instance;
        try {
            Constructor<? extends SchemaElement> constructor = thisClass.getConstructor(thisClass);
            instance = constructor.newInstance(this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to call cloning constructor of " + thisClass.getName(), e);
        }
        return (T) instance;
    }

}
