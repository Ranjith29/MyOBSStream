package org.obsplatform.workflow.eventaction.service;

import org.obsplatform.scheduledjobs.scheduledjobs.data.EventActionData;

public interface ProcessEventActionService {

	void processEventActions(EventActionData eventActionData);
	

}
