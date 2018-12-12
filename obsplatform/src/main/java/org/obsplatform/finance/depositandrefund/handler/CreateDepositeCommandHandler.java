package org.obsplatform.finance.depositandrefund.handler;
import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.depositandrefund.service.DepositeWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDepositeCommandHandler implements NewCommandSourceHandler {

	private final DepositeWritePlatformService depositewritePlatformService;

	@Autowired
	public CreateDepositeCommandHandler(
			final DepositeWritePlatformService depositewritePlatformService) {
		this.depositewritePlatformService = depositewritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.depositewritePlatformService.createDeposite(command);
	}
	
	
}

