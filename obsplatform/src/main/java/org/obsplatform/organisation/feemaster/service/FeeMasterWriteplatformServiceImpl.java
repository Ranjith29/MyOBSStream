package org.obsplatform.organisation.feemaster.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.feemaster.domain.FeeDetail;
import org.obsplatform.organisation.feemaster.domain.FeeDetailRepository;
import org.obsplatform.organisation.feemaster.domain.FeeMaster;
import org.obsplatform.organisation.feemaster.domain.FeeMasterRepository;
import org.obsplatform.organisation.feemaster.exception.FeeMasterNotFoundException;
import org.obsplatform.organisation.feemaster.serialization.FeeMasterCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class FeeMasterWriteplatformServiceImpl implements FeeMasterWriteplatformService {

	private final PlatformSecurityContext context;
	private final FeeMasterCommandFromApiJsonDeserializer feeMasterCommandFromApiJsonDeserializer;
	private final FeeMasterRepository feeMasterRepository;
	private final FeeDetailRepository feeDetailRepository;
	private final FromJsonHelper fromApiJsonHelper;
	
	
	@Autowired
	public FeeMasterWriteplatformServiceImpl(final PlatformSecurityContext context,
			final FeeMasterCommandFromApiJsonDeserializer feeMasterCommandFromApiJsonDeserializer,
			final FeeMasterRepository feeMasterRepository,final FeeDetailRepository feeDetailRepository,
            final FromJsonHelper fromApiJsonHelper) {

		this.context = context;
		this.feeMasterCommandFromApiJsonDeserializer = feeMasterCommandFromApiJsonDeserializer;
		this.feeMasterRepository = feeMasterRepository;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.feeDetailRepository = feeDetailRepository;
	}
	
	
	@Override
	public CommandProcessingResult createFeeMaster(JsonCommand command) {
		
		try {
			this.context.authenticatedUser();
			this.feeMasterCommandFromApiJsonDeserializer.validateForCreate(command.json());
			FeeMaster feeMaster = FeeMaster.fromJson(command);

    		final JsonArray regionPricesArray = command.arrayOfParameterNamed("regionPrices").getAsJsonArray();
			String[] feeMasterPriceRegions = null;
			feeMasterPriceRegions = new String[regionPricesArray.size()];
			if(regionPricesArray.size() > 0){
			for(int i = 0; i < regionPricesArray.size(); i++){
				feeMasterPriceRegions[i] = regionPricesArray.get(i).toString();
			}
			
			for (final String feeMasterPriceRegion : feeMasterPriceRegions) {
							 
				final JsonElement element = fromApiJsonHelper.parse(feeMasterPriceRegion);
				
				final String regionId = fromApiJsonHelper.extractStringNamed("regionId", element);
				final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
				final Long planId = fromApiJsonHelper.extractLongNamed("planId", element);
				final String contractPeriod = fromApiJsonHelper.extractStringNamed("contractPeriod", element);
				final Long categoryId = fromApiJsonHelper.extractLongNamed("categoryId", element);
				
				FeeDetail feeDetail = new FeeDetail(regionId, amount,planId,contractPeriod, categoryId);
				feeMaster.addRegionPrices(feeDetail);

			  }	 
			
		 }		 
			
    		this.feeMasterRepository.save(feeMaster);
    		return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(feeMaster.getId()).build();
    
    	} catch (DataIntegrityViolationException dve) {
    		handleItemDataIntegrityIssues(command, dve);
    		return CommandProcessingResult.empty();
    	}
	}
	
	 private void handleItemDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
	        Throwable realCause = dve.getMostSpecificCause();
	        if (realCause.getMessage().contains("fee_code")) {
	            final String name = command.stringValueOfParameterNamed("feeCode");
	            throw new PlatformDataIntegrityException("error.msg.fee.code.duplicate.name", "A Fee code with name '" + name + "' already exists");
	        } else if (realCause.getMessage().contains("fee_transaction_type")) {
	            final String name =command.stringValueOfParameterNamed("transactionType");
	            throw new PlatformDataIntegrityException("error.msg.fee.transaction.alredy.exists", "A Fee transactionType with this '" + name + "' already exists","transactionType");
	        }

	        //logger.error(dve.getMessage(), dve);
	        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource: " + realCause.getMessage());
	    }


	@Override
	public CommandProcessingResult updateFeeMaster(JsonCommand command) {

   	 try{
   		 this.context.authenticatedUser();
   		 this.feeMasterCommandFromApiJsonDeserializer.validateForCreate(command.json());
   		 FeeMaster feeMaster = this.retrieveCodeBy(command.entityId());
   		
   		 final JsonArray regionPricesArray = command.arrayOfParameterNamed("regionPrices").getAsJsonArray();
		 String[] feeMasterPriceRegions = new String[regionPricesArray.size()];
		 
		 //Remove feeDetails
		 JsonArray removeRegionPricesArray = new JsonArray();
			if (command.hasParameter("removeRegionPrices")) {
				removeRegionPricesArray = command.arrayOfParameterNamed("removeRegionPrices").getAsJsonArray();
			}
			 if(removeRegionPricesArray.size() != 0){
				 
					String[] removedFeeMasterPriceRegions = new String[removeRegionPricesArray.size()];
		 			
		 			 for(int i = 0; i < removeRegionPricesArray.size(); i++){
		 				removedFeeMasterPriceRegions[i] = removeRegionPricesArray.get(i).toString();
		 			 }
		 			 
		 			 for (final String removedFeeMasterPriceRegion : removedFeeMasterPriceRegions) {
		 							 
		 				final JsonElement element = fromApiJsonHelper.parse(removedFeeMasterPriceRegion);
		 				final Long feeDetailId = fromApiJsonHelper.extractLongNamed("id", element);
			 			
		 				if(feeDetailId != null){
		 					FeeDetail feeDetail =this.feeDetailRepository.findOne(feeDetailId);
		 					if(feeDetail !=null){
		 						feeDetail.deleted();
		 						this.feeDetailRepository.saveAndFlush(feeDetail);
		 					}
		 				}	
		 			 }	
				 }

			for (int i = 0; i < regionPricesArray.size(); i++) {
				feeMasterPriceRegions[i] = regionPricesArray.get(i).toString();
			}
             
			//update feeDatils
			 Map<String, Object> childChanges=new HashMap<String,Object>();
			 for (final String feeMasterPriceRegion : feeMasterPriceRegions) {
							 
				final JsonElement element = fromApiJsonHelper.parse(feeMasterPriceRegion);
				final Long feeDetailId = fromApiJsonHelper.extractLongNamed("id", element);
				
				if(feeDetailId != null){
					FeeDetail feeDetail =this.feeDetailRepository.findOne(feeDetailId);
					if(feeDetail != null){
						Map<String, Object> childDetail = feeDetail.update(element,fromApiJsonHelper);
						childChanges.putAll(childDetail);
						feeDetailRepository.saveAndFlush(feeDetail);
					}
	 				
				}else{
					
					final String regionId = fromApiJsonHelper.extractStringNamed("regionId", element);
					final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
					final Long planId = fromApiJsonHelper.extractLongNamed("planId", element);
					final String contractPeriod = fromApiJsonHelper.extractStringNamed("contractPeriod", element);
					final Long categoryId = fromApiJsonHelper.extractLongNamed("categoryId", element);
					
					FeeDetail feeDetail = new FeeDetail(regionId, amount,planId,contractPeriod, categoryId);
					feeMaster.addRegionPrices(feeDetail);
				}
			  }	
			 
			 final Map<String, Object> changes = feeMaster.update(command);
			 changes.putAll(childChanges);
			 this.feeMasterRepository.saveAndFlush(feeMaster);
   		
	   return new CommandProcessingResultBuilder().withCommandId(command.commandId())
			   .withEntityId(command.entityId()).with(changes).build();
	}catch (DataIntegrityViolationException dve) {
	      handleItemDataIntegrityIssues(command, dve);
	      return new CommandProcessingResult(Long.valueOf(-1));
	  }

}
	
	@Override
	public CommandProcessingResult deleteFeeMaster(final Long feeMasterId) {
		try{
			this.context.authenticatedUser();
			FeeMaster feeMaster=this.retrieveCodeBy(feeMasterId);
			if(feeMaster.getDeleted()=='Y'){
				throw new FeeMasterNotFoundException(feeMasterId.toString());
			}
			for(FeeDetail feeDetail: feeMaster.getRegionPrices()){
				feeDetail.deleted();
			}
			feeMaster.delete();
			this.feeMasterRepository.save(feeMaster);
			return new CommandProcessingResultBuilder().withEntityId(feeMasterId).build();
			
		}catch(DataIntegrityViolationException dve){
			handleItemDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
	
	private FeeMaster retrieveCodeBy(final Long feeMasterId) {
        final FeeMaster feeMaster = this.feeMasterRepository.findOne(feeMasterId);
        if (feeMaster == null) { throw new FeeMasterNotFoundException(feeMasterId.toString()); }
        return feeMaster;
    }

}
