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

package com.jaspersoft.jasperserver.war.util;

import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import com.jaspersoft.jasperserver.api.common.util.CharacterEncodingProvider;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class DefaultHttpUtils implements HttpUtils {

	public static class HeaderEncoder {
		private Pattern userAgentPattern;
		private StringEncoder headerEncoder;
		
		public boolean matches(String userAgent) {
			return userAgentPattern.matcher(userAgent).matches();
		}
		
		public String encodeHeader(String text, String charset) {
			return headerEncoder.encode(text, charset);
		}

		public Pattern getUserAgentPattern() {
			return userAgentPattern;
		}

		public void setUserAgentPattern(Pattern userAgentPattern) {
			this.userAgentPattern = userAgentPattern;
		}

		public StringEncoder getHeaderEncoder() {
			return headerEncoder;
		}

		public void setHeaderEncoder(StringEncoder headerEncoder) {
			this.headerEncoder = headerEncoder;
		}
	}
	
	private CharacterEncodingProvider characterEncodingProvider;
	private HeaderEncoder[] headerEncoders;
	
	public String encodeContentFilename(HttpServletRequest request, String filename) {
		String encodedFilename = filename;
		String userAgent = request.getHeader("User-Agent").toLowerCase();
		String charset = characterEncodingProvider.getCharacterEncoding();
		for (int i = 0; i < headerEncoders.length; i++) {
			HeaderEncoder encoder = headerEncoders[i];
			if (encoder.matches(userAgent)) {
				encodedFilename = encoder.encodeHeader(filename, charset);
				break;
			}
		}
		return encodedFilename;
	}

	public CharacterEncodingProvider getCharacterEncodingProvider() {
		return characterEncodingProvider;
	}

	public void setCharacterEncodingProvider(
			CharacterEncodingProvider characterEncodingProvider) {
		this.characterEncodingProvider = characterEncodingProvider;
	}

	public HeaderEncoder[] getHeaderEncoders() {
		return headerEncoders;
	}

	public void setHeaderEncoders(HeaderEncoder[] headerEncoders) {
		this.headerEncoders = headerEncoders;
	}

}

