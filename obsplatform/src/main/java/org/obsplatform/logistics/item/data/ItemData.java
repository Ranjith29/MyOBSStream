package org.obsplatform.logistics.item.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.chargecode.data.ChargesData;
import org.obsplatform.billing.chargevariant.data.ChargeVariantData;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.logistics.itemdetails.data.InventoryGrnData;
import org.obsplatform.organisation.feemaster.data.FeeMasterData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.region.data.RegionData;

public class ItemData {
	
	private Long id;
	private String itemCode;
	private String units;
	private String chargeCode;
	private BigDecimal unitPrice;
	private BigDecimal totalPrice;
	private List<ItemData> itemDatas;
	private ItemData itemData;
	private String quantity;
	private List<EnumOptionData> itemClassData;
	private List<EnumOptionData> unitData;
	private List<ChargesData> chargesData;
	private String itemDescription;
	private int warranty;
	private String itemClass;
	private List<DiscountMasterData> discountMasterDatas;
	private Long itemMasterId;
	private LocalDate changedDate;
	private List<ItemData> auditDetails;
	private Long usedItems;
	private Long availableItems;
	private Long totalItems;
	private List<RegionData> regionDatas;
	private Long regionId;
	private String price;
	private List<ItemData> itemPricesDatas;
	private Long reorderLevel;
	private List<FeeMasterData> feeMasterData;
	private LocalDate date;
	private Collection<InventoryGrnData> grnData;
	private List<ChargeVariantData> chargeVariantDatas;
	private Long chargeVariant;
	private String variantCode;
	
	private Collection<MCodeData> itemCategoryData;
	private Long itemCategoryId;
	private String itemCategoryName;
	
	public ItemData(Long id, String itemCode, String itemDesc,String itemClass,String units,   String chargeCode, int warranty, BigDecimal unitPrice,
			Long usedItems,Long availableItems,Long totalItems, Long reorderLevel,Long chargeVariant,
			Long itemCategoryId,String itemCategoryName) {
		
		this.id=id;
		this.itemCode=itemCode;
		this.units=units;
		this.unitPrice=unitPrice;
		this.chargeCode=chargeCode;
		this.itemDescription=itemDesc;
		this.warranty=warranty;
		this.itemClass=itemClass;
		this.usedItems=usedItems;
		this.availableItems=availableItems;
		this.totalItems=totalItems;
		this.reorderLevel = reorderLevel;
		this.chargeVariant = chargeVariant;
		this.itemCategoryId=itemCategoryId;
		this.itemCategoryName=itemCategoryName;
	}
	
	
	public ItemData(ItemData itemData, BigDecimal totalPrice,String quantity) {

		this.id=itemData.getId();
		this.itemCode=itemData.getItemCode();
		this.chargeCode=itemData.getChargeCode();
		this.units=itemData.getUnits();
		this.unitPrice=itemData.getUnitPrice();
		this.totalPrice=totalPrice;
		this.quantity=quantity;
	}

	public ItemData(ItemData itemData, List<EnumOptionData> itemClassdata,List<EnumOptionData> unitTypeData, 
			List<ChargesData> chargeDatas, List<RegionData> regionDatas,List<ChargeVariantData> chargeVariantDatas,
			Collection<MCodeData> itemCategoryData) {
		
		if (itemData != null) {
			this.id = itemData.getId();
			this.itemCode = itemData.getItemCode();
			this.units = itemData.getUnits();
			this.unitPrice = itemData.getUnitPrice();
			this.chargeCode = itemData.getChargeCode();
			this.itemDescription = itemData.getItemDescription();
			this.warranty = itemData.getWarranty();
			this.itemClass = itemData.getItemClass();
			this.reorderLevel = itemData.getReorderLevel();
			this.itemCategoryId = itemData.getItemCategoryId();
			this.itemCategoryName = itemData.getItemCategoryName();
		}
		this.itemClassData=itemClassdata;
		this.unitData=unitTypeData;
		this.chargesData=chargeDatas;
		this.regionDatas = regionDatas;
		this.chargeVariantDatas = chargeVariantDatas;
		this.itemCategoryData=itemCategoryData;
	}

	public ItemData(List<ItemData> itemCodes) {
		this.itemDatas = itemCodes;
	}

	public ItemData(Long id, Long itemMasterId, String itemCode,
			BigDecimal unitPrice, Date changedDate, Long regionId) {
		
		this.id=id;
		this.itemMasterId=itemMasterId;
		this.itemCode=itemCode;
		this.unitPrice=unitPrice;
		this.changedDate=new LocalDate(changedDate);
		this.regionId = regionId;
	}
	
