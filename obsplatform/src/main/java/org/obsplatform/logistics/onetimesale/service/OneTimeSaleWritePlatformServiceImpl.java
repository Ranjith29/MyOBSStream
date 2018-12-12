package org.obsplatform.logistics.onetimesale.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.obsplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.obsplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.obsplatform.billing.chargevariant.domain.ChargeVariant;
import org.obsplatform.billing.chargevariant.domain.ChargeVariantDetails;
import org.obsplatform.billing.chargevariant.domain.ChargeVariantRepository;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.finance.billingorder.domain.BillingOrder;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.billingorder.domain.InvoiceRepository;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.api.JsonQuery;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.grn.service.GrnReadPlatformService;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.item.domain.ItemMaster;
import org.obsplatform.logistics.item.domain.ItemRepository;
import org.obsplatform.logistics.item.domain.UnitEnumType;
import org.obsplatform.logistics.item.exception.NoItemRegionalPriceFound;
import org.obsplatform.logistics.item.service.ItemReadPlatformService;
import org.obsplatform.logistics.itemdetails.data.InventoryGrnData;
import org.obsplatform.logistics.itemdetails.domain.InventoryGrn;
import org.obsplatform.logistics.itemdetails.domain.InventoryGrnRepository;
import org.obsplatform.logistics.itemdetails.service.ItemDetailsReadPlatformService;
import org.obsplatform.logistics.itemdetails.service.ItemDetailsWritePlatformService;
import org.obsplatform.logistics.onetimesale.data.OneTimeSaleData;
import org.obsplatform.logistics.onetimesale.domain.OneTimeSale;
import org.obsplatform.logistics.onetimesale.domain.OneTimeSaleRepository;
import org.obsplatform.logistics.onetimesale.exception.DeviceSaleNotFoundException;
import org.obsplatform.logistics.onetimesale.exception.ItemCodeSerialNumberNotMatchedException;
import org.obsplatform.logistics.onetimesale.serialization.OneTimesaleCommandFromApiJsonDeserializer;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.obsplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.obsplatform.workflow.eventvalidation.service.EventValidationReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author hugo
 *
 */
@Service
public class OneTimeSaleWritePlatformServiceImpl implements OneTimeSaleWritePlatformService {
	
	
	private final static Logger LOGGER = LoggerFactory.getLogger(OneTimeSaleWritePlatformServiceImpl.class);
	
	private final FromJsonHelper fromJsonHelper;
	private final PlatformSecurityContext context;
	private final ItemRepository itemMasterRepository;
	private final OneTimesaleCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final InvoiceOneTimeSale invoiceOneTimeSale;
	private final OneTimeSaleRepository oneTimeSaleRepository;
	private final ItemReadPlatformService itemReadPlatformService;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService;
	private final EventValidationReadPlatformService eventValidationReadPlatformService;
	private final ChargeCodeRepository chargeCodeRepository;
	private final InvoiceRepository invoiceRepository;
	private final InventoryGrnRepository inventoryGrnRepository;
	private final GrnReadPlatformService grnReadPlatformService;
	private final ChargeVariantRepository chargeVariantRepository;
	private final ConfigurationRepository configurationRepository;
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
	private final ItemDetailsReadPlatformService itemDetailsReadPlatformService;

