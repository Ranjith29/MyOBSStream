package org.obsplatform.portfolio.addons.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.addons.domain.AddonServices;
import org.obsplatform.portfolio.addons.domain.AddonsPrices;
import org.obsplatform.portfolio.addons.domain.AddonsRepository;
import org.obsplatform.portfolio.addons.exceptions.AddonServicesNotFoundException;
import org.obsplatform.portfolio.addons.serialization.AddOnsCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


@Service
public class AddonsWritePlatformServiceImpl implements AddOnsWritePlatformService{
	
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromJsonHelper;
	private final AddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final AddonsRepository addonsRepository;
	
 @Autowired
 public AddonsWritePlatformServiceImpl(final PlatformSecurityContext context,
		 final AddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer,final FromJsonHelper fromJsonHelper,
		 final AddonsRepository addonsRepository){
		
	this.context=context;
	this.fromJsonHelper=fromJsonHelper;
	this.fromApiJsonDeserializer=fromApiJsonDeserializer;
	this.addonsRepository=addonsRepository;
}


@Transactional
@Override
public CommandProcessingResult createAddons(JsonCommand command) {
	
	try{
		
		this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForCreate(command.json());
		AddonServices addons=AddonServices.fromJson(command);
		final JsonElement element = fromJsonHelper.parse(command.json());
		final JsonArray addonServices = fromJsonHelper.extractJsonArrayNamed("addons", element);
		
		for (JsonElement jsonElement : addonServices) {
			AddonsPrices addonsPrices = AddonsPrices.fromJson(jsonElement,fromJsonHelper);
			addons.addAddonPrices(addonsPrices);
			}
		this.addonsRepository.saveAndFlush(addons);
		return new CommandProcessingResult(addons.getId());
	
	}catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(command, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}
	
}



private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
	
	 final Throwable realCause = dve.getMostSpecificCause();
       if (realCause.getMessage().contains("unique_addser_serv")) {
           final String name = command.stringValueOfParameterNamed("unique_addser_plan_ch");
           throw new PlatformDataIntegrityException("error.msg.code.duplicate.plan", "A plan with this service'" + name + "' already exists");
       }else{
      
       throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
               "Unknown data integrity issue with resource: " + realCause.getMessage());
	
       }
	}

@Override
public CommandProcessingResult UpdateAddons(JsonCommand command, Long entityId) {
	try{
		
		this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForCreate(command.json());
		final JsonElement element = fromJsonHelper.parse(command.json());
		final JsonArray addonServices = fromJsonHelper.extractJsonArrayNamed("addons", element);
		
		AddonServices addonService=getAddonById(entityId);
		final Map<String, Object> changes = addonService.update(command);
		
		Set<AddonsPrices> prices = new HashSet<AddonsPrices>();
			//addonService.getAddonsPrices().clear();
	    for (JsonElement jsonElement : addonServices) {
			AddonsPrices addonsPrice = AddonsPrices.fromJson(jsonElement,fromJsonHelper);
			addonsPrice.update(addonService);
			prices.add(addonsPrice);
	    }
		addonService.updateAddonPrices(prices);
		this.addonsRepository.save(addonService);
		return new CommandProcessingResult(entityId);
				
	}catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(command, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}
	
}


private AddonServices getAddonById(Long entityId) {
	
	AddonServices addonService = this.addonsRepository.findOne(entityId);
	if(addonService == null){
		throw new AddonServicesNotFoundException(entityId);
	}
	
	return addonService;
}


@Override
public CommandProcessingResult deleteAddons(Long entityId) {
	try{
	
		this.context.authenticatedUser();
		AddonServices addonServices=getAddonById(entityId);
		addonServices.delete();
		this.addonsRepository.saveAndFlush(addonServices);
		return new CommandProcessingResult(entityId);
	
	}catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(null, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}
	
	
}	
}
