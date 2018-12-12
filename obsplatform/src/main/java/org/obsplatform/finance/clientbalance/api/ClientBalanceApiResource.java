package org.obsplatform.finance.clientbalance.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.clientbalance.data.ClientBalanceData;
import org.obsplatform.finance.clientbalance.service.ClientBalanceReadPlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 *
 */
@Path("/clientBalance")
@Component
@Scope("singleton")
public class ClientBalanceApiResource {
	
	private final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("balanceAmount"));
    private final String resourceNameForPermissions = "CLIENTBALANCE";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<ClientBalanceData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final ClientBalanceReadPlatformService clientBalanceReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService;
	
	@Autowired
	public ClientBalanceApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<ClientBalanceData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final ClientBalanceReadPlatformService balanceReadPlatformService,
			final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService) {
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.clientBalanceReadPlatformService = balanceReadPlatformService;
		this.portfolioCommandSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
	}

	@GET
	@Path("template/{clientid}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveCharge(@PathParam("clientid") final Long clientid,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		ClientBalanceData balanceDatas = this.clientBalanceReadPlatformService.retrieveBalance(clientid);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, balanceDatas,RESPONSE_DATA_PARAMETERS);
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public String addNewClientBalanec(final String jsonRequestBody) {
		
		final CommandWrapper command = new CommandWrapperBuilder().createBalance().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(command);
		return this.toApiJsonSerializer.serialize(result);
	}

}
