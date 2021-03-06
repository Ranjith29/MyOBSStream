package org.obsplatform.billing.promotioncodes.api;

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

import org.obsplatform.billing.promotioncodes.data.PromotionCodeData;
import org.obsplatform.billing.promotioncodes.service.PromotionCodeReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.contract.data.PeriodData;
import org.obsplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * this api class use to create,update and delete different promotion codes
 */
@Path("/promotioncode")
@Component
@Scope("singleton")
public class PromotionCodesApiResource {
    
	/**
	 * The set of parameters that are supported in response for
	 * {@link PromotionCodeData}.
	 */	
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "promotionCode", "promotionDescription",
					"durationType", "duration", "discountType", "discountRate","startDate"));
	private final String resourceNameForPermissions = "PROMOTIONCODE";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<PromotionCodeData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final PromotionCodeReadPlatformService promotionCodeReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;

	@Autowired
	public PromotionCodesApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<PromotionCodeData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final MCodeReadPlatformService codeReadPlatformService,
			final PromotionCodeReadPlatformService promotionCodeReadPlatformService,
			final ContractPeriodReadPlatformService contractPeriodReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.mCodeReadPlatformService = codeReadPlatformService;
		this.promotionCodeReadPlatformService = promotionCodeReadPlatformService;
		this.contractPeriodReadPlatformService = contractPeriodReadPlatformService;
	}

	/**
	 * @param uriInfo
	 * @return retrieved all PromotionCodeDetails
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllPromotionCodeDetails(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<PromotionCodeData> promotionDatas = this.promotionCodeReadPlatformService.retrieveAllPromotionCodes();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,promotionDatas,RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param uriInfo
	 * @return retrieved template data for creating promotion codes
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievePromotionTemplateData(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final Collection<MCodeData> discountTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
		final List<PeriodData> contractTypedata = contractPeriodReadPlatformService.retrieveAllPlatformPeriod();
		final PromotionCodeData data = new PromotionCodeData(discountTypeData,contractTypedata);
		data.setDate(DateUtils.getLocalDateOfTenantForClient());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,data,RESPONSE_DATA_PARAMETERS);

	}

	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createPromotionCode(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createPromotionCode().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param promotionId
	 * @param uriInfo
	 * @return retrieved single promotion code details
	 */
	@GET
	@Path("{promotionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSinglePromotionCodeDetails(@PathParam("promotionId") final Long promotionId, @Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		PromotionCodeData promotionCodeData = this.promotionCodeReadPlatformService.retriveSinglePromotionCodeDetails(promotionId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if(settings.isTemplate()){
			final Collection<MCodeData> discountTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
			final List<PeriodData> contractTypedata = contractPeriodReadPlatformService.retrieveAllPlatformPeriod();
			promotionCodeData.setDiscounTypeData(discountTypeData);
			promotionCodeData.setContractTypedata(contractTypedata);
			promotionCodeData.setDate(DateUtils.getLocalDateOfTenantForClient());
		}
		return this.toApiJsonSerializer.serialize(settings,promotionCodeData,RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param promotionId
	 * @param apiRequestBodyAsJson
	 * @return single promotion code details are update here
	 */
	@PUT
	@Path("{promotionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateSinglePromotionCode(@PathParam("promotionId") final Long promotionId,final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePromotionCode(promotionId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param promotionId
	 * @return delete single promotion code
	 */
	@DELETE
	@Path("{promotionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteSinglePromotionCode(@PathParam("promotionId") final Long promotionId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deletePromotionCode(promotionId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);

	}

}
