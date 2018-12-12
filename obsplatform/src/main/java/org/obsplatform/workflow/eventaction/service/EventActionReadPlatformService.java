package org.obsplatform.workflow.eventaction.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.obsplatform.workflow.eventaction.data.OrderNotificationData;
import org.obsplatform.workflow.eventaction.data.VolumeDetailsData;

public interface EventActionReadPlatformService {

	VolumeDetailsData retrieveVolumeDetails(Long id);
	
	Page<EventActionData> retriveAllEventActions(SearchSqlQuery searchTicketMaster, String statusType);
	
	List<EventActionData> retrievePendingActionRequest(Long paymentgatewayId);

	List<EventActionData> retrievePendingRecurringRequest(Long clientId);
	
	OrderNotificationData retrieveNotifyDetails(Long clientId, Long orderId);

}
