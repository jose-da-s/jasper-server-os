/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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
package com.jaspersoft.jasperserver.dto.executions;

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptorTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.common.CommonErrorCode.buildProperties;
import static com.jaspersoft.jasperserver.dto.common.CommonErrorCode.paramNames;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.AGGREGATE_FIELD_ID;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.AGGREGATION_FUNCTION;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.AMBIGUOUS_REFS;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.AXIS_NAME;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.AXIS_TYPE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.AVAILABLE_TRANSFORMATION_PATHS;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_FIELD_EXPRESSION;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_TRANSFORMATION_PATH;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.DATASOURCE_TYPE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.DATASOURCE_URI;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.DETAIL_MESSAGE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.DIMENSION_COUNT;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.DIMENSION_LEVEL_COUNT;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.DIMENSION_NUMBER;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.DUPLICATED_IDS;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.END_RANGE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.ERROR_MESSAGE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.EXECUTION_ID;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.EXPRESSION;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.EXPRESSION_NAME;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.EXPRESSION_TYPE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIELD_COUNT;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIELD_EXPRESSION;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIELD_ID;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIELD_NAME;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIELDS_NOT_FOUND;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIELD_NUMBER;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIELD_TYPE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIELD_UNIQUE_NAME;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.FIRST_FIELD_ID;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_AGGREGATION_EXPRESSION;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_AGGREGATION_FUNCTION;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_CATEGORIZER_NAME;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_FILTER_EXPRESSION;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_LIMIT_VALUE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_PARAMETER_NAMES;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.INVALID_TYPE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.REFERENCE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MAX_QUERY_LIMIT;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MAX_SIZE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MEASURE_COUNT;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MEASURE_ID;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MEASURE_INDEX;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MEASURE_NAME;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MEASURE_NUMBER;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.METADATA_FIELD_NAMES_COLLISION_PARAMS;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MIN_PAGE_SIZE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MIN_QUERY_LIMIT;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.MIN_SIZE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.OPERATOR;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.PARAMETER_TYPES;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.PROPERTY_PATH;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.QUERY_FIELD_ID;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.QUERY_FIELD_IDS;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.SECOND_FIELD_ID;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.START_RANGE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.UNSUPPORTED_TYPE;
import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorParam.VAR_NAME;

/**
 * @author Volodya Sabadosh
 */
