package org.obsplatform.logistics.item.service;

import java.math.BigDecimal;
import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.domain.ItemAuditRepository;
import org.obsplatform.logistics.item.domain.ItemMaster;
import org.obsplatform.logistics.item.domain.ItemMasterAudit;
import org.obsplatform.logistics.item.domain.ItemPrice;
import org.obsplatform.logistics.item.domain.ItemPriceRepository;
import org.obsplatform.logistics.item.domain.ItemRepository;
import org.obsplatform.logistics.item.exception.ItemNotFoundException;
import org.obsplatform.logistics.item.serialization.ItemCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class ItemWritePlatformServiceImpl implements ItemWritePlatformService{

	private final static Logger LOGGER = LoggerFactory.getLogger(ItemWritePlatformServiceImpl.class);
	
	private final ItemRepository itemRepository;
	private final ItemPriceRepository itemPriceRepository;
	private final PlatformSecurityContext context;
	private final ItemAuditRepository itemAuditRepository;
	private final ItemCommandFromApiJsonDeserializer itemCommandFromApiJsonDeserializer; 
	private final FromJsonHelper fromApiJsonHelper;
	
 @Autowired
 public ItemWritePlatformServiceImpl(final PlatformSecurityContext context,final ItemRepository itemrepository,
		 final ItemCommandFromApiJsonDeserializer itemCommandFromApiJsonDeserializer,final ItemAuditRepository itemAuditRepository,
		 final FromJsonHelper fromApiJsonHelper, final ItemPriceRepository itemPriceRepository){

	 this.context=context;
	 this.itemRepository=itemrepository;
	 this.itemCommandFromApiJsonDeserializer = itemCommandFromApiJsonDeserializer;
	 this.itemAuditRepository=itemAuditRepository;
	 this.fromApiJsonHelper = fromApiJsonHelper;
	 this.itemPriceRepository = itemPriceRepository;
 }
	
	@Override
	public CommandProcessingResult createItem(final JsonCommand command) {
    	
    	try{	 
    		this.context.authenticatedUser();
    		this.itemCommandFromApiJsonDeserializer.validateForCreate(command.json());
    		ItemMaster itemMaster=ItemMaster.fromJson(command);
    		final JsonArray itemPricesArray = command.arrayOfParameterNamed("itemPrices").getAsJsonArray();
			String[] itemPriceRegions = null;
			itemPriceRegions = new String[itemPricesArray.size()];
			if (itemPricesArray.size() > 0) {
				for (int i = 0; i < itemPricesArray.size(); i++) {
					itemPriceRegions[i] = itemPricesArray.get(i).toString();
				}

				for (final String itemPriceRegionData : itemPriceRegions) {

					final JsonElement element = fromApiJsonHelper.parse(itemPriceRegionData);
					final String regionId = fromApiJsonHelper.extractStringNamed("regionId", element);
					final BigDecimal price = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("price", element);
					final Long chargeVariant = fromApiJsonHelper.extractLongNamed("chargeVariant", element);
					ItemPrice itemPrice = new ItemPrice(regionId, price,chargeVariant);
					itemMaster.addItemPrices(itemPrice);

				}

			} 
    		this.itemRepository.save(itemMaster);
    		return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(itemMaster.getId()).build();
    	} catch (DataIntegrityViolationException dve) {
    		handleItemDataIntegrityIssues(command, dve);
    		return CommandProcessingResult.empty();
    	}
	}

    private void handleItemDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        
    	Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("code_name_org")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.code.duplicate.name", "A code with name '" + name + "' already exists");
        }
        else  if (realCause.getMessage().contains("item_code")) {
            final String name = command.stringValueOfParameterNamed("itemCode");
            throw new PlatformDataIntegrityException("error.msg.item.code.duplicate.name", "A Item code with name '" + name + "' already exists");
        }
        else  if (realCause.getMessage().contains("itemid_with_region_uniquekey")) {
            final String name = command.stringValueOfParameterNamed("itemCode");
            throw new PlatformDataIntegrityException("error.msg.item.code.with.duplicate.priceregion", "A Item code '" + name + "' with given price region already exists");
        }

        LOGGER.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
   
     @Override
     public CommandProcessingResult updateItem(final JsonCommand command,final Long itemId) {

    	 try{
    		 this.context.authenticatedUser();
    		 this.itemCommandFromApiJsonDeserializer.validateForCreate(command.json());
    		 ItemMaster itemMaster = this.retrieveCodeBy(itemId);
    		 final int unitPrice = command.integerValueOfParameterNamed("unitPrice");
    		 final int existingUnitPrice = itemMaster.getUnitPrice().intValueExact();
    		 
    		 if(unitPrice!=existingUnitPrice){
    			 final ItemMasterAudit itemMasterAudit = new ItemMasterAudit(itemId,existingUnitPrice,null,command);
    			 this.itemAuditRepository.save(itemMasterAudit);
    		 }
    		 final Map<String, Object> changes = itemMaster.update(command);
    		 
    		 final JsonArray itemPricesArray = command.arrayOfParameterNamed("itemPrices").getAsJsonArray();
    		 final JsonArray removeItemPricesArray = command.arrayOfParameterNamed("removeItemPrices").getAsJsonArray();
 			 String[] itemPriceRegions = new String[itemPricesArray.size()];
 			 
 			 if(removeItemPricesArray.size() != 0){
 				 
  				String[] removedItemPriceRegions = new String[removeItemPricesArray.size()];
  	 			
  	 			 for(int i = 0; i < removeItemPricesArray.size(); i++){
  	 				removedItemPriceRegions[i] = removeItemPricesArray.get(i).toString();
  	 			 }
  	 			 
  	 			 for (final String itemPriceRegionData : removedItemPriceRegions) {
  	 							 
  	 				final JsonElement element = fromApiJsonHelper.parse(itemPriceRegionData);
  	 				final Long itemPriceId = fromApiJsonHelper.extractLongNamed("id", element);
  		 			final BigDecimal price = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("price", element);
  		 			
  	 				if(itemPriceId != null){
  	 					ItemPrice itemPrice =this.itemPriceRepository.findOne(itemPriceId);
  	 	 				itemPrice.delete();
  	 	 				itemPrice.setPrice(price);
  	 	 				itemPriceRepository.saveAndFlush(itemPrice);
  	 				}	
  	 			 }	
  			 }
 			 
			if (itemPricesArray.size() > 0) {
				for (int i = 0; i < itemPricesArray.size(); i++) {
					itemPriceRegions[i] = itemPricesArray.get(i).toString();
				}

				for (final String itemPriceRegionData : itemPriceRegions) {

					final JsonElement element = fromApiJsonHelper.parse(itemPriceRegionData);
					final Long itemPriceId = fromApiJsonHelper.extractLongNamed("id", element);
					final String regionId = fromApiJsonHelper.extractStringNamed("regionId", element);
					final BigDecimal price = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("price", element);
					final Long chargeVariant = fromApiJsonHelper.extractLongNamed("chargeVariant", element);

					if (itemPriceId != null) {
						ItemPrice itemPrice = this.itemPriceRepository.findOne(itemPriceId);
						final int existingRegionalUnitPrice = itemPrice.getPrice().intValueExact();
						itemPrice.setRegionId(regionId);
						itemPrice.setPrice(price);
						itemPrice.setChargeVariant(chargeVariant);
						itemPriceRepository.saveAndFlush(itemPrice);
						if (price.intValueExact() != existingRegionalUnitPrice) {
							final ItemMasterAudit itemMasterAudit = new ItemMasterAudit(itemId, existingRegionalUnitPrice,regionId, command);
							this.itemAuditRepository.save(itemMasterAudit);
						}
					} else {
						ItemPrice itemPrice = new ItemPrice(regionId, price,chargeVariant);
						itemMaster.addItemPrices(itemPrice);
					}
				}
			}
 			 this.itemRepository.save(itemMaster);
    		
	   return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(itemId).with(changes).build();
	}catch (DataIntegrityViolationException dve) {
	      handleItemDataIntegrityIssues(command, dve);
	      return new CommandProcessingResult(Long.valueOf(-1));
	  }

}

	@Override
	public CommandProcessingResult deleteItem(final Long itemId) {
		try{
			this.context.authenticatedUser();
			ItemMaster itemMaster= this.retrieveCodeBy(itemId);
			if(itemMaster.getDeleted()=='Y'){
				throw new ItemNotFoundException(itemId.toString());
			}
			itemMaster.delete();
			this.itemRepository.save(itemMaster);
			return new CommandProcessingResultBuilder().withEntityId(itemId).build();
			
		}catch(DataIntegrityViolationException dve){
			handleItemDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
	
	private ItemMaster retrieveCodeBy(final Long itemId) {
        final ItemMaster item = this.itemRepository.findOne(itemId);
        if (item == null) { throw new ItemNotFoundException(itemId.toString()); }
        return item;
    }
}
