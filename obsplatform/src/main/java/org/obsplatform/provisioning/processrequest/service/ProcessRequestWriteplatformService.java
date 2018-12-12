package org.obsplatform.provisioning.processrequest.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;

public interface ProcessRequestWriteplatformService {

//	void ProcessingRequestDetails();

    CommandProcessingResult addProcessRequest(JsonCommand command);

	void notifyProcessingDetails(ProcessRequest detailsData, char status);

	//void postProvisioningdetails(Client client, EventOrder eventOrder,String requestType, String provsystem, String response);

}
