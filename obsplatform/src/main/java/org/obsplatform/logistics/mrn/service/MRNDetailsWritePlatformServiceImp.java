package org.obsplatform.logistics.mrn.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.agent.domain.ItemSale;
import org.obsplatform.logistics.agent.domain.ItemSaleRepository;
import org.obsplatform.logistics.item.exception.ItemNotFoundException;
import org.obsplatform.logistics.itemdetails.domain.ItemDetails;
import org.obsplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.obsplatform.logistics.itemdetails.exception.SerialNumberNotFoundException;
import org.obsplatform.logistics.mrn.domain.InventoryTransactionHistory;
import org.obsplatform.logistics.mrn.domain.InventoryTransactionHistoryJpaRepository;
import org.obsplatform.logistics.mrn.domain.MRNDetails;
import org.obsplatform.logistics.mrn.domain.MRNDetailsJpaRepository;
import org.obsplatform.logistics.mrn.exception.InvalidMrnIdException;
import org.obsplatform.logistics.mrn.serialization.MRNDetailsCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class MRNDetailsWritePlatformServiceImp implements MRNDetailsWritePlatformService{

	private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(MRNDetailsWritePlatformServiceImp.class);
	
	private final PlatformSecurityContext context; 
	private final ItemSaleRepository itemSaleRepository;
	private final MRNDetailsJpaRepository mrnDetailsJpaRepository;
	private final MRNDetailsReadPlatformService mrnDetailsReadPlatformService;
	private final MRNDetailsCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final ItemDetailsRepository inventoryItemDetailsRepository;
	private final InventoryTransactionHistoryJpaRepository inventoryTransactionHistoryJpaRepository;
	private final FromJsonHelper fromApiJsonHelper;
	@Autowired
	public MRNDetailsWritePlatformServiceImp(final MRNDetailsJpaRepository mrnDetailsJpaRepository,final PlatformSecurityContext context,
			final MRNDetailsCommandFromApiJsonDeserializer apiJsonDeserializer,final MRNDetailsReadPlatformService mrnDetailsReadPlatformService,
			final InventoryTransactionHistoryJpaRepository inventoryTransactionHistoryJpaRepository,final ItemDetailsRepository inventoryItemDetailsRepository,
			final ItemSaleRepository itemSaleRepository,
			final FromJsonHelper fromApiJsonHelper) {
		
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.itemSaleRepository=itemSaleRepository;
		this.mrnDetailsJpaRepository = mrnDetailsJpaRepository;
		this.mrnDetailsReadPlatformService = mrnDetailsReadPlatformService;
		this.inventoryItemDetailsRepository = inventoryItemDetailsRepository;
		this.inventoryTransactionHistoryJpaRepository = inventoryTransactionHistoryJpaRepository;
		this.fromApiJsonHelper = fromApiJsonHelper;
	}
	
	
	@Transactional
	@Override
	public CommandProcessingResult createMRNDetails(final JsonCommand command) {
	
		try {
			context.authenticatedUser();
			apiJsonDeserializer.validateForCreate(command.json());
			final MRNDetails mrnDetails = MRNDetails.formJson(command);
			mrnDetailsJpaRepository.save(mrnDetails);
			return new CommandProcessingResultBuilder().withEntityId(mrnDetails.getId()).build();
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResultBuilder().withEntityId(Long.valueOf(-1)).build();
		
		}catch (ParseException e) {
			throw new PlatformDataIntegrityException("invalid.date.format", "invalid.date.format", "purchaseDate");
		}
	}
	
	@Transactional
	@Override
	public CommandProcessingResult moveMRN(JsonCommand command) {
		
		try {
			context.authenticatedUser();
			apiJsonDeserializer.validateForMove(command.json());
			final Long mrnId = command.longValueOfParameterNamed("mrnId");
			final String serialNumber=command.stringValueOfParameterNamed("serialNumber");
			final MRNDetails mrnDetails = mrnDetailsJpaRepository.findOne(mrnId);
			if(mrnDetails == null){
				throw new InvalidMrnIdException(mrnId.toString());
			}
			final List<String> serialNumbers = mrnDetailsReadPlatformService.retriveSerialNumbers(mrnId);
			
			if (!serialNumbers.contains(serialNumber)) {
					throw new PlatformDataIntegrityException("invalid.serialnumber.allocation",
							"invalid.serialnumber.allocation", "serialNumber", "");
				}

				ItemDetails details = inventoryItemDetailsRepository.getInventoryItemDetailBySerialNum(serialNumber);
				if (details == null) {
					throw new ItemNotFoundException(serialNumber);
				}
				if (details.getOfficeId().equals(mrnDetails.getToOffice())) {
					throw new PlatformDataIntegrityException("invalid.move.operation", "invalid.move.operation",
							"invalid.move.operation");
				}

				details.setOfficeId(mrnDetails.getToOffice());
				if (mrnDetails.getReceivedQuantity() < mrnDetails.getOrderdQuantity()) {
					mrnDetails.setReceivedQuantity(mrnDetails.getReceivedQuantity() + 1);
					mrnDetails.setStatus("Pending");
				} else if (mrnDetails.getReceivedQuantity().equals(mrnDetails.getOrderdQuantity())) {
					throw new PlatformDataIntegrityException("received.quantity.is.full", "received.quantity.is.full",
							"received quantity is full");
				}

				InventoryTransactionHistory transactionHistory = InventoryTransactionHistory.logTransaction(mrnDetails.getRequestedDate(), mrnId,
						"MRN", serialNumber, mrnDetails.getItemMasterId(), mrnDetails.getFromOffice(),
						mrnDetails.getToOffice());

				details.setOfficeId(mrnDetails.getToOffice());
				inventoryItemDetailsRepository.save(details);
				inventoryTransactionHistoryJpaRepository.save(transactionHistory);

				if (mrnDetails.getOrderdQuantity().equals(mrnDetails.getReceivedQuantity())) {
					mrnDetails.setStatus("Completed");
				}
				mrnDetailsJpaRepository.save(mrnDetails);
			return new CommandProcessingResultBuilder().withEntityId(transactionHistory.getId()).build();
			
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResultBuilder().withEntityId(Long.valueOf(-1)).build();
		}
	
	}
	
	
	
	private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
       if (realCause.getMessage().contains("serial_no_constraint")){
       	throw new PlatformDataIntegrityException("validation.error.msg.inventory.mrn.duplicate.entry", "validation.error.msg.inventory.mrn.duplicate.entry", "validation.error.msg.inventory.mrn.duplicate.entry","");
       	
       }
       LOGGER.error(dve.getMessage(), dve);   	
}


	@Override
	public CommandProcessingResult moveItemSale(JsonCommand command) {
		
		try {
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForMove(command.json());
			
			final Long itemId = command.longValueOfParameterNamed("itemId");
			final String serialNumber= command.stringValueOfParameterNamed("serialNumber");
			ItemSale mrnDetails = itemSaleRepository.findOne(itemId);
			final List<String> serialNumbers = mrnDetailsReadPlatformService.retriveSerialNumbersForItems(itemId,serialNumber);
			if(serialNumbers == null || serialNumbers.isEmpty()){
				
				throw new SerialNumberNotFoundException(serialNumber);
			}
			
			final ItemDetails details = inventoryItemDetailsRepository.getInventoryItemDetailBySerialNum(serialNumber);
			details.setOfficeId(mrnDetails.getPurchaseBy());
			
			if(mrnDetails.getReceivedQuantity() < mrnDetails.getOrderQuantity()){
				mrnDetails.setReceivedQuantity(mrnDetails.getReceivedQuantity()+1);
				mrnDetails.setStatus("Pending");
		
			} else if(mrnDetails.getReceivedQuantity().equals(mrnDetails.getOrderQuantity())){
				throw new PlatformDataIntegrityException("received.quantity.is.full", "received.quantity.is.full", "received.quantity.is.full");
			}

			InventoryTransactionHistory transactionHistory = InventoryTransactionHistory.logTransaction(DateUtils.getDateOfTenant(), itemId,"Move ItemSale",serialNumber, 
					mrnDetails.getItemId(), mrnDetails.getPurchaseFrom(), mrnDetails.getPurchaseBy());
			
			details.setOfficeId(mrnDetails.getPurchaseBy());
			this.inventoryItemDetailsRepository.save(details);
			this.inventoryTransactionHistoryJpaRepository.save(transactionHistory);
			if(mrnDetails.getOrderQuantity().equals(mrnDetails.getReceivedQuantity())){
				mrnDetails.setStatus("Completed");
			}
			this.itemSaleRepository.save(mrnDetails);
			
			return new CommandProcessingResultBuilder().withEntityId(transactionHistory.getId()).build();	
		}catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResultBuilder().withEntityId(Long.valueOf(-1)).build();
			
		} 
	}
}
