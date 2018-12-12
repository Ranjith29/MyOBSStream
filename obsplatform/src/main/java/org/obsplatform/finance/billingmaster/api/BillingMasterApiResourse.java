package org.obsplatform.finance.billingmaster.api;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.finance.billingmaster.domain.BillMaster;
import org.obsplatform.finance.billingmaster.domain.BillMasterRepository;
import org.obsplatform.finance.billingmaster.service.BillMasterReadPlatformService;
import org.obsplatform.finance.billingmaster.service.BillMasterWritePlatformService;
import org.obsplatform.finance.billingmaster.service.BillWritePlatformService;
import org.obsplatform.finance.billingorder.data.BillDetailsData;
import org.obsplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateConstants;

import org.obsplatform.scheduledjobs.scheduledjobs.data.BatchHistoryData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Path("/billmaster")
@Component
@Scope("singleton")
public class BillingMasterApiResourse {
	
	    private  final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("transactionId", "transactionDate", "transactionType", "amount", "orderId",
			"invoiceId", "chrageAmount", "taxAmount", "chargeType", "billDate", "dueDate", "id", "chargeStartDate", "chargeEndDate"));
	    
        private static final String RESOURCENAMEFORPERMISSIONS = "BILLMASTER";
	    private final PlatformSecurityContext context;
	    private final DefaultToApiJsonSerializer<BillDetailsData> toApiJsonSerializer;
	    private final ApiRequestParameterHelper apiRequestParameterHelper;
	    private final BillMasterReadPlatformService billMasterReadPlatformService;
		private final BillMasterRepository billMasterRepository;
		private final BillWritePlatformService billWritePlatformService;
	    private final FromJsonHelper fromApiJsonHelper;
	    private final BillMasterWritePlatformService billMasterWritePlatformService;
	    private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
		
		 @Autowired
	    public BillingMasterApiResourse(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
	    final ApiRequestParameterHelper apiRequestParameterHelper,
	    final DefaultToApiJsonSerializer<BillDetailsData> toApiJsonSerializer,
	    final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
	    final BillWritePlatformService billWritePlatformService,
	    final BillMasterReadPlatformService billMasterReadPlatformService,
	    final BillMasterRepository billMasterRepository,
	    final BillMasterWritePlatformService billMasterWritePlatformService) {
		        
			 this.context = context;
		     this.apiRequestParameterHelper = apiRequestParameterHelper;
		     this.billMasterReadPlatformService = billMasterReadPlatformService;
		     this.billMasterRepository = billMasterRepository;
		     this.fromApiJsonHelper = fromJsonHelper;
		     this.billMasterWritePlatformService = billMasterWritePlatformService;
		     this.billWritePlatformService = billWritePlatformService;
		     this.toApiJsonSerializer = toApiJsonSerializer;
		     this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		     
		    }		
		

	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String generateBillStatement(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {
		
		final CommandWrapper wrapper = new CommandWrapperBuilder().createStatement(clientId).withJson(apiRequestBodyAsJson).build();
		final JsonElement parsedCommand = this.fromApiJsonHelper.parse(wrapper.getJson());
        final JsonCommand command = JsonCommand.from(apiRequestBodyAsJson, parsedCommand,this.fromApiJsonHelper,
        		             wrapper.entityName(),wrapper.getEntityId(), 
        		             wrapper.getSubentityId(), wrapper.getGroupId(), 
        		             clientId, null, null, null, null, 
        		             wrapper.getSupportedEntityId(), wrapper.getTransactionId(),null);
       	final CommandProcessingResult result = this.billMasterWritePlatformService.createBillMaster(command,command.entityId());
	    return this.toApiJsonSerializer.serialize(result);
	}
	
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveBillStatements(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo,
			@QueryParam("sqlSearch") final String sqlSearch, @QueryParam("limit") final Integer limit,@QueryParam("offset") final Integer offset) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final SearchSqlQuery searchCodes =SearchSqlQuery.forSearch(sqlSearch, offset,limit);
		final Page<BillDetailsData> data = this.billMasterReadPlatformService.retrieveStatments(searchCodes,clientId);
		return this.toApiJsonSerializer.serialize(data);
	}
	
	@GET
	@Path("{billId}/billdetails")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getBillDetails(@PathParam("billId") final Long billId, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final List<BillDetailsData> data = this.billMasterReadPlatformService.retrieveStatementDetails(billId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);
	}

	@DELETE
	@Path("{billId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String cancelBillStatement(@PathParam("billId") final Long billId) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().cancelBillStatement(billId).build();
        final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("{billId}/print")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response printStatement(@PathParam("billId") final Long billId) {
			
		 final String printFileName=this.billWritePlatformService.generateStatementPdf(billId);
		 final File file = new File(printFileName);
		 final ResponseBuilder response = Response.ok(file);
		 response.header("Content-Disposition", "attachment; filename=\"" +file.getName()+ "\"");
		 response.header("Content-Type", "application/pdf");
		 return response.build();
	}
	
	@PUT
	@Path("/email/{billId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String sendBillPathToMsg(@PathParam("billId") final Long billId) {
		
		final BillMaster billMaster = this.billMasterRepository.findOne(billId);
		final String fileName = billMaster.getFileName();	
		if("invoice".equalsIgnoreCase(fileName)){
			final String msg = "No Generated Pdf file For This Statement";
			throw new BillingOrderNoRecordsFoundException(msg, billId);
		}
	     this.billWritePlatformService.sendPdfToEmail(fileName,billMaster.getClientId(),BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATEMENT);
	    return this.toApiJsonSerializer.serialize(CommandProcessingResult.resourceResult(billId, null));
	}
	
	@GET
	@Path("/invoice/{clientId}/{invoiceId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response printInvoice(@PathParam("invoiceId") final Long invoiceId, @PathParam("clientId") final Long clientId,@DefaultValue("true")@QueryParam("email") final boolean email) {
		
		 String printFileName=this.billWritePlatformService.generateInovicePdf(invoiceId); /*"/usr/share/tomcat7/.obs/InvoicePdfFiles/176_2015-09-18.pdf" */
		 final File file = new File(printFileName);
		 if(email){
		 this.billWritePlatformService.sendPdfToEmail(printFileName,clientId,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_INVOICE);
		 }
		 final ResponseBuilder response = Response.ok(file);
		 response.header("Content-Disposition", "attachment; filename=\"" +file.getName()+ "\"");
		 response.header("Content-Type", "application/pdf");
		 return response.build();
	}
	
	@GET
	@Path("/payment/{clientId}/{paymentId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response printPayment(@PathParam("paymentId") final Long paymentId, @PathParam("clientId") final Long clientId,@DefaultValue("true")@QueryParam("email") final boolean email) {
		
		 String printFileName=this.billWritePlatformService.generatePaymentPdf(paymentId);
		 final File file = new File(printFileName);
		 if(email){
		   this.billWritePlatformService.sendPdfToEmail(printFileName,clientId,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_PAYMENT);
		 }
		 final ResponseBuilder response = Response.ok(file);
		 response.header("Content-Disposition", "attachment; filename=\"" +file.getName()+ "\"");
		 response.header("Content-Type", "application/pdf");
		 return response.build();
	}
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveBatchProcessStatementsDetails(@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final List<BatchHistoryData> data = this.billMasterReadPlatformService.retrieveBatchProcessStatementsDetails();
		return this.toApiJsonSerializer.serialize(data);
	}
	
	@DELETE
	@Path("/batch/{batchId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String cancelBatchStatement(@PathParam("batchId") final String batchId) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().cancelBatchStatement(batchId).build();
	    final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
	    return this.toApiJsonSerializer.serialize(result);
		
	}
}