package org.obsplatform.cms.eventorder.api;

import java.math.BigDecimal;
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

import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.cms.eventmaster.service.EventMasterReadPlatformService;
import org.obsplatform.cms.eventorder.data.EventOrderData;
import org.obsplatform.cms.eventorder.data.EventOrderDeviceData;
import org.obsplatform.cms.eventorder.service.EventOrderReadplatformServie;
import org.obsplatform.cms.eventprice.data.ClientTypeData;
import org.obsplatform.cms.eventprice.service.EventPriceReadPlatformService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/eventorder")
@Component
@Scope("singleton")
public class EventOrderApiResource {
			private  final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("eventId","eventBookedDate","eventValidDate","clientId",
					   "eventPriceId","movieLocation","bookedPrice","eventStatus","chargeCode"));
		   
			private final String resourceNameForPermissions = "EVENTORDER";
		    private final PlatformSecurityContext context;
			private final DefaultToApiJsonSerializer<EventOrderData> toApiJsonSerializer;
			private final ApiRequestParameterHelper apiRequestParameterHelper;
			private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
			private final EventOrderReadplatformServie eventOrderReadplatformServie; 
			private final EventMasterReadPlatformService eventMasterReadPlatformService;
			private final MCodeReadPlatformService codeReadPlatformService;
			private final EventPriceReadPlatformService eventPricingReadService;
				
			@Autowired
			public EventOrderApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<EventOrderData> toApiJsonSerializer,
					final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
					final EventOrderReadplatformServie eventOrderReadplatformServie,final EventMasterReadPlatformService eventMasterReadPlatformService,
					final MCodeReadPlatformService codeReadPlatformService,final EventPriceReadPlatformService eventPricingReadService) {
				
				this.context = context;
				this.toApiJsonSerializer = toApiJsonSerializer;
				this.apiRequestParameterHelper = apiRequestParameterHelper;
				this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
				this.eventOrderReadplatformServie = eventOrderReadplatformServie;
				this.eventMasterReadPlatformService = eventMasterReadPlatformService;
				this.codeReadPlatformService = codeReadPlatformService;
				this.eventPricingReadService = eventPricingReadService;
			}

		@POST
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public String createNewEventOrder(@PathParam("clientId") final Long clientId,final String apiRequestBodyAsJson) {
			
			final CommandWrapper commandRequest = new CommandWrapperBuilder().createEventOrder(clientId).withJson(apiRequestBodyAsJson).build();
		    final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		    return this.toApiJsonSerializer.serialize(result);
			
		}
		
		@GET
		@Path("{clientId}")
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		public String getEventOrder(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo){			
			context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
			final List<EventOrderDeviceData> devices = eventOrderReadplatformServie.getDevices(clientId);
			//final List<EventMasterData> events = eventOrderReadplatformServie.getEvents();
			final List<EventMasterData> events = this.eventMasterReadPlatformService.retrieveEventMasterDataForEventOrders();
			final List<EnumOptionData> optType = this.eventMasterReadPlatformService.retrieveOptTypeData();
			final Collection<MCodeData> codes = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_MEDIAFORMAT);
			final List<ClientTypeData> clientType = this.eventPricingReadService.clientType();
			final EventOrderData data = new EventOrderData(devices,events,optType,codes,clientType);
			data.setDate(DateUtils.getLocalDateOfTenantForClient());
			final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	        return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);
		}
		
		@GET
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		public String gteEventPrice(@QueryParam("clientId") final Long clientId,@QueryParam("ftype") final String fType,
				@QueryParam("otype")final String oType, @Context final UriInfo uriInfo){
			
			List<EventOrderData> eventOrderDatas=this.eventOrderReadplatformServie.getTheClientEventOrders(clientId);
			final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			return this.toApiJsonSerializer.serialize(settings, eventOrderDatas, RESPONSE_DATA_PARAMETERS);
		}
		
		
		
		
		@GET 
		@Path("/geteventprice")
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		public String getEventOrderPrice(@QueryParam("clientId") final Long clientId,@QueryParam("ftype") final String fType,
				@QueryParam("otype")final String oType, @QueryParam("eventId") final Long eventId,@Context final UriInfo uriInfo){
			
			BigDecimal price=this.eventOrderReadplatformServie.retriveEventPriceNew( fType,oType,clientId ,eventId);
			
			EventOrderData eventOrderData= new EventOrderData(price);
			
			final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			return this.toApiJsonSerializer.serialize(settings, eventOrderData, RESPONSE_DATA_PARAMETERS);
		}
		
		
	
		/*@PUT
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		public String updatePrice(final String apiRequestBodyAsJson){
			
			final CommandWrapper commandRequest = new CommandWrapperBuilder().updateEventOrderPrice().withJson(apiRequestBodyAsJson).build();
		    final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		    return this.toApiJsonSerializer.serialize(result);
		    
		    
		}*/
		
}
