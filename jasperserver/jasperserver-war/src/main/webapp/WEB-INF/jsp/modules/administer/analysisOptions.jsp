<%@ page contentType="text/html; charset=utf-8" %>
<%--
  ~ Copyright (C) 2025-2026 the Jasper Server OS Authors
  ~ SPDX-License-Identifier: AGPL-3.0-or-later
  ~ Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
  ~ http://www.jaspersoft.com.
  ~
  ~ Unless you have purchased a commercial license agreement from Jaspersoft,
  ~ the following license terms apply:
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program. If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ page import="mondrian.olap.MondrianProperties" %>
<%@ page import="org.springframework.web.servlet.support.*" %>
<%@ page import="org.springframework.context.*" %>
<%@ page import="com.jaspersoft.ji.license.LicenseManager" %>
<%@ page import="com.jaspersoft.ji.util.profiling.api.GlobalProfilingState" %>
<%@ page import="com.jaspersoft.ji.adhoc.config.AdhocConfiguration" %>
<%@ page import="com.jaspersoft.jasperserver.war.action.EditMondrianPropertiesAction" %>

<%!
    private String[] options = {"OFF", "WARN", "ERROR"};;
    private String[] solvedModes = {"ABSOLUTE", "SCOPED"};
    private LicenseManager licenseManager;
    private MondrianProperties mProperties;
    private AdhocConfiguration adhocConfiguration;
    private ApplicationContext context;
    private GlobalProfilingState globalProfilingState;
%><%
    context = RequestContextUtils.findWebApplicationContext(request);
    globalProfilingState = (GlobalProfilingState) context.getBean("globalProfilingState");
    adhocConfiguration = context.getBean("adhocConfiguration", AdhocConfiguration.class);
    mProperties = MondrianProperties.instance();
    licenseManager = LicenseManager.getInstance();

%>

