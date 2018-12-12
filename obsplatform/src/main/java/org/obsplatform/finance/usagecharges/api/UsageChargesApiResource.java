package org.obsplatform.finance.usagecharges.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Ranjith
 * 
 */
@Path("/charges")
@Component
@Scope("singleton")
public class UsageChargesApiResource {

	/**
	 * The set of parameters that are supported in response for {@link UsageChargesData}
	 */
	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id","number","clientId"));

	private final DefaultToApiJsonSerializer<UsageChargesData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;

	@Autowired
	public UsageChargesApiResource(final DefaultToApiJsonSerializer<UsageChargesData> toApiJsonSerializer,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService) {

		this.toApiJsonSerializer = toApiJsonSerializer;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;

	}

	/**
	 * This method is using for posting raw data of customers usage charges from oss 
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createCdrRawData(final String apiRequestBodyAsJson) {
		
		final CommandWrapper wrapperRequest = new CommandWrapperBuilder().createUsageChargesRawData().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(wrapperRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}
