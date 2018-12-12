/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.client.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import org.obsplatform.portfolio.client.data.ClientCardDetailsData;
import org.obsplatform.portfolio.client.service.ClientCardDetailsReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/carddetails")
@Component
@Scope("singleton")
public class ClientCardDetailsApiResource {

	private static final Set<String> CLIENT_CARDDETAILS_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "clientId", "type", "name", "cardNumber", "cardType", "routingNumber",
					"bankAccountNumber", "bankName", "accountType", "cardExpiryDate"));

	private final String resourceNameForPermissions = "CLIENTCARDDETAILS";
	private final String resourceName = "PSEUDOCARD";
	private final String resourceNameWorldpay = "OBFUSCATEDCARD";
	private final PlatformSecurityContext context;
	private final ClientCardDetailsReadPlatformService clientCardDetailsReadPlatformService;
	private final DefaultToApiJsonSerializer<ClientCardDetailsData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

	@Autowired
	public ClientCardDetailsApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<ClientCardDetailsData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final ClientCardDetailsReadPlatformService clientCardDetailsReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.clientCardDetailsReadPlatformService = clientCardDetailsReadPlatformService;

	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo,
			@PathParam("clientId") final Long clientId) {

		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final Collection<ClientCardDetailsData> clientIdentifiers = this.clientCardDetailsReadPlatformService
				.retrieveClientDetails(clientId);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, clientIdentifiers, CLIENT_CARDDETAILS_DATA_PARAMETERS);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createClientIdentifier(@PathParam("clientId") final Long clientId,
			final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientCardDetails(clientId)
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);

	}

	@GET
	@Path("{id}/{type}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientIdentifiers(@PathParam("clientId") final Long clientId,
			@PathParam("type") final String type, @PathParam("id") final Long id, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
		ClientCardDetailsData clientCardDetailsData = this.clientCardDetailsReadPlatformService.retrieveClient(id, type,
				clientId);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, clientCardDetailsData, CLIENT_CARDDETAILS_DATA_PARAMETERS);
	}

	@PUT
	@Path("{id}/{type}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateClientIdentifer(@PathParam("clientId") final Long clientId, @PathParam("id") final Long id,
			@PathParam("type") final String type, final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCreditCardDetail(clientId, id, type)
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteClientIdentifier(@PathParam("id") final Long id, @PathParam("clientId") final Long clientId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClientCardDetails(id, clientId).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}
	
	@GET
	@Path("{identifier}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo,
			@PathParam("clientId") final Long clientId, @PathParam("identifier") final String identifier) {

		if(null != identifier && identifier.equalsIgnoreCase("PseudoCard")) {
			this.context.authenticatedUser().validateHasReadPermission(this.resourceName);
		}else if(null != identifier && identifier.equalsIgnoreCase("ObfuscatedCard")) {
			this.context.authenticatedUser().validateHasReadPermission(this.resourceNameWorldpay);
		}else {
			this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
		}
		
		final ClientCardDetailsData clientIdentifiers = this.clientCardDetailsReadPlatformService
				.retrieveClient(null, identifier, clientId);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		
		return this.toApiJsonSerializer.serialize(settings, clientIdentifiers, CLIENT_CARDDETAILS_DATA_PARAMETERS);
	}

}