	public ItemData(final Long id, final String itemCode, final String itemDescription, final String chargeCode, 
			final BigDecimal unitPrice, final Long chargeVariant) {
		
		this.id=id;
		this.itemCode=itemCode;
		this.itemDescription=itemDescription;
		this.chargeCode=chargeCode;
		this.unitPrice=unitPrice;
		this.chargeVariant = chargeVariant;
	}

	public ItemData(final Long id, final Long itemId, final Long regionId, final String price,final Long chargeVariant,
			final String variantCode) {
		
		this.id = id;
		this.itemMasterId = itemId;
		this.regionId = regionId;
		this.price = price;
		this.chargeVariant = chargeVariant;
		this.variantCode = variantCode;
		
	}
	
	// this for retrive item category type new one
	/*public ItemData(Long id, String itemCode, String itemDesc,String itemClass,String units,   String chargeCode, int warranty, BigDecimal unitPrice,
			Long usedItems,Long availableItems,Long totalItems, Long reorderLevel,Long chargeVariant,
			Long itemCategoryId,String itemCategoryName) {
		
		this.id=id;
		this.itemCode=itemCode;
		this.units=units;
		this.unitPrice=unitPrice;
		this.chargeCode=chargeCode;
		this.itemDescription=itemDesc;
		this.warranty=warranty;
		this.itemClass=itemClass;
		this.usedItems=usedItems;
		this.availableItems=availableItems;
		this.totalItems=totalItems;
		this.reorderLevel = reorderLevel;
		this.chargeVariant = chargeVariant;
		this.itemCategoryId=itemCategoryId;
		this.itemCategoryName=itemCategoryName;
	}*/
	

	public String getChargeCode() {
		return chargeCode;
	}

	public List<ItemData> getItemDatas() {
		return itemDatas;
	}

	public String getItemClass() {
		return itemClass;
	}

	public ItemData getItemData() {
		return itemData;
	}

	public String getQuantity() {
		return quantity;
	}

	public List<EnumOptionData> getItemClassData() {
		return itemClassData;
	}

	public List<EnumOptionData> getUnitData() {
		return unitData;
	}

	public List<ChargesData> getChargesData() {
		return chargesData;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public int getWarranty() {
		return warranty;
	}

	public Long getId() {
		return id;
	}

	public String getItemCode() {
		return itemCode;
	}

	public String getUnits() {
		return units;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void set(BigDecimal totalPrice) {
	this.totalPrice=totalPrice;
		
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public List<ItemData> getItemCodeData() {
		return 	itemDatas;
	}

	public List<RegionData> getRegionDatas() {
		return regionDatas;
	}

	public void setRegionDatas(List<RegionData> regionDatas) {
		this.regionDatas = regionDatas;
	}

	public List<ItemData> getItemPricesDatas() {
		return itemPricesDatas;
	}

	public void setItemPricesDatas(List<ItemData> itemPricesDatas) {
		this.itemPricesDatas = itemPricesDatas;
	}

	public Long getReorderLevel() {
		return reorderLevel;
	}

	public void setReorderLevel(Long reorderLevel) {
		this.reorderLevel = reorderLevel;
	}

	public void setUnitPrice(BigDecimal itemprice) {
	    this.unitPrice = itemprice;

	}

	public List<FeeMasterData> getFeeMasterData() {
		return feeMasterData;
	}

	public void setFeeMasterData(List<FeeMasterData> feeMasterData) {
		this.feeMasterData = feeMasterData;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
		
	public Collection<InventoryGrnData> getGrnData() {
		return grnData;
	}

	public void setGrnData(Collection<InventoryGrnData> grnData) {
		this.grnData = grnData;
	}
	
	public List<ItemData> getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(List<ItemData> auditDetails) {
		this.auditDetails = auditDetails;
	}

	public List<ChargeVariantData> getChargeVariantDatas() {
		return chargeVariantDatas;
	}

	public Long getRegionId() {
		return regionId;
	}

	public Long getChargeVariant() {
		return chargeVariant;
	}

	public String getVaraintCode() {
		return variantCode;
	}


	public Long getItemCategoryId() {
		return itemCategoryId;
	}


	public void setItemCategoryId(Long itemCategoryId) {
		this.itemCategoryId = itemCategoryId;
	}


	public String getItemCategoryName() {
		return itemCategoryName;
	}


	public void setItemCategoryName(String itemCategoryName) {
		this.itemCategoryName = itemCategoryName;
	}

}
