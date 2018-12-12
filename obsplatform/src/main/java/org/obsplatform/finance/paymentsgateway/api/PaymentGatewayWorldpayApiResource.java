
package org.obsplatform.finance.paymentsgateway.api;

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
import org.obsplatform.finance.paymentsgateway.data.PaymentGatewayData;
import org.obsplatform.infrastructure.codes.data.CodeData;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/worldpaypaymetgateway")
@Component
@Scope("singleton")

/**
 * The class <code>worldpaypaymetgateway</code> is developed for Third party
 * PaymentGateway systems. Using the below API to Communicate OBS with
 * Adapters/Third-Party servers.
 * 
 * @author anilreddy
 *
 */
public class PaymentGatewayWorldpayApiResource {
	
	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSEPARAMETERS = new HashSet<String>(
			Arrays.asList("id","paymentId", "serialNo", "paymentDate", "receiptNo","status","phoneNo","clientName","amountPaid","remarks"));
	
	private final String resourceNameForPermissions = "WORLDPAYPAYMENTGATEWAY";

	private final DefaultToApiJsonSerializer<PaymentGatewayData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	
	@Autowired
	public PaymentGatewayWorldpayApiResource(
			final DefaultToApiJsonSerializer<PaymentGatewayData> toApiJsonSerializer,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {

		this.toApiJsonSerializer = toApiJsonSerializer;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createOrderWorldpay(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createOrderWorldpay().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return toApiJsonSerializer.serialize(result);
	}

}

