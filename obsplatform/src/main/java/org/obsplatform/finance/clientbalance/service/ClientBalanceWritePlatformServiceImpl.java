package org.obsplatform.finance.clientbalance.service;

import org.obsplatform.finance.clientbalance.domain.ClientBalance;
import org.obsplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.obsplatform.finance.clientbalance.serialization.ClientBalanceCommandFromApiJsonDeserializer;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


@Service
public  class ClientBalanceWritePlatformServiceImpl implements ClientBalanceWritePlatformService{
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ClientBalanceWritePlatformServiceImpl.class);

	private final PlatformSecurityContext context;
	private final ClientBalanceCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private ClientBalanceRepository clientBalanceRepository;

	@Autowired
	public ClientBalanceWritePlatformServiceImpl(final PlatformSecurityContext context,
			final ClientBalanceRepository clientBalanceRepository,
			final ClientBalanceCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
		this.context = context;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.clientBalanceRepository = clientBalanceRepository;
	}

	@Override
	public CommandProcessingResult addClientBalance(JsonCommand command) {
		try {
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final ClientBalance clientBalance = ClientBalance.fromJson(command);
			this.clientBalanceRepository.save(clientBalance);
			return new CommandProcessingResult(clientBalance.getId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		
		Throwable realCause = dve.getMostSpecificCause();
		LOGGER.error(realCause.getMessage());
		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "+ realCause.getMessage());

	}
}
