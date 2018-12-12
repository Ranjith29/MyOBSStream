package org.obsplatform.workflow.eventaction.service;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.java.dev.obs.beesmart.AddExternalBeesmartMethod;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;
import org.obsplatform.billing.selfcare.domain.SelfCare;
import org.obsplatform.billing.selfcare.domain.SelfCareRepository;
import org.obsplatform.billing.servicetransfer.service.ServiceTransferReadPlatformService;
import org.obsplatform.cms.eventmaster.domain.EventMaster;
import org.obsplatform.cms.eventmaster.domain.EventMasterRepository;
import org.obsplatform.cms.eventorder.domain.EventOrder;
import org.obsplatform.cms.eventorder.domain.EventOrderRepository;
import org.obsplatform.cms.eventorder.domain.EventOrderdetials;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.domain.ClientProspect;
import org.obsplatform.crm.clientprospect.domain.ClientProspectJpaRepository;
import org.obsplatform.crm.ticketmaster.api.TicketMasterApiResource;
import org.obsplatform.crm.ticketmaster.data.TicketMasterData;
import org.obsplatform.crm.ticketmaster.domain.TicketMaster;
import org.obsplatform.crm.ticketmaster.domain.TicketMasterRepository;
import org.obsplatform.crm.ticketmaster.service.TicketMasterReadPlatformService;
import org.obsplatform.crm.userchat.domain.UserChat;
import org.obsplatform.crm.userchat.domain.UserChatRepository;
import org.obsplatform.finance.billingorder.api.BillingTransactionConstants;
import org.obsplatform.finance.billingorder.service.BillingOrderWritePlatformService;
import org.obsplatform.finance.billingorder.service.InvoiceClient;
import org.obsplatform.finance.depositandrefund.domain.DepositAndRefund;
import org.obsplatform.finance.depositandrefund.domain.DepositAndRefundRepository;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingRepositoryWrapper;
import org.obsplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.FileUtils;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.obsplatform.logistics.itemdetails.domain.ItemDetailsAllocation;
import org.obsplatform.logistics.itemdetails.domain.ItemDetailsAllocationRepository;
import org.obsplatform.logistics.onetimesale.service.InvoiceOneTimeSale;
import org.obsplatform.organisation.address.domain.Address;
import org.obsplatform.organisation.address.domain.AddressRepository;
import org.obsplatform.organisation.department.data.DepartmentData;
import org.obsplatform.organisation.department.service.DepartmentReadPlatformService;
import org.obsplatform.organisation.employee.data.EmployeeData;
import org.obsplatform.organisation.employee.service.EmployeeReadPlatformService;
import org.obsplatform.organisation.feemaster.data.FeeMasterData;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.organisation.message.domain.BillingMessage;
import org.obsplatform.organisation.message.domain.BillingMessageRepository;
import org.obsplatform.organisation.message.domain.BillingMessageTemplate;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.obsplatform.organisation.message.exception.BillingMessageTemplateNotFoundException;
import org.obsplatform.organisation.ticketassignrule.data.TicketAssignRuleData;
import org.obsplatform.organisation.ticketassignrule.service.TicketAssignRuleReadPlatformService;
import org.obsplatform.portfolio.association.data.AssociationData;
import org.obsplatform.portfolio.association.exception.HardwareDetailsNotFoundException;
import org.obsplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.contract.domain.Contract;
import org.obsplatform.portfolio.contract.domain.ContractRepository;
import org.obsplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.exceptions.NoOrdersFoundException;
import org.obsplatform.portfolio.order.service.OrderAssembler;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.obsplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.obsplatform.provisioning.wifimaster.data.WifiData;
import org.obsplatform.provisioning.wifimaster.service.WifiMasterReadPlatformService;
import org.obsplatform.useradministration.data.AppUserData;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.useradministration.domain.AppUserRepository;
import org.obsplatform.useradministration.service.AppUserReadPlatformService;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.data.EventActionProcedureData;
import org.obsplatform.workflow.eventaction.data.OrderNotificationData;
import org.obsplatform.workflow.eventaction.domain.EventAction;
import org.obsplatform.workflow.eventaction.domain.EventActionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.gson.JsonObject;

@Service
public class EventActionWritePlatformServiceImpl implements ActiondetailsWritePlatformService {
	
	private final static Logger logger = LoggerFactory.getLogger(EventActionWritePlatformServiceImpl.class);

	/*private final OrderRepository orderRepository;
	private final TicketMasterRepository repository;
	private final ClientRepository clientRepository;
	private final EventOrderRepository eventOrderRepository;
	private final EventMasterRepository eventMasterRepository;
	private final EventActionRepository eventActionRepository;
	private final BillingMessageRepository messageDataRepository;
	private final AppUserReadPlatformService readPlatformService;
	private final InvoiceClient invoiceClient;
	private final ProcessRequestRepository processRequestRepository;
	private final BillingMessageTemplateRepository messageTemplateRepository;
	private final TicketMasterReadPlatformService ticketMasterReadPlatformService;
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
	private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper;
	private final EventActionReadPlatformService eventActionReadPlatformService;
	private final ConfigurationRepository configurationRepository;
	private final ServiceTransferReadPlatformService serviceTransferReadPlatformService;
	private final InvoiceOneTimeSale invoiceOneTimeSale;

	private final UserChatRepository userChatRepository;

	private final ContractRepository contractRepository;
	private final DepositAndRefundRepository depositAndRefundRepository;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService;
	private final OrderAssembler orderAssembler;
	private final SelfCareRepository selfCareRepository;
	private final PlanRepository planRepository;

	private BillingMessageTemplate activationTemplates;
	private BillingMessageTemplate reConnectionTemplates;
	private BillingMessageTemplate disConnectionTemplates;
	private BillingMessageTemplate paymentTemplates;
	private BillingMessageTemplate changePlanTemplates;
	private BillingMessageTemplate orderTerminationTemplates;

	private BillingMessageTemplate smsActivationTemplates;
	private BillingMessageTemplate smsDisconnectionTemplates;
	private BillingMessageTemplate smsReConnectionTemplates;
	private BillingMessageTemplate smsPaymentTemplates;
	private BillingMessageTemplate smsChangePlanTemplates;
	private BillingMessageTemplate smsOrderTerminationTemplates;

	private BillingMessageTemplate notifyTechicalTeam;
	private DepartmentReadPlatformService departmentReadPlatformService;
	private EmployeeReadPlatformService employeeReadPlatformService;
	private WifiMasterReadPlatformService wifiMasterReadPlatformService;
	
	private final AddressRepository addressRepository;
	private final AppUserRepository appUserRepository;
	private final TicketMasterWritePlatformService ticketMasterWritePlatformService;*/
	
	/*@Autowired
	public EventActionWritePlatformServiceImpl(final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
			final EventActionRepository eventActionRepository,
			final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,
			final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
			final OrderRepository orderRepository, final TicketMasterRepository repository,
			final ProcessRequestRepository processRequestRepository, final InvoiceClient invoiceClient,
			final BillingMessageRepository messageDataRepository, final ClientRepository clientRepository,
			final BillingMessageTemplateRepository messageTemplateRepository,
			final EventMasterRepository eventMasterRepository, final EventOrderRepository eventOrderRepository,
			final TicketMasterReadPlatformService ticketMasterReadPlatformService,
			final AppUserReadPlatformService readPlatformService,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper,
			final EventActionReadPlatformService eventActionReadPlatformService,
			final ConfigurationRepository configurationRepository, final UserChatRepository userChatRepository,
			final ServiceTransferReadPlatformService serviceTransferReadPlatformService,
			final InvoiceOneTimeSale invoiceOneTimeSale, final ContractRepository contractRepository,
			final DepositAndRefundRepository depositAndRefundRepository,
			final BillingOrderWritePlatformService billingOrderWritePlatformService,
			final OrderAssembler orderAssembler,
			final DepartmentReadPlatformService departmentReadPlatformService,
			final EmployeeReadPlatformService employeeReadPlatformService, final SelfCareRepository selfCareRepository,
			final PlanRepository planRepository,
			final WifiMasterReadPlatformService wifiMasterReadPlatformService,
			final AddressRepository addressRepository,
			final AppUserRepository appUserRepository,final TicketMasterWritePlatformService ticketMasterWritePlatformService) {

		this.repository = repository;
		this.orderRepository = orderRepository;
		this.clientRepository = clientRepository;
		this.readPlatformService = readPlatformService;
		this.eventOrderRepository = eventOrderRepository;
		this.eventActionRepository = eventActionRepository;
		this.eventMasterRepository = eventMasterRepository;
		this.messageDataRepository = messageDataRepository;
		this.invoiceClient = invoiceClient;
		this.processRequestRepository = processRequestRepository;
		this.messageTemplateRepository = messageTemplateRepository;
		this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
		this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
		this.contractPeriodReadPlatformService = contractPeriodReadPlatformService;
		this.hardwareAssociationReadplatformService = hardwareAssociationReadplatformService;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.recurringBillingRepositoryWrapper = recurringBillingRepositoryWrapper;
		this.eventActionReadPlatformService = eventActionReadPlatformService;
		this.configurationRepository = configurationRepository;
		this.serviceTransferReadPlatformService = serviceTransferReadPlatformService;
		this.invoiceOneTimeSale = invoiceOneTimeSale;
		this.userChatRepository = userChatRepository;
		this.contractRepository = contractRepository;
		this.depositAndRefundRepository = depositAndRefundRepository;
		this.billingOrderWritePlatformService = billingOrderWritePlatformService;
		this.orderAssembler = orderAssembler;
		this.departmentReadPlatformService = departmentReadPlatformService;
		this.employeeReadPlatformService = employeeReadPlatformService;
		this.selfCareRepository = selfCareRepository;
		this.planRepository = planRepository;
		this.wifiMasterReadPlatformService=wifiMasterReadPlatformService;
		this.addressRepository = addressRepository;
		this.appUserRepository = appUserRepository;
		this.ticketMasterWritePlatformService = ticketMasterWritePlatformService;
	}*/
	
	
	private OrderRepository orderRepository;
	private TicketMasterRepository repository;
	private ClientRepository clientRepository;
	private EventOrderRepository eventOrderRepository;
	private EventMasterRepository eventMasterRepository;
	private EventActionRepository eventActionRepository;
	private BillingMessageRepository messageDataRepository;
	private AppUserReadPlatformService readPlatformService;
	private InvoiceClient invoiceClient;
	private ProcessRequestRepository processRequestRepository;
	private BillingMessageTemplateRepository messageTemplateRepository;
	private TicketMasterReadPlatformService ticketMasterReadPlatformService;
	private ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	private ContractPeriodReadPlatformService contractPeriodReadPlatformService;
	private HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
	private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper;
	private EventActionReadPlatformService eventActionReadPlatformService;
	private ConfigurationRepository configurationRepository;
	private ServiceTransferReadPlatformService serviceTransferReadPlatformService;
	private InvoiceOneTimeSale invoiceOneTimeSale;

