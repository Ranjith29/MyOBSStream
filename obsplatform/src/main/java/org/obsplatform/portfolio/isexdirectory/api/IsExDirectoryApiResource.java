package org.obsplatform.portfolio.isexdirectory.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
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
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.isexdirectory.data.IsExDirectoryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Naresh
 * 
 */
@Path("isexdirectory")
@Component
@Scope("singleton")
public class IsExDirectoryApiResource {

	private final Set<String> RESPONSE_ISEXDIRECTORY_PARAMETERS = new HashSet<String>(Arrays.asList("id", "clientId", "orderId", 
			"planId", "serviceId", "isExDirectory", "isNumberWithHeld", "isUmeeApp"));

	private String resourceNameForPermissions = "ISEXDIRECTORY";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<IsExDirectoryData> apiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService;

	@Autowired
	public IsExDirectoryApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<IsExDirectoryData> apiJsonSerializer,
			final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService) {

		this.context = context;
		this.apiJsonSerializer = apiJsonSerializer;
		this.portfolioCommandSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createIsExDirectory(final String jsonRequestBody) {

		final CommandWrapper command = new CommandWrapperBuilder().createIsExDirectory().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(command);
		return apiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateIsExDirectoryByOrderId(@PathParam("orderId") final Long orderId, @Context final UriInfo uriInfo, 
			final String jsonRequestBody) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateIsExDirectory(orderId).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(commandRequest);
		return apiJsonSerializer.serialize(result);
	}

}
