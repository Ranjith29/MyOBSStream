package org.obsplatform.organisation.ticketassignrule.service;

import java.util.HashMap;
import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.ticketassignrule.domain.TicketAssignRule;
import org.obsplatform.organisation.ticketassignrule.domain.TicketAssignRuleJpaRepository;
import org.obsplatform.organisation.ticketassignrule.serialization.TicketAssignRuleCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class TicketAssignRuleWritePlatformServiceImpl implements TicketAssignRuleWritePlatformService{
	
	private final static Logger logger = LoggerFactory.getLogger(TicketAssignRuleWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final TicketAssignRuleJpaRepository ticketAssignRuleJpaRepository;
	private final TicketAssignRuleCommandFromApiJsonDeserializer ticketAssignRuleCommandFromApiJsonDeserializer;
	
	@Autowired
	public TicketAssignRuleWritePlatformServiceImpl(final PlatformSecurityContext context,
			final TicketAssignRuleJpaRepository ticketAssignRuleJpaRepository,
			final TicketAssignRuleCommandFromApiJsonDeserializer ticketAssignRuleCommandFromApiJsonDeserializer) {
		this.context = context;
		this.ticketAssignRuleJpaRepository = ticketAssignRuleJpaRepository;
		this.ticketAssignRuleCommandFromApiJsonDeserializer = ticketAssignRuleCommandFromApiJsonDeserializer;
	}

	@Override
	public CommandProcessingResult createTicketAssignRule(JsonCommand command) {
	
	try{	
		context.authenticatedUser();
		this.ticketAssignRuleCommandFromApiJsonDeserializer.validateForCreate(command.json());
		final TicketAssignRule ticketassignrule = TicketAssignRule.fromJson(command);
		this.ticketAssignRuleJpaRepository.save(ticketassignrule);

		return new CommandProcessingResult(ticketassignrule.getId().toString());
	} catch (DataIntegrityViolationException dve) {
		handleDataIntegrityIssues(command, dve);
		return CommandProcessingResult.empty();
	}
}

	private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause();
		logger.error(dve.getMessage(), dve);
		if(realCause.getMessage().contains("businessprocessid_with_categorytype_uniquekey")) {
            final Long businessprocessId = command.longValueOfParameterNamed("businessprocessId");
            throw new PlatformDataIntegrityException("error.msg.businessprocess.duplicate.name", "A Business Process with Id '" + businessprocessId + "' already exists");
        }else{
		   throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource " + realCause.getMessage());
        }
	}

	@Override
	public CommandProcessingResult updateTicketAssignRule(Long entityId, JsonCommand command) {
		try {
			context.authenticatedUser();
			this.ticketAssignRuleCommandFromApiJsonDeserializer.validateForCreate(command.json());
			Map<String, Object> changes = new HashMap<String, Object>();
			final TicketAssignRule ticketAssignRule = this.ticketAssignRuleJpaRepository.findOne(entityId);
			changes = ticketAssignRule.update(command);
			this.ticketAssignRuleJpaRepository.save(ticketAssignRule);

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId()).withEntityId(entityId)
					.with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	@Override
	public CommandProcessingResult deleteTicketAssignRule(Long entityId) {
		try {
			context.authenticatedUser();
			final TicketAssignRule ticketAssignRule = this.ticketAssignRuleJpaRepository.findOne(entityId);
			ticketAssignRule.delete();
			this.ticketAssignRuleJpaRepository.save(ticketAssignRule);

			return new CommandProcessingResultBuilder().withEntityId(entityId).build();
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

}
