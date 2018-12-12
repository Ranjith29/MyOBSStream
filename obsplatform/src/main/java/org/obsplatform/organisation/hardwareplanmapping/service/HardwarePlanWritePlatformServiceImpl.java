package org.obsplatform.organisation.hardwareplanmapping.service;

import java.util.List;
import java.util.Map;

import org.obsplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.hardwareplanmapping.data.HardwarePlanData;
import org.obsplatform.organisation.hardwareplanmapping.domain.HardwarePlanMapper;
import org.obsplatform.organisation.hardwareplanmapping.domain.HardwarePlanMapperRepository;
import org.obsplatform.organisation.hardwareplanmapping.exception.ItemCodeDuplicateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class HardwarePlanWritePlatformServiceImpl implements
		HardwarePlanWritePlatformService {
	
	private final static Logger LOGGER = LoggerFactory
			.getLogger(HardwarePlanWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final HardwarePlanMapperRepository hardwarePlanMapperRepository;
	private final HardwarePlanCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final HardwarePlanReadPlatformService hardwarePlanReadPlatformService;

	@Autowired
	public HardwarePlanWritePlatformServiceImpl(
			final PlatformSecurityContext context,
			final HardwarePlanMapperRepository hardwarePlanMapperRepository,
			final HardwarePlanCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final HardwarePlanReadPlatformService hardwarePlanReadPlatformService) {

		this.context = context;
		this.hardwarePlanMapperRepository = hardwarePlanMapperRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.hardwarePlanReadPlatformService = hardwarePlanReadPlatformService;

	}

	@Override
	public CommandProcessingResult createHardwarePlan(JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final HardwarePlanMapper harwarePlan = HardwarePlanMapper.fromJson(command);
			final List<HardwarePlanData> datas = this.hardwarePlanReadPlatformService.retrieveItems(harwarePlan.getItemCode());
			
			for (HardwarePlanData data : datas) {
				if (data.getplanCode().equalsIgnoreCase(harwarePlan.getPlanCode())) {
					throw new ItemCodeDuplicateException(harwarePlan.getPlanCode());
				}
			}
			
			this.hardwarePlanMapperRepository.save(harwarePlan);
			return new CommandProcessingResult(Long.valueOf(harwarePlan.getId()));

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {

		final Throwable realCause = dve.getMostSpecificCause();
		LOGGER.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException(
				"error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());

	}

	private HardwarePlanMapper retrieveHardwarePlanMappingById(final Long planId) {
		final HardwarePlanMapper hardwarePlanMapper = this.hardwarePlanMapperRepository.findOne(planId);
		if (hardwarePlanMapper == null) {
			throw new CodeNotFoundException(planId.toString());
		}
		return hardwarePlanMapper;
	}

	@Override
	public CommandProcessingResult updatePlanMapping(final Long planMapId,
			final JsonCommand command) {

		try {

			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());		
			final List<HardwarePlanData> datas = this.hardwarePlanReadPlatformService.retrieveItems(command.stringValueOfParameterNamed("itemCode"));
			for (HardwarePlanData data : datas) {
				if (data.getplanCode().equalsIgnoreCase(command.stringValueOfParameterNamed("planCode"))) {
					throw new ItemCodeDuplicateException(command.stringValueOfParameterNamed("planCode"));
				}
			}
			
			final HardwarePlanMapper hardwarePlanMapper = retrieveHardwarePlanMappingById(planMapId);
			final Map<String, Object> changes = hardwarePlanMapper.update(command);
			this.hardwarePlanMapperRepository.save(hardwarePlanMapper);

			return new CommandProcessingResultBuilder() //
					.withCommandId(command.commandId()) //
					.withEntityId(planMapId) //
					.with(changes) //
					.build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

}
