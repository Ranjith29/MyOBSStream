package org.obsplatform.provisioning.wifimaster.api;

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
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.provisioning.wifimaster.data.WifiData;
import org.obsplatform.provisioning.wifimaster.service.WifiMasterReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author anil
 *
 *this api class use to create,update wifimaster 
 */
@Path("wifimaster")
@Component
@Scope("singleton")
public class WifiMasterApiResource {

	private final Set<String> RESPONSE_WIFIMASTER_PARAMETERS = new HashSet<String>(Arrays.asList("id","wifiCode", "wifiName", "brandName", "supplierName" ,"orderId","serviceId"));

	private String resourceNameForPermissions = "WIFIMASTER";
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<WifiData> apiJsonSerializer;
	private final WifiMasterReadPlatformService wifiMasterReadPlatformService;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;

	@Autowired
	public WifiMasterApiResource(
			final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<WifiData> apiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final WifiMasterReadPlatformService wifiMasterReadPlatformService) {
		
		this.context = context;
		this.apiJsonSerializer = apiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.wifiMasterReadPlatformService=wifiMasterReadPlatformService;
	}
	
	/*get all details of wifi master*/
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveWifiMasterDetails(@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		
		final List <WifiData> wifiAllDetailsData = this.wifiMasterReadPlatformService.wifiAllDetailsData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, wifiAllDetailsData,RESPONSE_WIFIMASTER_PARAMETERS);
	}
	
	/* get ID details of wifi master
	 * 
	 * */
	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleWifiData(@PathParam("id") final Long id,@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		WifiData wifiDatas = this.wifiMasterReadPlatformService.retrievedSingleWifiData(id);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, wifiDatas,RESPONSE_WIFIMASTER_PARAMETERS);
	}
	
	@GET
	@Path("clientid/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveWifiData(@PathParam("id") final Long id,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List <WifiData> wifiAllDetailsData = this.wifiMasterReadPlatformService.WifiDataGetByClientId(id);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, wifiAllDetailsData,RESPONSE_WIFIMASTER_PARAMETERS);
	}
	
	@GET
	@Path("clientid/{id}/{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getByOrderid(@PathParam("id") final Long id,@PathParam("orderId") final Long orderId,@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		
		WifiData wifiDatas = this.wifiMasterReadPlatformService.getByOrderId(id, orderId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, wifiDatas,RESPONSE_WIFIMASTER_PARAMETERS);
	}
	
	@PUT
	@Path("clientid/{id}/{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateByOrderid(@PathParam("id") final Long id,@PathParam("orderId") final Long orderId,@Context final UriInfo uriInfo,final String jsonRequestBody) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().UpdateWifiByOrderId(id,orderId).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = commandSourceWritePlatformService.logCommandSource(commandRequest);
		return apiJsonSerializer.serialize(result);
	}
	

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createwifi(final String jsonRequestBody) {

	final CommandWrapper command = new CommandWrapperBuilder().createWifi().withJson(jsonRequestBody).build();
	final CommandProcessingResult result = commandSourceWritePlatformService.logCommandSource(command);
	return apiJsonSerializer.serialize(result);
	}
	


	/*@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateWifiData(@PathParam("id") final Long id,final String jsonRequestBody) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateWifi(id).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = commandSourceWritePlatformService.logCommandSource(commandRequest);
		return apiJsonSerializer.serialize(result);
	}*/
	
	@DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteWifi(@PathParam("id") final Long id,final String jsonRequestBody) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteWifi(id).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = commandSourceWritePlatformService.logCommandSource(commandRequest);
		return apiJsonSerializer.serialize(result);
	}
}
