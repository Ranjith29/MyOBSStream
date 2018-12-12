package org.obsplatform.organisation.department.service;

import java.util.HashMap;
import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.department.domain.Department;
import org.obsplatform.organisation.department.domain.DepartmentRepository;
import org.obsplatform.organisation.department.serialization.DepartmentCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class DepartmentWritePlatformServiceImpl implements DepartmentWritePlatformService{

	private final static Logger logger = LoggerFactory.getLogger(DepartmentWritePlatformServiceImpl.class);
	private final DepartmentRepository departmentRepository;
	private final PlatformSecurityContext context;
	private final DepartmentCommandFromApiJsonDeserializer departmentCommandFromApiJsonDeserializer;
	
	@Autowired
	public DepartmentWritePlatformServiceImpl(final DepartmentRepository departmentRepository,
			final PlatformSecurityContext context,
			final DepartmentCommandFromApiJsonDeserializer departmentCommandFromApiJsonDeserializer){
		
		this.departmentRepository = departmentRepository;
		this.context = context;
		this.departmentCommandFromApiJsonDeserializer = departmentCommandFromApiJsonDeserializer;
	}
	
	@Override
	public CommandProcessingResult createDepartment(final JsonCommand command) {

		try {
			context.authenticatedUser();
			this.departmentCommandFromApiJsonDeserializer.validateForCreate(command.json());
			final Department department = Department.fromJson(command);
			this.departmentRepository.save(department);

			return new CommandProcessingResult(department.getId());
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	private void handleDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause();
		logger.error(dve.getMessage(),dve);
		throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource "+ realCause.getMessage());
	}

	@Override
	public CommandProcessingResult updateDepartment(final Long entityId,final JsonCommand command) {
		
		try {
			context.authenticatedUser();
			this.departmentCommandFromApiJsonDeserializer.validateForUpdate(command.json());
			Map<String, Object> changes = new HashMap<String, Object>();
			final Department department = this.departmentRepository.findOne(entityId);
			changes = department.update(command);
			this.departmentRepository.save(department);

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId()).withEntityId(entityId)
					.with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	@Override
	public CommandProcessingResult deleteDepartment(Long entityId) {
		
		try {
			context.authenticatedUser();
			final Department department = this.departmentRepository.findOne(entityId);
			department.delete();
			this.departmentRepository.save(department);

			return new CommandProcessingResultBuilder().withEntityId(entityId).build();
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
}

