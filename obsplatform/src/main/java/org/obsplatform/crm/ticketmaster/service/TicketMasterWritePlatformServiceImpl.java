package org.obsplatform.crm.ticketmaster.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.crm.ticketmaster.command.TicketMasterCommand;
import org.obsplatform.crm.ticketmaster.domain.TicketDetail;
import org.obsplatform.crm.ticketmaster.domain.TicketDetailsRepository;
import org.obsplatform.crm.ticketmaster.domain.TicketHistory;
import org.obsplatform.crm.ticketmaster.domain.TicketHistoryRepository;
import org.obsplatform.crm.ticketmaster.domain.TicketMaster;
import org.obsplatform.crm.ticketmaster.domain.TicketMasterRepository;
import org.obsplatform.crm.ticketmaster.serialization.TicketMasterFromApiJsonDeserializer;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.FileUtils;
import org.obsplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.obsplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.useradministration.domain.AppUserRepository;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.obsplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

@Service
public class TicketMasterWritePlatformServiceImpl implements TicketMasterWritePlatformService{
	
	/*private PlatformSecurityContext context;
	private TicketMasterRepository repository;
	private TicketDetailsRepository ticketDetailsRepository;
	private TicketMasterFromApiJsonDeserializer fromApiJsonDeserializer;
	private TicketMasterRepository ticketMasterRepository;
	private TicketDetailsRepository detailsRepository;
	private final TicketHistoryRepository historyRepository;
	private final AppUserRepository appUserRepository;
	private final TicketMasterReadPlatformService ticketMasterReadPlatformService ;
	private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	
	@Autowired
	public TicketMasterWritePlatformServiceImpl(final PlatformSecurityContext context,
			final TicketMasterRepository repository,final TicketDetailsRepository ticketDetailsRepository, 
			final TicketMasterFromApiJsonDeserializer fromApiJsonDeserializer,final TicketMasterRepository ticketMasterRepository,
			TicketDetailsRepository detailsRepository,AppUserRepository appUserRepository,
			TicketMasterReadPlatformService ticketMasterReadPlatformService, final TicketHistoryRepository historyRepository,
			final ActiondetailsWritePlatformService actiondetailsWritePlatformService,
			final ActionDetailsReadPlatformService actionDetailsReadPlatformService) {
		
		this.context = context;
		this.repository = repository;
		this.ticketDetailsRepository = ticketDetailsRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.ticketMasterRepository = ticketMasterRepository;
		this.detailsRepository = detailsRepository;
		this.appUserRepository = appUserRepository;
		this.ticketMasterReadPlatformService=ticketMasterReadPlatformService;
		this.historyRepository = historyRepository;
		this.actiondetailsWritePlatformService = actiondetailsWritePlatformService;
		this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
		
	}*/
		
	
	
	private PlatformSecurityContext context;
	private TicketMasterRepository repository;
	private TicketDetailsRepository ticketDetailsRepository;
	private TicketMasterFromApiJsonDeserializer fromApiJsonDeserializer;
	private TicketMasterRepository ticketMasterRepository;
	private TicketDetailsRepository detailsRepository;
	private TicketHistoryRepository historyRepository;
	private AppUserRepository appUserRepository;
	private TicketMasterReadPlatformService ticketMasterReadPlatformService ;
	private ActiondetailsWritePlatformService actiondetailsWritePlatformService;
	private ActionDetailsReadPlatformService actionDetailsReadPlatformService;

	
	@Autowired
	public void setContext(PlatformSecurityContext context) {
		this.context = context;
	}

	@Autowired
	public void setRepository(TicketMasterRepository repository) {
		this.repository = repository;
	}

	@Autowired
	public void setTicketDetailsRepository(TicketDetailsRepository ticketDetailsRepository) {
		this.ticketDetailsRepository = ticketDetailsRepository;
	}

	@Autowired
	public void setFromApiJsonDeserializer(TicketMasterFromApiJsonDeserializer fromApiJsonDeserializer) {
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
	}

	@Autowired
	public void setTicketMasterRepository(TicketMasterRepository ticketMasterRepository) {
		this.ticketMasterRepository = ticketMasterRepository;
	}

	@Autowired
	public void setDetailsRepository(TicketDetailsRepository detailsRepository) {
		this.detailsRepository = detailsRepository;
	}

