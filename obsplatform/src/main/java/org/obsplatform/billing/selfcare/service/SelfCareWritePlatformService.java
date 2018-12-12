package org.obsplatform.billing.selfcare.service;

import java.util.Map;

import org.obsplatform.billing.selfcare.domain.SelfCare;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.isexdirectory.domain.IsExDirectory;

public interface SelfCareWritePlatformService {

	CommandProcessingResult createSelfCare(JsonCommand command);

	CommandProcessingResult createSelfCareUDPassword(JsonCommand command);

	CommandProcessingResult updateClientStatus(JsonCommand command, Long entityId);

	CommandProcessingResult registerSelfCare(JsonCommand command);

	CommandProcessingResult selfCareEmailVerification(JsonCommand command);

	CommandProcessingResult generateNewSelfcarePassword(JsonCommand command);

	CommandProcessingResult changeSelfcarePassword(JsonCommand command);

	CommandProcessingResult updateSelfCareUDPassword(JsonCommand command);

	CommandProcessingResult forgotSelfCareUDPassword(JsonCommand command);

	void sendCredentialToProvisionSystem(SelfCare selfCare, String existingUserName, String existingPassword,
			Map<String,Object> credentialChanges);

	void sendIsExDirectoryToProvisionSystem(IsExDirectory isExDirectoryData, boolean oldIsExDirectory, boolean oldIsNumberWithHeld, 
			boolean oldIsUmeeApp, String serialNo, Map<String,Object> exDirectoryChanges);

}
