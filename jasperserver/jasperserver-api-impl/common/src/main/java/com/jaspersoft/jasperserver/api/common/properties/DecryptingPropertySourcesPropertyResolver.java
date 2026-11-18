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

package com.jaspersoft.jasperserver.api.common.properties;

import static com.jaspersoft.jasperserver.crypto.conf.Defaults.BuildEnc;

import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.lang.Nullable;

import com.jaspersoft.jasperserver.crypto.EncryptionEngine;
import com.jaspersoft.jasperserver.crypto.KeystoreManager;

public class DecryptingPropertySourcesPropertyResolver extends PropertySourcesPropertyResolver {

	/**
	 * Create a new resolver against the given property sources. (just pass on to super constructor)
	 * @param propertySources the set of {@link PropertySource} objects to use
	 */
	public DecryptingPropertySourcesPropertyResolver(@Nullable PropertySources propertySources) {
		super(propertySources);
	}


	/**
	 * If string, trim. If encrypted, decrypt and return. Otherwise, return as it is returned from super.
	 */
	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
		T value = super.getProperty(key, targetValueType, resolveNestedPlaceholders);
		if (targetValueType == String.class && value != null) {
			String stringValue = (String) value;
			stringValue = stringValue.trim();
			if (EncryptionEngine.isEncrypted(stringValue)) {
				KeystoreManager ksManager = KeystoreManager.getInstance();
				stringValue = EncryptionEngine.decrypt(ksManager.getKeystore(null).getKey(BuildEnc.getConfId()), stringValue);
			}
			value = (T) stringValue;
		}
		return value;
	}
}
