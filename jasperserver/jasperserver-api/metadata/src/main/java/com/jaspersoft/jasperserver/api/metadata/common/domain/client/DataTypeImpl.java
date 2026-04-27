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
package com.jaspersoft.jasperserver.api.metadata.common.domain.client;

import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import java.math.BigDecimal;
import java.util.Date;


/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: InputControlImpl.java 2332 2006-03-09 02:23:53Z tony $
 */
public class DataTypeImpl extends ResourceImpl implements DataType
{
	
	/**
	 * 
	 */
	private byte dataTypeType = TYPE_TEXT;
	private Integer maxLength = null;
	private Integer decimals = null;
	private String regularExpr = null;
	private Comparable minValue = null;
	private Comparable maxValue = null;
	private boolean isStrictMin = false;
	private boolean isStrictMax = false;


	/**
	 * 
	 */
	public byte getDataTypeType()
	{
		return dataTypeType;
	}

	/**
	 * 
	 */
	public void setDataTypeType(byte type)
	{
		this.dataTypeType = type;
	}

	/**
	 * 
	 */
	public Integer getMaxLength()
	{
		return maxLength;
	}

	/**
	 * 
	 */
	public void setMaxLength(Integer maxLength)
	{
		this.maxLength = maxLength;
	}

	/**
	 * 
	 */
	public Integer getDecimals()
	{
		return decimals;
	}

	/**
	 * 
	 */
	public void setDecimals(Integer decimals)
	{
		this.decimals = decimals;
	}

	/**
	 * 
	 */
	public String getRegularExpr()
	{
		return regularExpr;
	}

	/**
	 * 
	 */
	public void setRegularExpr(String regularExpr)
	{
		this.regularExpr = regularExpr;
	}

	/**
	 * 
	 */
    @XmlElements({
            @XmlElement(name = "stringMinValue", type = String.class),
            @XmlElement(name = "numberMinValue", type = BigDecimal.class),
            @XmlElement(name = "dateMinValue", type = Date.class)
    })
	public Comparable getMinValue()
	{
		return minValue;
	}

	/**
	 * 
	 */
	public void setMinValue(Comparable min)
	{
		this.minValue = min;
	}

	/**
	 * 
	 */
     @XmlElements({
            @XmlElement(name = "stringMaxValue", type = String.class),
            @XmlElement(name = "numberMaxValue", type = BigDecimal.class),
            @XmlElement(name = "dateMaxValue", type = Date.class)
    })
	public Comparable getMaxValue()
	{
		return maxValue;
	}

	/**
	 * 
	 */
	public void setMaxValue(Comparable max)
	{
		this.maxValue = max;
	}

	/**
	 * 
	 */
	public boolean isStrictMin()
	{
		return isStrictMin;
	}

	/**
	 * 
	 */
	public void setStrictMin(boolean isStrictMin)
	{
		this.isStrictMin = isStrictMin;
	}

	/**
	 * 
	 */
	public boolean isStrictMax()
	{
		return isStrictMax;
	}

	/**
	 * 
	 */
	public void setStrictMax(boolean isStrictMax)
	{
		this.isStrictMax = isStrictMax;
	}

	protected Class getImplementingItf() {
		return DataType.class;
	}

}
