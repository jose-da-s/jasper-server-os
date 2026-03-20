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

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jaspersoft.jasperserver.api.JSShowOnlyErrorMessage;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.azuresql.AzureManagementCredentials;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.azuresql.AzureSqlManagementService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.AzureSqlReportDataSource;
import com.jaspersoft.jasperserver.war.model.JSONObject;
import com.jaspersoft.jasperserver.war.model.TreeDataProvider;
import com.jaspersoft.jasperserver.war.model.TreeNode;

public class AzureSqlDataSourceTreeDataProvider implements TreeDataProvider, Serializable {

    private static final long serialVersionUID = 5453869790805887939L;
    private static final Logger log = LogManager.getLogger(AzureSqlDataSourceTreeDataProvider.class);
    private static final String TREE_NODE_TYPE_FOLDER = "com.jaspersoft.jasperserver.api.metadata.common.domain.Folder";
    private static final String TREE_NODE_TYPE_DB = "leaf";
    private static final String ROOT_URI = "/";
    private MessageSource messageSource;
    private RepositoryService repositoryService;
    private AzureSqlManagementService azureSqlManagementService;

    private Map<String, TreeNode> treeNodeBasedOnURI = new HashMap<String, TreeNode>();
    private Map<String, List<TreeNode>> childrenNodesBasedOnParentURI = new HashMap<String, List<TreeNode>>();
    private String defaultJdbcDriverClassName;
    private String defaultKeyStoreType;

    public AzureSqlDataSourceTreeDataProvider() {
    }

    private static class ExtraProperties implements JSONObject {
        private static final long serialVersionUID = -5541334893215758186L;
        public String dBName;
        public String serverName;
        public String jdbcTemplate;
        // TODO: do we need it?
        public String jdbcDriver;

        public String toJSONString() {
            org.json.JSONObject jsonObject = new org.json.JSONObject();
            try {
                jsonObject.put("dBName", dBName);
                jsonObject.put("serverName", serverName);
                jsonObject.put("jdbcTemplate", jdbcTemplate);
                jsonObject.put("jdbcDriver", jdbcDriver);

            } catch (org.json.JSONException ignored) { }
            return jsonObject.toString();
        }
    }

    @Override
    public TreeNode getNode(ExecutionContext executionContext, String uri, int depth) {
        if (treeNodeBasedOnURI.isEmpty() || ROOT_URI.equals(uri)) {
            refreshTreeData(getDatabases());
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

    public void refreshTreeData(ImmutableMap<String, ImmutableSet<String>> sqlServer2DatabasesMap) {
        childrenNodesBasedOnParentURI.clear();
        treeNodeBasedOnURI.clear();
        addSqlServerNode(ROOT_URI, "root");

        List<TreeNode> children = new ArrayList<TreeNode>();
        for (Entry<String, ImmutableSet<String>> entry : sqlServer2DatabasesMap.entrySet()) {
            String sqlServer = entry.getKey();
            String nodeUri = ROOT_URI + sqlServer.toLowerCase();
            TreeNode childNode = addSqlServerNode(nodeUri, sqlServer);
            childrenNodesBasedOnParentURI.put(nodeUri, createDatabaseNodes(sqlServer, entry.getValue()));
            children.add(childNode);
        }

        childrenNodesBasedOnParentURI.put(ROOT_URI, children);

    }

    private ImmutableMap<String, ImmutableSet<String>> getDatabases() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();

        AzureManagementCredentials azureSqlManagementCredentials = new AzureManagementCredentials();
        azureSqlManagementCredentials.setSubscriptionId(request.getParameter("subscriptionId"));
        String keyStoreType = request.getParameter("keyStoreType");
        if (StringUtils.isEmpty(keyStoreType)) {
            keyStoreType= defaultKeyStoreType;
        }
        azureSqlManagementCredentials.setKeyStoreType(keyStoreType);
        azureSqlManagementCredentials.setKeyStoreBytes(getKeyStoreBytes(request.getParameter("keyStoreUri")));
        String keyStorePassword = request.getParameter("keyStorePassword");

        if (keyStorePassword != null && keyStorePassword.equals(messageSource.getMessage("input.password.substitution", null, LocaleContextHolder.getLocale()))) {
            String uri = request.getParameter("datasourceUri");
            AzureSqlReportDataSource existingDs = (AzureSqlReportDataSource) getRepositoryService().getResource(null, uri);
            if (existingDs != null) {
                keyStorePassword = existingDs.getKeyStorePassword();
            }
        }

        azureSqlManagementCredentials.setKeyStorePassword(keyStorePassword.toCharArray());

        ImmutableMap<String, ImmutableSet<String>> sqlServer2DatabasesMap = azureSqlManagementService.getDatabases(azureSqlManagementCredentials);
        return sqlServer2DatabasesMap;
    }

    private byte[] getKeyStoreBytes(String keyStoreUri) {
        byte[] keyStoreBytes;
        InputStream data = null;
        try {
            FileResource fileResource = (FileResource) getRepositoryService().getResource(null, keyStoreUri);
            if (fileResource.hasData()) {
                data = fileResource.getDataStream();
            } else {
                FileResourceData resourceData = repositoryService.getResourceData(null, fileResource.getURIString());
                data = resourceData.getDataStream();
            }
            keyStoreBytes = IOUtils.toByteArray(data);
        } catch (Exception e) {
            log.error("Unable to read azure datasource cerfificate file from repository; fileUri:" + keyStoreUri, e);
            throw new JSShowOnlyErrorMessage("azure.exception.datasource.key.error");
        } finally {
            try { if (data != null) data.close();
            } catch (Exception ex) {};
        }
        return keyStoreBytes;
    }

    private TreeNode addSqlServerNode(String nodeUri, String sqlServerName) {
        ExtraProperties extraProperties = new ExtraProperties();
        TreeNode treeNode = new TreeNodeImpl(this, sqlServerName.toLowerCase(), sqlServerName,
                TREE_NODE_TYPE_FOLDER, nodeUri, 1, extraProperties);
        treeNodeBasedOnURI.put(nodeUri, treeNode);
        return treeNode;
    }

    private List<TreeNode> createDatabaseNodes(String sqlServer, Set<String> dbInstances) {
        List<TreeNode> treeNodeList = new ArrayList<TreeNode>();
        for (String dbInstance : dbInstances) {
            ExtraProperties extraProperties = new ExtraProperties();
            extraProperties.serverName = sqlServer;
            extraProperties.dBName = dbInstance;
            extraProperties.jdbcDriver = defaultJdbcDriverClassName;
            extraProperties.jdbcTemplate = azureSqlManagementService.getJdbcUrl("$[serverName]",  "$[dbName]");

            String nodeUri = "/" + sqlServer + "/" + dbInstance;
            TreeNode treeNode = new TreeNodeImpl(this, extraProperties.dBName,
                    extraProperties.dBName, TREE_NODE_TYPE_DB, nodeUri, 1, extraProperties);

            treeNodeBasedOnURI.put(nodeUri, treeNode);

            treeNodeList.add(treeNode);
        }
        return treeNodeList;

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

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public void setAzureSqlManagementService(AzureSqlManagementService azureSqlManagementService) {
            this.azureSqlManagementService = azureSqlManagementService;
    }

    public void setDefaultJdbcDriverClassName(String defaultJdbcDriverClassName) {
            this.defaultJdbcDriverClassName = defaultJdbcDriverClassName;
    }

    public void setDefaultKeyStoreType(String defaultKeyStoreType) {
            this.defaultKeyStoreType = defaultKeyStoreType;
    }

}

