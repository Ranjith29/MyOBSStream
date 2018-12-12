package org.obsplatform.billing.taxmapping.service;

import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.obsplatform.billing.taxmapping.domain.TaxMap;
import org.obsplatform.billing.taxmapping.domain.TaxMapRepository;
import org.obsplatform.billing.taxmapping.serialization.TaxMapCommandFromApiJsonDeserializer;
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
public class TaxMapWritePlatformServiceImp implements TaxMapWritePlatformService{

	private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(TaxMapWritePlatformService.class);
	
	private final PlatformSecurityContext context;
	private final TaxMapRepository taxMapRepository;
	private final TaxMapCommandFromApiJsonDeserializer apiJsonDeserializer;
	
	@Autowired
	public TaxMapWritePlatformServiceImp(final PlatformSecurityContext context,final TaxMapRepository taxMapRepository,
			final TaxMapCommandFromApiJsonDeserializer apiJsonDeserializer){
		this.context = context;
		this.taxMapRepository = taxMapRepository;
		this.apiJsonDeserializer = apiJsonDeserializer;
		
	}
	
	/* (non-Javadoc)
	 * @see #createTaxMap(org.obsplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createTaxMap(final JsonCommand command){
		TaxMap  taxmap = null;
		try{
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command);
			taxmap = TaxMap.fromJson(command);
			this.taxMapRepository.save(taxmap);
		}catch(final DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		return new CommandProcessingResultBuilder().withEntityId(taxmap.getId()).build();
	}
	
	/* (non-Javadoc)
	 * @see #updateTaxMap(org.obsplatform.infrastructure.core.api.JsonCommand, java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult updateTaxMap(final JsonCommand command,final Long taxMapId){
		TaxMap taxMap = null;
		try{
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command);
			taxMap = retrieveTaxMapById(taxMapId);
			final Map<String, Object> changes = taxMap.update(command);
			
			if(!changes.isEmpty()){
				this.taxMapRepository.saveAndFlush(taxMap);
			}
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(taxMap.getId())
					.with(changes).build();
		}catch(final DataIntegrityViolationException dve){
			if (dve.getCause() instanceof ConstraintViolationException) {
			handleDataIntegrityIssues(command, dve);
		  }
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	
	}
	
	 private TaxMap retrieveTaxMapById(final Long taxMapId) {
		 
	        final TaxMap taxMap = this.taxMapRepository.findOne(taxMapId);
	        if (taxMap == null) { 
	        	throw new PlatformDataIntegrityException("validation.error.msg.taxmap.taxcode.doesnotexist",
	        	"validation.error.msg.taxmap.taxcode.doesnotexist",taxMapId.toString(),
	        	"validation.error.msg.taxmap.taxcode.doesnotexist");
	        	}
	        return taxMap;
	    }
	
	
	private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

      final Throwable realCause = dve.getMostSpecificCause();
      
      LOGGER.error(dve.getMessage(), dve);   
       if (realCause.getMessage().contains("taxcode")){
       	throw new PlatformDataIntegrityException("validation.error.msg.taxmap.taxcode.duplicate",
       			"A taxcode with name'"+ command.stringValueOfParameterNamed("taxCode")+"'already exists",
       			command.stringValueOfParameterNamed("taxCode"));
       }else{
    	   throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
					"Unknown data integrity issue with resource: "+ dve.getMessage());
       }
       
    }
}
