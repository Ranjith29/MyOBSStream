/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.client.service;

import java.io.InputStream;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.domain.Base64EncodedImage;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ClientWritePlatformService {

    CommandProcessingResult createClient(JsonCommand command);

    CommandProcessingResult updateClient(Long clientId, JsonCommand command);

    CommandProcessingResult activateClient(Long clientId, JsonCommand command);

    CommandProcessingResult deleteClient(Long clientId,JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CLIENTIMAGE')")
    CommandProcessingResult saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CLIENTIMAGE')")
    CommandProcessingResult saveOrUpdateClientImage(Long clientId, Base64EncodedImage encodedImage);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_CLIENTIMAGE')")
    CommandProcessingResult deleteClientImage(Long clientId);

	CommandProcessingResult updateClientTaxExemption(Long entityId,JsonCommand command);

	CommandProcessingResult updateClientBillModes(Long entityId,JsonCommand command);

	CommandProcessingResult createParentClient(Long entityId, JsonCommand command);

	CommandProcessingResult deleteChildFromParentClient(Long entityId,JsonCommand command);

	CommandProcessingResult createClientAdditionalInfo(JsonCommand command,Long entityId);

	CommandProcessingResult updateClientAdditionalInfo(JsonCommand command);

	CommandProcessingResult updateBeesmartClient(JsonCommand command);

	CommandProcessingResult deleteBeesmartClient(JsonCommand command,Long entityId);

	CommandProcessingResult deleteCustomerNoRecord(Long clientId,JsonCommand command);

	CommandProcessingResult updateClientRequiredFields(Long entityId, JsonCommand command);
}