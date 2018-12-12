package org.obsplatform.provisioning.processrequest.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.billingorder.service.InvoiceClient;
import org.obsplatform.infrastructure.configuration.domain.EnumDomainServiceRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.ippool.domain.IpPoolManagementDetail;
import org.obsplatform.organisation.ippool.domain.IpPoolManagementJpaRepository;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.client.domain.ClientStatus;
import org.obsplatform.portfolio.order.data.OrderStatusEnumaration;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderAddons;
import org.obsplatform.portfolio.order.domain.OrderAddonsRepository;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.obsplatform.portfolio.order.service.OrderAssembler;
import org.obsplatform.portfolio.order.service.OrderReadPlatformService;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.obsplatform.provisioning.preparerequest.domain.PrepareRequsetRepository;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.obsplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.obsplatform.provisioning.provisioning.domain.ServiceParameters;
import org.obsplatform.provisioning.provisioning.domain.ServiceParametersRepository;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.obsplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service(value = "processRequestWriteplatformService")
public class ProcessRequestWriteplatformServiceImpl implements ProcessRequestWriteplatformService{

	  private static final Logger logger =LoggerFactory.getLogger(ProcessRequestReadplatformServiceImpl.class);
	  
	  private final PlanRepository planRepository;
	  private final IpPoolManagementJpaRepository ipPoolManagementJpaRepository;
	  private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	  private final ActiondetailsWritePlatformService actiondetailsWritePlatformService; 
	  private final PlatformSecurityContext context;
	  private final OrderRepository orderRepository;
	  private final ClientRepository clientRepository;
	  private final OrderReadPlatformService orderReadPlatformService;
	  private final ProcessRequestRepository processRequestRepository;
	  private final ServiceParametersRepository serviceParametersRepository;
	  private final OrderAddonsRepository orderAddonsRepository;
      private final OrderAssembler orderAssembler;
      private final InvoiceClient invoiceClient;
	  
	  

	    @Autowired
	    public ProcessRequestWriteplatformServiceImpl(final OrderReadPlatformService orderReadPlatformService,final OrderAssembler orderAssembler,
	    		final OrderRepository orderRepository,final ProcessRequestRepository processRequestRepository,final PrepareRequsetRepository prepareRequsetRepository,
	    		final ClientRepository clientRepository,final PlanRepository planRepository,final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
	    		final ActiondetailsWritePlatformService actiondetailsWritePlatformService,final PlatformSecurityContext context,
	    		final EnumDomainServiceRepository enumDomainServiceRepository,final ServiceParametersRepository parametersRepository,
	    		final IpPoolManagementJpaRepository ipPoolManagementJpaRepository,final OrderAddonsRepository orderAddonsRepository,
	    	    final InvoiceClient invoiceClient) {

	    	
	    	    this.context = context;
	    	    this.planRepository=planRepository;
	    	    this.ipPoolManagementJpaRepository=ipPoolManagementJpaRepository;
	    	    this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
	    	    this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
	    	    this.orderAddonsRepository=orderAddonsRepository;
	    	    this.orderRepository=orderRepository;
	    	    this.clientRepository=clientRepository;
	    	    this.orderAssembler = orderAssembler;
	    	    this.serviceParametersRepository=parametersRepository;
	    	    this.processRequestRepository=processRequestRepository;
	    	    this.orderReadPlatformService=orderReadPlatformService;
	    	    this.invoiceClient = invoiceClient;

	             
	    }