	@Autowired
	public void setHistoryRepository(TicketHistoryRepository historyRepository) {
		this.historyRepository = historyRepository;
	}

	@Autowired
	public void setTicketMasterReadPlatformService(TicketMasterReadPlatformService ticketMasterReadPlatformService) {
		this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
	}

	@Autowired
	public void setAppUserRepository(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Autowired
	public void setActiondetailsWritePlatformService(ActiondetailsWritePlatformService actiondetailsWritePlatformService) {
		this.actiondetailsWritePlatformService = actiondetailsWritePlatformService;
	}

	@Autowired
	public void setActionDetailsReadPlatformService(ActionDetailsReadPlatformService actionDetailsReadPlatformService) {
		this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
	}

	private void handleDataIntegrityIssues(final TicketMasterCommand command,
			final DataIntegrityViolationException dve) {
		
	}

	@Override
	public Long upDateTicketDetails(TicketMasterCommand ticketMasterCommand,
			DocumentCommand documentCommand, Long ticketId, InputStream inputStream, String ticketURL, String action) {
		
		if(action.equalsIgnoreCase("save")){

			
		 	try {
			 String fileUploadLocation = FileUtils.generateFileParentDirectory(documentCommand.getParentEntityType(),
	                 documentCommand.getParentEntityId());
	        
	         /** Recursively create the directory if it does not exist **/
	         if (!new File(fileUploadLocation).isDirectory()) {
	             new File(fileUploadLocation).mkdirs();
	         }
	         String fileLocation = null;
	         if(documentCommand.getFileName() != null){
	          fileLocation = FileUtils.saveToFileSystem(inputStream, fileUploadLocation, documentCommand.getFileName());
	         }
	        Long createdbyId = context.authenticatedUser().getId();
	        TicketDetail ticket = this.ticketMasterReadPlatformService.retrieveTicketDetail(ticketId);
	        TicketHistory history = this.ticketMasterReadPlatformService.retrieveTickethistory(ticketId);
	        String assignFrom=null;
	        AppUser user =this.appUserRepository.findOne(ticket.getAssignedTo());
	        if(user.getUsername().equalsIgnoreCase(ticket.getAssignFrom())) {
	        	assignFrom =ticket.getAssignFrom();
	        }else{
	        	assignFrom = user.getFirstname();
	        }
			TicketDetail detail = new TicketDetail(ticketId,ticketMasterCommand.getComments(),fileLocation,ticketMasterCommand.getAssignedTo(),createdbyId,assignFrom,ticketMasterCommand.getStatus(),ticketMasterCommand.getUsername(),ticketMasterCommand.getNotes());
	        TicketMaster ticketMaster = this.ticketMasterRepository.findOne(ticketId);
			if (ticketMasterCommand.getStatus().equalsIgnoreCase("Closed")) {
				JsonObject obj = new JsonObject();
				obj.addProperty("resolutionDescription", ticketMasterCommand.getResolutionDescription());
				obj.addProperty("status", ticketMasterCommand.getStatus());
				obj.addProperty("assignedTo", ticketMasterCommand.getAssignedTo());
				if(ticketMasterCommand.getResolutionDate() != null){
					obj.addProperty("resolutionDate", ticketMasterCommand.getResolutionDate().toString());
				}else{
					obj.addProperty("resolutionDate", StringUtils.EMPTY);
				}
				this.fromApiJsonDeserializer.validateForUpdate(obj.toString());
			}else if(ticketMasterCommand.getStatus().equalsIgnoreCase("FollowUp")){
				JsonObject obj = new JsonObject();
				if(ticketMasterCommand.getNextCallDate()!=null){
				   obj.addProperty("nextCallDate", ticketMasterCommand.getNextCallDate().toString());
				   obj.addProperty("nextCallTime", ticketMasterCommand.getNextCallTime().toString());
				}else{
					obj.addProperty("nextCallDate", StringUtils.EMPTY);
					obj.addProperty("nextCallTime", StringUtils.EMPTY);
				}
				if(ticketMasterCommand.getNotes().equalsIgnoreCase("undefined"))
				  obj.addProperty("notes", StringUtils.EMPTY);
				this.fromApiJsonDeserializer.validateForUpdate(obj.toString());
			}else if(ticketMasterCommand.getStatus().equalsIgnoreCase("Problems")){
				JsonObject obj = new JsonObject();
				if(ticketMasterCommand.getNextCallDate()!=null){
				   obj.addProperty("nextCallDate", ticketMasterCommand.getNextCallDate().toString());
				   obj.addProperty("nextCallTime", ticketMasterCommand.getNextCallTime().toString());
				}else{
					obj.addProperty("nextCallDate", StringUtils.EMPTY);
					obj.addProperty("nextCallTime", StringUtils.EMPTY);
				}
				if(ticketMasterCommand.getNotes().equalsIgnoreCase("undefined"))
				  obj.addProperty("notes", StringUtils.EMPTY);
				this.fromApiJsonDeserializer.validateForUpdate(obj.toString());
			}else if(ticketMasterCommand.getStatus().equalsIgnoreCase("Appointment")){
				JsonObject obj = new JsonObject();
				if(ticketMasterCommand.getAppointmentDate()!=null){
				  obj.addProperty("appointmentDate", ticketMasterCommand.getAppointmentDate().toString());
				  obj.addProperty("appointmentTime", ticketMasterCommand.getAppointmentTime().toString());
				}else{
					obj.addProperty("appointmentDate", StringUtils.EMPTY);
					obj.addProperty("appointmentTime", StringUtils.EMPTY);
				}
				this.fromApiJsonDeserializer.validateForUpdate(obj.toString());
			}
			
	        ticketMaster.updateTicket(ticketMasterCommand);
	        //ticketMaster.setDescription(ticketMasterCommand.getDescription());
	        ticketMaster.setIssue(ticketMasterCommand.getIssue());
	        ticketMaster.setStatus(ticketMasterCommand.getStatus());
	        ticketMaster.setStatusCode(ticketMasterCommand.getStatusCode());
	        this.ticketMasterRepository.save(ticketMaster);
	        
	        this.ticketDetailsRepository.save(detail);
	        if(history.getStatus()!=null){
	        	if(!history.getStatus().equalsIgnoreCase(ticketMasterCommand.getStatus()) || !history.getAssignedTo().equals(ticketMasterCommand.getAssignedTo())){
	        	TicketHistory thistory = new TicketHistory(ticketId,ticketMasterCommand.getAssignedTo(),ticketMasterCommand.getStatus(),assignFrom);
	        	thistory.setCreatedbyId(ticketMasterCommand.getCreatedbyId());
	        	this.historyRepository.saveAndFlush(thistory);
	        	}
	        } if("Closed".equalsIgnoreCase(ticketMasterCommand.getStatus())){
		    		processNotifyMessages(EventActionConstants.EVENT_CLOSE_TICKET, ticketMaster.getClientId(), ticketMaster.getId().toString(), ticketURL);
			  		processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, ticketMaster.getClientId(), ticketMaster.getId().toString(), "CLOSE TICKET");
		        }else if("Appointment".equalsIgnoreCase(ticketMasterCommand.getStatus())){
	        		processNotifyMessages(EventActionConstants.EVENT_APPOINTMENT_TICKET, ticketMaster.getClientId(), ticketMaster.getId().toString(), ticketURL);
	        		processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, ticketMaster.getClientId(), ticketMaster.getId().toString(), "UPDATE TICKET");
	             }else{
		        	processNotifyMessages(EventActionConstants.EVENT_EDIT_TICKET, ticketMaster.getClientId(), ticketMaster.getId().toString(), ticketURL);
		            processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, ticketMaster.getClientId(), ticketMaster.getId().toString(), "UPDATE TICKET");
		         }  
	       
	        return ticket.getId();
		 }
		 catch (DataIntegrityViolationException dve) {
		handleDataIntegrityIssues(ticketMasterCommand, dve);
		return Long.valueOf(-1);
		 } catch (IOException e) {
	         throw new DocumentManagementException(documentCommand.getName());
		 }
			

		}
		
		else{
			

			
		 	try {
			 String fileUploadLocation = FileUtils.generateFileParentDirectory(documentCommand.getParentEntityType(),
	                 documentCommand.getParentEntityId());
	        
	         /** Recursively create the directory if it does not exist **/
	         if (!new File(fileUploadLocation).isDirectory()) {
	             new File(fileUploadLocation).mkdirs();
	         }
	         String fileLocation = null;
	         if(documentCommand.getFileName() != null){
	          fileLocation = FileUtils.saveToFileSystem(inputStream, fileUploadLocation, documentCommand.getFileName());
	         }
	        Long createdbyId = context.authenticatedUser().getId();
	        TicketDetail ticket = this.ticketMasterReadPlatformService.retrieveTicketDetail(ticketId);
	        TicketHistory history = this.ticketMasterReadPlatformService.retrieveTickethistory(ticketId);
	        //String assignFrom = null;
	        /*AppUser user =this.appUserRepository.findOne(ticket.getAssignedTo());
	        if(user.getUsername().equalsIgnoreCase(ticket.getAssignFrom())) {
	        	assignFrom =ticket.getAssignFrom();
	        }else{
	        	assignFrom = user.getUsername();
	        }*/
	        TicketDetail detail = new TicketDetail(ticketId,ticketMasterCommand.getComments(),fileLocation,ticket.getAssignedTo(),createdbyId,ticket.getAssignFrom(),ticketMasterCommand.getStatus(),ticketMasterCommand.getUsername(),ticketMasterCommand.getNotes());
	        /*TicketDetail detail = this.ticketDetailsRepository.findOne(ticket.getId());
	        detail.setAttachments(fileLocation);*/
	        TicketMaster ticketMaster = this.ticketMasterRepository.findOne(ticketId);
			/*if (ticketMasterCommand.getStatus().equalsIgnoreCase("Closed")) {
				JsonObject obj = new JsonObject();
				obj.addProperty("resolutionDescription", ticketMasterCommand.getResolutionDescription());
				obj.addProperty("status", ticketMasterCommand.getStatus());
				obj.addProperty("assignedTo", ticketMasterCommand.getAssignedTo());
				this.fromApiJsonDeserializer.validateForUpdate(obj.toString());
			}else if(ticketMasterCommand.getStatus().equalsIgnoreCase("FollowUp")){
				JsonObject obj = new JsonObject();
				if(ticketMasterCommand.getNextCallDate()!=null){
				   obj.addProperty("nextCallDate", ticketMasterCommand.getNextCallDate().toString());
				   obj.addProperty("nextCallTime", ticketMasterCommand.getNextCallTime().toString());
				c}else{
					obj.addProperty("nextCallDate", StringUtils.EMPTY);
					obj.addProperty("nextCallTime", StringUtils.EMPTY);
				}
				if(ticketMasterCommand.getNotes().equalsIgnoreCase("undefined"))
				  obj.addProperty("notes", StringUtils.EMPTY);
				this.fromApiJsonDeserializer.validateForUpdate(obj.toString());
			}else if(ticketMasterCommand.getStatus().equalsIgnoreCase("Appointment")){
				JsonObject obj = new JsonObject();
				if(ticketMasterCommand.getAppointmentDate()!=null){
				  obj.addProperty("appointmentDate", ticketMasterCommand.getAppointmentDate().toString());
				  obj.addProperty("appointmentTime", ticketMasterCommand.getAppointmentTime().toString());
				}else{
					obj.addProperty("appointmentDate", StringUtils.EMPTY);
					obj.addProperty("appointmentTime", StringUtils.EMPTY);
				}
				this.fromApiJsonDeserializer.validateForUpdate(obj.toString());
			}*/
			
	        ticketMaster.updateTicket(ticketMasterCommand);
	        if(action.equalsIgnoreCase("save")){
	        	
	        this.ticketMasterRepository.save(ticketMaster);
	        
	        }
	        this.ticketDetailsRepository.save(detail);
	       /* if(history.getStatus()!=null){
	        	if(!history.getStatus().equalsIgnoreCase(ticketMasterCommand.getStatus()) || !history.getAssignedTo().equals(ticketMasterCommand.getAssignedTo())){
	        	TicketHistory thistory = new TicketHistory(ticketId,ticketMasterCommand.getAssignedTo(),ticketMasterCommand.getStatus(),assignFrom);
	        	this.historyRepository.saveAndFlush(thistory);
	        	}
	        } if("Closed".equalsIgnoreCase(ticketMasterCommand.getStatus()))
		        {
		    		 this.orderWritePlatformService.processNotifyMessages(EventActionConstants.EVENT_CLOSE_TICKET, ticketMaster.getClientId(), ticketMaster.getId().toString(), ticketURL);
			  		 this.orderWritePlatformService.processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, ticketMaster.getClientId(), ticketMaster.getId().toString(), "CLOSE TICKET");
					
		        }else{
		        		this.orderWritePlatformService.processNotifyMessages(EventActionConstants.EVENT_EDIT_TICKET, ticketMaster.getClientId(), ticketMaster.getId().toString(), ticketURL);
		        		this.orderWritePlatformService.processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, ticketMaster.getClientId(), ticketMaster.getId().toString(), "UPDATE TICKET");
		         }*/
	       
	        return detail.getId();
		 }
		 catch (DataIntegrityViolationException dve) {
		handleDataIntegrityIssues(ticketMasterCommand, dve);
		return Long.valueOf(-1);
		 } catch (IOException e) {
	         throw new DocumentManagementException(documentCommand.getName());
		 }
			

		}
		
	}

	@Override
	public CommandProcessingResult closeTicket( final JsonCommand command) {
		TicketMaster ticketMaster = null;
		try {
			this.context.authenticatedUser();
			
			this.fromApiJsonDeserializer.validateForClose(command.json());
			String ticketURL = command.stringValueOfParameterNamed("ticketURL");
			ticketMaster = this.repository.findOne(command.entityId());
			
			if (!ticketMaster.getStatus().equalsIgnoreCase("CLOSED")) {
				ticketMaster.closeTicket(command,this.context.authenticatedUser().getId());
				this.repository.save(ticketMaster);
		  		 
		  		processNotifyMessages(EventActionConstants.EVENT_CLOSE_TICKET, ticketMaster.getClientId(), ticketMaster.getId().toString(), ticketURL);
		  		processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, ticketMaster.getClientId(), ticketMaster.getId().toString(), "CLOSE TICKET");
				
			} else {
				
			}
		}catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssuesforJson(command, dve);
		}
		return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(command.entityId()).withClientId(ticketMaster.getClientId()).build();
	}

	private void handleDataIntegrityIssuesforJson(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		
	}

	@Override
	public String retrieveTicketProblems(final Long ticketId) {
		try {
			final TicketMaster master = this.repository.findOne(ticketId);
			final String description = master.getDescription();
			return description;
		}catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(null, dve);
			return "";
		}
	}
	
	@Transactional
	@Override
	public CommandProcessingResult createTicketMaster(final JsonCommand command){
		
		 try {
			 Long created = null;
			 SecurityContext context = SecurityContextHolder.getContext();
			 if (context.getAuthentication() != null) {
				 final AppUser appUser = this.context.authenticatedUser();
				 created = appUser.getId();
	        }else{
	        		created = new Long(0);
	        }	 
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			String ticketURL = command.stringValueOfParameterNamed("ticketURL");
			final TicketMaster ticketMaster = TicketMaster.fromJson(command);
			ticketMaster.setCreatedbyId(created);
			this.repository.saveAndFlush(ticketMaster);
			final TicketDetail details = TicketDetail.fromJson(command);
			details.setAttachments(command.stringValueOfParameterNamed("fileLocation"));
			details.setComments("undefined");
			details.setNotes(command.stringValueOfParameterNamed("notes"));
			details.setTicketId(ticketMaster.getId());
			details.setCreatedbyId(created);
			this.detailsRepository.saveAndFlush(details);
			final TicketHistory history = TicketHistory.fromJson(command);
			history.setTicketId(ticketMaster.getId());
			history.setCreatedbyId(created);
			this.historyRepository.saveAndFlush(history);
			
			processNotifyMessages(EventActionConstants.EVENT_CREATE_TICKET, command.getClientId(), ticketMaster.getId().toString(), ticketURL);
			
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, command.getClientId(), ticketMaster.getId().toString(), "CREATE TICKET");
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(ticketMaster.getId()).withClientId(command.getClientId()).build();
		 } catch (DataIntegrityViolationException dve) {
			 	return new CommandProcessingResult(Long.valueOf(-1));
		   } catch (ParseException e) {
			 throw new PlatformDataIntegrityException("invalid.date.format", "invalid.date.format", "ticketDate","invalid.date.format");
		 	 }
	}
	
	@Override
	public void processNotifyMessages(String eventName, Long clientId, String orderId, String actionType) {

		List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(eventName);

		if (actionDetaislDatas.size() != 0) {
			this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, clientId, orderId, actionType);
		}
	}
	
	
	
}