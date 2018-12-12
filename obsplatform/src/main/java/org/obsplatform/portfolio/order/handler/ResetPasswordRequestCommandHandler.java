/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.order.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.order.service.OrderWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordRequestCommandHandler implements NewCommandSourceHandler {

	private final OrderWritePlatformService writePlatformService;

	@Autowired
	public ResetPasswordRequestCommandHandler(final OrderWritePlatformService writePlatformService) {
		
		this.writePlatformService = writePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.resetPasswordRequest(command, command.entityId());
	}
}