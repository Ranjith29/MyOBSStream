package org.obsplatform.organisation.officepayments.service;

import java.math.BigDecimal;


import org.obsplatform.finance.officebalance.domain.OfficeBalance;
import org.obsplatform.finance.officebalance.domain.OfficeBalanceRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.officepayments.domain.OfficePayments;
import org.obsplatform.organisation.officepayments.domain.OfficePaymentsRepository;
import org.obsplatform.organisation.officepayments.serialization.OfficePaymentsCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hugo
 *
 */
@Service
public class OfficePaymentsWritePlatformServiceImpl implements OfficePaymentsWritePlatformService {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(OfficePaymentsWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final OfficePaymentsRepository officePaymentsRepository;
	private final OfficePaymentsCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final OfficeBalanceRepository officeBalanceRepository;
	
	@Autowired
	public OfficePaymentsWritePlatformServiceImpl(final PlatformSecurityContext context, 
				final OfficePaymentsRepository  officePaymentsRepository,
				final OfficePaymentsCommandFromApiJsonDeserializer fromApiJsonDeserializer,
				final OfficeBalanceRepository officeBalanceRepository){
		
		this.context = context;
		this.officePaymentsRepository = officePaymentsRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.officeBalanceRepository = officeBalanceRepository;
	}

	/* (non-Javadoc)
	 * @see #createOfficePayment(org.obsplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createOfficePayment(final JsonCommand command) {

		try {
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final OfficePayments officePayments = OfficePayments.fromJson(command);
			this.officePaymentsRepository.save(officePayments);
			
			OfficeBalance officeBalance =this.officeBalanceRepository.findOneByOfficeId(officePayments.getOfficeId());
			
			if(officeBalance != null){
				officeBalance.updateBalance("CREDIT",officePayments.getAmountPaid());
			
			}else if(officeBalance == null){
				
                    BigDecimal balance=BigDecimal.ZERO.subtract(officePayments.getAmountPaid());
                    officeBalance =OfficeBalance.create(officePayments.getOfficeId(),balance);
			}
			this.officeBalanceRepository.saveAndFlush(officeBalance);
			
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					     .withEntityId(officePayments.getId()).withOfficeId(command.entityId()).build();
		}catch(DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	private void handleCodeDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
		
		 final Throwable realCause = dve.getMostSpecificCause();
	     if (realCause.getMessage().contains("receipt_no")) {
	    	 final String name = command.stringValueOfParameterNamed("receiptNo");
	         throw new PlatformDataIntegrityException("error.msg.officePayment_receiptNo.duplicate.name", "A Receipt Number with this Code'"
	                    + name + "'already exists", "receiptNo", name);
	     }

	     LOGGER.error(dve.getMessage(), dve);
	     throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource: " + realCause.getMessage());
		
	}

}