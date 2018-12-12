package org.obsplatform.portfolio.service.api;

import java.util.Arrays;
import java.util.Collection;
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

import org.obsplatform.billing.emun.data.EnumValuesData;
import org.obsplatform.billing.emun.service.EnumReadplaformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.obsplatform.portfolio.service.data.ServiceMasterData;
import org.obsplatform.portfolio.service.service.ServiceMasterReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo 
 * this api class use to create,update and delete different services
 * 
 */
@Path("/servicemasters")
@Component
@Scope("singleton")
public class ServiceMasterApiResource {
    
	/**
	 * The set of parameters that are supported in response for
	 * {@link ServiceMasterData}.
	 */
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "serviceType", "serviceCode",
					"serviceDescription", "serviceTypes", "serviceUnitTypes","serviceUnitTypes", "isOptional", "status",
					"serviceStatus", "isAutoProvision"));
	private final static String RESOURCENAMEFORPERMISSIONS = "SERVICE";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<ServiceMasterData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ServiceMasterReadPlatformService serviceMasterReadPlatformService;
	private final PlanReadPlatformService planReadPlatformService;
	private final EnumReadplaformService enumReadplaformService;

	@Autowired
	public ServiceMasterApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<ServiceMasterData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final ServiceMasterReadPlatformService serviceMasterReadPlatformService,
			final PlanReadPlatformService planReadPlatformService,
			final EnumReadplaformService enumReadplaformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.enumReadplaformService = enumReadplaformService;
		this.planReadPlatformService = planReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.serviceMasterReadPlatformService = serviceMasterReadPlatformService;

	}

	/**
	 * using this method posting service data
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewService(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createService().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * using this method getting all services data
	 */
	/*@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllService(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("limit") final Integer limit,@QueryParam("offset") final Integer offset) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final SearchSqlQuery searchCodes = SearchSqlQuery.forSearch(sqlSearch,offset, limit);
		final Page<ServiceMasterData> masterOptionsDatas = this.serviceMasterReadPlatformService.retrieveServices(searchCodes);
		return this.toApiJsonSerializer.serialize(masterOptionsDatas);
	}*/
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllService(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("limit") final Integer limit,@QueryParam("offset") final Integer offset) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final SearchSqlQuery searchCodes = SearchSqlQuery.forSearch(sqlSearch,offset, limit);
		final Page<ServiceMasterData> masterOptionsDatas = this.serviceMasterReadPlatformService.retrieveServices(searchCodes);
		return this.toApiJsonSerializer.serialize(masterOptionsDatas);
	}

	/**
	 * using this method getting template data for service
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTempleteInfo(@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final ServiceMasterData masterOptionsData = handleTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, masterOptionsData,RESPONSE_DATA_PARAMETERS);
	}

	private ServiceMasterData handleTemplateData() {

		final Collection<EnumValuesData> serviceType = this.enumReadplaformService.getEnumValues("service_type");
		final List<EnumOptionData> status = this.planReadPlatformService.retrieveNewStatus();
		final List<EnumOptionData> serviceUnitType = this.serviceMasterReadPlatformService.retrieveServiceUnitType();
		return new ServiceMasterData(serviceType, serviceUnitType,status);

	}

	/**
	 * this method for getting service data using by id
	 */
	@GET
	@Path("{serviceId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleServiceDetails(@PathParam("serviceId") final Long serviceId,
			@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final ServiceMasterData productData = this.serviceMasterReadPlatformService.retrieveIndividualService(serviceId);
		final ServiceMasterData masterOptionsData = handleTemplateData();
		productData.setStatus(masterOptionsData.getStatus());
		productData.setServiceTypes(masterOptionsData.getServiceTypes());
		productData.setServiceUnitTypes(masterOptionsData.getServiceUnitTypes());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, productData,RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * using this method editing single service data
	 */
	@PUT
	@Path("{serviceId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateServiceData(@PathParam("serviceId") final Long serviceId,final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateService(serviceId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * using this method deleting single service data
	 */
	@DELETE
	@Path("{serviceId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteServiceData(@PathParam("serviceId") final Long serviceId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteService(serviceId).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);

	}

}
