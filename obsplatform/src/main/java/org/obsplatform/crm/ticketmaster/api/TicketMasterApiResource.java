package org.obsplatform.crm.ticketmaster.api;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.crm.ticketmaster.data.ClientTicketData;
import org.obsplatform.crm.ticketmaster.data.TicketMasterData;
import org.obsplatform.crm.ticketmaster.data.UsersData;
import org.obsplatform.crm.ticketmaster.domain.TicketDetail;
import org.obsplatform.crm.ticketmaster.domain.TicketDetailsRepository;
import org.obsplatform.crm.ticketmaster.service.TicketMasterReadPlatformService;
import org.obsplatform.crm.ticketmaster.service.TicketMasterWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiConstants;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.FileUtils;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.obsplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("/tickets")
@Component
@Scope("singleton")
public class TicketMasterApiResource {

	private Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "priorityType", "problemsDatas", "usersData",
					"status", "assignedTo", "userName", "ticketDate", "lastComment", "masterData", "comments", "departmentId"));

	
	/*private final String resourceNameForPermission = "TICKET";
	private final TicketMasterWritePlatformService ticketMasterWritePlatformService;
	private final TicketMasterReadPlatformService ticketMasterReadPlatformService;
	private final DefaultToApiJsonSerializer<TicketMasterData> toApiJsonSerializer;
	private final DefaultToApiJsonSerializer<ClientTicketData> clientToApiJsonSerializer;
	private final DefaultToApiJsonSerializer<MCodeData> statusToApiJsonSerializer;
	private final DocumentWritePlatformService documentWritePlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final TicketDetailsRepository detailsRepository;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final PlatformSecurityContext context;
	private final MCodeReadPlatformService codeReadPlatformService;
	private final FromJsonHelper fromApiJsonHelper;
	private final ClientReadPlatformService clientReadPlatformService;

	@Autowired
	public TicketMasterApiResource(final TicketMasterWritePlatformService ticketMasterWritePlatformService,
			final TicketMasterReadPlatformService ticketMasterReadPlatformService,
			final DefaultToApiJsonSerializer<TicketMasterData> toApiJsonSerializer,
			final DefaultToApiJsonSerializer<ClientTicketData> clientToApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper, final TicketDetailsRepository detailsRepository,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final PlatformSecurityContext context, final MCodeReadPlatformService codeReadPlatformService,
			final DocumentWritePlatformService documentWritePlatformService, final FromJsonHelper fromApiJsonHelper,
			final DefaultToApiJsonSerializer<MCodeData> statusToApiJsonSerializer,
			final ClientReadPlatformService clientReadPlatformService) {

		this.ticketMasterWritePlatformService = ticketMasterWritePlatformService;
		this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.clientToApiJsonSerializer = clientToApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.detailsRepository = detailsRepository;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.context = context;
		this.codeReadPlatformService = codeReadPlatformService;
		this.documentWritePlatformService = documentWritePlatformService;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.statusToApiJsonSerializer = statusToApiJsonSerializer;
		this.clientReadPlatformService = clientReadPlatformService;
	}*/
	 

	private String resourceNameForPermission = "TICKET";

	private TicketMasterWritePlatformService ticketMasterWritePlatformService;
	private TicketMasterReadPlatformService ticketMasterReadPlatformService;
	private DefaultToApiJsonSerializer<TicketMasterData> toApiJsonSerializer;
	private DefaultToApiJsonSerializer<ClientTicketData> clientToApiJsonSerializer;
	private DefaultToApiJsonSerializer<MCodeData> statusToApiJsonSerializer;
	private ApiRequestParameterHelper apiRequestParameterHelper;
	private TicketDetailsRepository detailsRepository;
	private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private PlatformSecurityContext context;
	private MCodeReadPlatformService codeReadPlatformService;
	private FromJsonHelper fromApiJsonHelper;
	private ClientReadPlatformService clientReadPlatformService;

	@Autowired
	public void setTicketMasterWritePlatformService(TicketMasterWritePlatformService ticketMasterWritePlatformService) {
		this.ticketMasterWritePlatformService = ticketMasterWritePlatformService;
	}

	@Autowired
	public void setTicketMasterReadPlatformService(TicketMasterReadPlatformService ticketMasterReadPlatformService) {
		this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
	}

	@Autowired
	public void setToApiJsonSerializer(DefaultToApiJsonSerializer<TicketMasterData> toApiJsonSerializer) {
		this.toApiJsonSerializer = toApiJsonSerializer;
	}

	@Autowired
	public void setClientToApiJsonSerializer(DefaultToApiJsonSerializer<ClientTicketData> clientToApiJsonSerializer) {
		this.clientToApiJsonSerializer = clientToApiJsonSerializer;
	}

	@Autowired
	public void setStatusToApiJsonSerializer(DefaultToApiJsonSerializer<MCodeData> statusToApiJsonSerializer) {
		this.statusToApiJsonSerializer = statusToApiJsonSerializer;
	}

	@Autowired
	public void setApiRequestParameterHelper(ApiRequestParameterHelper apiRequestParameterHelper) {
		this.apiRequestParameterHelper = apiRequestParameterHelper;
	}

	@Autowired
	public void setDetailsRepository(TicketDetailsRepository detailsRepository) {
		this.detailsRepository = detailsRepository;
	}

	@Autowired
	public void setCommandsSourceWritePlatformService(PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}

	@Autowired
	public void setContext(PlatformSecurityContext context) {
		this.context = context;
	}

	@Autowired
	public void setCodeReadPlatformService(MCodeReadPlatformService codeReadPlatformService) {
		this.codeReadPlatformService = codeReadPlatformService;
	}

	@Autowired
	public void setFromApiJsonHelper(FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	@Autowired
	public void setClientReadPlatformService(ClientReadPlatformService clientReadPlatformService) {
		this.clientReadPlatformService = clientReadPlatformService;
	}

	/**
	 * Method To Create TicketMaster
	 * 
	 * @param clientId
	 * @param jsonRequestBody
	 * @return String Json Result
	 */
	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createTicketMaster(
			@PathParam("clientId") final Long clientId, @HeaderParam("Content-Length") final Long fileSize,
			@FormDataParam("file") final InputStream inputStream, @FormDataParam("file") final FormDataContentDisposition fileDetails,
			@FormDataParam("file") final FormDataBodyPart bodyPart, @FormDataParam("assignedTo") final int assignedTo,
			@FormDataParam("dateFormat") final String dateFormat, @FormDataParam("description") final String description,
			@FormDataParam("dueTime") final String dueTime, @FormDataParam("locale") final String locale,
			@FormDataParam("priority") final String priority, @FormDataParam("problemCode") final int problemCode,
			@FormDataParam("sourceOfTicket") final String sourceOfTicket, @FormDataParam("ticketDate") final String ticketDate,
			@FormDataParam("ticketTime") final String ticketTime, @FormDataParam("ticketURL") final String ticketURL, 
			@FormDataParam("issue") final String issue, @FormDataParam("status") final String status, @FormDataParam("notes") final String notes, 
			@FormDataParam("departmentId") final Long departmentId, @FormDataParam("action") final String action) {

		try {

			FileUtils.validateFileSizeWithinPermissibleRange(fileSize, null, ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);

			String fileUploadLocation = FileUtils.generateFileParentDirectory("clients", clientId);

			/** Recursively create the directory if it does not exist **/
			if (!new File(fileUploadLocation).isDirectory()) {
				new File(fileUploadLocation).mkdirs();
			}
			String fileLocation = null;
			if (fileDetails != null) {
				fileLocation = FileUtils.saveToFileSystem(inputStream,
						fileUploadLocation, fileDetails.getFileName());
			}

			JSONObject ticketJson = new JSONObject();
			ticketJson.put("assignedTo", assignedTo);
			ticketJson.put("dateFormat", dateFormat);
			ticketJson.put("description", description);
			ticketJson.put("dueTime", dueTime);
			ticketJson.put("locale", locale);
			ticketJson.put("priority", priority);
			ticketJson.put("problemCode", problemCode);
			ticketJson.put("sourceOfTicket", sourceOfTicket);
			ticketJson.put("ticketDate", ticketDate);
			ticketJson.put("ticketTime", ticketTime);
			ticketJson.put("ticketURL", ticketURL);
			ticketJson.put("fileLocation", fileLocation);
			ticketJson.put("issue", issue);
			ticketJson.put("status", status);
			ticketJson.put("notes", notes);
			ticketJson.put("departmentId", departmentId);
			ClientData client = this.clientReadPlatformService.retrieveOne(clientId);
			ticketJson.put("assignFrom", client.getDisplayName());

			/*
			 * CommandProcessingResult result = processCreateTicket(clientId, ticketJson.toString()); 
			 * return this.toApiJsonSerializer.serialize(result);
			 */

			return returnTicket(clientId, ticketJson.toString());
		} catch (Exception e) {
			e.getStackTrace();
			String result = null;
			return result;
		}
	}

	public String returnTicket(final Long clientId, final String ticketJson) {
		CommandProcessingResult result = processCreateTicket(clientId, ticketJson);
		return this.toApiJsonSerializer.serialize(result);
	}

	public CommandProcessingResult processCreateTicket(Long clientId, String apiRequestData) {

		try {
			CommandProcessingResult result = null;
			Long userId = null;
			final SecurityContext context = SecurityContextHolder.getContext();
			if (context.getAuthentication() != null) {
				final AppUser appUser = this.context.authenticatedUser();
				userId = appUser.getId();
			}
			if (userId != null) {
				final CommandWrapper commandRequest = new CommandWrapperBuilder().createTicketMaster(clientId).withJson(apiRequestData.toString()).build();
				result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
				return result;
			} else {
				final JsonElement parsedCommand = this.fromApiJsonHelper.parse(apiRequestData.toString());
				final JsonCommand command = JsonCommand.from(apiRequestData.toString(), parsedCommand, this.fromApiJsonHelper, "TICKET", 
						clientId, null, null, clientId, null, null, null, null, null, null, null);
				result = this.ticketMasterWritePlatformService.createTicketMaster(command);
				return result;
			}

		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Method to Close Ticket
	 * 
	 * @param ticketId
	 * @param jsonRequestBody
	 * @return
	 */
	@PUT
	@Path("{ticketId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String closeTicket(@PathParam("ticketId") final Long ticketId,
			final String jsonRequestBody) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteTicketMaster(ticketId).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * Listing tickets
	 * */
	@GET
	@Path("alltickets")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String assignedAllTickets(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset, 
			@QueryParam("statusType") final String statusType) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final SearchSqlQuery searchTicketMaster = SearchSqlQuery.forSearch(sqlSearch, offset, limit);
		final Page<ClientTicketData> data = this.ticketMasterReadPlatformService.retrieveAssignedTicketsForNewClient(searchTicketMaster, statusType, null);

		return this.clientToApiJsonSerializer.serialize(data);
	}

	/**
	 * Method for Retrieving Ticket Details for template
	 * 
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTicketMasterTemplateData(@Context final UriInfo uriInfo, @QueryParam("templateFor") final String templateFor) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

		if (templateFor != null && "closeticket".equalsIgnoreCase(templateFor)) {
			final Collection<MCodeData> closedStatusdata = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TICKET_STATUS, "2");
			return this.statusToApiJsonSerializer.serialize(settings, closedStatusdata, RESPONSE_PARAMETERS);

		} else {
			final Collection<MCodeData> sourceData = codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TICKET_SOURCE);
			final Collection<MCodeData> statusdata = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TICKET_STATUS, "1");
			final TicketMasterData templateData = handleTicketTemplateData(sourceData);
			templateData.setDate(DateUtils.getLocalDateOfTenantForClient());
			templateData.setStatusData(statusdata);

			return this.toApiJsonSerializer.serialize(settings, templateData, RESPONSE_PARAMETERS);
		}
	}

	/**
	 * Method for retrieve Single Ticket for clientId
	 * 
	 * @param clientId
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleClientTicketDetails(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final List<TicketMasterData> data = this.ticketMasterReadPlatformService.retrieveClientTicketDetails(clientId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_PARAMETERS);
	}

	/**
	 * Method to get TicketDetails for update
	 * 
	 * @param clientId
	 * @param ticketId
	 * @param uriInfo
	 * @return
	 */
	// clientapp side
	@GET
	@Path("{clientId}/update/{ticketId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientSingleTicketDetails(@PathParam("clientId") final Long clientId, @PathParam("ticketId") final Long ticketId,
			@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);
		final TicketMasterData data = this.ticketMasterReadPlatformService.retrieveSingleTicketDetails(clientId, ticketId);
		final Collection<MCodeData> statusdata = this.codeReadPlatformService.getCodeValue("Ticket Status", "1");
		data.setStatusData(statusdata);
		final List<UsersData> userData = this.ticketMasterReadPlatformService.retrieveUsers();
		data.setUsersData(userData);
		final List<EnumOptionData> priorityData = this.ticketMasterReadPlatformService.retrievePriorityData();
		final Collection<MCodeData> problemsDatas = this.codeReadPlatformService.getCodeValue("Problem Code");
		data.setPriorityType(priorityData);
		data.setProblemsDatas(problemsDatas);
		data.clientData(clientData);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_PARAMETERS);
	}

	/**
	 * Method to retrieve Ticket History
	 * 
	 * @param ticketId
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("{ticketId}/history")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String ticketHistory(@PathParam("ticketId") final Long ticketId, @QueryParam("historyParam") String historyParam,
			@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final String description = this.ticketMasterWritePlatformService.retrieveTicketProblems(ticketId);
		final List<TicketMasterData> data = this.ticketMasterReadPlatformService.retrieveClientTicketHistory(ticketId, historyParam);

		final TicketMasterData masterData = new TicketMasterData(description, data);

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, masterData, RESPONSE_PARAMETERS);

	}

	/**
	 * Method to download file and print if exists
	 * 
	 * @param ticketId
	 * @return
	 */
	@GET
	@Path("{ticketId}/print")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response downloadFile(@PathParam("ticketId") final Long ticketId) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final TicketDetail ticketDetail = this.detailsRepository.findOne(ticketId);
		final String printFileName = ticketDetail.getAttachments();
		final File file = new File(printFileName);
		final ResponseBuilder response = Response.ok(file);
		response.header("Content-Disposition", "attachment; filename=\"" + printFileName + "\"");
		response.header("Content-Type", "application/pdf");
		return response.build();
	}

	/**
	 * Method to handle Template Data
	 * 
	 * @param responseParameters
	 * @param singleTicketData
	 * @return
	 */
	private TicketMasterData handleTicketTemplateData(final Collection<MCodeData> sourceData) {
		final List<EnumOptionData> priorityData = this.ticketMasterReadPlatformService.retrievePriorityData();
		final Collection<MCodeData> datas = this.codeReadPlatformService.getCodeValue("Problem Code");
		final List<UsersData> userData = this.ticketMasterReadPlatformService.retrieveUsers();

		return new TicketMasterData(datas, userData, priorityData, sourceData);
	}

	@GET
	@Path("{clientId}/{ticketId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientSingleTicket(@PathParam("clientId") final Long clientId,
			@PathParam("ticketId") final Long ticketId, @Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final TicketMasterData data = this.ticketMasterReadPlatformService.retrieveTicket(clientId, ticketId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_PARAMETERS);

	}

	/*
	 * this is to retrive last created ticket's issueid
	 */
	@GET
	@Path("issueid")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveIssueIdOfLastTicket(@Context final UriInfo uriInfo) {
		try {
			context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
			Object issueId = this.ticketMasterReadPlatformService.retrieveIssueIdOfLastTicket();
			JSONObject json = new JSONObject();
			json.put("issueId", issueId);
			return this.toApiJsonSerializer.serialize(json);/* return issueId; */
		} catch (Exception e) {
			return null;
		}
	}

	@GET
	@Path("getTicketsByDate")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTicketsByDate(@Context final UriInfo uriInfo, @QueryParam("statusType") final String statusType,
			@QueryParam("date") final String date) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		final List<TicketMasterData> data = this.ticketMasterReadPlatformService.retrieveTicketsByDate(statusType, date);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_PARAMETERS);
	}

}