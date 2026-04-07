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
package com.jaspersoft.jasperserver.api.engine.common.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.LogEvent;
import com.jaspersoft.jasperserver.api.common.domain.impl.RepoLogEvent;
import com.jaspersoft.jasperserver.api.engine.common.service.LoggingService;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.common.user.UserPersistenceHandler;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoUser;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service interface used for persistent logging of scheduled reports.
 *
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class HibernateLoggingService extends HibernateDaoSupport implements LoggingService {

    private static final Log log = LogFactory.getLog(HibernateLoggingService.class);

    private static final long EVENT_ID_NEW = 0l;

    private SecurityContextProvider securityContextProvider;
    private int maximumAge;
    private ResourceFactory objectFactory;
    private UserPersistenceHandler userHandler;

    public void setUserHandler(UserPersistenceHandler userHandler) {
        this.userHandler = userHandler;
    }

    public void setObjectMappingFactory(ResourceFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void setSecurityContextProvider(SecurityContextProvider securityContextProvider) {
        this.securityContextProvider = securityContextProvider;
    }

    public int getMaximumAge() {
        return maximumAge;
    }

    public void setMaximumAge(int days) {
        this.maximumAge = days;
    }

    public LogEvent instantiateLogEvent() {
        return createLogEvent();
    }

    protected LogEvent createLogEvent() {
        return new RepoLogEvent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void log(LogEvent event) {
        prepareForSave(event);
        getHibernateTemplate().save(event);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void update(LogEvent event) {
        RepoLogEvent repoLogEvent = getRepoLogEvent(event.getId());
        repoLogEvent.copyFromClient(event);

        getHibernateTemplate().saveOrUpdate(repoLogEvent);
    }

    protected void prepareForSave(LogEvent event) {
        event.setId(EVENT_ID_NEW);
        if (event.getOccurrenceDate() == null) {
            event.setOccurrenceDate(new Date());
        }
        RepoLogEvent repoLogEvent = (RepoLogEvent) event;

        RepoUser persistentUser = userHandler.getPersistentUserFromContext();
        repoLogEvent.setUser(persistentUser);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void purge() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -getMaximumAge());
        Date last = cal.getTime();

        if (log.isDebugEnabled()) {
            log.debug("Purging log events older than " + last);
        }
        getHibernateTemplate().execute((s)-> s.createQuery("delete RepoLogEvent e where e.occurrenceDate < ?1")
                .setParameter(1, last)
                .executeUpdate());
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(ExecutionContext context, long[] eventIds) {
        for (long eventId : eventIds) {
            LogEvent event = getRepoLogEvent(eventId);
            getHibernateTemplate().delete(event);
        }
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public List getUserEvents(ExecutionContext context) {
        List<RepoLogEvent> events;
        RepoUser user = userHandler.getPersistentUserFromContext();
        if (user != null) {
            DetachedCriteria criteria = DetachedCriteria.forClass(RepoLogEvent.class);
            criteria.add(Restrictions.eq("user", user));
            criteria.addOrder(Order.desc("occurrenceDate"));

            //noinspection unchecked
            events = (List<RepoLogEvent>)getHibernateTemplate().findByCriteria(criteria);
        } else {
            events = Collections.emptyList();
        }

        return getClientEvents(events);
    }

    private List<LogEvent> getClientEvents(List<RepoLogEvent> repoEvents) {
        List<LogEvent> events = new ArrayList<LogEvent>();

        for (RepoLogEvent repoEvent : repoEvents) {
            events.add(repoEvent.toClient(objectFactory));
        }
        return events;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<LogEvent> getUnreadEvents(ExecutionContext context) {
        List<RepoLogEvent> events;
        RepoUser user = userHandler.getPersistentUserFromContext();
        if (user != null) {
            DetachedCriteria criteria = DetachedCriteria.forClass(RepoLogEvent.class);
            criteria.add(Restrictions.eq("state", LogEvent.STATE_UNREAD));
            criteria.add(Restrictions.eq("user", user));
            criteria.addOrder(Order.desc("occurrenceDate"));

            //noinspection unchecked
            events = (List<RepoLogEvent>) getHibernateTemplate().findByCriteria(criteria);
        } else {
            events = Collections.emptyList();
        }

        return getClientEvents(events);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public LogEvent getLogEvent(ExecutionContext context, long id) {
        return getRepoLogEvent(id).toClient(objectFactory);
    }

    protected RepoLogEvent getRepoLogEvent(long id) {
        RepoLogEvent event = getHibernateTemplate().get(RepoLogEvent.class, id);
        if (event == null) {
            log.debug("Log event with id " + id + " not found");
        } else {
            User user = securityContextProvider.getContextUser();
            if (event.getUser() == null) {
                log.debug("Log event with id " + id + " has no user.");
            } else {
                User eventUser = (User) ((RepoUser) event.getUser()).toClient(objectFactory);
                if (user.getTenantId() == null) {
                    if (!eventUser.getUsername().equals(user.getUsername()) || eventUser.getTenantId() != null) {
                        log.debug("Log event with id " + id + " does not belongs to user [username=\"" +
                                user.getUsername() + "\", tenantId=\"" + user.getTenantId() + "\".");
                        event = null;
                    }
                }
            }
        }

        return event;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int getUserEventsCount(ExecutionContext context) {
        //TODO: FIXME - at the moment method returns all the events.
        String username = securityContextProvider.getContextUsername();

        Object result = getHibernateTemplate().execute((s)-> s.createQuery("select count(*) from RepoLogEvent where state=?1")
                .setParameter(1, new Byte(LogEvent.STATE_UNREAD))
                .uniqueResult());

        if (result != null) {
            return (Integer) result;
        }
        return 0;
    }
}
