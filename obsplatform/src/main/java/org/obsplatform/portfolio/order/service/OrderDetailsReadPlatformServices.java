package org.obsplatform.portfolio.order.service;

import java.util.List;

import org.obsplatform.billing.planprice.data.PriceData;
import org.obsplatform.cms.eventmaster.data.EventMasterData;
import org.obsplatform.portfolio.order.data.OrderData;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.plan.data.ServiceData;

public interface OrderDetailsReadPlatformServices {
	
	List<ServiceData> retrieveAllServices(Long plan_code);

	List<PriceData> retrieveAllPrices(Long plan_code, String billingFreq,Long clientId);

	List<PriceData> retrieveDefaultPrices(Long planId, String billingFrequency,Long clientId);

	Long retrieveClientActivePlanOrdersCount(Long clientId, Long planId);
	
	List<Long> retrieveDisconnectingOrderSecondaryConnections(Long clientId,Long planId);

	List<Long> retrieveChangingOrderSecondaryConnections(Long clientId,Long planId);
	
	List<Long> retrieveTerminatableOrderSecondaryConnections(Long clientId,Long planId);

	List<Long> retrieveSuspendableOrderSecondaryConnections(Long clientId,Long planId);

	List<Long> retrieveReactivableOrderSecondaryConnections(Long clientId,Long planId);

	List<Long> retrieveReconnectingOrderSecondaryConnections(Long clientId,Long planId);

	List<Long> retrieveRenewalOrderSecondaryConnections(Long clientId,Long planId, Long orderStatus);

	List<PriceData> retrievCustomerRegionPlanPrices(Long planId, Long clientId,String billFrequency,String state, String country);

	List<ServiceData> retrieveReconnectionPrices(Long clientId,Long planId, Long orderId);

	List<EventMasterData> retrieveAllEvents(Long planId);
}
