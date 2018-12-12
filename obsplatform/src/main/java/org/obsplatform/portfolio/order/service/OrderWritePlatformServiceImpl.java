package org.obsplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.SQLGrammarException;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.obsplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.obsplatform.billing.payterms.data.PaytermData;
import org.obsplatform.billing.planprice.domain.Price;
import org.obsplatform.billing.planprice.domain.PriceRepository;
import org.obsplatform.billing.planprice.exceptions.ChargeCodeAndContractPeriodException;
import org.obsplatform.billing.planprice.exceptions.ContractNotNullException;
import org.obsplatform.billing.planprice.exceptions.PriceNotFoundException;
import org.obsplatform.billing.promotioncodes.domain.PromotionCodeMaster;
import org.obsplatform.billing.promotioncodes.domain.PromotionCodeRepository;
import org.obsplatform.billing.promotioncodes.exception.PromotionCodeNotFoundException;
import org.obsplatform.cms.eventorder.service.PrepareRequestWriteplatformService;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.obsplatform.finance.billingorder.service.InvoiceClient;
import org.obsplatform.finance.billingorder.service.ReverseInvoice;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingRepositoryWrapper;
import org.obsplatform.infrastructure.codes.domain.CodeValue;
import org.obsplatform.infrastructure.codes.domain.CodeValueRepository;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.obsplatform.portfolio.allocation.domain.HardwareAssociationRepository;
import org.obsplatform.portfolio.allocation.service.AllocationReadPlatformService;
import org.obsplatform.portfolio.association.data.AssociationData;
import org.obsplatform.portfolio.association.domain.HardwareAssociation;
import org.obsplatform.portfolio.association.exception.HardwareDetailsNotFoundException;
import org.obsplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.obsplatform.portfolio.association.service.HardwareAssociationWriteplatformService;
import org.obsplatform.portfolio.client.domain.AccountNumberGenerator;
import org.obsplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.client.domain.ClientStatus;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.contract.domain.Contract;
import org.obsplatform.portfolio.contract.domain.ContractRepository;
import org.obsplatform.portfolio.contract.exception.ContractPeriodNotFoundException;
import org.obsplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.obsplatform.portfolio.isexdirectory.domain.IsExDirectory;
import org.obsplatform.portfolio.isexdirectory.domain.IsExDirectoryRepository;
import org.obsplatform.portfolio.order.api.OrderAddonsApiResource;
import org.obsplatform.portfolio.order.data.OrderAddonsData;
import org.obsplatform.portfolio.order.data.OrderData;
import org.obsplatform.portfolio.order.data.OrderPriceData;
import org.obsplatform.portfolio.order.data.OrderStatusEnumaration;
import org.obsplatform.portfolio.order.data.UserActionStatusEnumaration;
import org.obsplatform.portfolio.order.domain.ConnectionTypeEnum;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderAddons;
import org.obsplatform.portfolio.order.domain.OrderAddonsRepository;
import org.obsplatform.portfolio.order.domain.OrderDiscount;
import org.obsplatform.portfolio.order.domain.OrderHistory;
import org.obsplatform.portfolio.order.domain.OrderHistoryRepository;
import org.obsplatform.portfolio.order.domain.OrderLine;
import org.obsplatform.portfolio.order.domain.OrderPrice;
import org.obsplatform.portfolio.order.domain.OrderPriceRepository;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.domain.PaymentFollowup;
import org.obsplatform.portfolio.order.domain.PaymentFollowupRepository;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.obsplatform.portfolio.order.exceptions.EventActionsAvailabeForRenewalWithChangePlanFound;
import org.obsplatform.portfolio.order.exceptions.NoDurationFound;
import org.obsplatform.portfolio.order.exceptions.NoOrdersFoundException;
import org.obsplatform.portfolio.order.exceptions.RenewalOrderException;
import org.obsplatform.portfolio.order.serialization.OrderCommandFromApiJsonDeserializer;
import org.obsplatform.portfolio.plan.data.PlanCodeData;
import org.obsplatform.portfolio.plan.data.PlanData;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanDetails;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.obsplatform.portfolio.plan.exceptions.PlanNotFundException;
import org.obsplatform.portfolio.plan.service.PlanReadPlatformService;
import org.obsplatform.portfolio.service.domain.ServiceMaster;
import org.obsplatform.portfolio.service.domain.ServiceMasterRepository;
import org.obsplatform.provisioning.preparerequest.domain.PrepareRequest;
import org.obsplatform.provisioning.preparerequest.domain.PrepareRequsetRepository;
import org.obsplatform.provisioning.preparerequest.exception.PrepareRequestActivationException;
import org.obsplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.obsplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.obsplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.obsplatform.provisioning.provsionactions.domain.ProvisionActions;
import org.obsplatform.provisioning.provsionactions.domain.ProvisioningActionsRepository;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.domain.EventAction;
import org.obsplatform.workflow.eventaction.domain.EventActionRepository;
import org.obsplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.obsplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.obsplatform.workflow.eventvalidation.service.EventValidationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class OrderWritePlatformServiceImpl implements OrderWritePlatformService {
	
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	private final ProvisioningWritePlatformService provisioningWritePlatformService;
	private final PlanRepository planRepository;
	private final ReverseInvoice reverseInvoice;
	private final PlatformSecurityContext context;
	private final OrderRepository orderRepository;
	private final OrderAddonsRepository orderAddonsRepository;
	private final PriceRepository priceRepository;
	private final OrderAssembler orderAssembler;
	private final ClientRepository clientRepository;
	private final EventValidationReadPlatformService eventValidationReadPlatformService;
	private final PrepareRequestReadplatformService prepareRequestReadplatformService;
	private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
	private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
	private final PrepareRequestWriteplatformService prepareRequestWriteplatformService;
	private final HardwareAssociationWriteplatformService associationWriteplatformService;
	private final OrderReadPlatformService orderReadPlatformService;
	private final ServiceMasterRepository serviceMasterRepository;
	private final PrepareRequsetRepository prepareRequsetRepository;
	private final PaymentFollowupRepository paymentFollowupRepository;
	private final CodeValueRepository codeValueRepository;
	private final HardwareAssociationRepository associationRepository;
	private final AllocationReadPlatformService allocationReadPlatformService;
	private final OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ConfigurationRepository configurationRepository;
	private final PromotionCodeRepository promotionCodeRepository;
	private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
	private final ChargeCodeRepository chargeCodeRepository;
	private final OrderPriceRepository orderPriceRepository;
	private final EventActionRepository eventActionRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
	private final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper;
	private final ContractRepository contractRepository;
	private final InvoiceClient invoiceClient;
	private final FromJsonHelper fromJsonHelper;
	private final PlanReadPlatformService planReadPlatformService;
	private final ProcessRequestRepository processRequestRepository;
	private final JdbcTemplate jdbcTemplate;
	private final ProvisioningActionsRepository provisioningActionsRepository;
	private final OrderAddOnsWritePlatformService orderAddOnsWritePlatformService;
	private final OrderAddonsApiResource orderAddonsApiResource;
	private final IsExDirectoryRepository isExDirectoryRepository;


	@Autowired
	public OrderWritePlatformServiceImpl(final PlatformSecurityContext context,
			final OrderRepository orderRepository,final PlanRepository planRepository,
			final OrderPriceRepository OrderPriceRepository,final CodeValueRepository codeRepository,
			final ServiceMasterRepository serviceMasterRepository,
			final OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final ReverseInvoice reverseInvoice,final PrepareRequestWriteplatformService prepareRequestWriteplatformService,
			final OrderHistoryRepository orderHistoryRepository,final ConfigurationRepository configurationRepository,
			final AllocationReadPlatformService allocationReadPlatformService,final HardwareAssociationWriteplatformService associationWriteplatformService,
			final PrepareRequestReadplatformService prepareRequestReadplatformService,final OrderReadPlatformService orderReadPlatformService,
			final OrderAddonsRepository addonsRepository,final OrderAssembler orderAssembler,
			final ProcessRequestRepository processRequestRepository,final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,
			final PrepareRequsetRepository prepareRequsetRepository,final PromotionCodeRepository promotionCodeRepository,
			final ContractRepository contractRepository,final ClientRepository clientRepository,
			final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
			final ActiondetailsWritePlatformService actiondetailsWritePlatformService,
			final EventValidationReadPlatformService eventValidationReadPlatformService,
			final EventActionRepository eventActionRepository,final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
			final InvoiceClient invoiceClient,final HardwareAssociationRepository associationRepository,
			final ProvisioningWritePlatformService provisioningWritePlatformService,final PaymentFollowupRepository paymentFollowupRepository,
			final PriceRepository priceRepository,final ChargeCodeRepository chargeCodeRepository,
			final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper,
			final FromJsonHelper fromJsonHelper,final PlanReadPlatformService planReadPlatformService,
			final RoutingDataSource dataSource,
			final ProvisioningActionsRepository provisioningActionsRepository,
			final OrderAddOnsWritePlatformService orderAddOnsWritePlatformService,
			final OrderAddonsApiResource orderAddonsApiResource,
			final IsExDirectoryRepository isExDirectoryRepository) {

		this.context = context;
		this.reverseInvoice = reverseInvoice;
		this.serviceMasterRepository = serviceMasterRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.configurationRepository = configurationRepository;
		this.prepareRequsetRepository = prepareRequsetRepository;
		this.orderReadPlatformService = orderReadPlatformService;
		this.paymentFollowupRepository = paymentFollowupRepository;
		this.allocationReadPlatformService = allocationReadPlatformService;
		this.orderAssembler = orderAssembler;
		this.eventValidationReadPlatformService = eventValidationReadPlatformService;
		this.priceRepository = priceRepository;
		this.planRepository = planRepository;
		this.orderRepository = orderRepository;
		this.orderAddonsRepository = addonsRepository;
		this.clientRepository = clientRepository;
		this.codeValueRepository = codeRepository;
		this.promotionCodeRepository = promotionCodeRepository;
		this.provisioningWritePlatformService = provisioningWritePlatformService;
		this.prepareRequestReadplatformService = prepareRequestReadplatformService;
		this.actiondetailsWritePlatformService = actiondetailsWritePlatformService;
		this.contractPeriodReadPlatformService = contractPeriodReadPlatformService;
		this.prepareRequestWriteplatformService = prepareRequestWriteplatformService;
		this.hardwareAssociationReadplatformService = hardwareAssociationReadplatformService;
		this.chargeCodeRepository = chargeCodeRepository;
		this.orderPriceRepository = OrderPriceRepository;
		this.eventActionRepository = eventActionRepository;
		this.associationRepository = associationRepository;
		this.orderHistoryRepository = orderHistoryRepository;
		this.associationWriteplatformService = associationWriteplatformService;
		this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
		this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
		this.recurringBillingRepositoryWrapper = recurringBillingRepositoryWrapper;
		this.contractRepository = contractRepository;
		this.invoiceClient = invoiceClient;
		this.fromJsonHelper = fromJsonHelper;
		this.planReadPlatformService = planReadPlatformService;
		this.processRequestRepository=processRequestRepository;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.provisioningActionsRepository = provisioningActionsRepository;
		this.orderAddOnsWritePlatformService = orderAddOnsWritePlatformService;
		this.orderAddonsApiResource = orderAddonsApiResource;
		this.isExDirectoryRepository = isExDirectoryRepository;
	}
	
	/*private  ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	private  ProvisioningWritePlatformService provisioningWritePlatformService;
	private  PlanRepository planRepository;
	private  ReverseInvoice reverseInvoice;
	private  PlatformSecurityContext context;
	private  OrderRepository orderRepository;
	private  OrderAddonsRepository orderAddonsRepository;
	private  PriceRepository priceRepository;
	private  OrderAssembler orderAssembler;
	private  ClientRepository clientRepository;
	private  EventValidationReadPlatformService eventValidationReadPlatformService;
	private  PrepareRequestReadplatformService prepareRequestReadplatformService;
	private  ActiondetailsWritePlatformService actiondetailsWritePlatformService;
	private  ContractPeriodReadPlatformService contractPeriodReadPlatformService;
	private  PrepareRequestWriteplatformService prepareRequestWriteplatformService;
	private  HardwareAssociationWriteplatformService associationWriteplatformService;
	private  OrderReadPlatformService orderReadPlatformService;
	private  ServiceMasterRepository serviceMasterRepository;
	private  PrepareRequsetRepository prepareRequsetRepository;
	private  PaymentFollowupRepository paymentFollowupRepository;
	private  CodeValueRepository codeValueRepository;
	private  HardwareAssociationRepository associationRepository;
	private  AllocationReadPlatformService allocationReadPlatformService;
	private  OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private  ConfigurationRepository configurationRepository;
	private  PromotionCodeRepository promotionCodeRepository;
	private  HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
	private  ChargeCodeRepository chargeCodeRepository;
	private  OrderPriceRepository orderPriceRepository;
	private  EventActionRepository eventActionRepository;
	private  OrderHistoryRepository orderHistoryRepository;
	private  AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
	private  RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper;
	private  ContractRepository contractRepository;
	private  InvoiceClient invoiceClient;
	private  FromJsonHelper fromJsonHelper;
	private  PlanReadPlatformService planReadPlatformService;
	private  ProcessRequestRepository processRequestRepository;
	private  JdbcTemplate jdbcTemplate;
	private  MCodeReadPlatformService codeReadPlatformService;
	private  DepartmentReadPlatformService departmentReadPlatformService;
	private  EmployeeReadPlatformService employeeReadPlatformService;
	private  TicketMasterApiResource ticketMasterApiResource;
	
	@Autowired
	public void setActionDetailsReadPlatformService(ActionDetailsReadPlatformService actionDetailsReadPlatformService) {
		this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
	}

	@Autowired
	public void setProvisioningWritePlatformService(ProvisioningWritePlatformService provisioningWritePlatformService) {
		this.provisioningWritePlatformService = provisioningWritePlatformService;
	}

	@Autowired
	public void setPlanRepository(PlanRepository planRepository) {
		this.planRepository = planRepository;
	}

	@Autowired
	public void setReverseInvoice(ReverseInvoice reverseInvoice) {
		this.reverseInvoice = reverseInvoice;
	}

	@Autowired
	public void setContext(PlatformSecurityContext context) {
		this.context = context;
	}

	@Autowired
	public void setOrderRepository(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Autowired
	public void setOrderAddonsRepository(OrderAddonsRepository orderAddonsRepository) {
		this.orderAddonsRepository = orderAddonsRepository;
	}

	@Autowired
	public void setPriceRepository(PriceRepository priceRepository) {
		this.priceRepository = priceRepository;
	}

	@Autowired
	public void setOrderAssembler(OrderAssembler orderAssembler) {
		this.orderAssembler = orderAssembler;
	}

	@Autowired
	public void setClientRepository(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	@Autowired
	public void setEventValidationReadPlatformService(EventValidationReadPlatformService eventValidationReadPlatformService) {
		this.eventValidationReadPlatformService = eventValidationReadPlatformService;
	}

	@Autowired
	public void setPrepareRequestReadplatformService(PrepareRequestReadplatformService prepareRequestReadplatformService) {
		this.prepareRequestReadplatformService = prepareRequestReadplatformService;
	}

	@Autowired
	public void setActiondetailsWritePlatformService(ActiondetailsWritePlatformService actiondetailsWritePlatformService) {
		this.actiondetailsWritePlatformService = actiondetailsWritePlatformService;
	}

	@Autowired
	public void setContractPeriodReadPlatformService(ContractPeriodReadPlatformService contractPeriodReadPlatformService) {
		this.contractPeriodReadPlatformService = contractPeriodReadPlatformService;
	}

	@Autowired
	public void setPrepareRequestWriteplatformService(
			PrepareRequestWriteplatformService prepareRequestWriteplatformService) {
		this.prepareRequestWriteplatformService = prepareRequestWriteplatformService;
	}

	@Autowired
	public void setAssociationWriteplatformService(
			HardwareAssociationWriteplatformService associationWriteplatformService) {
		this.associationWriteplatformService = associationWriteplatformService;
	}

	@Autowired
	public void setOrderReadPlatformService(OrderReadPlatformService orderReadPlatformService) {
		this.orderReadPlatformService = orderReadPlatformService;
	}

	@Autowired
	public void setServiceMasterRepository(ServiceMasterRepository serviceMasterRepository) {
		this.serviceMasterRepository = serviceMasterRepository;
	}

	@Autowired
	public void setPrepareRequsetRepository(PrepareRequsetRepository prepareRequsetRepository) {
		this.prepareRequsetRepository = prepareRequsetRepository;
	}

	@Autowired
	public void setPaymentFollowupRepository(PaymentFollowupRepository paymentFollowupRepository) {
		this.paymentFollowupRepository = paymentFollowupRepository;
	}

	@Autowired
	public void setCodeValueRepository(CodeValueRepository codeValueRepository) {
		this.codeValueRepository = codeValueRepository;
	}

	@Autowired
	public void setAssociationRepository(HardwareAssociationRepository associationRepository) {
		this.associationRepository = associationRepository;
	}

	@Autowired
	public void setAllocationReadPlatformService(AllocationReadPlatformService allocationReadPlatformService) {
		this.allocationReadPlatformService = allocationReadPlatformService;
	}

	@Autowired
	public void setFromApiJsonDeserializer(OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
	}

	@Autowired
	public void setConfigurationRepository(ConfigurationRepository configurationRepository) {
		this.configurationRepository = configurationRepository;
	}

	@Autowired
	public void setPromotionCodeRepository(PromotionCodeRepository promotionCodeRepository) {
		this.promotionCodeRepository = promotionCodeRepository;
	}

	@Autowired
	public void setHardwareAssociationReadplatformService(
			HardwareAssociationReadplatformService hardwareAssociationReadplatformService) {
		this.hardwareAssociationReadplatformService = hardwareAssociationReadplatformService;
	}
	
	@Autowired
	public void setChargeCodeRepository(ChargeCodeRepository chargeCodeRepository) {
		this.chargeCodeRepository = chargeCodeRepository;
	}

	@Autowired
	public void setOrderPriceRepository(OrderPriceRepository orderPriceRepository) {
		this.orderPriceRepository = orderPriceRepository;
	}

	@Autowired
	public void setEventActionRepository(EventActionRepository eventActionRepository) {
		this.eventActionRepository = eventActionRepository;
	}

	@Autowired
	public void setOrderHistoryRepository(OrderHistoryRepository orderHistoryRepository) {
		this.orderHistoryRepository = orderHistoryRepository;
	}

	@Autowired
	public void setAccountIdentifierGeneratorFactory(AccountNumberGeneratorFactory accountIdentifierGeneratorFactory) {
		this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
	}

	@Autowired
	public void setRecurringBillingRepositoryWrapper(RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper) {
		this.recurringBillingRepositoryWrapper = recurringBillingRepositoryWrapper;
	}

	@Autowired
	public void setContractRepository(ContractRepository contractRepository) {
		this.contractRepository = contractRepository;
	}
  
	@Autowired
	public void setInvoiceClient(InvoiceClient invoiceClient) {
		this.invoiceClient = invoiceClient;
	}

	@Autowired
	public void setFromJsonHelper(FromJsonHelper fromJsonHelper) {
		this.fromJsonHelper = fromJsonHelper;
	}

	@Autowired
	public void setPlanReadPlatformService(PlanReadPlatformService planReadPlatformService) {
		this.planReadPlatformService = planReadPlatformService;
	}

	@Autowired
	public void setProcessRequestRepository(ProcessRequestRepository processRequestRepository) {
		this.processRequestRepository = processRequestRepository;
	}

	@Autowired
	public void setJdbcTemplate(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Autowired
	public void setCodeReadPlatformService(MCodeReadPlatformService codeReadPlatformService) {
		this.codeReadPlatformService = codeReadPlatformService;
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
	public void setTicketMasterApiResource(TicketMasterApiResource ticketMasterApiResource) {
		this.ticketMasterApiResource = ticketMasterApiResource;
	}*/


	@Override
	public CommandProcessingResult createOrder(Long clientId, JsonCommand command) {

		try {
			
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			String serialnum = command.stringValueOfParameterNamed("serialnumber");
			String allocationType = command.stringValueOfParameterNamed("allocation_type");
			final Long userId = getUserId();

			this.checkingContractPeriodAndBillfrequncyValidation(command.longValueOfParameterNamed("contractPeriod"),
					command.stringValueOfParameterNamed("paytermCode"));

			// Check for Custome_Validation
			this.eventValidationReadPlatformService.checkForCustomValidations(clientId, EventActionConstants.EVENT_CREATE_ORDER, command.json(), userId);

			Plan plan = this.findPlanWithNotFoundDetection(command.longValueOfParameterNamed("planCode"));
			Order order = this.orderAssembler.assembleOrderDetails(command, clientId, plan);
			this.orderRepository.save(order);

			boolean isNewPlan = command.booleanPrimitiveValueOfParameterNamed("isNewplan");
			String requstStatus = UserActionStatusTypeEnum.ACTIVATION.toString();

			if (isNewPlan) {

				final AccountNumberGenerator orderNoGenerator = this.accountIdentifierGeneratorFactory.determineClientAccountNoGenerator(order.getId());
				order.updateOrderNum(orderNoGenerator.generate());
				Set<PlanDetails> planDetails = plan.getDetails();
				ServiceMaster service = this.serviceMasterRepository.findOneByServiceCode(planDetails.iterator().next().getServiceCode());
				Long commandId = Long.valueOf(0);

				if (service != null && service.isAuto() == 'Y' && !plan.getProvisionSystem().equalsIgnoreCase("None")) {
					CommandProcessingResult processingResult = this.prepareRequestWriteplatformService.prepareNewRequest(order, plan, requstStatus);
					commandId = processingResult.commandId();
				}

				// For Order History
				OrderHistory orderHistory = new OrderHistory(order.getId(), DateUtils.getLocalDateOfTenant(), DateUtils.getLocalDateOfTenant(), commandId, requstStatus, userId, null);
				this.orderHistoryRepository.save(orderHistory);
			}

			// For Plan And HardWare Association
			Configuration configurationProperty = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_IMPLICIT_ASSOCIATION);

			if(configurationProperty.isEnabled() && isNewPlan &&  StringUtils.isEmpty(serialnum) ){
				
				if(plan.isHardwareReq() == 'Y'){
					
					List<AllocationDetailsData> allocationDetailsDatas=this.allocationReadPlatformService.retrieveHardWareDetailsByItemCode(clientId,plan.getPlanCode());
					if(allocationDetailsDatas.size() == 1 ){
						
						this.associationWriteplatformService.createNewHardwareAssociation(clientId,plan.getId(),allocationDetailsDatas.get(0).getSerialNo(),
								order.getId(),allocationDetailsDatas.get(0).getAllocationType(),null);
					}else if(allocationDetailsDatas.isEmpty()){//plan and hardware mapping not exist's
					configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SERVICE_DEVICE_MAPPING);
					   if(configurationProperty != null && configurationProperty.isEnabled()){
						   List<OrderLine> orderServices=order.getServices();
						   for(OrderLine service:orderServices){
							   List<AllocationDetailsData> allocationDetails=this.allocationReadPlatformService.retrieveHardWareDetailsByServiceMap(clientId,service.getServiceId());
							   if(!allocationDetails.isEmpty() ){
									this.associationWriteplatformService.createNewHardwareAssociation(clientId,plan.getId(),allocationDetails.get(0).getSerialNo(),
											order.getId(),allocationDetails.get(0).getAllocationType(),service.getServiceId());
								}
						   }
					   }
					}
				}

			} else if (configurationProperty.isEnabled() && isNewPlan && StringUtils.isNotBlank(serialnum)) {

				this.associationWriteplatformService.createNewHardwareAssociation(clientId, plan.getId(), serialnum, order.getId(), allocationType,null);
			}

			if(plan.getProvisionSystem().equalsIgnoreCase("None")){
				
				Client client=this.clientRepository.findOne(clientId);
				client.setStatus(ClientStatus.ACTIVE.getValue());
				this.clientRepository.save(client);
				List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_ORDER);
				
				if(actionDetaislDatas.size() != 0){
					this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,clientId, order.getId().toString(),null);
				}
			}

			//processNotifyMessages(EventActionConstants.EVENT_CREATE_ORDER, clientId, order.getId().toString(), null);
			if (isNewPlan) {
				processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, clientId, order.getId().toString(), "ACTIVATION");
			}
			
			if (!plan.getProvisionSystem().equalsIgnoreCase("None")) {
				List<ActionDetaislData> clientOrderActionDetails = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_ORDER);
				if (clientOrderActionDetails.size() != 0) {
					/**
					 *Here isNewPlan added only for u-mee client,bcz he don't wants to create a ticket when changeplan.
					 *And this isNewPlan will check the condition in EventActions for TicketCreation when OrderBooking.
					 */
					if(isNewPlan){
						this.actiondetailsWritePlatformService.AddNewActions(clientOrderActionDetails, clientId, order.getId().toString(), String.valueOf(isNewPlan));
					}
				}
			}
			
			this.orderRepository.save(order);
			return new CommandProcessingResult(order.getId(),order.getClientId());	
		} catch (JSONException | DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	

	@Override
	public void processNotifyMessages(String eventName, Long clientId, String orderId, String actionType) {

		List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(eventName);

		if (actionDetaislDatas.size() != 0) {
			this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, clientId, orderId, actionType);
		}
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final Exception dve) {
		
		throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue","Unknown data integrity issue with resource.");
	}

	
	private Order retrieveOrderById(Long orderId) {
		final Order order = this.orderRepository.findOne(orderId);
		if (order == null) {
			throw new NoOrdersFoundException(orderId);
		}
		return order;
	}

	@Override
	public CommandProcessingResult deleteOrder(final Long orderId, final JsonCommand command) {

		final Long userId = context.authenticatedUser().getId();
		final Order order = this.retrieveOrderById(orderId);
		List<OrderLine> orderline = order.getServices();
		List<OrderPrice> orderPrices = order.getPrice();
		final Plan plan = this.findPlanWithNotFoundDetection(order.getPlanId());
		if (!plan.getProvisionSystem().equalsIgnoreCase("None")) {
			List<Long> prepareIds = this.prepareRequestReadplatformService.getPrepareRequestDetails(orderId);
			if (prepareIds.isEmpty()) {
				throw new PrepareRequestActivationException();
			}
			for (Long id : prepareIds) {
				PrepareRequest prepareRequest = this.prepareRequsetRepository.findOne(id);
				prepareRequest.setCancelStatus("CANCEL");
				this.prepareRequsetRepository.save(prepareRequest);
			}
		}
		for (OrderPrice price : orderPrices) {
			price.delete();
		}
		for (OrderLine orderData : orderline) {
			orderData.delete();
		}
		order.delete();
		this.orderRepository.save(order);

		// For Order History
		OrderHistory orderHistory = new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(), 
				null, "CANCELLED", userId,null);
		this.orderHistoryRepository.save(orderHistory);
		
		return new CommandProcessingResult(order.getId(),order.getClientId());
	}

	@Override
	public CommandProcessingResult disconnectOrder(final JsonCommand command, final Long orderId) {

		try {
			
			this.fromApiJsonDeserializer.validateForDisconnectOrder(command.json());
			Order order = this.retrieveOrderById(orderId);
			final LocalDate disconnectionDate = command.localDateValueOfParameterNamed("disconnectionDate");
			final LocalDate monthEndDate = disconnectionDate.dayOfMonth().withMaximumValue();
			LocalDate invoiceTillDate = new LocalDate(order.getPrice().get(0).getInvoiceTillDate());
			//Get List of secondary connection's(Multi Room Tv Connection)
			List<Long> secondaryConnections = new ArrayList<Long>();
			if(ConnectionTypeEnum.PRIMARY.toString().equalsIgnoreCase(order.getConnectionType())){
			    secondaryConnections=this.orderAssembler.checkDisconnectOrderChilds(order.getId(),order.getClientId(),order.getPlanId());
			}
			final Configuration configurationProperty = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);
			for (OrderPrice price : order.getPrice()) {
				   price.updateDates(disconnectionDate);
			}
			final Plan plan = this.findPlanWithNotFoundDetection(order.getPlanId());
			Long orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();

			if ("None".equalsIgnoreCase(plan.getProvisionSystem())) {
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.DISCONNECTED).getId();
			} 
			if (configurationProperty.isEnabled() && plan.isPrepaid() == 'N') {
				if (plan.getBillRule() != 400 && plan.getBillRule() != 300 && 
						(!disconnectionDate.equals(monthEndDate) && !disconnectionDate.equals(invoiceTillDate))) {
				 // This Condition Added only for Freemium Plan (Only If Condition added but Method(reverseInvoiceServices) is there Previously)
				  if(disconnectionDate.toDate().after(order.getStartDate()) || disconnectionDate.toDate().compareTo(order.getStartDate()) == 0)
					this.reverseInvoice.reverseInvoiceServices(orderId,order.getClientId(), disconnectionDate);
				}
			}
			order.update(command, orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.DISCONNECTION.toString());
			this.orderRepository.saveAndFlush(order);

			Long processingResultId = Long.valueOf(0);
			// Update Client Status
			if ("None".equalsIgnoreCase(plan.getProvisionSystem())) {
				final Long activeOrders = this.orderReadPlatformService.retrieveClientActiveOrderDetails(order.getClientId(),null);
				if (activeOrders == 0) {
					Client client = this.clientRepository.findOne(order.getClientId());
					client.setStatus(ClientStatus.DEACTIVE.getValue());
					this.clientRepository.saveAndFlush(client);
				}
				processNotifyMessages(EventActionConstants.EVENT_DISCONNECTION_ORDER,order.getClientId(), order.getId().toString(), null);
			} else {

				CommandProcessingResult processingResult = this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getPlanCode(),
						UserActionStatusTypeEnum.DISCONNECTION.toString(), processingResultId, null,null, order.getId(), plan.getProvisionSystem(),null,null);
				processingResultId = processingResult.commandId();
			}

			// checking for Paypal Recurring DisConnection
			processPaypalRecurringActions(orderId,	EventActionConstants.EVENT_PAYPAL_RECURRING_TERMINATE_ORDER);
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM,order.getClientId(), order.getId().toString(),"DISCONNECTION");

			// For Order History
		    OrderHistory orderHistory = new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(), 
					processingResultId,UserActionStatusTypeEnum.DISCONNECTION.toString(), getUserId(), null);
			this.orderHistoryRepository.save(orderHistory);
			
			if (secondaryConnections.size() != 0) {
				for(Long disconnectId:secondaryConnections){
				    this.disconnectSecondaryConnections(command,disconnectId);
				}
			}
			
			/**
			 * AddOn disconnect code
			 */
			
			List<OrderAddons> orderAddOnsDatas = this.orderAddonsRepository.findAddonsByOrderId(order.getId());
			
			if (!orderAddOnsDatas.isEmpty()) {
				
				for (OrderAddons orderAddons : orderAddOnsDatas) {
					
					if (orderAddons.getStatus().equalsIgnoreCase(StatusTypeEnum.ACTIVE.toString())){
						
						this.orderAddOnsWritePlatformService.disconnectOrderAddon(null, orderAddons.getId());
					}
					/*if(orderAddons.getStatus().equalsIgnoreCase(StatusTypeEnum.PENDING.toString())){
						
						this.orderAddOnsWritePlatformService.cancelOrderAddon(null, orderAddons.getId());

					}*/
				}
			}

        return  new CommandProcessingResult(Long.valueOf(order.getId()),order.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}
	
	private void disconnectSecondaryConnections(final JsonCommand command,final Long orderId) {
		
		this.disconnectOrder(command, orderId);
	}
	
	@Override
	public CommandProcessingResult orderTermination(final JsonCommand command, final Long orderId) {

		try {
			
			final AppUser appUser = this.context.authenticatedUser();
			final Order order = this.retrieveOrderById(orderId);
			Long resourceId = Long.valueOf(0);
			Long orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.TERMINATED).getId();
			Plan plan = this.findPlanWithNotFoundDetection(order.getPlanId());
			
			//Get List of secondary connection's(Multi Room Tv Connection)
			List<Long> secondaryConnections = new ArrayList<Long>();
			if (ConnectionTypeEnum.PRIMARY.toString().equalsIgnoreCase(order.getConnectionType())) {
				 secondaryConnections = this.orderAssembler.checkTerminateOrderChilds(order.getClientId(),order.getPlanId());
			}

			if (!"None".equalsIgnoreCase(plan.getProvisionSystem())) {

				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
				CommandProcessingResult processingResult = this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(),
								UserActionStatusTypeEnum.TERMINATION.toString(),resourceId, null, null, order.getId(), plan.getProvisionSystem(), null,null);
				resourceId = processingResult.resourceId();
			}
			order.setStatus(orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.TERMINATION.toString());
			this.orderRepository.saveAndFlush(order);
            
			//For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(), DateUtils.getLocalDateOfTenant(), DateUtils.getLocalDateOfTenant(), resourceId,
					UserActionStatusTypeEnum.TERMINATION.toString(), appUser.getId(), null);
			this.orderHistoryRepository.save(orderHistory);

			// checking for Paypal Recurring DisConnection
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_ORDER_TERMINATE, order.getClientId(), order.getId().toString(), null);
			processPaypalRecurringActions(orderId,EventActionConstants.EVENT_PAYPAL_RECURRING_TERMINATE_ORDER);

			if (secondaryConnections.size() != 0 && ConnectionTypeEnum.PRIMARY.toString().equalsIgnoreCase(order.getConnectionType())) {
				for (Long terminateId : secondaryConnections) {
					this.terminateSecondaryConnections(command, terminateId);
				}
			}
			
			if ("None".equalsIgnoreCase(plan.getProvisionSystem()))
				processNotifyMessages(EventActionConstants.TERMINATION_FEE, order.getClientId(),order.getId().toString(), null);
			
			return new CommandProcessingResult(orderId, order.getClientId());

		} catch (DataIntegrityViolationException exception) {
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(new Long(-1));
		}
	}

	
	private void terminateSecondaryConnections(final JsonCommand command,final Long orderId) {

		this.orderTermination(command, orderId);
	}
	

	@Override
	public CommandProcessingResult orderSuspention(final JsonCommand command,final Long orderId) {

		try {

			this.fromApiJsonDeserializer.validateForOrderSuspension(command.json());
			final Order order = this.retrieveOrderById(orderId);
			Long resourceId = Long.valueOf(0);
			Long orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.SUSPENDED).getId();
			final Plan plan = this.findPlanWithNotFoundDetection(order.getPlanId());
			//Get List of secondary connection's(Multi Room Tv Connection)
			List<Long> secondaryConnections = new ArrayList<Long>();
			if (ConnectionTypeEnum.PRIMARY.toString().equalsIgnoreCase(order.getConnectionType())) {
				 secondaryConnections = this.orderAssembler.checkSuspentionOrderChilds(order.getClientId(),order.getPlanId());
			}

			if (!"None".equalsIgnoreCase(plan.getProvisionSystem())) {
				
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
				CommandProcessingResult commandProcessingResult = this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(),
								UserActionStatusTypeEnum.SUSPENSION.toString(), resourceId, null, null,order.getId(), plan.getProvisionSystem(), null,null);
				resourceId = commandProcessingResult.resourceId();

			}
			order.setStatus(orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.SUSPENSION.toString());

			// Post Details in Payment followup
			final PaymentFollowup paymentFollowup = PaymentFollowup.fromJson(command, order.getClientId(), order.getId(),StatusTypeEnum.ACTIVE.toString(),StatusTypeEnum.SUSPENDED.toString());
			this.paymentFollowupRepository.save(paymentFollowup);

			this.orderRepository.save(order);
			
			//For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),resourceId,
					UserActionStatusTypeEnum.SUSPENSION.toString(),getUserId(), null);
			this.orderHistoryRepository.save(orderHistory);
			
			// checking for Paypal Recurring Disconnection
			processPaypalRecurringActions(orderId,EventActionConstants.EVENT_PAYPAL_RECURRING_DISCONNECT_ORDER);
			
			if (secondaryConnections.size() != 0 && ConnectionTypeEnum.PRIMARY.toString().equalsIgnoreCase(order.getConnectionType())) {
				for (Long suspendOrderId : secondaryConnections) {
					this.suspendSecondaryConnections(command, suspendOrderId);
				}
			}

			return new CommandProcessingResult(orderId, order.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	private void suspendSecondaryConnections(final JsonCommand command,final Long orderId) {
                
		this.orderSuspention(command, orderId);
	}

	@Override
	public CommandProcessingResult reactiveOrder(final JsonCommand command,final Long orderId) {

		try {
			
			Order order = this.retrieveOrderById(orderId);
			Long resourceId = Long.valueOf(0);
			Long orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId();
			final Plan plan = this.findPlanWithNotFoundDetection(order.getPlanId());
			//Get List of secondary connection's(Multi Room Tv Connection)
			List<Long> secondaryConnections = new ArrayList<Long>();
			if (!ConnectionTypeEnum.REGULAR.toString().equalsIgnoreCase(order.getConnectionType())) {
				secondaryConnections=this.orderAssembler.checkReactiveOrderChilds(order.getId(),order.getClientId(),order.getPlanId());
			}

			if (!"None".equalsIgnoreCase(plan.getProvisionSystem())) {
				
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
				CommandProcessingResult commandProcessingResult = this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(),
								UserActionStatusTypeEnum.REACTIVATION.toString(), resourceId, null, null,order.getId(), plan.getProvisionSystem(), null, null);
				resourceId = commandProcessingResult.resourceId();

			} 
			order.setStatus(orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.REACTIVATION.toString());
			PaymentFollowup paymentFollowup = this.paymentFollowupRepository.findOneByorderId(order.getId());

			if (paymentFollowup != null) {
				paymentFollowup.setReactiveDate(DateUtils.getDateOfTenant());
				this.paymentFollowupRepository.save(paymentFollowup);
			}

			this.orderRepository.saveAndFlush(order);
			
			//For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(), resourceId,
					UserActionStatusTypeEnum.REACTIVATION.toString(),getUserId(), null);
			this.orderHistoryRepository.save(orderHistory);

			// checking for Paypal Recurring Reconnection
			processPaypalRecurringActions(orderId,EventActionConstants.EVENT_PAYPAL_RECURRING_RECONNECTION_ORDER);
			
			if (secondaryConnections.size() != 0 && ConnectionTypeEnum.PRIMARY.toString().equalsIgnoreCase(order.getConnectionType())) {
				secondaryConnections.remove(order.getId());//Remove Primary Order
				for (Long reactiveOrderId : secondaryConnections) {
					this.reactiveSecondaryConnections(command, reactiveOrderId);
				}
			}
			return new CommandProcessingResult(orderId, order.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	private void reactiveSecondaryConnections(final JsonCommand command,final Long orderId) {
        
		this.reactiveOrder(command, orderId);
	}
	
	@Override
	public CommandProcessingResult reconnectOrder(final Long orderId) {

		try {
			
			final Order order = this.retrieveOrderById(orderId);
			final LocalDate startDate = DateUtils.getLocalDateOfTenant();
			final Plan plan = this.findPlanWithNotFoundDetection(order.getPlanId());
			Long processingResultId = Long.valueOf(0);
			Long orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId();
			//Get List of secondary connection's(Multi Room Tv Connection)
			List<Long> secondaryConnections = new ArrayList<Long>();
			if(!ConnectionTypeEnum.REGULAR.toString().equalsIgnoreCase(order.getConnectionType())){
				secondaryConnections = this.orderAssembler.checkReconnectOrderChilds(order.getId(),order.getClientId(), order.getPlanId());
			}
			List<SubscriptionData> subscriptionDatas = this.contractPeriodReadPlatformService.retrieveSubscriptionDatabyOrder(orderId);
			/*Contract contractPeriod = this.subscriptionRepository.findOne(subscriptionDatas.get(0).getId());*/		
			//End Date
			LocalDate EndDate = this.orderAssembler.calculateEndDate(startDate,subscriptionDatas.get(0).getSubscriptionType(),subscriptionDatas.get(0).getUnits());
			order.setStartDate(startDate);
			order.setEndDate(EndDate);
			order.setNextBillableDay(null);
			for (OrderPrice price : order.getPrice()) {
				if (price.isAddon() == 'N') {
					price.setBillStartDate(startDate);
					if ("NRC".equalsIgnoreCase(price.getChargeType())) {
						price.setBillEndDate(startDate);
					} else {
						price.setBillEndDate(EndDate);
					}
					price.setNextBillableDay(null);
					price.setInvoiceTillDate(null);
				}
			}

			if (!"None".equalsIgnoreCase(plan.getProvisionSystem())) {
				
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
				CommandProcessingResult processingResult = this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getPlanCode(), 
						UserActionStatusTypeEnum.RECONNECTION.toString(),Long.valueOf(0), null, null, order.getId(),plan.getProvisionSystem(), null, null);
				processingResultId = processingResult.commandId();
			} else {

				final Client client = this.clientRepository.findOne(order.getClientId());
				client.setStatus(ClientStatus.ACTIVE.getValue());
				this.clientRepository.save(client);
				processNotifyMessages(EventActionConstants.EVENT_RECONNECTION_ORDER,order.getClientId(), order.getId().toString(), null);

			}
			order.setStatus(orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.RECONNECTION.toString());
			this.orderRepository.saveAndFlush(order);

			// For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(), processingResultId,
					UserActionStatusTypeEnum.RECONNECTION.toString(), getUserId(), null);
			this.orderHistoryRepository.save(orderHistory);

			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM,order.getClientId(), order.getId().toString(),"RECONNECTION");
			
			if (secondaryConnections.size() != 0 && ConnectionTypeEnum.PRIMARY.toString().equalsIgnoreCase(order.getConnectionType())) {
				 secondaryConnections.remove(order.getId());// Remove primary Order
				for (Long reconnectId : secondaryConnections) {
					this.reconnectSecondaryConnections(reconnectId);
				}
			}

			return new CommandProcessingResult(order.getId(),order.getClientId());

		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void reconnectSecondaryConnections(final Long orderId) {

		this.reconnectOrder(orderId);

	}
	
	@Override
	public CommandProcessingResult changePlan(final JsonCommand command, final Long entityId) {

		try {
			
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			this.checkingContractPeriodAndBillfrequncyValidation(command.longValueOfParameterNamed("contractPeriod"), 
					command.stringValueOfParameterNamed("paytermCode"));
			final Order oldOrder = this.retrieveOrderById(entityId);
			//Get List of secondary connection's for change plan (Multi Room Tv Connection)
			List<Long> secondaryConnections = new ArrayList<Long>();
			if (!ConnectionTypeEnum.REGULAR.toString().equalsIgnoreCase(oldOrder.getConnectionType())) {
				secondaryConnections=this.orderAssembler.checkChangeOrderChilds(entityId,oldOrder.getClientId(),oldOrder.getPlanId());
			}
			oldOrder.updateDisconnectionstate();
			this.orderRepository.save(oldOrder);

			Configuration changePlanAlignDateConfiguration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_CHANGE_PLAN_ALIGN_DATES);
			if (!changePlanAlignDateConfiguration.isEnabled()) {
				Configuration dcConfiguration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);

				if (dcConfiguration.isEnabled()) {
					final Plan oldPlan = this.findPlanWithNotFoundDetection(oldOrder.getPlanId());
					if (oldPlan.isPrepaid() == 'N'&& oldPlan.getBillRule() != 400 && oldPlan.getBillRule() != 300) {
					//This Condition Added only for Freemium Plan (Only If Condition added but Method(reverseInvoiceServices) is there Previously)
					 if(DateUtils.getLocalDateOfTenant().toDate().after(oldOrder.getStartDate()) || DateUtils.getLocalDateOfTenant().toDate().compareTo(oldOrder.getStartDate()) == 0)
						this.reverseInvoice.reverseInvoiceServices(oldOrder.getId(), oldOrder.getClientId(),DateUtils.getLocalDateOfTenant());
					}
				}
			}
			
			List<HardwareAssociation> associations=this.associationRepository.findOneByOrderAndClient(oldOrder.getId(),oldOrder.getClientId());
			
			if(!associations.isEmpty()){
			  for(HardwareAssociation association:associations)	{
				association.delete();
				this.associationRepository.saveAndFlush(association);
			  }
			}

			CommandProcessingResult result = this.createOrder(oldOrder.getClientId(), command);
			final Order newOrder = this.orderRepository.findOne(result.resourceId());

			newOrder.updateOrderNum(oldOrder.getOrderNo());
			newOrder.updateActivationDate(oldOrder.getActiveDate());
			
			Configuration  isDiscountsOnChangePlan = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_DISCOUNTS_APPLY_ON_CHANGEPLAN);
			if(isDiscountsOnChangePlan !=null && isDiscountsOnChangePlan.isEnabled()){
				for(OrderDiscount oldOrderDiscount:oldOrder.getOrderDiscount()) {
				     if(oldOrderDiscount.getDiscountEndDate() !=null && oldOrderDiscount.getDiscountEndDate().after(newOrder.getStartDate())
				    		 &&  newOrder.getOrderDiscount().size() > 0){
				    	    newOrder.getOrderDiscount().get(0).updateDates(oldOrderDiscount.getDiscountRate(), oldOrderDiscount.getDiscountType(), 
				    	    		new LocalDate(oldOrderDiscount.getDiscountEndDate()), new LocalDate(newOrder.getStartDate()), oldOrderDiscount.getDiscountCode());
				    	    newOrder.getOrderDiscount().get(0).setDiscountId(oldOrderDiscount.getDiscountId()); 
				    	    break;
				     }
				  }
			}
			
			List<OrderAddons> addons = this.orderAddonsRepository.findAddonsByOrderId(oldOrder.getId());
			
			List<OrderData> orderCounts = this.orderReadPlatformService.clientActiveOrderDetails(oldOrder.getClientId());
			Plan plan = this.planRepository.findOne(oldOrder.getPlanId());
		
			if (!addons.isEmpty() && "u-mee sync server".equalsIgnoreCase(plan.getProvisionSystem())){
				for (OrderData orderCount : orderCounts){
					PlanCodeData newPlan = this.orderReadPlatformService.getPlanDetails(newOrder.getPlanId());
					OrderAddonsData talkAddons = this.orderReadPlatformService.retrieveTalkAddons(oldOrder.getClientId(), orderCount.getPdid());
					if("u-mee plus".equalsIgnoreCase(newPlan.getPlanCode())){
				
					if(null!=talkAddons){ 
						
					if (("Talk".equalsIgnoreCase(talkAddons.getPlanName())) && ("Talk app H".equalsIgnoreCase(talkAddons.getServiceName()))){
						
						   List<PlanData>  planAddonDatas = this.planReadPlatformService.findTalkPlanAddons(orderCount.getPdid());
						   for (PlanData planAddonData : planAddonDatas){
							   if(!talkAddons.getServiceName().equalsIgnoreCase(planAddonData.getServiceName())){
								   
								   List<OrderAddons> orderTalkAddons = this.orderAddonsRepository.findTalkAddonsByOrderId(talkAddons.getId());
								   for (OrderAddons orderTalkAddon : orderTalkAddons){
									   orderTalkAddon.updateTalkAddonstate();
									   //orderTalkAddon.isDeleted();
									   OrderPrice orderTalkPrice = this.orderPriceRepository.findOne(orderTalkAddon.getPriceId());
									   orderTalkPrice.delete();
									   this.orderPriceRepository.saveAndFlush(orderTalkPrice);
									   this.orderAddonsRepository.saveAndFlush(orderTalkAddon);
									   JSONObject jsonChild = new JSONObject();
									   jsonChild.put("serviceId", planAddonData.getServiceId());
										jsonChild.put("chargeCodeId",planAddonData.getChargeCodeId());
										jsonChild.put("price", planAddonData.getPrice());
										jsonChild.put("locale", "en");
				
										JSONArray addonServices = new JSONArray();
										addonServices.add(jsonChild.toString());
				
										JSONObject json = new JSONObject();
										json.put("planName", planAddonData.getPlanCode());
										json.put("contractId", talkAddons.getContractId());
										json.put("addonServices", addonServices);
										json.put("locale", "en");
										json.put("oldOrderId", orderCount.getId());
										json.put("dateFormat", "dd MMMM yyyy");
										DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
										json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
										json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
										
										this.orderAddonsApiResource.addOrderAddonServices(talkAddons.getId(), json.toString());
										
										OrderPriceData data = this.orderReadPlatformService.findNewTalkAddonsPriceByTalkOrderId(orderCount.getId());
										OrderPrice newTalkAddonPrice = this.orderPriceRepository.findOne(data.getId());
										newTalkAddonPrice.updateBillingDates(orderTalkPrice.getInvoiceTillDate(),orderTalkPrice.getNextBillableDay());
										this.orderPriceRepository.saveAndFlush(newTalkAddonPrice);
								   }
							   }
						   }
						   
						}
					else if (("Talk".equalsIgnoreCase(talkAddons.getPlanName())) && ("Talk app P".equalsIgnoreCase(talkAddons.getServiceName()))){
						
						   List<PlanData>  planAddonDatas = this.planReadPlatformService.findTalkPlanAddons(orderCount.getPdid());
						   for (PlanData planAddonData : planAddonDatas){
							   if(talkAddons.getServiceName().equalsIgnoreCase(planAddonData.getServiceName())){
								   
								   List<OrderAddons> orderTalkAddons = this.orderAddonsRepository.findTalkAddonsByOrderId(talkAddons.getId());
								   for (OrderAddons orderTalkAddon : orderTalkAddons){
									   orderTalkAddon.updateTalkAddonstate();
									   //orderTalkAddon.isDeleted();
									   OrderPrice orderTalkPrice = this.orderPriceRepository.findOne(orderTalkAddon.getPriceId());
									   orderTalkPrice.delete();
									   this.orderPriceRepository.saveAndFlush(orderTalkPrice);
									   this.orderAddonsRepository.saveAndFlush(orderTalkAddon);
									   JSONObject jsonChild = new JSONObject();
									   jsonChild.put("serviceId", planAddonData.getServiceId());
										jsonChild.put("chargeCodeId",planAddonData.getChargeCodeId());
										jsonChild.put("price", planAddonData.getPrice());
										jsonChild.put("locale", "en");
				
										JSONArray addonServices = new JSONArray();
										addonServices.add(jsonChild.toString());
				
										JSONObject json = new JSONObject();
										json.put("planName", planAddonData.getPlanCode());
										json.put("contractId", talkAddons.getContractId());
										json.put("addonServices", addonServices);
										json.put("locale", "en");
										json.put("oldOrderId", orderCount.getId());
										json.put("dateFormat", "dd MMMM yyyy");
										DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
										json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
										json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
										
										this.orderAddonsApiResource.addOrderAddonServices(talkAddons.getId(), json.toString());
										
										OrderPriceData data = this.orderReadPlatformService.findNewTalkAddonsPriceByTalkOrderId(orderCount.getId());
										OrderPrice newTalkAddonPrice = this.orderPriceRepository.findOne(data.getId());
										newTalkAddonPrice.updateBillingDates(orderTalkPrice.getInvoiceTillDate(),orderTalkPrice.getNextBillableDay());
										this.orderPriceRepository.saveAndFlush(newTalkAddonPrice);
								   }
							   }
						   }
						   
						}
					  }
					}
					else if("u-mee home".equalsIgnoreCase(newPlan.getPlanCode()) || "u-mee fam1".equalsIgnoreCase(newPlan.getPlanCode()) ||"u-mee fam2".equalsIgnoreCase(newPlan.getPlanCode())){
						//OrderAddonsData talkAddons = this.orderReadPlatformService.retrieveTalkAddons(oldOrder.getClientId(), orderCount.getPdid());
						
						if(null!=talkAddons){
							
						if (("Talk".equalsIgnoreCase(talkAddons.getPlanName())) && ("Talk app H".equalsIgnoreCase(talkAddons.getServiceName()))){
							
							   List<PlanData>  planAddonDatas = this.planReadPlatformService.findTalkPlanAddons(orderCount.getPdid());
							   for (PlanData planAddonData : planAddonDatas){
								   if(talkAddons.getServiceName().equalsIgnoreCase(planAddonData.getServiceName())){
									   
									   List<OrderAddons> orderTalkAddons = this.orderAddonsRepository.findTalkAddonsByOrderId(talkAddons.getId());
									   for (OrderAddons orderTalkAddon : orderTalkAddons){
										   orderTalkAddon.updateTalkAddonstate();
										   //orderTalkAddon.isDeleted();
										   OrderPrice orderTalkPrice = this.orderPriceRepository.findOne(orderTalkAddon.getPriceId());
										   orderTalkPrice.delete();
										   this.orderPriceRepository.saveAndFlush(orderTalkPrice);
										   this.orderAddonsRepository.saveAndFlush(orderTalkAddon);
										   JSONObject jsonChild = new JSONObject();
										   jsonChild.put("serviceId", planAddonData.getServiceId());
											jsonChild.put("chargeCodeId",planAddonData.getChargeCodeId());
											jsonChild.put("price", planAddonData.getPrice());
											jsonChild.put("locale", "en");
					
											JSONArray addonServices = new JSONArray();
											addonServices.add(jsonChild.toString());
					
											JSONObject json = new JSONObject();
											json.put("planName", planAddonData.getPlanCode());
											json.put("contractId", talkAddons.getContractId());
											json.put("addonServices", addonServices);
											json.put("locale", "en");
											json.put("oldOrderId", orderCount.getId());
											json.put("dateFormat", "dd MMMM yyyy");
											DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
											json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
											json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
											
											this.orderAddonsApiResource.addOrderAddonServices(talkAddons.getId(), json.toString());
											
											OrderPriceData data = this.orderReadPlatformService.findNewTalkAddonsPriceByTalkOrderId(orderCount.getId());
											OrderPrice newTalkAddonPrice = this.orderPriceRepository.findOne(data.getId());
											newTalkAddonPrice.updateBillingDates(orderTalkPrice.getInvoiceTillDate(),orderTalkPrice.getNextBillableDay());
											this.orderPriceRepository.saveAndFlush(newTalkAddonPrice);
									   }
								   }
							   }
							   
							}
						else if (("Talk".equalsIgnoreCase(talkAddons.getPlanName())) && ("Talk app P".equalsIgnoreCase(talkAddons.getServiceName()))){
							
							   List<PlanData>  planAddonDatas = this.planReadPlatformService.findTalkPlanAddons(orderCount.getPdid());
							   for (PlanData planAddonData : planAddonDatas){
								   if(!talkAddons.getServiceName().equalsIgnoreCase(planAddonData.getServiceName())){
									   
									   List<OrderAddons> orderTalkAddons = this.orderAddonsRepository.findTalkAddonsByOrderId(talkAddons.getId());
									   for (OrderAddons orderTalkAddon : orderTalkAddons){
										   orderTalkAddon.updateTalkAddonstate();
										   //orderTalkAddon.isDeleted();
										   OrderPrice orderTalkPrice = this.orderPriceRepository.findOne(orderTalkAddon.getPriceId());
										   orderTalkPrice.delete();
										   this.orderPriceRepository.saveAndFlush(orderTalkPrice);
										   this.orderAddonsRepository.saveAndFlush(orderTalkAddon);
										   JSONObject jsonChild = new JSONObject();
										   jsonChild.put("serviceId", planAddonData.getServiceId());
											jsonChild.put("chargeCodeId",planAddonData.getChargeCodeId());
											jsonChild.put("price", planAddonData.getPrice());
											jsonChild.put("locale", "en");
					
											JSONArray addonServices = new JSONArray();
											addonServices.add(jsonChild.toString());
					
											JSONObject json = new JSONObject();
											json.put("planName", planAddonData.getPlanCode());
											json.put("contractId", talkAddons.getContractId());
											json.put("addonServices", addonServices);
											json.put("locale", "en");
											json.put("oldOrderId", orderCount.getId());
											json.put("dateFormat", "dd MMMM yyyy");
											DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
											json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
											json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
											
											this.orderAddonsApiResource.addOrderAddonServices(talkAddons.getId(), json.toString());
											
											OrderPriceData data = this.orderReadPlatformService.findNewTalkAddonsPriceByTalkOrderId(orderCount.getId());
											OrderPrice newTalkAddonPrice = this.orderPriceRepository.findOne(data.getId());
											newTalkAddonPrice.updateBillingDates(orderTalkPrice.getInvoiceTillDate(),orderTalkPrice.getNextBillableDay());
											this.orderPriceRepository.saveAndFlush(newTalkAddonPrice);
									   }
								   }
							   }
							   
							}
						  }
						
					}
				}
				
				for (OrderAddons orderAddons : addons) {
					   //orderAddons.setOrderId(newOrder.getId());
					   orderAddons.updateDisconnectionstate();
					   OrderPrice orderPrice = this.orderPriceRepository.findOne(orderAddons.getPriceId());
					   orderPrice.delete();
					   if('N' == oldOrder.getPrice().get(0).isIsDeleted()){
					   OrderPrice oldOrderPrice = this.orderPriceRepository.findOneByOldOrder(oldOrder.getPrice().get(0).getId());
					   oldOrderPrice.delete();
					   }
					   //orderPrice.update(newOrder);
					   this.orderRepository.save(newOrder);
					   this.orderPriceRepository.saveAndFlush(orderPrice);
					   this.orderAddonsRepository.save(orderAddons);
					   
					   List<OrderAddonsData> addonDatas = this.orderReadPlatformService.getNewPlanAddon(newOrder.getPlanId());
					   
					for (OrderAddonsData addonData : addonDatas) {
						if (orderAddons.getServiceId() == addonData.getServiceId()) {
							JSONObject jsonChild = new JSONObject();
							jsonChild.put("serviceId", addonData.getServiceId());
							jsonChild.put("chargeCodeId",addonData.getChargeCodeId());
							jsonChild.put("price", addonData.getPrice());
							jsonChild.put("locale", "en");
	
							JSONArray addonServices = new JSONArray();
							addonServices.add(jsonChild.toString());
	
							JSONObject json = new JSONObject();
							json.put("planName", addonData.getPlanName());
							json.put("contractId", orderAddons.getContractId());
							json.put("addonServices", addonServices);
							json.put("locale", "en");
							json.put("oldOrderId", oldOrder.getId());
							json.put("dateFormat", "dd MMMM yyyy");
							DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
							json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
							json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
	
							this.orderAddonsApiResource.addOrderAddonServices(newOrder.getId(), json.toString());
							
							IsExDirectory isExDirectory = this.isExDirectoryRepository.findOneByOrderId(orderAddons.getOrderId());
							/*   isExDirectory.setOrderId(newOrder.getId());
							   isExDirectory.setPlanId(newOrder.getPlanId());
							   
							   this.isExDirectoryRepository.saveAndFlush(isExDirectory);
							*/
							//change for change plan issue
							if(isExDirectory != null){
								   isExDirectory.setOrderId(newOrder.getId());
								   isExDirectory.setPlanId(newOrder.getPlanId());
								   this.isExDirectoryRepository.saveAndFlush(isExDirectory);
								}
							
							OrderAddonsData data = this.orderReadPlatformService.findNewAddonsPriceByOrderId(newOrder.getId());
							OrderPrice newAddonPrice = this.orderPriceRepository.findOne(data.getPriceId());
							newAddonPrice.updateBillingDates(orderPrice.getInvoiceTillDate(),orderPrice.getNextBillableDay());
							this.orderPriceRepository.saveAndFlush(newAddonPrice);
						}
					} 
					   
				}
			}  else if("u-mee sync server".equalsIgnoreCase(plan.getProvisionSystem())){
				
				for (OrderData orderCount : orderCounts){
					PlanCodeData newPlan = this.orderReadPlatformService.getPlanDetails(newOrder.getPlanId());
					OrderAddonsData talkAddons = this.orderReadPlatformService.retrieveTalkAddons(oldOrder.getClientId(), orderCount.getPdid());
					if("u-mee plus".equalsIgnoreCase(newPlan.getPlanCode())){
				
					if(null!=talkAddons){ 
						
					if (("Talk".equalsIgnoreCase(talkAddons.getPlanName())) && ("Talk app H".equalsIgnoreCase(talkAddons.getServiceName()))){
						
						   List<PlanData>  planAddonDatas = this.planReadPlatformService.findTalkPlanAddons(orderCount.getPdid());
						   for (PlanData planAddonData : planAddonDatas){
							   if(!talkAddons.getServiceName().equalsIgnoreCase(planAddonData.getServiceName())){
								   
								   List<OrderAddons> orderTalkAddons = this.orderAddonsRepository.findTalkAddonsByOrderId(talkAddons.getId());
								   for (OrderAddons orderTalkAddon : orderTalkAddons){
									   orderTalkAddon.updateTalkAddonstate();
									   //orderTalkAddon.isDeleted();
									   OrderPrice orderTalkPrice = this.orderPriceRepository.findOne(orderTalkAddon.getPriceId());
									   orderTalkPrice.delete();
									   this.orderPriceRepository.saveAndFlush(orderTalkPrice);
									   this.orderAddonsRepository.saveAndFlush(orderTalkAddon);
									   JSONObject jsonChild = new JSONObject();
									   jsonChild.put("serviceId", planAddonData.getServiceId());
										jsonChild.put("chargeCodeId",planAddonData.getChargeCodeId());
										jsonChild.put("price", planAddonData.getPrice());
										jsonChild.put("locale", "en");
				
										JSONArray addonServices = new JSONArray();
										addonServices.add(jsonChild.toString());
				
										JSONObject json = new JSONObject();
										json.put("planName", planAddonData.getPlanCode());
										json.put("contractId", talkAddons.getContractId());
										json.put("addonServices", addonServices);
										json.put("locale", "en");
										json.put("oldOrderId", orderCount.getId());
										json.put("dateFormat", "dd MMMM yyyy");
										DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
										json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
										json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
										
										this.orderAddonsApiResource.addOrderAddonServices(talkAddons.getId(), json.toString());
										
										OrderPriceData data = this.orderReadPlatformService.findNewTalkAddonsPriceByTalkOrderId(orderCount.getId());
										OrderPrice newTalkAddonPrice = this.orderPriceRepository.findOne(data.getId());
										newTalkAddonPrice.updateBillingDates(orderTalkPrice.getInvoiceTillDate(),orderTalkPrice.getNextBillableDay());
										this.orderPriceRepository.saveAndFlush(newTalkAddonPrice);
								   }
							   }
						   }
						   
						}
					else if (("Talk".equalsIgnoreCase(talkAddons.getPlanName())) && ("Talk app P".equalsIgnoreCase(talkAddons.getServiceName()))){
						
						   List<PlanData>  planAddonDatas = this.planReadPlatformService.findTalkPlanAddons(orderCount.getPdid());
						   for (PlanData planAddonData : planAddonDatas){
							   if(talkAddons.getServiceName().equalsIgnoreCase(planAddonData.getServiceName())){
								   
								   List<OrderAddons> orderTalkAddons = this.orderAddonsRepository.findTalkAddonsByOrderId(talkAddons.getId());
								   for (OrderAddons orderTalkAddon : orderTalkAddons){
									   orderTalkAddon.updateTalkAddonstate();
									   //orderTalkAddon.isDeleted();
									   OrderPrice orderTalkPrice = this.orderPriceRepository.findOne(orderTalkAddon.getPriceId());
									   orderTalkPrice.delete();
									   this.orderPriceRepository.saveAndFlush(orderTalkPrice);
									   this.orderAddonsRepository.saveAndFlush(orderTalkAddon);
									   JSONObject jsonChild = new JSONObject();
									   jsonChild.put("serviceId", planAddonData.getServiceId());
										jsonChild.put("chargeCodeId",planAddonData.getChargeCodeId());
										jsonChild.put("price", planAddonData.getPrice());
										jsonChild.put("locale", "en");
				
										JSONArray addonServices = new JSONArray();
										addonServices.add(jsonChild.toString());
				
										JSONObject json = new JSONObject();
										json.put("planName", planAddonData.getPlanCode());
										json.put("contractId", talkAddons.getContractId());
										json.put("addonServices", addonServices);
										json.put("locale", "en");
										json.put("oldOrderId", orderCount.getId());
										json.put("dateFormat", "dd MMMM yyyy");
										DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
										json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
										json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
										
										this.orderAddonsApiResource.addOrderAddonServices(talkAddons.getId(), json.toString());
										
										OrderPriceData data = this.orderReadPlatformService.findNewTalkAddonsPriceByTalkOrderId(orderCount.getId());
										OrderPrice newTalkAddonPrice = this.orderPriceRepository.findOne(data.getId());
										newTalkAddonPrice.updateBillingDates(orderTalkPrice.getInvoiceTillDate(),orderTalkPrice.getNextBillableDay());
										this.orderPriceRepository.saveAndFlush(newTalkAddonPrice);
								   }
							   }
						   }
						   
						}
					  }
					}
					else if("u-mee home".equalsIgnoreCase(newPlan.getPlanCode()) || "u-mee fam1".equalsIgnoreCase(newPlan.getPlanCode()) ||"u-mee fam2".equalsIgnoreCase(newPlan.getPlanCode())){
						//OrderAddonsData talkAddons = this.orderReadPlatformService.retrieveTalkAddons(oldOrder.getClientId(), orderCount.getPdid());
						
						if(null!=talkAddons){
							
						if (("Talk".equalsIgnoreCase(talkAddons.getPlanName())) && ("Talk app H".equalsIgnoreCase(talkAddons.getServiceName()))){
							
							   List<PlanData>  planAddonDatas = this.planReadPlatformService.findTalkPlanAddons(orderCount.getPdid());
							   for (PlanData planAddonData : planAddonDatas){
								   if(talkAddons.getServiceName().equalsIgnoreCase(planAddonData.getServiceName())){
									   
									   List<OrderAddons> orderTalkAddons = this.orderAddonsRepository.findTalkAddonsByOrderId(talkAddons.getId());
									   for (OrderAddons orderTalkAddon : orderTalkAddons){
										   orderTalkAddon.updateTalkAddonstate();
										   //orderTalkAddon.isDeleted();
										   OrderPrice orderTalkPrice = this.orderPriceRepository.findOne(orderTalkAddon.getPriceId());
										   orderTalkPrice.delete();
										   this.orderPriceRepository.saveAndFlush(orderTalkPrice);
										   this.orderAddonsRepository.saveAndFlush(orderTalkAddon);
										   JSONObject jsonChild = new JSONObject();
										   jsonChild.put("serviceId", planAddonData.getServiceId());
											jsonChild.put("chargeCodeId",planAddonData.getChargeCodeId());
											jsonChild.put("price", planAddonData.getPrice());
											jsonChild.put("locale", "en");
					
											JSONArray addonServices = new JSONArray();
											addonServices.add(jsonChild.toString());
					
											JSONObject json = new JSONObject();
											json.put("planName", planAddonData.getPlanCode());
											json.put("contractId", talkAddons.getContractId());
											json.put("addonServices", addonServices);
											json.put("locale", "en");
											json.put("oldOrderId", orderCount.getId());
											json.put("dateFormat", "dd MMMM yyyy");
											DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
											json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
											json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
											
											this.orderAddonsApiResource.addOrderAddonServices(talkAddons.getId(), json.toString());
											
											OrderPriceData data = this.orderReadPlatformService.findNewTalkAddonsPriceByTalkOrderId(orderCount.getId());
											OrderPrice newTalkAddonPrice = this.orderPriceRepository.findOne(data.getId());
											newTalkAddonPrice.updateBillingDates(orderTalkPrice.getInvoiceTillDate(),orderTalkPrice.getNextBillableDay());
											this.orderPriceRepository.saveAndFlush(newTalkAddonPrice);
									   }
								   }
							   }
							   
							}
						else if (("Talk".equalsIgnoreCase(talkAddons.getPlanName())) && ("Talk app P".equalsIgnoreCase(talkAddons.getServiceName()))){
							
							   List<PlanData>  planAddonDatas = this.planReadPlatformService.findTalkPlanAddons(orderCount.getPdid());
							   for (PlanData planAddonData : planAddonDatas){
								   if(!talkAddons.getServiceName().equalsIgnoreCase(planAddonData.getServiceName())){
									   
									   List<OrderAddons> orderTalkAddons = this.orderAddonsRepository.findTalkAddonsByOrderId(talkAddons.getId());
									   for (OrderAddons orderTalkAddon : orderTalkAddons){
										   orderTalkAddon.updateTalkAddonstate();
										   //orderTalkAddon.isDeleted();
										   OrderPrice orderTalkPrice = this.orderPriceRepository.findOne(orderTalkAddon.getPriceId());
										   orderTalkPrice.delete();
										   this.orderPriceRepository.saveAndFlush(orderTalkPrice);
										   this.orderAddonsRepository.saveAndFlush(orderTalkAddon);
										   JSONObject jsonChild = new JSONObject();
										   jsonChild.put("serviceId", planAddonData.getServiceId());
											jsonChild.put("chargeCodeId",planAddonData.getChargeCodeId());
											jsonChild.put("price", planAddonData.getPrice());
											jsonChild.put("locale", "en");
					
											JSONArray addonServices = new JSONArray();
											addonServices.add(jsonChild.toString());
					
											JSONObject json = new JSONObject();
											json.put("planName", planAddonData.getPlanCode());
											json.put("contractId", talkAddons.getContractId());
											json.put("addonServices", addonServices);
											json.put("locale", "en");
											json.put("oldOrderId", orderCount.getId());
											json.put("dateFormat", "dd MMMM yyyy");
											DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
											json.put("startDate", formatter.print(DateUtils.getLocalDateOfTenant()));
											json.put("addOnChangePlan", UserActionStatusTypeEnum.CHANGE_PLAN.toString());
											
											this.orderAddonsApiResource.addOrderAddonServices(talkAddons.getId(), json.toString());
											
											OrderPriceData data = this.orderReadPlatformService.findNewTalkAddonsPriceByTalkOrderId(orderCount.getId());
											OrderPrice newTalkAddonPrice = this.orderPriceRepository.findOne(data.getId());
											newTalkAddonPrice.updateBillingDates(orderTalkPrice.getInvoiceTillDate(),orderTalkPrice.getNextBillableDay());
											this.orderPriceRepository.saveAndFlush(newTalkAddonPrice);
									   }
								   }
							   }
							   
							}
						  }
						
					}
				}
				
			}

			if (changePlanAlignDateConfiguration !=null && changePlanAlignDateConfiguration.isEnabled()) {

				for (OrderPrice orderPrice : newOrder.getPrice()) {
					orderPrice.setInvoiceTillDate(oldOrder.getPrice().get(0).getInvoiceTillDate());
					orderPrice.setNextBillableDay(oldOrder.getPrice().get(0).getNextBillableDay());
				}
			}

			newOrder.setuserAction(UserActionStatusTypeEnum.CHANGE_PLAN.toString());
			this.orderRepository.save(newOrder);

			Plan newPlan = this.findPlanWithNotFoundDetection(newOrder.getPlanId());
			Long processResuiltId = new Long(0);
			
			
			Configuration configurationProperty = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_IMPLICIT_ASSOCIATION);

				
				if(newPlan.isHardwareReq() == 'Y'){
					
					List<AllocationDetailsData> allocationDetailsDatas=this.allocationReadPlatformService.retrieveHardWareDetailsByItemCode(newOrder.getClientId(),newPlan.getPlanCode());
					if(!allocationDetailsDatas.isEmpty() ){
						
						this.associationWriteplatformService.createNewHardwareAssociation(newOrder.getClientId(),newPlan.getId(),allocationDetailsDatas.get(0).getSerialNo(),
								newOrder.getId(),allocationDetailsDatas.get(0).getAllocationType(),null);
					}else if(allocationDetailsDatas.isEmpty()){//plan and hardware mapping not exist's
					configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SERVICE_DEVICE_MAPPING);
					   if(configurationProperty != null && configurationProperty.isEnabled()){
						   List<OrderLine> orderServices=newOrder.getServices();
						   for(OrderLine service:orderServices){
							   List<AllocationDetailsData> allocationDetails=this.allocationReadPlatformService.retrieveHardWareDetailsByServiceMap(newOrder.getClientId(),service.getServiceId());
							   if(!allocationDetails.isEmpty() ){
									this.associationWriteplatformService.createNewHardwareAssociation(newOrder.getClientId(),newPlan.getId(),allocationDetails.get(0).getSerialNo(),
											newOrder.getId(),allocationDetails.get(0).getAllocationType(),service.getServiceId());
								}
						   }
					   }
					}
				}

            /*//Pairing New order with old order mac 
			if (!associations.isEmpty()) {
				this.associationWriteplatformService.createNewHardwareAssociation(newOrder.getClientId(),newPlan.getId(), associations.get(0).getSerialNo(),
								newOrder.getId(),associations.get(0).getAllocationType(),null);

			}*/
			

			if (!"None".equalsIgnoreCase(newPlan.getProvisionSystem())) {
				
				CommandProcessingResult processingResult = this.provisioningWritePlatformService.postOrderDetailsForProvisioning(newOrder, newPlan.getCode(),
								UserActionStatusTypeEnum.CHANGE_PLAN.toString(), new Long(0), null, null, newOrder.getId(), newPlan.getProvisionSystem(), null, null);

				processResuiltId = processingResult.commandId();
			} else {
				// Notify details for change plan
				processNotifyMessages(EventActionConstants.EVENT_CHANGE_PLAN, newOrder.getClientId(), newOrder.getId().toString(), null);
			}
     
			// For old order History
			OrderHistory orderHistory=new OrderHistory(oldOrder.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),processResuiltId,					
					UserActionStatusTypeEnum.CHANGE_PLAN.toString(),getUserId(),null);
			this.orderHistoryRepository.save(orderHistory);
			
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, newOrder.getClientId(), newOrder.getId().toString(), "CHANGE PLAN");
			
			if (secondaryConnections.size() != 0 && ConnectionTypeEnum.PRIMARY.toString().equalsIgnoreCase(oldOrder.getConnectionType())) {
				secondaryConnections.remove(oldOrder.getId());// Remove primary old Order
				for (Long changeOrderId : secondaryConnections) {
					this.changePlanOnSecondaryConnections(command,changeOrderId);
				}
			}
			
			//Sending Email when change plan
			List<ActionDetaislData> clientOrderActionDetails = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CHANGE_ORDER);
			if(clientOrderActionDetails.size()!=0){
			   this.actiondetailsWritePlatformService.AddNewActions(clientOrderActionDetails,newOrder.getClientId(), newOrder.getId().toString(), null);
			}
			
			return new CommandProcessingResult(result.resourceId(),oldOrder.getClientId());

		} catch (DataIntegrityViolationException exception) {
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(Long.valueOf(-1L));
		}catch (JSONException e) {
			e.printStackTrace();
			return new CommandProcessingResult(Long.valueOf(-1l));
		}

	}

	private void changePlanOnSecondaryConnections(final JsonCommand command,final Long changeOrderId) {
		
	      this.changePlan(command, changeOrderId);
	}


	@Override
	public CommandProcessingResult renewalClientOrder(final JsonCommand command, final Long orderId) {

		try {

			LocalDate newStartdate = null;
			String requstStatus = null;
			String requestStatusForProv = null;
			final Long userId = getUserId();
			this.fromApiJsonDeserializer.validateForRenewalOrder(command.json());
			Order orderDetails = this.retrieveOrderById(orderId);
			/*secondary connection's(Multi Room Tv Connection)
			if (ConnectionTypeEnum.SECONDARY.toString().equalsIgnoreCase(orderDetails.getConnectionType())) {
				 this.orderAssembler.checkRenewalOrderChilds(orderDetails.getId(),orderDetails.getClientId(),orderDetails.getPlanId(),orderDetails.getStatus());
			}*/
			final Long contractPeriod = command.longValueOfParameterNamed("renewalPeriod");
			final String description = command.stringValueOfParameterNamed("description");
			final Contract contractDetails = this.contractRepository.findOne(contractPeriod);
			if (contractDetails == null) {
				throw new ContractNotNullException();
			}
			List<ChargeCodeMaster> chargeCodeMaster = chargeCodeRepository.findOneByBillFrequency(orderDetails.getBillingFrequency());
			/*
			 * Integer chargeCodeDuration = chargeCodeMaster.get(0).getChargeDuration();
			 * if (chargeCodeDuration > contract.getUnits().intValue()) { 
			 * throw new ChargeCodeAndContractPeriodException(chargeCodeMaster.get(0).getBillFrequencyCode(), "Renewal"); }
			 */
			this.eventValidationReadPlatformService.checkForCustomValidations(orderDetails.getClientId(),EventActionConstants.EVENT_ORDER_RENEWAL, command.json(),userId);
			List<OrderPrice> orderPrices = orderDetails.getPrice();
			final Plan plan = this.findPlanWithNotFoundDetection(orderDetails.getPlanId());
			 /*Contract contractDetails = this.subscriptionRepository.findOne(contractPeriod);
			 chargeCodeMaster = chargeCodeRepository.findOneByBillFrequency(orderDetails.getBillingFrequency());
			 Integer chargeCodeDuration = chargeCodeMaster.get(0).getChargeDuration();*/
			LocalDate contractEndDate = this.orderAssembler.calculateEndDate(DateUtils.getLocalDateOfTenant(), contractDetails.getSubscriptionType(), contractDetails.getUnits());
			LocalDate chargeCodeEndDate = this.orderAssembler.calculateEndDate(DateUtils.getLocalDateOfTenant(), chargeCodeMaster.get(0).getDurationType(), chargeCodeMaster.get(0).getChargeDuration().longValue());
			if (contractEndDate != null && chargeCodeEndDate != null) {
				if (contractEndDate.toDate().before(chargeCodeEndDate.toDate())) {
					if (plan.isPrepaid() == 'N' || plan.isPrepaid() == 'n') {
						throw new ChargeCodeAndContractPeriodException(chargeCodeMaster.get(0).getBillFrequencyCode(), contractDetails.getSubscriptionPeriod());
					} /*else {
						throw new ChargeCodeAndContractPeriodException(
								chargeCodeMaster.get(0).getBillFrequencyCode(),true);
					}*/
				}
			}

			if (orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())) {

				newStartdate = new LocalDate(orderDetails.getEndDate()).plusDays(1);
				requstStatus = UserActionStatusEnumaration.OrderStatusType(UserActionStatusTypeEnum.RENEWAL_BEFORE_AUTOEXIPIRY).getValue();

			} else if (orderDetails.getStatus().equals(StatusTypeEnum.DISCONNECTED.getValue().longValue())) {

				newStartdate = DateUtils.getLocalDateOfTenant();
				requstStatus = UserActionStatusEnumaration.OrderStatusType(UserActionStatusTypeEnum.RENEWAL_AFTER_AUTOEXIPIRY).getValue();

				if (!"None".equalsIgnoreCase(plan.getProvisionSystem())) {
					orderDetails.setStatus(StatusTypeEnum.PENDING.getValue().longValue());
				} else {
					orderDetails.setStatus(StatusTypeEnum.ACTIVE.getValue().longValue());
					Client client = this.clientRepository.findOne(orderDetails.getClientId());
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.clientRepository.saveAndFlush(client);
				}
				requestStatusForProv = "RENEWAL_AE";// UserActionStatusTypeEnum.ACTIVATION.toString();
				orderDetails.setNextBillableDay(null);
				orderDetails.setRenewalDate(newStartdate.toDate());
			}
			LocalDate renewalEndDate = this.orderAssembler.calculateEndDate(newStartdate, contractDetails.getSubscriptionType(), contractDetails.getUnits());

			Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_ALIGN_BILLING_CYCLE);

			if (configuration != null && configuration.isEnabled() && plan.isPrepaid() == 'N') {
				
				JSONObject configValue = new JSONObject(configuration.getValue());
				if (renewalEndDate != null && configValue.getBoolean("fixed")) {
					orderDetails.setBillingAlign('Y');
					orderDetails.setEndDate(renewalEndDate.dayOfMonth().withMaximumValue());
				} else if(renewalEndDate == null && configValue.getBoolean("perpetual")){
					orderDetails.setBillingAlign('Y');
					orderDetails.setEndDate(renewalEndDate);
				}else{
					orderDetails.setEndDate(renewalEndDate);
				}
			} else {
				orderDetails.setEndDate(renewalEndDate);
			}
			// orderDetails.setEndDate(renewalEndDate);
			orderDetails.setuserAction(requstStatus);
			
			//Secondary connection's Renewal always depends on primary connection.
			if (ConnectionTypeEnum.SECONDARY.toString().equalsIgnoreCase(orderDetails.getConnectionType())) {

				final Order primaryOrder = this.orderRepository.findOnePrimaryActiveOrderDetails(orderDetails.getClientId(), plan.getId());
				if (primaryOrder != null && primaryOrder.getEndDate() != null && orderDetails.getEndDate() != null) {
					//For disconnected secodary connection 
					if (orderDetails.getStartDate().equals(DateUtils.getLocalDateOfTenant().toDate())
							&& (orderDetails.getEndDate().after(primaryOrder.getEndDate()))){
						orderDetails.setEndDate(new LocalDate(primaryOrder.getEndDate()));
						
					}else if (orderDetails.getEndDate().after(primaryOrder.getEndDate())){
							
						throw new RenewalOrderException();
					}
				}

			}

			for (OrderPrice orderprice : orderPrices) {
				if (plan.isPrepaid() == 'Y' && orderprice.isAddon() == 'N') {
					final Long priceId = command.longValueOfParameterNamed("priceId");
					ServiceMaster service = this.serviceMasterRepository.findOne(orderprice.getServiceId());
					Price renewalPrice = this.priceRepository.findOne(priceId);
					Price price = this.priceRepository.findOneByPlanAndService(plan.getId(), service.getServiceCode(), contractDetails.getSubscriptionPeriod(),
							renewalPrice.getChargeCode(), renewalPrice.getPriceRegion());
					if (price != null ) {
						ChargeCodeMaster chargeCode = this.chargeCodeRepository.findOneByChargeCode(price.getChargeCode());
						orderprice.setChargeCode(chargeCode.getChargeCode());
						orderprice.setChargeDuration(chargeCode.getChargeDuration().toString());
						orderprice.setChargeType(chargeCode.getChargeType());
						orderprice.setChargeDurationType(chargeCode.getDurationType());
						if(price.getChargingVariant() == 0){ 
						 orderprice.setPrice(price.getPrice());
						 orderDetails.setConnectionType(ConnectionTypeEnum.REGULAR.toString());
						}

					} else {
						throw new PriceNotFoundException(priceId);
					}
				}
				 orderprice.setDatesOnOrderStatus(newStartdate, new LocalDate(orderDetails.getEndDate()), orderDetails.getUserAction());
				/*orderDetails.setNextBillableDay(null);*/
		  	}

			orderDetails.setContractPeriod(contractDetails.getId());
			orderDetails.setuserAction(requstStatus);
			this.orderRepository.saveAndFlush(orderDetails);

			// Set<PlanDetails> planDetails=plan.getDetails();
			// ServiceMaster  serviceMaster=this.serviceMasterRepository.findOneByServiceCode(planDetails.iterator().next().getServiceCode());
			Long resourceId = Long.valueOf(0);

			if (!"None".equalsIgnoreCase(plan.getProvisionSystem())) {
				// Prepare Provisioning Req
				CodeValue codeValue = this.codeValueRepository.findOneByCodeValue(plan.getProvisionSystem());

				if (codeValue.position() == 1&& orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())) {
					requestStatusForProv = "RENEWAL_BE";

				}
				if (requestStatusForProv != null) {
					CommandProcessingResult commandProcessingResult = this.provisioningWritePlatformService.postOrderDetailsForProvisioning(orderDetails,
									plan.getPlanCode(), requestStatusForProv, Long.valueOf(0), null, null, orderDetails.getId(), plan.getProvisionSystem(), null, null);
									resourceId = commandProcessingResult.resourceId();
				}

			} else {
				processNotifyMessages(EventActionConstants.EVENT_RECONNECTION_ORDER, orderDetails.getClientId(), orderId.toString(), null);
			}

			// For Order History
			OrderHistory orderHistory = new OrderHistory(orderDetails.getId(), DateUtils.getLocalDateOfTenant(), newStartdate, resourceId, requstStatus, userId, description);
			this.orderHistoryRepository.saveAndFlush(orderHistory);

			// Auto renewal with invoice process for Topup orders

			if (plan.isPrepaid() == 'Y'&& orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())) {

				Invoice invoice = this.invoiceClient.singleOrderInvoice(orderDetails.getId(), orderDetails.getClientId(), newStartdate.plusDays(1));

				if (invoice != null) {
					List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_TOPUP_INVOICE_MAIL);
					if (actionDetaislDatas.size() != 0) {
						this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, orderDetails.getClientId(), invoice.getId().toString(), null);
					}
				}
			}
			processNotifyMessages(EventActionConstants.EVENT_NOTIFY_TECHNICALTEAM, orderDetails.getClientId(), orderId.toString(), "RENEWAL");
			
			return new CommandProcessingResult(Long.valueOf(orderDetails.getClientId()), orderDetails.getClientId());

		} catch (JSONException | DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

/*
	private void renewalOnSecondaryConnections(final JsonCommand command,final Long renewalOrderId) {
		
		this.renewalClientOrder(command, renewalOrderId);
		
	}*/
	
	@Transactional
	@Override
	public CommandProcessingResult orderExtension(JsonCommand command, Long entityId) {

		try {

			Long userId = this.context.authenticatedUser().getId();
			Order order = this.orderRepository.findOne(entityId);
			String extensionperiod = command.stringValueOfParameterNamed("extensionPeriod");
			String extensionReason = command.stringValueOfParameterNamed("extensionReason");
			LocalDate newStartdate = new LocalDate(order.getEndDate());
			newStartdate = newStartdate.plusDays(1);
			String[] periodData = extensionperiod.split(" ");
			LocalDate endDate = this.orderAssembler.calculateEndDate(newStartdate, periodData[1], new Long(periodData[0]));
			List<OrderPrice> orderPrices = order.getPrice();
			Plan plan = this.findPlanWithNotFoundDetection(order.getPlanId());
			if (order.getStatus().intValue() == StatusTypeEnum.ACTIVE.getValue()) {
				order.setEndDate(endDate);
				for (OrderPrice orderprice : orderPrices) {
					orderprice.setBillEndDate(endDate);
					orderprice.setInvoiceTillDate(endDate.toDate());
					orderprice.setNextBillableDay(endDate.toDate());
					this.orderPriceRepository.save(orderprice);
				}
			} else if (order.getStatus().intValue() == StatusTypeEnum.DISCONNECTED.getValue()) {
				for (OrderPrice orderprice : orderPrices) {
					orderprice.setBillStartDate(newStartdate);
					orderprice.setBillEndDate(endDate);
					orderprice.setNextBillableDay(null);
					orderprice.setInvoiceTillDate(null);
					this.orderPriceRepository.save(orderprice);
				}
				if (plan.getProvisionSystem().equalsIgnoreCase("None")) {
					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					Client client = this.clientRepository.findOne(order.getClientId());
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.clientRepository.save(client);
				} else {
					// Check For HardwareAssociation
					AssociationData associationData = this.hardwareAssociationReadplatformService.retrieveSingleDetails(entityId);
					if (associationData == null) {
						throw new HardwareDetailsNotFoundException(entityId.toString());
					}
					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
				}
			}
			order.setEndDate(endDate);
			order.setuserAction(UserActionStatusTypeEnum.RECONNECTION.toString());
			this.orderRepository.save(order);

			// for Prepare Request
			String requstStatus = UserActionStatusTypeEnum.RECONNECTION.toString().toString();
			this.prepareRequestWriteplatformService.prepareNewRequest(order, plan, requstStatus);

			// For Order History
			SecurityContext context = SecurityContextHolder.getContext();
			if (context.getAuthentication() != null) {
				AppUser appUser = this.context.authenticatedUser();
				userId = appUser.getId();
			} else {
				userId = new Long(0);
			}

			// For Order History
			OrderHistory orderHistory = new OrderHistory(order.getId(), DateUtils.getLocalDateOfTenant(), DateUtils.getLocalDateOfTenant(), entityId,
					UserActionStatusTypeEnum.EXTENSION.toString(), userId, extensionReason);
			this.orderHistoryRepository.save(orderHistory);
			return new CommandProcessingResult(entityId, order.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(new Long(-1));

		}
	}
	
	@Override
	public CommandProcessingResult updateOrderPrice(final Long orderId, final JsonCommand command) {
		
		try {

			final Long userId = context.authenticatedUser().getId();
			this.fromApiJsonDeserializer.validateForUpdatePrice(command.json());
			final Order order = this.retrieveOrderById(orderId);
			final Long orderPriceId = command.longValueOfParameterNamed("priceId");
			final BigDecimal price = command.bigDecimalValueOfParameterNamed("price");
			
			final OrderPrice orderPrice = this.orderPriceRepository.findOne(orderPriceId);
			if (orderPrice != null) {
				orderPrice.setPrice(price);
				this.orderPriceRepository.save(orderPrice);
				// For Order History
				OrderHistory orderHistory = new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),
						DateUtils.getLocalDateOfTenant(), null, "UPDATE PRICE",userId, null);
				this.orderHistoryRepository.save(orderHistory);

			}

			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(order.getId()).withClientId(order.getClientId()).build();

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	
	@Override
	public CommandProcessingResult applyPromo(JsonCommand command) {
		try {
			this.context.authenticatedUser().getUsername();
			this.fromApiJsonDeserializer.validateForPromo(command.json());
			final Long promoId = command.longValueOfParameterNamed("promoId");
			final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
			PromotionCodeMaster promotion = this.promotionCodeRepository.findOne(promoId);

			if (promotion == null) {
				throw new PromotionCodeNotFoundException(promoId.toString());
			}
			Order order = this.orderRepository.findOne(command.entityId());
			List<OrderDiscount> orderDiscounts = order.getOrderDiscount();
			LocalDate enddate = this.orderAssembler.calculateEndDate(startDate,promotion.getDurationType(),promotion.getDuration());

			for (OrderDiscount orderDiscount : orderDiscounts) {
				orderDiscount.updateDates(promotion.getDiscountRate(),promotion.getDiscountType(), enddate, startDate, promotion.getPromotionCode());
			}
			this.orderRepository.save(order);
			return new CommandProcessingResult(command.entityId(),order.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return null;
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult scheduleOrderCreation(Long clientId,JsonCommand command) {

		try {
			String actionType = command.stringValueOfParameterNamed("actionType");
			if (!actionType.equalsIgnoreCase("renewalorder")) {
				this.fromApiJsonDeserializer.validateForCreate(command.json());
			}
			LocalDate startDate = command.localDateValueOfParameterNamed("start_date");

			char status = 'N';
			if (command.hasParameter("status")) {
				status = command.stringValueOfParameterNamed("status").trim().charAt(0);
			}

			EventAction eventAction = null;
			JSONObject jsonObject = new JSONObject();
			Long userId = getUserId();

			if (actionType.equalsIgnoreCase("renewalorder")) {

				// Check for Custome_Validation
				this.eventValidationReadPlatformService.checkForCustomValidations(clientId, EventActionConstants.EVENT_ORDER_RENEWAL, command.json(), userId);

				jsonObject.put("renewalPeriod", command.longValueOfParameterNamed("renewalPeriod"));
				jsonObject.put("description", command.stringValueOfParameterNamed("description"));

				eventAction = new EventAction(DateUtils.getLocalDateOfTenant().toDate(), "RENEWAL", "ORDER", EventActionConstants.ACTION_RENEWAL,
						"/orders/renewalorder/" + clientId, clientId, command.json(), null, clientId);

			} else if (actionType.equalsIgnoreCase("changeorder")) {
				//Check for Custome_Validation
				this.eventValidationReadPlatformService.checkForCustomValidations(clientId,EventActionConstants.EVENT_CHANGE_ORDER,command.json(),userId);
						Long orderId = command.longValueOfParameterNamed("orderId");
			    	  	jsonObject.put("billAlign",command.booleanPrimitiveValueOfParameterNamed("billAlign"));
			    	  	jsonObject.put("contractPeriod",command.longValueOfParameterNamed("contractPeriod"));
			    	  	jsonObject.put("dateFormat",command.booleanPrimitiveValueOfParameterNamed("dateFormat"));
			    	  	jsonObject.put("locale",command.booleanPrimitiveValueOfParameterNamed("locale"));
			    	  	jsonObject.put("isNewPlan",command.booleanPrimitiveValueOfParameterNamed("isNewPlan"));
			    	  	jsonObject.put("paytermCode",command.stringValueOfParameterNamed("paytermCode"));
			    	  	jsonObject.put("planCode",command.longValueOfParameterNamed("planCode"));
			    	  	jsonObject.put("start_date",command.stringValueOfParameterNamed("start_date"));
			    	  	jsonObject.put("disconnectionDate",command.stringValueOfParameterNamed("disconnectionDate"));
			    	  	jsonObject.put("disconnectReason",command.stringValueOfParameterNamed("disconnectReason"));
		        	   
		        	    eventAction=new EventAction(startDate.toDate(), "CHANGEPLAN", "ORDER",EventActionConstants.ACTION_CHNAGE_PLAN,"/orders/changPlan/"+orderId, 
		        			  clientId,command.json(),orderId,clientId);

			} else {

				// Check for Custome_Validation
				this.eventValidationReadPlatformService.checkForCustomValidations(clientId, EventActionConstants.EVENT_CREATE_ORDER, command.json(), userId);
			
				//Check for Active Orders	
		    	 /* Long activeorderId=this.orderReadPlatformService.retrieveClientActiveOrderDetails(clientId,null);
		    	/*  Long activeorderId=this.orderReadPlatformService.retrieveClientActiveOrderDetails(clientId,null);
	>>>>>>> upstream/obsplatform-3.0
	>>>>>>> obsplatform-3.0
		    	  	if(activeorderId !=null && activeorderId !=0){
		    	  		Order order=this.orderRepository.findOne(activeorderId);
					   		if(order.getEndDate() == null || !startDate.isAfter(new LocalDate(order.getEndDate()))){
					   			throw new SchedulerOrderFoundException(activeorderId);				   
					   		}
		    	  	}*/
				
				jsonObject.put("billAlign",command.booleanPrimitiveValueOfParameterNamed("billAlign"));
	    	  	jsonObject.put("contractPeriod",command.longValueOfParameterNamed("contractPeriod"));
	    	  	jsonObject.put("dateFormat","dd MMMM yyyy");
	    	  	jsonObject.put("locale","en");
	    	  	jsonObject.put("isNewPlan","true");
	    	  	jsonObject.put("paytermCode",command.stringValueOfParameterNamed("paytermCode"));
	    	  	jsonObject.put("planCode",command.longValueOfParameterNamed("planCode"));
	    	  	jsonObject.put("start_date",startDate.toDate());
	    	  	jsonObject.put("serialnumber", "");
        	   
        	    eventAction=new EventAction(startDate.toDate(), "CREATE", "ORDER",EventActionConstants.ACTION_NEW,"/orders/"+clientId, 
        			  clientId,command.json(),null,clientId);
			
				
			}

			eventAction.updateStatus(status);
			this.eventActionRepository.save(eventAction);
			return new CommandProcessingResult(command.entityId(), clientId);

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, null);
			return new CommandProcessingResult(Long.valueOf(-1));
		} catch (JSONException dve) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	@Override
	public CommandProcessingResult deleteSchedulingOrder(Long entityId, JsonCommand command) {

		try {
			this.context.authenticatedUser();
			EventAction eventAction = this.eventActionRepository.findOne(entityId);
			if (eventAction.IsProcessed() == 'Y') {
				throw new PrepareRequestActivationException();
			} else {
				eventAction.updateStatus('C');
				this.eventActionRepository.saveAndFlush(eventAction);
			}
			return new CommandProcessingResult(Long.valueOf(entityId), eventAction.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	public CommandProcessingResult scheduleOrderUpdation(Long entityId,JsonCommand command){
		
		try{
			
			String actionType = command.stringValueOfParameterNamed("actionType");
			 String serialnum =command.stringValueOfParameterNamed("serialnumber");
			 String allcation_type = command.stringValueOfParameterNamed("allcation_type");
			  if(!actionType.equalsIgnoreCase("renewalorder")){
				  if(serialnum.isEmpty()){
					  this.fromApiJsonDeserializer.validateForUpdate(command.json());
				  }
			  }
			  String startDate=command.stringValueOfParameterNamed("start_date");
			  
			  char status = 'N';
			  if(command.hasParameter("status")){
				  status = command.stringValueOfParameterNamed("status").trim().charAt(0);
			  }
			 
			  EventAction eventAction=this.eventActionRepository.findOne(entityId);
			  
			  JSONObject jsonObject=new JSONObject(eventAction.getCommandAsJson());
			  Long clientId= eventAction.getClientId();
			  this.eventValidationReadPlatformService.checkForCustomValidations(entityId,EventActionConstants.EVENT_CREATE_ORDER,command.json(),clientId);
			  if(!serialnum.isEmpty()){
				  jsonObject.remove("serialnumber");
				  jsonObject.put("serialnumber", serialnum);
				  jsonObject.put("allocation_type", allcation_type );
			  }
			  if(!startDate.isEmpty()){
				  jsonObject.remove("start_date");
				  jsonObject.put("start_date", startDate);
				  Date startDate1=command.DateValueOfParameterNamed("start_date");
				  eventAction.setTransDate(startDate1);
				 
			  }
      	      eventAction.setCommandAsJson(jsonObject.toString());
			  eventAction.updateStatus(status);
			  this.eventActionRepository.save(eventAction);
        	return  new CommandProcessingResult(command.entityId(),entityId);
	
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, null);
			return new CommandProcessingResult(Long.valueOf(-1));
		} catch (JSONException dve) {

			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}


	@Override
	public CommandProcessingResult retrackOsdMessage(final JsonCommand command) {
		try {
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForRetrack(command.json());
			String requstStatus = null;
			String message = null;
			final String commandName = command.stringValueOfParameterNamed("commandName");
			final Order order = this.orderRepository.findOne(command.entityId());
			
			if (order == null) {
				throw new NoOrdersFoundException(command.entityId());
			}
			final Plan plan = this.findPlanWithNotFoundDetection(order.getPlanId());
			if (commandName.equalsIgnoreCase("RETRACK")) {
				final String restrict = orderReadPlatformService.checkRetrackInterval(command.entityId());
				if (restrict != null && restrict.equalsIgnoreCase("yes")) {
					requstStatus = UserActionStatusTypeEnum.RETRACK.toString();
				} else {
					throw new PlatformDataIntegrityException("retrack.already.done", "retrack.already.done", "retrack.already.done");
				}
			} else if (commandName.equalsIgnoreCase("OSM")) {
				Client client = this.clientRepository.findOne(order.getClientId());
				requstStatus = UserActionStatusTypeEnum.MESSAGE.toString();
				message = "dear "+ client.getDisplayName() +"\n Your Plan "+plan.getDescription()+" will expire on "+order.getEndDate();
				final ProcessRequest processRequest=new ProcessRequest(0L,order.getClientId(),order.getId(),plan.getProvisionSystem(),requstStatus ,'N','N');
				final ProcessRequestDetails processRequestDetails = new
						  ProcessRequestDetails(0L,0L,message, "Recieved", null,order.getStartDate(), order.getEndDate(),null,null, 'N',requstStatus,null);
				 processRequest.add(processRequestDetails);					  
				 this.processRequestRepository.save(processRequest);
				 return new CommandProcessingResult(order.getId(), order.getClientId());
			
			} else if (commandName.equalsIgnoreCase("REBOOT")) {
				requstStatus = UserActionStatusTypeEnum.REBOOT.toString();
			}else if (commandName.equalsIgnoreCase("RELOAD")) {
				requstStatus = UserActionStatusTypeEnum.RELOAD.toString();
			}

			Long resourceId = Long.valueOf(0);
			if (requstStatus != null && plan != null) {

				CommandProcessingResult commandProcessingResult = this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getPlanCode(), requstStatus,
								Long.valueOf(0), null, null, order.getId(),plan.getProvisionSystem(), null, null);
								resourceId = commandProcessingResult.resourceId();
								
								

				/*
				 * final AllocationDetailsData detailsData =
				 * this.allocationReadPlatformService
				 * .getTheHardwareItemDetails(command.entityId()); final
				 * ProcessRequest processRequest=new
				 * ProcessRequest(Long.valueOf(
				 * 0),order.getClientId(),order.getId
				 * (),plan.getProvisionSystem(),requstStatus ,'N','N');
				 * processRequest.setNotify(); final List<OrderLine>
				 * orderLineData = order.getServices(); for (OrderLine orderLine
				 * : orderLineData) { String hardWareId = null; if (detailsData
				 * != null) { hardWareId = detailsData.getSerialNo(); } final
				 * List<ServiceMapping> provisionServiceDetails =
				 * this.provisionServiceDetailsRepository
				 * .findOneByServiceId(orderLine.getServiceId()); final
				 * ServiceMaster service =
				 * this.serviceMasterRepository.findOne(orderLine
				 * .getServiceId()); if (!provisionServiceDetails.isEmpty()) {
				 * if (message == null) { message =
				 * provisionServiceDetails.get(0).getServiceIdentification(); }
				 * final ProcessRequestDetails processRequestDetails = new
				 * ProcessRequestDetails(orderLine.getId(),
				 * orderLine.getServiceId(),message, "Recieved",
				 * hardWareId,order.getStartDate(), order.getEndDate(),
				 * null,null, 'N',requstStatus,service.getServiceType());
				 * 
				 * processRequest.add(processRequestDetails); } }
				 * this.processRequestRepository.save(processRequest);
				 */

				this.orderRepository.save(order);
				final OrderHistory orderHistory = new OrderHistory(order.getId(), DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(), resourceId, requstStatus, getUserId(), null);
				this.orderHistoryRepository.save(orderHistory);

			}
			return new CommandProcessingResult(order.getId(), order.getClientId());
		} catch (EmptyResultDataAccessException dve) {
			throw new PlatformDataIntegrityException("retrack.already.done", "retrack.already.done", "retrack.already.done");
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	
	private Long getUserId() {

		Long userId = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if (context.getAuthentication() != null) {
			AppUser appUser = this.context.authenticatedUser();
			userId = appUser.getId();
		} else {
			userId = new Long(0);
		}
		return userId;
	}

	private void processPaypalRecurringActions(Long orderId, String eventActionName) {

		// checking for Paypal Recurring DisConnection
		RecurringBilling billing = this.recurringBillingRepositoryWrapper.findOneByOrderId(orderId);

		if (null != billing && null != billing.getSubscriberId()) {

			List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(eventActionName);

			if (actionDetaislDatas.size() != 0) {
				this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, billing.getClientId(), orderId.toString(), null);
			}
		}
	}
	
	/*private void processReactiveOrderActions(Long orderId, Long clientId, String eventActionName) {

			List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(eventActionName);

			if (actionDetaislDatas.size() != 0) {
				this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, clientId, orderId.toString(), null);
			}
	}*/

	/*
	 * private void checkingContractPeriodAndBillfrequncyValidation(Long
	 * contractPeriod, String paytermCode){
	 * 
	 * 
	 * Contract contract = contractRepository.findOne(contractPeriod);
	 * List<ChargeCodeMaster> chargeCodeMaster =
	 * chargeCodeRepository.findOneByBillFrequency(paytermCode); Integer
	 * chargeCodeDuration = chargeCodeMaster.get(0).getChargeDuration();
	 * if(contract == null){ throw new ContractNotNullException(); }
	 * if(chargeCodeDuration > contract.getUnits().intValue()){ throw new
	 * ChargeCodeAndContractPeriodException(); }
	 * 
	 * }
	 */
	@Override
	public void checkingContractPeriodAndBillfrequncyValidation(Long contractPeriod, String paytermCode) {

		Contract contract = contractRepository.findOne(contractPeriod);
		List<ChargeCodeMaster> chargeCodeMaster = chargeCodeRepository.findOneByBillFrequency(paytermCode);
		// Integer chargeCodeDuration =
		// chargeCodeMaster.get(0).getChargeDuration();
		if (contract == null) {
			throw new ContractNotNullException();
		}
		LocalDate contractEndDate = this.orderAssembler.calculateEndDate(DateUtils.getLocalDateOfTenant(), contract.getSubscriptionType(), contract.getUnits());
		LocalDate chargeCodeEndDate = this.orderAssembler.calculateEndDate(DateUtils.getLocalDateOfTenant(), chargeCodeMaster.get(0).getDurationType(),
				chargeCodeMaster.get(0).getChargeDuration().longValue());
		if (contractEndDate != null && chargeCodeEndDate != null) {
			if (contractEndDate.toDate().before(chargeCodeEndDate.toDate())) {
				throw new ChargeCodeAndContractPeriodException();
			}
		}
	}
  
	@Override
	public Plan findPlanWithNotFoundDetection(final Long planId) {
		Plan plan = this.planRepository.findPlanCheckDeletedStatus(planId);

		if (plan == null) {
			throw new PlanNotFundException(planId);
		}
		return plan;
	}

	@Override
	public CommandProcessingResult renewalOrderWithClient(JsonCommand command, Long clientId) {

		try {

			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForOrderRenewalWithClient(command.json());
			CommandProcessingResult result;
			
			Long oldplanId = command.longValueOfParameterNamed("oldplanId");
			String contractPeriod = command.stringValueOfParameterNamed("duration");
			//Long orderId = command.longValueOfParameterNamed("orderId");
			Long newplanId = command.longValueOfParameterNamed("newplanId");
			Long planId;
			if(oldplanId == newplanId){
				planId = oldplanId;
			}else{
				planId = newplanId;
			}
			
			Plan  planData = this.planRepository.findOne(planId);
			if(planData == null){ throw new PlanNotFundException(planId);}
			
			String isPrepaid = planData.getIsPrepaid() == 'N' ? "postpaid" : "prepaid";
			
			Contract contract =this.contractRepository.findOneByContractId(contractPeriod);
			if(contract == null){
				throw new ContractPeriodNotFoundException(contractPeriod,clientId);
			}
			List<Long> orderIds = this.orderReadPlatformService.retrieveOrderActiveAndDisconnectionIds(clientId, planId);
			/*final List<OrderData> clientOrders = this.orderReadPlatformService.retrieveClientOrderDetails(clientId);
			Boolean flag = false;
			for(OrderData orders:clientOrders){
				if(orderId == Long.valueOf(orders.getOrderNo())){
					flag = true;
				}
			}
			if(!flag){
				throw new OrderNotFoundException(orderId);
			}*/
			if(orderIds.isEmpty()){
				//throw new NoOrdersFoundException(clientId,planId);
				
				List<PaytermData> datas  = this.orderReadPlatformService.getChargeCodes(planId,null);
				if(datas.size()==0){
					throw new BillingOrderNoRecordsFoundException(planId);
				}
				List<Long> oldOrderIds = this.orderReadPlatformService.retrieveOrderActiveAndDisconnectionIds(clientId, oldplanId);
				if(oldOrderIds.isEmpty()){
					throw new NoOrdersFoundException(clientId,oldplanId);
				}
				List<Long> isEventActionsAvailabel = this.orderReadPlatformService.getEventActionsData(clientId, oldOrderIds.get(0).longValue());
				if(!isEventActionsAvailabel.isEmpty()){
					throw new EventActionsAvailabeForRenewalWithChangePlanFound(clientId, oldOrderIds.get(0).longValue());
				}
				Order mainOrder = retrieveOrderById(oldOrderIds.get(0).longValue());
				final Order order= this.orderRepository.findOneOrderByOrderNO(mainOrder.getOrderNo());
		        if (order == null) { throw new NoOrdersFoundException(clientId.toString(),oldOrderIds.get(0).longValue()); }
		        
				LocalDate date = new LocalDate(order.getEndDate()).plusDays(1);
				DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMMM yyyy");
				
				JSONObject jsonObject = new JSONObject();
	    	  	jsonObject.put("billAlign","false");
	    	  	jsonObject.put("autoRenew","false");
	    	  	jsonObject.put("contractPeriod",contract.getId());
	    	  	jsonObject.put("dateFormat","dd MMMM yyyy");
	    	  	jsonObject.put("locale","en");
	    	  	jsonObject.put("isNewPlan","false");
	    	  	if(isPrepaid.equalsIgnoreCase("prepaid")){
	    	  		boolean flag = false;
	    	  	for(PaytermData data : datas){
					if(data.getDuration().equalsIgnoreCase(contractPeriod)){
						jsonObject.put("paytermCode",data.getPaytermtype());
						flag = true;
					}
				}
	    	  	if(!flag){
	    	  		throw new NoDurationFound(contractPeriod);
	    	  	}
	    	  	}
	    	  	jsonObject.put("planCode",planId);
	    	  	jsonObject.put("start_date",formatter.print(date));
	    	  	jsonObject.put("disconnectionDate",formatter.print(date));
	    	  	jsonObject.put("disconnectReason","Not Interested");
	    	  	jsonObject.put("actionType","changeorder");
	    	  	jsonObject.put("orderId",oldOrderIds.get(0).longValue());
	    	  	final JsonElement element = fromJsonHelper.parse(jsonObject.toString());
				JsonCommand changeCommandCommand = new JsonCommand(null,jsonObject.toString(), element, fromJsonHelper,
						null, null, null, null, null, null, null, null, null, null, 
						null, null);
				result = scheduleOrderCreation(clientId, changeCommandCommand);
				
			}else{
				
				List<SubscriptionData> subscriptionDatas = this.planReadPlatformService.retrieveSubscriptionData(orderIds.get(0), isPrepaid);
				if(subscriptionDatas.isEmpty()){
					throw new PriceNotFoundException(orderIds.get(0),clientId);
				}
				List<Long> isEventActionsAvailabel = this.orderReadPlatformService.getEventActionsData(clientId, orderIds.get(0));
				if(!isEventActionsAvailabel.isEmpty()){
					throw new EventActionsAvailabeForRenewalWithChangePlanFound(clientId, orderIds.get(0));
				}
				Long priceId = Long.valueOf(0);
				
				if(planData.getIsPrepaid() == 'Y'){
				  for(SubscriptionData subscriptionData : subscriptionDatas){
					if(subscriptionData.getContractdata().equalsIgnoreCase(contractPeriod)){
						priceId = subscriptionData.getPriceId();
						break;
					}
				}
			}

			if (planData.getIsPrepaid() == 'Y'&& priceId.equals(Long.valueOf(0))) {
				throw new ContractPeriodNotFoundException(contractPeriod,orderIds.get(0), clientId);
			}

			JSONObject renewalJson = new JSONObject();
			renewalJson.put("renewalPeriod", contract.getId());
			renewalJson.put("priceId", priceId);
			renewalJson.put("description", "Order renewal with clientId="+ clientId + " and planId=" + planId);
			final JsonElement element = fromJsonHelper.parse(renewalJson.toString());
			JsonCommand renewalCommand = new JsonCommand(null, renewalJson.toString(), element, fromJsonHelper, null,
					null, null, null, null, null, null, null, null, null, null,null);
	
			 result = this.renewalClientOrder(renewalCommand,orderIds.get(0));
			}
			return result;
		  }catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		} catch (JSONException e) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	@Transactional
	@Override
	public CommandProcessingResult deleteOrderWithNoRecord(final Long orderId, final JsonCommand command) {
		
		CommandProcessingResult  result = null;
		try {
		this.context.authenticatedUser();
		//final Long userId = context.authenticatedUser().getId();
		final Order order = this.retrieveOrderById(orderId);
		
		//HashMap<String, String> tables = new HashMap<>();
		String tables[]= {"b_association","b_order_line", "b_order_discount", "b_order_price", "b_orders_history", "b_orders"};
		
		for(int i=0; i<tables.length;  i++){
			if(tables[i].equalsIgnoreCase("b_orders")){
				String sql = "delete from "+tables[i]+ " where id ="+orderId;
				this.jdbcTemplate.execute(sql);
			}else{
				String sql1 ="delete from "+tables[i]+" where order_id ="+orderId;
				this.jdbcTemplate.execute(sql1);
			}
		}
		result = new CommandProcessingResult(order.getId(),order.getClientId());
		}catch (final SQLGrammarException e) {
			final Throwable realCause = e.getCause();
			final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
					dataValidationErrors).resource("orderdelete");
			if (realCause.getMessage().contains("Unknown table")) {
				baseDataValidator.reset().parameter("order table")
						.failWithCode("does.not.exist");
			}

			throwExceptionIfValidationWarningsExist(dataValidationErrors);
		}
		return result;
		
	}
	
	@Override
	public CommandProcessingResult newPasswordRequest(final JsonCommand command, final Long entityId) {

		try {

			final String planCode = command.stringValueOfParameterNamed("planCode");
			final Date startDate = command.DateValueOfParameterNamed("startDate");
			final String phoneNumber = command.stringValueOfParameterNamed("phoneNumber");
			final String hwSerialNo = command.stringValueOfParameterNamed("hwSerialNo");
			final String contractPeriod = command.stringValueOfParameterNamed("contractPeriod");
			final Order order = this.retrieveOrderById(entityId);
			
			ProvisionActions provisionActions = this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.REQUEST_NEW_PASSWORD);
			
			if(provisionActions != null && provisionActions.isEnable() == 'Y'){
				
				this.provisioningWritePlatformService.postPsswordForProvisioning(order, planCode, ProvisioningApiConstants.REQUEST_NEW_PASSWORD,
						order.getId(), startDate, phoneNumber, hwSerialNo, contractPeriod, provisionActions.getProvisioningSystem());
				
			}

			return new CommandProcessingResult(order.getClientId());

		} catch (DataIntegrityViolationException exception) {
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(Long.valueOf(-1L));
		}

	}
	
	@Override
	public CommandProcessingResult resetPasswordRequest(final JsonCommand command, final Long entityId) {

		try {

			final String planCode = command.stringValueOfParameterNamed("planCode");
			final Date startDate = command.DateValueOfParameterNamed("startDate");
			final String phoneNumber = command.stringValueOfParameterNamed("phoneNumber");
			final String hwSerialNo = command.stringValueOfParameterNamed("hwSerialNo");
			final String contractPeriod = command.stringValueOfParameterNamed("contractPeriod");
			final Order order = this.retrieveOrderById(entityId);
			
			ProvisionActions provisionActions = this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.REQUEST_RESET_PASSWORD);
			
			if(provisionActions != null && provisionActions.isEnable() == 'Y'){
				
				this.provisioningWritePlatformService.postPsswordForProvisioning(order, planCode, ProvisioningApiConstants.REQUEST_RESET_PASSWORD,
						order.getId(), startDate, phoneNumber, hwSerialNo, contractPeriod, provisionActions.getProvisioningSystem());
				
			}

			return new CommandProcessingResult(order.getClientId());

		} catch (DataIntegrityViolationException exception) {
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(Long.valueOf(-1L));
		}

	}
	
	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
	
	/*private void createTicket(Long clientId, Long orderId) {
		List<ActionDetaislData> reactivationActionDetails = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_ORDER);
		if (reactivationActionDetails.size() != 0) {
			this.actiondetailsWritePlatformService.AddNewActions(reactivationActionDetails, clientId,orderId.toString(), null);
		}
	}*/
	
}
