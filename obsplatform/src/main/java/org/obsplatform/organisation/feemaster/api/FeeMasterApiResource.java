package org.obsplatform.organisation.feemaster.api;

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
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.service.ItemReadPlatformService;
import org.obsplatform.organisation.feemaster.data.FeeMasterData;
import org.obsplatform.organisation.feemaster.service.FeeMasterReadplatformService;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.organisation.region.data.RegionData;
import org.obsplatform.organisation.region.service.RegionReadPlatformService;
import org.obsplatform.portfolio.client.service.ClientCategoryData;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.plan.data.PlanData;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 * @author Hugo
 * this api class used to create,update and delete different additional charges for services 
 */
@Path("/feemaster")
@Component
@Scope("singleton")
public class FeeMasterApiResource {
	
	/**
	 * The set of parameters that are supported in response for
	 * {@link FeeMasterData}.
	 */
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("feeMasterData", "transactionTypeDatas","chargeDatas", "regionDatas"));
	
	private final String resourceNameForPermissions = "FEEMASTER";
	
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<FeeMasterData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final ItemReadPlatformService itemReadPlatformService;
	private final RegionReadPlatformService regionReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final FeeMasterReadplatformService feeMasterReadplatformService;
	private final PlanReadPlatformService planReadPlatformService;
	private final ClientReadPlatformService clientReadPlatformService;

	@Autowired
	public FeeMasterApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<FeeMasterData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final ItemReadPlatformService itemReadPlatformService,final RegionReadPlatformService regionReadPlatformService,
			final MCodeReadPlatformService mCodeReadPlatformService,final FeeMasterReadplatformService feeMasterReadplatformService,
			final PlanReadPlatformService planReadPlatformService, final ClientReadPlatformService clientReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.itemReadPlatformService = itemReadPlatformService;
		this.regionReadPlatformService = regionReadPlatformService;
		this.mCodeReadPlatformService = mCodeReadPlatformService;
		this.feeMasterReadplatformService = feeMasterReadplatformService;
		this.planReadPlatformService = planReadPlatformService;
		this.clientReadPlatformService = clientReadPlatformService;

	}
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllContracts(@Context final UriInfo uriInfo,@QueryParam("transactionType") String transType) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final Collection<FeeMasterData> feeMasterData = this.feeMasterReadplatformService.retrieveAllData(transType);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, feeMasterData,RESPONSE_DATA_PARAMETERS);
	}
	
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveFeeMasterTemplateInfo(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		FeeMasterData feeMasterData = handledTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,feeMasterData,RESPONSE_DATA_PARAMETERS);

	}
	
	private FeeMasterData handledTemplateData(){
		
		final Collection<MCodeData> transactionTypeDatas = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.TRANSACTION_TYPE);
		final List<ChargesData> chargeDatas = this.itemReadPlatformService.retrieveChargeCode();
		final List<RegionData> regionDatas = this.regionReadPlatformService.getRegionDetails();
		final List<PlanData> planDatas=this.planReadPlatformService.retrieveAllPlanDetails();
		final List<SubscriptionData> subscriptionDatas = this.planReadPlatformService.retrieveSubscriptionData(null,null);
		final Collection<ClientCategoryData> categoryDatas = clientReadPlatformService.retrieveClientCategories();
		
		return new FeeMasterData(transactionTypeDatas, chargeDatas, regionDatas,planDatas,subscriptionDatas, categoryDatas);
	}
	
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewFeeMaster(final String apiRequestBodyAsJson,@Context final UriInfo uriInfo) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createFeeMaster().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	
	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingletemData(@PathParam("id") final Long feeMasterId, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		FeeMasterData feeMasterData=this.feeMasterReadplatformService.retrieveSingleFeeMasterDetails(feeMasterId); 
		final List<FeeMasterData> feeMasterRegionPricesDatas = this.feeMasterReadplatformService.retrieveRegionPrice(feeMasterId);
		feeMasterData.setFeeMasterRegionPricesDatas(feeMasterRegionPricesDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if (settings.isTemplate()) {
			final Collection<MCodeData> transactionTypeDatas = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.TRANSACTION_TYPE);
			final List<ChargesData> chargeDatas = this.itemReadPlatformService.retrieveChargeCode();
			final List<RegionData> regionDatas = this.regionReadPlatformService.getRegionDetails();
			final List<PlanData> planDatas=this.planReadPlatformService.retrieveAllPlanDetails();
			final List<SubscriptionData> subscriptionDatas=this.planReadPlatformService.retrieveSubscriptionData(null,null);
			final Collection<ClientCategoryData> categoryDatas = clientReadPlatformService.retrieveClientCategories();
			feeMasterData.setTransactionTypeDatas(transactionTypeDatas);
			feeMasterData.setChargeDatas(chargeDatas);
			feeMasterData.setRegionDatas(regionDatas);
			feeMasterData.setPlanDatas(planDatas);
			feeMasterData.setSubscriptionDatas(subscriptionDatas);
			feeMasterData.setCategoryDatas(categoryDatas);
		}
   		return this.toApiJsonSerializer.serialize(settings, feeMasterData, RESPONSE_DATA_PARAMETERS);
	}
	

	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateFeeMaster(@PathParam("id") final Long id,final String jsonRequestBody) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateFeeMaster(id).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteFeeMaster(@PathParam("id") final Long id) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteFeeMaster(id).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
}
