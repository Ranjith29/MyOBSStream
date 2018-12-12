package org.obsplatform.organisation.ticketassignrule.api;

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

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.department.data.DepartmentData;
import org.obsplatform.organisation.department.service.DepartmentReadPlatformService;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.organisation.ticketassignrule.data.TicketAssignRuleData;
import org.obsplatform.organisation.ticketassignrule.service.TicketAssignRuleReadPlatformService;
import org.obsplatform.portfolio.client.service.ClientCategoryData;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/assignticketrule")
@Component
@Scope("singleton")
public class AssignTicketRuleApiResource {
	
	private Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "clientcategoryId", "departmentId"));
	
	private String resourceNameForPermission = "TICKETASSIGNRULE";
	
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<TicketAssignRuleData> toApiJsonSerializer;
	private final PlatformSecurityContext context;
	private final TicketAssignRuleReadPlatformService ticketAssignRuleReadPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final ClientReadPlatformService clientReadPlatformService;
	private final MCodeReadPlatformService codeReadPlatformService;
	private final DepartmentReadPlatformService departmentReadPlatformService;
	
	@Autowired
	public AssignTicketRuleApiResource(final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final DefaultToApiJsonSerializer<TicketAssignRuleData> toApiJsonSerializer,
			final PlatformSecurityContext context,final TicketAssignRuleReadPlatformService ticketAssignRuleReadPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,final ClientReadPlatformService clientReadPlatformService,
			final MCodeReadPlatformService codeReadPlatformService,final DepartmentReadPlatformService departmentReadPlatformService){
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.context = context;
		this.ticketAssignRuleReadPlatformService = ticketAssignRuleReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.clientReadPlatformService = clientReadPlatformService;
		this.codeReadPlatformService = codeReadPlatformService;
		this.departmentReadPlatformService = departmentReadPlatformService;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String createRuleForTicketAssignment(final String apiRequestBodyAsJson){
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createTicketAssignRule().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
	}
	
	
	@PUT
	@Path("{ticketassignruleid}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String updateRuleForTicketAssignment(@PathParam("ticketassignruleid") final Long ticketassignruleid, final String apiRequestBodyAsJson){
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTicketAssignRule(ticketassignruleid).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		 return this.toApiJsonSerializer.serialize(result);

	}
	
	@GET
	@Path("{ticketassignruleid}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTicketAssignmentData(@PathParam("ticketassignruleid") final Long ticketassignruleid,@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final TicketAssignRuleData ticketassignruledatas = this.ticketAssignRuleReadPlatformService.retrieveTicketAssignRuleData(ticketassignruleid);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if (settings.isTemplate()) {
			final TicketAssignRuleData ticketAssignRuleData = handleTemplateData();
			ticketassignruledatas.setBusinessprocessCodes(ticketAssignRuleData.getBusinessprocessCodes());
			ticketassignruledatas.setClientCategoryData(ticketAssignRuleData.getClientCategoryData());
			ticketassignruledatas.setDepartmentdatas(ticketAssignRuleData.getDepartmentdatas());
		}
		return this.toApiJsonSerializer.serialize(settings, ticketassignruledatas,RESPONSE_PARAMETERS);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllTicketAssignmentData(@Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
	    final List<TicketAssignRuleData> ticketassignruledatas = this.ticketAssignRuleReadPlatformService.retrieveAllTicketAssignRuleData();
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, ticketassignruledatas,RESPONSE_PARAMETERS);
	}
	
	@GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String ticketAssignmentTemplateDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
        TicketAssignRuleData ticketassignreleData = handleTemplateData();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, ticketassignreleData, RESPONSE_PARAMETERS);
    }
    private TicketAssignRuleData handleTemplateData() {
        final Collection<ClientCategoryData> clientCategoryData=this.clientReadPlatformService.retrieveClientCategories();
        final Collection<MCodeData> businessprocessCodes = this.codeReadPlatformService.getCodeValue("Problem Code");
        final Collection<DepartmentData> departmentdatas = this.departmentReadPlatformService.retrieveAllDepartmentData();
		return new TicketAssignRuleData(clientCategoryData,businessprocessCodes,departmentdatas);
	}
	
	@DELETE
	@Path("{ticketassignruleid}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteTicketAssignmentRule(@PathParam("ticketassignruleid") final Long ticketassignruleid) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteTicketAssignRule(ticketassignruleid).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}
