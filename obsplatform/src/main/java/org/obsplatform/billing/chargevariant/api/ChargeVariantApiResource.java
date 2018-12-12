package org.obsplatform.billing.chargevariant.api;

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

import org.obsplatform.billing.chargevariant.data.ChargeVariantData;
import org.obsplatform.billing.chargevariant.data.ChargeVariantDetailsData;
import org.obsplatform.billing.chargevariant.service.ChargeVariantReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * this api class used to create,update and delete diff charge variants 
 */
@Path("/chargevariant")
@Component
@Scope("singleton")
public class ChargeVariantApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "chargeVariantCode",
			"status","startDate","endDate","variantType","from","range","amountType","amount"));
	
	private final String resourceNameForPermissions = "CHARGEVARIANT";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<ChargeVariantData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final PlanReadPlatformService planReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final ChargeVariantReadPlatformService chargeVariantReadPlatformService; 
	
	

	@Autowired
	public ChargeVariantApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<ChargeVariantData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PlanReadPlatformService planReadPlatformService,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final ChargeVariantReadPlatformService chargeVariantReadPlatformService,
			final MCodeReadPlatformService mCodeReadPlatformService) {
		
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.planReadPlatformService = planReadPlatformService;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.mCodeReadPlatformService = mCodeReadPlatformService;
		this.chargeVariantReadPlatformService = chargeVariantReadPlatformService;
		
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievechargeVariantTemplate(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		ChargeVariantData discountMasterData = handleTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, discountMasterData, RESPONSE_DATA_PARAMETERS);

	}
	
	private ChargeVariantData handleTemplateData() {
		
		final List<EnumOptionData> statusData = this.planReadPlatformService.retrieveNewStatus();
		final Collection<MCodeData> amountTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
		final Collection<MCodeData> chargeVariantTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_CHARGEVARIANT_TYPE);
		return new ChargeVariantData(statusData, amountTypeData,chargeVariantTypeData);
	}



	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllChargeVariantDetails(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ChargeVariantData> datas = this.chargeVariantReadPlatformService.retrieveAllChargeVariantData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, datas, RESPONSE_DATA_PARAMETERS);
	}
	
	
	@GET
	@Path("{discountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleChargeVariantDetails(@PathParam("discountId") final Long discountId,@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		ChargeVariantData chargeVariantData = this.chargeVariantReadPlatformService.retrieveChargeVariantData(discountId);
		List<ChargeVariantDetailsData> chargeVariantDetailsDatas = this.chargeVariantReadPlatformService.retrieveAllChargeVariantDetails(discountId);
		chargeVariantData.setChargeVariantDetailsData(chargeVariantDetailsDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if(settings.isTemplate()){
		final List<EnumOptionData> statusData = this.planReadPlatformService.retrieveNewStatus();
		final Collection<MCodeData> chargeVariantTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_CHARGEVARIANT_TYPE);
		final Collection<MCodeData> amountTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
		chargeVariantData.setStatusData(statusData);
		chargeVariantData.setVariantTypeData(chargeVariantTypeData);
		chargeVariantData.setAmountTypeData(amountTypeData);
		chargeVariantData.setDate(DateUtils.getLocalDateOfTenant());
	    }
		return this.toApiJsonSerializer.serialize(settings,chargeVariantData,RESPONSE_DATA_PARAMETERS);
	}


	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createChargeVariant(final String apiRequestBodyAsJson) {

		context.authenticatedUser();
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createchargeVariant().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@PUT
	@Path("/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateChargeVariant(@PathParam("id")final Long id,final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateChargeVariant(id).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	
    @DELETE	
	@Path("/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteChargeVariant(@PathParam("id")final Long id,final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteChargeVariant(id).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	

}
