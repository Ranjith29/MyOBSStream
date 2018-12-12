package org.obsplatform.finance.depositandrefund.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.billing.servicetransfer.service.ServiceTransferReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.feemaster.data.FeeMasterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * 
 */
@Path("/deposit")
@Component
@Scope("singleton")
public class DepositApiResource {
	
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "feeCode", "feeDescription","transactionType","chargeCode","defaultFeeAmount"));
	
	private final String resourceNameForPermissions = "DEPOSIT";
	private final PlatformSecurityContext context;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<FeeMasterData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final ServiceTransferReadPlatformService serviceTransferReadPlatformService;

	@Autowired
	public DepositApiResource(final PlatformSecurityContext context,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final DefaultToApiJsonSerializer<FeeMasterData> toApiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper,
		    final ServiceTransferReadPlatformService serviceTransferReadPlatformService) {
		
		this.context = context;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.serviceTransferReadPlatformService = serviceTransferReadPlatformService;
		
	}

	/**
	 * @param uriInfo
	 * 
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })

	public String createDepositAmount(final String apiRequestBodyAsJson,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createDeposite().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	
	}
	
	/**
	 * @param uriInfo
	 * @return retrieved all Fee details of customer region
	 */
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveFeeDetailsofCustomerRegion(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo ,
			@QueryParam("transactionType") final String transactionType) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<FeeMasterData> feeMasterData = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId,transactionType,null,null);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,feeMasterData,RESPONSE_DATA_PARAMETERS);
	}
	
	/**
	 * @param uriInfo
	 * 
	 */
	@POST
	@Path("starterPack")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })

	public String createStarterPackAmount(final String apiRequestBodyAsJson,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createRegistrationFee().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	
	}
	
	/**
	 * @param uriInfo
	 * 
	 */
	@POST
	@Path("setupfee")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })

	public String createSetupFeePackAmount(final String apiRequestBodyAsJson,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createSetupFee().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	
	}
	

}
