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
import com.jaspersoft.jasperserver.dto.job.adapters.NoTimezoneDateToStringXmlAdapter;

import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.lang.reflect.Constructor;
import java.util.Date;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p/>
 * <p/>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
public abstract class ClientJobTrigger implements DeepCloneable<ClientJobTrigger>{

    /**
     * Start type that indicates that the job should be scheduled inFolder start
     * immediately.
     *
     * @see #getStartType()
     */
    public static final byte START_TYPE_NOW = 1;

    /**
     * Start type that indicates that the job should be scheduled inFolder start
     * at the specified start date.
     *
     * @see #getStartType()
     * @see #getStartDate()
     */
    public static final byte START_TYPE_SCHEDULE = 2;

    private Long id;
    private Integer version;
    private String timezone;
    private String calendarName;
    private int startType;
    private Date startDate;
    private Date endDate;
    private Integer misfireInstruction;

    public ClientJobTrigger() {
    }

    public ClientJobTrigger(ClientJobTrigger other) {
        checkNotNull(other);

        this.id = other.getId();
        this.version = other.getVersion();
        this.timezone = other.getTimezone();
        this.calendarName = other.getCalendarName();
        this.startType = other.getStartType();
        this.startDate = copyOf(other.getStartDate());
        this.endDate = copyOf(other.getEndDate());
        this.misfireInstruction = other.misfireInstruction;
    }

    public Long getId() {
        return id;
    }

    public ClientJobTrigger setId(Long id) {
        this.id = id;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public ClientJobTrigger setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public String getTimezone() {
        return timezone;
    }

    public ClientJobTrigger setTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public ClientJobTrigger setCalendarName(String calendarName) {
        this.calendarName = calendarName;
        return this;
    }

    public int getStartType() {
        return startType;
    }

    public ClientJobTrigger setStartType(int startType) {
        this.startType = startType;
        return this;
    }

    @XmlJavaTypeAdapter(NoTimezoneDateToStringXmlAdapter.class)
    public Date getStartDate() {
        return startDate;
    }

    public ClientJobTrigger setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    @XmlJavaTypeAdapter(NoTimezoneDateToStringXmlAdapter.class)
    public Date getEndDate() {
        return endDate;
    }

    public ClientJobTrigger setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public Integer getMisfireInstruction() {
        return misfireInstruction;
    }

    public ClientJobTrigger setMisfireInstruction(Integer misfireInstruction) {
        this.misfireInstruction = misfireInstruction;
        return this;
    }

    public ClientJobTrigger deepClone() {
        Class<? extends ClientJobTrigger> thisClass = this.getClass();

        ClientJobTrigger instance = null;
        try {
            Constructor<? extends ClientJobTrigger> constructor = thisClass.getConstructor(thisClass);
            instance = constructor.newInstance(this);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientJobTrigger)) return false;

        ClientJobTrigger trigger = (ClientJobTrigger) o;

        if (startType != trigger.startType) return false;
        if (calendarName != null ? !calendarName.equals(trigger.calendarName) : trigger.calendarName != null)
            return false;
        if (endDate != null ? !endDate.equals(trigger.endDate) : trigger.endDate != null) return false;
        if (id != null ? !id.equals(trigger.id) : trigger.id != null) return false;
        if (misfireInstruction != null ? !misfireInstruction.equals(trigger.misfireInstruction) : trigger.misfireInstruction != null)
            return false;
        if (startDate != null ? !startDate.equals(trigger.startDate) : trigger.startDate != null) return false;
        if (timezone != null ? !timezone.equals(trigger.timezone) : trigger.timezone != null) return false;
        if (version != null ? !version.equals(trigger.version) : trigger.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (timezone != null ? timezone.hashCode() : 0);
        result = 31 * result + (calendarName != null ? calendarName.hashCode() : 0);
        result = 31 * result + startType;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (misfireInstruction != null ? misfireInstruction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobTrigger{" +
                "id=" + id +
                ", version=" + version +
                ", timezone='" + timezone + '\'' +
                ", calendarName='" + calendarName + '\'' +
                ", startType=" + startType +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", misfireInstruction=" + misfireInstruction +
                '}';
    }
}
