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

package com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FavoriteResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.HibernateDaoImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoFavoriteResource;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoResource;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoResourceBase;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoUser;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.api.search.SearchCriteria;
import com.jaspersoft.jasperserver.core.util.DBUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional(propagation = Propagation.SUPPORTS)
public class FavoriteResourceServiceImpl extends HibernateDaoImpl implements FavoriteResourceService {

    private UserAuthorityService userAuthorityService;
    @Resource(name = "tenantService")
    private TenantService tenantService;
    @Resource(name = "${bean.tenantService}")
    private TenantService mTenantService;

    protected FavoriteResourceService favoriteResourceProxyService;

    public FavoriteResourceService getFavoriteResourceProxyService() {
        return favoriteResourceProxyService;
    }

    public void setFavoriteResourceProxyService(FavoriteResourceService favoriteResourceProxyService) {
        this.favoriteResourceProxyService = favoriteResourceProxyService;
    }

    protected static final Log log = LogFactory.getLog(FavoriteResourceServiceImpl.class);
    private SessionFactory sessionFactory;

    public FavoriteResourceServiceImpl() {
    }


    public UserAuthorityService getUserAuthorityService() {
        return userAuthorityService;
    }

    public void setUserAuthorityService(UserAuthorityService userAuthorityService) {
        this.userAuthorityService = userAuthorityService;
    }

    public RepoUser getRepoUser(Object clientObject) {
        if (clientObject == null) {
            return null;
        }
        return (RepoUser) ((PersistentObjectResolver) userAuthorityService).getPersistentObject(clientObject);
    }

