package org.obsplatform.portfolio.service.service;

import java.util.Map;

import org.obsplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.service.domain.ServiceMaster;
import org.obsplatform.portfolio.service.domain.ServiceMasterRepository;
import org.obsplatform.portfolio.service.serialization.ServiceCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ServiceMasterWritePlatformServiceImpl  implements ServiceMasterWritePlatformService{
	
	private final static Logger logger = LoggerFactory.getLogger(ServiceMasterWritePlatformServiceImpl.class);
	
	private final PlatformSecurityContext context;
	private final ServiceMasterRepository serviceMasterRepository;
	private final ServiceCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	
 @Autowired
	public ServiceMasterWritePlatformServiceImpl(final PlatformSecurityContext context,
			final ServiceMasterRepository serviceMasterRepository,
			final ServiceCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
	 
		this.context = context;
		this.serviceMasterRepository = serviceMasterRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
	}
 
    @Transactional
	@Override
	public CommandProcessingResult createNewService(final JsonCommand command) {
		try {
			   context.authenticatedUser();
			   this.fromApiJsonDeserializer.validateForCreate(command.json());
			   final ServiceMaster serviceMaster = ServiceMaster.fromJson(command);
			   this.serviceMasterRepository.save(serviceMaster);
			   
			   return new CommandProcessingResult(serviceMaster.getId());
		} catch (DataIntegrityViolationException dve) {
			 handleCodeDataIntegrityIssues(command, dve);
			return  CommandProcessingResult.empty();
		}
	}
    
	@Override
	public CommandProcessingResult updateService(final Long id,final JsonCommand command) {
		
		try{
			    context.authenticatedUser();
			    this.fromApiJsonDeserializer.validateForCreate(command.json());
		        final ServiceMaster master = retrieveCodeBy(id);
		        final Map<String, Object> changes = master.update(command);
	            if (!changes.isEmpty()) {
	                this.serviceMasterRepository.saveAndFlush(master);
	            }
	            
         return new CommandProcessingResultBuilder() //
         .withCommandId(command.commandId()) //
         .withEntityId(id) //
         .with(changes) //
         .build();
	} catch (DataIntegrityViolationException dve) {
		 handleCodeDataIntegrityIssues(command, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}
	}
	 private void handleCodeDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
	        final Throwable realCause = dve.getMostSpecificCause();
	        if (realCause.getMessage().contains("service_code_key")) {
	            final String name = command.stringValueOfParameterNamed("serviceCode");
	            throw new PlatformDataIntegrityException("error.msg.code.duplicate.name", "A code with name'"
	                    + name + "'already exists", "displayName", name);
	        }

	        logger.error(dve.getMessage(), dve);
	        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource: " + realCause.getMessage());
	    }

	private ServiceMaster retrieveCodeBy(final Long serviceId) {
		
		final ServiceMaster serviceMaster = this.serviceMasterRepository.findOne(serviceId);
		if (serviceMaster == null) {
			throw new CodeNotFoundException(serviceId.toString());
		}
		return serviceMaster;
	}
	 
	 
	@Override
	public CommandProcessingResult deleteService(final Long serviceId) {
				
		    context.authenticatedUser();
	        final ServiceMaster serviceMaster = retrieveCodeBy(serviceId);
	        serviceMaster.delete();
			this.serviceMasterRepository.save(serviceMaster);
	        return new CommandProcessingResultBuilder().withEntityId(serviceId).build();
	    }

	}




