package org.obsplatform.portfolio.servicepartnermapping.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.servicepartnermapping.data.ServicePartnerMappingData;
import org.obsplatform.portfolio.servicepartnermapping.service.ServicePartnerMappingReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Naresh
 * 
 */
@Path("/servicepartnermapping")
@Component
@Scope("singleton")
public class ServicePartnerMappingApiResource {

	private final Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "partnerName", "serviceId", "serviceCode",
					"serviceDescription", "serviceType"));

	private final String resourceNameForPermissions = "SERVICEPARTNERMAPPING";
	private final PlatformSecurityContext context;
	private final ServicePartnerMappingReadPlatformService servicePartnerMappingReadPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<ServicePartnerMappingData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;

	@Autowired
	public ServicePartnerMappingApiResource(final PlatformSecurityContext context,
			final ServicePartnerMappingReadPlatformService servicePartnerMappingReadPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final DefaultToApiJsonSerializer<ServicePartnerMappingData> toApiJsonSerializer,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService) {

		this.context = context;
		this.servicePartnerMappingReadPlatformService = servicePartnerMappingReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;

	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getAllServicePartnerMapping(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final SearchSqlQuery searchCodes = SearchSqlQuery.forSearch(sqlSearch, offset, limit);
		final Page<ServicePartnerMappingData> servicePartnerMappingData = this.servicePartnerMappingReadPlatformService.getAllServicePartnerMappingData(searchCodes);
		return this.toApiJsonSerializer.serialize(servicePartnerMappingData);
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getTemplateRelatedData(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ServicePartnerMappingData> serviceDatas = this.servicePartnerMappingReadPlatformService.getServiceCode();
		final List<ServicePartnerMappingData> partnerNames = this.servicePartnerMappingReadPlatformService.getPartnerNames();
		final ServicePartnerMappingData servicePartnerMappingData = new ServicePartnerMappingData(serviceDatas, partnerNames);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, servicePartnerMappingData, RESPONSE_PARAMETERS);

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createServicePartnerMapping(@Context final UriInfo uriInfo, final String jsonRequestBody) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createServicePartnerMapping().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("{servicePtrMappId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getServicePartnerMappingById(
			@PathParam("servicePtrMappId") final Long servicePtrMappId, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final ServicePartnerMappingData servicePartnerMappingData = servicePartnerMappingReadPlatformService.getServicePtrMappingById(servicePtrMappId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if (settings.isTemplate()) {
			servicePartnerMappingData.setServiceDatas(this.servicePartnerMappingReadPlatformService.getServiceCode(servicePartnerMappingData.getServiceId()));
			servicePartnerMappingData.setPartnerNames(this.servicePartnerMappingReadPlatformService.getPartnerNames());
		}
		return this.toApiJsonSerializer.serialize(settings, servicePartnerMappingData, RESPONSE_PARAMETERS);
	}

	@PUT
	@Path("{servicePtrMapId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateServicePartnerMapping(
			@PathParam("servicePtrMapId") final Long servicePtrMapId, final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateServicePartnerMapping(servicePtrMapId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@DELETE
	@Path("{servicePtrMapId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteProvisioningSystem(@PathParam("servicePtrMapId") final Long servicePtrMapId, final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteServicePartnerMapping(servicePtrMapId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}