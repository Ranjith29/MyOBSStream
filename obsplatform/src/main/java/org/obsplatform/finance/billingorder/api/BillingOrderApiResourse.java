package org.obsplatform.finance.billingorder.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.finance.billingorder.service.InvoiceClient;
import org.obsplatform.infrastructure.codes.data.CodeData;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;


@Path("/billingorder")
@Component
@Scope("singleton")
public class BillingOrderApiResourse {

    private final DefaultToApiJsonSerializer<CodeData> toApiJsonSerializer;
    private final InvoiceClient invoiceClient;
    private final FromJsonHelper fromApiJsonHelper;
    
	@Autowired
	public BillingOrderApiResourse(final DefaultToApiJsonSerializer<CodeData> toApiJsonSerializer,final InvoiceClient invoiceClient,
			final FromJsonHelper fromApiJsonHelper){
		
        this.toApiJsonSerializer = toApiJsonSerializer;
		this.invoiceClient=invoiceClient;
		this.fromApiJsonHelper=fromApiJsonHelper;
	}
	
	
	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createBillingToProducts(@PathParam("clientId") final Long clientId,final String apiRequestBodyAsJson) {
		 final CommandWrapper wrapper = new CommandWrapperBuilder().createInvoice(clientId).withJson(apiRequestBodyAsJson).build();
		 final JsonElement parsedCommand = this.fromApiJsonHelper.parse(wrapper.getJson());
		 final JsonCommand command = JsonCommand.from(wrapper.getJson(),parsedCommand,this.fromApiJsonHelper, wrapper.getEntityName(),
						wrapper.getEntityId(), wrapper.getSubentityId(),wrapper.getGroupId(), wrapper.getClientId(),
						wrapper.getLoanId(), wrapper.getSavingsId(),wrapper.getCodeId(), wrapper.getSupportedEntityType(),
						wrapper.getSupportedEntityId(), wrapper.getTransactionId(),null);
		final CommandProcessingResult result=this.invoiceClient.createInvoiceBill(command); 
		return this.toApiJsonSerializer.serialize(result);
	}
	
}
