package org.obsplatform.organisation.officeadjustments.service;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.finance.officebalance.data.OfficeBalanceData;
import org.obsplatform.finance.officebalance.domain.OfficeBalance;
import org.obsplatform.finance.officebalance.domain.OfficeBalanceRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.office.domain.Office;
import org.obsplatform.organisation.office.domain.OfficeRepository;
import org.obsplatform.organisation.office.exception.OfficeNotFoundException;
import org.obsplatform.organisation.officeadjustments.domain.OfficeAdjustments;
import org.obsplatform.organisation.officeadjustments.domain.OfficeAdjustmentsRepository;
import org.obsplatform.organisation.officeadjustments.serializer.OfficeAdjustmentsCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hugo
 * 
 */
@Service
public class OfficeAdjustmentsWritePlatformServiceImpl implements OfficeAdjustmentsWritePlatformService {

	private final PlatformSecurityContext context;
	private final OfficeAdjustmentsRepository officeAdjustmentsRepository;
	private final OfficeAdjustmentsCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final OfficeAdjustmentsReadPaltformService officeAdjustmentsReadPaltformService;
	private final OfficeBalanceRepository officeBalanceRepository;
	private final OfficeRepository officeRepository;

	@Autowired
	public OfficeAdjustmentsWritePlatformServiceImpl(final PlatformSecurityContext context,
			final OfficeAdjustmentsRepository officeAdjustmentsRepository,
			final OfficeAdjustmentsCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final OfficeAdjustmentsReadPaltformService officeAdjustmentsReadPaltformService,
			final OfficeBalanceRepository officeBalanceRepository,
			final OfficeRepository officeRepository) {
		
		this.context = context;
		this.officeAdjustmentsRepository = officeAdjustmentsRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.officeAdjustmentsReadPaltformService = officeAdjustmentsReadPaltformService;
		this.officeBalanceRepository = officeBalanceRepository;
		this.officeRepository = officeRepository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * #createOfficeAdjustment(org.obsplatform.infrastructure.core.api.JsonCommand
	 * )
	 */
	@Transactional
	@Override
	public CommandProcessingResult createOfficeAdjustment(final JsonCommand command) {

		try {
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final OfficeAdjustments officeAdjustment = OfficeAdjustments.fromJson(command);
			
			//check exsists or not
			final List<OfficeBalanceData> balanceDatas = this.officeAdjustmentsReadPaltformService.retrieveOfficeBalance(command.entityId());
			OfficeBalance officeBalance = null;
			if (balanceDatas.size() != 0 ) {
				officeBalance = this.officeBalanceRepository.findOne(balanceDatas.get(0).getId());
			}
			if (officeBalance != null) {
				officeBalance = doUpdateAdjustmentOfficeBalance(command,officeBalance);

			} else if (officeBalance == null) {
				officeBalance = createAdjustmentOfficeBalance(command,officeBalance);
			}

		    this.saveOfficeBalanceEntity(officeBalance);
			this.officeAdjustmentsRepository.save(officeAdjustment);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(officeAdjustment.getId()).withOfficeId(command.entityId()).build();
		} catch (DataIntegrityViolationException dve) {
			return new CommandProcessingResult(Long.valueOf(-1L));
		}
	}

	public OfficeBalance createAdjustmentOfficeBalance(final JsonCommand command,OfficeBalance officeBalance) {

		officeBalance = calculateCreateOfficeBalance(OfficeAdjustments.fromJson(command).getAdjustmentType(), OfficeAdjustments
						.fromJson(command).getAmountPaid(), officeBalance,command.entityId());
		return officeBalance;
	}

	public OfficeBalance doUpdateAdjustmentOfficeBalance(final JsonCommand command, OfficeBalance officeBalance) {

		officeBalance = calculateUpdateOfficeBalance(OfficeAdjustments.fromJson(command).getAdjustmentType(), OfficeAdjustments
				.fromJson(command).getAmountPaid(), officeBalance);
		return officeBalance;
	}

	public OfficeBalance calculateUpdateOfficeBalance(final String transactionType, BigDecimal amount,OfficeBalance officeBalance) {

		if (amount == null)
			amount = new BigDecimal(0);
		if (transactionType.equalsIgnoreCase("DEBIT")) {
	    	officeBalance.setBalanceAmount(officeBalance.getBalanceAmount().add(amount));
		} else if (transactionType.equalsIgnoreCase("CREDIT")) {
			officeBalance.setBalanceAmount(officeBalance.getBalanceAmount().subtract(amount));
		}
		return officeBalance;

	}

	public OfficeBalance calculateCreateOfficeBalance(final String transactionType, BigDecimal amount,OfficeBalance officeBalance, 
			  final Long officeId) {
		
		BigDecimal balanceAmount = BigDecimal.ZERO;
		
		if (transactionType.equalsIgnoreCase("DEBIT")) {
			balanceAmount = amount;
		} else if (transactionType.equalsIgnoreCase("CREDIT")) {
			balanceAmount = BigDecimal.ZERO.subtract(amount);
		}
		officeBalance = new OfficeBalance(officeId, balanceAmount);

		return officeBalance;

	}
	
	public OfficeBalance saveOfficeBalanceEntity(OfficeBalance officeBalance) {

		Office office = this.officeRepository.findOne(officeBalance.getofficeId());
		if (office == null) {
			throw new OfficeNotFoundException(officeBalance.getofficeId());
		}
		OfficeBalance resultantOfficeBalance = this.officeBalanceRepository.save(officeBalance);
		return resultantOfficeBalance;
	}

}