	@Autowired
	public OneTimeSaleWritePlatformServiceImpl(final PlatformSecurityContext context,final OneTimeSaleRepository oneTimeSaleRepository,
			final ItemRepository itemMasterRepository,final OneTimesaleCommandFromApiJsonDeserializer apiJsonDeserializer,
			final InvoiceOneTimeSale invoiceOneTimeSale,final ItemReadPlatformService itemReadPlatformService,
			final FromJsonHelper fromJsonHelper,final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService,
			final EventValidationReadPlatformService eventValidationReadPlatformService,
			final ChargeCodeRepository chargeCodeRepository,
			final InvoiceRepository invoiceRepository, final InventoryGrnRepository inventoryGrnRepository,
			final GrnReadPlatformService grnReadPlatformService,final ChargeVariantRepository chargeVariantRepository,
			final ConfigurationRepository configurationRepository,
			final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
			final ActiondetailsWritePlatformService actiondetailsWritePlatformService,
			final ItemDetailsReadPlatformService itemDetailsReadPlatformService) {

		this.context = context;
		this.fromJsonHelper = fromJsonHelper;
		this.invoiceOneTimeSale = invoiceOneTimeSale;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.itemMasterRepository = itemMasterRepository;
		this.oneTimeSaleRepository = oneTimeSaleRepository;
		this.itemReadPlatformService = itemReadPlatformService;
		this.oneTimeSaleReadPlatformService = oneTimeSaleReadPlatformService;
		this.inventoryItemDetailsWritePlatformService = inventoryItemDetailsWritePlatformService;
		this.eventValidationReadPlatformService = eventValidationReadPlatformService;
		this.chargeCodeRepository = chargeCodeRepository;
		this.invoiceRepository = invoiceRepository;
		this.inventoryGrnRepository = inventoryGrnRepository;
		this.grnReadPlatformService = grnReadPlatformService;
		this.chargeVariantRepository = chargeVariantRepository;
		this.configurationRepository= configurationRepository;
		this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
		this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
		this.itemDetailsReadPlatformService = itemDetailsReadPlatformService;
	}

	/* (non-Javadoc)
	 * @see #createOneTimeSale(JsonCommand, java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createOneTimeSale(final JsonCommand command,final Long clientId) {

		try {
			
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			final JsonElement element = fromJsonHelper.parse(command.json());
			final Long itemId = command.longValueOfParameterNamed("itemId");
			
			
			JsonObject obj = element.getAsJsonObject();
			 JsonElement serial =  obj.get("serialNumber");
			
			ItemMaster item = this.itemMasterRepository.findOne(itemId);
			final Long quantity = command.longValueOfParameterNamed("quantity");

            Configuration hardwaresaleProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_HARDWARE_SALE_LIMIT);	
            if(hardwaresaleProperty !=null && hardwaresaleProperty.isEnabled()){
            	final Long saledevicesCount = this.oneTimeSaleReadPlatformService.retrieveCustomerExistsSaleDevices(clientId,null);
            	if(saledevicesCount >= Long.valueOf(hardwaresaleProperty.getValue())){
            		 throw new DeviceSaleNotFoundException(saledevicesCount);
            	}
            }
			// Check for Custome_Validation
			this.eventValidationReadPlatformService.checkForCustomValidations(clientId, "Rental",command.json(),getUserId());
			final OneTimeSale oneTimeSale = OneTimeSale.fromJson(clientId, command,item);
			/**	Call if Item units is not PIECES */
			if(!UnitEnumType.PIECES.toString().equalsIgnoreCase(item.getUnits())){
			
			this.oneTimeSaleRepository.saveAndFlush(oneTimeSale);
			
			final List<OneTimeSaleData> oneTimeSaleDatas = this.oneTimeSaleReadPlatformService.retrieveOnetimeSalesForInvoice(clientId);
			JsonObject jsonObject = new JsonObject();
			final String saleType = command.stringValueOfParameterNamed("saleType");
			if (saleType.equalsIgnoreCase("NEWSALE")) {
				for (OneTimeSaleData oneTimeSaleData : oneTimeSaleDatas) {
					CommandProcessingResult invoice=this.invoiceOneTimeSale.invoiceOneTimeSale(clientId,oneTimeSaleData,false);
					updateOneTimeSale(oneTimeSaleData,invoice);
				}
			  }
			}
			
			
			/**	Call if Item units is PIECES */
			if(UnitEnumType.PIECES.toString().equalsIgnoreCase(item.getUnits())){
				
				final Long itemMasterId = itemDetailsReadPlatformService.getItemMasterIdBySerialNo(serial.getAsJsonArray().get(0).getAsJsonObject().get("serialNumber").getAsString());
				//check condition
				if(itemMasterId.equals((serial.getAsJsonArray().get(0).getAsJsonObject().get("itemMasterId").getAsLong()))){
							
				this.oneTimeSaleRepository.saveAndFlush(oneTimeSale);
				
				final List<OneTimeSaleData> oneTimeSaleDatas = this.oneTimeSaleReadPlatformService.retrieveOnetimeSalesForInvoice(clientId);
				JsonObject jsonObject = new JsonObject();
				final String saleType = command.stringValueOfParameterNamed("saleType");
				if (saleType.equalsIgnoreCase("NEWSALE")) {
					for (OneTimeSaleData oneTimeSaleData : oneTimeSaleDatas) {
						CommandProcessingResult invoice=this.invoiceOneTimeSale.invoiceOneTimeSale(clientId,oneTimeSaleData,false);
						updateOneTimeSale(oneTimeSaleData,invoice);
					}
				}
				
				
				
				JsonArray serialData = fromJsonHelper.extractJsonArrayNamed("serialNumber", element);
				for (JsonElement je : serialData) {
					JsonObject serialNumber = je.getAsJsonObject();
					serialNumber.addProperty("clientId", oneTimeSale.getClientId());
					serialNumber.addProperty("orderId", oneTimeSale.getId());
				}
				jsonObject.addProperty("itemId", oneTimeSale.getItemId());
				jsonObject.addProperty("quantity", oneTimeSale.getQuantity());
				//jsonObject.addProperty("allocationDate", oneTimeSale.getSaleDate().toString());
				jsonObject.add("serialNumber", serialData);
				JsonCommand jsonCommand = new JsonCommand(null,jsonObject.toString(), element, fromJsonHelper, null, null,
						null, null, null, null, null, null, null, null, null, null);
				this.inventoryItemDetailsWritePlatformService.allocateHardware(jsonCommand);
				}else{
					throw new ItemCodeSerialNumberNotMatchedException("Item Code Not Matched for this Item Check Serial Number and Item Code");
				}
			}else if(UnitEnumType.ACCESSORIES.toString().equalsIgnoreCase(item.getUnits()) || 
								UnitEnumType.METERS.toString().equalsIgnoreCase(item.getUnits())){
				
				final Collection<InventoryGrnData> grnDatas = this.grnReadPlatformService.retriveGrnIdswithItemId(itemId);
				for(InventoryGrnData grnData : grnDatas){
					InventoryGrn inventoryGrn = inventoryGrnRepository.findOne(grnData.getId());
					if(inventoryGrn.getReceivedQuantity() > 0 && inventoryGrn.getStockQuantity() > 0){
						inventoryGrn.setStockQuantity(inventoryGrn.getStockQuantity()-quantity);
						this.inventoryGrnRepository.save(inventoryGrn);
						break;
					}
				}
			}
			
