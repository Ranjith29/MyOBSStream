package org.obsplatform.crm.clientprospect.service;

import org.codehaus.jettison.json.JSONException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface ClientProspectWritePlatformService {

	public CommandProcessingResult createProspect(JsonCommand command);

	public CommandProcessingResult followUpProspect(JsonCommand command, final Long prospectId);

	public CommandProcessingResult deleteProspect(JsonCommand command);

	public CommandProcessingResult convertToClient(Long entityId);

	public CommandProcessingResult updateProspect(JsonCommand command);
	
	public CommandProcessingResult createPaymentProspect(JsonCommand command, final Long clientId);

}