public enum QueryExecutionsErrorCode implements ErrorDescriptorTemplate {
    QUERY_EXECUTION_NOT_FOUND(Codes.QUERY_EXECUTION_NOT_FOUND,
            paramNames(EXECUTION_ID)),
    QUERY_EXECUTION_REJECTED(Codes.QUERY_EXECUTION_REJECTED,
            paramNames(EXECUTION_ID)),
    QUERY_DATASOURCE_NOT_FOUND(Codes.QUERY_DATASOURCE_NOT_FOUND,
            paramNames(DATASOURCE_URI)),
    QUERY_DATASOURCE_TYPE_NOT_SUPPORTED(Codes.QUERY_DATASOURCE_TYPE_NOT_SUPPORTED,
            paramNames(DATASOURCE_URI)),
    QUERY_DATASOURCE_ACCESS_DENIED(Codes.QUERY_DATASOURCE_ACCESS_DENIED,
            paramNames(DATASOURCE_URI)),
    QUERY_IN_MEMORY_DATASOURCE_TYPE_NOT_SUPPORTED(Codes.QUERY_IN_MEMORY_DATASOURCE_TYPE_NOT_SUPPORTED,
            paramNames(PROPERTY_PATH, DATASOURCE_TYPE)),
    QUERY_FILTER_IS_NOT_VALID(Codes.QUERY_FILTER_IS_NOT_VALID,
            paramNames(INVALID_FILTER_EXPRESSION)),
    DATASOURCE_CONNECTION_FIELD(Codes.DATASOURCE_CONNECTION_FIELD,
            null),
    CALCULATED_FIELD_EXPRESSION_EVALUATION_ERROR(Codes.CALCULATED_FIELD_EXPRESSION_EVALUATION_ERROR,
            paramNames(FIELD_EXPRESSION, FIELD_NAME, DETAIL_MESSAGE)),
    QUERY_EXECUTION_CANCELLED(Codes.QUERY_EXECUTION_CANCELLED,
            paramNames(EXECUTION_ID)),
    QUERY_METADATA_FIELDS_NOT_FOUND(Codes.QUERY_METADATA_FIELDS_NOT_FOUND,
            paramNames(FIELDS_NOT_FOUND)),
    QUERY_DATASOURCE_INVALID(Codes.QUERY_DATASOURCE_INVALID,
            paramNames(DATASOURCE_URI)),
    QUERY_AGGREGATE_DEFINITION_ERROR(Codes.QUERY_AGGREGATE_DEFINITION_ERROR,
            paramNames(PROPERTY_PATH)),
    QUERY_FIELD_IDS_COLLISION(Codes.QUERY_FIELD_IDS_COLLISION, paramNames(QUERY_FIELD_IDS)),
    QUERY_FIELD_IDS_METADATA_FIELD_COLLISION(Codes.QUERY_FIELD_IDS_METADATA_FIELD_COLLISION,
            paramNames(QUERY_FIELD_IDS)),
    QUERY_FIELDS_FROM_MULTIPLE_DATAISLANDS(Codes.QUERY_FIELDS_FROM_MULTIPLE_DATAISLANDS,
            paramNames(FIRST_FIELD_ID, SECOND_FIELD_ID)),
    QUERY_EXPRESSION_OPERANDS_SIZE_OF_BOUND(Codes.QUERY_EXPRESSION_OPERANDS_SIZE_OF_BOUND,
            paramNames(PROPERTY_PATH, MIN_SIZE, MAX_SIZE)),
    QUERY_WHERE_EXPRESSION_TYPE_NOT_SUPPORTED(Codes.QUERY_WHERE_EXPRESSION_TYPE_NOT_SUPPORTED,
            paramNames(EXPRESSION_TYPE)),
    QUERY_AGGREGATION_EXPRESSION_NOT_VALID(Codes.QUERY_AGGREGATION_EXPRESSION_NOT_VALID,
            paramNames(EXPRESSION_TYPE)),
    //NOT Supported for now. But used in validation annotations and some tests check it.
    QUERY_CALCULATED_FIELD_EXPRESSION_NOT_VALID(Codes.QUERY_CALCULATED_FIELD_EXPRESSION_NOT_VALID,
            paramNames(EXPRESSION_TYPE)),
    QUERY_FIELD_EXPRESSION_NOT_VALID(Codes.QUERY_FIELD_EXPRESSION_NOT_VALID,
            paramNames(PROPERTY_PATH, EXPRESSION_NAME)),
    QUERY_AGGREGATE_EXPRESSION_NOT_VALID(Codes.QUERY_AGGREGATE_EXPRESSION_NOT_VALID,
            paramNames(PROPERTY_PATH, EXPRESSION_NAME)),
    QUERY_AGGREGATION_FUNCTION_NAME_NOT_VALID(Codes.QUERY_AGGREGATION_FUNCTION_NAME_NOT_VALID,
            paramNames(PROPERTY_PATH, AGGREGATION_FUNCTION)),
    QUERY_GROUPBY_ALLGROUP_NOT_FIRST(Codes.QUERY_GROUPBY_ALLGROUP_NOT_FIRST,
            paramNames(PROPERTY_PATH)),
    QUERY_GROUPBY_CATEGORIZER_NOT_VALID(Codes.QUERY_GROUPBY_CATEGORIZER_NOT_VALID,
            paramNames(INVALID_CATEGORIZER_NAME)),
    QUERY_MULTI_AXIS_AGGREGATION_IS_EMPTY(Codes.QUERY_MULTI_AXIS_AGGREGATION_IS_EMPTY,
            null),
    QUERY_INVALID_STRUCTURE(Codes.QUERY_INVALID_STRUCTURE,
            null),
    QUERY_ORDERBY_TOP_OR_BOTTOM_LIMIT_NOT_VALID(Codes.QUERY_ORDERBY_TOP_OR_BOTTOM_LIMIT_NOT_VALID,
            paramNames(PROPERTY_PATH, INVALID_LIMIT_VALUE)),
    QUERY_WHERE_PARAMETERS_EXPRESSION_NOT_VALID(Codes.QUERY_WHERE_PARAMETERS_EXPRESSION_NOT_VALID,
            null),
    QUERY_DETAILS_UNSUPPORTED(Codes.QUERY_DETAILS_UNSUPPORTED,
            null),
    QUERY_TRANSFORMATION_TOPN_NOT_ALLOWED_AGGREGATIONS_IN_ROWS(Codes.QUERY_TRANSFORMATION_TOPN_NOT_ALLOWED_AGGREGATIONS_IN_ROWS,
            null),
    EXPRESSION_REPRESENTATION_REQUIRED(Codes.EXPRESSION_REPRESENTATION_REQUIRED,
            paramNames(PROPERTY_PATH)),
    QUERY_TIMEBALANCE_INVALID_AGGREGATE_FUNCTION(Codes.QUERY_TIMEBALANCE_INVALID_AGGREGATE_FUNCTION,
            paramNames(QUERY_FIELD_ID)),
    QUERY_TIMEBALANCE_NO_DATE_FIELD(Codes.QUERY_TIMEBALANCE_NO_DATE_FIELD,
            paramNames(QUERY_FIELD_ID)),
    QUERY_TIMEBALANCE_CANNOT_BE_APPLIED(Codes.QUERY_TIMEBALANCE_CANNOT_BE_APPLIED,
            paramNames(QUERY_FIELD_ID, INVALID_TYPE)),
    QUERY_TIMEBALANCE_MULTIPLE_DATE_FIELD(Codes.QUERY_TIMEBALANCE_MULTIPLE_DATE_FIELD,
            paramNames(QUERY_FIELD_ID)),
    QUERY_FIELD_AGGREGATE_FUNCTION_IS_UNKNOWN(Codes.QUERY_FIELD_AGGREGATE_FUNCTION_IS_UNKNOWN,
            paramNames(AGGREGATE_FIELD_ID, INVALID_AGGREGATION_FUNCTION)),
    QUERY_FIELD_AGGREGATE_EXPRESSION_PARSE_ERROR(Codes.QUERY_FIELD_AGGREGATE_EXPRESSION_PARSE_ERROR,
            paramNames(AGGREGATE_FIELD_ID, INVALID_AGGREGATION_EXPRESSION,
            ERROR_MESSAGE)),
    QUERY_FIELD_EXPRESSION_PARSE_ERROR(Codes.QUERY_FIELD_EXPRESSION_PARSE_ERROR,
            paramNames(FIELD_ID, INVALID_FIELD_EXPRESSION,
                    ERROR_MESSAGE)),
    QUERY_ORDERBY_LEVEL_REFERENCE_NOT_VALID(Codes.QUERY_ORDERBY_LEVEL_REFERENCE_NOT_VALID,
            paramNames(PROPERTY_PATH)),
    QUERY_LIMT_OUT_OF_RANGE(Codes.QUERY_LIMT_OUT_OF_RANGE,
            paramNames(MIN_QUERY_LIMIT, MAX_QUERY_LIMIT)),
    QUERY_WHERE_PARAMETERS_EXPRESSION_TYPE_NOT_VALID(Codes.QUERY_WHERE_PARAMETERS_EXPRESSION_TYPE_NOT_VALID,
            paramNames(INVALID_PARAMETER_NAMES, OPERATOR, PARAMETER_TYPES)),
    PAGING_AXIS_COUNT_OUT_OF_RANGE(Codes.PAGING_AXIS_COUNT_OUT_OF_RANGE,
            null),
    PAGING_OFFSET_OUT_OF_RANGE(Codes.PAGING_OFFSET_OUT_OF_RANGE,
            paramNames(START_RANGE, END_RANGE)),
    PAGING_PAGE_SIZE_OUT_OF_RANGE(Codes.PAGING_PAGE_SIZE_OUT_OF_RANGE,
            paramNames(MIN_PAGE_SIZE)),
    //Error Code that is not documented
    PARAMETER_NAME_IN_NOT_VALID(Codes.QUERY_WHERE_PARAMETERS_NAME_IS_NOT_VALID, null),
    //Error Codes, that not appears in REST API and is not documented.//Discuss with Andy:
    FIELD_OR_MEASURE_IS_NOT_FOUND(Codes.FIELD_OR_MEASURE_IS_NOT_FOUND,
            paramNames(FIELD_NAME, MEASURE_NAME)),
    FIELD_NAME_IS_NULL(Codes.FIELD_NAME_IS_NULL,
            null),
    FIELD_IS_NOT_FOUND(Codes.FIELD_IS_NOT_FOUND,
            paramNames(FIELD_NAME)),
    CALC_FIELD_IS_NOT_FOUND(Codes.CALC_FIELD_IS_NOT_FOUND,
            paramNames(FIELD_NAME)),
    CALC_FIELD_EXPRESSION_IS_NULL(Codes.CALC_FIELD_EXPRESSION_IS_NULL,
            paramNames(FIELD_NAME, FIELD_EXPRESSION)),
    CALC_FIELD_EXPRESSION_PARSE_ERROR(Codes.CALC_FIELD_EXPRESSION_PARSE_ERROR,
            paramNames(FIELD_NAME, FIELD_EXPRESSION, ERROR_MESSAGE)),
    CALC_FIELD_AGGREGATE_EXPRESSION_IS_INVALID(Codes.CALC_FIELD_AGGREGATE_EXPRESSION_IS_INVALID,
            paramNames(FIELD_ID, INVALID_AGGREGATION_EXPRESSION)),
    CALC_FIELD_IS_MISSING_KIND_PROPERTY(Codes.CALC_FIELD_IS_MISSING_KIND_PROPERTY,
            paramNames(FIELD_ID)),
    ADHOC_FIELD_AGGREGATE_FUNCTION_AGGREGATE_FORMULA_IS_NOT_APPLICABLE(
            Codes.ADHOC_FIELD_AGGREGATE_FUNCTION_AGGREGATE_FORMULA_IS_NOT_APPLICABLE,
            paramNames(FIELD_ID, INVALID_AGGREGATION_FUNCTION)),
    //Strange Error code...
    ADHOC_FIELD_AGGREGATE_EXPRESSION_IS_NULL(Codes.ADHOC_FIELD_AGGREGATE_EXPRESSION_IS_NULL,
            paramNames(FIELD_ID, INVALID_AGGREGATION_FUNCTION)),
    SORT_FIELD_HAS_BAD_FIELD_OR_MEASURE_NAME(Codes.SORT_FIELD_HAS_BAD_FIELD_OR_MEASURE_NAME,
            paramNames(FIELD_NUMBER, FIELD_COUNT)),
    SORT_FIELD_HAS_BAD_MEASURE_INDEX(Codes.SORT_FIELD_HAS_BAD_MEASURE_INDEX,
            paramNames(FIELD_NAME, MEASURE_INDEX)),
    MEASURE_HAS_BAD_NAME(Codes.MEASURE_HAS_BAD_NAME,
            paramNames(MEASURE_NUMBER, MEASURE_COUNT)),
    MEASURE_IS_NOT_FOUND(Codes.MEASURE_IS_NOT_FOUND,
            paramNames(MEASURE_ID)),
    FILTER_VALIDATION_ERROR(Codes.FILTER_VALIDATION_ERROR,
            null),
    EXPRESSION_IS_INVALID(Codes.EXPRESSION_IS_INVALID,
            paramNames(FIELD_ID, FIELD_EXPRESSION, ERROR_MESSAGE)),
    EXPRESSION_HAS_UNDECLARED_VARIABLE(Codes.EXPRESSION_HAS_UNDECLARED_VARIABLE,
            paramNames(EXPRESSION, VAR_NAME)),
    MULTI_AXIS_QUERY_IS_MISSING_ROW_AXIS(Codes.MULTI_AXIS_QUERY_IS_MISSING_ROW_AXIS, null),
    MULTI_AXIS_QUERY_IS_MISSING_COLUMN_AXIS(Codes.MULTI_AXIS_QUERY_IS_MISSING_COLUMN_AXIS, null),
    MULTI_AXIS_QUERY_AXIS_HAS_WRONG_TYPE(Codes.MULTI_AXIS_QUERY_AXIS_HAS_WRONG_TYPE,
            paramNames(AXIS_NAME, AXIS_TYPE)),
    MULTI_AXIS_QUERY_IS_MISSING_DIMENSION(Codes.MULTI_AXIS_QUERY_IS_MISSING_DIMENSION,
            paramNames(AXIS_NAME,
            DIMENSION_NUMBER, DIMENSION_COUNT)),
    MULTI_AXIS_QUERY_HAS_BAD_FIELD(Codes.MULTI_AXIS_QUERY_HAS_BAD_FIELD,
            paramNames(AXIS_NAME,
            DIMENSION_NUMBER, DIMENSION_COUNT)),
    MULTI_AXIS_QUERY_ADHOC_DIMENSION_HAS_MULTIPLE_LEVELS(Codes.MULTI_AXIS_QUERY_ADHOC_DIMENSION_HAS_MULTIPLE_LEVELS,
            paramNames(AXIS_NAME, DIMENSION_NUMBER, DIMENSION_COUNT, DIMENSION_LEVEL_COUNT)),
    MULTI_AXIS_QUERY_ADHOC_DIMENSION_HAS_NO_LEVELS(Codes.MULTI_AXIS_QUERY_ADHOC_DIMENSION_HAS_NO_LEVELS,
            paramNames(AXIS_NAME, DIMENSION_NUMBER, DIMENSION_COUNT)),
    MULTI_AXIS_QUERY_DIMENSION_HAS_MULTIPLE_LEVELS(Codes.MULTI_AXIS_QUERY_DIMENSION_HAS_MULTIPLE_LEVELS,
            paramNames(AXIS_NAME, DIMENSION_NUMBER, DIMENSION_COUNT, DIMENSION_LEVEL_COUNT)),
    MULTI_AXIS_QUERY_DIMENSION_HAS_NO_LEVELS(Codes.MULTI_AXIS_QUERY_DIMENSION_HAS_NO_LEVELS,
            paramNames(AXIS_NAME, DIMENSION_NUMBER, DIMENSION_COUNT, DIMENSION_LEVEL_COUNT)),
    MULTI_AXIS_QUERY_DIMENSION_HAS_BAD_UNIQUE_NAME(Codes.MULTI_AXIS_QUERY_DIMENSION_HAS_BAD_UNIQUE_NAME,
            paramNames(AXIS_NAME, DIMENSION_NUMBER, DIMENSION_COUNT, FIELD_UNIQUE_NAME)),
    MULTI_AXIS_QUERY_DIMENSION_HAS_BAD_LEVEL_NAME(Codes.MULTI_AXIS_QUERY_DIMENSION_HAS_BAD_LEVEL_NAME,
            paramNames(AXIS_NAME, DIMENSION_NUMBER, DIMENSION_COUNT, FIELD_UNIQUE_NAME)),
    MULTI_LEVEL_QUERY_MEASURE_HAS_WRONG_TYPE(Codes.MULTI_LEVEL_QUERY_MEASURE_HAS_WRONG_TYPE,
            paramNames(FIELD_TYPE)),
    FLAT_QUERY_GROUP_BY_HAS_WRONG_TYPE(Codes.FLAT_QUERY_GROUP_BY_HAS_WRONG_TYPE,
            paramNames(FIELD_TYPE)),
    FLAT_QUERY_MEASURE_HAS_WRONG_TYPE(Codes.FLAT_QUERY_MEASURE_HAS_WRONG_TYPE,
            paramNames(FIELD_TYPE)),
    PARAMETER_NAME_METADATA_FIELD_COLLISION_ERROR(Codes.PARAMETER_NAME_METADATA_FIELD_COLLISION_ERROR,
            paramNames(METADATA_FIELD_NAMES_COLLISION_PARAMS)),
    PATH_NOT_INCLUDE_ALL_MEMBERS(Codes.PATH_NOT_INCLUDE_ALL_MEMBERS,
            paramNames(INVALID_TRANSFORMATION_PATH, AVAILABLE_TRANSFORMATION_PATHS)),
    QUERY_LEVEL_EXPANSION_AMBIGUOUS_REFERENCE(Codes.QUERY_LEVEL_EXPANSION_AMBIGUOUS_REFERENCE,
            paramNames(AMBIGUOUS_REFS)),
    QUERY_EXPANSION_LEVEL_REFERENCE_NOT_VALID(Codes.QUERY_EXPANSION_LEVEL_REFERENCE_NOT_VALID,
            paramNames(REFERENCE)),
    QUERY_PARAMETER_NAME_QUERY_FIELD_ID_COLLISION_ERROR(Codes.QUERY_PARAMETER_NAME_QUERY_FIELD_ID_COLLISION_ERROR,
            paramNames(DUPLICATED_IDS)),
    QUERY_REFERENCE_INTEGRITY_ERROR(Codes.QUERY_REFERENCE_INTEGRITY_ERROR,
            null),
    QUERY_FILTER_REFERENCE_CLASS_ERROR(Codes.QUERY_FILTER_REFERENCE_CLASS_ERROR,
            paramNames(FIELD_NAME, UNSUPPORTED_TYPE)),
    QUERY_ORDER_GENERIC_REFERENCE_CLASS_ERROR(Codes.QUERY_ORDER_GENERIC_REFERENCE_CLASS_ERROR,
            paramNames(FIELD_NAME, UNSUPPORTED_TYPE)),
    QUERY_ORDER_HEADER_NULL_REFERENCE_ERROR(Codes.QUERY_ORDER_HEADER_NULL_REFERENCE_ERROR,
            null),
    QUERY_ORDER_HEADER_REFERENCE_CLASS_ERROR(Codes.QUERY_ORDER_HEADER_REFERENCE_CLASS_ERROR,
            paramNames(FIELD_NAME, UNSUPPORTED_TYPE)),
    QUERY_EXPANSION_HEADER_NULL_REFERENCE_ERROR(Codes.QUERY_EXPANSION_HEADER_NULL_REFERENCE_ERROR,
            null),
    QUERY_EXPANSION_LEVEL_REFERENCE_CLASS_ERROR(Codes.QUERY_EXPANSION_LEVEL_REFERENCE_CLASS_ERROR,
            paramNames(FIELD_NAME, UNSUPPORTED_TYPE)),
    QUERY_AGGREGATED_FIELD_REFERENCE_CLASS_ERROR(Codes.QUERY_AGGREGATED_FIELD_REFERENCE_CLASS_ERROR,
            paramNames(FIELD_NAME, UNSUPPORTED_TYPE)),
    MULTIPLE_GROUP_BY_AGGREGATIONS(Codes.MULTIPLE_GROUP_BY_AGGREGATIONS,
            null),
    QUERY_SELECT_ILLEGAL_FIELDS_STATE(Codes.QUERY_SELECT_ILLEGAL_FIELDS_STATE,
            null),
    QUERY_DISTINCT_FIELDS_AND_GROUP_BY_NOT_ALLOWED_FOR_CALCULATED_FIELD_WITH_AGGREGATE(Codes.QUERY_DISTINCT_FIELDS_AND_GROUP_BY_NOT_ALLOWED_OVER_CALCULATED_FIELD_WITH_AGGREGATE, paramNames(FIELD_ID, FIELD_EXPRESSION));

