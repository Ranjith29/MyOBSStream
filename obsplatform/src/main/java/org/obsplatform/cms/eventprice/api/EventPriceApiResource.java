/**
 * 
 */
package org.obsplatform.cms.eventprice.api;

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

import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.billing.discountmaster.service.DiscountReadPlatformService;
import org.obsplatform.cms.eventmaster.data.EventDetailsData;
import org.obsplatform.cms.eventmaster.service.EventMasterReadPlatformService;
import org.obsplatform.cms.eventprice.data.ClientTypeData;
import org.obsplatform.cms.eventprice.data.EventPriceData;
import org.obsplatform.cms.eventprice.domain.EventPrice;
import org.obsplatform.cms.eventprice.service.EventPriceReadPlatformService;
import org.obsplatform.cms.mediadetails.data.MediaAssetLocationDetails;
import org.obsplatform.cms.mediadetails.service.MediaAssetDetailsReadPlatformService;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiParameterHelper;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Class to Create, Update and Delete {@link EventPrice}
 * @author Rakesh
 */
@Path("/eventprice")
@Component
@Scope("singleton")
public class EventPriceApiResource {
	
	private final Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "eventId", "discountId", "formatType", "optType", "clientType", "discount", "price",
			"eventName", "clientTypeId"));
	
	private final String resourceNameForPermissions = "EVENTPRICE";
	private final PlatformSecurityContext context;
	private final EventMasterReadPlatformService eventMasterReadPlatformService;
	private final MediaAssetDetailsReadPlatformService assetDetailsReadPlatformService;
	private final EventPriceReadPlatformService eventPricingReadService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<EventPriceData> apiJsonSerializer;
	private final DiscountReadPlatformService discountReadPlatformService;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final MCodeReadPlatformService codeReadPlatformService;
	
	@Autowired
	public EventPriceApiResource(final EventMasterReadPlatformService eventMasterReadPlatformService,
								 final MediaAssetDetailsReadPlatformService assetReadPlatformService,
								 final EventPriceReadPlatformService eventPricingReadService,
								 final ApiRequestParameterHelper apiRequestParameterHelper,
								 final DefaultToApiJsonSerializer<EventPriceData> apiJsonSerializer,
								 final DiscountReadPlatformService discountReadPlatformService,
								 final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
								 final PlatformSecurityContext context,
								 final MCodeReadPlatformService codeReadPlatformService) {
		this.eventMasterReadPlatformService = eventMasterReadPlatformService;
		this.assetDetailsReadPlatformService = assetReadPlatformService;
		this.eventPricingReadService = eventPricingReadService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.apiJsonSerializer = apiJsonSerializer;
		this.discountReadPlatformService = discountReadPlatformService;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.context = context;
		this.codeReadPlatformService = codeReadPlatformService;
		
	}

	/**
	 * Method to retrieve {@link EventPrice} Data
	 * 
	 * @param eventId
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("template")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String retrieveEventPriceTemplateData(@QueryParam("eventId") final Long eventId, @Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		responseParameters.addAll(RESPONSE_PARAMETERS);
		final EventPriceData templateData = handleEventPriceTemplateData(eventId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, templateData, RESPONSE_PARAMETERS);
	}
	
	public EventPriceData handleEventPriceTemplateData(final Long eventId) {
		final List<EnumOptionData> optType = this.eventMasterReadPlatformService.retrieveOptTypeData();
		final List<EventDetailsData> details = this.eventMasterReadPlatformService.retrieveEventDetailsData(eventId.intValue());
		final List<ClientTypeData> clientType = this.eventPricingReadService.clientType();
		//final List<MediaAssetLocationDetails> format = this.assetDetailsReadPlatformService.retrieveMediaAssetLocationData(details.get(0).getMediaId());
		final Collection<MCodeData> format = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_MEDIAFORMAT);
		final List<DiscountMasterData> discountdata = this.discountReadPlatformService.retrieveAllDiscounts();
		final EventPriceData pricingData = new EventPriceData(optType, format, discountdata, clientType, eventId, details.get(0).getEventName());
		return pricingData;
	}
	
	/**
	 * Method to retrieve single {@link EventPrice} for eventId
	 * 
	 * @param eventId
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("{eventId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String retrieveAllEventPriceDatas(@PathParam("eventId") final Long eventId, @Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<EventPriceData> priceData = this.eventPricingReadService.retrieventPriceData(eventId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.apiJsonSerializer.serialize(settings, priceData, RESPONSE_PARAMETERS);
	}
	
	/**
	 * Method to Create single {@link EventPrice} for eventId
	 * 
	 * @param eventId
	 * @param jsonBodyRequest
	 * @return
	 */
	@POST
	@Path("{eventId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createEventPrice(@PathParam("eventId") final Long eventId, final String jsonBodyRequest) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createEventPrice(eventId).withJson(jsonBodyRequest).build();
		final CommandProcessingResult result  = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		
		return this.apiJsonSerializer.serialize(result);
	}
	
	/**
	 * Method to get details for single {@link EventPrice} for eventPriceId
	 * 
	 * @param eventPriceId
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("singleeventprice/{eventPriceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String retrieveSingleEventPriceDatas(@PathParam("eventPriceId") final Long eventPriceId, @Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		responseParameters.addAll(RESPONSE_PARAMETERS);

		final EventPriceData eventPricing = this.eventPricingReadService.retrieventPriceDetails(eventPriceId);
		final List<EnumOptionData> optType = this.eventMasterReadPlatformService.retrieveOptTypeData();
		final List<EventDetailsData> details = this.eventMasterReadPlatformService.retrieveEventDetailsData(eventPricing.getEventId().intValue());
		final List<ClientTypeData> clientType = this.eventPricingReadService.clientType();
		//final List<MediaAssetLocationDetails> format = this.assetDetailsReadPlatformService.retrieveMediaAssetLocationData(details.get(0).getMediaId());
		final Collection<MCodeData> format = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_MEDIAFORMAT);
		final List<DiscountMasterData> discountdata = this.discountReadPlatformService.retrieveAllDiscounts();
		eventPricing.setClientTypes(clientType);
		eventPricing.setOptTypes(optType);
		eventPricing.setFormat(format);
		eventPricing.setDiscountdata(discountdata);
		
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		
		return this.apiJsonSerializer.serialize(settings,eventPricing,RESPONSE_PARAMETERS);
	}
	
	/**
	 * Method to update single {@link EventPrice} for eventPriceId
	 * 
	 * @param eventPriceId
	 * @param jsonRequestBody
	 * @return
	 */
	@PUT
	@Path("{eventPriceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateEventPrice(@PathParam("eventPriceId")final Long eventPriceId, final String jsonRequestBody) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateEventPrice(eventPriceId).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		
		return this.apiJsonSerializer.serialize(result);
	}
	
	/**
	 * Method to delete single {@link EventPrice} for eventPriceId
	 * 
	 * @param eventPriceId
	 * @return
	 */
	@DELETE
	@Path("{eventPriceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteEventPrice(@PathParam("eventPriceId")final Long eventPriceId) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteEventPrice(eventPriceId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		
		return this.apiJsonSerializer.serialize(result);
	}
}
