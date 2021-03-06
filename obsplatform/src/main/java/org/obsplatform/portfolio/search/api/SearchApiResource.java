package org.obsplatform.portfolio.search.api;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.search.SearchConstants.SEARCH_RESPONSE_PARAMETERS;
import org.obsplatform.portfolio.search.data.SearchConditions;
import org.obsplatform.portfolio.search.data.SearchData;
import org.obsplatform.portfolio.search.service.SearchReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/search")
@Component
@Scope("singleton")
public class SearchApiResource {

    private final Set<String> searchResponseParameters = SEARCH_RESPONSE_PARAMETERS.getAllValues();

    private final String resourceNameForPermissions = "LOCATION";
   	private final PlatformSecurityContext context;
    private final SearchReadPlatformService searchReadPlatformService;
    private final ToApiJsonSerializer<SearchData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public SearchApiResource(final SearchReadPlatformService searchReadPlatformService,
            final ToApiJsonSerializer<SearchData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PlatformSecurityContext context) {

        this.searchReadPlatformService = searchReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.context = context;

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String searchData(@Context final UriInfo uriInfo, @QueryParam("query") final String query,@QueryParam("resource") final String resource) {

    	context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        final SearchConditions searchConditions = new SearchConditions(query, resource);

        final Collection<SearchData> searchResults = this.searchReadPlatformService.retriveMatchingData(searchConditions);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, searchResults, searchResponseParameters);
    }
}