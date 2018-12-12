package org.obsplatform.portfolio.order.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.addons.data.AddonsPriceData;
import org.obsplatform.portfolio.addons.service.AddonServiceReadPlatformService;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.order.data.OrderAddonsData;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * 
 */
@Path("/orderaddons")
@Component
@Scope("singleton")
public class OrderAddonsApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "orderId", "serviceId", "startDate", "endDate",
					"contracrId", "status", "provisionSystem"));

	private static final String RESOURCENAMEFORPERMISSIONS = "ADDONS";

	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<OrderAddonsData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PlanReadPlatformService planReadPlatformService;
	private final AddonServiceReadPlatformService addonServiceReadPlatformService;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;

	@Autowired
	public OrderAddonsApiResource(final DefaultToApiJsonSerializer<OrderAddonsData> apiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final PlatformSecurityContext context,
			final AddonServiceReadPlatformService addonServiceReadPlatformService,
			final PlanReadPlatformService planReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = apiJsonSerializer;
		this.addonServiceReadPlatformService = addonServiceReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.planReadPlatformService = planReadPlatformService;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;

	}

	@POST
	@Path("{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addOrderAddonServices(@PathParam("orderId") final Long orderId,final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createOrderAddons(orderId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);

	}

	@GET
	@Path("template/{planId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOrderTemplate(@PathParam("planId") final Long planId,@QueryParam("chargeCode") final String chargeCode,@Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		List<AddonsPriceData> addonsPriceDatas = this.addonServiceReadPlatformService.retrievePlanAddonDetails(planId, chargeCode);
		List<SubscriptionData> contractPeriod = this.planReadPlatformService.retrieveSubscriptionData(null, null);
		OrderAddonsData addonsData = new OrderAddonsData(addonsPriceDatas,contractPeriod);
		addonsData.setDate(DateUtils.getLocalDateOfTenantForClient());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, addonsData,RESPONSE_DATA_PARAMETERS);

	}

	@DELETE
	@Path("{orderAddonId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteOrder(@PathParam("orderAddonId") final Long orderAddonId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().disconnectOrderAddon(orderAddonId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	/**
	 * 
	 * delete method for Order Addon
	 */
	@DELETE
	@Path("delete/{orderAddonId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteOrderAddOn(@PathParam("orderAddonId") final Long orderAddonId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteOrderAddOn(orderAddonId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@DELETE
	@Path("cancel/{orderAddonId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String cancelOrderAddOn(@PathParam("orderAddonId") final Long orderAddonId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().cancelOrderAddOn(orderAddonId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}


