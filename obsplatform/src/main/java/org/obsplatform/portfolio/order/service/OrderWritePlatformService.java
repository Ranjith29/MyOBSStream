package org.obsplatform.portfolio.order.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.plan.domain.Plan;

public interface OrderWritePlatformService {
	
	CommandProcessingResult createOrder(Long entityId, JsonCommand command);
	
	CommandProcessingResult updateOrderPrice(Long orderId, JsonCommand command);
	
	CommandProcessingResult deleteOrder(Long orderId,JsonCommand command);
	
	CommandProcessingResult renewalClientOrder(JsonCommand command,Long orderId);
	
	CommandProcessingResult reconnectOrder(Long entityId);
	
	CommandProcessingResult disconnectOrder(JsonCommand command, Long orderId);
	
	CommandProcessingResult retrackOsdMessage(JsonCommand command);
	
	CommandProcessingResult changePlan(JsonCommand command, Long entityId);
	
	CommandProcessingResult applyPromo(JsonCommand command);
	
	CommandProcessingResult scheduleOrderCreation(Long entityId,JsonCommand command);
	
	CommandProcessingResult deleteSchedulingOrder(Long entityId,JsonCommand command);

	CommandProcessingResult orderExtension(JsonCommand command, Long entityId);

	CommandProcessingResult orderTermination(JsonCommand command, Long entityId);

	CommandProcessingResult orderSuspention(JsonCommand command, Long entityId);

	CommandProcessingResult reactiveOrder(JsonCommand command, Long entityId);
	
	void processNotifyMessages(String eventName, Long clientId, String orderId, String actionType);
	
	void checkingContractPeriodAndBillfrequncyValidation(Long contractPeriod, String paytermCode);
	
	CommandProcessingResult renewalOrderWithClient(JsonCommand command,Long clientId);

	Plan findPlanWithNotFoundDetection(Long planId);

	CommandProcessingResult scheduleOrderUpdation(Long entityId,JsonCommand command);
	
	CommandProcessingResult deleteOrderWithNoRecord(Long orderId,JsonCommand command);

	CommandProcessingResult newPasswordRequest(JsonCommand command, Long entityId);

	CommandProcessingResult resetPasswordRequest(JsonCommand command, Long entityId);
	
}
