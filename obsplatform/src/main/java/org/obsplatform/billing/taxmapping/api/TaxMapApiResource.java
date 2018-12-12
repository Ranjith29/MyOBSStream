package org.obsplatform.billing.taxmapping.api;

import java.util.Arrays;
import java.util.Collection;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.billing.chargecode.data.ChargeCodeData;
import org.obsplatform.billing.taxmapping.data.TaxMapData;
import org.obsplatform.billing.taxmapping.service.TaxMapReadPlatformService;
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
import org.obsplatform.organisation.priceregion.data.PriceRegionData;
import org.obsplatform.organisation.priceregion.service.RegionalPriceReadplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 *
 *this api class use to create,update taxes for different charge codes
 */
@Path("taxmap")
@Component
@Scope("singleton")
public class TaxMapApiResource {

	/**
	 * The set of parameters that are supported in response for
	 * {@link TaxMapData}.
	 */
	private final Set<String> RESPONSE_TAXMAPPING_PARAMETERS = new HashSet<String>(Arrays.asList("id", "chargeCode", "taxCode", "startDate", "type",
					"rate", "taxRegionId", "taxRegion", "priceRegionData"));

	private String resourceNameForPermissions = "TAXMAPPING";
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<TaxMapData> apiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final TaxMapReadPlatformService taxMapReadPlatformService;
	private final RegionalPriceReadplatformService regionalPriceReadplatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;

	@Autowired
	public TaxMapApiResource(final ApiRequestParameterHelper apiRequestParameterHelper,final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<TaxMapData> apiJsonSerializer,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final TaxMapReadPlatformService taxMapReadPlatformService,final RegionalPriceReadplatformService regionalPriceReadplatformService,
			final MCodeReadPlatformService mCodeReadPlatformService) {
		
		this.context = context;
		this.apiJsonSerializer = apiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.taxMapReadPlatformService = taxMapReadPlatformService;
		this.regionalPriceReadplatformService = regionalPriceReadplatformService;
		this.mCodeReadPlatformService = mCodeReadPlatformService;
	}
    
	/**
	 * @param chargeCode
	 * @param uriInfo
	 * @return retrieved template data for creating taxes to charge codes
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveTaxMapTemplate(@QueryParam("chargeCode") final String chargeCode,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final Collection<MCodeData> taxTypeData = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
		final List<PriceRegionData> priceRegionData = this.regionalPriceReadplatformService.getPriceRegionsDetails();
		final TaxMapData taxMapData=new TaxMapData(taxTypeData,priceRegionData,chargeCode, DateUtils.getLocalDateOfTenantForClient());
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, taxMapData,RESPONSE_TAXMAPPING_PARAMETERS);
	}

	/**
	 * @param chargeCode
	 * @param jsonRequestBody
	 * @return  create new tax for single charge code
	 */
	@POST
	@Path("{chargCode}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createTaxMap(@PathParam("chargeCode") final String chargeCode,final String jsonRequestBody) {

	final CommandWrapper command = new CommandWrapperBuilder().createTaxMap(chargeCode).withJson(jsonRequestBody).build();
	final CommandProcessingResult result = commandSourceWritePlatformService.logCommandSource(command);
	return apiJsonSerializer.serialize(result);
	}
	
	
	/**
	 * @param chargeCode
	 * @param uriInfo
	 * @return retrieved all tax details of chargeCode
	 */
	@GET
	@Path("{chargCode}/chargetax")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveTaxDetailsForChargeCode(@PathParam("chargCode") final String chargeCode,	@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<TaxMapData> taxMapData = taxMapReadPlatformService.retriveTaxMapData(chargeCode);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, taxMapData,RESPONSE_TAXMAPPING_PARAMETERS);
	}

    
    /**
     * @param taxMapId
     * @param uriInfo
     * @return  retrieved single tax details 
     */
    @GET
	@Path("{taxMapId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievedSingleTaxMap(@PathParam("taxMapId") final Long taxMapId,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		TaxMapData taxMapData = taxMapReadPlatformService.retrievedSingleTaxMapData(taxMapId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if(settings.isTemplate()){
		final List<ChargeCodeData> chargeCodeData = this.taxMapReadPlatformService.retrivedChargeCodeTemplateData();
		final Collection<MCodeData> taxTypeData = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
		final List<PriceRegionData> priceRegionData = this.regionalPriceReadplatformService.getPriceRegionsDetails();
		taxMapData.setChargeCodesForTax(chargeCodeData);
		taxMapData.setTaxTypeData(taxTypeData);
		taxMapData.setPriceRegionData(priceRegionData);
		taxMapData.setDate(DateUtils.getLocalDateOfTenantForClient());
		}
		return this.apiJsonSerializer.serialize(settings, taxMapData,RESPONSE_TAXMAPPING_PARAMETERS);

	}

	/**
	 * @param taxMapId
	 * @param jsonRequestBody
	 * @return update single tax details of chargecode here
	 */
	@PUT
	@Path("{taxMapId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateTaxMapData(@PathParam("taxMapId") final Long taxMapId,final String jsonRequestBody) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTaxMap(taxMapId).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = commandSourceWritePlatformService.logCommandSource(commandRequest);
		return apiJsonSerializer.serialize(result);
	}
}
