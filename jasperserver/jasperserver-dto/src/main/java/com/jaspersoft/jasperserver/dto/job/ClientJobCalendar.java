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
package com.jaspersoft.jasperserver.dto.job;


import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.job.adapters.ExcludeDaysXmlAdapter;
import com.jaspersoft.jasperserver.dto.job.adapters.TimeZoneXmlAdapter;
import com.jaspersoft.jasperserver.dto.job.wrappers.ClientExcludeDaysWrapper;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "reportJobCalendar")
public class ClientJobCalendar implements DeepCloneable<ClientJobCalendar> {
    public enum Type {
        annual,
        base,
        cron,
        daily,
        holiday,
        monthly,
        weekly;
    }

    private Type calendarType;
    // base fields
    private ClientJobCalendar baseCalendar;
    private String description;
    private TimeZone timeZone;
    //annual calendar's fields
    //excludeDays field used for holiday calendar too
    private ArrayList<Calendar> excludeDays = new ArrayList<java.util.Calendar>();

    // true, if excludeDays is sorted
    private Boolean dataSorted;
    //cron calendar's fields
    private String cronExpression;

    //daily calendar's fields
    private Calendar rangeStartingCalendar;

    private Calendar rangeEndingCalendar;
    private Boolean invertTimeRange;
    //monthly calendar's fields
    // An array to store a months days which are to be excluded.
    // java.util.Calendar.get( ) as index.
    private boolean[] excludeDaysFlags;


    public ClientJobCalendar() {
    }

    public ClientJobCalendar(ClientJobCalendar other) {
        checkNotNull(other);

        this.calendarType = other.getCalendarType();
        this.baseCalendar = copyOf(other.getBaseCalendar());
        this.description = other.getDescription();
        this.timeZone = copyOf(other.getTimeZone());
        this.excludeDays = copyOf(other.getExcludeDays());
        this.dataSorted = other.isDataSorted();
        this.cronExpression = other.getCronExpression();
        this.rangeStartingCalendar = copyOf(other.getRangeStartingCalendar());
        this.rangeEndingCalendar = copyOf(other.getRangeEndingCalendar());
        this.invertTimeRange = other.isInvertTimeRange();
        this.excludeDaysFlags = copyOf(other.getExcludeDaysFlags());
    }

    public Type getCalendarType() {
        return calendarType;
    }


    public ClientJobCalendar setCalendarType(Type calendarType) {
        this.calendarType = calendarType;
        return this;
    }

    public ClientJobCalendar getBaseCalendar() {
        return baseCalendar;
    }

    public ClientJobCalendar setBaseCalendar(ClientJobCalendar baseCalendar) {
        this.baseCalendar = baseCalendar;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ClientJobCalendar setDescription(String description) {
        this.description = description;
        return this;
    }

    @XmlJavaTypeAdapter(TimeZoneXmlAdapter.class)
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public ClientJobCalendar setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    @XmlJavaTypeAdapter(ExcludeDaysXmlAdapter.class)
    public ArrayList<Calendar> getExcludeDays() {
        return excludeDays;
    }

    public ClientJobCalendar setExcludeDays(ArrayList<Calendar> excludeDays) {
        this.excludeDays = (excludeDays == null) ? new ArrayList<Calendar>() : excludeDays;
        setExcludeDaysTimeZone(this.excludeDays);
        return this;
    }

    public ClientJobCalendar setExcludeDaysTimeZone(ArrayList<Calendar> excludeDays) {
        for(Calendar excludeDay: excludeDays) {
            if(timeZone != null) {
                excludeDay.setTimeZone(timeZone);
            }
        }
        return this;
    }

    public Boolean isDataSorted() {
        return dataSorted;
    }

    public ClientJobCalendar setDataSorted(Boolean dataSorted) {
        this.dataSorted = dataSorted;
        return this;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public ClientJobCalendar setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    public Calendar getRangeStartingCalendar() {
        return rangeStartingCalendar;
    }

    public ClientJobCalendar setRangeStartingCalendar(Calendar rangeStartingCalendar) {
        this.rangeStartingCalendar = rangeStartingCalendar;
        return this;
    }

    public Calendar getRangeEndingCalendar() {
        return rangeEndingCalendar;
    }

    public ClientJobCalendar setRangeEndingCalendar(Calendar rangeEndingCalendar) {
        this.rangeEndingCalendar = rangeEndingCalendar;
        return this;
    }

    public Boolean isInvertTimeRange() {
        return invertTimeRange;
    }

    public ClientJobCalendar setInvertTimeRange(Boolean invertTimeRange) {
        this.invertTimeRange = invertTimeRange;
        return this;
    }

    @XmlElementWrapper(name = "excludeDaysFlags")
    @XmlElement(name = "excludeDayFlag")
    public boolean[] getExcludeDaysFlags() {
        return excludeDaysFlags;
    }

    public ClientJobCalendar setExcludeDaysFlags(boolean[] excludeDaysFlags) {
        this.excludeDaysFlags = excludeDaysFlags;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientJobCalendar)) return false;

        ClientJobCalendar that = (ClientJobCalendar) o;

        if (calendarType != that.calendarType) return false;
        if (baseCalendar != null ? !baseCalendar.equals(that.baseCalendar) : that.baseCalendar != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (timeZone != null ? !timeZone.equals(that.timeZone) : that.timeZone != null) return false;
        if (!excludeDays.equals(that.excludeDays)) return false;
        if (dataSorted != null ? !dataSorted.equals(that.dataSorted) : that.dataSorted != null) return false;
        if (cronExpression != null ? !cronExpression.equals(that.cronExpression) : that.cronExpression != null)
            return false;
        if (rangeStartingCalendar != null ? !rangeStartingCalendar.equals(that.rangeStartingCalendar) : that.rangeStartingCalendar != null)
            return false;
        if (rangeEndingCalendar != null ? !rangeEndingCalendar.equals(that.rangeEndingCalendar) : that.rangeEndingCalendar != null)
            return false;
        if (invertTimeRange != null ? !invertTimeRange.equals(that.invertTimeRange) : that.invertTimeRange != null)
            return false;
        return Arrays.equals(excludeDaysFlags, that.excludeDaysFlags);
    }

    @Override
    public int hashCode() {
        int result = calendarType != null ? calendarType.hashCode() : 0;
        result = 31 * result + (baseCalendar != null ? baseCalendar.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + excludeDays.hashCode();
        result = 31 * result + (dataSorted != null ? dataSorted.hashCode() : 0);
        result = 31 * result + (cronExpression != null ? cronExpression.hashCode() : 0);
        result = 31 * result + (rangeStartingCalendar != null ? rangeStartingCalendar.hashCode() : 0);
        result = 31 * result + (rangeEndingCalendar != null ? rangeEndingCalendar.hashCode() : 0);
        result = 31 * result + (invertTimeRange != null ? invertTimeRange.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(excludeDaysFlags);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobCalendar{" +
                "baseCalendar=" + baseCalendar +
                ", calendarType=" + calendarType +
                ", description='" + description + '\'' +
                ", timeZone=" + timeZone +
                ", excludeDays=" + excludeDays +
                ", dataSorted=" + dataSorted +
                ", cronExpression='" + cronExpression + '\'' +
                ", rangeStartingCalendar=" + rangeStartingCalendar +
                ", rangeEndingCalendar=" + rangeEndingCalendar +
                ", invertTimeRange=" + invertTimeRange +
                ", excludeDaysFlags=" + Arrays.toString(excludeDaysFlags) +
                '}';
    }

    @Override
    public ClientJobCalendar deepClone() {
        return new ClientJobCalendar(this);
    }
}