    private String code;
    private String[] paramNames;

    private static final Map<String, QueryExecutionsErrorCode> stringToEnum = new HashMap<String, QueryExecutionsErrorCode>();
    static {
        for (QueryExecutionsErrorCode code : values())
            stringToEnum.put(code.toString(), code);
    }

    QueryExecutionsErrorCode(String code, String[] paramNames) {
        this.code = code;
        this.paramNames = paramNames;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String[] getParamNames() {
        return this.paramNames;
    }

    @Override
    public ErrorDescriptor createDescriptor(Object... propValues) {
        return new ErrorDescriptor().setErrorCode(this.code)
                .setProperties(buildProperties(paramNames, propValues));
    }

    @Override
    public String toString() {
        return this.code;
    }

    public static ErrorDescriptor createDescriptorFrom(String code, Object... propValues) {
        return createDescriptorFrom(code, null, propValues);
    }

    public static ErrorDescriptor createDescriptorFrom(String code, String message, Object[] propValues) {
        if (stringToEnum.get(code) == null) {
            throw new IllegalArgumentException("Ad Hoc query execution error code " + code + " is not defined");
        }
        return stringToEnum.get(code).createDescriptor(propValues).setMessage(message);
    }


    public interface Codes {
        String QUERY_EXECUTION_NOT_FOUND = "query.execution.not.found";
        String QUERY_EXECUTION_REJECTED = "query.execution.rejected";
        String QUERY_DATASOURCE_NOT_FOUND = "query.datasource.not.found";
        String QUERY_DATASOURCE_TYPE_NOT_SUPPORTED = "query.datasource.type.not.supported";
        String QUERY_DATASOURCE_ACCESS_DENIED = "query.datasource.access.denied";
        String QUERY_IN_MEMORY_DATASOURCE_TYPE_NOT_SUPPORTED = "query.in.memory.datasource.type.not.supported";
        String QUERY_FILTER_IS_NOT_VALID = "query.filter.is.not.valid";
        String DATASOURCE_CONNECTION_FIELD = "datasource.connection.failed";
        String CALCULATED_FIELD_EXPRESSION_EVALUATION_ERROR = "calculated.field.expression.evaluation.error";
        String QUERY_EXECUTION_CANCELLED = "query.execution.cancelled";
        String QUERY_METADATA_FIELDS_NOT_FOUND = "query.metadata.fields.not.found";
        String QUERY_DATASOURCE_INVALID = "query.datasource.invalid";
        String QUERY_AGGREGATE_DEFINITION_ERROR = "query.aggregate.definition.error";
        String QUERY_FIELD_IDS_COLLISION = "query.field.ids.collision";
        String QUERY_FIELD_IDS_METADATA_FIELD_COLLISION = "query.field.ids.metadata.field.collision";
        String QUERY_FIELDS_FROM_MULTIPLE_DATAISLANDS = "query.fields.from.multiple.dataislands";
        String QUERY_EXPRESSION_OPERANDS_SIZE_OF_BOUND = "query.expression.operands.size.out.of.bound";
        String QUERY_WHERE_EXPRESSION_TYPE_NOT_SUPPORTED = "query.where.expression.type.not.supported";
        String QUERY_AGGREGATION_EXPRESSION_NOT_VALID = "query.aggregation.expression.not.valid";
        String QUERY_CALCULATED_FIELD_EXPRESSION_NOT_VALID = "query.calculated.field.expression.not.valid";
        String QUERY_AGGREGATE_EXPRESSION_NOT_VALID = "query.aggregate.expression.not.valid";
        String QUERY_FIELD_EXPRESSION_NOT_VALID = "query.field.expression.not.valid";
        String QUERY_AGGREGATION_FUNCTION_NAME_NOT_VALID = "query.aggregation.function.name.not.valid";
        String QUERY_GROUPBY_ALLGROUP_NOT_FIRST = "query.groupBy.allGroup.not.first";
        String QUERY_GROUPBY_CATEGORIZER_NOT_VALID = "query.groupBy.categorizer.not.valid";
        String QUERY_MULTI_AXIS_AGGREGATION_IS_EMPTY = "query.multiAxis.aggregation.is.empty";
        String QUERY_INVALID_STRUCTURE = "query.invalid.structure";
        String QUERY_ORDERBY_TOP_OR_BOTTOM_LIMIT_NOT_VALID = "query.orderBy.topOrBottom.limit.not.valid";
        String QUERY_WHERE_PARAMETERS_EXPRESSION_NOT_VALID = "query.where.parameters.expression.not.valid";
        String QUERY_DETAILS_UNSUPPORTED = "query.details.unsupported";
        String QUERY_TRANSFORMATION_TOPN_NOT_ALLOWED_AGGREGATIONS_IN_ROWS = "query.transformation.topn.not.allowed.aggregations.in.rows";
        String EXPRESSION_REPRESENTATION_REQUIRED = "expression.representation.required";
        String QUERY_ORDERBY_LEVEL_REFERENCE_NOT_VALID = "query.orderBy.level.reference.not.valid";
        String QUERY_LIMT_OUT_OF_RANGE = "query.limit.out.of.range";
        String PAGING_AXIS_COUNT_OUT_OF_RANGE = "paging.axis.count.out.of.range";
        String PAGING_OFFSET_OUT_OF_RANGE = "paging.offset.out.of.range";
        String PAGING_PAGE_SIZE_OUT_OF_RANGE = "paging.page.size.out.of.range";
        String QUERY_TIMEBALANCE_INVALID_AGGREGATE_FUNCTION = "query.timebalance.invalid.aggregate.function";
        String QUERY_TIMEBALANCE_NO_DATE_FIELD = "query.timebalance.no.datefield";
        String QUERY_TIMEBALANCE_CANNOT_BE_APPLIED = "query.timebalance.cannot.be.applied";
        String QUERY_TIMEBALANCE_MULTIPLE_DATE_FIELD = "query.timebalance.multiple.datefield";
        String QUERY_FIELD_AGGREGATE_FUNCTION_IS_UNKNOWN = "query.field.aggregate.function.is.unknown";
        String QUERY_FIELD_EXPRESSION_PARSE_ERROR = "query.field.expression.parse.error";
        String QUERY_FIELD_AGGREGATE_EXPRESSION_PARSE_ERROR = "query.field.aggregate.expression.parse.error";
        String QUERY_WHERE_PARAMETERS_NAME_IS_NOT_VALID = "query.where.parameters.name.is.not.valid";
        //Is not documented yet.
        String QUERY_WHERE_PARAMETERS_EXPRESSION_TYPE_NOT_VALID = "query.where.parameters.expression.type.not.valid";
        //Error Codes, that not appears in REST API and is not documented:
        String FIELD_OR_MEASURE_IS_NOT_FOUND = "field.or.measure.is.not.found";
        String FIELD_NAME_IS_NULL = "field.name.is.null";
        String FIELD_IS_NOT_FOUND = "field.is.not.found";
        String CALC_FIELD_IS_NOT_FOUND = "calc.field.is.not.found";
        String CALC_FIELD_EXPRESSION_IS_NULL = "calc.field.expression.is.null";
        String CALC_FIELD_EXPRESSION_PARSE_ERROR = "calc.field.expression.parse.error";
        String CALC_FIELD_AGGREGATE_EXPRESSION_IS_INVALID = "calc.field.aggregate.expression.is.invalid";
        String CALC_FIELD_IS_MISSING_KIND_PROPERTY = "calc.field.is.missing.kind.property";
        String ADHOC_FIELD_AGGREGATE_FUNCTION_AGGREGATE_FORMULA_IS_NOT_APPLICABLE = "query.field.aggregate.function.aggregate.formula.is.not.applicable";
        String ADHOC_FIELD_AGGREGATE_EXPRESSION_IS_NULL = "query.field.aggregate.expression.is.null";
        String SORT_FIELD_HAS_BAD_FIELD_OR_MEASURE_NAME = "sort.field.has.bad.field.or.measure.name";
        String SORT_FIELD_HAS_BAD_MEASURE_INDEX = "sort.field.has.bad.measure.index";
        String MEASURE_HAS_BAD_NAME = "measure.has.bad.name";
        String MEASURE_IS_NOT_FOUND = "measure.is.not.found";
        String TIMEBALANCE_CANNOT_REMOVE_DATEFIELD_ERROR = "query.timebalance.cannot.remove.datefield";
        String FILTER_VALIDATION_ERROR = "filter.validation.error";
        String EXPRESSION_IS_INVALID = "expression.is.invalid";
        String EXPRESSION_HAS_UNDECLARED_VARIABLE = "expression.has.undeclared.variable";
        String MULTI_AXIS_QUERY_IS_MISSING_ROW_AXIS = "multi.axis.query.is.missing.row.axis";
        String MULTI_AXIS_QUERY_IS_MISSING_COLUMN_AXIS = "multi.axis.query.is.missing.column.axis";
        String MULTI_AXIS_QUERY_AXIS_HAS_WRONG_TYPE = "multi.axis.query.axis.has.wrong.type";
        String MULTI_AXIS_QUERY_IS_MISSING_DIMENSION = "multi.axis.query.is.missing.dimension";
        String MULTI_AXIS_QUERY_HAS_BAD_FIELD = "multi.axis.query.has.bad.field";
        String MULTI_AXIS_QUERY_ADHOC_DIMENSION_HAS_MULTIPLE_LEVELS = "multi.axis.query.adhoc.dimension.has.multiple.levels";
        String MULTI_AXIS_QUERY_ADHOC_DIMENSION_HAS_NO_LEVELS = "multi.axis.query.adhoc.dimension.has.no.levels";
        String MULTI_AXIS_QUERY_DIMENSION_HAS_MULTIPLE_LEVELS = "multi.axis.query.dimension.has.multiple.levels";
        String MULTI_AXIS_QUERY_DIMENSION_HAS_NO_LEVELS = "multi.axis.query.dimension.has.no.levels";
        String MULTI_AXIS_QUERY_DIMENSION_HAS_BAD_UNIQUE_NAME = "multi.axis.query.dimension.has.bad.unique.name";
        String MULTI_AXIS_QUERY_DIMENSION_HAS_BAD_LEVEL_NAME = "multi.axis.query.dimension.has.bad.level.name";
        String MULTI_LEVEL_QUERY_MEASURE_HAS_WRONG_TYPE = "multi.level.query.measure.has.wrong.type";
        String FLAT_QUERY_GROUP_BY_HAS_WRONG_TYPE = "flat.query.group.by.has.wrong.type";
        String FLAT_QUERY_MEASURE_HAS_WRONG_TYPE = "flat.query.measure.has.wrong.type";
        String PARAMETER_NAME_METADATA_FIELD_COLLISION_ERROR = "query.parameter.name.metadata.field.collision";
        String PATH_NOT_INCLUDE_ALL_MEMBERS ="transformation.path.not.include.all.members";
        String QUERY_LEVEL_EXPANSION_AMBIGUOUS_REFERENCE = "query.expansion.level.ambiguous.reference.error";
        String QUERY_EXPANSION_LEVEL_REFERENCE_NOT_VALID = "query.expansion.level.reference.not.valid";
        String QUERY_PARAMETER_NAME_QUERY_FIELD_ID_COLLISION_ERROR = "query.parameter.name.query.field.id.collision";
        String QUERY_REFERENCE_INTEGRITY_ERROR = "query.reference.integrity.error";
        String QUERY_FILTER_REFERENCE_CLASS_ERROR = "query.filter.reference.class.error";
        String QUERY_ORDER_GENERIC_REFERENCE_CLASS_ERROR = "query.order.generic.reference.class.error";
        String QUERY_ORDER_HEADER_NULL_REFERENCE_ERROR = "query.order.header.null.reference.error";
        String QUERY_ORDER_HEADER_REFERENCE_CLASS_ERROR = "query.order.header.reference.class.error";
        String QUERY_EXPANSION_HEADER_NULL_REFERENCE_ERROR = "query.expansion.header.null.reference.error";
        String QUERY_EXPANSION_LEVEL_REFERENCE_CLASS_ERROR = "query.expansion.level.reference.class.error";
        String QUERY_AGGREGATED_FIELD_REFERENCE_CLASS_ERROR = "query.aggregated.field.reference.class.error";
        String MULTIPLE_GROUP_BY_AGGREGATIONS = "parameter.groupBy.multiple.aggregations.error";
        String QUERY_SELECT_ILLEGAL_FIELDS_STATE = "query.select.illegal.fields.state";
        String QUERY_DISTINCT_FIELDS_AND_GROUP_BY_NOT_ALLOWED_OVER_CALCULATED_FIELD_WITH_AGGREGATE = "query.distinctfields.and.groupBy.not.allowed.for.calcfield.with.aggregate";
    }
}
