package org.obsplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.obsplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.obsplatform.billing.planprice.exceptions.ChargeCodeAndContractPeriodException;
import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.billingorder.service.BillingOrderWritePlatformService;
import org.obsplatform.finance.billingorder.service.GenerateBillingOrderService;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.allocation.domain.HardwareAssociationRepository;
import org.obsplatform.portfolio.association.data.AssociationData;
import org.obsplatform.portfolio.association.domain.HardwareAssociation;
import org.obsplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.obsplatform.portfolio.association.service.HardwareAssociationWriteplatformService;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.obsplatform.portfolio.contract.domain.Contract;
import org.obsplatform.portfolio.contract.domain.ContractRepository;
import org.obsplatform.portfolio.isexdirectory.domain.IsExDirectory;
import org.obsplatform.portfolio.isexdirectory.domain.IsExDirectoryRepository;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderAddons;
import org.obsplatform.portfolio.order.domain.OrderAddonsRepository;
import org.obsplatform.portfolio.order.domain.OrderPrice;
import org.obsplatform.portfolio.order.domain.OrderPriceRepository;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.obsplatform.portfolio.order.exceptions.AddonEndDateValidationException;
import org.obsplatform.portfolio.order.exceptions.NoOrdersFoundException;
import org.obsplatform.portfolio.order.serialization.OrderAddOnsCommandFromApiJsonDeserializer;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.obsplatform.portfolio.servicemapping.domain.ServiceMapping;
import org.obsplatform.portfolio.servicemapping.domain.ServiceMappingRepository;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.obsplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


