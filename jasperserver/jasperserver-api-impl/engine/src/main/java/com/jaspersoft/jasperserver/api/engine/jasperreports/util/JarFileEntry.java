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
package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import java.io.ByteArrayInputStream;

import net.sf.jasperreports.engine.util.ObjectUtils;
import net.sf.jasperreports.engine.util.ObjectUtils.HashCode;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class JarFileEntry {
	private final JarFile jar;
	private final JarEntry jarEntry;
	private byte[] overrideData;
	
	protected JarFileEntry(JarFile jar, JarEntry jarEntry) {
		this.jar = jar;
		this.jarEntry = jarEntry;
	}

	private String entryName() {
		return jarEntry == null ? null : jarEntry.getName();
	}
	
	@Override
	public int hashCode() {
		HashCode hash = ObjectUtils.hash();
		hash.add(jar);
		hash.add(entryName());
		return hash.getHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JarFileEntry)) {
			return false;
		}
		
		JarFileEntry other = (JarFileEntry) obj;
		return ObjectUtils.equals(jar, other.jar) 
				&& ObjectUtils.equals(entryName(), other.entryName());
	}
	
	public long getSize() {
		return jarEntry.getSize();
	}
	
	public long getTime() {
		return jarEntry.getTime();
	}
	
	public InputStream getInputStream() throws IOException {
		return overrideData == null ? jar.getInputStream(jarEntry)
				: new ByteArrayInputStream(overrideData);
	}
	
	public JarFile getJarFile() {
		return jar;
	}
	
	public JarEntry getEntry() {
		return jarEntry;
	}

	public byte[] getOverrideData() {
		return overrideData;
	}

	public void setOverrideData(byte[] overrideData) {
		this.overrideData = overrideData;
	}
	
}
