package org.obsplatform.finance.creditdistribution.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.billing.invoice.data.InvoiceData;
import org.obsplatform.billing.invoice.service.InvoiceReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.creditdistribution.data.CreditDistributionData;
import org.obsplatform.finance.creditdistribution.domain.CreditDistribution;
import org.obsplatform.finance.creditdistribution.domain.CreditDistributionRepository;
import org.obsplatform.finance.creditdistribution.service.CreditDistributionReadPlatformService;
import org.obsplatform.finance.payments.data.PaymentData;
import org.obsplatform.finance.payments.service.PaymentReadPlatformService;
import org.obsplatform.infrastructure.codes.data.CodeData;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/creditdistributions")
@Component
@Scope("singleton")
public class CreditDistributionApiResource {

	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "clientId", "paymentDate", "paymentId",
					"amount", "invoiceAmount","invoiceDatas","paymentDatas","availAmount"));
	private final String resourceNameForPermissions = "CREDITDISTRIBUTION";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<CreditDistributionData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService writePlatformService;
	private final InvoiceReadPlatformService invoiceReadPlatformService;
	private final PaymentReadPlatformService paymentReadPlatformservice;
	private final CreditDistributionReadPlatformService creditDistributionReadPlatformService;
	private final DefaultToApiJsonSerializer<CreditDistributionData> apiJsonSerializer;
	private final DefaultToApiJsonSerializer<CreditDistribution> creditApiJsonSerializer;
	
	private final CreditDistributionRepository creditDistributionRepository;
	@Autowired
	public CreditDistributionApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<CreditDistributionData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService writePlatformService,
			final InvoiceReadPlatformService invoiceReadPlatformService,final PaymentReadPlatformService paymentReadPlatformservice,
			final CreditDistributionReadPlatformService creditDistributionReadPlatformService,DefaultToApiJsonSerializer<CreditDistributionData> apiJsonSerializer,
			final CreditDistributionRepository creditDistributionRepository,final DefaultToApiJsonSerializer<CreditDistribution> creditApiJsonSerializer) {
		
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.writePlatformService = writePlatformService;
		this.invoiceReadPlatformService=invoiceReadPlatformService;
		this.paymentReadPlatformservice=paymentReadPlatformservice;
		this.creditDistributionReadPlatformService=creditDistributionReadPlatformService;
		this.apiJsonSerializer=apiJsonSerializer;
		this.creditDistributionRepository = creditDistributionRepository;
		this.creditApiJsonSerializer = creditApiJsonSerializer;
	}

	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addCreditDistribution(@PathParam("clientId") final Long clientId,	final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createCreditDistribution(clientId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@GET
	@Path("template/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievetemplatedata(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {
 		 context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		List<InvoiceData> invoiceDatas= this.invoiceReadPlatformService.retrieveDueAmountInvoiceDetails(clientId);
		List<PaymentData> paymentDatas=this.paymentReadPlatformservice.retrieveClientPaymentDetails(clientId);
		List<PaymentData> depositDatas = this.paymentReadPlatformservice.retrieveDepositDetails(clientId);
		CreditDistributionData creditDistributionData=new CreditDistributionData(invoiceDatas,paymentDatas,depositDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,creditDistributionData, RESPONSE_DATA_PARAMETERS);
	}
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveCreditDistributiondata(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {
 		 context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		Page<CreditDistributionData> creditDistributionDatas= this.creditDistributionReadPlatformService.getClientDistributionData(clientId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(creditDistributionDatas);
	}
	
	@GET
	@Path("paymentId/{paymentId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievePaymentId(@PathParam("paymentId") final Long paymentId,@Context final UriInfo uriInfo) {
 		 context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
 		List<CreditDistributionData> listOfCreditDistributions = this.creditDistributionReadPlatformService.retrievePaymentId(paymentId);
 		if (listOfCreditDistributions.isEmpty()){
 			return null;
 		}
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(listOfCreditDistributions);
	}
	
	 @PUT
	 @Path("cancelCreditdistribution/{distributionId}")
	 @Consumes({ MediaType.APPLICATION_JSON })
	 @Produces({ MediaType.APPLICATION_JSON })
	 public String cancelPayment(@PathParam("distributionId") final Long distributionId,final String apiRequestBodyAsJson) {
		 
			final CommandWrapper commandRequest = new CommandWrapperBuilder().cancelCreditDistribution(distributionId).withJson(apiRequestBodyAsJson).build();
			final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
			return this.toApiJsonSerializer.serialize(result);
		}
	
}