@Service
public class OrderAddOnsWritePlatformServiceImpl implements OrderAddOnsWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(OrderAddOnsWritePlatformServiceImpl.class);

	private final PlatformSecurityContext context;
	private final FromJsonHelper fromJsonHelper;
	private final ServiceMappingRepository serviceMappingRepository;
	private final OrderAddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ConfigurationRepository configurationRepository;
	private final ContractRepository contractRepository;
	private final ProvisioningWritePlatformService provisioningWritePlatformService;
	private final OrderAssembler orderAssembler;
	private final OrderRepository orderRepository;
	private final OrderPriceRepository orderPriceRepository;
	private final HardwareAssociationRepository hardwareAssociationRepository;
	private final OrderAddonsRepository addonsRepository;
	private final GenerateBillingOrderService generateBillingOrderService;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService;
	private final PlanRepository planRepository;
	private final ChargeCodeRepository chargeCodeRepository;
	private final ClientRepositoryWrapper clientRepository;
	private final HardwareAssociationReadplatformService associationReadplatformService;
	private final HardwareAssociationWriteplatformService associationWriteplatformService;
	private final IsExDirectoryRepository isExDirectoryRepository;
	private final ProcessRequestRepository processRequestRepository;

	

	@Autowired
	public OrderAddOnsWritePlatformServiceImpl(final PlatformSecurityContext context,final OrderAddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final FromJsonHelper fromJsonHelper,final ConfigurationRepository configurationRepository,final ContractRepository contractRepository,
			final OrderAssembler orderAssembler,final OrderRepository orderRepository,
			final ServiceMappingRepository serviceMappingRepository,final OrderAddonsRepository addonsRepository,
			final PlanRepository planRepository,final ProvisioningWritePlatformService provisioningWritePlatformService,
			final HardwareAssociationRepository associationRepository,final OrderPriceRepository orderPriceRepository,
			final GenerateBillingOrderService generateBillingOrderService,
			final BillingOrderWritePlatformService billingOrderWritePlatformService,
			final ChargeCodeRepository chargeCodeRepository,final ClientRepositoryWrapper clientRepository,
			final HardwareAssociationReadplatformService associationReadplatformService,
			final HardwareAssociationWriteplatformService associationWriteplatformService,
			final IsExDirectoryRepository isExDirectoryRepository,final ProcessRequestRepository processRequestRepository) {

		this.context = context;
		this.fromJsonHelper = fromJsonHelper;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.configurationRepository = configurationRepository;
		this.contractRepository = contractRepository;
		this.orderRepository = orderRepository;
		this.provisioningWritePlatformService = provisioningWritePlatformService;
		this.orderPriceRepository = orderPriceRepository;
		this.orderAssembler = orderAssembler;
		this.hardwareAssociationRepository = associationRepository;
		this.planRepository = planRepository;
		this.addonsRepository = addonsRepository;
		this.serviceMappingRepository = serviceMappingRepository;
		this.billingOrderWritePlatformService = billingOrderWritePlatformService;
		this.generateBillingOrderService = generateBillingOrderService;
		this.chargeCodeRepository = chargeCodeRepository;
		this.clientRepository = clientRepository;
		this.associationReadplatformService = associationReadplatformService;
		this.associationWriteplatformService = associationWriteplatformService;
		this.isExDirectoryRepository = isExDirectoryRepository;
		this.processRequestRepository = processRequestRepository;

	}

	@Transactional
	@Override
	public CommandProcessingResult createOrderAddons(final JsonCommand command,final Long orderId) {

		try {

			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final JsonElement element = fromJsonHelper.parse(command.json());
			final JsonArray addonServices = fromJsonHelper.extractJsonArrayNamed("addonServices", element);
			final String planName = command.stringValueOfParameterNamed("planName");
			final Long contractId = command.longValueOfParameterNamed("contractId");
			final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
			final String phoneNumber = command.stringValueOfParameterNamed("phoneNumber");
			final Long oldOrderId = command.longValueOfParameterNamed("oldOrderId");
			final String addOnChangePlan = command.stringValueOfParameterNamed("addOnChangePlan");
			Order order = this.orderRepository.findOne(orderId);
			Contract contract = this.contractRepository.findOne(contractId);

			LocalDate endDate = this.orderAssembler.calculateEndDate(startDate,contract.getSubscriptionType(), contract.getUnits());
			Date addonEndDate = null;

			if (endDate == null && order.getEndDate() != null) {
				throw new AddonEndDateValidationException(orderId);
			}

			if (order.getEndDate() != null&& endDate.isAfter(new LocalDate(order.getEndDate()))) {
				throw new AddonEndDateValidationException(orderId);
			}
			
			Client client = this.clientRepository.findOneWithNotFoundDetection(order.getClientId());
			List<HardwareAssociation> association =null;
			if(oldOrderId == null){
			association = this.hardwareAssociationRepository.findOneByOrderId(orderId);
			}else{
				 association = this.retrieveByOldOrderId(oldOrderId);
			}

			for (JsonElement jsonElement : addonServices) {

				OrderAddons addons = this.assembleOrderAddons(jsonElement,fromJsonHelper, order, startDate, endDate, contractId,association, planName, phoneNumber, oldOrderId, addOnChangePlan);
				this.addonsRepository.saveAndFlush(addons);
				
				OrderPrice orderPrice = this.orderPriceRepository.findOne(addons.getPriceId());
				List<BillingOrderData> billingOrderDatas = new ArrayList<BillingOrderData>();
				
				if (endDate != null	&& "RC".equalsIgnoreCase(orderPrice.getChargeType())) {
					addonEndDate = endDate.toDate();
				} else if ("NRC".equalsIgnoreCase(orderPrice.getChargeType())) {
					addonEndDate = orderPrice.getBillStartDate();
				} else {
					addonEndDate = startDate.plusYears(100).toDate();
				}

				if (order.getNextBillableDay() != null) {
					if(!"CHANGE_PLAN".equalsIgnoreCase(addOnChangePlan)){

					billingOrderDatas.add(new BillingOrderData(orderId, addons.getPriceId(), order.getPlanId(), order.getClientId(), startDate.toDate(), orderPrice.getNextBillableDay(), addonEndDate, "", 
							orderPrice.getChargeCode(), orderPrice.getChargeType(),Integer.valueOf(orderPrice.getChargeDuration()),orderPrice.getDurationType(), orderPrice.getInvoiceTillDate(), 
							orderPrice.getPrice(), String.valueOf(order.getbillAlign()), orderPrice.getBillStartDate(), addonEndDate, order.getStatus(),orderPrice.isTaxInclusive() ? 1 : 0, 
							String.valueOf(client.getTaxExemption())));

					List<BillingOrderCommand> billingOrderCommands = this.generateBillingOrderService.generateBillingOrder(billingOrderDatas);

					Invoice invoice = this.generateBillingOrderService.generateInvoice(billingOrderCommands);

					// Update Client Balance
					this.billingOrderWritePlatformService.updateClientBalance(invoice.getInvoiceAmount(), order.getClientId(),false);

					// Update order-price
					this.billingOrderWritePlatformService.updateBillingOrder(billingOrderCommands);
					logger.info("---------------------"+ billingOrderCommands.get(0).getNextBillableDate());
					}
				}
			}

			return new CommandProcessingResult(order.getId(),order.getClientId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	private OrderAddons assembleOrderAddons(JsonElement jsonElement,FromJsonHelper fromJsonHelper, Order order, LocalDate startDate,LocalDate endDate, Long contractId,
			List<HardwareAssociation> orderAssociation, String planName, String phoneNumber, Long oldOrderId, String addOnChangePlan) {

		OrderAddons orderAddon = OrderAddons.fromJson(jsonElement,fromJsonHelper, order.getId(), startDate, contractId);
		final BigDecimal price = fromJsonHelper.extractBigDecimalWithLocaleNamed("price", jsonElement);

		ChargeCodeMaster chargeCodeMaster = chargeCodeRepository.findOne(fromJsonHelper.extractLongNamed("chargeCodeId",jsonElement));
		Contract contract = contractRepository.findOne(orderAddon.getContractId());
		Plan plan = this.planRepository.findOne(order.getPlanId());

		if ('Y' == plan.isPrepaid() && endDate != null&& chargeCodeMaster.getChargeDuration() != contract.getUnits().intValue()
				&& chargeCodeMaster.getDurationType().equalsIgnoreCase(contract.getSubscriptionType())) {

			throw new ChargeCodeAndContractPeriodException(chargeCodeMaster.getBillFrequencyCode(), "addon");

		} else if ('N' == plan.isPrepaid() && endDate != null && !chargeCodeMaster.getDurationType().equalsIgnoreCase(contract.getSubscriptionType())) {

			throw new ChargeCodeAndContractPeriodException(chargeCodeMaster.getBillFrequencyCode(), "addon");
		}

		OrderPrice orderPrice = new OrderPrice(orderAddon.getServiceId(),chargeCodeMaster.getChargeCode(),chargeCodeMaster.getChargeType(), price, null,
				chargeCodeMaster.getChargeType(), chargeCodeMaster.getChargeDuration().toString(),chargeCodeMaster.getDurationType(), startDate.toDate(),
				endDate, chargeCodeMaster.getTaxInclusive() == 1 ? true : false);

		orderPrice.update(order);
		orderPrice.setIsAddon('Y');
		orderPrice.setBillEndDate("NRC".equalsIgnoreCase(orderPrice.getChargeType()) ? startDate : endDate);
		order.addOrderDeatils(orderPrice);
		this.orderPriceRepository.saveAndFlush(orderPrice);
		this.orderRepository.saveAndFlush(order);

		String status = StatusTypeEnum.ACTIVE.toString();
		
		Long associationId = Long.valueOf(0);

		String serialNo = orderAssociation.isEmpty() ? null : orderAssociation.get(0).getSerialNo();

		List<ServiceMapping> serviceMapping = this.serviceMappingRepository.findOneByServiceId(orderAddon.getServiceId());

		if (!"None".equalsIgnoreCase(plan.getProvisionSystem())	&& serviceMapping.isEmpty()) {

			throw new AddonEndDateValidationException(orderAddon.getServiceId().toString());
		} else {

			if ('Y' == plan.isHardwareReq() && 'Y' == serviceMapping.get(0).getIsHwReq()) {

				if (!this.isAddonPairWithNewDevices()) {
					List<AssociationData> clientOrderAllocations = this.associationReadplatformService.retrieveCustomerHardwareAllocationData(order.getClientId(), order.getId(),serviceMapping.get(0).getItemId());
					for (AssociationData clientOrderAllocation : clientOrderAllocations) {
						if (serviceMapping.get(0).getItemId().equals(clientOrderAllocation.getItemId())) {
							serialNo = clientOrderAllocation.getSerialNum();
							break;
						}
					}

				} else {
					List<AssociationData> clientAllocations = this.associationReadplatformService.retrieveCustomerHardwareAllocationData(order.getClientId(), null, serviceMapping.get(0).getItemId());
					if (clientAllocations.isEmpty()) {
						throw new AddonEndDateValidationException();
					} else {
						// association with new Device
						associationId=this.associationWriteplatformService.createNewHardwareAssociation(order.getClientId(),order.getPlanId(),clientAllocations.get(0).getSerialNum(),order.getId(), "ALLOT",orderAddon.getServiceId());
						serialNo = clientAllocations.get(0).getSerialNum();
					}
				}
			}
		}

		if (!"None".equalsIgnoreCase(serviceMapping.get(0).getProvisionSystem())) {
			status = StatusTypeEnum.PENDING.toString();
		}
		
		if ("CHANGE_PLAN".equalsIgnoreCase(addOnChangePlan)){
			status = StatusTypeEnum.ACTIVE.toString();
		}

		if (endDate != null && "RC".equalsIgnoreCase(chargeCodeMaster.getChargeType())) {
			orderAddon.setEndDate(endDate.toDate());
		} else if ("NRC".equalsIgnoreCase(chargeCodeMaster.getChargeType())) {
			orderAddon.setEndDate(startDate.toDate());
		} else {
			orderAddon.setEndDate(null);
		}
		orderAddon.setProvisionSystem(serviceMapping.get(0).getProvisionSystem());
		orderAddon.setStatus(status);
		orderAddon.setPriceId(orderPrice.getId());
		orderAddon.setAssociationId(associationId);

		this.addonsRepository.saveAndFlush(orderAddon);

		if (!"None".equalsIgnoreCase(orderAddon.getProvisionSystem())) {
			if(!"CHANGE_PLAN".equalsIgnoreCase(addOnChangePlan)){

			this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, planName,UserActionStatusTypeEnum.ADDON_ACTIVATION.toString(), Long.valueOf(0), null,
							serialNo, order.getId(), orderAddon.getProvisionSystem(), orderAddon.getId(), phoneNumber);
			}
		}

		return orderAddon;
	}

	@Override
	public CommandProcessingResult disconnectOrderAddon(final JsonCommand command, final Long entityId) {

		try {
			// this.context.authenticatedUser();
			OrderAddons orderAddons = this.addonsRepository.findOne(entityId);
			Order order = this.orderRepository.findOne(orderAddons.getOrderId());
			
			IsExDirectory isExDirectory = this.isExDirectoryRepository.findOneByOrderId(orderAddons.getOrderId());
			//isExDirectory.setIsUmeeApp(false);
			
			if(isExDirectory != null)
			{
				isExDirectory.setIsUmeeApp(false);
				this.isExDirectoryRepository.saveAndFlush(isExDirectory);
			}
			
			LocalDate endDate=DateUtils.getLocalDateOfTenant();
			if(orderAddons.getEndDate() !=null && endDate.toDate().after(orderAddons.getEndDate())){
				 endDate = new LocalDate(orderAddons.getEndDate());
			}
			orderAddons.setEndDate(endDate.toDate());
			List<OrderPrice> orderAddonPrices=order.getPrice();
			for (OrderPrice orderPrice : orderAddonPrices) {
				if (orderPrice.getId().equals(orderAddons.getPriceId())) {
					orderPrice.delete();
					orderPrice.setBillEndDate(endDate);
					break;
				}
			}
			List<ServiceMapping> serviceMapping = this.serviceMappingRepository.findOneByServiceId(orderAddons.getServiceId());
			if (!serviceMapping.isEmpty()) {
				if ("None".equalsIgnoreCase(serviceMapping.get(0).getProvisionSystem())) {
					orderAddons.setStatus(StatusTypeEnum.DISCONNECTED.toString());
				} else {
					Plan plan = this.planRepository.findOne(order.getPlanId());
					OrderPrice orderAddonPrice = this.orderPriceRepository.findOne(orderAddons.getPriceId());
					List<HardwareAssociation> association = this.hardwareAssociationRepository.findOneByOrderId(orderAddons.getOrderId());
					String serialNo = association.isEmpty() ? null: association.get(0).getSerialNo();
					if ('Y' == plan.isHardwareReq() && 'Y' == serviceMapping.get(0).getIsHwReq()) {

						List<AssociationData> clientOrderAllocations = this.associationReadplatformService.retrieveCustomerHardwareAllocationData(order.getClientId(), order.getId(),serviceMapping.get(0).getItemId());
						if (!clientOrderAllocations.isEmpty()) {
							for (AssociationData clientOrderAllocation : clientOrderAllocations) {
								if (this.isAddonPairWithNewDevices() && clientOrderAllocation.getServiceId() != null && clientOrderAllocation.getServiceId().equals(orderAddons.getServiceId())) {
									serialNo = clientOrderAllocation.getSerialNum();
									break;
								}else {
									serialNo = clientOrderAllocation.getSerialNum();
								}
							}
						}
					}
					orderAddonPrice.setBillEndDate(endDate);
					orderAddonPrice.delete();
					this.orderPriceRepository.saveAndFlush(orderAddonPrice);
					orderAddons.setStatus(StatusTypeEnum.PENDING.toString());
					this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getPlanCode(),UserActionStatusTypeEnum.ADDON_DISCONNECTION.toString(), Long.valueOf(0), null,
									serialNo, order.getId(), serviceMapping.get(0).getProvisionSystem(),orderAddons.getId(), null);
				}
            
				this.addonsRepository.save(orderAddons);
				this.orderRepository.saveAndFlush(order);
				//this.isExDirectoryRepository.saveAndFlush(isExDirectory);

			} else {
				throw new AddonEndDateValidationException(orderAddons.getServiceId().toString());
			}
			return new CommandProcessingResult(entityId,order.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	@Override
	public CommandProcessingResult deleteOrderAddon(final JsonCommand command, final Long orderAddOnId) {

		try {
			this.context.authenticatedUser();
			OrderAddons orderAddons = this.addonsRepository.findOne(orderAddOnId);
			Order order = this.orderRepository.findOne(orderAddons.getOrderId());
			orderAddons.isDeleted();
			this.addonsRepository.saveAndFlush(orderAddons);

			return new CommandProcessingResult(orderAddOnId, order.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	@Override
	public CommandProcessingResult cancelOrderAddon(final JsonCommand command, final Long orderAddOnId) {

		try {
			this.context.authenticatedUser();
		    disconnectOrderAddon(command, orderAddOnId);
		    
		    OrderAddons orderAddons = this.addonsRepository.findOne(orderAddOnId);
		    Order order = this.orderRepository.findOne(orderAddons.getOrderId());
		    orderAddons.isDeleted();
		    this.addonsRepository.saveAndFlush(orderAddons);
		    
		    List<ProcessRequest> processrequest = this.processRequestRepository.findOutExistsChangeOrderAddOnsRequest(orderAddons.getOrderId());
			for (ProcessRequest processRequest : processrequest) {
				processRequest.setNotifyAddOn();
				processRequest.setProcessedAddOn();
				processRequest.getProcessRequestDetails().get(0).setRecievedMessage("Cancelled");

				this.processRequestRepository.saveAndFlush(processRequest);
			}

			return new CommandProcessingResult(orderAddOnId, order.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		
        logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue","Unknown data integrity issue with resource: "
						+ dve.getMostSpecificCause().getMessage());
	}
	
	public boolean isAddonPairWithNewDevices() {

		final Configuration property = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_Is_ADDON_PAIR_WITH_NEWDEVICE);

		if (property != null && property.isEnabled()) {
			return true;
		} else {
			return false;
		}

	}
	
	private List<HardwareAssociation> retrieveByOldOrderId(Long orderId){
		String clientId=null;
		final List<HardwareAssociation> association = this.hardwareAssociationRepository.findOneByOldOrderId(orderId);
		if(association == null){
			throw new NoOrdersFoundException(orderId,clientId);
		}
		return association;
		
	}

}