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
package com.jaspersoft.jasperserver.war.webflow;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.View;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class JsonModelView implements View {

	private static final Log log = LogFactory.getLog(JsonModelView.class);

	private final String[] modelNames;

	public JsonModelView(String ... modelNames) {
		this.modelNames = modelNames;
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}

	public void render(Map<String, ?> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("rendering json model view");
		}
		
		LinkedHashMap<String, Object> responseMap = new LinkedHashMap<String, Object>();
		for (String modelName : modelNames) {
			Object modelObject = model.get(modelName);
			if (modelObject != null) {
				responseMap.put(modelName, modelObject);
			}
		}
		
		response.setContentType("application/json; charset=UTF-8");
		
		ObjectMapper jsonMapper = new ObjectMapper();
		ServletOutputStream out = response.getOutputStream();
		jsonMapper.writeValue(out, responseMap);
	}

}

