package org.obsplatform.workflow.eventactionmapping.api;

import java.util.Arrays;
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

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.workflow.eventactionmapping.data.EventActionMappingData;
import org.obsplatform.workflow.eventactionmapping.service.EventActionMappingReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * this api class use to create,update and delete different events with their actions
 */
@Path("/eventactionmapping")
@Component
@Scope("singleton")
public class EventActionMappingApiResource {

	/**
	 * The set of parameters that are supported in response for
	 * {@link EventActionMappingData}
	 * 
	 */
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "action", "event", "process"));
	
	private final String resourceNameForPermissions = "EVENTACTIONMAP";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<EventActionMappingData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final EventActionMappingReadPlatformService eventActionMappingReadPlatformService;

	@Autowired
	public EventActionMappingApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<EventActionMappingData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final EventActionMappingReadPlatformService eventActionMappingReadPlatformService) {
		
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.eventActionMappingReadPlatformService = eventActionMappingReadPlatformService;
	}

	/**
	 * @param planType
	 * @param uriInfo
	 * @return retrieved all action details
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllEventActionMappingDetails(@QueryParam("planType") final String planType,
			@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<EventActionMappingData> eventActionDatas = this.eventActionMappingReadPlatformService.retrieveAllEventMapping();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, eventActionDatas, RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param uriInfo
	 * @return retrieved drop down data for creating eventactionmap 
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveEventActionMapTemplate(@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final EventActionMappingData data = handledTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);

	}

	/**
	 * @return
	 */
	private EventActionMappingData handledTemplateData() {

		final List<McodeData> actionData = this.eventActionMappingReadPlatformService.retrieveEventMapData("Action");
		final List<McodeData> eventsData = this.eventActionMappingReadPlatformService.retrieveEventMapData("Events");
		return new EventActionMappingData(actionData, eventsData);
	}

	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createEventActionMapping(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createEventActionMapping().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param id
	 * @param uriInfo
	 * @return single event details
	 */
	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleEventActionMappingDetails(@PathParam("id") final Long id,@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final EventActionMappingData actionMappingData = this.eventActionMappingReadPlatformService.retrieveEventActionDetail(id);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if(settings.isTemplate()){
		final List<McodeData> actionData = this.eventActionMappingReadPlatformService.retrieveEventMapData("Action");
		final List<McodeData> eventData = this.eventActionMappingReadPlatformService.retrieveEventMapData("Events");
		 actionMappingData.setEventData(eventData);
		 actionMappingData.setActionData(actionData);
		}
		return this.toApiJsonSerializer.serialize(settings, actionMappingData, RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param id
	 * @param apiRequestBodyAsJson
	 * @return single event action details are update here
	 */
	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateEventAction(@PathParam("id") final Long id,final String apiRequestBodyAsJson) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateEventActionMapping(id).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteEventAction(@PathParam("id") final Long id) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteEventActionMapping(id).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}
