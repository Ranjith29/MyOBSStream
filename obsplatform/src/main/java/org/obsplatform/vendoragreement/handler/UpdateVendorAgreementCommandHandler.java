/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.obsplatform.vendoragreement.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.vendoragreement.service.VendorAgreementWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateVendorAgreementCommandHandler implements NewCommandSourceHandler {

    private final VendorAgreementWritePlatformService writePlatformService;

    @Autowired
    public UpdateVendorAgreementCommandHandler(final VendorAgreementWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        final Long vendorAgreementId = command.entityId();
        
        return this.writePlatformService.updateVendorAgreement(vendorAgreementId, command);
    }
}
