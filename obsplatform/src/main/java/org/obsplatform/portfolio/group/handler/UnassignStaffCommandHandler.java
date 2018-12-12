/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.group.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.group.service.GroupingTypesWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnassignStaffCommandHandler implements NewCommandSourceHandler {

    private final GroupingTypesWritePlatformService groupWritePlatformService;

    @Autowired
    public UnassignStaffCommandHandler(final GroupingTypesWritePlatformService groupWritePlatformService) {
        this.groupWritePlatformService = groupWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.groupWritePlatformService.unassignStaff(command.entityId(), command);
    }
}