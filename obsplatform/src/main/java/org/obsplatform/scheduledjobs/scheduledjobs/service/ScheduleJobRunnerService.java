package org.obsplatform.scheduledjobs.scheduledjobs.service;

import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.obsplatform.billing.planprice.domain.Price;
import org.obsplatform.billing.planprice.domain.PriceRepository;
import org.obsplatform.finance.billingorder.service.BillingOrderReadPlatformService;
import org.obsplatform.finance.billingorder.service.GenerateBill;
import org.obsplatform.finance.clientbalance.domain.ClientBalance;
import org.obsplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBillingRepositoryWrapper;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientCardDetails;
import org.obsplatform.portfolio.client.domain.ClientCardDetailsRepository;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.contract.domain.Contract;
import org.obsplatform.portfolio.contract.domain.ContractRepository;
import org.obsplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.obsplatform.portfolio.order.data.OrderData;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderPrice;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.order.service.OrderWritePlatformService;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.obsplatform.portfolio.service.domain.ServiceMaster;
import org.obsplatform.portfolio.service.domain.ServiceMasterRepository;
import org.obsplatform.scheduledjobs.scheduledjobs.data.JobParameterData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class ScheduleJobRunnerService {
	
	private final static Logger logger = LoggerFactory.getLogger(ScheduleJobRunnerService.class);
	
	private final  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");

	private final ClientBalanceRepository clientBalanceRepository;
	private final OrderRepository orderRepository;
	private final FromJsonHelper fromApiJsonHelper;
	private final OrderWritePlatformService orderWritePlatformService;
	private final PlanRepository planRepository;
	private final PriceRepository priceRepository;
	private final ServiceMasterRepository serviceMasterRepository;
	private final ContractRepository contractRepository;
	private final ClientRepository clientRepository;
	private final ClientCardDetailsRepository clientCardDetailsRepository;

	@Autowired
	public ScheduleJobRunnerService(final ClientBalanceRepository clientBalanceRepository,
			final BillingOrderReadPlatformService billingOrderReadPlatformService,
			final GenerateBill generateBill,
			final OrderRepository orderRepository,
			final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
			final FromJsonHelper fromApiJsonHelper,
			final OrderWritePlatformService orderWritePlatformService,
			final PriceRepository priceRepository,
			final RecurringBillingRepositoryWrapper recurringBillingRepositoryWrapper,
			final PlanRepository planRepository,
			final ServiceMasterRepository serviceMasterRepository,
			final ContractRepository contractRepository,
			final ClientRepository clientRepository,
			final ClientCardDetailsRepository clientCardDetailsRepository) {

		this.clientBalanceRepository = clientBalanceRepository;
		this.orderRepository = orderRepository;
		this.priceRepository = priceRepository;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.orderWritePlatformService = orderWritePlatformService;
		this.serviceMasterRepository = serviceMasterRepository;
		this.planRepository = planRepository;
		this.contractRepository = contractRepository;
		this.clientRepository = clientRepository;
		this.clientCardDetailsRepository = clientCardDetailsRepository;

	}

	public boolean checkClientBalanceForOrderrenewal(OrderData orderData,Long clientId, List<OrderPrice> orderPrices) {

		boolean isAmountSufficient = false;
		final ClientBalance clientBalance = this.clientBalanceRepository.findByClientId(clientId);
		if (clientBalance != null) {
			BigDecimal resultanceBal = clientBalance.getBalanceAmount();
			if (resultanceBal.compareTo(BigDecimal.ZERO) != 1) {
				isAmountSufficient = true;
			}
		}
		return isAmountSufficient;
	}

	
	 public void ProcessAutoExipiryDetails(final OrderData orderData, FileWriter fw, final LocalDate exipirydate,
				final JobParameterData data, final Long clientId, boolean isSufficientAmountForRenewal) {

			try {

				if (!(orderData.getStatus().equalsIgnoreCase(StatusTypeEnum.DISCONNECTED.toString())
						|| orderData.getStatus().equalsIgnoreCase(StatusTypeEnum.PENDING.toString()))) {

					 SimpleDateFormat dateFormat = new  SimpleDateFormat("dd MMMM yyyy");
					if (exipirydate.equals(orderData.getEndDate()) || exipirydate.isAfter(orderData.getEndDate())) {

						JSONObject jsonobject = new JSONObject();
						if ("Y".equalsIgnoreCase(data.getIsAutoRenewal()) && "Y".equalsIgnoreCase(orderData.getAutoRenew())) {
						  
							final Order order = this.orderRepository.findOne(orderData.getId());
							final Plan plan = this.planRepository.findOne(order.getPlanId());
							
							   if(isSufficientAmountForRenewal){
								
									if (plan.isPrepaid() == 'Y') {
										ServiceMaster serviceMaster = this.serviceMasterRepository.findOne(order.getPrice().get(0).getServiceId());
										Contract contract = this.contractRepository.findOne(order.getContarctPeriod());
										List<Price> prices = this.priceRepository.findOneByPlanAndService(order.getPlanId(),serviceMaster.getServiceCode(), contract.getSubscriptionPeriod(),
												order.getPrice().get(0).getChargeCode());
										if (!prices.isEmpty()) {
											jsonobject.put("priceId", prices.get(0).getId());
										}
									}

									jsonobject.put("renewalPeriod", order.getContarctPeriod());
									jsonobject.put("description", "Order Renewal By Scheduler");

									final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonobject.toString());
									final JsonCommand command = JsonCommand.from(jsonobject.toString(), parsedCommand,this.fromApiJsonHelper, "RENEWAL", order.getClientId(), 
											null, null,order.getClientId(), null, null, null, null, null, null, null);
									fw.append("sending json data for Renewal Order is : " + jsonobject.toString() + "\r\n");
									this.orderWritePlatformService.renewalClientOrder(command, orderData.getId());
									fw.append("Client Id" + clientId + " With this Orde" + orderData.getId()+ " has been renewaled for one month via " + "Auto Exipiry on Dated"+ exipirydate);

								} else {
									
									jsonobject.put("disconnectReason", "Date Expired");
									jsonobject.put("disconnectionDate", dateFormat.format(orderData.getEndDate().toDate()));
									jsonobject.put("dateFormat", "dd MMMM yyyy");
									jsonobject.put("locale", "en");
									fw.append("sending json data for Disconnecting the Order is : " + jsonobject.toString()+ "\r\n");

									final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonobject.toString());
									final JsonCommand command = JsonCommand.from(jsonobject.toString(), parsedCommand,this.fromApiJsonHelper, "DissconnectOrder", order.getClientId(), 
											null, null,order.getClientId(), null, null, null, null, null, null, null);
									this.orderWritePlatformService.disconnectOrder(command, orderData.getId());
									fw.append("Client Id" + order.getClientId() + " With this Orde" + order.getId()+ " has been disconnected via Auto Exipiry on Dated" + exipirydate);
								}
							} 
						 else {

							jsonobject.put("disconnectReason", "Date Expired");
							jsonobject.put("disconnectionDate", dateFormat.format(orderData.getEndDate().toDate()));
							jsonobject.put("dateFormat", "dd MMMM yyyy");
							jsonobject.put("locale", "en");
							final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonobject.toString());
							final JsonCommand command = JsonCommand.from(jsonobject.toString(), parsedCommand,this.fromApiJsonHelper, "DissconnectOrder", clientId, 
									null, null, clientId, null, null,null, null, null, null, null);
							this.orderWritePlatformService.disconnectOrder(command, orderData.getId());
							fw.append("Client Id" + clientId + " With this Orde" + orderData.getId() + " has been disconnected via Auto Exipiry on Dated" + exipirydate);
						}
					}
				}
			} catch (Exception ex) {
				logger.error("Disconnection of Service Failed :" +ex.getMessage());
			} 
		}


	/**
	 * @param order
	 * @param fw
	 * @param clientId
	 */
	public void ProcessDisconnectUnPaidCustomers(final Long orderId,FileWriter fw, final Long clientId) {

		try {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("disconnectReason", "Payment Due");
			jsonobject.put("disconnectionDate",dateFormat.format(DateUtils.getDateOfTenant()));
			jsonobject.put("dateFormat", "dd MMMM yyyy");
			jsonobject.put("locale", "en");
			final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonobject.toString());
			final JsonCommand command = JsonCommand.from(jsonobject.toString(),parsedCommand, this.fromApiJsonHelper, "DisconnectOrder",
					clientId, null, null, clientId, null, null, null, null,null, null, null);
			this.orderWritePlatformService.disconnectOrder(command,orderId);
			fw.append("Client Id " + clientId + " With this Order "	+ orderId + " has been disconnected via Payment Due on Dated "
					+ dateFormat.format(DateUtils.getDateOfTenant()) + "\r\n");

		} catch (Exception ex) {
			logger.error("Disconnection of Service Failed :" +ex.getMessage());
		}
	}

	/**
	 * @param orderId
	 * @param fw
	 * @param clientId
	 */
	public void suspendOrder(final Long orderId, FileWriter fw, final Long clientId) {

		try {
			JSONObject jsonobject = new JSONObject();
			jsonobject.put("suspensionReason", "Payment Due");
			jsonobject.put("suspensionDate",dateFormat.format(DateUtils.getDateOfTenant()));
			jsonobject.put("dateFormat", "dd MMMM yyyy");
			jsonobject.put("locale", "en");
			final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonobject.toString());
			final JsonCommand command = JsonCommand.from(jsonobject.toString(),parsedCommand, this.fromApiJsonHelper, "SuspendOrder",
					clientId, null, null, clientId, null, null, null, null,null, null, null);
			this.orderWritePlatformService.orderSuspention(command, orderId);
			fw.append("Client Id " + clientId + " With this Order " + orderId + " has been SUSPENDED via Payment Due on Dated "
					+ dateFormat.format(DateUtils.getDateOfTenant()) + "\r\n");

		} catch (Exception ex) {
			logger.error("Suspention of Service Failed :" +ex.getMessage());
		}
	}

	/**
	 * * @param clientId
	 */
	/*public void updateRtfType(final Long clientId) {

		Client client = this.clientRepository.findOne(clientId);
		if (null != client) {
			ClientCardDetails clientCardDetails = this.clientCardDetailsRepository.findOneByClient(client);
			if (null != clientCardDetails) {
				if ("I".equalsIgnoreCase(clientCardDetails.getRtftype())) {
					clientCardDetails.setRtftype("R");
					this.clientCardDetailsRepository.save(clientCardDetails);
					logger.info("RTF Type Updated with R" + clientId);
				}
			}
		}
	}*/

}