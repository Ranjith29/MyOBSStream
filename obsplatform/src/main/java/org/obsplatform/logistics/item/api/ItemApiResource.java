package org.obsplatform.logistics.item.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.billing.chargecode.data.ChargesData;
import org.obsplatform.billing.chargevariant.data.ChargeVariantData;
import org.obsplatform.billing.chargevariant.service.ChargeVariantReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.item.service.ItemReadPlatformService;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.organisation.region.data.RegionData;
import org.obsplatform.organisation.region.service.RegionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/items")
@Component
@Scope("singleton")
public class ItemApiResource {
	
	private static final Set<String> RESPONSE_ITEM_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("itemId","chargedatas","unitData",
			           "itemclassData","chargeCode","unit","warranty","itemDescription","itemCode","unitPrice","itemCategoryTypeId"));
	private final String resourceNameForPermissions = "ITEM";
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<ItemData> toApiJsonSerializer;
	private final PlatformSecurityContext context;
	private final ItemReadPlatformService itemReadPlatformService;
	private final RegionReadPlatformService regionReadPlatformService; 
	private final ChargeVariantReadPlatformService chargeVariantReadPlatformService;
    private final MCodeReadPlatformService codeReadPlatformService;
	
	
	@Autowired
	public ItemApiResource(final PlatformSecurityContext context,final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService,
			final ApiRequestParameterHelper requestParameterHelper,final DefaultToApiJsonSerializer<ItemData> defaultToApiJsonSerializer,
			final ItemReadPlatformService itemReadPlatformService, final RegionReadPlatformService regionReadPlatformService,
		    final ChargeVariantReadPlatformService chargeVariantReadPlatformService,
		   final  MCodeReadPlatformService codeReadPlatformService){
		
		this.context = context;
		this.commandsSourceWritePlatformService=portfolioCommandSourceWritePlatformService;
		this.apiRequestParameterHelper = requestParameterHelper;
		this.toApiJsonSerializer = defaultToApiJsonSerializer;
		this.itemReadPlatformService = itemReadPlatformService;
		this.regionReadPlatformService = regionReadPlatformService;
		this.chargeVariantReadPlatformService = chargeVariantReadPlatformService;
		this.codeReadPlatformService=codeReadPlatformService;
	}
	
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveItemTemplateData(@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());	
		ItemData itemData = null;
		         itemData = handleTemplateData(itemData);
		return this.toApiJsonSerializer.serialize(settings, itemData, RESPONSE_ITEM_DATA_PARAMETERS);
	}

	private ItemData handleTemplateData(ItemData itemData) {
		final List<EnumOptionData> itemClassdata = this.itemReadPlatformService.retrieveItemClassType();
		final List<EnumOptionData> unitTypeData = this.itemReadPlatformService.retrieveUnitTypes();
		final List<ChargesData> chargeDatas = this.itemReadPlatformService.retrieveChargeCode();
		final List<RegionData> regionDatas = this.regionReadPlatformService.getRegionDetails();
		final List<ChargeVariantData> chargeVariantDatas = this.chargeVariantReadPlatformService.retrieveAllChargeVariantData();
		
		final Collection<MCodeData> itemCategory = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_ITEM_CATEGORY);
		return new ItemData(itemData,itemClassdata, unitTypeData, chargeDatas, regionDatas,chargeVariantDatas ,itemCategory);
		
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewItem(final String jsonRequestBody) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createItem().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
		
	}

	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllItems(@Context final UriInfo uriInfo,  @QueryParam("sqlSearch") final String sqlSearch, 
			@QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final SearchSqlQuery searchItems =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		Page<ItemData> itemData=this.itemReadPlatformService.retrieveAllItems(searchItems);
		return this.toApiJsonSerializer.serialize(itemData);
	}

	@GET
	@Path("{itemId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingletemData(@PathParam("itemId") final Long itemId, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		ItemData itemData=this.itemReadPlatformService.retrieveSingleItemDetails(null, itemId,null, false); // If you pass clientId set to 'true' else 'false'
		final List<ItemData> auditDetails = this.itemReadPlatformService.retrieveAuditDetails(itemId);
		final List<ItemData> itemPricesDatas = this.itemReadPlatformService.retrieveItemPrice(itemId);
   		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
   		if(settings.isTemplate()){
   			itemData=handleTemplateData(itemData);
   		}
   		itemData.setAuditDetails(auditDetails);
   		itemData.setItemPricesDatas(itemPricesDatas);
   		return this.toApiJsonSerializer.serialize(settings, itemData, RESPONSE_ITEM_DATA_PARAMETERS);
	}

	@PUT
	@Path("{itemId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateItem(@PathParam("itemId") final Long itemId,final String jsonRequestBody) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateItem(itemId).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@DELETE
	@Path("{itemId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteItem(@PathParam("itemId") final Long itemId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteItem(itemId).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
}
