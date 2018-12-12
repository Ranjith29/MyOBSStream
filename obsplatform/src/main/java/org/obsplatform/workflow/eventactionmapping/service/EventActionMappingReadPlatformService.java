package org.obsplatform.workflow.eventactionmapping.service;

import java.util.List;

import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.workflow.eventactionmapping.data.EventActionMappingData;

public interface EventActionMappingReadPlatformService {

	List<EventActionMappingData> retrieveAllEventMapping();

	List<McodeData> retrieveEventMapData(String str);

	EventActionMappingData retrieveEventActionDetail(Long id);

	List<EventActionMappingData> retrieveEvents(String event);

}
