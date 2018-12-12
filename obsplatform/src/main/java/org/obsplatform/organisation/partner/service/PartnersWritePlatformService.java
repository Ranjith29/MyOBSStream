package org.obsplatform.organisation.partner.service;

import java.io.InputStream;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.domain.Base64EncodedImage;

public interface PartnersWritePlatformService {

	CommandProcessingResult createNewPartner(JsonCommand command);
	
	CommandProcessingResult updatePartner(JsonCommand command,Long partnerId);

	CommandProcessingResult saveOrUpdatePartnerImage(Long partnerId,
			String fileName, InputStream inputStream);

	CommandProcessingResult saveOrUpdatePartnerImage(Long partnerId,
			Base64EncodedImage base64EncodedImage);
	
}
