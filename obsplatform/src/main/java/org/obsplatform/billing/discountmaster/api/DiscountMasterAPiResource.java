package org.obsplatform.billing.discountmaster.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.billing.discountmaster.data.DiscountDetailData;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.billing.discountmaster.service.DiscountReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Ranjith
 * this api class used to create,update and delete diff discounts 
 */
@Path("/discount")
@Component
@Scope("singleton")
public class DiscountMasterAPiResource {
   
	/**
	 * The set of parameters that are supported in response for
	 * {@link DiscountMasterData}.
	 */	
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "discountCode", "discountDescription",
					"discountType", "discountRate", "startDate","discountStatus"));
	
	private final String resourceNameForPermissions = "DISCOUNT";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<DiscountMasterData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final DiscountReadPlatformService discountReadPlatformService;
	private final PlanReadPlatformService planReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;

	@Autowired
	public DiscountMasterAPiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<DiscountMasterData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final DiscountReadPlatformService discountReadPlatformService,final PlanReadPlatformService planReadPlatformService,
			final MCodeReadPlatformService codeReadPlatformService) {
		
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.discountReadPlatformService = discountReadPlatformService;
		this.planReadPlatformService = planReadPlatformService;
		this.mCodeReadPlatformService = codeReadPlatformService;
	}

	/**
	 * @param uriInfo
	 * @return retrieved all discounts details
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDiscountDetails(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<DiscountMasterData> discountMasterDatas = this.discountReadPlatformService.retrieveAllDiscounts();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,discountMasterDatas,RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param uriInfo
	 * @return retrieved drop down data for creating discounts
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveDiscountTemplate(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		DiscountMasterData discountMasterData = handleTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,discountMasterData,RESPONSE_DATA_PARAMETERS);

	}

	private DiscountMasterData handleTemplateData() {
		
		final List<EnumOptionData> statusData = this.planReadPlatformService.retrieveNewStatus();
		final Collection<MCodeData> discountTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
		final Collection<MCodeData> clientCategoryDatas = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_CLIENT_CATEGORY);
		return new DiscountMasterData(statusData, discountTypeData,clientCategoryDatas);
	}

	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewDiscount(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createDiscount().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param discountId
	 * @param uriInfo
	 * @return single discount details
	 */
	@GET
	@Path("{discountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleDiscountDetails(@PathParam("discountId") final Long discountId,@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		DiscountMasterData discountMasterData = this.discountReadPlatformService.retrieveSingleDiscountDetail(discountId);
		List<DiscountDetailData> discountDetailDatas = this.discountReadPlatformService.retrieveDiscountdetails(discountId);
		discountMasterData.setDiscountDetailsData(discountDetailDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if (settings.isTemplate()) {
			discountMasterData.setStatusData(this.planReadPlatformService.retrieveNewStatus());
			discountMasterData.setclientCategoryData(this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_CLIENT_CATEGORY));
			discountMasterData.setDiscountTypeData(this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE));
			discountMasterData.setDate(DateUtils.getLocalDateOfTenant());
		}
		return this.toApiJsonSerializer.serialize(settings,discountMasterData,RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param discountId
	 * @param apiRequestBodyAsJson
	 * @return single discount details are update here
	 */
	@PUT
	@Path("{discountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateDiscount(@PathParam("discountId") final Long discountId,final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDiscount(discountId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param discountId
	 * @return
	 */
	@DELETE
	@Path("{discountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteDiscount(@PathParam("discountId") final Long discountId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDiscount(discountId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);

	}

}
