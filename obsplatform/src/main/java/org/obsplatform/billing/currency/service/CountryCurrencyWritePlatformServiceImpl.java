package org.obsplatform.billing.currency.service;

import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.obsplatform.billing.chargecode.service.ChargeCodeWritePlatformServiceImp;
import org.obsplatform.billing.currency.domain.CountryCurrency;
import org.obsplatform.billing.currency.domain.CountryCurrencyRepository;
import org.obsplatform.billing.currency.exception.DuplicateCurrencyConfigurationException;
import org.obsplatform.billing.currency.serialization.CountryCurrencyCommandFromApiJsonDeserializer;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
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
public class CountryCurrencyWritePlatformServiceImpl implements CountryCurrencyWritePlatformService {

	private final static Logger LOGGER = LoggerFactory.getLogger(ChargeCodeWritePlatformServiceImp.class);
	private final PlatformSecurityContext context;
	private final CountryCurrencyCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final CountryCurrencyRepository countryCurrencyRepository;

	@Autowired
	public CountryCurrencyWritePlatformServiceImpl(final PlatformSecurityContext context,
			final CountryCurrencyCommandFromApiJsonDeserializer apiJsonDeserializer,
			final CountryCurrencyRepository countryCurrencyRepository) {

		this.context = context;
		this.fromApiJsonDeserializer = apiJsonDeserializer;
		this.countryCurrencyRepository = countryCurrencyRepository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * #createCountryCurrency(org.obsplatform.infrastructure.core.api.JsonCommand
	 * )
	 */
	@Transactional
	@Override
	public CommandProcessingResult createCountryCurrency(final JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final CountryCurrency countryCurrency = CountryCurrency.fromJson(command);
			this.countryCurrencyRepository.save(countryCurrency);
			return new CommandProcessingResult(countryCurrency.getId());

		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegretyIssue(dve, command);
			return new CommandProcessingResult(Long.valueOf(-1L));

		}

	}

	private void handleDataIntegretyIssue(final DataIntegrityViolationException dve, final JsonCommand command) {

		final Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("country_key")) {
			final String name = command.stringValueOfParameterNamed("country");
			throw new PlatformDataIntegrityException(
					"error.msg.countrycurrency.duplicate.configuration",
					"Country is already configured with'" + name + "'",
					"country", name);
			
		} else if (realCause.getMessage().contains("country_ISD")) {
			final String name = command.stringValueOfParameterNamed("countryISD");
			throw new PlatformDataIntegrityException(
					"error.msg.countrycurrency.duplicate.configuration",
					"Country is already configured with'" + name + "'",
					"countryISD", name);
		}
		
		

		LOGGER.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException(
				"error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #updateCountryCurrency(java.lang.Long,
	 * org.obsplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override
	public CommandProcessingResult updateCountryCurrency(final Long entityId,final JsonCommand command) {
		try {
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final CountryCurrency countryCurrency = retrieveCodeById(entityId);
			final Map<String, Object> changes = countryCurrency.update(command);
			if (!changes.isEmpty()) {
				this.countryCurrencyRepository.saveAndFlush(countryCurrency);
			}
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(countryCurrency.getId()).with(changes)
					.build();
		} catch (DataIntegrityViolationException dve) {
			if (dve.getCause() instanceof ConstraintViolationException) {
				handleDataIntegretyIssue(dve, command);
			}
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #deleteCountryCurrency(java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult deleteCountryCurrency(final Long entityId) {

		this.context.authenticatedUser();
		final CountryCurrency countryCurrency = retrieveCodeById(entityId);
		countryCurrency.delete();
		this.countryCurrencyRepository.save(countryCurrency);
		return new CommandProcessingResultBuilder().withEntityId(entityId).build();
	}

	private CountryCurrency retrieveCodeById(final Long currencyConfigId) {
		final CountryCurrency countryCurrency = this.countryCurrencyRepository.findOne(currencyConfigId);
		if (countryCurrency == null) {
			throw new DuplicateCurrencyConfigurationException(currencyConfigId);
		}
		return countryCurrency;
	}

}
