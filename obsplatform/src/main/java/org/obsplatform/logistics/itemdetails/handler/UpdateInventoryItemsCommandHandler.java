/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.logistics.itemdetails.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.logistics.itemdetails.service.ItemDetailsWritePlatformService;
import org.obsplatform.workflow.eventactionmapping.service.EventActionMappingWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateInventoryItemsCommandHandler implements NewCommandSourceHandler {

	private final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService;

    @Autowired
    public UpdateInventoryItemsCommandHandler(final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService) {
    	this.inventoryItemDetailsWritePlatformService = inventoryItemDetailsWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.inventoryItemDetailsWritePlatformService.updateItem(command.entityId(), command);
    }
}