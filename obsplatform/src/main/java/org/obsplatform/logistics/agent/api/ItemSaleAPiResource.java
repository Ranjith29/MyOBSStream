package org.obsplatform.logistics.agent.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.billing.chargecode.data.ChargesData;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.agent.data.AgentItemSaleData;
import org.obsplatform.logistics.agent.service.ItemSaleReadPlatformService;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.item.service.ItemReadPlatformService;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;



@Path("itemsales")
@Component
@Scope("singleton")
public class ItemSaleAPiResource {
	
	private static final Set<String> RESPONSE_AGENT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("agentId",
			"agentName","purchaseDate","orderQuantity","itemCode","itemId","invoiceAmount","taxAmount","chargeAmount","itemPrice"));
	private final String resourceNameForPermissions = "AGENT";
	
	
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<AgentItemSaleData> toApiJsonSerializer;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final ItemReadPlatformService itemReadPlatformService;
	private final ItemSaleReadPlatformService  agentReadPlatformService;
	private final PlatformSecurityContext context;
	
	@Autowired
	public ItemSaleAPiResource(final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,final DefaultToApiJsonSerializer<AgentItemSaleData> apiJsonSerializer,
			final PlatformSecurityContext context,final OfficeReadPlatformService officeReadPlatformService,
			final ItemReadPlatformService itemReadPlatformService,final ItemSaleReadPlatformService  agentReadPlatformService)
	{
		
		this.context=context;
		this.toApiJsonSerializer=apiJsonSerializer;
		this.apiRequestParameterHelper=apiRequestParameterHelper;
		this.commandsSourceWritePlatformService=commandSourceWritePlatformService;
		this.officeReadPlatformService=officeReadPlatformService;
		this.itemReadPlatformService=itemReadPlatformService;
		this.agentReadPlatformService=agentReadPlatformService;
	
	}
	
/**
 * @param uriInfo
 * @return
 * get dropdown template data
 */
@GET
@Path("template")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public String retrieveTemplateData(@Context final UriInfo uriInfo){
	
	this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
	AgentItemSaleData  itemSaleData=null;
    itemSaleData=handleAgentTemplateData(itemSaleData);
    if(itemSaleData != null){
    	itemSaleData.setDate(DateUtils.getLocalDateOfTenantForClient());
    }
	final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	return this.toApiJsonSerializer.serialize(settings, itemSaleData, RESPONSE_AGENT_DATA_PARAMETERS);
	
}

/**
 * @param apiRequestBodyAsJson
 * @return
 */
@POST
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public String createNewItemSale(final String apiRequestBodyAsJson) {

    final CommandWrapper commandRequest = new CommandWrapperBuilder().createItemSale().withJson(apiRequestBodyAsJson).build();
    final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    return this.toApiJsonSerializer.serialize(result);
}

private AgentItemSaleData handleAgentTemplateData(AgentItemSaleData  itemSaleData) {
	
	final Collection<OfficeData> officeDatas=this.officeReadPlatformService.retrieveAllOffices();
	final List<ItemData> itemDatas=this.itemReadPlatformService.retrieveAllItems();
	final List<ChargesData> chargesDatas=this.itemReadPlatformService.retrieveChargeCode();
	if(itemSaleData == null){
	return  AgentItemSaleData.withTemplateData(officeDatas,itemDatas,chargesDatas);
	}else{
		return AgentItemSaleData.instance(itemSaleData, officeDatas, itemDatas,chargesDatas);

	}
}

/**
 * @param uriInfo
 * @return get list of itemsales 
 */
@GET
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public String retrieveAllSaleData(@Context final UriInfo uriInfo){
	
	this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
	final List<AgentItemSaleData> agentDatas=this.agentReadPlatformService.retrieveAllData();
	final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	return this.toApiJsonSerializer.serialize(settings, agentDatas, RESPONSE_AGENT_DATA_PARAMETERS);
}

/**
 * @param id
 * @param uriInfo
 * @return
 * single ItemSaleData
 */
@GET
@Path("{id}")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public String retrieveSingleItemSaleData(@PathParam("id") final Long id,@Context final UriInfo uriInfo){
	
	this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
	AgentItemSaleData itemSaleData=this.agentReadPlatformService.retrieveSingleItemSaleData(id);
	final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	if(settings.isTemplate()){
		itemSaleData=handleAgentTemplateData(itemSaleData);	
		
	}
	return this.toApiJsonSerializer.serialize(settings, itemSaleData, RESPONSE_AGENT_DATA_PARAMETERS);
	
}

}
