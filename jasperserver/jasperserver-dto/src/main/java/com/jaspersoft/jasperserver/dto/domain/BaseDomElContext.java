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
package com.jaspersoft.jasperserver.dto.domain;

import com.jaspersoft.jasperserver.dto.adhoc.query.validation.ValidDomElType;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.Valid;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.Arrays;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public abstract class BaseDomElContext<FinalType extends BaseDomElContext<FinalType>> implements DeepCloneable<FinalType> {
    @Valid
    private List<DomElVariable> variables;
    private List<String> forbiddenVariableNames;
    @ValidDomElType(errorCode = "domel.invalid.result.type", message = "Invalid expression result type")
    private String resultType;
    private Boolean aggregate;
    private Boolean nonStrictMode;
    private Boolean skipUndefinedVariablesCheck;
    private Boolean skipTypeAndFunctionsValidation;

    public BaseDomElContext(){}

    public BaseDomElContext(BaseDomElContext source){
        checkNotNull(source);

        variables = copyOf(source.getVariables());
        forbiddenVariableNames = copyOf(source.getForbiddenVariableNames());
        resultType = source.getResultType();
        aggregate = source.getAggregate();
        nonStrictMode = source.getNonStrictMode();
        skipTypeAndFunctionsValidation = source.getSkipTypeAndFunctionsValidation();
        skipUndefinedVariablesCheck = source.getSkipUndefinedVariablesCheck();
    }

    public Boolean getSkipTypeAndFunctionsValidation() {
        return skipTypeAndFunctionsValidation;
    }

    public FinalType setSkipTypeAndFunctionsValidation(Boolean skipTypeAndFunctionsValidation) {
        this.skipTypeAndFunctionsValidation = skipTypeAndFunctionsValidation;
        return (FinalType) this;
    }

    public Boolean getNonStrictMode() {
        return nonStrictMode;
    }

    public FinalType setNonStrictMode(Boolean nonStrictMode) {
        this.nonStrictMode = nonStrictMode;
        return (FinalType) this;
    }

    public Boolean getSkipUndefinedVariablesCheck() {
        return skipUndefinedVariablesCheck;
    }

    public FinalType setSkipUndefinedVariablesCheck(Boolean skipUndefinedVariablesCheck) {
        this.skipUndefinedVariablesCheck = skipUndefinedVariablesCheck;
        return (FinalType) this;
    }

    public Boolean getAggregate() {
        return aggregate;
    }

    public FinalType setAggregate(Boolean aggregate) {
        this.aggregate = aggregate;
        return (FinalType) this;
    }

    @XmlElementWrapper(name = "variables")
    @XmlElement(name = "variable")
    public List<DomElVariable> getVariables() {
        return variables;
    }

    public FinalType setVariables(List<DomElVariable> variables) {
        this.variables = variables;
        return (FinalType) this;
    }

    public List<String> getForbiddenVariableNames() {
        return forbiddenVariableNames;
    }

    public FinalType addForbiddenVariableNames(String... names){
        final List<String> forbiddenNames = Arrays.asList(names);
        if(forbiddenVariableNames == null){
            forbiddenVariableNames = forbiddenNames;
        } else {
            forbiddenVariableNames.addAll(forbiddenNames);
        }
        return (FinalType) this;
    }

    public FinalType setForbiddenVariableNames(List<String> forbiddenVariableNames) {
        this.forbiddenVariableNames = forbiddenVariableNames;
        return (FinalType) this;
    }

    public String getResultType() {
        return resultType;
    }

    public FinalType setResultType(String resultType) {
        this.resultType = resultType;
        return (FinalType) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseDomElContext<?> that = (BaseDomElContext<?>) o;

        if (variables != null ? !variables.equals(that.variables) : that.variables != null) return false;
        if (forbiddenVariableNames != null ? !forbiddenVariableNames.equals(that.forbiddenVariableNames) : that.forbiddenVariableNames != null)
            return false;
        if (resultType != null ? !resultType.equals(that.resultType) : that.resultType != null) return false;
        if (aggregate != null ? !aggregate.equals(that.aggregate) : that.aggregate != null) return false;
        if (nonStrictMode != null ? !nonStrictMode.equals(that.nonStrictMode) : that.nonStrictMode != null)
            return false;
        if (skipUndefinedVariablesCheck != null ? !skipUndefinedVariablesCheck.equals(that.skipUndefinedVariablesCheck) : that.skipUndefinedVariablesCheck != null)
            return false;
        return skipTypeAndFunctionsValidation != null ? skipTypeAndFunctionsValidation.equals(that.skipTypeAndFunctionsValidation) : that.skipTypeAndFunctionsValidation == null;
    }

    @Override
    public int hashCode() {
        int result = variables != null ? variables.hashCode() : 0;
        result = 31 * result + (forbiddenVariableNames != null ? forbiddenVariableNames.hashCode() : 0);
        result = 31 * result + (resultType != null ? resultType.hashCode() : 0);
        result = 31 * result + (aggregate != null ? aggregate.hashCode() : 0);
        result = 31 * result + (nonStrictMode != null ? nonStrictMode.hashCode() : 0);
        result = 31 * result + (skipUndefinedVariablesCheck != null ? skipUndefinedVariablesCheck.hashCode() : 0);
        result = 31 * result + (skipTypeAndFunctionsValidation != null ? skipTypeAndFunctionsValidation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "variables=" + variables +
                ", forbiddenVariableNames=" + forbiddenVariableNames +
                ", resultType='" + resultType + '\'' +
                ", aggregate=" + aggregate +
                ", nonStrictMode=" + nonStrictMode +
                ", skipUndefinedVariablesCheck=" + skipUndefinedVariablesCheck +
                ", skipTypeAndFunctionsValidation=" + skipTypeAndFunctionsValidation +
                '}';
    }
}
