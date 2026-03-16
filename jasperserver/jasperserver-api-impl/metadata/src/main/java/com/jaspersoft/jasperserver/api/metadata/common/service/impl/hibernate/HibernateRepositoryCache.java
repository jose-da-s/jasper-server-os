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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.util.RepositoryCacheIndicator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import javax.cache.Cache;
import org.springframework.orm.hibernate5.HibernateCallback;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryCache;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryCacheableItem;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.HibernateDaoImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.CachedItem;
import com.jaspersoft.jasperserver.api.metadata.common.util.LocalLockManager;
import com.jaspersoft.jasperserver.api.metadata.common.util.LockHandle;
import com.jaspersoft.jasperserver.api.metadata.common.util.LockManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class HibernateRepositoryCache extends HibernateDaoImpl implements RepositoryCache {
	private static final Log log = LogFactory.getLog(HibernateRepositoryCache.class);

	private RepositoryService repository;
	private LockManager lockManager = new LocalLockManager();
	private boolean setFindByCriteriaToReadOnly = false;
	private boolean isEnabledRepositoryCaching = false;
	private Cache<String, CachedItem> hibernateRepositoryEhcache;

	public HibernateRepositoryCache() {
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public InputStream cache(ExecutionContext context, FileResource resource, RepositoryCacheableItem cacheableItem) {
		if (isCachingOn()) {
			CachedItem cachedItem = getCachedItem(context, resource, cacheableItem);
			while(cachedItem.isItemReference()) {
				cachedItem = cachedItem.getReference();
			}
			return new ByteArrayInputStream(cachedItem.getDataBytes());
		} else {
			return new ByteArrayInputStream(cacheableItem.getData(context, resource));
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public InputStream cache(ExecutionContext context, String uri, RepositoryCacheableItem cacheableItem) {
		FileResource resource = (FileResource) repository.getResource(context, uri);
		return cache(context, resource, cacheableItem);
	}

	protected LockHandle lock(FileResource resource, RepositoryCacheableItem cacheableItem) {
		return lockManager.lock(cacheableItem.getCacheName(), resource.getURIString());
	}

	protected void unlock(LockHandle lock) {
		lockManager.unlock(lock);
	}

	protected CachedItem getCachedItem(ExecutionContext context, FileResource resource, RepositoryCacheableItem cacheableItem) {
		if (log.isDebugEnabled()) {
			log.debug("Looking in repository cache \"" + cacheableItem.getCacheName() + "\" for resource \"" + resource.getURIString() +
					"\", version " + resource.getVersion() + "\", version date " + resource.getCreationDate());
		}

		LockHandle lock = lock(resource, cacheableItem);

		try {
			CachedItem cachedItem = getCachedItem(resource.getURIString(), cacheableItem, true);
			if (cachedItem == null
					|| cachedItem.getVersion() < resource.getVersion()
					|| cachedItem.getVersionDate() == null
					|| cachedItem.getVersionDate().before(resource.getCreationDate())) {
				if (resource.isReference()) {
					cachedItem = saveRefence(context, resource, cachedItem, cacheableItem);
				} else {
					cachedItem = saveData(context, resource, cachedItem, cacheableItem);
				}
			} else if (resource.isReference()) {
				//load the reference to force updates
				FileResource ref = (FileResource) repository.getResource(context, resource.getReferenceURI());
				CachedItem refItem = getCachedItem(context, ref, cacheableItem);
				if (!refItem.equals(cachedItem.getReference())) {
					updateReference(cachedItem, refItem);
				}
			}

			return cachedItem;
		} finally {
			unlock(lock);
		}
	}
	protected CachedItem getCachedItem(String uri, RepositoryCacheableItem cacheableItem) {
		return getCachedItem(uri, cacheableItem, false, null);
	}
	protected CachedItem getCachedItem(String uri, RepositoryCacheableItem cacheableItem, Session session) {
		return getCachedItem(uri, cacheableItem, false, session);
	}
	protected CachedItem getCachedItem(String uri, RepositoryCacheableItem cacheableItem, boolean clearPendingSavesCreatedByFindByCriteria) {
		return getCachedItem(uri, cacheableItem, clearPendingSavesCreatedByFindByCriteria, null);
	}

	protected CachedItem getCachedItem(String uri, RepositoryCacheableItem cacheableItem, boolean clearPendingSavesCreatedByFindByCriteria, Session session) {
		if (log.isDebugEnabled()) {
			log.debug("HibernateRepositoryCache:  Looking in repository cache \"" + cacheableItem.getCacheName() + "\" for resource \"" + uri);
		}
		if (hibernateRepositoryEhcache != null) {
			CachedItem value = hibernateRepositoryEhcache.get(uri);
			if (value != null) {
				return value;
			}
		}
		if (isEnabledRepositoryCaching) {
			return getCachedItemFromRepository(uri, cacheableItem, clearPendingSavesCreatedByFindByCriteria, session);
		}

		return null;
	}

	private CachedItem getCachedItemFromRepository(String uri, RepositoryCacheableItem cacheableItem,
												   boolean clearPendingSavesCreatedByFindByCriteria, Session session) {

		DetachedCriteria criteria = DetachedCriteria.forClass(CachedItem.class);
		criteria.add(Restrictions.naturalId().set("cacheName", cacheableItem.getCacheName()).set("uri", uri));
		criteria.getExecutableCriteria(getSession()).setCacheable(true);
		List list = null;

		if (setFindByCriteriaToReadOnly) {
			// flush all pending saves, updates and deletes to the database (However, it may slows down the performance, since we have to synchronize the method
			list = findByCritera(criteria, clearPendingSavesCreatedByFindByCriteria);
		} else {
			// no synchronization, but spring hibernate template creates an extra update statement when calling findByCriteria
			if (session==null)
				list = getHibernateTemplate().findByCriteria(criteria);
			else
				list = criteria.getExecutableCriteria(session).list();
		}

		CachedItem item = null;
		if (list.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("did not find \"" + uri + "\" in cache " +
						cacheableItem.getCacheName());
			}
			item = null;
		} else {
			item = (CachedItem) list.get(0);
			if (log.isDebugEnabled()) {
				log.debug("found \"" + uri + "\" in cache " +
						cacheableItem.getCacheName() +
						". isReference: " + item.isItemReference());
			}
		}
		return item;
	}

	/**
	 * whenever we made a hibernate call, findByCriteria(...) for JIRepositoryCache table, it would actually execute 2 SQL statements.
	 * One is a SELECT statement and the other one is an UPDATE statement
	 * SOLUTION:  1) FLUSH ALL THE PENDING SAVES FIRST
	 *            2) SET FLUSH MODE TO NEVER AND CALL FINDBYCRITERIA()
	 *            3) CLEAR ALL THE PENDING SAVES CREATED BY FINDBYCRITERIA()
	 *            4) RESET THE FLUSH MODE
	 * @param criteria searching criteria
	 * @param clearPendingSavesCreatedByFindByCriteria remove the "extra" update statement that caused by spring findByCriteria() method
	 * @return search result
	 */
	private synchronized List findByCritera(DetachedCriteria criteria, boolean clearPendingSavesCreatedByFindByCriteria) {
		if (clearPendingSavesCreatedByFindByCriteria) {
			throw new RuntimeException ("Unsafe hibernate code blocked");
        	/*
        // flush all pending saves, updates and deletes to the database
            getHibernateTemplate().flush();
            // take a snapshot of original flush mode
            int flushMode = getHibernateTemplate().getFlushMode();
            // set flush mode to read only:  to avoid unnecessary write or update that is called by findByCriteria(...)
            getHibernateTemplate().setFlushMode(HibernateAccessor.FLUSH_NEVER);
            List list = getHibernateTemplate().findByCriteria(criteria);
            try {
                // remove all objects from the session cache, and cancel all pending saves, updates, and deletes
               getHibernateTemplate().clear();
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("HibernateRepositoryCache:  Fail to cancel all pending saves, updates, and deletes");
                }
            }
            // set it back to original flush mode
            getHibernateTemplate().setFlushMode(flushMode);
            return list;*/
		} else {
			return getHibernateTemplate().findByCriteria(criteria);
		}
	}

	protected CachedItem saveRefence(ExecutionContext context, FileResource resource, CachedItem item, RepositoryCacheableItem cacheableItem) {
		FileResource ref = (FileResource) repository.getResource(context, resource.getReferenceURI());
		CachedItem refItem = getCachedItem(context, ref, cacheableItem);

		CachedItem saveItem;
		if (item == null) {
			saveItem = new CachedItem();
		} else {
			saveItem = item;
		}
		saveItem.setCacheName(cacheableItem.getCacheName());
		saveItem.setData(null);
		saveItem.setReference(refItem);
		saveItem.setUri(resource.getURIString());
		saveItem.setVersion(resource.getVersion());
		saveItem.setVersionDate(resource.getCreationDate());

		if (hibernateRepositoryEhcache != null) {
			hibernateRepositoryEhcache.put(resource.getURIString(), saveItem);
		}
		saveOrUpdateInRepositoryCache(saveItem, item);

		return saveItem;
	}

	protected CachedItem saveData(ExecutionContext context, FileResource resource, CachedItem item, RepositoryCacheableItem cacheableItem) {
		if (log.isDebugEnabled()) {
			log.debug("Saving repository cache \"" + cacheableItem.getCacheName() + "\" for resource \"" + resource.getURIString() +
					"\", version " + resource.getVersion() + "\", version date " + resource.getCreationDate());
		}

		byte[] data = cacheableItem.getData(context, resource);

		CachedItem saveItem;
		if (item == null) {
			saveItem = new CachedItem();
		} else {
			saveItem = item;
		}
		saveItem.setCacheName(cacheableItem.getCacheName());
		saveItem.setDataBytes(data);
		saveItem.setReference(null);
		saveItem.setUri(resource.getURIString());
		saveItem.setVersion(resource.getVersion());
		saveItem.setVersionDate(resource.getCreationDate());

		if (hibernateRepositoryEhcache != null) {
			hibernateRepositoryEhcache.put(resource.getURIString(), saveItem);
		}
		saveOrUpdateInRepositoryCache(saveItem, item);

		return saveItem;
	}

	protected void updateReference(CachedItem saveItem, CachedItem refItem) {
		saveItem.setReference(refItem);
		if (hibernateRepositoryEhcache != null) {
			hibernateRepositoryEhcache.put(saveItem.getUri(), saveItem);
		}

		if (isEnabledRepositoryCaching) {
			getHibernateTemplate().update(saveItem);
		}
	}

	private void saveOrUpdateInRepositoryCache(CachedItem saveItem, CachedItem item) {
		if (!isEnabledRepositoryCaching) return;

		if (item == null) {
			getHibernateTemplate().save(saveItem);
		} else {
			getHibernateTemplate().update(saveItem);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void clearCache(ExecutionContext ctx, final String uri, final RepositoryCacheableItem cacheableItem) {
		if (isCachingOn()) {
			executeWriteCallback(new DaoCallback() {
				public Object execute() {
					removeCached(ctx, uri, cacheableItem);
					return null;
				}
			}, false);
		}
	}

	protected void removeCached(ExecutionContext ctx, String uri, RepositoryCacheableItem cacheableItem) {
		if (log.isDebugEnabled()) {
			log.debug("Clearing cache " + cacheableItem.getCacheName() + " for resource " + uri);
		}

		if (hibernateRepositoryEhcache != null) {
			hibernateRepositoryEhcache.remove(uri);
		}

		if (isEnabledRepositoryCaching) {
			getHibernateTemplate().executeWithNativeSession(new RemoveCachedCallback(uri, cacheableItem));
		}
	}

	class RemoveCachedCallback implements HibernateCallback<Object> {
		String uri;
		RepositoryCacheableItem cacheableItem;
		public RemoveCachedCallback(String uri, RepositoryCacheableItem cacheableItem) {
			this.uri=uri;
			this.cacheableItem = cacheableItem;
		}
		public Object doInHibernate(Session session) {
			CachedItem cachedItem = getCachedItem(uri, cacheableItem, session);
			if (cachedItem != null) {
				session.delete(cachedItem);
			}
			return null;
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void clearCache(final RepositoryCacheableItem cacheableItem) {
		if (isCachingOn()) {
			executeWriteCallback(new DaoCallback() {
				public Object execute() {
					removeCached(cacheableItem);
					return null;
				}
			}, false);
		}
	}

	protected void removeCached(RepositoryCacheableItem cacheableItem) {
		if (log.isDebugEnabled()) {
			log.debug("Clearing entire cache " + cacheableItem.getCacheName());
		}

		if (hibernateRepositoryEhcache != null) {
			hibernateRepositoryEhcache.clear();
		}

		if (isEnabledRepositoryCaching) {
			getHibernateTemplate().execute((s)-> s.createQuery("delete CachedItem where cacheName = ?1")
					.setParameter(1, cacheableItem.getCacheName())
					.executeUpdate());
		}
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	private boolean isCachingOn() {
		return RepositoryCacheIndicator.isOn();
	}

	public Cache<String, CachedItem> getHibernateRepositoryEhcache() {
		return hibernateRepositoryEhcache;
	}

	public void setHibernateRepositoryEhcache(Cache<String, CachedItem> hibernateRepositoryEhcache) {
		this.hibernateRepositoryEhcache = hibernateRepositoryEhcache;
	}

	public RepositoryService getRepository() {
		return repository;
	}

	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

	public boolean getFindByCriteriaToReadOnly() {
		return setFindByCriteriaToReadOnly;
	}

	public void setFindByCriteriaToReadOnly(boolean setFindByCriteriaToReadOnly) {
		this.setFindByCriteriaToReadOnly = setFindByCriteriaToReadOnly;
	}

	public boolean isEnabledRepositoryCaching() {
		return isEnabledRepositoryCaching;
	}

	public void setIsEnabledRepositoryCaching(boolean isEnabledRepositoryCaching) {
		this.isEnabledRepositoryCaching = isEnabledRepositoryCaching;
	}
}
