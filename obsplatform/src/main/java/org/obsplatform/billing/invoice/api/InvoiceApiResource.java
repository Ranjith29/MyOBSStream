package org.obsplatform.billing.invoice.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.billing.invoice.data.InvoiceData;
import org.obsplatform.billing.invoice.service.InvoiceReadPlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/invoice")
@Component
@Scope("singleton")
public class InvoiceApiResource {
	
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "amount", "dueAmount", "billId", "billDate"));

	private final String resourceNameForPermissions = "INVOICE";

	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<InvoiceData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final InvoiceReadPlatformService invoiceReadPlatformService;

	@Autowired
	public InvoiceApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<InvoiceData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final InvoiceReadPlatformService invoiceReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.invoiceReadPlatformService = invoiceReadPlatformService;

	}

	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllPromotionDetails(@PathParam("id") final Long id,@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		List<InvoiceData> invoiceDatas = this.invoiceReadPlatformService.retrieveInvoiceDetails(id);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, invoiceDatas,RESPONSE_DATA_PARAMETERS);
	}
	
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllExpiryOrderList(@Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
	    final List<InvoiceData> expiryOrders = this.invoiceReadPlatformService.retrieveExpiryOrderList();
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, expiryOrders,RESPONSE_DATA_PARAMETERS);
	}
}