	private UserChatRepository userChatRepository;

	private ContractRepository contractRepository;
	private DepositAndRefundRepository depositAndRefundRepository;
	private BillingOrderWritePlatformService billingOrderWritePlatformService;
	private OrderAssembler orderAssembler;
	private SelfCareRepository selfCareRepository;
	private PlanRepository planRepository;
	private DepartmentReadPlatformService departmentReadPlatformService;
	private EmployeeReadPlatformService employeeReadPlatformService;
	private WifiMasterReadPlatformService wifiMasterReadPlatformService;
	private AddressRepository addressRepository;
	private AppUserRepository appUserRepository;
	private TicketMasterApiResource ticketMasterApiResource;
	private MCodeReadPlatformService codeReadPlatformService;
	private ItemDetailsAllocationRepository itemDetailsAllocationRepository;
	private TicketAssignRuleReadPlatformService ticketAssignRuleReadPlatformService;
	
	private RoutingDataSource dataSource;
	private ClientProspectJpaRepository clientProspectJpaRepository;
	
	private BillingMessageTemplate activationTemplates;
	private BillingMessageTemplate reConnectionTemplates;
	private BillingMessageTemplate disConnectionTemplates;
	private BillingMessageTemplate paymentTemplates;
	private BillingMessageTemplate changePlanTemplates;
	private BillingMessageTemplate orderTerminationTemplates;

	private BillingMessageTemplate smsActivationTemplates;
	private BillingMessageTemplate smsDisconnectionTemplates;
	private BillingMessageTemplate smsReConnectionTemplates;
	private BillingMessageTemplate smsPaymentTemplates;
	private BillingMessageTemplate smsChangePlanTemplates;
	private BillingMessageTemplate smsOrderTerminationTemplates;
	private BillingMessageTemplate notifyTechicalTeam;
	
	
	@Autowired
	public void setOrderRepository(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}
	@Autowired
	public void setRepository(TicketMasterRepository repository) {
		this.repository = repository;
	}
	@Autowired
	public void setClientRepository(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}
	@Autowired
	public void setEventOrderRepository(EventOrderRepository eventOrderRepository) {
		this.eventOrderRepository = eventOrderRepository;
	}
	@Autowired
	public void setEventMasterRepository(EventMasterRepository eventMasterRepository) {
		this.eventMasterRepository = eventMasterRepository;
	}
	@Autowired
	public void setEventActionRepository(EventActionRepository eventActionRepository) {
		this.eventActionRepository = eventActionRepository;
	}
	@Autowired
	public void setMessageDataRepository(BillingMessageRepository messageDataRepository) {
		this.messageDataRepository = messageDataRepository;
	}
	@Autowired
	public void setReadPlatformService(AppUserReadPlatformService readPlatformService) {
		this.readPlatformService = readPlatformService;
	}
	@Autowired
	public void setInvoiceClient(InvoiceClient invoiceClient) {
		this.invoiceClient = invoiceClient;
	}
	@Autowired
	public void setProcessRequestRepository(ProcessRequestRepository processRequestRepository) {
		this.processRequestRepository = processRequestRepository;
	}
	@Autowired
	public void setMessageTemplateRepository(BillingMessageTemplateRepository messageTemplateRepository) {
		this.messageTemplateRepository = messageTemplateRepository;
	}
	@Autowired
	public void setTicketMasterReadPlatformService(TicketMasterReadPlatformService ticketMasterReadPlatformService) {
		this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
	}
	@Autowired
	public void setActionDetailsReadPlatformService(ActionDetailsReadPlatformService actionDetailsReadPlatformService) {
		this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
	}
	@Autowired
	public void setContractPeriodReadPlatformService(ContractPeriodReadPlatformService contractPeriodReadPlatformService) {
		this.contractPeriodReadPlatformService = contractPeriodReadPlatformService;
	}
	@Autowired
	public void setHardwareAssociationReadplatformService(
			HardwareAssociationReadplatformService hardwareAssociationReadplatformService) {
		this.hardwareAssociationReadplatformService = hardwareAssociationReadplatformService;
	}
	@Autowired
	public void setCommandsSourceWritePlatformService(
			PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}
	@Autowired
	public void setRecurringBillingRepositoryWrapper(RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper) {
		this.recurringBillingRepositoryWrapper = recurringBillingRepositoryWrapper;
	}
	@Autowired
	public void setEventActionReadPlatformService(EventActionReadPlatformService eventActionReadPlatformService) {
		this.eventActionReadPlatformService = eventActionReadPlatformService;
	}
	@Autowired
	public void setConfigurationRepository(ConfigurationRepository configurationRepository) {
		this.configurationRepository = configurationRepository;
	}
	@Autowired
	public void setServiceTransferReadPlatformService(
			ServiceTransferReadPlatformService serviceTransferReadPlatformService) {
		this.serviceTransferReadPlatformService = serviceTransferReadPlatformService;
	}
	@Autowired
	public void setInvoiceOneTimeSale(InvoiceOneTimeSale invoiceOneTimeSale) {
		this.invoiceOneTimeSale = invoiceOneTimeSale;
	}
	@Autowired
	public void setUserChatRepository(UserChatRepository userChatRepository) {
		this.userChatRepository = userChatRepository;
	}
	@Autowired
	public void setContractRepository(ContractRepository contractRepository) {
		this.contractRepository = contractRepository;
	}
	@Autowired
	public void setDepositAndRefundRepository(DepositAndRefundRepository depositAndRefundRepository) {
		this.depositAndRefundRepository = depositAndRefundRepository;
	}
	@Autowired
	public void setBillingOrderWritePlatformService(BillingOrderWritePlatformService billingOrderWritePlatformService) {
		this.billingOrderWritePlatformService = billingOrderWritePlatformService;
	}
	@Autowired
	public void setOrderAssembler(OrderAssembler orderAssembler) {
		this.orderAssembler = orderAssembler;
	}
	@Autowired
	public void setSelfCareRepository(SelfCareRepository selfCareRepository) {
		this.selfCareRepository = selfCareRepository;
	}
	@Autowired
	public void setPlanRepository(PlanRepository planRepository) {
		this.planRepository = planRepository;
	}
	@Autowired
	public void setDepartmentReadPlatformService(DepartmentReadPlatformService departmentReadPlatformService) {
		this.departmentReadPlatformService = departmentReadPlatformService;
	}
	@Autowired
	public void setEmployeeReadPlatformService(EmployeeReadPlatformService employeeReadPlatformService) {
		this.employeeReadPlatformService = employeeReadPlatformService;
	}
	@Autowired
	public void setWifiMasterReadPlatformService(WifiMasterReadPlatformService wifiMasterReadPlatformService) {
		this.wifiMasterReadPlatformService = wifiMasterReadPlatformService;
	}
	@Autowired
	public void setAddressRepository(AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
	}
	@Autowired
	public void setAppUserRepository(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}
    @Autowired
	public void setTicketMasterApiResource(TicketMasterApiResource ticketMasterApiResource) {
		this.ticketMasterApiResource = ticketMasterApiResource;
	}
    @Autowired
	public void setCodeReadPlatformService(MCodeReadPlatformService codeReadPlatformService) {
		this.codeReadPlatformService = codeReadPlatformService;
	}
    @Autowired
	public void setItemDetailsAllocationRepository(ItemDetailsAllocationRepository itemDetailsAllocationRepository) {
		this.itemDetailsAllocationRepository = itemDetailsAllocationRepository;
	}
    @Autowired
	public void setTicketAssignRuleReadPlatformService(TicketAssignRuleReadPlatformService ticketAssignRuleReadPlatformService) {
		this.ticketAssignRuleReadPlatformService = ticketAssignRuleReadPlatformService;
	}
    
    @Autowired
	public void setRoutingDataSource(RoutingDataSource dataSource) {
		this.dataSource = dataSource;
	}
    @Autowired
	public void setClientProspectJpaRepository(ClientProspectJpaRepository clientProspectJpaRepository) {
		this.clientProspectJpaRepository = clientProspectJpaRepository;
	}
    
