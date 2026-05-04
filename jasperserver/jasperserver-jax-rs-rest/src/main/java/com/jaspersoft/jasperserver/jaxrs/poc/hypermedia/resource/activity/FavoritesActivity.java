package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.activity;

import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.MediaTypes;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.Activity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.Link;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.EmbeddedElement;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.PluralEmbeddedElement;
import com.jaspersoft.jasperserver.search.mode.SearchMode;

import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

public class FavoritesActivity extends SearchResourcesActivity{

    public FavoritesActivity() {
        super();
    }

    public FavoritesActivity(Map<Relation, Activity> relations, List<Relation> linkRelations) {
        super(relations, linkRelations);
    }

    @Override
    public EmbeddedElement buildLink() {
        PluralEmbeddedElement embeddedElements = null;
        if (criteria != null){
            Boolean isSearch = getOwnRelation() == Relation.favorites;
            embeddedElements =  new PluralEmbeddedElement(getOwnRelation());
            String message = isSearch ? getMessage("view.list") : getMessage("view.repository");
            embeddedElements.add(new Link()
                        .setHref(super.buildRestUrl())
                        .setTitle(message)
                        .setType(MediaTypes.APPLICATION_HAL_JSON)
                        .setProfile("GET")
                        .setRelation(getOwnRelation())
            );
        }else{
            throw new IllegalStateException("Search criteria isn't initialized, wrong state of activity");
        }
        return embeddedElements;
    }

    @Override
    public Relation getOwnRelation() {
        return  Relation.favorites;
    }
}
