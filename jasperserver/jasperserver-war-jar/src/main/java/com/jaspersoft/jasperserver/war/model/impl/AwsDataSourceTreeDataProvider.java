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
package com.jaspersoft.jasperserver.war.model.impl;

import com.amazonaws.auth.AWSCredentials;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.AwsCredentialUtil;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.AwsReportDataSource;
import com.jaspersoft.jasperserver.war.amazon.client.AwsDataSourceService;
import com.jaspersoft.jasperserver.war.dto.AwsDBInstanceDTO;
import com.jaspersoft.jasperserver.war.model.JSONObject;
import com.jaspersoft.jasperserver.war.model.TreeDataProvider;
import com.jaspersoft.jasperserver.war.model.TreeNode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @author vsabadosh
 */
public class AwsDataSourceTreeDataProvider implements TreeDataProvider, Serializable {

    public static final String AWS_DB = "awsDb";
    public static final String ROOT_URI = "/";

    private MessageSource messageSource;
    private RepositoryService repositoryService;

    private AwsDataSourceService awsDataSourceService;
    private AwsCredentialUtil awsCredentialUtil;

    private List<String> amazonDBServices;

    private Map<String, TreeNode> treeNodeBasedOnURI = new HashMap<String, TreeNode>();
    private Map<String, List<TreeNode>> childrenNodesBasedOnParentURI = new HashMap<String, List<TreeNode>>();

    public AwsDataSourceTreeDataProvider() {
    }

    private static class RDSProperties implements JSONObject {
        public String dBName = null;
        public String dbType = null;
        public String dbVersion = null;
        public String dnsAddress = null;
        public Integer dbPort = null;
        public String dbUri = null;
        public String jdbcTemplate = null;
        public String jdbcUrl = null;
        public String jdbcDriverClass = null;

        public String toJSONString() {
            org.json.JSONObject jsonObject = new org.json.JSONObject();
            try {
                jsonObject.put("dBName", dBName);
                jsonObject.put("dbType", dbType);
                jsonObject.put("dbVersion", dbVersion);
                jsonObject.put("dnsAddress", dnsAddress);
                jsonObject.put("dbPort", dbPort);
                jsonObject.put("dbUri", dbUri);
                jsonObject.put("jdbcTemplate", jdbcTemplate);
                jsonObject.put("jdbcUrl", jdbcUrl);
                jsonObject.put("jdbcDriverClass", jdbcDriverClass);
            } catch (org.json.JSONException ignored) { }
            return jsonObject.toString();
        }
    }

    @Override
    public TreeNode getNode(ExecutionContext executionContext, String uri, int depth) {
        if (treeNodeBasedOnURI.isEmpty() || ROOT_URI.equals(uri)) {
            refreshTreeData(retrieveCredentialsFromRequest(), retrieveEndpointFromRequest());
        }
        TreeNode node = treeNodeBasedOnURI.get(uri);
        if (node != null) {
            if (depth > 0) {
                processFolder(node, depth - 1);
            }
            return node;
        }
        return null;
    }

    public void refreshTreeData(AWSCredentials awsCredentials, String endpoint) {
        childrenNodesBasedOnParentURI.clear();
        treeNodeBasedOnURI.clear();
        createRootNode(ROOT_URI, "root");
        List<TreeNode> children = new ArrayList<TreeNode>();
        for (String service : amazonDBServices) {
            String nodeUri = ROOT_URI + service.toLowerCase();
            TreeNode childNode = createRootNode(nodeUri, service);
            childrenNodesBasedOnParentURI.put(nodeUri, createChildrenNodes(awsDataSourceService.
                    getAwsDBInstances(awsCredentials, service, endpoint)));
            children.add(childNode);
        }
        childrenNodesBasedOnParentURI.put(ROOT_URI, children);
    }

    private AWSCredentials retrieveCredentialsFromRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();

        String accessKey = request.getParameter("awsAccessKey");
        String secretKey = request.getParameter("awsSecretKey");


