package org.obsplatform.portfolio.servicepartnermapping.service;

import java.util.Map;
import org.obsplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.contract.domain.Contract;
import org.obsplatform.portfolio.servicepartnermapping.domain.ServicePartnerMapping;
import org.obsplatform.portfolio.servicepartnermapping.domain.ServicePartnerMappingRepository;
import org.obsplatform.portfolio.servicepartnermapping.serialization.ServicePartnerMappingCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * @author Naresh
 * 
 */
@Service
public class ServicePartnerMappingWritePlatformServiceImpl implements ServicePartnerMappingWritePlatformService {

	private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ServicePartnerMappingWritePlatformServiceImpl.class);

	private final PlatformSecurityContext platformSecurityContext;
	private final ServicePartnerMappingRepository servicePartnerMappingRepository;
	private final ServicePartnerMappingCommandFromApiJsonDeserializer servicePartnerMappingCommandFromApiJsonDeserializer;

	@Autowired
	public ServicePartnerMappingWritePlatformServiceImpl(final PlatformSecurityContext platformSecurityContext,
			final ServicePartnerMappingRepository servicePartnerMappingRepository,
			final ServicePartnerMappingCommandFromApiJsonDeserializer servicePartnerMappingCommandFromApiJsonDeserializer) {

		this.platformSecurityContext = platformSecurityContext;
		this.servicePartnerMappingRepository = servicePartnerMappingRepository;
		this.servicePartnerMappingCommandFromApiJsonDeserializer = servicePartnerMappingCommandFromApiJsonDeserializer;

	}

	@Override
	public CommandProcessingResult createServicePartnerMapping(final JsonCommand command) {

		try {
			platformSecurityContext.authenticatedUser();
			this.servicePartnerMappingCommandFromApiJsonDeserializer.validateForCreate(command.json());
			final ServicePartnerMapping servicePartnerMapping = ServicePartnerMapping.fromJson(command);
			servicePartnerMappingRepository.save(servicePartnerMapping);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(servicePartnerMapping.getId()).build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	@Override
	public CommandProcessingResult updateServicePartnerMapping(final Long servicePtrMapId, final JsonCommand command) {
		try {
			platformSecurityContext.authenticatedUser();
			this.servicePartnerMappingCommandFromApiJsonDeserializer.validateForCreate(command.json());
			final ServicePartnerMapping servicePartnerMapping = retrieveServicePartnerMappingById(servicePtrMapId);

			final Map<String, Object> changes = servicePartnerMapping.update(command);
			if (!changes.isEmpty()) {
				this.servicePartnerMappingRepository.saveAndFlush(servicePartnerMapping);
			}

			return new CommandProcessingResultBuilder().withCommandId(command.commandId()) //
					.withEntityId(servicePtrMapId).with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}
	
	@Override
	public CommandProcessingResult deleteServicePartnerMapping(final Long servicePtrMapId) {
		
		platformSecurityContext.authenticatedUser();
		final ServicePartnerMapping servicePartnerMapping = retrieveServicePartnerMappingById(servicePtrMapId);
		servicePartnerMapping.delete();
		this.servicePartnerMappingRepository.save(servicePartnerMapping);
		return new CommandProcessingResultBuilder().withEntityId(servicePtrMapId).build();
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

		final Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("service_code_uq")) {
			
			final Long serviceId = command.longValueOfParameterNamed("serviceId");
			throw new PlatformDataIntegrityException("error.msg.service.mapping.duplicate", "A code with Service Id '" + serviceId + "' already exists", "serviceId");

		}
		LOGGER.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: " + realCause.getMessage());
	}

	private ServicePartnerMapping retrieveServicePartnerMappingById(Long servicePtrMapId) {
		final ServicePartnerMapping servicePartnerMapping = this.servicePartnerMappingRepository.findOne(servicePtrMapId);
		if (servicePartnerMapping == null) {
			throw new CodeNotFoundException(servicePtrMapId.toString());
		}
		return servicePartnerMapping;
	}
}