	@Override
	public void notifyProcessingDetails(ProcessRequest detailsData, char status) {
		
		try {
			if (detailsData != null && !(detailsData.getRequestType().equalsIgnoreCase(ProvisioningApiConstants.REQUEST_TERMINATE))
					&& status != 'F') {

				Order order = null;
				Plan plan = null;

				if (detailsData.getOrderId() != null && detailsData.getOrderId() > 0) {
					order = this.orderRepository.findOne(detailsData.getOrderId());
					plan = this.planRepository.findOne(order.getPlanId());
				}

				Client client = this.clientRepository.findOne(detailsData.getClientId());

				switch (detailsData.getRequestType()) {

				case ProvisioningApiConstants.REQUEST_ACTIVATION:

					if (detailsData.getRequestType().equalsIgnoreCase(UserActionStatusTypeEnum.ACTIVATION.toString())) {
						order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
						ActionDetaislData provActionDetail = this.actionDetailsReadPlatformService.retrieveEventWithAction(EventActionConstants.EVENT_PROVISION_CONFIRM,EventActionConstants.ACTION_CHANGE_START);
						if (provActionDetail != null) {
							order.setStartDate(DateUtils.getLocalDateOfTenant());
							order = this.orderAssembler.setDatesOnOrderActivation(order,DateUtils.getLocalDateOfTenant(),detailsData.getRequestType());
						}
						client.setStatus(ClientStatus.ACTIVE.getValue());
						this.orderRepository.saveAndFlush(order);
						List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_ACTIVE_ORDER);
						if (actionDetaislDatas.size() != 0) {
							this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,order.getClientId(), order.getId().toString(), null);
						}
					}

					break;
					
				case ProvisioningApiConstants.REQUEST_RECONNECTION:

					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					ActionDetaislData provReconnectActionDetail = this.actionDetailsReadPlatformService.retrieveEventWithAction(EventActionConstants.EVENT_PROVISION_CONFIRM,EventActionConstants.ACTION_CHANGE_START);
					if (provReconnectActionDetail != null) {
						order.setStartDate(DateUtils.getLocalDateOfTenant());
						order = this.orderAssembler.setDatesOnOrderActivation(order, DateUtils.getLocalDateOfTenant(),detailsData.getRequestType());
					}
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.orderRepository.saveAndFlush(order);
					List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_RECONNECTION_ORDER);
					if (actionDetaislDatas.size() != 0) {
						this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, order.getClientId(),order.getId().toString(), null);
					}

					break;