    private void addFavorite(RepoUser repoUser, RepoResource repoResource) {
        RepoFavoriteResource repoFavoriteResource = new RepoFavoriteResource();
        repoFavoriteResource.setResource(repoResource);
        repoFavoriteResource.setUser(repoUser);
        repoFavoriteResource.setCreationDate(new Date(System.currentTimeMillis()));
        getHibernateTemplate().saveOrUpdate(repoFavoriteResource);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void addFavorites(User user, List<String> uriList) {
        List<RepoResource> repoResourceList = getRepoResourceListValidate(uriList);
        RepoUser repoUser = getRepoUser(user);
        addFavoritesIfNotExist(repoUser, repoResourceList);
    }

    private void addFavoritesIfNotExist(RepoUser repoUser, List<RepoResource> repoResourceList) {
        //skip adding already marked favorite resources
        List<RepoFavoriteResource> repoFavoriteResources = getRepoFavoriteResources(repoUser, repoResourceList);
        if (!repoFavoriteResources.isEmpty()) {
            List<RepoResource> favRepoResourceList = repoFavoriteResources.stream().map(favRes -> favRes.getResource()).collect(Collectors.toList());
            repoResourceList = repoResourceList.stream().filter(res -> !favRepoResourceList.contains(res)).collect(Collectors.toList());
        }
        String result = StringUtils.join(repoResourceList.stream().map(RepoResource::getResourceURI).collect(Collectors.toList()), ",\n");
        log.debug("adding resources as favorites: " + result);
        for (RepoResource resource : repoResourceList) {
            addFavorite(repoUser, resource);
        }
    }

    protected void validateResources(List<String> uriList, List<RepoResource> repoResourceList) {
        // Validation of Resource existence
        List<String> validUriList = getValidUriList(repoResourceList);
        if (uriList.size() > validUriList.size()) {
            List<String> inValidUriList = uriList.stream().filter(uri -> !(validUriList.contains(uri))).collect(Collectors.toList());
            throw new JSResourceNotFoundException(inValidUriList.get(0));
        }
        // Validation of Resource Access
        List<String> accessibleUriList = getAccessibleUriList(repoResourceList);
        if (validUriList.size() > accessibleUriList.size()) {
            List<String> inAccessibleUriList = uriList.stream().filter(uri -> !(accessibleUriList.contains(uri))).collect(Collectors.toList());
            throw new AccessDeniedException(inAccessibleUriList.get(0));
        }
    }

    protected List<String> getAccessibleUriList(List<RepoResource> repoResourceList) {
        List<ResourceLookup> resourceLookups = favoriteResourceProxyService.toLookups(repoResourceList);
        return resourceLookups.stream().map(ResourceLookup::getURIString).collect(Collectors.toList());
    }

    protected List<String> getValidUriList(List<RepoResource> repoResourceList) {
        return repoResourceList.stream().map(RepoResourceBase::getResourceURI).collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void removeFavorites(User user, List<String> uriList) {
        List<RepoResource> repoResourceList = getRepoResourceListValidate(uriList);
        List<RepoFavoriteResource> repoFavoriteResources = getRepoFavoriteResources(getRepoUser(user), repoResourceList);
        removeFavourites(repoFavoriteResources);
    }

    private void removeFavourites(List<RepoFavoriteResource> repoFavoriteResources) {
        String result = StringUtils.join(repoFavoriteResources.stream().map(RepoFavoriteResource::getResource).map(RepoResource::getResourceURI).collect(Collectors.toList()), ",\n");
        log.debug("removing resources as favorites: " + result);
        getHibernateTemplate().deleteAll(repoFavoriteResources);
    }

    protected String getExternalURI(String resourceURI) {
        return resourceURI;
    }

    private List<RepoFavoriteResource> getRepoFavoriteResources(RepoUser repoUser, List<RepoResource> resourceList) {
        DetachedCriteria criteria = DetachedCriteria.forClass(RepoFavoriteResource.class)
                .add(Restrictions.eq("user", repoUser))
                .add(DBUtil.getBoundedInCriterion("resource", resourceList));

        List<RepoFavoriteResource> results = (List<RepoFavoriteResource>) getHibernateTemplate().findByCriteria(criteria);
        return results;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<String> getFavoritesURIList(User user, List<String> resourceURIList) {
        RepoUser repoUser = getRepoUser(user);
        List<RepoResource> resourceList = getRepoResourceList(resourceURIList);
        DetachedCriteria criteria = DetachedCriteria.forClass(RepoFavoriteResource.class)
                .add(DBUtil.getBoundedInCriterion("resource", resourceList))
                .add(Restrictions.eq("user", repoUser))
                .setProjection(Projections.property("resource"));
        List<RepoResource> favoriteResourcesList = (List<RepoResource>) getHibernateTemplate().findByCriteria(criteria);
        List<String> favoriteURIsList = favoriteResourcesList.stream().map(t -> t.getResourceURI()).collect(Collectors.toList());
        return favoriteURIsList;
    }

    @Override
    public List<Long> getFavoriteIdsForUser(User user, boolean isFilterEnabled) {
        RepoUser repoUser = getRepoUser(user);
        DetachedCriteria criteria = DetachedCriteria.forClass(RepoFavoriteResource.class)
                .add(Restrictions.eq("user", repoUser));
        List<RepoFavoriteResource> resourceList = (List<RepoFavoriteResource>) getHibernateTemplate().findByCriteria(criteria);
        return resourceList.stream().map(RepoFavoriteResource::getResourceId).collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<RepoResource> getRepoResourceList(List<String> uriList) {
        if (!uriList.isEmpty()) {
            SearchCriteria resourceCriteria = SearchCriteria.forClass(RepoResource.class);//set cache true
            Disjunction disjunction = Restrictions.disjunction();
            String alias = resourceCriteria.getAlias("parent", "p");
            resourceCriteria.add(Restrictions.eq(alias + ".hidden", Boolean.FALSE));
            for (Object o : uriList) {
                String uri = (String) o;
                disjunction.add(getResourceCriterion(alias, uri));
            }
            resourceCriteria.add(disjunction);

            List resourceList = getHibernateTemplate().findByCriteria(resourceCriteria);
            return resourceList;
        }
        return null;
    }

    public List<RepoResource> getRepoResourceListValidate(List<String> uriList) {
        List<RepoResource> repoResourceList = getRepoResourceList(uriList);
        validateResources(uriList, repoResourceList);
        return repoResourceList;
    }


    private Criterion getResourceCriterion(String alias, String uri) {
        final Pair<String, String> parentAndName = getResourceParentAndName(uri);

        String folderUri = parentAndName.getLeft();
        String resourceName = parentAndName.getRight();

        return Restrictions.and(Restrictions.eq("name", resourceName), Restrictions.eq(alias + ".URI", folderUri));
    }

    private Pair<String, String> getResourceParentAndName(String uri) {
        if (!uri.startsWith(Folder.SEPARATOR))
            uri = Folder.SEPARATOR + uri;
        int lastSlashIndex = uri.lastIndexOf(Folder.SEPARATOR);

        String folderUri = uri.substring(0, lastSlashIndex);
        String resourceName = uri.substring(lastSlashIndex + 1);

        if (folderUri.length() == 0) {
            folderUri = Folder.SEPARATOR;
        }

        return Pair.of(folderUri, resourceName);
    }

    public List<ResourceLookup> toLookups(List<RepoResource> repoResources) {
        List result = repoResources.stream().map(resource -> resource.toClientLookup()).collect(Collectors.toList());
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<FavoriteResourceImpl> getFavoritesForUri(ExecutionContext context, String uri) {
        return getFavoritesForUri(context, uri, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<FavoriteResourceImpl> getFavoritesForUri(ExecutionContext context, String uri, Filters filters) {
        if (uri == null) {
            return Collections.EMPTY_LIST;
        }

        SearchCriteria favoriteCriteria = SearchCriteria.forClass(RepoFavoriteResource.class);//set cache true
        String resAlias = favoriteCriteria.getAlias("resource", "res");
        String folderAlias = favoriteCriteria.getAlias(resAlias + ".parent", "folder");
        String userAlias = favoriteCriteria.getAlias("user", "u");
        String tenantAlias = favoriteCriteria.getAlias(userAlias + ".tenant", "t");

        final Pair<String, String> parentAndName = getResourceParentAndName(uri);
        String folderUri = parentAndName.getLeft();
        String resourceName = parentAndName.getRight();

        favoriteCriteria.add(
                Restrictions.and(
                        Restrictions.or(
                                Restrictions.and(
                                        Restrictions.eq(folderAlias + ".hidden", Boolean.FALSE)
                                        , Restrictions.eq(folderAlias + ".URI", uri))
                                , Restrictions.and(
                                        Restrictions.eq(resAlias + ".name", resourceName)
                                        , Restrictions.eq(folderAlias + ".URI", folderUri)))));


        addTenantUsers(context, favoriteCriteria,tenantAlias);

        favoriteCriteria.addProjection(Projections.projectionList()
                .add(Projections.property("id"), "id")
                .add(Projections.property(folderAlias + ".URI"), "parentURI")
                .add(Projections.property(resAlias+".name"), "resourceName")
                .add(Projections.property(userAlias+".username"), "tenantUser")
                .add(Projections.property(tenantAlias+".tenantId"), "tenantId")
        );

        favoriteCriteria.setResultTransformer(Transformers.aliasToBean(FavoriteResourceImpl.class));
        List favoriteList = getHibernateTemplate().findByCriteria(favoriteCriteria);
        return toClientFavorites(context,favoriteList);
    }
    protected void addTenantUsers(ExecutionContext context, SearchCriteria favoriteCriteria, String tenantAlias) {
    }

    protected List<FavoriteResourceImpl> toClientFavorites(ExecutionContext context, List repoFavorites) {
        return (List<FavoriteResourceImpl>) repoFavorites;
    }

    public String getClientUsername(RepoUser user) {
        return user == null ? null : user.getUsername();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void importFavorites(ExecutionContext context, Map<String, List<String>> userURIsFavMap) {

        Set<String> userNames = userURIsFavMap.keySet();
        Set<String> resourceURIs = getInternalURIs(context, userURIsFavMap);
        Map<String, RepoUser> userMap = getRepoUserMap(context, userNames);
        if(userMap.isEmpty()) return;
        Map<String, RepoResource> resourceMap = getRepoResourceMap(resourceURIs);

        for (String userName : userNames) {
            RepoUser repoUser = userMap.get(userName);
            List<String> uris = userURIsFavMap.get(userName);
            List<RepoResource> repoResourceList = new ArrayList<>();
            for (String uri : uris) {
                repoResourceList.add(resourceMap.get(uri));
            }
            //skip adding already marked favorite resources
            addFavoritesIfNotExist(repoUser, repoResourceList);
        }
    }
    protected Set<String> getInternalURIs(ExecutionContext context, Map<String, List<String>> userURIsFavMap) {
        Set<String> userNames = userURIsFavMap.keySet();
        Set<String> resourceURIs= new HashSet<>();
        for (String username : userNames) {
            List<String> list = userURIsFavMap.get(username);
            resourceURIs.addAll(list);
        }
        return resourceURIs;
    }

    private Map<String, RepoUser> getRepoUserMap(ExecutionContext context, Set<String> userNames) {
        if (!userNames.isEmpty()) {
            Map<String, RepoUser> userMap = new HashMap<>();
            for (String userName : userNames) {
                try {
                    User user = userAuthorityService.getUser(context, userName);
                    if(user==null) {
                        log.warn("The User " + userName + " is not available, skipping his/her favorite(s)");
                        continue;
                    }
                    userMap.put(userName, getRepoUser(user));
                } catch (AccessDeniedException e){
                    log.warn("The User "+userName+" is not available, skipping his/her favorite(s)");
                    continue;
                }
            }
            return userMap;
        }
        return null;
    }

    private Map<String, RepoResource> getRepoResourceMap(Set<String> uriList) {
        if (!uriList.isEmpty()) {
            SearchCriteria resourceCriteria = SearchCriteria.forClass(RepoResource.class);//set cache true
            Disjunction disjunction = Restrictions.disjunction();
            String alias = resourceCriteria.getAlias("parent", "p");
            resourceCriteria.add(Restrictions.eq(alias + ".hidden", Boolean.FALSE));
            for (Object o : uriList) {
                String uri = (String) o;
                disjunction.add(getResourceCriterion(alias, uri));
            }
            resourceCriteria.add(disjunction);

            List<RepoResource> resourceList = (List<RepoResource>) getHibernateTemplate().findByCriteria(resourceCriteria);
            return resourceList.stream().collect(Collectors.toMap(RepoResource::getResourceURI, Function.identity()));
        }
        return null;
    }
}

