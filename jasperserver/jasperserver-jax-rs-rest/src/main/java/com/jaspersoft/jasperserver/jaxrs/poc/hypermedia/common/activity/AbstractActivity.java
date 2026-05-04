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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity;

import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.HypermediaRepresentation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.Link;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.EmbeddedElement;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.visitor.RelationsVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import jakarta.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Igor.Nesterenko
 * @version: ${Id}
 */
public abstract class AbstractActivity<R extends  HypermediaRepresentation, D> implements Activity< R, D> {

    private static Logger log = LogManager.getLogger(AbstractActivity.class);

    protected GenericRequest genericRequest = new GenericRequest().setExpanded(true);

    protected R representation;

    protected D data;

    protected Link link;

    private RelationsVisitor visitor;

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    protected Map<Relation, Activity> relations;
    protected List<Relation> linkRelations;

    public AbstractActivity() {
        super();
        this.relations = new HashMap<Relation, Activity>();
        this.linkRelations = new ArrayList<Relation>();
    }

    public AbstractActivity(Map<Relation, Activity> relations, List<Relation> linkRelations) {
        super();
        this.relations = relations;
        this.linkRelations = linkRelations;
    }

    public Boolean isLink(Relation relation){
        return linkRelations.contains(relation);
    }

    @Override
    public abstract D findData(GenericRequest request);

    /**
     *
     * @return data - dto object
     * @deprecated: state shouldn't be in activity
     */
    @Override
    public D getData() {
        return data;
    }

    /**
     *
     * @param data - dto object without any hypermedia
     * @return this
     * @deprecated: state shouldn't be in activity
     */
    @Override
    public Activity setData(D data) {
        this.data = data;
        return this;
    }

    @Override
    public EmbeddedElement proceed() {

        if (data == null) {
            data = findData(genericRequest);
        }

        representation = buildRepresentation();

        if (visitor != null){

            visitor.setRepresentation(representation);

            for (Relation relation : relations.keySet()) {

                Activity relatedActivity = relations.get(relation);
                Boolean isLink = isLink(relation);

                Class<? extends Activity> activityClass = relatedActivity.getClass();
                Class<? extends Boolean> paramClass = isLink.getClass();
                Class<? extends Relation> relationClass = relation.getClass();

                try {

                    Method method = visitor.getClass().getDeclaredMethod("resolve", activityClass, relationClass, paramClass);
                    try {
                        method.invoke(visitor, relatedActivity, relation, isLink);
                    } catch (IllegalAccessException e) {
                        log.error(e);
                    } catch (InvocationTargetException e) {
                        log.error(e);
                    }

                } catch (NoSuchMethodException e) {
                    log.error(e);
                }
            }
        }

        return representation;
    }

    /**
     * @deprecated: replace with search criteria
     */
    @Override
    public Activity setGenericRequest(GenericRequest genericRequest) {
        this.genericRequest = genericRequest;
        return this;
    }

    /**
     * @deprecated: replace with search criteria
     */
    @Override
    public GenericRequest getGenericRequest() {
        return genericRequest;
    }

    @Override
    public Activity setRepresentation(R representation) {
        this.representation = representation;
        return this;
    }

    @Override
    public R getRepresentation() {
        return this.representation;
    }

    @Override
    public EmbeddedElement buildLink() {
        return null;
    }

    protected String getMessage(String key){
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public RelationsVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(RelationsVisitor visitor) {
        this.visitor = visitor;
    }
}

