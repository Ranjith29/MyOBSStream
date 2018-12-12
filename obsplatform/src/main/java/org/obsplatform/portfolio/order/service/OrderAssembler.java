package org.obsplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.billing.chargevariant.domain.ChargeVariant;
import org.obsplatform.billing.chargevariant.domain.ChargeVariantDetails;
import org.obsplatform.billing.chargevariant.domain.ChargeVariantRepository;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.billing.discountmaster.domain.DiscountDetails;
import org.obsplatform.billing.discountmaster.domain.DiscountMaster;
import org.obsplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.obsplatform.billing.discountmaster.exception.DiscountMasterNotFoundException;
import org.obsplatform.billing.payterms.data.PaytermData;
import org.obsplatform.billing.planprice.data.PriceData;
import org.obsplatform.billing.servicetransfer.service.ServiceTransferReadPlatformService;
import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.finance.billingorder.service.GenerateBill;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.logistics.itemdetails.exception.OrderQuantityExceedsException;
import org.obsplatform.organisation.feemaster.data.FeeMasterData;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.contract.domain.Contract;
import org.obsplatform.portfolio.contract.domain.ContractRepository;
import org.obsplatform.portfolio.order.data.OrderStatusEnumaration;
import org.obsplatform.portfolio.order.domain.ConnectionTypeEnum;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderDiscount;
import org.obsplatform.portfolio.order.domain.OrderEvent;
import org.obsplatform.portfolio.order.domain.OrderLine;
import org.obsplatform.portfolio.order.domain.OrderPrice;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.obsplatform.portfolio.order.exceptions.ChangeOrderException;
import org.obsplatform.portfolio.order.exceptions.NoOrdersFoundException;
import org.obsplatform.portfolio.order.exceptions.NoRegionalPriceFound;
import org.obsplatform.portfolio.order.exceptions.OrderReactivationException;
import org.obsplatform.portfolio.order.exceptions.OrderReconnectException;
import org.obsplatform.portfolio.order.exceptions.RenewalOrderException;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.obsplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderAssembler {
	
private final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices;
private final ContractRepository contractRepository;
private final ConfigurationRepository configurationRepository;
private final DiscountMasterRepository discountMasterRepository;
private final ClientRepository clientRepository;
private final GenerateBill generateBill;
private final ChargeVariantRepository chargeVariantRepository;
private final OrderRepository orderRepository;
private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
private final ServiceTransferReadPlatformService serviceTransferReadPlatformService;
private final PlanRepository planRepository;

@Autowired
public OrderAssembler(final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices,final ContractRepository contractRepository,
		   final DiscountMasterRepository discountMasterRepository,final ConfigurationRepository configurationRepository,
		   final ClientRepository clientRepository, final GenerateBill generateBill, 
		   final ChargeVariantRepository chargeVariantRepository,final OrderRepository orderRepository,
		   final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
		   final ServiceTransferReadPlatformService serviceTransferReadPlatformService,
		   final PlanRepository planRepository){
	
	this.orderDetailsReadPlatformServices=orderDetailsReadPlatformServices;
	this.contractRepository=contractRepository;
	this.discountMasterRepository=discountMasterRepository;
	this.configurationRepository = configurationRepository;
	this.clientRepository = clientRepository;
	this.generateBill = generateBill;
	this.chargeVariantRepository = chargeVariantRepository;
	this.orderRepository = orderRepository;
	this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
	this.serviceTransferReadPlatformService = serviceTransferReadPlatformService;
	this.planRepository = planRepository;
}

	public Order assembleOrderDetails(final JsonCommand command, final Long clientId, Plan plan) throws JSONException {
		
		List<OrderLine> serviceDetails = new ArrayList<OrderLine>();
		List<OrderPrice> orderprice = new ArrayList<OrderPrice>();
		Long orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
		
		LocalDate endDate = null;
		BigDecimal discountRate = BigDecimal.ZERO;
		
		final Long activeOrdersCount = this.orderDetailsReadPlatformServices.retrieveClientActivePlanOrdersCount(clientId, plan.getId());
		Configuration orderBookingProperty = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_HARDWARE_SALE_LIMIT);
		if (orderBookingProperty != null && orderBookingProperty.isEnabled()) {
			if (activeOrdersCount >= Long.valueOf(orderBookingProperty.getValue())) {
				throw new OrderQuantityExceedsException(plan.getDescription(),activeOrdersCount);
			}
		}

		Order order = Order.fromJson(clientId, command);
		List<ServiceData> planServicesDetails = this.orderDetailsReadPlatformServices.retrieveAllServices(order.getPlanId());
		List<EventMasterData> planEventsDetails = this.orderDetailsReadPlatformServices.retrieveAllEvents(order.getPlanId());
		List<PriceData> priceDatas = this.orderDetailsReadPlatformServices.retrieveAllPrices(order.getPlanId(),order.getBillingFrequency(), clientId);
		// datas=this.orderDetailsReadPlatformServices.retrieveDefaultPrices(order.getPlanId(),order.getBillingFrequency(),clientId);
		if (priceDatas.isEmpty()) {
			throw new NoRegionalPriceFound();
		}

		Contract contractData = this.contractRepository.findOne(order.getContarctPeriod());
		LocalDate startDate = new LocalDate(order.getStartDate());

		if ("None".equalsIgnoreCase(plan.getProvisionSystem())) {
			orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId();
		}

		// Calculate EndDate
		endDate = this.calculateEndDate(startDate,contractData.getSubscriptionType(), contractData.getUnits());

		order = new Order(order.getClientId(), order.getPlanId(), orderStatus,null, order.getBillingFrequency(), startDate, endDate,
				order.getContarctPeriod(), serviceDetails, orderprice,order.getbillAlign(),UserActionStatusTypeEnum.ACTIVATION.toString(),
				plan.isPrepaid(), order.isAutoRenewal(),ConnectionTypeEnum.REGULAR.toString());

		//Check align flag
		Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_ALIGN_BILLING_CYCLE);

		if (configuration != null && configuration.isEnabled() && plan.isPrepaid() == 'N') {

			JSONObject configValue = new JSONObject(configuration.getValue());
			if (endDate != null && configValue.getBoolean("fixed")) {
				order.setBillingAlign('Y');
				order.setEndDate(endDate.dayOfMonth().withMaximumValue());
			} else if (endDate == null && configValue.getBoolean("perpetual")) {
				order.setBillingAlign('Y');
			} else {
				order.setBillingAlign('N');
			}
		 } else {
			order.setBillingAlign('N');
		}
		
		
		for (PriceData data : priceDatas) {

     		 LocalDate billstartDate = startDate;
			 LocalDate billEndDate = null;
			 BigDecimal orderVariantPrice = data.getPrice();
			 
		   if(!"UC".equalsIgnoreCase(data.getChagreType())){
			 
			  orderVariantPrice = this.calculateChargeVariantPrice(data.getChargingVariant(),data.getPrice(),clientId,plan.getId(),activeOrdersCount,1L);//no of connections;
			//Set secondary connection endate to primary connection endate
			if (activeOrdersCount >=1 && orderVariantPrice.compareTo(data.getPrice()) != 0 ) {
				
				final Order primaryOrder = this.orderRepository.findOnePrimaryActiveOrderDetails(clientId, plan.getId());

				if (primaryOrder != null && primaryOrder.getEndDate() != null && endDate != null) {
					
					if (endDate.toDate().after(primaryOrder.getEndDate())) {
						order.setEndDate(new LocalDate(primaryOrder.getEndDate()));

					}/*else if(startDate.toDate().before(primaryOrder.getStartDate())){
						order.setStartDate(new LocalDate(primaryOrder.getStartDate()));
						billstartDate = new LocalDate(primaryOrder.getStartDate());
					}*/

				}else if (primaryOrder != null && primaryOrder.getEndDate() != null && endDate == null){
					
					    order.setEndDate(new LocalDate(primaryOrder.getEndDate()));
					    endDate = new LocalDate(primaryOrder.getEndDate());
				}
				
				primaryOrder.setConnectionType(ConnectionTypeEnum.PRIMARY.toString());
				this.orderRepository.saveAndFlush(primaryOrder);
				order.setConnectionType(ConnectionTypeEnum.SECONDARY.toString());
			}	
		  }
			// end date is null for rc

			if (endDate != null && ("RC".equalsIgnoreCase(data.getChagreType()) || "UC".equalsIgnoreCase(data.getChagreType()))) {

				billEndDate = new LocalDate(order.getEndDate());
			} else if ("NRC".equalsIgnoreCase(data.getChagreType())) {
				billEndDate = billstartDate;
			}

			OrderPrice price = new OrderPrice(data.getServiceId(),data.getChargeCode(),data.getChargingVariant(),orderVariantPrice,// data.getPrice(),
					null, data.getChagreType(), data.getChargeDuration(),data.getDurationType(), billstartDate.toDate(),
					billEndDate, data.isTaxInclusive());
			order.addOrderDeatils(price);
		
			Client client = this.clientRepository.findOne(clientId);
			final DiscountMaster discountMaster = this.discountMasterRepository.findOne(data.getDiscountId());
			if (discountMaster == null) {
				throw new DiscountMasterNotFoundException();
			}

			List<DiscountDetails> discountDetails = discountMaster.getDiscountDetails();
			for (DiscountDetails discountDetail : discountDetails) {
				if (client.getCategoryType().equals(Long.valueOf(discountDetail.getCategoryType()))) {
					discountRate = discountDetail.getDiscountRate();
				} else if (discountRate.equals(BigDecimal.ZERO) && Long.valueOf(discountDetail.getCategoryType()).equals(Long.valueOf(0))) {
					discountRate = discountDetail.getDiscountRate();
				}
			}

			// discount Order
			OrderDiscount orderDiscount = new OrderDiscount(order, price,discountMaster.getId(), discountMaster.getStartDate(),
					null, discountMaster.getDiscountType(), discountRate, discountMaster.getDiscountCode());
			order.addOrderDiscount(orderDiscount);
		}

		for (ServiceData data : planServicesDetails) {
			OrderLine orderServicedetail = new OrderLine(data.getPlanId(),data.getServiceType(), plan.getStatus(), 'n');
			order.addServiceDeatils(orderServicedetail);
		}
		if(planEventsDetails.size() > 0){
			for(EventMasterData data : planEventsDetails){
				OrderEvent orderEventDetail = new OrderEvent(data.getEventId(),"VOD",plan.getStatus(), 'n');
				order.addEventDeatils(orderEventDetail);
			}
		}

		return order;

	}
	

    //Calculate EndDate
	public LocalDate calculateEndDate(LocalDate startDate,String durationType,Long duration) {

			LocalDate contractEndDate = null;
			 		if (durationType.equalsIgnoreCase("DAY(s)")) {
			 			contractEndDate = startDate.plusDays(duration.intValue() - 1);
			 		} else if (durationType.equalsIgnoreCase("MONTH(s)")) {
			 			contractEndDate = startDate.plusMonths(duration.intValue()).minusDays(1);
			 		} else if (durationType.equalsIgnoreCase("YEAR(s)")) {
			 		contractEndDate = startDate.plusYears(duration.intValue()).minusDays(1);
			 		} else if (durationType.equalsIgnoreCase("week(s)")) {
			 		contractEndDate = startDate.plusWeeks(duration.intValue()).minusDays(1);
			 		}
			 	return contractEndDate;
			}

	
	public Order setDatesOnOrderActivation(Order order, LocalDate startDate, String requestType) {
		
		// for Freemium
		Configuration isPremiumPlan = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_PREMIUM_PLAN);
		Plan plan = this.planRepository.findPlanCheckDeletedStatus(order.getPlanId());
		if (isPremiumPlan.isEnabled() && requestType.equalsIgnoreCase(ProvisioningApiConstants.REQUEST_ACTIVATION)) {
			if (plan.getTrialDays() != null) {
				startDate = startDate.plusDays(plan.getTrialDays().intValue());
				order.setNextBillableDay(startDate.toDate());
			}
			order.setOrderActivationDate(DateUtils.getLocalDateOfTenant().toDate());
		} else if (isPremiumPlan.isEnabled()
				&& requestType.equalsIgnoreCase(ProvisioningApiConstants.REQUEST_CHANGE_PLAN)) {
			Order oldOrder = this.orderRepository.findOldOrderByOrderNO(order.getOrderNo());
			LocalDate oldOrderActivationDate = new LocalDate(oldOrder.getOrderActivationDate());
			LocalDate oldOrderEndDate = new LocalDate(oldOrder.getEndDate());
			if (oldOrderEndDate.toDate().before(oldOrder.getStartDate())) {
				int usedTrialDays = Days.daysBetween(oldOrderActivationDate, oldOrderEndDate).getDays();
				if (plan.getTrialDays() != null) {
					int changePlanTrialDays = plan.getTrialDays().intValue() - usedTrialDays;
					if (changePlanTrialDays > 0) {
						startDate = startDate.plusDays(changePlanTrialDays);
						order.setNextBillableDay(startDate.toDate());
					}
				}
			}
			order.setOrderActivationDate(DateUtils.getLocalDateOfTenant().toDate());
		}
		Contract contract = this.contractRepository.findOne(order.getContarctPeriod());
	    LocalDate endDate = this.calculateEndDate(startDate, contract.getSubscriptionType(), contract.getUnits());
	    order.setStartDate(startDate);
	    if(order.getbillAlign() == 'Y' && endDate != null){
	    	order.setEndDate(endDate.dayOfMonth().withMaximumValue());
		}else{
			order.setEndDate(endDate);
		}

		for (OrderPrice orderPrice : order.getPrice()) {
			LocalDate billstartDate = startDate;

			orderPrice.setBillStartDate(billstartDate);
			//for Freemium
			if(isPremiumPlan.isEnabled() && (requestType.equalsIgnoreCase(ProvisioningApiConstants.REQUEST_ACTIVATION)
					|| requestType.equalsIgnoreCase(ProvisioningApiConstants.REQUEST_CHANGE_PLAN))){
			   orderPrice.setNextBillableDay(order.getNextBillableDay());
			}
			// end date is null for rc
			if (("RC".equalsIgnoreCase(orderPrice.getChargeType()) || "UC".equalsIgnoreCase(orderPrice.getChargeType())) && endDate != null) {
				orderPrice.setBillEndDate(new LocalDate(order.getEndDate()));
			} else if ("RC".equalsIgnoreCase(orderPrice.getChargeType()) && endDate == null) {
				orderPrice.setBillEndDate(endDate);
			} else if ("NRC".equalsIgnoreCase(orderPrice.getChargeType())) {
				orderPrice.setBillEndDate(billstartDate);
			}
		}
		return order;

	}
	
	public BigDecimal calculateChargeVariantPrice(final String chargingVariant,BigDecimal orderPrice,final Long clientId, 
			final Long planId,final Long activeOrdersCount,final Long countOfConnections) {

		final ChargeVariant chargeVariant = this.chargeVariantRepository.findOne(Long.valueOf(chargingVariant));

		if (chargeVariant != null && !"None".equalsIgnoreCase(chargeVariant.getChargevariantCode())) {
			for (ChargeVariantDetails chargeVariantDetails : chargeVariant.getChargeVariantDetails()) {

				DiscountMasterData discountMasterData = new DiscountMasterData(chargeVariant.getId(), null, null,
						chargeVariantDetails.getAmountType(),chargeVariantDetails.getAmount(), null, null,chargeVariant.getStatus());
				
				if (activeOrdersCount >= 1) {
					if ("ANY".equalsIgnoreCase(chargeVariantDetails.getVariantType())) {
						
						orderPrice = this.generateBill.calculateDiscount(discountMasterData, orderPrice).getDiscountedChargeAmount();
						return orderPrice.multiply(BigDecimal.valueOf(countOfConnections));
						
					} else if ("Range".equalsIgnoreCase(chargeVariantDetails.getVariantType())) {

						if (activeOrdersCount >= (chargeVariantDetails.getFrom()-1) && activeOrdersCount <= chargeVariantDetails.getTo()) {
							orderPrice = this.generateBill.calculateDiscount(discountMasterData, orderPrice).getDiscountedChargeAmount();
							return orderPrice.multiply(BigDecimal.valueOf(countOfConnections));
						}
					}

				}else if(countOfConnections > 1 && activeOrdersCount == 0  ){
					
					if ("ANY".equalsIgnoreCase(chargeVariantDetails.getVariantType())) {
						
						BigDecimal price = orderPrice;
						orderPrice = this.generateBill.calculateDiscount(discountMasterData, orderPrice).getDiscountedChargeAmount();
						return price.add(orderPrice.multiply(BigDecimal.valueOf(countOfConnections-1)));//Remove First one
						
					} else if ("Range".equalsIgnoreCase(chargeVariantDetails.getVariantType())) {
						
						if (countOfConnections >= (chargeVariantDetails.getFrom()-1) && countOfConnections <= chargeVariantDetails.getTo()) {
							
							BigDecimal price = orderPrice;
							orderPrice = this.generateBill.calculateDiscount(discountMasterData, orderPrice).getDiscountedChargeAmount();
							return price.add(orderPrice.multiply(BigDecimal.valueOf(countOfConnections-1)));//Remove First one
							
						}
					}
				}
			}
		}
		
		 return orderPrice.multiply(BigDecimal.valueOf(countOfConnections));

	}
	
	public List<Long> checkDisconnectOrderChilds(final Long orderId,final Long clientId, final Long planId) {

		return this.orderDetailsReadPlatformServices.retrieveDisconnectingOrderSecondaryConnections(clientId,planId);

	}

	public List<Long> checkTerminateOrderChilds(final Long clientId, final Long planId) {

		return this.orderDetailsReadPlatformServices.retrieveTerminatableOrderSecondaryConnections(clientId, planId);

	}
	
	public List<Long> checkSuspentionOrderChilds(final Long clientId, final Long planId) {

		return this.orderDetailsReadPlatformServices.retrieveSuspendableOrderSecondaryConnections(clientId, planId);

	}
	
	public List<Long> checkReactiveOrderChilds(final Long orderId,final Long clientId, final Long planId) {
		
		List<Long> secondaryConnections = this.orderDetailsReadPlatformServices.retrieveReactivableOrderSecondaryConnections(clientId, planId);

		if (secondaryConnections.size() != 0 && !orderId.equals(Collections.min(secondaryConnections))) {
               throw new OrderReactivationException();
		}
		return secondaryConnections;

	}

	public List<Long> checkReconnectOrderChilds(final Long orderId,final Long clientId, final Long planId) {
		
	   List<Long> secondaryConnections =  this.orderDetailsReadPlatformServices.retrieveReconnectingOrderSecondaryConnections(clientId, planId);
	   
		if (secondaryConnections.size() != 0 && !orderId.equals(Collections.min(secondaryConnections))) {
            throw new OrderReconnectException();
		}

		return secondaryConnections;
	}
	
	public List<Long> checkChangeOrderChilds(final Long orderId,final Long clientId, final Long planId) {

		List<Long> secondaryConnections = new ArrayList<Long>();

		secondaryConnections = this.orderDetailsReadPlatformServices.retrieveChangingOrderSecondaryConnections(clientId, planId);

		if (secondaryConnections.size() != 0 && !orderId.equals(Collections.min(secondaryConnections))) {
			throw new ChangeOrderException();
		}
		return secondaryConnections;
	}

	public List<Long> checkRenewalOrderChilds(final Long orderId, final Long clientId,final Long planId,final Long orderStatus) {
		
		final Long activeOrdersCount = this.orderDetailsReadPlatformServices.retrieveClientActivePlanOrdersCount(clientId,planId);
		List<Long> secondaryConnections = this.orderDetailsReadPlatformServices.retrieveRenewalOrderSecondaryConnections(clientId, planId,orderStatus);

		if (secondaryConnections.size() != 0 && !orderId.equals(Collections.min(secondaryConnections))
				&& orderStatus.equals(StatusTypeEnum.DISCONNECTED.getValue().longValue()) 
				&& activeOrdersCount == 0) { // After Renewal Case
			throw new RenewalOrderException();
		} 
		return secondaryConnections;
	}

	public PaytermData calculateChargeVaraintBasePriceForPlan(final Long planId,final Long clientId,final String billFrequency, final Long connections, final String contract,
			final String state,final String country) {
		
		BigDecimal depositPrice = BigDecimal.ZERO;
		BigDecimal secondaryPrice = BigDecimal.ZERO;
	    Long activeOrdersCount=0L;
	    
		if (clientId !=0){
			activeOrdersCount = this.orderDetailsReadPlatformServices.retrieveClientActivePlanOrdersCount(clientId, planId);
		}
		final List<PriceData> priceDatas= this.orderDetailsReadPlatformServices.retrievCustomerRegionPlanPrices(planId,clientId,billFrequency,state,country);
		if (priceDatas.isEmpty()) {
			throw new NoRegionalPriceFound();
		}
		BigDecimal planPrice = BigDecimal.ZERO;
		for(int i=0;i<priceDatas.size();i++){
			planPrice = planPrice.add(this.calculateChargeVariantPrice(priceDatas.get(i).getChargingVariant(), priceDatas.get(i).getPrice(),clientId,planId,
				     activeOrdersCount,connections));
		}
		
		
		ActionDetaislData actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveEventWithAction(EventActionConstants.EVENT_CREATE_ORDER,EventActionConstants.ACTION_SUBSCRIPTION_DEPOSIT);
		if(null != actionDetaislDatas){
			List<FeeMasterData> feeData;
			if(clientId ==0){
				 feeData = this.serviceTransferReadPlatformService.retrieveSingleFeeDetailsforclientZero(clientId, "Deposit", planId, contract,state,country);

			}
			else{
				feeData = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId, "Deposit", planId, contract);
			}
			if(!feeData.isEmpty()){
				depositPrice = feeData.get(0).getDefaultFeeAmount();
			}
		}
		
		if (activeOrdersCount == 0 && connections > 1) {

			secondaryPrice = this.secondaryPrice(priceDatas.get(0).getPrice(),connections, planPrice);

		} else if (activeOrdersCount == 0 && connections == 1) {

			secondaryPrice = this.calculateChargeVariantPrice(priceDatas.get(0).getChargingVariant(), priceDatas.get(0).getPrice(),
					clientId, planId, 1L, connections);
		}

		return new PaytermData(planPrice.setScale(2,RoundingMode.HALF_UP),secondaryPrice.setScale(2,RoundingMode.HALF_UP),activeOrdersCount, depositPrice);

	}

	private BigDecimal secondaryPrice(BigDecimal price, final Long count,final BigDecimal variantPrice) {

		switch (count.intValue()) {
		case 2:
			price = variantPrice.subtract(price);
			break;
		default:
			price = (variantPrice.subtract(price)).divide(new BigDecimal(count - 1));
			break;
		}

		return price;
	}
	