				case ProvisioningApiConstants.REQUEST_CHANGE_PLAN:

					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					ActionDetaislData provChangePlanActionDetail = this.actionDetailsReadPlatformService.retrieveEventWithAction(EventActionConstants.EVENT_PROVISION_CONFIRM,EventActionConstants.ACTION_CHANGE_START);
					if (provChangePlanActionDetail != null) {
						order.setStartDate(DateUtils.getLocalDateOfTenant());
						order = this.orderAssembler.setDatesOnOrderActivation(order, DateUtils.getLocalDateOfTenant(),detailsData.getRequestType());
					}
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.orderRepository.saveAndFlush(order);
					actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CHANGE_PLAN);
					if (actionDetaislDatas.size() != 0) {
						this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, order.getClientId(),order.getId().toString(), null);
					}

					break;

				case ProvisioningApiConstants.REQUEST_DISCONNECTION:

					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.DISCONNECTED).getId());
					this.orderRepository.saveAndFlush(order);
					Long activeOrders = this.orderReadPlatformService.retrieveClientActiveOrderDetails(order.getClientId(), null);
					if (activeOrders == 0) {
						client.setStatus(ClientStatus.DEACTIVE.getValue());
					}
					actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_DISCONNECTION_ORDER);
					if (actionDetaislDatas.size() != 0) {
						this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, order.getClientId(),order.getId().toString(), null);
					}

					break;
					
				case ProvisioningApiConstants.REQUEST_RENEWAL_AE:

					if (detailsData.getOrderId() != null && detailsData.getOrderId() > 0) {
						order = this.orderRepository.findOne(detailsData.getOrderId());
						plan = this.planRepository.findOne(order.getPlanId());
					}

					client = this.clientRepository.findOne(detailsData.getClientId());
					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					ActionDetaislData provRenewalAfterActionDetail = this.actionDetailsReadPlatformService.retrieveEventWithAction(EventActionConstants.EVENT_PROVISION_CONFIRM,EventActionConstants.ACTION_CHANGE_START);
					if (provRenewalAfterActionDetail != null) {
						order = this.orderAssembler.setDatesOnOrderActivation(order, DateUtils.getLocalDateOfTenant(),detailsData.getRequestType());
					}
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.orderRepository.saveAndFlush(order);

					if (plan.isPrepaid() == 'Y') {
						Invoice invoice = this.invoiceClient.singleOrderInvoice(order.getId(), order.getClientId(),new LocalDate(order.getStartDate()).plusDays(1));
						if (invoice != null) {
							List<ActionDetaislData> actionDetaislData = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_TOPUP_INVOICE_MAIL);
							if (actionDetaislData.size() != 0) {
								this.actiondetailsWritePlatformService.AddNewActions(actionDetaislData,order.getClientId(), invoice.getId().toString(), null);
							}
						}
					}

					actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_RECONNECTION_ORDER);
					if (actionDetaislDatas.size() != 0) {
						this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, order.getClientId(),order.getId().toString(), null);
					}

					break;	

				case ProvisioningApiConstants.REQUEST_SUSPENSION:

					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.SUSPENDED).getId());
					this.orderRepository.saveAndFlush(order);

					break;

				case ProvisioningApiConstants.REQUEST_REACTIVATION:

					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					this.orderRepository.saveAndFlush(order);
					
					break;

				case ProvisioningApiConstants.REQUEST_TERMINATION:

					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.TERMINATED).getId());
					this.orderRepository.saveAndFlush(order);
					List<ActionDetaislData> TerminationActionDetaislData = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.TERMINATION_FEE);
					if (TerminationActionDetaislData.size() != 0) {
						this.actiondetailsWritePlatformService.AddNewActions(TerminationActionDetaislData,order.getClientId(), order.getId().toString(), null);
					}

					if (plan.getProvisionSystem().equalsIgnoreCase(ProvisioningApiConstants.PROV_PACKETSPAN)) {

						List<ServiceParameters> parameters = this.serviceParametersRepository.findDataByOrderId(order.getId());
						for (ServiceParameters serviceParameter : parameters) {
							if (serviceParameter.getParameterName().equalsIgnoreCase(ProvisioningApiConstants.PROV_DATA_IPADDRESS)) {
								JSONArray ipAddresses = new JSONArray(serviceParameter.getParameterValue());
								for (int i = 0; i < ipAddresses.length(); i++) {
									IpPoolManagementDetail ipPoolManagementDetail = this.ipPoolManagementJpaRepository.findAllocatedIpAddressData(ipAddresses.getString(i));
									if (ipPoolManagementDetail != null) {
										ipPoolManagementDetail.setStatus('T');
										ipPoolManagementDetail.setClientId(null);
										this.ipPoolManagementJpaRepository.save(ipPoolManagementDetail);
									}
								}
							}
						}
					}

					break;

				case ProvisioningApiConstants.REQUEST_ADDON_ACTIVATION:

					List<ProcessRequestDetails> requestDetails = detailsData.getProcessRequestDetails();
					JSONObject jsonObject = new JSONObject(requestDetails.get(0).getSentMessage());
					JSONArray array = jsonObject.getJSONArray("services");
					for (int i = 0; i < array.length(); i++) {
						JSONObject addOnJson = array.getJSONObject(i);
						OrderAddons addons = this.orderAddonsRepository.findOne(addOnJson.getLong("addonId"));
						addons.setStatus(StatusTypeEnum.ACTIVE.toString());
						this.orderAddonsRepository.saveAndFlush(addons);
					}

					break;
				case ProvisioningApiConstants.REQUEST_ADDON_DISCONNECTION:

					requestDetails = detailsData.getProcessRequestDetails();
					jsonObject = new JSONObject(requestDetails.get(0).getSentMessage());
					array = jsonObject.getJSONArray("services");
					for (int i = 0; i < array.length(); i++) {
						JSONObject addOnJson = array.getJSONObject(i);
						OrderAddons addons = this.orderAddonsRepository.findOne(addOnJson.getLong("addonId"));
						addons.setStatus(StatusTypeEnum.DISCONNECTED.toString());
						this.orderAddonsRepository.saveAndFlush(addons);
					}

					break;
					
				case ProvisioningApiConstants.REQUEST_CHANGE_CREDENTIALS:
					
					break;
					
                case ProvisioningApiConstants.REQUEST_CHANGE_EXDIRECTORY:
					
					break;

			

				default:

					if (order != null) {
						order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
						this.orderRepository.saveAndFlush(order);
					}
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.clientRepository.saveAndFlush(client);

					break;
				}

				this.clientRepository.saveAndFlush(client);
				detailsData.setNotify();
				detailsData.setProcessStatus(status);
			}
			this.processRequestRepository.saveAndFlush(detailsData);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult addProcessRequest(JsonCommand command) {

		try {
			this.context.authenticatedUser();
			ProcessRequest processRequest = ProcessRequest.fromJson(command);
			ProcessRequestDetails processRequestDetails = ProcessRequestDetails.fromJson(processRequest, command);
			processRequest.add(processRequestDetails);
			this.processRequestRepository.save(processRequest);
			return new CommandProcessingResult(Long.valueOf(processRequest.getPrepareRequestId()),
					processRequest.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}

	}

	private void handleCodeDataIntegrityIssues(JsonCommand command, DataIntegrityViolationException dve) {
		Throwable realCause = dve.getMostSpecificCause();
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: " + realCause.getMessage());
	}

}
