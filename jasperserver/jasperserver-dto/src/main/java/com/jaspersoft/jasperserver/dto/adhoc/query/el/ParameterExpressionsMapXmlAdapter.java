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
package com.jaspersoft.jasperserver.dto.adhoc.query.el;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
public class ParameterExpressionsMapXmlAdapter extends XmlAdapter<ArrayList<ClientParameterExpression>, Map<String, ClientExpressionContainer>> {

    @Override
    public ArrayList<ClientParameterExpression> marshal(Map<String, ClientExpressionContainer> v) throws Exception {
        ArrayList<ClientParameterExpression> result = null;
        if (v != null) {
            result = new ArrayList<ClientParameterExpression>();
            for (String name : v.keySet()) {
                result.add(new ClientParameterExpression(name, v.get(name)));
            }
        }

        return result;
    }

    @Override
    public Map<String, ClientExpressionContainer> unmarshal(ArrayList<ClientParameterExpression> v) throws Exception {
        Map<String, ClientExpressionContainer> result = null;
        if(v != null) {
            result = new HashMap<String, ClientExpressionContainer>();
            for(ClientParameterExpression currentResource : v){
                result.put(currentResource.getName(), currentResource.getExpression());
            }
        }

        return result;
    }
}