public PaytermData calculateChargeVaraintBasePriceForPlanRenewal(final Long planId,final Long clientId,final String billFrequency,
		final Long connections, final String contract, final Long orderId) {
		
		BigDecimal depositPrice = BigDecimal.ZERO;
		BigDecimal secondaryPrice = BigDecimal.ZERO;
		BigDecimal planPrice = BigDecimal.ZERO;
		
		final List<ServiceData> activeOrdersCountList = this.orderDetailsReadPlatformServices.retrieveReconnectionPrices(clientId, planId, orderId);
		if(activeOrdersCountList.isEmpty()){
			throw new NoOrdersFoundException(clientId, planId);
		}
		for(ServiceData data: activeOrdersCountList){
			
			if(data.getBillingFrequency().equalsIgnoreCase(billFrequency)){
				secondaryPrice = data.getPrice();
			}
		}
		
		final List<PriceData> priceDatas= this.orderDetailsReadPlatformServices.retrievCustomerRegionPlanPrices(planId,clientId,billFrequency,null,null);
		if (priceDatas.isEmpty()) {
			throw new NoRegionalPriceFound();
		}
		planPrice = this.calculateChargeVariantPrice(priceDatas.get(0).getChargingVariant(), priceDatas.get(0).getPrice(),clientId,planId,
				Long.valueOf(activeOrdersCountList.size()),connections);
		return new PaytermData(planPrice.setScale(2, RoundingMode.HALF_UP), secondaryPrice.setScale(2, RoundingMode.HALF_UP), null, depositPrice);

	}
	
}
