package org.obsplatform.portfolio.isexdirectory.service;

import java.util.HashMap;
import java.util.Map;

import org.obsplatform.billing.selfcare.service.SelfCareWritePlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.isexdirectory.domain.IsExDirectory;
import org.obsplatform.portfolio.isexdirectory.domain.IsExDirectoryRepository;
import org.obsplatform.portfolio.isexdirectory.serialization.IsExDirectoryCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Naresh
 * 
 */
@Service
public class IsExDirectoryWritePlatformServiceImpl implements IsExDirectoryWritePlatformService {

	private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(IsExDirectoryWritePlatformService.class);

	private final PlatformSecurityContext context;
	private final IsExDirectoryCommandFromApiJsonDeserializer isExDirectoryCommandFromApiJsonDeserializer;
	private final IsExDirectoryRepository isExDirectoryRepository;
	private final SelfCareWritePlatformService selfCareWritePlatformService;

	@Autowired
	public IsExDirectoryWritePlatformServiceImpl(final PlatformSecurityContext context,
			final IsExDirectoryCommandFromApiJsonDeserializer isExDirectoryCommandFromApiJsonDeserializer,
			final IsExDirectoryRepository isExDirectoryRepository,
			final SelfCareWritePlatformService selfCareWritePlatformService) {

		this.context = context;
		this.isExDirectoryCommandFromApiJsonDeserializer = isExDirectoryCommandFromApiJsonDeserializer;
		this.isExDirectoryRepository = isExDirectoryRepository;
		this.selfCareWritePlatformService = selfCareWritePlatformService;
	}

	@Override
	public CommandProcessingResult createIsExDirectory(JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.isExDirectoryCommandFromApiJsonDeserializer.validateForCreate(command);
			IsExDirectory isExDirectory = IsExDirectory.fromJson(command);
			final boolean newIsExDirectory = command.booleanObjectValueOfParameterNamed("isExDirectory"); 
			final boolean newIsNumberWithHeld = command.booleanObjectValueOfParameterNamed("isNumberWithHeld");
			boolean oldIsExDirectory = false;
			boolean oldIsNumberWithHeld = false;
			boolean oldIsUmeeApp = false;
			final String serialNo = command.stringValueOfParameterNamed("serialNo");
			
			this.isExDirectoryRepository.save(isExDirectory);
			
			Map<String, Object> changes = new HashMap<String, Object>();
			
			if((oldIsExDirectory != newIsExDirectory) || (oldIsNumberWithHeld != newIsNumberWithHeld)){
				
				this.selfCareWritePlatformService.sendIsExDirectoryToProvisionSystem(isExDirectory, oldIsExDirectory, 
						oldIsNumberWithHeld, oldIsUmeeApp, serialNo, changes);
			}

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId())
					.withEntityId(isExDirectory.getId()).build();

		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	@Override
	public CommandProcessingResult updateIsExDirectory(final Long orderId, final JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.isExDirectoryCommandFromApiJsonDeserializer.validateForCreate(command);
			
			final boolean newIsExDirectory = command.booleanObjectValueOfParameterNamed("isExDirectory");
			final boolean newIsNumberWithHeld = command.booleanObjectValueOfParameterNamed("isNumberWithHeld");
			
			final IsExDirectory isExDirectoryData = this.retrieveIsExDirectoryByOrderId(orderId);
			
			boolean oldIsExDirectory = isExDirectoryData.getIsExDirectory();
			boolean oldIsNumberWithHeld = isExDirectoryData.getIsNumberWithHeld();
			boolean oldIsUmeeApp = isExDirectoryData.getIsUmeeApp();
			
			final String serialNo = command.stringValueOfParameterNamed("serialNo");

			final Map<String, Object> changes = isExDirectoryData.update(command);

			this.isExDirectoryRepository.saveAndFlush(isExDirectoryData);
			
            if((oldIsExDirectory != newIsExDirectory) || (oldIsNumberWithHeld != newIsNumberWithHeld)){
				
            	this.selfCareWritePlatformService.sendIsExDirectoryToProvisionSystem(isExDirectoryData, oldIsExDirectory, 
    					oldIsNumberWithHeld, oldIsUmeeApp, serialNo, changes);
			}
			
			

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId())
					.withEntityId(command.entityId()).with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private IsExDirectory retrieveIsExDirectoryByOrderId(Long orderId) {

		final IsExDirectory isExDirectory = this.isExDirectoryRepository.findOneByOrderId(orderId);

		return isExDirectory;
	}

	private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

		final Throwable realCause = dve.getMostSpecificCause();
		LOGGER.error(dve.getMessage(), dve);

		throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: " + dve.getMessage());
	}

}
