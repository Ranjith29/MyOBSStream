package org.obsplatform.portfolio.order.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.order.service.OrderAddOnsWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteOrderAddonsCommandHandler implements NewCommandSourceHandler {

	private final OrderAddOnsWritePlatformService orderAddOnsWritePlatformService;

	@Autowired
	public DeleteOrderAddonsCommandHandler(final OrderAddOnsWritePlatformService addOnsWritePlatformService) {
		this.orderAddOnsWritePlatformService = addOnsWritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {
		return this.orderAddOnsWritePlatformService.deleteOrderAddon(command, command.entityId());
	}

}
