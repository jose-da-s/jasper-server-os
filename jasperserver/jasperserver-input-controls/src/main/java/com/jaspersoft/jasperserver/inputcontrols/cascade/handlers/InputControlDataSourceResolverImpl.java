package com.jaspersoft.jasperserver.inputcontrols.cascade.handlers;

import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedEngineService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedRepositoryService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.token.FilterResolver;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * <p></p>
 *
 * @author Vlad Zavadskyi
 */
@Service
public class InputControlDataSourceResolverImpl implements InputControlDataSourceResolver {
    @jakarta.annotation.Resource
    protected CachedRepositoryService cachedRepositoryService;
    @jakarta.annotation.Resource
    protected FilterResolver filterResolver;
    @jakarta.annotation.Resource
    protected CachedEngineService cachedEngineService;

    @Override
    public ResourceReference resolveDatasource(InputControl inputControl, ResourceReference dataSourceReference) throws CascadeResourceNotFoundException {
        ResourceReference queryReference = inputControl.getQuery();
        if (queryReference != null) {
            Resource queryResource = cachedRepositoryService.getResource(Resource.class, queryReference);
            if (queryResource instanceof Query && ((Query) queryResource).getDataSource() != null) {
                dataSourceReference = ((Query) queryResource).getDataSource();
            }
        }
        return dataSourceReference;
    }

    @Override
    public Map<String, Object> prepareDomainDataSourceParameters(ResourceReference dataSourceReference) throws CascadeResourceNotFoundException {
        ReportDataSource dataSource = (ReportDataSource) cachedRepositoryService.getResource(Resource.class, dataSourceReference);
        if (filterResolver.paramTestNeedsDataSourceInit(dataSource)) {
            return cachedEngineService.getSLParameters(dataSource);
        }
        return Collections.emptyMap();
    }
}
