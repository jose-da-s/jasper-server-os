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
package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;

import net.sf.jasperreports.data.cache.DataSnapshot;
import org.springframework.cache.Cache;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.DataSnapshotCachingService;
import com.jaspersoft.jasperserver.api.metadata.data.cache.DataCacheSnapshot;
import com.jaspersoft.jasperserver.api.metadata.data.cache.DataSnapshotPersistentMetadata;
import com.jaspersoft.jasperserver.api.metadata.data.cache.DataSnapshotSavedId;
import com.jaspersoft.jasperserver.api.metadata.data.cache.DefaultDataSnapshotPersistentMetadata;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class DataSnapshotEhcacheService implements DataSnapshotCachingService {

	private Cache metadataCache;
	private Cache contentsCache;

	public DataSnapshotPersistentMetadata getSnapshotMetadata(ExecutionContext context, long snapshotId) {
		if (metadataCache == null) {
			return null;
		}

		Cache.ValueWrapper wrapper = metadataCache.get(snapshotId);
		if (wrapper == null) {
			return null;
		}
		
		return (DataSnapshotPersistentMetadata) wrapper.get();
	}

	public void putSnapshotMetadata(ExecutionContext context, 
			long snapshotId, DataSnapshotPersistentMetadata metadata) {
		if (metadataCache != null) {
			metadataCache.put(snapshotId, metadata);
		}
	}

	public DataSnapshot getSnapshotContents(ExecutionContext context, long contentsId) {
		if (contentsCache == null) {
			return null;
		}

		Cache.ValueWrapper wrapper = contentsCache.get(contentsId);
		if (wrapper == null) {
			return null;
		}
		
		return (DataSnapshot) wrapper.get();
	}

	public void putSnapshotContents(ExecutionContext context, 
			long contentsId, DataSnapshot dataSnapshot) {
		if (contentsCache != null) {
			contentsCache.put(contentsId, dataSnapshot);
		}
	}

	public void put(ExecutionContext context, DataSnapshotSavedId savedId, DataCacheSnapshot snapshot) {
		// put the metadata
		DefaultDataSnapshotPersistentMetadata persistentMetadata = new DefaultDataSnapshotPersistentMetadata();
		persistentMetadata.setSnapshotMetadata(snapshot.getMetadata());
		persistentMetadata.setVersion(savedId.getVersion());
		persistentMetadata.setContentsId(savedId.getContentsId());
		putSnapshotMetadata(context, savedId.getSnapshotId(), persistentMetadata);
		
		// put the contents
		putSnapshotContents(context, savedId.getContentsId(), snapshot.getSnapshot());
	}

	public void invalidateSnapshot(ExecutionContext context, long snapshotId) {
		if (metadataCache == null) {
			// nothing to do
			return;
		}

		Cache.ValueWrapper wrapper = metadataCache.get(snapshotId);
		if (wrapper == null) {
			// nothing to do
			return;
		}
		
		DataSnapshotPersistentMetadata metadata = (DataSnapshotPersistentMetadata) wrapper.get();
		// remove from the two caches
		metadataCache.evict(snapshotId);
		if (contentsCache != null && metadata != null) {
			contentsCache.evict(metadata.getContentsId());
		}
	}

	public Cache getMetadataCache() {
		return metadataCache;
	}

	public void setMetadataCache(Cache metadataCache) {
		this.metadataCache = metadataCache;
	}

	public Cache getContentsCache() {
		return contentsCache;
	}

	public void setContentsCache(Cache contentsCache) {
		this.contentsCache = contentsCache;
	}

}
