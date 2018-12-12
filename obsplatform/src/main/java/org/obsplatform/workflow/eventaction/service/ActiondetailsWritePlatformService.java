package org.obsplatform.workflow.eventaction.service;

import java.util.List;

import org.obsplatform.workflow.eventaction.data.ActionDetaislData;

public interface ActiondetailsWritePlatformService {

	String AddNewActions(List<ActionDetaislData> actionDetaislDatas, Long clientId, String resorceId, String ticketURL);

	//void ProcessEventActions(EventActionData eventActionData);

}