        if (secretKey != null && secretKey.equals(messageSource.getMessage("input.password.substitution", null, LocaleContextHolder.getLocale()))) {
            String uri = request.getParameter("datasourceUri");
            AwsReportDataSource existingDs = (AwsReportDataSource) getRepositoryService().getResource(null, uri);
            if (existingDs != null) {
                secretKey = existingDs.getAWSSecretKey();
            }
        }

        String arn = request.getParameter("arn");

        arn = !isBlank(arn) ? arn : null;

        return awsCredentialUtil.getAWSCredentials(accessKey, secretKey, arn);
    }

    private String retrieveEndpointFromRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();

        return request.getParameter("region");
    }

    private TreeNode createRootNode(String nodeUri, String serviceName) {
        RDSProperties extraProperty = new RDSProperties();
        extraProperty.dbUri = nodeUri;
        String label = messageSource.getMessage("tree.aws.folder." + serviceName, null, LocaleContextHolder.getLocale());

        TreeNode treeNode = new TreeNodeImpl(this, serviceName.toLowerCase(), label,
                "com.jaspersoft.jasperserver.api.metadata.common.domain.Folder", nodeUri, 1, extraProperty);

        treeNodeBasedOnURI.put(nodeUri, treeNode);
        return treeNode;
    }

    private List<TreeNode> createChildrenNodes(List<AwsDBInstanceDTO> dbInstances) {
        List<TreeNode> treeNodeList = new ArrayList<TreeNode>();
        if (dbInstances != null && dbInstances.size() > 0) {
            for (AwsDBInstanceDTO dbInstance : dbInstances) {
                RDSProperties extraProperty = new RDSProperties();
                String uri = generateAmazonDbInstanceURI(dbInstance);
                extraProperty.dbUri = uri;
                extraProperty.dBName = dbInstance.getdBName();
                extraProperty.dbType = dbInstance.getEngine();
                extraProperty.dbVersion = dbInstance.getEngineVersion();
                extraProperty.dnsAddress = dbInstance.getAddress();
                extraProperty.dbPort = dbInstance.getPort();
                extraProperty.jdbcTemplate = dbInstance.getJdbcTemplate();
                extraProperty.jdbcUrl = dbInstance.getJdbcUrl();
                extraProperty.jdbcDriverClass = dbInstance.getJdbcDriverClass();
                TreeNode treeNode = new TreeNodeImpl(this, dbInstance.getdBInstanceIdentifier(),
                        dbInstance.getdBInstanceIdentifier(), AWS_DB, uri, 1, extraProperty);

                treeNodeBasedOnURI.put(uri, treeNode);

                treeNodeList.add(treeNode);
            }
            return treeNodeList;
        }
        return treeNodeList;
    }

    private String generateAmazonDbInstanceURI(AwsDBInstanceDTO dbInstance) {
        if (dbInstance != null) {
            return ROOT_URI + dbInstance.getAmazonDbService().toLowerCase() + "/" + dbInstance.getdBInstanceIdentifier();
        } else {
            return ROOT_URI;
        }
    }
    
    @Override
    public List getChildren(ExecutionContext executionContext, String parentUri, int depth) {
        TreeNode n = getNode(executionContext, parentUri, depth + 1);
        if (n != null) {
            return n.getChildren();
        }
        return null;
    }

    private void processFolder(TreeNode node, int depth) {
        List<TreeNode> children = childrenNodesBasedOnParentURI.get(node.getUriString());
        if (children != null) {
            node.getChildren().clear();
            for (TreeNode child : children) {
                node.getChildren().add(child);
                if (depth > 0) {
                    processFolder(child, depth - 1);
                }
            }
        }
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setAwsDataSourceService(AwsDataSourceService awsDatasourceService) {
        this.awsDataSourceService = awsDatasourceService;
    }

    public void setAmazonDBServices(List<String> amazonDBServices) {
        this.amazonDBServices = amazonDBServices;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void setAwsCredentialUtil(AwsCredentialUtil awsCredentialUtil) {
        this.awsCredentialUtil = awsCredentialUtil;
    }
}

