package org.obsplatform.logistics.ownedhardware.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.ownedhardware.data.OwnedHardwareData;
import org.obsplatform.logistics.ownedhardware.service.OwnedHardwareReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Hugo
 * this api class used to add,update and delete customer own devices  
 */
@Path("ownedhardware")
@Component
@Scope("singleton")
public class OwnedHardwareApiResource {

	/**
	 * The set of parameters that are supported in response for
	 * {@link OwnedHardwareData}.
	 */	
	private static final Set<String> SUPPORTED_RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "clientId", "allocationDate", "itemType",
					"serialNumber", "provisioningSerialNumber", "status","locale", "dateFormat"));
	private static final Set<String> SUPPORTED_RESPONSE_PARAMETERS_ITEMCODE = new HashSet<String>(Arrays.asList("id", "itemDescription"));

	private String resourceType = "OWNEDHARDWARE";
	private PlatformSecurityContext context;
	private PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService;
	private DefaultToApiJsonSerializer<OwnedHardwareData> apiJsonSerializer;
	private DefaultToApiJsonSerializer<ItemData> apiJsonSerializerForItemCode;
	private OwnedHardwareReadPlatformService ownedHardwareReadPlatformService;
	private ApiRequestParameterHelper apiRequestParameterHelper;

	@Autowired
	public OwnedHardwareApiResource(final PlatformSecurityContext context,
			final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService,
			final DefaultToApiJsonSerializer<OwnedHardwareData> apiJsonSerializer,
			final OwnedHardwareReadPlatformService ownedHardwareReadPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			DefaultToApiJsonSerializer<ItemData> apiJsonSerializerForItemCode) {
		this.context = context;
		this.portfolioCommandSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
		this.apiJsonSerializer = apiJsonSerializer;
		this.ownedHardwareReadPlatformService = ownedHardwareReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.apiJsonSerializerForItemCode = apiJsonSerializerForItemCode;
	}

	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveOwnedHardwareData(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(resourceType);
		List<OwnedHardwareData> ownedHardwareDatas = ownedHardwareReadPlatformService.retriveOwnedHardwareData(clientId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, ownedHardwareDatas,SUPPORTED_RESPONSE_PARAMETERS);
	}

	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createOwnedHardware(@PathParam("clientId") final Long clientId,final String JsonRequestBody) {

		final CommandWrapper command = new CommandWrapperBuilder().createOwnedHardware(clientId).withJson(JsonRequestBody).build();
		final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(command);
		return apiJsonSerializer.serialize(result);
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveOwnedHardwareTemplate(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(resourceType);
		List<ItemData> itemCodes = ownedHardwareReadPlatformService.retriveTemplate();
		ItemData itemCode = new ItemData(itemCodes);
		itemCode.setDate(DateUtils.getLocalDateOfTenantForClient());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializerForItemCode.serialize(settings, itemCode,SUPPORTED_RESPONSE_PARAMETERS_ITEMCODE);
	}

	@PUT
	@Path("own/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateOwnedHardware(@PathParam("id") final Long id,final String JsonRequestBody) {

		final CommandWrapper command = new CommandWrapperBuilder().updateOwnedHardware(id).withJson(JsonRequestBody).build();
		final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(command);
		return apiJsonSerializer.serialize(result);
	}

	@DELETE
	@Path("own/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteOwnHardware(@PathParam("id") final Long id) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteOwnHardware(id).build();
		final CommandProcessingResult result = this.portfolioCommandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.apiJsonSerializer.serialize(result);

	}

	@GET
	@Path("own/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveOwnedHardwareSingleData(@PathParam("id") final Long id, @Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(resourceType);
		List<ItemData> itemCodes = ownedHardwareReadPlatformService.retriveTemplate();
		List<OwnedHardwareData> ownedHardwareDatas = ownedHardwareReadPlatformService.retriveSingleOwnedHardwareData(id);
		OwnedHardwareData ss=new OwnedHardwareData(itemCodes,ownedHardwareDatas);
		ss.setDate(DateUtils.getLocalDateOfTenantForClient());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, ss,SUPPORTED_RESPONSE_PARAMETERS);
	}
}
