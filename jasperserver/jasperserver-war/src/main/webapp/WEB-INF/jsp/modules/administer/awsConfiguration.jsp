<%@ page contentType="text/html; charset=utf-8" %>
<%--
  ~ Copyright (C) 2025 the Jasper Server OS Authors
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
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page import="com.jaspersoft.ji.license.ProInstanceProductTypeResolver" %>
<%@ page import="org.mvel2.ast.Proto" %>

<style type="text/css">
    h2, h3 	{
        margin:6px 0px 0px 14px;
    }

    p.intro			{margin-left:12px;}
    p.preList		{margin-bottom:6px}
    p:last-child	{margin-bottom:0;}

    .instructions {
        width:100%;
        margin:24px 0;
        border-top: 1px solid #f1f1f1;
    }

    .instructions th {
        text-align: left;
        padding:8px 8px 8px 16px;
        border-bottom: 1px solid #f1f1f1;
        border-right: 1px solid #f1f1f1;
    }

    .instructions td 			{padding:8px 8px 8px 0; border-bottom:1px solid  #f1f1f1;}
    .instructions ul			{margin-top: 0; list-style-type: none;}
    .instructions ul li 		{margin-bottom: 6px;}
    .instructions ul li ul 		{margin-left: 20px;}
    .instructions ul li ul li	{margin: auto;}

    .details 		{border:none;}

    .details td,
    .details th 	{border:none; padding-left:8px;}

    .details th 		{text-align:right; color:#7d7d7d;}
    .details th span 	{background-color:#eeefef; padding:4px; width:100%; display: inline-block}
    .code 				{font-family: monospace; color:green;}
</style>

<script type="text/javascript">//<![CDATA[
function externalLinks() {
    if (!document.getElementsByTagName) return;
    var anchors = document.getElementsByTagName("a");
    for (var i=0; i<anchors.length; i++) {
        var anchor = anchors[i];
        if (anchor.getAttribute("href") &&
                anchor.getAttribute("rel") == "external") {
            anchor.target = "_blank";
            anchor.setAttribute("rel", "external noopener noreferrer");
        }
    }
}
window.onload = externalLinks;
//]]></script>

<%@ include file="../common/jsEdition.jsp" %>
<t:insertTemplate template="/WEB-INF/jsp/templates/page.jsp">
    <t:putAttribute name="pageTitle">Jasper Server OS Instance Configuration</t:putAttribute>
    <t:putAttribute name="bodyID" value="awsConfiguration"/>
    <t:putAttribute name="moduleName" value="administer/administerAwsOptionsMain"/>
    <t:putAttribute name="headerContent">
        <script type="text/javascript" src="${pageContext.request.contextPath}/scripts/lib/require-jquery-2.1.10.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/scripts/require.config.js"></script>

        <%@ include file="administerState.jsp" %>
        <script type="text/javascript">
            __jrsConfigs__.Administer = Administer;
        </script>

    </t:putAttribute>
    <t:putAttribute name="bodyClass" value="oneColumn"/>
    <c:set var='prefix' value='AWS'/>
    <t:putAttribute name="bodyContent" >
        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerClass" value="column decorated primary"/>
            <t:putAttribute name="containerTitle">Configuration Information</t:putAttribute>
            <t:putAttribute name="bodyClass" value="flow"/>
            <t:putAttribute name="bodyContent">
                <h2><spring:message code="IMA_${prefix}_CONFIGURATION_HEADER"/></h2>
                <h3><spring:message code="IMA_${prefix}_CONFIGURATION_HEADER_DESCR"/></h3>

                <table class="instructions">
                    <tr>
                        <th>Logins</th>
                        <td>
                            <table class="details">
                                <tr>
                                    <th><span>Jasper Server OS:</span></th>
                                    <td>
                                        <p><spring:message code="IMA_${prefix}_CONFIGURATION_LOGIN_DESCR_1"/></p>
                                        <p><spring:message code="IMA_${prefix}_CONFIGURATION_LOGIN_DESCR_2"/></p>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>Operating system:</span></th>
                                    <td>
                                        <p><spring:message code="IMA_${prefix}_CONFIGURATION_OS_VERSION"/><!-- can be checked as follows: more /etc/system-release --></p>
                                        <p><spring:message code="IMA_${prefix}_CONFIGURATION_OS_DESCR"/></p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <th>Jasper Server OS</th>
                        <td>
                            <table class="details">
                                <tr>
                                    <th><span>repository database technology:</span></th>
                                    <td><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_TYPE"/></td>
                                </tr>
                                <tr>
                                    <th><span>repository database name:</span></th>
                                    <td><spring:message code="IMA_${prefix}_CONFIGURATION_REPO_DATABASE_NAME"/></td>
                                </tr>
                                <tr>
                                    <th><span>deployed location:</span></th>
                                    <td>
                                        <span class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_DEPLOY_LOCATION"/></span>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>logs:</span></th>
                                    <td>
                                        <span class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_LOG_LOCATION"/></span>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>volumes:</span></th>
                                    <td>
                                        <span class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_VOLUMES_DESCR"/></span>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>import/export utility:</span></th>
                                    <td>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_SOURCE_LOCATION"/>buildomatic/js-import.sh</p>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_SOURCE_LOCATION"/>buildomatic/js-export.sh</p>
                                        <p style="margin-top:16px;">examples:</p>
                                        <p class="code">[<spring:message code="IMA_${prefix}_CONFIGURATION_DEFAULT_USER"/>@ip-10-0-0-1 ~]# cd <spring:message code="IMA_${prefix}_CONFIGURATION_SOURCE_LOCATION"/>buildomatic/</p>
                                        <p class="code">[<spring:message code="IMA_${prefix}_CONFIGURATION_DEFAULT_USER"/>@ip-10-0-0-1 buildomatic]# sudo ./js-export.sh --help</p>
                                        <p class="code">[<spring:message code="IMA_${prefix}_CONFIGURATION_DEFAULT_USER"/>@ip-10-0-0-1 buildomatic]# sudo ./js-export.sh --uris /organizations/organization_1/reports --output-zip /tmp/my_reports.zip</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    <tr>
                        <th>Apache Tomcat</th>
                        <td>
                            <p class="intro">Jasper Server OS is deployed to the application server <a href="http://tomcat.apache.org/">Apache Tomcat</a> <spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_VERSION"/>.</p>
                            <p class="intro">Tomcat listens on port <spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_PORT"/>.</p>
                            <table class="details">
                                <tr>
                                    <th><span>configuration settings:</span></th>
                                    <td>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_CONFIG_FILE_1"/></p>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_CONFIG_FILE_2"/></p>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_CONFIG_FILE_3"/></p>
                                        <p><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_CONFIG_FILE_DESCR"/></p>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>lib:</span></th>
                                    <td><span class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_LIB_LOCATION"/></span></td>
                                </tr>
                                <tr>
                                    <th><span>webapps:</span></th>
                                    <td><span class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_WEBAPP_LOCATION"/></span></td>
                                </tr>
                                <tr>
                                    <th><span>log files:</span></th>
                                    <td><span class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_LOG_LOCATION"/></span></td>
                                </tr>
                                <tr>
                                    <th><span>memory settings:</span></th>
                                    <td><span class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_MEMORY_CONFIGURATION"/></span></td>
                                </tr>
                                <tr>
                                    <th><span>start &amp; stop command line:</span></th>
                                    <td>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_COMMAND_STOP"/></p>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_COMMAND_START"/></p>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>start &amp; stop configuration:</span></th>
                                    <td><p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_TOMCAT_COMMAND_SCRIPT"/></p></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <th><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_TYPE"/></th>
                        <td>
                            <p class="intro"><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_DESCR"/></p>
                            <table class="details">
                                <tr>
                                    <th><span>default login:</span></th>
                                    <td><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_CREDENTIALS"/></td>
                                </tr>
                                <tr>
                                    <th><span>configuration:</span></th>
                                    <td><p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_CONF_1"/></p>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_CONF_2"/></p>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>useful examples:</span></th>
                                    <td>
                                        <p class="code">[<spring:message code="IMA_${prefix}_CONFIGURATION_DEFAULT_USER"/>@ip-10-0-0-1 bin]# <spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_CONSOLE"/></p>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_EXAMPLE_1_COMMAND"/><br/>
                                            <spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_EXAMPLE_1_OUTPUT"/>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_EXAMPLE_2_COMMAND"/><br />
                                        <spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_EXAMPLE_2_OUTPUT"/></p>
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>start &amp; stop command line:</span></th>
                                    <td>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_COMMAND_STOP"/></p>
                                        <p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_COMMAND_START"/></p>
                                    </td>
                                </tr>
                                <tr>
                                    <th><span>start &amp; stop configuration:</span></th>
                                    <td><p class="code"><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_COMMAND_SCRIPT"/></p></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <th>Security Considerations</th>
                        <td>
                            <p class="intro">You must consider your own requirements when creating a production server. For example:</p>
                            <p class="intro">Close unneeded ports like PostgreSQL on 5432</p>
                            <p class="intro preList">Change default passwords!</p>
                            <ul style="margin:0 0 12px 48px; list-style-type:disc">
                                <li><spring:message code="IMA_${prefix}_CONFIGURATION_DATABASE_TYPE"/></li>
                                <li>Jasper Server OS</li>
                            </ul>
                            <p class="intro">Configure the Tomcat server for https access only.</p>
                            <p class="intro">Take regular snapshots of volumes.</p>
                        </td>
                    </tr>
                </table>
            </t:putAttribute>
        </t:insertTemplate>

        <t:insertTemplate template="/WEB-INF/jsp/templates/container.jsp">
            <t:putAttribute name="containerID" value="filters"/>
            <t:putAttribute name="containerClass" value="column decorated secondary sizeable"/>
            <t:putAttribute name="containerElements">
                <div class="sizer horizontal"></div>
                <button class="button minimize"></button>
            </t:putAttribute>
            <t:putAttribute name="containerTitle"><spring:message code="menu.settings"/></t:putAttribute>
            <t:putAttribute name="bodyClass" value=""/>
            <t:putAttribute name="bodyContent">
            </t:putAttribute>
            <t:putAttribute name="footerContent">
            </t:putAttribute>
        </t:insertTemplate>

    </t:putAttribute>
</t:insertTemplate>
