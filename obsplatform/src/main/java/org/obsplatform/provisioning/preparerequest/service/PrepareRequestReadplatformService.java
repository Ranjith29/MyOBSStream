package org.obsplatform.provisioning.preparerequest.service;

import java.util.List;

import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.provisioning.preparerequest.data.PrepareRequestData;
public interface PrepareRequestReadplatformService {

	List<PrepareRequestData> retrieveDataForProcessing();
	
	List<Long> retrieveRequestClientOrderDetails(Long clientId);
	
	CommandProcessingResult processingClientDetails(PrepareRequestData requestData);
	
	List<Long> getPrepareRequestDetails(Long id);
	
	int getLastPrepareId(Long orderId);

}
