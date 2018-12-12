package org.obsplatform.logistics.grn.service;

import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.itemdetails.domain.InventoryGrn;
import org.obsplatform.logistics.itemdetails.domain.InventoryGrnRepository;
import org.obsplatform.logistics.itemdetails.serialization.InventoryGrnCommandFromApiJsonDeserializer;
import org.obsplatform.workflow.eventactionmapping.exception.EventActionMappingNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrnDetailsWritePlatformServiceImpl implements GrnDetailsWritePlatformService{

	
	private PlatformSecurityContext context;
	private InventoryGrnRepository inventoryGrnRepository;
	private InventoryGrnCommandFromApiJsonDeserializer inventoryGrnCommandFromApiJsonDeserializer;
	
	@Autowired
	public GrnDetailsWritePlatformServiceImpl(final PlatformSecurityContext context,InventoryGrnRepository inventoryGrnRepository, InventoryGrnCommandFromApiJsonDeserializer inventoryGrnCommandFromApiJsonDeserializer) {
		this.context = context;
		this.inventoryGrnRepository = inventoryGrnRepository;
		this.inventoryGrnCommandFromApiJsonDeserializer = inventoryGrnCommandFromApiJsonDeserializer;
	}
	
	private final static Logger logger = (Logger) LoggerFactory.getLogger(GrnDetailsWritePlatformServiceImpl.class);	
		
		
	@Transactional
	@Override
	public CommandProcessingResult addGrnDetails(JsonCommand command) {
		InventoryGrn inventoryGrn = null;
		try{
			context.authenticatedUser();
			inventoryGrnCommandFromApiJsonDeserializer.validateForCreate(command.json());
			inventoryGrn = InventoryGrn.fromJson(command);
			this.inventoryGrnRepository.save(inventoryGrn);
		}catch(DataIntegrityViolationException dve){
			//logger.error(dve.getMessage(), dve);   
			 handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		
		}
		return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(inventoryGrn.getId()).build();
	}

	private InventoryGrn grnRetrieveById(Long id) {
        
		InventoryGrn grnId=this.inventoryGrnRepository.findOne(id);
              if (grnId== null) { throw new EventActionMappingNotFoundException(id.toString()); }
	          return grnId;	
	}

	@Override
	public CommandProcessingResult editGrnDetails(JsonCommand command,
			Long entityId) {
		
		try{
			context.authenticatedUser();
			inventoryGrnCommandFromApiJsonDeserializer.validateForCreate(command.json());
			InventoryGrn inventoryGrnDetails=grnRetrieveById(entityId);
			
			final Map<String, Object> changes = inventoryGrnDetails.update(command);
			
			if(!changes.isEmpty()){
        		this.inventoryGrnRepository.save(inventoryGrnDetails);
        	}
			
			return new CommandProcessingResult(entityId);
		}catch(DataIntegrityViolationException dve){
			logger.error(dve.getMessage(), dve);   
			return new CommandProcessingResult(Long.valueOf(-1));
		
		}
	}

	/*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

    	final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("purchase_no_code_key")) {
            final String purchaseNo = command.stringValueOfParameterNamed("purchaseNo");
            throw new PlatformDataIntegrityException("error.msg.grn.duplicate.purchaseNo", "Grn with purchaseNo" + purchaseNo
                    + "` already exists", "purchaseNo", purchaseNo);
        }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }
}
