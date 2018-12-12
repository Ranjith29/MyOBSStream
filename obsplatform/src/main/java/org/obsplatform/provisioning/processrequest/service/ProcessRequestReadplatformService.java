package org.obsplatform.provisioning.processrequest.service;

import java.util.List;

import org.obsplatform.provisioning.processrequest.data.ProcessingDetailsData;

public interface ProcessRequestReadplatformService {

	List<ProcessingDetailsData> retrieveProcessingDetails();

	List<ProcessingDetailsData> retrieveUnProcessingDetails();

	Long retrievelatestReqId(Long clientId, String oldHardware);

	Boolean retrieveChangeCredentialsProcessRequests(Long clientId);

}
