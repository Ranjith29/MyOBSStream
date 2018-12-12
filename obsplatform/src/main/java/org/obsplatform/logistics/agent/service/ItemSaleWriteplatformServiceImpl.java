package org.obsplatform.logistics.agent.service;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.billing.taxmapping.domain.TaxMap;
import org.obsplatform.billing.taxmapping.domain.TaxMapRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.agent.domain.ItemSale;
import org.obsplatform.logistics.agent.domain.ItemSaleInvoice;
import org.obsplatform.logistics.agent.domain.ItemSaleRepository;
import org.obsplatform.logistics.agent.serialization.AgentItemSaleCommandFromApiJsonDeserializer;
import org.obsplatform.logistics.item.domain.ItemMaster;
import org.obsplatform.logistics.item.domain.ItemRepository;
import org.obsplatform.logistics.item.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author hugo
 *
 */
@Service
public class ItemSaleWriteplatformServiceImpl implements ItemSaleWriteplatformService{

	private final PlatformSecurityContext context;
	private final ItemSaleRepository itemSaleRepository;
	private final AgentItemSaleCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final ItemRepository itemRepository;
	private final TaxMapRepository taxMapRepository;
	
	@Autowired
	public ItemSaleWriteplatformServiceImpl(final PlatformSecurityContext context,final ItemSaleRepository itemSaleRepository,
			final AgentItemSaleCommandFromApiJsonDeserializer apiJsonDeserializer,final ItemRepository itemRepository,final TaxMapRepository taxMapRepository){
		
		   this.context=context;
		   this.itemSaleRepository=itemSaleRepository;
		   this.taxMapRepository=taxMapRepository;
		   this.apiJsonDeserializer=apiJsonDeserializer;
		   this.itemRepository=itemRepository;
		
	}
	
	/* (non-Javadoc)
	 * @see #createNewItemSale(org.obsplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createNewItemSale(final JsonCommand command) {

        try{
        	
        	this.context.authenticatedUser();
        	this.apiJsonDeserializer.validateForCreate(command.json());
        	final ItemSale itemSale=ItemSale.fromJson(command);
        	if(itemSale.getPurchaseFrom().equals(itemSale.getPurchaseBy())){
        		
        		throw new PlatformDataIntegrityException("invalid.move.operation", "invalid.move.operation", "invalid.move.operation");
        	}
            final ItemMaster itemMaster=this.itemRepository.findOne(itemSale.getItemId());
            final List<TaxMap> taxMaps=this.taxMapRepository.findOneByChargeCode(itemSale.getChargeCode());
          	ItemSaleInvoice invoice=ItemSaleInvoice.fromJson(command);
            BigDecimal taxAmount=BigDecimal.ZERO;
            BigDecimal taxRate=BigDecimal.ZERO;
           
            	for(TaxMap taxMap:taxMaps){
            	taxRate=taxMap.getRate();
            	if(taxMap.getTaxType().equalsIgnoreCase("percentage")){
            		taxAmount=invoice.getChargeAmount().multiply(taxRate.divide(new BigDecimal(100)));
            	}else{
            		taxAmount=invoice.getChargeAmount().add(taxRate);
            	}
            }
            if(itemMaster == null){
        	  throw new ItemNotFoundException(itemSale.getItemId().toString());
            }
        	invoice.updateAmounts(taxAmount);
        	invoice.setTaxpercentage(taxRate);
        	itemSale.setItemSaleInvoice(invoice);
        	
        	this.itemSaleRepository.save(itemSale);
           return new CommandProcessingResult(itemSale.getId());        	
        }catch(DataIntegrityViolationException dve){
        	handleCodeDataIntegrityIssues(command, dve);
        	return new CommandProcessingResult(Long.valueOf(-1L));
        	
        }
		
		
	}


	private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
	
		Throwable realCause = dve.getMostSpecificCause();
	        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource: " + realCause.getMessage());
		
	}

}
