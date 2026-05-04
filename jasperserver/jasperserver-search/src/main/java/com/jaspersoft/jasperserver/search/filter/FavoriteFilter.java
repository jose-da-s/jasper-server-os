package com.jaspersoft.jasperserver.search.filter;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.FavoriteResourceService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.search.SearchCriteria;
import com.jaspersoft.jasperserver.core.util.DBUtil;
import com.jaspersoft.jasperserver.search.common.SearchAttributes;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import com.jaspersoft.jasperserver.search.state.State;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.Restrictions;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class FavoriteFilter extends BaseSearchFilter implements Serializable {

    protected TenantService tenantService;

    @Resource(name = "${bean.favoriteResourceService}")
    private FavoriteResourceService favoriteResourceService;

    public void applyRestrictions(String type, ExecutionContext context, SearchCriteria criteria) {
        RepositorySearchCriteria searchCriteria = getTypedAttribute(context, RepositorySearchCriteria.class);
        SearchAttributes searchAttributes = getSearchAttributes(context);
        User user = null;
        if (context.getAttributes() != null) {
            for (Object o : context.getAttributes()) {
                if (o instanceof User) {
                    user = (User) o;
                }
            }
        }
        user = user == null && searchCriteria!=null ? searchCriteria.getUser() : user;
        String favoriteFilter = Optional.ofNullable(searchAttributes)
                .map(SearchAttributes::getState)
                .map(State::getCustomFiltersMap)
                .map(r -> r.get("favoriteFilter"))
                .orElse(null);
        boolean isFilterEnabled = favoriteFilter != null && favoriteFilter.equals("favoriteFilter-favorites");
        if (isFilterEnabled || (searchCriteria!=null && searchCriteria.isFavorites())) {
            createCriteria(criteria, user, isFilterEnabled);
        }
    }

    private void createCriteria(SearchCriteria criteria, User user, boolean isFilterEnabled) {
        List idList = favoriteResourceService.getFavoriteIdsForUser(user, isFilterEnabled);
        if (CollectionUtils.isNotEmpty(idList)) {
                criteria.add(DBUtil.getBoundedInCriterion("id", idList));
            }
       else {
           //To return empty
            criteria.add(Restrictions.eq("id", -1L));
        }
    }

}


