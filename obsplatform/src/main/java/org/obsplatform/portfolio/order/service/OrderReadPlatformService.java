package org.obsplatform.portfolio.order.service;

import java.util.List;

import org.obsplatform.billing.payterms.data.PaytermData;
import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.portfolio.order.data.OrderAddonsData;
import org.obsplatform.portfolio.order.data.OrderData;
import org.obsplatform.portfolio.order.data.OrderDiscountData;
import org.obsplatform.portfolio.order.data.OrderHistoryData;
import org.obsplatform.portfolio.order.data.OrderLineData;
import org.obsplatform.portfolio.order.data.OrderPriceData;
import org.obsplatform.portfolio.plan.data.PlanCodeData;

public interface OrderReadPlatformService {

	List<PlanCodeData> retrieveAllPlatformData(Long planId, Long clientId);

	List<PaytermData> retrieveAllPaytermData();
	
	List<OrderPriceData> retrieveOrderPriceData(Long orderId);
	
	List<PaytermData> getChargeCodes(Long planCode, Long clientId);
	
	List<OrderPriceData> retrieveOrderPriceDetails(Long orderId, Long clientId);
	
	List<OrderData> retrieveClientOrderDetails(Long clientId);
	
	List<OrderHistoryData> retrieveOrderHistoryDetails(String orderNo);
	
	List<OrderData> getActivePlans(Long clientId, String planType);
	
	OrderData retrieveOrderDetails(Long orderId);
	
	Long getRetrackId(Long entityId);
	
	String getOSDTransactionType(Long id);
	
	String checkRetrackInterval(Long entityId);
	
	List<OrderLineData> retrieveOrderServiceDetails(Long orderId);
	
	List<OrderDiscountData> retrieveOrderDiscountDetails(Long orderId);

	Long retrieveClientActiveOrderDetails(Long clientId, String serialNo);

	List<OrderData> retrieveCustomerActiveOrders(Long clientId);
	
	List<Long> retrieveOrderActiveAndDisconnectionIds(Long clientId,Long planId);
	
	List<Long> getEventActionsData(Long clientId, Long orderId);

	List<OrderData> primaryOrderDetails(Long planId, Long clientId);
	
	List<EventMasterData> retrieveOrderEventDetails(Long orderId);
	
	List<Long> retrieveCustomerActiveOrderIds(Long clientId);
	
	List<Long> retrieveCustomerSuspendedOrderIds(Long clientId);
	
	List<Long> retrieveCustomerTalkSuspendedOrders(Long clientId);
	
	List<Long> retrieveCustomerTalkDisconnectedOrders(Long clientId);
	
	List<OrderAddonsData> getNewPlanAddon(Long id);

	OrderAddonsData retrieveTalkAddons(Long clientId, Long planId);
	
	List<OrderData> clientActiveOrderDetails( Long clientId);
	
	PlanCodeData getPlanDetails(Long id);

	OrderPriceData findNewTalkAddonsPriceByTalkOrderId(Long orderId);
	
	OrderAddonsData findNewAddonsPriceByOrderId(Long orderId);
}
