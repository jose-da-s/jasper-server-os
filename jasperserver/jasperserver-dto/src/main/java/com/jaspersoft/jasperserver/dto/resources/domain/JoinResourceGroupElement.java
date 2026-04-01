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

import jakarta.validation.Valid;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class JoinResourceGroupElement extends AbstractResourceGroupElement<JoinResourceGroupElement> {
    @Valid
    private JoinInfo joinInfo;

    public JoinResourceGroupElement(){}

    public JoinResourceGroupElement(JoinResourceGroupElement source){
        super(source);
        joinInfo = copyOf(source.getJoinInfo());
    }

    public JoinInfo getJoinInfo() {
        return joinInfo;
    }

    public JoinResourceGroupElement setJoinInfo(JoinInfo joinInfo) {
        this.joinInfo = joinInfo;
        return this;
    }

    @Override
    public JoinResourceGroupElement deepClone() {
        return new JoinResourceGroupElement(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinResourceGroupElement)) return false;
        if (!super.equals(o)) return false;

        JoinResourceGroupElement that = (JoinResourceGroupElement) o;

        if (joinInfo != null ? !joinInfo.equals(that.joinInfo) : that.joinInfo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (joinInfo != null ? joinInfo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JoinResourceGroupElement{" +
                "joinInfo=" + joinInfo +
                "} " + super.toString();
    }
}
