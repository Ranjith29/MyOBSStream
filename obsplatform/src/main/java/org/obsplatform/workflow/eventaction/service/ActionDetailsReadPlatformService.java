package org.obsplatform.workflow.eventaction.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.portfolio.order.data.SchedulingOrderData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.data.EventActionProcedureData;

public interface ActionDetailsReadPlatformService {
	
	List<ActionDetaislData> retrieveActionDetails(String eventType);
	
	EventActionProcedureData checkCustomeValidationForEvents(Long clientId,String eventName,String actionName,String resourceId);

	List<EventActionData> retrieveAllActionsForProccessing();

	Collection<SchedulingOrderData> retrieveClientSchedulingOrders(Long clientId);
	
	ActionDetaislData retrieveEventWithAction(String eventName,String ActionName);
}
