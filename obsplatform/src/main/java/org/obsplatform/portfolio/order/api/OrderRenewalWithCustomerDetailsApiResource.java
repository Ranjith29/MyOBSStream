package org.obsplatform.portfolio.order.api;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.portfolio.order.data.OrderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/orderRenewal")
@Component
@Scope("singleton")
public class OrderRenewalWithCustomerDetailsApiResource {
	
	  private final DefaultToApiJsonSerializer<OrderData> toApiJsonSerializer;
	  private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	  

	  @Autowired
	   public OrderRenewalWithCustomerDetailsApiResource(final DefaultToApiJsonSerializer<OrderData> toApiJsonSerializer, 
			   						final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {


		        this.toApiJsonSerializer = toApiJsonSerializer;
		        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		        
		    }	
	  
	    @POST
		@Path("{clientId}")
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public String renewalOrder(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().renewalOrderWithClient(clientId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
		}
	 

}
