package org.obsplatform.logistics.mrn.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
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
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.agent.service.ItemSaleReadPlatformService;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.mrn.data.InventoryTransactionHistoryData;
import org.obsplatform.logistics.mrn.data.MRNDetailsData;
import org.obsplatform.logistics.mrn.service.MRNDetailsReadPlatformService;
import org.obsplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope("singleton")
@Path("/mrn")
public class MRNDetailsApiResource {

	
	
	private final Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("mrnId","movement","transactionDate","requestedDate","itemDescription","fromOffice","toOffice","orderdQuantity","receivedQuantity","status","officeId","officeName","parentId","movedDate"));
	private final static String RESOURCE_TYPE = "MRNDETAILS";
	
	 private final  PlatformSecurityContext context;
	 private final ApiRequestParameterHelper apiRequestParameterHelper;
	 private final OfficeReadPlatformService officeReadPlatformService;
	 private final ItemSaleReadPlatformService agentReadPlatformService;
	 private final ToApiJsonSerializer<MRNDetailsData> apiJsonSerializer;
	 private final MRNDetailsReadPlatformService mrnDetailsReadPlatformService;
	 private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	 private final ToApiJsonSerializer<InventoryTransactionHistoryData> apiJsonSerializerForData;
	 private final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService;
	 
	@Autowired
	public MRNDetailsApiResource(final PlatformSecurityContext context, final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService
			,final MRNDetailsReadPlatformService mrnDetailsReadPlatformService, final ApiRequestParameterHelper apiRequestParameterHelper,
			final ToApiJsonSerializer<MRNDetailsData> apiJsonSerializer,final OfficeReadPlatformService officeReadPlatformService,
			final ToApiJsonSerializer<InventoryTransactionHistoryData> apiJsonSerializerForData,final ItemSaleReadPlatformService agentReadPlatformService,
			final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService) {
		
		this.context = context;
		this.apiJsonSerializer =  apiJsonSerializer;
		this.agentReadPlatformService=agentReadPlatformService;
		this.apiJsonSerializerForData = apiJsonSerializerForData;
		this.officeReadPlatformService = officeReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.mrnDetailsReadPlatformService = mrnDetailsReadPlatformService;
		this.oneTimeSaleReadPlatformService=oneTimeSaleReadPlatformService;
		this.portfolioCommandSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
	}
	
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveMRNDetails(@Context final UriInfo uriInfo ){
		context.authenticatedUser().validateHasReadPermission(RESOURCE_TYPE);
		final List<MRNDetailsData> mrnDetailsDatas = mrnDetailsReadPlatformService.retriveMRNDetails();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return apiJsonSerializer.serialize(settings,mrnDetailsDatas,RESPONSE_PARAMETERS);
	}
	

	@GET
	@Path("/view")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveMRNDetails(@Context final UriInfo uriInfo , @QueryParam("sqlSearch") final String sqlSearch, @QueryParam("limit") final Integer limit,
			         @QueryParam("offset") final Integer offset){
		
		context.authenticatedUser().validateHasReadPermission(RESOURCE_TYPE);
		final SearchSqlQuery searchItemDetails =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		final Page<MRNDetailsData> mrnDetailsDatas = mrnDetailsReadPlatformService.retriveMRNDetails(searchItemDetails);
		return apiJsonSerializer.serialize(mrnDetailsDatas);
	}
	
	
	
	@GET
	@Path("template")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public String retriveMrnTemplate(@Context UriInfo uriInfo){
		context.authenticatedUser().validateHasReadPermission(RESOURCE_TYPE);
		final Collection<OfficeData> officeData = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
		final Collection<ItemData> itemMasterData = this.oneTimeSaleReadPlatformService.retrieveItemData();
		final MRNDetailsData mrnDetailsData = new MRNDetailsData(officeData,itemMasterData);
		mrnDetailsData.setDate(DateUtils.getLocalDateOfTenantForClient());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return apiJsonSerializer.serialize(settings,mrnDetailsData,RESPONSE_PARAMETERS);
	}
	
	@Path("template/ids")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public String retriveSerialNumbers(@Context final UriInfo uriInfo, @QueryParam("mrnId") final Long mrnId,
			@QueryParam("itemsaleId") final Long itemsaleId){
		
		context.authenticatedUser();
		if(mrnId!=null && mrnId > 0){
			
			final List<String> serialNumber = mrnDetailsReadPlatformService.retriveSerialNumbers(mrnId);
			final MRNDetailsData mrnDetailsData = new MRNDetailsData(serialNumber);
			final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			return apiJsonSerializer.serialize(settings,mrnDetailsData,RESPONSE_PARAMETERS);
		}
		if(itemsaleId!=null && itemsaleId > 0){
			
			final List<String> serialNumberForItems = mrnDetailsReadPlatformService.retriveSerialNumbersForItems(itemsaleId,null);
			final MRNDetailsData mrnDetailsData = new MRNDetailsData(serialNumberForItems);
			final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			return apiJsonSerializer.serialize(settings,mrnDetailsData,RESPONSE_PARAMETERS);
		}
		final Collection<MRNDetailsData> mrnIds = mrnDetailsReadPlatformService.retriveMrnIds();
		final List<MRNDetailsData> itemsaleIds = agentReadPlatformService.retriveItemsaleIds();
		final MRNDetailsData mrnDetailsData = new MRNDetailsData(mrnIds,itemsaleIds);
		mrnDetailsData.setDate(DateUtils.getLocalDateOfTenantForClient());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return apiJsonSerializer.serialize(settings,mrnDetailsData,RESPONSE_PARAMETERS);
	}
	
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public String createMRN(final String jsonRequestBody){
		final CommandWrapper command = new CommandWrapperBuilder().createMRN().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(command);
		return apiJsonSerializer.serialize(result);
	}
	
	@Path("movemrn")
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public String moveMRN(final String jsonRequestBody){
		final CommandWrapper command = new CommandWrapperBuilder().moveMRN().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(command);
		return apiJsonSerializer.serialize(result);
	}
	
	@Path("moveitemsale")
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public String moveitemsale(final String jsonRequestBody){
		final CommandWrapper command = new CommandWrapperBuilder().moveItemSale().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = portfolioCommandSourceWritePlatformService.logCommandSource(command);
		return apiJsonSerializer.serialize(result);
	}
	
	@GET
	@Path("/history")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveMMRNHistory(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch, @QueryParam("limit") final Integer limit, 
			    @QueryParam("offset") final Integer offset){
		
		context.authenticatedUser().validateHasReadPermission(RESOURCE_TYPE);
		final SearchSqlQuery searchItemDetails =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		final Page<InventoryTransactionHistoryData> mrnDetailsDatas = mrnDetailsReadPlatformService.retriveHistory(searchItemDetails);
		return apiJsonSerializerForData.serialize(mrnDetailsDatas);
	
	}
	
	
	@GET
	@Path("movemrn/{mrnId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveSingleMrn(@Context final UriInfo uriInfo, @PathParam("mrnId") final Long mrnId){
		context.authenticatedUser().validateHasReadPermission(RESOURCE_TYPE);
		final InventoryTransactionHistoryData mrnDetailsDatas = mrnDetailsReadPlatformService.retriveSingleMovedMrn(mrnId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return apiJsonSerializerForData.serialize(settings,mrnDetailsDatas,RESPONSE_PARAMETERS);
	
	}
	
	@GET
	@Path("{mrnId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveSingleMrnDetail(@Context final UriInfo uriInfo , @PathParam("mrnId") final Long mrnId){
		context.authenticatedUser().validateHasReadPermission(RESOURCE_TYPE);
		final MRNDetailsData mrnDetailsDatas = mrnDetailsReadPlatformService.retriveSingleMrnDetail(mrnId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return apiJsonSerializer.serialize(settings,mrnDetailsDatas,RESPONSE_PARAMETERS);
	}
	
	//For Mrn Track History
	@GET
	@Path("mrntrack/{mrnId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveMrnTrackDetails(@PathParam("mrnId") final Long mrnId){
		context.authenticatedUser().validateHasReadPermission(RESOURCE_TYPE);
		final List<InventoryTransactionHistoryData> mrnDetailsDatas = mrnDetailsReadPlatformService.retriveMrnTrackDetails(mrnId);
		return apiJsonSerializer.serialize(mrnDetailsDatas);
	}
	
}
