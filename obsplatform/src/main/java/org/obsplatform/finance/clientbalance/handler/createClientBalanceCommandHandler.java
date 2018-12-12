package org.obsplatform.finance.clientbalance.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.clientbalance.service.ClientBalanceWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class createClientBalanceCommandHandler implements NewCommandSourceHandler{

	
	private final ClientBalanceWritePlatformService balanceWritePlatformService;
	
	@Autowired
	public createClientBalanceCommandHandler(final ClientBalanceWritePlatformService balanceWritePlatformService) {
		this.balanceWritePlatformService = balanceWritePlatformService;
		
	}
	
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return balanceWritePlatformService.addClientBalance(command);
	}
}
