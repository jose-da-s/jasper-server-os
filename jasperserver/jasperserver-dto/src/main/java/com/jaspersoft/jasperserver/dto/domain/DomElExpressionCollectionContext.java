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
package com.jaspersoft.jasperserver.dto.domain;

import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class DomElExpressionCollectionContext extends BaseDomElContext<DomElExpressionCollectionContext> {
    @NotNull
    private List<DomElExpressionContext> expressionContexts;

    public DomElExpressionCollectionContext(){}

    public DomElExpressionCollectionContext(DomElExpressionCollectionContext source){
        super(source);
        expressionContexts = copyOf(source.getExpressionContexts());
    }

    @XmlElementWrapper(name = "expressionContexts")
    @XmlElement(name = "expressionContext")
    public List<DomElExpressionContext> getExpressionContexts() {
        return expressionContexts;
    }

    public DomElExpressionCollectionContext setExpressionContexts(List<DomElExpressionContext> expressionContexts) {
        this.expressionContexts = expressionContexts;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        if (!(o instanceof DomElExpressionCollectionContext)) return false;

        DomElExpressionCollectionContext that = (DomElExpressionCollectionContext) o;

        return expressionContexts != null ? expressionContexts.equals(that.expressionContexts) : that.expressionContexts == null;
    }

    @Override
    public int hashCode() {
        return expressionContexts != null ? expressionContexts.hashCode() : 0;
    }

    @Override
    public String toString() {
        return super.toString() +
                "expressionContexts=" + expressionContexts +
                '}';
    }

    @Override
    public DomElExpressionCollectionContext deepClone() {
        return new DomElExpressionCollectionContext(this);
    }
}