	@Override
	public String AddNewActions(List<ActionDetaislData> actionDetaislDatas,final Long clientId,final String resourceId,String ticketURL) {
    
  try{
    	
	if(actionDetaislDatas!=null){
	   EventAction eventAction=null;
	   String headerMessage = null, bodyMessage = null, footerMessage = null, messageFrom = "";
	   BillingMessage billingMessage = null;
	   OrderNotificationData orderData = null;
	   BillingMessageTemplate template = null;
	   WifiData wifidata=null;
			
	   	for(ActionDetaislData detailsData:actionDetaislDatas){
	   		
		      EventActionProcedureData actionProcedureData=this.actionDetailsReadPlatformService.checkCustomeValidationForEvents(clientId, detailsData.getEventName(),detailsData.getActionName(),resourceId);
			  JSONObject jsonObject=new JSONObject();
			  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
			  if(actionProcedureData.isCheck()){
				   
				    List<SubscriptionData> subscriptionDatas=this.contractPeriodReadPlatformService.retrieveSubscriptionDatabyContractType("Month(s)",1);
				   
				    switch(detailsData.getActionName()){
				  
				    case EventActionConstants.ACTION_SEND_EMAIL :
				    	   
				          TicketMasterData data = this.ticketMasterReadPlatformService.retrieveTicket(clientId,new Long(resourceId));
				          TicketMaster ticketMaster=this.repository.findOne(new Long(resourceId));
				          AppUserData user = this.readPlatformService.retrieveUser(new Long(data.getUserId()));
				          Client client = this.clientRepository.findOne(clientId);
				          Address clientAddress = this.addressRepository.findOneByClientId(clientId);
				          AppUser assignedUser = this.appUserRepository.findOne(Long.valueOf(data.getUserId()));
				          final String officeMail = client.getOffice().getOfficeAddress().getEmail();
				          if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CREATE_TICKET)){
				        	  BillingMessageTemplate billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_TICKET_TEMPLATE);
				        	  if(billingMessageTemplate !=null){
				        		  if(!user.getEmail().isEmpty()){
				        		  //Send Mail To Customer	  
				        		  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", client.getFirstname());
				        		  String ticketId = String.valueOf(ticketMaster.getId());
				        		  
				        		  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketId>", ticketId);
				        		  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
				        		  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
				        		  bodyMessage = bodyMessage.replaceAll("<customer>", client.getFirstname());
				        		  bodyMessage = bodyMessage.replaceAll("<ticketname>", user.username());
				        		  bodyMessage = bodyMessage.replaceAll("<ticketdesc>", ticketMaster.getDescription());
						    	
				        		  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, client.getEmail(), 
						    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								 
				        		  this.messageDataRepository.save(billingMessage);
				        		  
				        		 //Send Mail To Assigned User	  
				        		  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", user.username());
				        		  
				        		  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketId>", ticketId);
				        		  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
				        		  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
				        		  bodyMessage = bodyMessage.replaceAll("<customer>", client.getFirstname());
				        		  bodyMessage = bodyMessage.replaceAll("<ticketname>", user.username());
				        		  bodyMessage = bodyMessage.replaceAll("<ticketdesc>", ticketMaster.getDescription());
						    	
				        		  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, user.getEmail(), 
						    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								 
				        		  this.messageDataRepository.save(billingMessage);
						    	}				        	  
				        	  }
				        	  
				          }else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_EDIT_TICKET)){
				        	  BillingMessageTemplate billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EDIT_TICKET);
				        	  if(billingMessageTemplate !=null){
				        		  if(!user.getEmail().isEmpty()){
				        			/*if(ticketMaster.getStatus().equalsIgnoreCase("Appointment")){
				        				  //Send Mail To Customer
				        	    		  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", client.getFirstname());
					        			  String ticketId = String.valueOf(ticketMaster.getId());
					        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, client.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);
								    	//Send Mail To Assigned User
								    	headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", user.username());
					        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, user.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);
				        	    	}*/if(ticketMaster.getStatus().equalsIgnoreCase("FollowUp")){
				        	    		  //Send Mail To Customer
				        	    		 /* headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", client.getFirstname());*/
					        			  String ticketId = String.valueOf(ticketMaster.getId());
					        			  /*bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, client.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);*/
								    	
								    	  //Send Mail To Assigned User
								    	  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", user.username());
					        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, user.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);
				        	    	}else if(ticketMaster.getStatus().equalsIgnoreCase("Working")){
				        	    		  //Send Mail To Customer
				        	    		//  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", client.getFirstname());
					        			  String ticketId = String.valueOf(ticketMaster.getId());
					        			  /*bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, client.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);*/
								    	
								    	//Send Mail To Assigned User
				        	    		  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", user.username());
					        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, user.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);
								    	
				        	    	} else if(ticketMaster.getStatus().equalsIgnoreCase("Open")){
				        	    		  //Send Mail To Customer
				        	    		 /* headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", client.getFirstname());*/
					        			  String ticketId = String.valueOf(ticketMaster.getId());
					        			  /*bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, client.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);*/
								    	
								    	  //Send Mail To Assigned User
								    	  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", user.username());
					        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, user.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);
				        	    	} else if(ticketMaster.getStatus().equalsIgnoreCase("Problems")){
				        	    		  //Send Mail To Customer
				        	    		 /* headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", client.getFirstname());*/
					        			  String ticketId = String.valueOf(ticketMaster.getId());
					        			  /*bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, client.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);*/
								    	
								    	  //Send Mail To Assigned User
								    	  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", user.username());
					        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
					        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
					        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
								    	
					        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, user.getEmail(), 
								    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
								    		
								    	this.messageDataRepository.save(billingMessage);
				        	    	}
				        		  }
				        	  }
			        	
			        	}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CLOSE_TICKET)){
			        		
			        		 BillingMessageTemplate billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_ClOSE_TICKET);
				        	 if(billingMessageTemplate !=null){
				        		 if(!user.getEmail().isEmpty()){
				        			 //Send Mail To Customer
				        			 headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", client.getFirstname());
				        			  String ticketId = String.valueOf(ticketMaster.getId());
				        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
				        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
				        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
				        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
				        			  bodyMessage = bodyMessage.replaceAll("<resolution>", ticketMaster.getResolutionDescription());
							    	
				        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, client.getEmail(), 
							    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
							    		
							    	this.messageDataRepository.save(billingMessage);
							    	
							    	//Send Mail TO Assigned User
							    	headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", user.username());
				        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<ticketassign>", user.username());
				        			  bodyMessage = bodyMessage.replaceAll("<ticketId>", ticketId);
				        			  bodyMessage = bodyMessage.replaceAll("<ticketstatus>", ticketMaster.getStatus());
				        			  bodyMessage = bodyMessage.replaceAll("<ticketdate>", dateFormat.format(ticketMaster.getCreatedDate()));
				        			  bodyMessage = bodyMessage.replaceAll("<resolution>", ticketMaster.getResolutionDescription());
							    	
				        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, user.getEmail(), 
							    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
							    		
							    	this.messageDataRepository.save(billingMessage);
			        			 
				        	       }
				        		 }
				        	 }else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_APPOINTMENT_TICKET)){
				        		 //For Time Format like From 05:45:00 to 05:45 AM
				        		 DateFormat inputFormat = new SimpleDateFormat( "HH:mm:ss" );
				        		 DateFormat outputFormat = new SimpleDateFormat( "hh:mm aa" );
				        		 Date appointmentTime = inputFormat.parse(ticketMaster.getAppointmentTime().toString());
				        		 //For DateFormat like dd/mm/yy
				        		 DateFormat dateFormatt = new SimpleDateFormat("dd/MM/yy");
				        		 BillingMessageTemplate billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_APPOINTMENT_TICKET);
					        	  if(billingMessageTemplate !=null){
					        		  if(!user.getEmail().isEmpty()){
					        				  //Send Mail To Customer
					        	    		  headerMessage = billingMessageTemplate.getHeader().replaceAll("<customer>", client.getFirstname());
						        			  bodyMessage = billingMessageTemplate.getBody().replaceAll("<technicianName>", assignedUser.getFirstname());
						        			  bodyMessage = bodyMessage.replaceAll("<appointmentDate>", dateFormatt.format(ticketMaster.getAppointmentDate()));
						        			  bodyMessage = bodyMessage.replaceAll("<appointmentTime>", outputFormat.format(appointmentTime).toLowerCase());
						        			  bodyMessage = bodyMessage.replaceAll("<Address>", clientAddress.getAddressNo());
						        			  bodyMessage = bodyMessage.replaceAll("<Street>", clientAddress.getStreet());
						        			  bodyMessage = bodyMessage.replaceAll("<Country>", clientAddress.getCountry());
									    	
						        			  billingMessage =new BillingMessage(headerMessage, bodyMessage, billingMessageTemplate.getFooter(), officeMail, client.getEmail(), 
									    			 billingMessageTemplate.getSubject(), "N", billingMessageTemplate, billingMessageTemplate.getMessageType(), null);
									    		
									    	this.messageDataRepository.save(billingMessage);
									    	}
					        		  }
				        	 }else{
				        		  throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_TICKET_TEMPLATE);
				        	 }
				       break;
				       
				    case EventActionConstants.ACTION_ACTIVE : 
				    	
				         AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(actionProcedureData.getOrderId());
				          		if(associationData == null){
				          			throw new HardwareDetailsNotFoundException(actionProcedureData.getOrderId().toString());
				          		}
				          		jsonObject.put("renewalPeriod",subscriptionDatas.get(0).getId());	
				          		jsonObject.put("description","Order Renewal By Scheduler");
				          		eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",EventActionConstants.ACTION_RENEWAL.toString(),"/orders/renewal", 
			        			 Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
				          		this.eventActionRepository.save(eventAction);
				         break; 		

				    case EventActionConstants.ACTION_NEW :

				    	jsonObject.put("billAlign","false");
				    	jsonObject.put("contractPeriod",subscriptionDatas.get(0).getId());
				    	jsonObject.put("dateFormat","dd MMMM yyyy");
                      jsonObject.put("locale","en");
                      jsonObject.put("paytermCode","Monthly");
                      jsonObject.put("planCode",actionProcedureData.getPlanId());
                      jsonObject.put("isNewplan","true");
                      jsonObject.put("start_date",dateFormat.format(DateUtils.getDateOfTenant()));
                      eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",actionProcedureData.getActionName(),"/orders/"+clientId, 
                      		Long.parseLong(resourceId), jsonObject.toString(),null,clientId);
			        	this.eventActionRepository.save(eventAction);
			        	   
				    	break;
				    	
				    case EventActionConstants.ACTION_DISCONNECT :

			        	   eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",EventActionConstants.ACTION_ACTIVE.toString(),"/orders/reconnect/"+clientId, 
			        	   Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
			        	   this.eventActionRepository.save(eventAction);

			        	   break; 
					default:
						break;
				    }
			  	}




					switch (detailsData.getActionName()) {

					case EventActionConstants.ACTION_PROVISION_IT:

						Client client = this.clientRepository.findOne(clientId);
						EventOrder eventOrder = this.eventOrderRepository.findOne(Long.valueOf(resourceId));
						EventMaster eventMaster = this.eventMasterRepository.findOne(eventOrder.getEventId());
						String response = AddExternalBeesmartMethod.addVodPackage(client.getOffice().getExternalId().toString(), client.getAccountNo(),eventMaster.getEventName());

						ProcessRequest processRequest = new ProcessRequest(Long.valueOf(0), eventOrder.getClientId(), eventOrder.getId(), ProvisioningApiConstants.PROV_BEENIUS,
								ProvisioningApiConstants.REQUEST_ACTIVATION_VOD, 'Y', 'Y');
						List<EventOrderdetials> eventDetails = eventOrder.getEventOrderdetials();
						jsonObject.put("officeUid", client.getOffice().getExternalId());
						jsonObject.put("subscriberUid", client.getAccountNo());
						jsonObject.put("vodUid", eventMaster.getEventName());

						for (EventOrderdetials details : eventDetails) {
							ProcessRequestDetails processRequestDetails = new ProcessRequestDetails(details.getId(),details.getEventDetails().getId(), jsonObject.toString(), response, null,
									eventMaster.getEventStartDate(), eventMaster.getEventEndDate(),DateUtils.getDateOfTenant(), DateUtils.getDateOfTenant(), 'N',ProvisioningApiConstants.REQUEST_ACTIVATION_VOD, null);
							processRequest.add(processRequestDetails);
						}
						this.processRequestRepository.save(processRequest);

						break;
						
					case EventActionConstants.ACTION_PROSPECT_QUOTATION :
				    	
				    	ClientProspect clientProspect = this.clientProspectJpaRepository.findOne(new Long(resourceId));

						final String fileLocation = FileUtils.OBS_BASE_DIR;
						/** Recursively create the directory if it does not exist **/
						if (!new File(fileLocation).isDirectory()) {
							new File(fileLocation).mkdirs();
						}
						final String statementDetailsLocation = fileLocation+ File.separator +"ProspectQuotationPdfFiles";
						if (!new File(statementDetailsLocation).isDirectory()) {
							new File(statementDetailsLocation).mkdirs();
						}
						final String printStatementLocation = statementDetailsLocation+ File.separator +resourceId+"_"+DateUtils.getLocalDateOfTenant()+".pdf";
						final String jpath = fileLocation + File.separator + "jasper";
						final ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
						final String jfilepath = jpath + File.separator + "ProspectQuotation_"+ tenant.getTenantIdentifier() + ".jasper";
						File destinationFile = new File(jfilepath);
						if (!destinationFile.exists()) {
							File sourceFile = new File(this.getClass().getClassLoader().getResource("Files/ProspectQuotation.jasper").getFile());
							FileUtils.copyFileUsingApacheCommonsIO(sourceFile,destinationFile);
						}
						Connection connection = this.dataSource.getConnection();
						Map<String, Object> parameters = new HashMap<String, Object>();
						final Long id = Long.valueOf(resourceId);
						parameters.put("param1", id);
						//parameters.put("SUBREPORT_DIR", jpath + "" + File.separator);
						//parameters.put(JRParameter.REPORT_LOCALE, getLocale(tenant)); 
						final JasperPrint jasperPrint = JasperFillManager.fillReport(jfilepath, parameters, connection);
						JasperExportManager.exportReportToPdfFile(jasperPrint,printStatementLocation);
						clientProspect.setFileName(printStatementLocation);
						this.clientProspectJpaRepository.save(clientProspect);
						logger.info("Prospect Quotation generated successfully...");
						
						
						
						//logger.info("Filling report successfully...");
						
						/*final Configuration statementNotify=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_STATEMENT_NOTIFY);
						if(statementNotify != null && statementNotify.isEnabled()){
							sendPdfToEmail(billMaster.getFileName(),billMaster.getClientId(),BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATEMENT);
							*/
				    	   break;
				    	   
				    case EventActionConstants.ACTION_CLIENT_QUOTATION :
				    	
				    	Client clientData = this.clientRepository.findOne(clientId);

						final String pdfFileLocation = FileUtils.OBS_BASE_DIR;
						/** Recursively create the directory if it does not exist **/
						if (!new File(pdfFileLocation).isDirectory()) {
							new File(pdfFileLocation).mkdirs();
						}
						final String statementPdfDetailsLocation = pdfFileLocation+ File.separator +"ClientQuotationPdfFiles";
						if (!new File(statementPdfDetailsLocation).isDirectory()) {
							new File(statementPdfDetailsLocation).mkdirs();
						}
						final String printPdfStatementLocation = statementPdfDetailsLocation+ File.separator +resourceId+"_"+DateUtils.getLocalDateOfTenant()+".pdf";
						final String jpathpdf = pdfFileLocation + File.separator + "jasper";
						final ObsPlatformTenant tenantpdf = ThreadLocalContextUtil.getTenant();
						final String jfilepathpdf = jpathpdf + File.separator + "ClientQuotation_"+ tenantpdf.getTenantIdentifier() + ".jasper";
						File destinationFilePdf = new File(jfilepathpdf);
						if (!destinationFilePdf.exists()) {
							File sourceFile = new File(this.getClass().getClassLoader().getResource("Files/ClientQuotation.jasper").getFile());
							FileUtils.copyFileUsingApacheCommonsIO(sourceFile,destinationFilePdf);
						}
						Connection connectionPdf = this.dataSource.getConnection();
						Map<String, Object> pdfParameters = new HashMap<String, Object>();
						final Double cId = Double.valueOf(resourceId);
						pdfParameters.put("id", cId);
						//parameters.put("SUBREPORT_DIR", jpath + "" + File.separator);
						//parameters.put(JRParameter.REPORT_LOCALE, getLocale(tenant)); 
						final JasperPrint jasperPrintPdf = JasperFillManager.fillReport(jfilepathpdf, pdfParameters, connectionPdf);
						JasperExportManager.exportReportToPdfFile(jasperPrintPdf,printPdfStatementLocation);
						clientData.setFileName(printPdfStatementLocation);
						this.clientRepository.save(clientData);
						logger.info("Client Quotation generated successfully...");
						
						
						
						//logger.info("Filling report successfully...");
						
						/*final Configuration statementNotify=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_STATEMENT_NOTIFY);
						if(statementNotify != null && statementNotify.isEnabled()){
							sendPdfToEmail(billMaster.getFileName(),billMaster.getClientId(),BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATEMENT);
							*/
				    	   break;	

					case EventActionConstants.ACTION_SEND_PROVISION:

						eventAction = new EventAction(DateUtils.getDateOfTenant(), "CLOSE", "Client",EventActionConstants.ACTION_SEND_PROVISION.toString(), "/processrequest/" + clientId,
								Long.parseLong(resourceId), jsonObject.toString(), clientId, clientId);
						this.eventActionRepository.save(eventAction);

						break;

					case EventActionConstants.ACTION_ACTIVE_LIVE_EVENT:
						
						eventMaster = this.eventMasterRepository.findOne(Long.valueOf(resourceId));
						
						eventAction = new EventAction(eventMaster.getEventStartDate(), "Create", "Live Event",EventActionConstants.ACTION_ACTIVE_LIVE_EVENT.toString(), "/eventmaster",
								Long.parseLong(resourceId), jsonObject.toString(), Long.valueOf(0), Long.valueOf(0));
						this.eventActionRepository.saveAndFlush(eventAction);

						eventAction = new EventAction(eventMaster.getEventEndDate(), "Disconnect", "Live Event",EventActionConstants.ACTION_INACTIVE_LIVE_EVENT.toString(), "/eventmaster",
								Long.parseLong(resourceId), jsonObject.toString(), Long.valueOf(0), Long.valueOf(0));
						this.eventActionRepository.saveAndFlush(eventAction);

						break;

					case EventActionConstants.ACTION_INVOICE:

						Order order = this.orderRepository.findOne(new Long(resourceId));
						jsonObject.put("dateFormat", "dd MMMM yyyy");
						jsonObject.put("locale", "en");
						jsonObject.put("systemDate", dateFormat.format(order.getStartDate()));
						if (detailsData.IsSynchronous().equalsIgnoreCase("N")) {
							eventAction = new EventAction(DateUtils.getDateOfTenant(), "CREATE",EventActionConstants.EVENT_ACTIVE_ORDER.toString(),EventActionConstants.ACTION_INVOICE.toString(), 
									"/billingorder/" + clientId,Long.parseLong(resourceId), jsonObject.toString(), Long.parseLong(resourceId),clientId);
							this.eventActionRepository.save(eventAction);

						} else {
							this.invoiceClient.singleOrderInvoice(order.getId(), order.getClientId(),new LocalDate(order.getStartDate()));
						}
						
						break;

					case EventActionConstants.ACTION_SEND_PAYMENT:

						eventAction = new EventAction(DateUtils.getDateOfTenant(), "SEND", "Payment Receipt",EventActionConstants.ACTION_SEND_PAYMENT.toString(),
								"/billmaster/payment/" + clientId + "/" + Long.parseLong(resourceId),Long.parseLong(resourceId), jsonObject.toString(), Long.parseLong(resourceId),clientId);
						this.eventActionRepository.save(eventAction);
						
						break;

					case EventActionConstants.ACTION_TOPUP_INVOICE_MAIL:
						
						eventAction = new EventAction(DateUtils.getDateOfTenant(), "SEND",EventActionConstants.EVENT_TOPUP_INVOICE_MAIL.toString(),
								EventActionConstants.ACTION_TOPUP_INVOICE_MAIL.toString(),"/billmaster/invoice/" + clientId + "/" + resourceId, Long.parseLong(resourceId),
								jsonObject.toString(), Long.parseLong(resourceId), clientId);
						this.eventActionRepository.save(eventAction);
						
						break;

					case EventActionConstants.ACTION_RECURRING_DISCONNECT:

						JsonObject apiRequestBodyAsJson = new JsonObject();
						apiRequestBodyAsJson.addProperty("orderId", Long.parseLong(resourceId));
						apiRequestBodyAsJson.addProperty("recurringStatus", "SUSPEND");

						final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(apiRequestBodyAsJson.toString()).build();
						final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

						Map<String, Object> resultMap = result.getChanges();

						JsonObject resultJson = new JsonObject();
						resultJson.addProperty("result", resultMap.get("result").toString());
						resultJson.addProperty("acknoledgement", resultMap.get("acknoledgement").toString());
						resultJson.addProperty("error", resultMap.get("error").toString());

						EventAction event = new EventAction(DateUtils.getDateOfTenant(), "Recurring Disconnect","Recurring Disconnect", EventActionConstants.ACTION_RECURRING_DISCONNECT.toString(),
								"/eventmaster", Long.parseLong(resourceId), resultJson.toString(), Long.valueOf(0),	Long.valueOf(0));
						event.updateStatus('Y');
						this.eventActionRepository.saveAndFlush(event);

						break;

					case EventActionConstants.ACTION_RECURRING_RECONNECTION:

						JsonObject JsonString = new JsonObject();
						JsonString.addProperty("orderId", Long.parseLong(resourceId));
						JsonString.addProperty("recurringStatus", "REACTIVATE");

						final CommandWrapper commandRequestForReconn = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(JsonString.toString()).build();
						final CommandProcessingResult commandResult = this.commandsSourceWritePlatformService.logCommandSource(commandRequestForReconn);

						Map<String, Object> resultMapObj = commandResult.getChanges();

						JsonObject resultJsonObj = new JsonObject();
						resultJsonObj.addProperty("result", resultMapObj.get("result").toString());
						resultJsonObj.addProperty("acknoledgement", resultMapObj.get("acknoledgement").toString());
						resultJsonObj.addProperty("error", resultMapObj.get("error").toString());

						EventAction eventActionObj = new EventAction(DateUtils.getDateOfTenant(),"Recurring Reconnection", "Recurring Reconnection",
								EventActionConstants.ACTION_RECURRING_RECONNECTION.toString(), "/eventmaster",Long.parseLong(resourceId), resultJsonObj.toString(),Long.valueOf(0), Long.valueOf(0));
						eventActionObj.updateStatus('Y');
						this.eventActionRepository.saveAndFlush(eventActionObj);

						break;

					case EventActionConstants.ACTION_RECURRING_TERMINATION:

						Long orderId = Long.parseLong(resourceId);
						RecurringBilling billing = this.recurringBillingRepositoryWrapper.findOneByOrderId(orderId);

						if (billing.getDeleted() == 'N') {
							
							JsonObject terminationObj = new JsonObject();
							terminationObj.addProperty("orderId", orderId);
							terminationObj.addProperty("recurringStatus", "CANCEL");

							final CommandWrapper terminateCommandRequest = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(terminationObj.toString()).build();
							final CommandProcessingResult terminateResult = this.commandsSourceWritePlatformService.logCommandSource(terminateCommandRequest);

							Map<String, Object> resultMapForTerminate = terminateResult.getChanges();

							JsonObject resultJsonObject = new JsonObject();
							resultJsonObject.addProperty("result", resultMapForTerminate.get("result").toString());
							resultJsonObject.addProperty("acknoledgement",resultMapForTerminate.get("acknoledgement").toString());
							resultJsonObject.addProperty("error", resultMapForTerminate.get("error").toString());

							EventAction eventActionTermination = new EventAction(DateUtils.getDateOfTenant(),"Cancel Recurring", "Cancel Recurring Profile",
									EventActionConstants.ACTION_RECURRING_TERMINATION.toString(), "/eventmaster",Long.parseLong(resourceId), resultJsonObject.toString(),Long.valueOf(0),Long.valueOf(0));
							eventActionTermination.updateStatus('Y');
							this.eventActionRepository.saveAndFlush(eventActionTermination);
						}

						break;

					case EventActionConstants.ACTION_NOTIFY_TECHNICALTEAM:

						String userName = "billing";

						Configuration configValue = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_APPUSER);
						String deptName = configValue.getValue();
						String departname = deptName.substring(1, deptName.length() - 1);
						final DepartmentData departmentDatas = this.departmentReadPlatformService.retrieveDepartmentId(departname);
						Long departmentId = departmentDatas.getId();
						final List<EmployeeData> employeedata = this.employeeReadPlatformService.retrieveAllEmployeeDataByDeptId(departmentId);
						String assigned = null;
						for (EmployeeData employeedatas : employeedata) {
							if (employeedatas.isIsprimary()) {
								assigned = employeedatas.getName();
							}
						}

						if (null != configValue && configValue.isEnabled() && configValue.getValue() != null && !configValue.getValue().isEmpty()) {
							userName = assigned;
						}

						template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_TECHNICAL_TEAM);
						bodyMessage = template.getBody().replaceAll("<ActionType>", ticketURL);
						bodyMessage = bodyMessage.replaceAll("<clientId>", clientId.toString());
						bodyMessage = bodyMessage.replaceAll("<id>", resourceId == null ? "" : resourceId);

						final LocalDate messageDate = DateUtils.getLocalDateOfTenant();
						UserChat userChat = new UserChat(userName, messageDate.toDate(), bodyMessage,ConfigurationConstants.OBSUSER, ticketURL);
						this.userChatRepository.save(userChat);

						break;

					case EventActionConstants.ACTION_NOTIFY_ACTIVATION:

						orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,new Long(resourceId));
						
						System.out.println(" plan code is "+orderData.getPlanCode());
						
						if(orderData.getPlanCode().equalsIgnoreCase("Bus_Supp") || 
								orderData.getPlanCode().equalsIgnoreCase("BusT_Ext_0") || 
								orderData.getPlanCode().equalsIgnoreCase("BusT_Ext_1") ||
								orderData.getPlanCode().equalsIgnoreCase("BusT_Ext_2") || 
								orderData.getPlanCode().equalsIgnoreCase("BusT_Ext_3") || 
								orderData.getPlanCode().equalsIgnoreCase("BusT_Ext_4") ||
								orderData.getPlanCode().equalsIgnoreCase("BusT_Ext_5") ||
								orderData.getPlanCode().equalsIgnoreCase("BusT_Ext_6") ||
								orderData.getPlanCode().equalsIgnoreCase("Bus_TV") ||
								orderData.getPlanCode().equalsIgnoreCase("Bus_Tel") ){
							System.out.println(" skip order email"+orderData.getPlanCode());
							
						}else{

						template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION);

						headerMessage = template.getHeader().replaceAll("<CustomerName>",orderData.getFirstName() + " " + orderData.getLastName());
						bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
						bodyMessage = bodyMessage.replaceAll("<Activation Date>",dateFormat.format(orderData.getActivationDate().toDate()));
						
						
						wifidata = this.wifiMasterReadPlatformService.getByOrderId(clientId, new Long(resourceId));
						if(wifidata!=null){
							if((orderData.getPlanCode().equalsIgnoreCase("u-mee home")) || (orderData.getPlanCode().equalsIgnoreCase("u-mee plus")) || 
									(orderData.getPlanCode().equalsIgnoreCase("u-mee fam2")) || (orderData.getPlanCode().equalsIgnoreCase("u-mee fam1")) ){
								bodyMessage=bodyMessage.concat("Following are the Credentials"+"<br/><br/>");
								bodyMessage=bodyMessage.concat("SSID :"+wifidata.getSsid()+"<br/>");
								bodyMessage=bodyMessage.concat("WIFI PASSWORD :"+wifidata.getWifiPassword()+"<br/><br/>");
							}
					   }
						footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
						footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
						footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
						messageFrom = orderData.getOfficeEmail() == null ? orderData.getEmailId(): orderData.getOfficeEmail();

						billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, messageFrom,orderData.getEmailId(), template.getSubject(),
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

						this.messageDataRepository.save(billingMessage);
						}

						break;

					case EventActionConstants.ACTION_NOTIFY_DISCONNECTION:

						orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,new Long(resourceId));

						template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION);

						headerMessage = template.getHeader().replaceAll("<CustomerName>",orderData.getFirstName() + " " + orderData.getLastName());
						bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
						bodyMessage = bodyMessage.replaceAll("<Disconnection Date>",dateFormat.format(orderData.getEndDate().toDate()));

						footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
						footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
						footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
						messageFrom = orderData.getOfficeEmail() == null ? orderData.getEmailId(): orderData.getOfficeEmail();
						billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, messageFrom,orderData.getEmailId(), template.getSubject(),
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

						this.messageDataRepository.save(billingMessage);

						break;

					case EventActionConstants.ACTION_NOTIFY_RECONNECTION:

						orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,new Long(resourceId));

						template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION);

						headerMessage = template.getHeader().replaceAll("<CustomerName>",orderData.getFirstName() + " " + orderData.getLastName());
						bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
						bodyMessage = bodyMessage.replaceAll("<Reconnection Date>",dateFormat.format(orderData.getStartDate().toDate()));

						footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
						footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
						footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
						messageFrom = orderData.getOfficeEmail() == null ? orderData.getEmailId(): orderData.getOfficeEmail();

						billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, messageFrom,orderData.getEmailId(), template.getSubject(),
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

						this.messageDataRepository.save(billingMessage);

						break;

					case EventActionConstants.ACTION_NOTIFY_PAYMENT:

						orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);

						template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT);

						headerMessage = template.getHeader().replaceAll("<CustomerName>",orderData.getFirstName() + " " + orderData.getLastName());
						bodyMessage = template.getBody().replaceAll("<Amount>", resourceId);
						bodyMessage = bodyMessage.replaceAll("<Payment Date>",dateFormat.format(DateUtils.getLocalDateOfTenant().toDate()));

						footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
						footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
						footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
						messageFrom = orderData.getOfficeEmail() == null ? orderData.getEmailId(): orderData.getOfficeEmail();

						billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, messageFrom,orderData.getEmailId(), template.getSubject(),
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

						this.messageDataRepository.save(billingMessage);

						break;

					case EventActionConstants.ACTION_NOTIFY_CHANGEPLAN:

						orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,new Long(resourceId));

						template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_CHANGEPLAN);

						headerMessage = template.getHeader().replaceAll("<CustomerName>", orderData.getFirstName());
						bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
						bodyMessage = bodyMessage.replaceAll("<Activation Date>",dateFormat.format(orderData.getActivationDate().toDate()));

						footerMessage = template.getFooter();
						footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
						footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
						footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
						messageFrom = orderData.getOfficeEmail() == null ? orderData.getEmailId(): orderData.getOfficeEmail();

						billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, messageFrom,orderData.getEmailId(), template.getSubject(),
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

						this.messageDataRepository.save(billingMessage);

						break;

					case EventActionConstants.ACTION_CHNAGE_PLAN:

						orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,new Long(resourceId));

						template = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_CHANGE_PLAN);

						headerMessage = template.getHeader().replaceAll("<CustomerName>", orderData.getFirstName());
						bodyMessage = template.getBody().replaceAll("<Plan_Name_PARAM>", orderData.getPlanName());
						bodyMessage = bodyMessage.replaceAll("<DATE_CHANGE_PLAN>", dateFormat.format(orderData.getStartDate().toDate()));

						footerMessage = template.getFooter();
						messageFrom = orderData.getOfficeEmail() == null ? orderData.getEmailId(): orderData.getOfficeEmail();

						billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, messageFrom,orderData.getEmailId(), template.getSubject(),
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

						this.messageDataRepository.save(billingMessage);

						break;

					case EventActionConstants.ACTION_NOTIFY_ORDER_TERMINATE:

						orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,new Long(resourceId));

						template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ORDERTERMINATION);

						headerMessage = template.getHeader().replaceAll("<CustomerName>",orderData.getFirstName() + " " + orderData.getLastName());
						bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
						bodyMessage = bodyMessage.replaceAll("<Disconnection Date>",dateFormat.format(DateUtils.getLocalDateOfTenant().toDate()));

						footerMessage = template.getFooter().replaceAll("<Reseller Name>", orderData.getOfficeName());
						footerMessage = footerMessage.replaceAll("<Contact Name>", orderData.getOfficeEmail());
						footerMessage = footerMessage.replaceAll("<Number>", orderData.getOfficePhoneNo());
						messageFrom = orderData.getOfficeEmail() == null ? orderData.getEmailId(): orderData.getOfficeEmail();

						billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, messageFrom,orderData.getEmailId(), template.getSubject(),
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

						this.messageDataRepository.save(billingMessage);

						break;

					case EventActionConstants.RECONNECTION_FEE:

						List<FeeMasterData> feeDetails = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId, "Reconnection", null, null);
						// call one time invoice
						final String chargeCode = feeDetails.get(0).getChargeCode();
						final BigDecimal shiftChargeAmount = feeDetails.get(0).getDefaultFeeAmount();
						final Order orderDetail = this.orderRepository.findOne(Long.valueOf(resourceId));
						if (chargeCode != null && !StringUtils.isEmpty(chargeCode)) {

							this.invoiceOneTimeSale.calculateAdditionalFeeCharges(chargeCode, orderDetail.getId(), -1L,clientId, shiftChargeAmount, BillingTransactionConstants.RECONNECTION_FEE);
						}
						break;

					case EventActionConstants.ACTION_SUBSCRIPTION_DEPOSIT:

						final Order orderDatas = this.orderRepository.findOne(Long.valueOf(resourceId));
						if (orderDatas == null) {
							throw new NoOrdersFoundException(resourceId);
						}
						final Contract contractDatas = this.contractRepository.findOne(orderDatas.getContarctPeriod());
						if (contractDatas == null) {
							throw new CodeNotFoundException(orderDatas.getContarctPeriod().toString());
						}
						List<FeeMasterData> feeData = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId, "Deposit", orderDatas.getPlanId(), contractDatas.getSubscriptionPeriod());

						DepositAndRefund depositfund = new DepositAndRefund(clientId, feeData.get(0).getId(),feeData.get(0).getDefaultFeeAmount(), DateUtils.getDateOfTenant(),feeData.get(0).getTransactionType());
						this.depositAndRefundRepository.save(depositfund);

						// Update Client Balance
						this.billingOrderWritePlatformService.updateClientBalance(feeData.get(0).getDefaultFeeAmount(),clientId, false);

						break;

					case EventActionConstants.ACTION_ORDER_REACTIVATE:

						eventAction = new EventAction(DateUtils.getDateOfTenant(), "REACTIVE",EventActionConstants.EVENT_ORDER_REACTIVATE.toString(),EventActionConstants.ACTION_ORDER_REACTIVATE.toString(),
								"/orders/reactive/" + Long.parseLong(resourceId), Long.parseLong(resourceId),jsonObject.toString(), Long.parseLong(resourceId), clientId);
						this.eventActionRepository.save(eventAction);
						
						break;

					case EventActionConstants.ACTION_ORDER_REACTIVATION_FEE:

						List<FeeMasterData> feeDetailes = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId, "Reactivation", null, null);
						// call one time invoice
						final String chargeeCode = feeDetailes.get(0).getChargeCode();
						final BigDecimal reactivationChargeAmount = feeDetailes.get(0).getDefaultFeeAmount();
						if (chargeeCode != null && !StringUtils.isEmpty(chargeeCode)) {
							this.invoiceOneTimeSale.calculateAdditionalFeeCharges(chargeeCode, Long.valueOf(resourceId),-1L, clientId, reactivationChargeAmount,BillingTransactionConstants.REACTIVATION_FEE);
						}
					
						break;

					case EventActionConstants.TERMINATION_FEE:

						final Order terminateOrder = this.orderRepository.findOne(Long.valueOf(resourceId));
						final Contract contractDetails = this.contractRepository.findOne(terminateOrder.getContarctPeriod());
						// Calculate EndDate
						final LocalDate endDate = this.orderAssembler.calculateEndDate(new LocalDate(terminateOrder.getStartDate()), contractDetails.getSubscriptionType(),contractDetails.getUnits());
						if (endDate == null || (endDate != null && DateUtils.getLocalDateOfTenant().isBefore(endDate))) {
							List<FeeMasterData> feeDatas = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId, "Termination Fee", null, null);
							final String terimateCharge = feeDatas.get(0).getChargeCode();
							if (terimateCharge != null && !StringUtils.isEmpty(terimateCharge)) {
								this.invoiceOneTimeSale.calculateAdditionalFeeCharges(terimateCharge,terminateOrder.getId(), -1L, clientId, feeDatas.get(0).getDefaultFeeAmount(),BillingTransactionConstants.TERMINATION_FEE);
							}
						}
						
						break;

					case EventActionConstants.ACTION_ORDER_RECONNECTION:
						
						eventAction = new EventAction(DateUtils.getDateOfTenant(), "RECONNECT",EventActionConstants.EVENT_ORDER_RECONNECTION,
								EventActionConstants.ACTION_ORDER_RECONNECTION.toString(),"/order/reconnect/" + Long.parseLong(resourceId), Long.parseLong(resourceId),
								jsonObject.toString(), Long.parseLong(resourceId), clientId);
						this.eventActionRepository.save(eventAction);
						
						break;

					case EventActionConstants.ACTION_SEND_CLIENTCREATION_EMAIL:
						
						BillingMessageTemplate createSelfcareMessageDetails = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_CREATE_SELFCARE);
						if (createSelfcareMessageDetails != null) {
							Client newClient = this.clientRepository.findOne(clientId);
							SelfCare selfCare = this.selfCareRepository.findOneByClientId(clientId);
							Order orderPlan = this.orderRepository.findOne(Long.valueOf(resourceId));
							Plan plan = this.planRepository.findOne(orderPlan.getPlanId());
							Configuration isNoteForPlan = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_NOTE_FOR_PLAN);
							if(isNoteForPlan.isEnabled()){
								
								System.out.println(" details ........"+newClient.getCategoryType()+"........."+plan.getId());
								if(newClient.getCategoryType() == 239 && plan.getId() == 12 ){
									String subject = createSelfcareMessageDetails.getSubject();
									String body = createSelfcareMessageDetails.getBody();
									String footer = createSelfcareMessageDetails.getFooter();
									String header = createSelfcareMessageDetails.getHeader().replace("<PARAM1>",newClient.getFirstname());
									String planNotes = plan.getPlanNotes();
									if (planNotes != null) {
										planNotes = planNotes.replace("<PARAM_USERNAME>",selfCare.getUserName().trim());
										planNotes = planNotes.replace("<PARAM_PASSWORD>",selfCare.getPassword().trim());
										planNotes = planNotes.replace("<PARAM_PLAN_NAME>", plan.getPlanCode());
										String telno = null;
										if (plan.getId() == 12) {
											telno = this.hardwareAssociationReadplatformService.retrieveClientTalkSerialNoFirstNo(clientId);
											System.out.println(" tel no "+telno);
											if(telno == null){
												telno="not Defined";
												planNotes = planNotes.replace("<PARAM_TEL_NO>", telno);
											}else{
												planNotes = planNotes.replace("<PARAM_TEL_NO>", telno);
											}
										}
										body = body.replace("<PLAN_NOTES_PARAM>", planNotes);
									}

									BillingMessage newClientBillingMessage = new BillingMessage(header, body, footer,
											newClient.getOffice().getOfficeAddress().getEmail(), newClient.getEmail(),
											subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS,
											createSelfcareMessageDetails,
											BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
									this.messageDataRepository.save(newClientBillingMessage);
								}
								
								 if((plan.getId()!=4) && !(newClient.getCategoryType() == 239)){
									String subject = createSelfcareMessageDetails.getSubject();
									String body = createSelfcareMessageDetails.getBody();
									String footer = createSelfcareMessageDetails.getFooter();
									String header = createSelfcareMessageDetails.getHeader().replace("<PARAM1>",newClient.getFirstname());
									String planNotes = plan.getPlanNotes();
									if (planNotes != null) {
										planNotes = planNotes.replace("<PARAM_USERNAME>",selfCare.getUserName().trim());
										planNotes = planNotes.replace("<PARAM_PASSWORD>",selfCare.getPassword().trim());
										planNotes = planNotes.replace("<PARAM_PLAN_NAME>", plan.getPlanCode());
										String telno = null;
										if (plan.getId() == 1 || plan.getId() == 2 || plan.getId() == 6 || plan.getId() == 7) {
											telno = this.hardwareAssociationReadplatformService.retrieveClientTalkSerialNo(clientId);
											planNotes = planNotes.replace("<PARAM_TEL_NO>", telno);
										}
										if (plan.getId() == 5) {
											telno = this.hardwareAssociationReadplatformService.retrieveClientTalkgoSerialNo(clientId, orderPlan.getId(), plan.getId());
											planNotes = planNotes.replace("<PARAM_TEL_NO>", telno);
										}
										body = body.replace("<PLAN_NOTES_PARAM>", planNotes);
									}

									BillingMessage newClientBillingMessage = new BillingMessage(header, body, footer,
											newClient.getOffice().getOfficeAddress().getEmail(), newClient.getEmail(),
											subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS,
											createSelfcareMessageDetails,
											BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
									this.messageDataRepository.save(newClientBillingMessage);
						  
								}
							}

						}
						break;
						
					case EventActionConstants.ACTION_TICKET_CREATION:
						
						Long problemcode = null;
						Long depatmentId = Long.valueOf(1);
						Long assignd = Long.valueOf(1);
						DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
						JSONObject ticketJson = new JSONObject();
						Collection<MCodeData> problemCode = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROBLEM_CODE);
						Client cclient = this.clientRepository.findOne(clientId);
						if (detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CREATE_CLIENT)) {
							for (MCodeData problemcodes : problemCode) {
								if (problemcodes.getmCodeValue().equalsIgnoreCase(detailsData.getEventName())) {
									problemcode = problemcodes.getId();
									break;
								}
							}
							TicketAssignRuleData cticketAssignRuleData = this.ticketAssignRuleReadPlatformService.retrieveCategoryDepartment(problemcode, cclient.getCategoryType());
							if (cticketAssignRuleData != null)
								depatmentId = cticketAssignRuleData.getDepartmentId();
							final List<EmployeeData> cemployeesdata = this.employeeReadPlatformService.retrieveAllEmployeeDataByDeptId(depatmentId);
							for (EmployeeData employeedatas : cemployeesdata) {
								if (employeedatas.isIsprimary()) {
									assignd = employeedatas.getUserId();
								}
							}
							ticketJson.put("assignedTo", assignd);
							ticketJson.put("dateFormat", "dd MMMM yyyy");
							ticketJson.put("description", "Client Creation");
							ticketJson.put("locale", "en");
							ticketJson.put("priority", "LOW");
							ticketJson.put("problemCode", problemcode.intValue());
							ticketJson.put("ticketDate", df.format(DateUtils.getDateOfTenant()));
							ticketJson.put("ticketTime", " " + DateFormat.getTimeInstance().format(new Date()));
							ticketJson.put("ticketURL", "null");
							StringBuilder cbuilder = new StringBuilder();
							cbuilder.append("Hi " + cclient.getFirstname() + ",").append("\n");
							cbuilder.append("You have been created with Id : ").append(cclient.getId());
							ticketJson.put("issue", cbuilder);
							ticketJson.put("status", "Open");
							ticketJson.put("departmentId", depatmentId);
							this.ticketMasterApiResource.returnTicket(clientId, ticketJson.toString());
						} else if (detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CREATE_ORDER)) {
							Order orders = this.orderRepository.findOne(Long.valueOf(resourceId));
							Plan plan = this.planRepository.findOne(orders.getPlanId());
							boolean isNewPlan = Boolean.parseBoolean(ticketURL);
							if(plan.getProvisionSystem().equalsIgnoreCase("u-mee sync server")){
								if(isNewPlan){
									if (plan.getId() == 1 || plan.getId() == 2 || plan.getId() == 6 || plan.getId() == 7 || plan.getId() == 12) {
										for (MCodeData problemcodes : problemCode) {
											if (problemcodes.getmCodeValue().equalsIgnoreCase(detailsData.getEventName())) {
												problemcode = problemcodes.getId();
												break;
											}
										}
										TicketAssignRuleData ticketAssignRuleData = this.ticketAssignRuleReadPlatformService.retrieveCategoryDepartment(problemcode, cclient.getCategoryType());
										if (ticketAssignRuleData != null)
											depatmentId = ticketAssignRuleData.getDepartmentId();
										final List<EmployeeData> employeesdata = this.employeeReadPlatformService.retrieveAllEmployeeDataByDeptId(depatmentId);
										for (EmployeeData employeedatas : employeesdata) {
											if (employeedatas.isIsprimary()) {
												assignd = employeedatas.getUserId();
											}
										}
										ticketJson.put("assignedTo", assignd);
										ticketJson.put("dateFormat", "dd MMMM yyyy");
										ticketJson.put("description", "Order Booking");
										ticketJson.put("locale", "en");
										ticketJson.put("priority", "LOW");
										ticketJson.put("problemCode", problemcode.intValue());
										ticketJson.put("ticketDate", df.format(DateUtils.getDateOfTenant()));
										ticketJson.put("ticketTime"," " + DateFormat.getTimeInstance().format(new Date()));
										ticketJson.put("ticketURL", "null");
										StringBuilder builder = new StringBuilder();
										builder.append("Hi " + cclient.getFirstname() + ",").append("\n");
										builder.append("You have been booked with New Order following are the details:").append("\n");
										builder.append("Plan Name: " + plan.getPlanCode()).append("\n").append("OrderId :" + resourceId);
										ticketJson.put("issue", builder);
										ticketJson.put("status", "Open");
										ticketJson.put("departmentId", depatmentId);
										this.ticketMasterApiResource.returnTicket(clientId, ticketJson.toString());
									}
							    }
							}else{
								for (MCodeData problemcodes : problemCode) {
									if (problemcodes.getmCodeValue().equalsIgnoreCase(detailsData.getEventName())) {
										problemcode = problemcodes.getId();
										break;
									}
								}
								TicketAssignRuleData ticketAssignRuleData = this.ticketAssignRuleReadPlatformService.retrieveCategoryDepartment(problemcode, cclient.getCategoryType());
								if (ticketAssignRuleData != null)
									depatmentId = ticketAssignRuleData.getDepartmentId();
								final List<EmployeeData> employeesdata = this.employeeReadPlatformService.retrieveAllEmployeeDataByDeptId(depatmentId);
								for (EmployeeData employeedatas : employeesdata) {
									if (employeedatas.isIsprimary()) {
										assignd = employeedatas.getUserId();
									}
								}
								ticketJson.put("assignedTo", assignd);
								ticketJson.put("dateFormat", "dd MMMM yyyy");
								ticketJson.put("description", "Order Booking");
								ticketJson.put("locale", "en");
								ticketJson.put("priority", "LOW");
								ticketJson.put("problemCode", problemcode.intValue());
								ticketJson.put("ticketDate", df.format(DateUtils.getDateOfTenant()));
								ticketJson.put("ticketTime", " " + DateFormat.getTimeInstance().format(new Date()));
								ticketJson.put("ticketURL", "null");
								StringBuilder builder = new StringBuilder();
								builder.append("Hi " + cclient.getFirstname() + ",").append("\n");
								builder.append("You have been booked with New Order following are the details:").append("\n");
								builder.append("Plan Name: " + plan.getPlanCode()).append("\n").append("OrderId :" + resourceId);
								ticketJson.put("issue", builder);
								ticketJson.put("status", "Open");
								ticketJson.put("departmentId", depatmentId);
								this.ticketMasterApiResource.returnTicket(clientId, ticketJson.toString());
							}
						} else if (detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_HARDWARE_SALE)) {
							for (MCodeData problemcodes : problemCode) {
								if (problemcodes.getmCodeValue().equalsIgnoreCase(detailsData.getEventName())) {
									problemcode = problemcodes.getId();
									break;
								}
							}
							TicketAssignRuleData hticketAssignRuleData = this.ticketAssignRuleReadPlatformService.retrieveCategoryDepartment(problemcode, cclient.getCategoryType());
							if (hticketAssignRuleData != null)
								depatmentId = hticketAssignRuleData.getDepartmentId();
							final List<EmployeeData> hemployeesdata = this.employeeReadPlatformService.retrieveAllEmployeeDataByDeptId(depatmentId);
							for (EmployeeData employeedatas : hemployeesdata) {
								if (employeedatas.isIsprimary()) {
									assignd = employeedatas.getUserId();
								}
							}
							List<ItemDetailsAllocation> itemDetailsAllocation = this.itemDetailsAllocationRepository.findRemainingAllocatedDevice(clientId, Long.valueOf(resourceId));
							for (ItemDetailsAllocation itemAllocated : itemDetailsAllocation) {
								ticketJson.put("assignedTo", assignd);
								ticketJson.put("dateFormat", "dd MMMM yyyy");
								ticketJson.put("description", "Hardware Sale");
								ticketJson.put("locale", "en");
								ticketJson.put("priority", "LOW");
								ticketJson.put("problemCode", problemcode.intValue());
								ticketJson.put("ticketDate", df.format(DateUtils.getDateOfTenant()));
								ticketJson.put("ticketTime", " " + DateFormat.getTimeInstance().format(new Date()));
								ticketJson.put("ticketURL", "null");
								ticketJson.put("issue", "Device allocated to ClientId :" + clientId + "  and Sale id is :" + resourceId
												+ " and SerialNumber : " + itemAllocated.getSerialNumber());
								ticketJson.put("status", "Open");
								ticketJson.put("departmentId", depatmentId);
								this.ticketMasterApiResource.returnTicket(clientId, ticketJson.toString());
							}
						}
						break;
						
					default:
						break;
					}

					
					/* SMS SENDING EVENT ACTIONS */
					Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_SMS);

					if (null != configuration && configuration.isEnabled()) {

						orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);
						messageFrom = orderData.getOfficeEmail();

						if (null == messageFrom) {
							messageFrom = orderData.getClientPhone();
						}

						

						switch (detailsData.getActionName()) {

						case EventActionConstants.ACTION_NOTIFY_SMS_ACTIVATION:

							orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,
									new Long(resourceId));

							template = getTemplate(
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ACTIVATION);

							bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
							bodyMessage = bodyMessage.replaceAll("<Activation Date>",
									dateFormat.format(orderData.getActivationDate().toDate()));

							billingMessage = new BillingMessage(null, bodyMessage, null, messageFrom,
									orderData.getClientPhone(), template.getSubject(),
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

							this.messageDataRepository.save(billingMessage);

							break;

						case EventActionConstants.ACTION_NOTIFY_SMS_DISCONNECTION:

							orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,
									new Long(resourceId));

							template = getTemplate(
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_DISCONNECTION);

							bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
							bodyMessage = bodyMessage.replaceAll("<Disconnection Date>",
									dateFormat.format(orderData.getEndDate().toDate()));

							billingMessage = new BillingMessage(null, bodyMessage, null, messageFrom,
									orderData.getClientPhone(), template.getSubject(),
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

							this.messageDataRepository.save(billingMessage);

							break;

						case EventActionConstants.ACTION_NOTIFY_SMS_RECONNECTION:

							orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId,
									new Long(resourceId));

							template = getTemplate(
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_RECONNECTION);

							bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
							bodyMessage = bodyMessage.replaceAll("<Reconnection Date>",
									dateFormat.format(orderData.getStartDate().toDate()));

							billingMessage = new BillingMessage(null, bodyMessage, null, messageFrom,
									orderData.getClientPhone(), template.getSubject(),
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

							this.messageDataRepository.save(billingMessage);

							break;

						case EventActionConstants.ACTION_NOTIFY_SMS_PAYMENT:

							orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);

							template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT);

							bodyMessage = template.getBody().replaceAll("<Amount>", resourceId);

							bodyMessage = bodyMessage.replaceAll("<Payment Date>",
									dateFormat.format(DateUtils.getLocalDateOfTenant().toDate()));

							billingMessage = new BillingMessage(null, bodyMessage, null, messageFrom,
									orderData.getClientPhone(), template.getSubject(),
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

							this.messageDataRepository.save(billingMessage);

							break;

						case EventActionConstants.ACTION_NOTIFY_SMS_CHANGEPLAN:

							orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);

							template = getTemplate(
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_CHANGEPLAN);

							bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());
							bodyMessage = bodyMessage.replaceAll("<Activation Date>",
									dateFormat.format(orderData.getActivationDate().toDate()));

							billingMessage = new BillingMessage(null, bodyMessage, null, messageFrom,
									orderData.getClientPhone(), template.getSubject(),
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

							this.messageDataRepository.save(billingMessage);

							break;

						case EventActionConstants.ACTION_NOTIFY_SMS_ORDER_TERMINATE:

							orderData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);

							template = getTemplate(
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ORDERTERMINATION);

							bodyMessage = template.getBody().replaceAll("<Service name>", orderData.getPlanName());

							bodyMessage = bodyMessage.replaceAll("<Disconnection Date>",
									dateFormat.format(DateUtils.getLocalDateOfTenant().toDate()));

							billingMessage = new BillingMessage(null, bodyMessage, null, messageFrom,
									orderData.getClientPhone(), template.getSubject(),
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, template,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

							this.messageDataRepository.save(billingMessage);

							break;

						default:
							break;
						}
					}

				}
			}
			return null;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}

	}

	private BillingMessageTemplate getTemplate(String templateName) {

		if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION.equalsIgnoreCase(templateName)) {

			if (null == activationTemplates) {
				activationTemplates = this.messageTemplateRepository
						.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION);
			}
			return activationTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION
				.equalsIgnoreCase(templateName)) {

			if (null == disConnectionTemplates) {
				disConnectionTemplates = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION);
			}
			return disConnectionTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION
				.equalsIgnoreCase(templateName)) {

			if (null == reConnectionTemplates) {
				reConnectionTemplates = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION);
			}
			return reConnectionTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT.equalsIgnoreCase(templateName)) {

			if (null == paymentTemplates) {
				paymentTemplates = this.messageTemplateRepository
						.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT);
			}
			return paymentTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_CHANGEPLAN.equalsIgnoreCase(templateName)) {

			if (null == changePlanTemplates) {
				changePlanTemplates = this.messageTemplateRepository
						.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_CHANGEPLAN);
			}
			return changePlanTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ORDERTERMINATION
				.equalsIgnoreCase(templateName)) {

			if (null == orderTerminationTemplates) {
				orderTerminationTemplates = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ORDERTERMINATION);
			}
			return orderTerminationTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ACTIVATION
				.equalsIgnoreCase(templateName)) {

			if (null == smsActivationTemplates) {
				smsActivationTemplates = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ACTIVATION);
			}
			return smsActivationTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_DISCONNECTION
				.equalsIgnoreCase(templateName)) {

			if (null == smsDisconnectionTemplates) {
				smsDisconnectionTemplates = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_DISCONNECTION);
			}
			return smsDisconnectionTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_RECONNECTION
				.equalsIgnoreCase(templateName)) {

			if (null == smsReConnectionTemplates) {
				smsReConnectionTemplates = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_RECONNECTION);
			}
			return smsReConnectionTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_PAYMENT.equalsIgnoreCase(templateName)) {

			if (null == smsPaymentTemplates) {
				smsPaymentTemplates = this.messageTemplateRepository
						.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_PAYMENT);
			}
			return smsPaymentTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_CHANGEPLAN
				.equalsIgnoreCase(templateName)) {

			if (null == smsChangePlanTemplates) {
				smsChangePlanTemplates = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_CHANGEPLAN);
			}
			return smsChangePlanTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ORDERTERMINATION
				.equalsIgnoreCase(templateName)) {

			if (null == smsOrderTerminationTemplates) {
				smsOrderTerminationTemplates = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NOTIFY_ORDERTERMINATION);
			}
			return smsOrderTerminationTemplates;

		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_TECHNICAL_TEAM
				.equalsIgnoreCase(templateName)) {

			if (null == notifyTechicalTeam) {
				notifyTechicalTeam = this.messageTemplateRepository.findByTemplateDescription(
						BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_TECHNICAL_TEAM);
			}
			return notifyTechicalTeam;

		} else {
			throw new BillingMessageTemplateNotFoundException(templateName);
		}

	}
}