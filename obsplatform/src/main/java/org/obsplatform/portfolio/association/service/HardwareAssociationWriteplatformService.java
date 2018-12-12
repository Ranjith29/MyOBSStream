package org.obsplatform.portfolio.association.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface HardwareAssociationWriteplatformService {

	Long createNewHardwareAssociation(Long clientId, Long planId, String serialNo, Long orderId, String allocationType,Long ServiceId);

	CommandProcessingResult createAssociation(JsonCommand command);

	CommandProcessingResult updateAssociation(JsonCommand command);

	CommandProcessingResult deAssociationHardware(Long orderId);


}
