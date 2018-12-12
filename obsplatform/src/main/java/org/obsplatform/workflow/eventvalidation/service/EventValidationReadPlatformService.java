package org.obsplatform.workflow.eventvalidation.service;

import java.util.List;

import org.obsplatform.workflow.eventvalidation.data.EventValidationData;

public interface EventValidationReadPlatformService {

	List<EventValidationData> retrieveAllEventValidation();

	void checkForCustomValidations(Long clientId,String eventName, String strjson, Long userId);
	
}
