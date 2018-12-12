package org.obsplatform.portfolio.servicemapping.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
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
import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.finance.payments.service.PaymentReadPlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.obsplatform.portfolio.servicemapping.data.ServiceCodeData;
import org.obsplatform.portfolio.servicemapping.data.ServiceMappingData;
import org.obsplatform.portfolio.servicemapping.service.ServiceMappingReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * this api class used to define,update different services mapping details 
 */
@Path("/servicemapping")
@Component
@Scope("singleton")
public class ServiceMappingApiResource {

	/**
	 * The set of parameters that are supported in response for
	 * {@link ServiceMappingData}.
	 */
	private final Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "serviceCode", "serviceId","serviceIdentification", "status", "image"));
	
	private final String resourceNameForPermissions = "SERVICEMAPPING";
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<ServiceMappingData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PlatformSecurityContext context;
	private final ServiceMappingReadPlatformService serviceMappingReadPlatformService;
	private final PlanReadPlatformService planReadPlatformService;
	private final PaymentReadPlatformService paymodeReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;

	@Autowired
	public ServiceMappingApiResource(final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final DefaultToApiJsonSerializer<ServiceMappingData> toApiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper,
			final PlatformSecurityContext context,final ServiceMappingReadPlatformService serviceMappingReadPlatformService,
			final PlanReadPlatformService planReadPlatformService,final PaymentReadPlatformService paymodeReadPlatformService,
			final MCodeReadPlatformService mCodeReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.planReadPlatformService = planReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.mCodeReadPlatformService=mCodeReadPlatformService;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.serviceMappingReadPlatformService = serviceMappingReadPlatformService;
		this.paymodeReadPlatformService = paymodeReadPlatformService;

	}

	/**
	 * @param uriInfo
	 * @param sqlSearch
	 * @param limit
	 * @param offset
	 * @return retrieved all defined servicemap details
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getServiceMapping(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch, @QueryParam("limit") final Integer limit,
			@QueryParam("offset") final Integer offset) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final SearchSqlQuery searchCodes =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		final Page<ServiceMappingData> serviceMapping = this.serviceMappingReadPlatformService.getServiceMapping(searchCodes);
		return this.toApiJsonSerializer.serialize(serviceMapping);
	}

	/**
	 * @param uriInfo
	 * @return retrieved drop down data for defining a new servicemapping details 
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getTemplateRelatedData(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ServiceCodeData> serviceCodeData = this.serviceMappingReadPlatformService.getServiceCode();
		final List<EnumOptionData> status = this.planReadPlatformService.retrieveNewStatus();
		final Collection<McodeData> categories = this.paymodeReadPlatformService.retrievemCodeDetails("Service Category");
		final Collection<McodeData> subCategories = this.paymodeReadPlatformService.retrievemCodeDetails("Asset language");
		final Collection<MCodeData> provisionSysData = this.mCodeReadPlatformService.getCodeValue("Provisioning");
		final List<ItemData> itemsData = this.serviceMappingReadPlatformService.retrieveItems();
		final ServiceMappingData serviceMappingData = new ServiceMappingData(null,serviceCodeData, status, null, categories, subCategories,provisionSysData,itemsData);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, serviceMappingData, RESPONSE_PARAMETERS);
	}

	
	/**
	 * @param uriInfo
	 * @param jsonRequestBody
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addServiceMapping(@Context final UriInfo uriInfo, final String jsonRequestBody) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createServiceMapping().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param serviceMappingId
	 * @param uriInfo
	 * @return single servicemapping details
	 */
	@GET
	@Path("{serviceMappingId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getServiceMappingForEdit(@PathParam("serviceMappingId") final Long serviceMappingId,@Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final ServiceMappingData serviceMappingData = serviceMappingReadPlatformService.getServiceMapping(serviceMappingId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if (settings.isTemplate()) {
			serviceMappingData.setServiceCodeData(this.serviceMappingReadPlatformService.getServiceCode());
			serviceMappingData.setStatusData(this.planReadPlatformService.retrieveNewStatus());
			serviceMappingData.setCategories(this.paymodeReadPlatformService.retrievemCodeDetails("Service Category"));
			serviceMappingData.setSubCategories(this.paymodeReadPlatformService.retrievemCodeDetails("Asset language"));
			serviceMappingData.setProvisionSysData(this.mCodeReadPlatformService.getCodeValue("Provisioning"));
			serviceMappingData.setItemsData(this.serviceMappingReadPlatformService.retrieveItems());
		}
		return this.toApiJsonSerializer.serialize(settings, serviceMappingData, RESPONSE_PARAMETERS);
	}

	/**
	 * @param serviceMapId
	 * @param apiRequestBodyAsJson
	 * @return single servicemapping details are update here
	 */
	@PUT
	@Path("{serviceMapId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateServiceMapping(@PathParam("serviceMapId") final Long serviceMapId,final String apiRequestBodyAsJson) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateServiceMapping(serviceMapId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	
}
