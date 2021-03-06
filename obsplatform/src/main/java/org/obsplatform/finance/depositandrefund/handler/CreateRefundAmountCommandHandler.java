package org.obsplatform.finance.depositandrefund.handler;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.depositandrefund.service.RefundWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRefundAmountCommandHandler implements NewCommandSourceHandler {

	private final RefundWritePlatformService writePlatformService;

	@Autowired
	public CreateRefundAmountCommandHandler(
			final RefundWritePlatformService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.writePlatformService.createRefund(command,command.entityId());
	}
	
	
}