			/** 
			 * This Code is for Ticket Generation When Device Sale
		     */
			 createTicket(clientId,Long.valueOf(oneTimeSale.getId()));
				
			 	return new CommandProcessingResult(Long.valueOf(oneTimeSale.getId()), clientId);
			
		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	 private Long getUserId() {
			Long userId=null;
			SecurityContext context = SecurityContextHolder.getContext();
				if(context.getAuthentication() != null){
					AppUser appUser=this.context.authenticatedUser();
					userId=appUser.getId();
				}else {
					userId=new Long(0);
				}
				
				return userId;
		}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		
		LOGGER.error(dve.getMessage(), dve);
		final Throwable realCause=dve.getMostSpecificCause();
		throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "+ realCause.getMessage());

	}

	public void updateOneTimeSale(final OneTimeSaleData oneTimeSaleData,final CommandProcessingResult invoice) {

		OneTimeSale oneTimeSale = oneTimeSaleRepository.findOne(oneTimeSaleData.getId());
		oneTimeSale.setIsInvoiced('Y');
		oneTimeSale.setInvoiceId(invoice.resourceId());
		oneTimeSaleRepository.save(oneTimeSale);

	}

	/** (non-Javadoc)
	 * @see #calculatePrice(java.lang.Long, JsonQuery)
	 */
	@Override
	public ItemData calculatePrice(final Long itemId, final JsonQuery query,final Long clientId) {

		try {
			
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForPrice(query.parsedJson());
			BigDecimal itemprice = BigDecimal.ZERO;
			BigDecimal totalPrice = BigDecimal.ZERO;
			final BigDecimal unitprice = fromJsonHelper.extractBigDecimalWithLocaleNamed("unitPrice",query.parsedJson());
			final String units = fromJsonHelper.extractStringNamed("units", query.parsedJson());
			ItemData itemData = this.itemReadPlatformService.retrieveSingleItemDetails(clientId, itemId, null,clientId != null ? true : false);
			if (itemData == null) {
				throw new NoItemRegionalPriceFound();
			}
			if (unitprice != null) {
				itemprice = unitprice;
			} else {
				itemprice = itemData.getUnitPrice();
			}

			if (UnitEnumType.PIECES.toString().equalsIgnoreCase(units)) {
				itemprice = this.calculateChargeVariantPriceForItems(itemprice, clientId, itemData);
				final Integer quantity = fromJsonHelper.extractIntegerWithLocaleNamed("quantity",query.parsedJson());
				Configuration hardwaresaleProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_HARDWARE_SALE_LIMIT);	
	            if(hardwaresaleProperty !=null && hardwaresaleProperty.isEnabled()){
	            	final Long saledevicesCount = this.oneTimeSaleReadPlatformService.retrieveCustomerExistsSaleDevices(clientId,null);
	            	Long totalQuantity = saledevicesCount+Long.valueOf(quantity);
	            	if(totalQuantity > Long.valueOf(hardwaresaleProperty.getValue())){
	            		 throw new DeviceSaleNotFoundException(saledevicesCount);
	            	}
	            }
				totalPrice = itemprice.multiply(new BigDecimal(quantity));
				/* If Same devices sale more than one at first time */
				if (quantity > 1 && itemprice.compareTo(itemData.getUnitPrice()) == 0
						&& !Long.valueOf(0L).equals(itemData.getChargeVariant())) {
					totalPrice = this.calculateMultiDeviceSalePrice(itemprice,itemData,quantity.longValue());
				}
				return new ItemData(itemData, totalPrice, quantity.toString());
			} else {
				final String quantityValue = fromJsonHelper.extractStringNamed("quantity", query.parsedJson());
				totalPrice = itemprice.multiply(new BigDecimal(quantityValue));
				return new ItemData(itemData, totalPrice, quantityValue);
			}
			
		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return null;

		}
	}

	/* (non-Javadoc)
	 * @see #deleteOneTimeSale(JsonCommand, java.lang.Long)
	 */
	@Override
	public CommandProcessingResult deleteOneTimeSale(final Long entityId) {

		try {
			this.context.authenticatedUser();
			OneTimeSale oneTimeSale = this.findOneById(entityId);
		    if(oneTimeSale.getDeviceMode().equalsIgnoreCase("NEWSALE")&&oneTimeSale.getIsInvoiced()=='Y'){
				ChargeCodeMaster chargeCode=this.chargeCodeRepository.findOneByChargeCode(oneTimeSale.getChargeCode());
				// check for old onetimesale's
				if (oneTimeSale.getInvoiceId() != null) {
					Invoice oldInvoice = this.invoiceRepository.findOne(oneTimeSale.getInvoiceId());
					List<BillingOrder> charge = oldInvoice.getCharges();
					BigDecimal discountAmount = charge.get(0).getDiscountAmount();
					// cancel sale calling
					OneTimeSale cancelDeviceSale = new OneTimeSale(oneTimeSale.getClientId(), oneTimeSale.getItemId(),oneTimeSale.getUnits(), oneTimeSale.getQuantity(),
							oneTimeSale.getChargeCode(),oneTimeSale.getUnitPrice(),oneTimeSale.getTotalPrice(),DateUtils.getLocalDateOfTenant(),
							oneTimeSale.getDiscountId(),oneTimeSale.getOfficeId(),CodeNameConstants.CODE_CANCEL_SALE, null);
					this.oneTimeSaleRepository.saveAndFlush(cancelDeviceSale);
					OneTimeSaleData oneTimeSaleData = new OneTimeSaleData(cancelDeviceSale.getId(),oneTimeSale.getClientId(),oneTimeSale.getUnits(),
							oneTimeSale.getChargeCode(),chargeCode.getChargeType(),oneTimeSale.getUnitPrice(),oneTimeSale.getQuantity(),
							oneTimeSale.getTotalPrice(), "Y",oneTimeSale.getItemId(),oneTimeSale.getDiscountId(),chargeCode.getTaxInclusive());
					final CommandProcessingResult invoice = this.invoiceOneTimeSale.reverseInvoiceForOneTimeSale(oneTimeSale.getClientId(),oneTimeSaleData,discountAmount,false);
					cancelDeviceSale.setIsDeleted('Y');
					cancelDeviceSale.setInvoiceId(invoice.resourceId());
					cancelDeviceSale.setIsInvoiced('Y');
					this.oneTimeSaleRepository.save(cancelDeviceSale);
					oldInvoice.setDueAmount(BigDecimal.ZERO);
					this.invoiceRepository.save(oldInvoice);
				}
			 }
			oneTimeSale.setIsDeleted('Y');
			this.oneTimeSaleRepository.save(oneTimeSale);
			return new CommandProcessingResult(Long.valueOf(oneTimeSale.getId()),oneTimeSale.getClientId());

		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private OneTimeSale findOneById(final Long saleId) {
	
		try{
			OneTimeSale oneTimeSale=this.oneTimeSaleRepository.findOne(saleId);
			return oneTimeSale;
		}catch(Exception e){
			throw new DeviceSaleNotFoundException(saleId.toString());
		}
	}
	
	@Override
	public BigDecimal calculateChargeVariantPriceForItems(BigDecimal itemprice, final Long clientId, final ItemData itemData) {

		ChargeVariant chargeVariant = this.chargeVariantRepository.findOne(itemData.getChargeVariant());

		if (chargeVariant != null) {

			final Long devicesExistsCount = this.oneTimeSaleReadPlatformService.retrieveCustomerExistsSaleDevices(clientId, itemData.getId());

			for (ChargeVariantDetails chargeVariantDetails : chargeVariant.getChargeVariantDetails()) {

				DiscountMasterData discountMasterData = new DiscountMasterData(chargeVariant.getId(),chargeVariant.getChargevariantCode(), null,
						chargeVariantDetails.getAmountType(),chargeVariantDetails.getAmount(), null,null,chargeVariant.getStatus());
				if (devicesExistsCount >= 1) {
					if ("ANY".equalsIgnoreCase(chargeVariantDetails.getVariantType())) {
						itemprice = this.invoiceOneTimeSale.calculateDiscount(discountMasterData, itemprice).getDiscountedChargeAmount();
						return itemprice;
					} else if ("Range".equalsIgnoreCase(chargeVariantDetails.getVariantType())) {

						if (devicesExistsCount >= chargeVariantDetails.getFrom()-1 && devicesExistsCount <= chargeVariantDetails.getTo()) {
							itemprice = this.invoiceOneTimeSale.calculateDiscount(discountMasterData,itemprice).getDiscountedChargeAmount();
							return itemprice;
						}

					}

				}
			}
		}

		return itemprice;
	}
	
	private BigDecimal calculateMultiDeviceSalePrice(BigDecimal itemprice,final ItemData itemData,final Long quantity) {

		ChargeVariant chargeVariant = this.chargeVariantRepository.findOne(itemData.getChargeVariant());

		if (chargeVariant != null) {

			for (ChargeVariantDetails chargeVariantDetails : chargeVariant.getChargeVariantDetails()) {

				DiscountMasterData discountMasterData = new DiscountMasterData(chargeVariant.getId(),chargeVariant.getChargevariantCode(), null,
						chargeVariantDetails.getAmountType(),chargeVariantDetails.getAmount(), null,null, chargeVariant.getStatus());
			
					if ("ANY".equalsIgnoreCase(chargeVariantDetails.getVariantType())) {
						itemprice = this.invoiceOneTimeSale.calculateDiscount(discountMasterData, itemprice).getDiscountedChargeAmount();
						return itemprice.multiply(new BigDecimal(quantity.longValue()-1)).add(itemData.getUnitPrice());
					} else if ("Range".equalsIgnoreCase(chargeVariantDetails.getVariantType())) {

						if (quantity >= chargeVariantDetails.getFrom()-1 && quantity <= chargeVariantDetails.getTo()) {
							itemprice = this.invoiceOneTimeSale.calculateDiscount(discountMasterData,itemprice).getDiscountedChargeAmount();
							return itemprice.multiply(new BigDecimal(quantity.longValue()-1)).add(itemData.getUnitPrice());
						}

					}

				}
			}
	       /*If charge vary range cross then taking initial price of device */
			return itemprice.multiply(new BigDecimal(quantity));
	}
	
	private void createTicket(Long clientId,Long saleId) {
		List<ActionDetaislData> reactivationActionDetails = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_HARDWARE_SALE);
		if (reactivationActionDetails.size() != 0) {
			this.actiondetailsWritePlatformService.AddNewActions(reactivationActionDetails, clientId, saleId.toString(), null);
		}
	}
}