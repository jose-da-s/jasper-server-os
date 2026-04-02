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

import com.jaspersoft.jasperserver.dto.job.adapters.DaysByteXmlAdapter;
import com.jaspersoft.jasperserver.dto.job.adapters.MonthsByteXmlAdapter;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.SortedSet;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p/>
 * <p/>
 *
 * @author tetiana.iefimenko
 * @version $Id$
 * @see
 */
@XmlRootElement(name = "calendarTrigger")
public class ClientJobCalendarTrigger extends ClientJobTrigger {

    private String minutes;
    private String hours;
    private ClientCalendarDaysType daysType;
    private SortedSet<Byte> weekDays;
    private String monthDays;
    private SortedSet<Byte> months;

    public ClientJobCalendarTrigger() {
        super();
    }

    public ClientJobCalendarTrigger(ClientJobCalendarTrigger other) {
        super(other);
        this.minutes = other.getMinutes();
        this.hours = other.getHours();
        this.daysType = other.getDaysType();
        this.weekDays = copyOf(other.getWeekDays());
        this.monthDays = other.getMonthDays();
        this.months = copyOf(other.getMonths());
    }

    public String getMinutes() {
        return minutes;
    }

    public ClientJobCalendarTrigger setMinutes(String minutes) {
        this.minutes = minutes;
        return this;
    }

    public String getHours() {
        return hours;
    }

    public ClientJobCalendarTrigger setHours(String hours) {
        this.hours = hours;
        return this;
    }

    public String getMonthDays() {
        return monthDays;
    }

    public ClientJobCalendarTrigger setMonthDays(String monthDays) {
        this.monthDays = monthDays;
        return this;
    }

    public ClientCalendarDaysType getDaysType() {
        return daysType;
    }

    public ClientJobCalendarTrigger setDaysType(ClientCalendarDaysType daysType) {
        this.daysType = daysType;
        return this;
    }

    @XmlJavaTypeAdapter(DaysByteXmlAdapter.class)
    public SortedSet<Byte> getWeekDays() {
        return weekDays;
    }

    public ClientJobCalendarTrigger setWeekDays(SortedSet<Byte> weekDays) {
        this.weekDays = weekDays;
        return this;
    }

    @XmlJavaTypeAdapter(MonthsByteXmlAdapter.class)
    public SortedSet<Byte> getMonths() {
        return months;
    }

    public ClientJobCalendarTrigger setMonths(SortedSet<Byte> months) {
        this.months = months;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientJobCalendarTrigger that = (ClientJobCalendarTrigger) o;

        if (minutes != null ? !minutes.equals(that.minutes) : that.minutes != null) return false;
        if (hours != null ? !hours.equals(that.hours) : that.hours != null) return false;
        if (daysType != that.daysType) return false;
        if (weekDays != null ? !weekDays.equals(that.weekDays) : that.weekDays != null) return false;
        if (monthDays != null ? !monthDays.equals(that.monthDays) : that.monthDays != null) return false;
        return months != null ? months.equals(that.months) : that.months == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (minutes != null ? minutes.hashCode() : 0);
        result = 31 * result + (hours != null ? hours.hashCode() : 0);
        result = 31 * result + (daysType != null ? daysType.hashCode() : 0);
        result = 31 * result + (weekDays != null ? weekDays.hashCode() : 0);
        result = 31 * result + (monthDays != null ? monthDays.hashCode() : 0);
        result = 31 * result + (months != null ? months.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientJobCalendarTrigger{" +
                "minutes='" + minutes + '\'' +
                ", hours='" + hours + '\'' +
                ", daysType='" + daysType + '\'' +
                ", weekDays=" + weekDays +
                ", monthDays='" + monthDays + '\'' +
                ", months=" + months +
                "} " + super.toString();
    }
}
