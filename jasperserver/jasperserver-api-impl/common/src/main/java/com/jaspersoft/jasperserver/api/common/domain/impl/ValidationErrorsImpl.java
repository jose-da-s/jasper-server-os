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
package com.jaspersoft.jasperserver.api.common.domain.impl;

import com.jaspersoft.jasperserver.api.common.domain.ValidationError;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@XmlRootElement(name = "errors")
public class ValidationErrorsImpl implements ValidationErrors, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final List errors;
	
	public ValidationErrorsImpl() {
		errors = new ArrayList();
	}

	public ValidationErrorsImpl(List errors) {
		this.errors = errors;
	}

	public boolean isError() {
		return !errors.isEmpty();
	}

    @XmlElement(name =  "error", type = ValidationErrorImpl.class)
	public List getErrors() {
		return errors;
	}

	public void add(ValidationError error) {
		errors.add(error);
	}

	public void addErrors(List errors) {
		this.errors.addAll(errors);
	}
	
	public String toString() {
		if (!isError()) {
			return "No errors";
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(errors.size());
		sb.append(" error(s)\n");
		for (Iterator it = errors.iterator(); it.hasNext();) {
			ValidationError error = (ValidationError) it.next();
			sb.append(error.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

	public void removeError(String code, String field) {
		for (Iterator it = errors.iterator(); it.hasNext();) {
			ValidationError error = (ValidationError) it.next();
			if (matches(error, code, field)) {
				it.remove();
			}
		}
	}

	protected boolean matches(ValidationError error, String code, String field) {
		return code.equals(error.getErrorCode())
				&& field.equals(error.getField());
	}
}