<c:set var="analyticFeatureSupported" value="<%=licenseManager.isAnalysisFeatureSupported()%>"/>
<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="showServerSettingsHeader" value="true"/>
    <c:if test="${analyticFeatureSupported}">
        <t:putAttribute name="pageTitle"><spring:message code="menu.mondrian.properties"/></t:putAttribute>
        <t:putAttribute name="bodyID" value="analysisOptions"/>
        <t:putAttribute name="bodyClass" value="twoColumn"/>
        <t:putAttribute name="moduleName" value="administer/administerAdhocOptionsMain"/>
        <t:putAttribute name="headerContent">
            <%@ include file="administerState.jsp" %>
        </t:putAttribute>
        <t:putAttribute name="bodyContent">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerID" value="settings"/>
                <t:putAttribute name="containerClass" value="column decorated primary"/>
                    <button id="flushOLAPCache" class="button capsule text up"><span class="wrap"><spring:message code="menu.flush.olap.cache"/></span></button>
                <t:putAttribute name="bodyClass" value=""/>
                <t:putAttribute name="bodyContent">
					<div class="title"><spring:message code="menu.mondrian.properties"/></div>

                    <ol class="list settings">
                        <li class="node">
                            <div class="wrap">
                                <h2 class="title settingsGroup"><spring:message code="menu.general"/></h2>
                            </div>
                            <ol class="list settings">

                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="ji.profiling.enabled"/>
                                    <jsp:param name="oLabelCode" value= "JAM_003_PROFILING_ENABLED"/>
                                    <jsp:param name="oValue" value="<%=globalProfilingState.isProfilingEnabled()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.DisableCaching.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_005_DISABLE_MEMORY_CACHE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.DisableCaching.get()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableDrillThrough.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_070_ENABLE_DRILL_THROUGH"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableDrillThrough.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.GenerateFormattedSql.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_006_GENERATE_FORMATTED_SQL"/>
                                    <jsp:param name="oValue" value="<%=mProperties.GenerateFormattedSql.get()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.CaseSensitive.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_024_CASE_SENSITIVE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.CaseSensitive.get()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.CompareSiblingsByOrderKey.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_023_COMPARE_SIBLINGS_BY_ORDER_KEY"/>
                                    <jsp:param name="oValue" value="<%=mProperties.CompareSiblingsByOrderKey.get()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.IgnoreInvalidMembers.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_025_IGNORE_INVALID_MEMBERS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.IgnoreInvalidMembers.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.IgnoreInvalidMembersDuringQuery.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_026_IGNORE_INVALID_MEMBERS_DURING_QUERY"/>
                                    <jsp:param name="oValue" value="<%=mProperties.IgnoreInvalidMembersDuringQuery.get()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableInMemoryRollup.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_064_ENABLE_IN_MEMORY_ROLLUP"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableInMemoryRollup.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.IgnoreMeasureForNonJoiningDimension.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_033_IGNORE_MEASURE_FOR_NON_JOINING_PREFIX"/>
                                    <jsp:param name="oValue" value="<%=mProperties.IgnoreMeasureForNonJoiningDimension.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.NeedDimensionPrefix.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_032_NEED_DIMENSION_PREFIX"/>
                                    <jsp:param name="oValue" value="<%=mProperties.NeedDimensionPrefix.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.NullDenominatorProducesNull.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_031_NULL_OR_ZERO_DENOMINATOR_PRODUCES_NULL"/>
                                    <jsp:param name="oValue" value="<%=mProperties.NullDenominatorProducesNull.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="adhoc.olap.maxFilterValues"/>
                                    <jsp:param name="oLabelCode" value= "JAM_071_MAX_FILTER_COUNT"/>
                                    <jsp:param name="oValue" value="<%=adhocConfiguration.getAdhocOlapMaxFilters()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.QueryLimit.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_009_QUERY_LIMIT"/>
                                    <jsp:param name="oValue" value="<%=mProperties.QueryLimit.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.ResultLimit.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_010_RESULT_LIMIT"/>
                                    <jsp:param name="oValue" value="<%=mProperties.ResultLimit.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.RolapConnectionShepherdNbThreads.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_066_MAX_QUERY_THREADS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.RolapConnectionShepherdNbThreads.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.RolapConnectionShepherdThreadPollingInterval.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_067_SHEPHERD_THREAD_POLLING_INTERVAL"/>
                                    <jsp:param name="oValue" value="<%=mProperties.RolapConnectionShepherdThreadPollingInterval.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.MaxEvalDepth.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_022_EVALUATE_MAX_EVAL_DEPTH"/>
                                    <jsp:param name="oValue" value="<%=mProperties.MaxEvalDepth.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.ExpCompilerClass.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_052_EXP_CALC_CLASS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.ExpCompilerClass.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.CellBatchSize.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_063_CELL_BATCH_SIZE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.CellBatchSize.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.QueryTimeout.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_025_QUERY_TIMEOUT"/>
                                    <jsp:param name="oValue" value="<%=mProperties.QueryTimeout.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.HighCardChunkSize.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_055_HIGH_CARD_CHUNK_SIZE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.HighCardChunkSize.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.SparseSegmentDensityThreshold.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_012_SPARSE_DENSITY_THRESHOLD"/>
                                    <jsp:param name="oValue" value="<%=mProperties.SparseSegmentDensityThreshold.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.SparseSegmentCountThreshold.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_013_SPARSE_COUNT_THRESHOLD"/>
                                    <jsp:param name="oValue" value="<%=mProperties.SparseSegmentCountThreshold.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.NullMemberRepresentation.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_027_NULL_MEMBER_REPRESENTATION"/>
                                    <jsp:param name="oValue" value="<%=mProperties.NullMemberRepresentation.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.IterationLimit.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_028_ITERATION_LIMIT"/>
                                    <jsp:param name="oValue" value="<%=mProperties.IterationLimit.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.CrossJoinOptimizerSize.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_029_OPTIMIZER_SIZE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.CrossJoinOptimizerSize.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.StatisticsProviders.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_069_STATISTICS_PROVIDER"/>
                                    <jsp:param name="oValue" value="<%=mProperties.StatisticsProviders.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateList.jsp" flush="true">
                                    <jsp:param name="oName" value="<%=mProperties.SolveOrderMode.getPath()%>"/>
                                    <jsp:param name="oDesc" value="<%=mProperties.SolveOrderMode.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value="JAM_030_SOLVE_ORDER_MODE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.SolveOrderMode.get()%>"/>
                                    <jsp:param name="oSelectOptions" value="<%=solvedModes%>"/>
                                </jsp:include>


                            </ol>
                        </li>

                        <li class="node">
                            <div class="wrap">
                                <h2 class="title settingsGroup"><spring:message code="JAM_014_AGGREGATE_SECTION"/></h2>
                            </div>


                            <ol class="list settings">
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.UseAggregates.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_014_ENABLE_AGGREGATES"/>
                                    <jsp:param name="oValue" value="<%=mProperties.UseAggregates.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.ReadAggregates.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_072_READ_AGGREGATE_TABLES"/>
                                    <jsp:param name="oValue" value="<%=mProperties.ReadAggregates.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.ChooseAggregateByVolume.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_015_CHOOSE_AGGREGATES_BY_VOL"/>
                                    <jsp:param name="oValue" value="<%=mProperties.ChooseAggregateByVolume.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.OptimizePredicates.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_016_AGGREGATES_OPTIMIZE_PREDICATES"/>
                                    <jsp:param name="oValue" value="<%=mProperties.OptimizePredicates.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.GenerateAggregateSql.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_019_AGGREGATES_GENERATE_SQL"/>
                                    <jsp:param name="oValue" value="<%=mProperties.GenerateAggregateSql.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.AggregateRules.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_017_AGGREGATES_RULES"/>
                                    <jsp:param name="oValue" value="<%=mProperties.AggregateRules.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.AggregateRuleTag.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_018_AGGREGATES_RULE_TAG"/>
                                    <jsp:param name="oValue" value="<%=mProperties.AggregateRuleTag.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.JdbcFactoryClass.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_020_AGGREGATES_JDBC_FACTORY_CLASS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.JdbcFactoryClass.get()%>"/>
                                </jsp:include>

                            </ol>
                        </li>

                        <li class="node">
                            <div class="wrap">
                                <h2 class="title settingsGroup"><spring:message code="JAM_034_CACHING_SECTION"/></h2>
                            </div>
                            <ol class="list settings">
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableExpCache.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_035_EXP_CACHE_ENABLE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableExpCache.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableRolapCubeMemberCache.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_036_ENABLE_CUBE_MEMBER_CACHE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableRolapCubeMemberCache.get()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.DisableLocalSegmentCache.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_062_DISABLE_LOCAL_SEGMENT_CACHE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.DisableLocalSegmentCache.get()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableNativeCrossJoin.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_037_NATIVE_CROSS_JOIN_ENABLE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableNativeCrossJoin.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableNativeTopCount.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_038_NATIVE_TOP_COUNT_ENABLE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableNativeTopCount.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableNativeFilter.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_039_NATIVE_FILTER_ENABLE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableNativeFilter.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableNativeNonEmpty.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_040_NATIVE_NON_EMPTY_ENABLE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableNativeNonEmpty.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.ExpandNonNative.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_053_EXPAND_NON_NATIVE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.ExpandNonNative.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.FilterChildlessSnowflakeMembers.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_065_FILTER_CHILD_LESS_SNOW_FLAKE_MEMBERS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.FilterChildlessSnowflakeMembers.get()%>"/>
                                </jsp:include>
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableGroupingSets.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_041_GROUPING_SETS_ENABLE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableGroupingSets.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.SegmentCache.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_059_SEGMENT_CACHE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.SegmentCache.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.SegmentCacheManagerNumberCacheThreads.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_060_MAX_CACHE_THREADS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.SegmentCacheManagerNumberCacheThreads.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.SegmentCacheManagerNumberSqlThreads.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_061_MAX_SQL_THREADS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.SegmentCacheManagerNumberSqlThreads.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.MaxConstraints.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_054_MAX_CONSTRAINTS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.MaxConstraints.get()%>"/>
                                </jsp:include>
                                <c:set var="oName" value="<%=mProperties.AlertNativeEvaluationUnsupported.getPath()%>"/>
                                <jsp:include page="templateList.jsp" flush="true">
                                    <jsp:param name="oName" value="${oName}"/>
                                    <jsp:param name="oDesc" value="<%=mProperties.SolveOrderMode.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value="JAM_048_NATIVE_UNSUPPORTED_ALERT"/>
                                    <jsp:param name="oValue" value="<%=mProperties.AlertNativeEvaluationUnsupported.get()%>"/>
                                    <jsp:param name="oSelectOptions" value="<%=options%>"/>
                                </jsp:include>



                            </ol>
                        </li>

                        <li class="node">
                            <div class="wrap">
                                <h2 class="title settingsGroup"><spring:message code="JAM_043_XMLA_SECTION"/></h2>
                            </div>
                            <ol class="list settings">
								<jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.EnableTotalCount.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_042_XMLA_DRILL_THROUGH_TOTAL_COUNT_ENABLE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.EnableTotalCount.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.MaxRows.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_004_XMLA_MAX_DRILL_THROUGH"/>
                                    <jsp:param name="oValue" value="<%=mProperties.MaxRows.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.XmlaSchemaRefreshInterval.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_068_SCHEMA_REFRESH_INTERVAL"/>
                                    <jsp:param name="oValue" value="<%=mProperties.XmlaSchemaRefreshInterval.get()%>"/>
                                </jsp:include>

                                        </ol>
                                                  </li>
                                                  <li class="node">
                                                      <div class="wrap">
                                                          <h2 class="title settingsGroup"><spring:message code="JAM_044_MEMORY_MONITOR_SECTION"/></h2>
                                                      </div>
                                                      <ol class="list settings">
                                <jsp:include page="templateCheckbox.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.MemoryMonitor.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_045_MEMORY_MONITOR_ENABLE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.MemoryMonitor.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.MemoryMonitorThreshold.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_046_MEMORY_MONITOR_THRESHOLD"/>
                                    <jsp:param name="oValue" value="<%=mProperties.MemoryMonitorThreshold.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.ExecutionHistorySize.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_058_MONITOR_EXECUTION_HISTORY_SIZE"/>
                                    <jsp:param name="oValue" value="<%=mProperties.ExecutionHistorySize.get()%>"/>
                                </jsp:include>
                                <jsp:include page="templateInputText.jsp" flush="true">
                                    <jsp:param name="oDesc" value="<%=mProperties.MemoryMonitorClass.getPath()%>"/>
                                    <jsp:param name="oLabelCode" value= "JAM_047_MEMORY_MONITOR_CLASS"/>
                                    <jsp:param name="oValue" value="<%=mProperties.MemoryMonitorClass.get()%>"/>
                                </jsp:include>


                            </ol>
                        </li>
                    </ol>

                </t:putAttribute>
                <t:putAttribute name="footerContent">
                </t:putAttribute>
            </t:insertTemplate>

            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerID" value="serverSettingsMenu"/>
                <t:putAttribute name="containerClass" value="column decorated secondary sizeable"/>
                <t:putAttribute name="containerElements">
                    <div class="sizer horizontal"></div>
                    <button class="button minimize"></button>
                </t:putAttribute>
                <t:putAttribute name="containerTitle"><spring:message code="menu.settings"/></t:putAttribute>
                <t:putAttribute name="bodyClass" value=""/>
                <t:putAttribute name="bodyContent">

                    <!--
                       NOTE: these objects serve as navigation links, load respective pages
                     -->
                    <ul class="list responsive filters">
						<li class="leaf"><p class="wrap button" id="navGeneralSettings"><b class="icon"></b><spring:message code="menu.general"/></p></li>
                        <li class="leaf"><p class="wrap button" id="navLogSettings"><b class="icon"></b><spring:message code="menu.log.Settings"/></p></li>
                        <li class="leaf"><p class="wrap button" id="logCollectors"><b class="icon"></b><spring:message code="logCollectors.title"/></p></li>
                        <li class="leaf"><p class="wrap button" id="navDesignerOptions"><b class="icon"></b><spring:message code="menu.adhoc.options"/></p></li>
                        <li class="leaf"><p class="wrap button" id="navDesignerCache"><b class="icon"></b><spring:message code="menu.adhoc.cache"/></p></li>
                        <li class="leaf selected"><p class="wrap button" id="navAnalysisOptions"><b class="icon"></b><spring:message code="menu.mondrian.properties"/></p></li>
                        <li class="leaf"><p class="wrap button" id="navAwsSettings"><b class="icon"></b><spring:message code="menu.aws.settings"/></p></li>
                        <li class="leaf"><p class="wrap button" id="navCustomAttributes"><b class="icon"></b><spring:message code="menu.server.attributes"/></p></li>
                        <li class="leaf"><p class="wrap button" id="navResetSettings"><b class="icon"></b><spring:message code="menu.edit.settings"/></p></li>
                        <li class="leaf"><p class="wrap button" id="navImport"><b class="icon"></b><spring:message code="import.import"/></p></li>
                        <li class="leaf"><p class="wrap button" id="navExport"><b class="icon"></b><spring:message code="export.export"/></p></li>
                        <li class="leaf" disabled="disabled"><p class="wrap separator" href="#"><b class="icon"></b></p></li>
                    </ul>
                </t:putAttribute>
                <t:putAttribute name="footerContent">
                </t:putAttribute>
            </t:insertTemplate>
        </t:putAttribute>
    </c:if>
    <c:if test="${!analyticFeatureSupported}">
        <t:putAttribute name="pageTitle"><spring:message code='LIC_019_license.failed'/></t:putAttribute>
        <t:putAttribute name="bodyID" value="licenseError"/>
        <t:putAttribute name="bodyClass" value="oneColumn"/>

        <t:putAttribute name="bodyContent">
            <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
                <t:putAttribute name="containerClass" value="column decorated primary noHeader"/>
                <t:putAttribute name="containerTitle"><spring:message code='LIC_019_license.failed'/></t:putAttribute>
                <t:putAttribute name="bodyClass" value="flow"/>
                <t:putAttribute name="bodyContent">
                <div id="flowControls">

                </div>
                    <div id="stepDisplay">
                        <fieldset class="row instructions">
                            <h2 class="textAccent02"><spring:message code='LIC_014_feature.not.licensed'/></h2>
                            <h4><spring:message code='LIC_020_licence.contact.support'/></h4>
                        </fieldset>
                    </div>
                </t:putAttribute>
            </t:insertTemplate>
        </t:putAttribute>
    </c:if>


</t:insertTemplate>
