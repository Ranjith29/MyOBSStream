/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.client.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.client.service.ClientWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateClientCommandHandler implements NewCommandSourceHandler {

    private final ClientWritePlatformService clientWritePlatformService;

    @Autowired
    public UpdateClientCommandHandler(final ClientWritePlatformService clientWritePlatformService) {
        this.clientWritePlatformService = clientWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.clientWritePlatformService.updateClient(command.entityId(), command);
    }
}