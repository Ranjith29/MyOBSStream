package org.obsplatform.logistics.onetimesale.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.chargecode.data.ChargesData;
import org.obsplatform.cms.eventorder.data.EventOrderData;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.organisation.office.data.OfficeData;

public class OneTimeSaleData {
	
	private List<ChargesData> chargesDatas;
	private List<ItemData> itemDatas;
	private Long itemId;
	private Long id;
	private String units;
	private String itemCode;
	private String chargeCode;
	private String quantity;
	private BigDecimal unitPrice;
	private BigDecimal totalPrice;
	private LocalDate saleDate;
	private Long clientId;
	private String isInvoiced;
	private OneTimeSaleData salesData;
	private boolean flag = false;
	private String hardwareAllocated;
	private String itemClass;
	private List<AllocationDetailsData> allocationData;
	private List<DiscountMasterData> discountMasterDatas;
	private Long discountId;
	private List<OneTimeSaleData> oneTimeSaleData;
	private List<EventOrderData> eventOrdersData;
	private String serialNo;
	private Collection<OfficeData> officesData;
	private Integer taxInclusive;
	private String chargeType;
	private String saleType;
	private LocalDate warrantyDate;
	private String propertyCode;
	private LocalDate date;
	private Long itemDetailId;
	private String provserialnumber;
	private String quality;
	private String remarks;
	
	public OneTimeSaleData(final List<ItemData> itemData, final List<DiscountMasterData> discountData,
			final Collection<OfficeData> officesData) {
		
		this.itemDatas=itemData;
		this.discountMasterDatas=discountData;
		this.officesData=officesData;
	}

	/*hardware allocated and flag is added by rahman */

	public OneTimeSaleData(Long id, LocalDate saleDate, String itemCode,
			String chargeCode, String quantity, BigDecimal totalPrice,String hardwareAllocated,String itemClass, String serialNo,
			String units,String saleType,LocalDate warrantyDate, String propertyCode,Long itemDetailId,String provserialnumber,String quality, 
			String remarks) {
		this.id=id;
		this.saleDate=saleDate;
		this.itemCode=itemCode;
		this.chargeCode=chargeCode;
		this.quantity=quantity;
		this.totalPrice=totalPrice;
		this.hardwareAllocated = hardwareAllocated;
		this.flag = hardwareAllocated.equalsIgnoreCase("ALLOCATED")?true:false;
		this.itemClass = itemClass;
		this.serialNo=serialNo;
		this.units = units;
		this.saleType = saleType;
		this.warrantyDate = warrantyDate;
		this.propertyCode = propertyCode;
		this.itemDetailId = itemDetailId;
		this.provserialnumber = provserialnumber;
		this.quality = quality;
		this.remarks = remarks;
	}


	public OneTimeSaleData(Long oneTimeSaleId, Long clientId, String units,String chargeCode,
			String chargeType, BigDecimal unitPrice, String quantity,BigDecimal totalPrice, 
			String isInvoiced, Long itemId, Long discountId,Integer taxInclusive) {
		this.id = oneTimeSaleId;
		this.clientId = clientId;
		this.units=units;
		this.chargeCode = chargeCode;
		this.chargeType = chargeType;
		this.unitPrice= unitPrice;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.isInvoiced = isInvoiced;
		this.itemId = itemId;
		this.discountId =discountId;
		this.taxInclusive = taxInclusive;
		
	}


	public OneTimeSaleData() {
		
	}

	public OneTimeSaleData(final List<OneTimeSaleData> salesData, final List<EventOrderData> eventOrderDatas) {
             
		this.oneTimeSaleData=salesData;
		this.eventOrdersData=eventOrderDatas;
	
	}

	/**
	 * @return the chargesDatas
	 */
	public List<ChargesData> getChargesDatas() {
		return chargesDatas;
	}

	public void setChargesDatas(List<ChargesData> chargesDatas) {
		this.chargesDatas = chargesDatas;
	}

	/**
	 * @return the itemDatas
	 */
	public List<ItemData> getItemDatas() {
		return itemDatas;
	}

	public void setItemDatas(List<ItemData> itemDatas) {
		this.itemDatas = itemDatas;
	}

	/**
	 * @return the itemId
	 */
	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * @return the itemCode
	 */
	public String getItemCode() {
		return itemCode;
	}

	/**
	 * @param itemCode the itemCode to set
	 */
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	/**
	 * @return the chargeCode
	 */
	public String getChargeCode() {
		return chargeCode;
	}

	/**
	 * @param chargeCode the chargeCode to set
	 */
	public void setChargeCode(String chargeCode) {
		this.chargeCode = chargeCode;
	}

	/**
	 * @return the quantity
	 */
	public String getQuantity() {
		return quantity;
	}


	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the totalPrice
	 */
	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	
	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	/**
	 * @return the saleDate
	 */
	public LocalDate getSaleDate() {
		return saleDate;
	}

	public void setSaleDate(LocalDate saleDate) {
		this.saleDate = saleDate;
	}

	/**
	 * @return the clientId
	 */
	public Long getClientId() {
		return clientId;
	}

	
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the isInvoiced
	 */
	public String getIsInvoiced() {
		return isInvoiced;
	}

	public void setIsInvoiced(String isInvoiced) {
		this.isInvoiced = isInvoiced;
	}

	/**
	 * @return the salesData
	 */
	public OneTimeSaleData getSalesData() {
		return salesData;
	}

	public void setSalesData(OneTimeSaleData salesData) {
		this.salesData = salesData;
	}

	/**
	 * @return the hardwareAllocated
	 */
	public String getHardwareAllocated() {
		return hardwareAllocated;
	}

	public void setHardwareAllocated(String hardwareAllocated) {
		this.hardwareAllocated = hardwareAllocated;
	}

	/**
	 * @return the itemClass
	 */
	public String getItemClass() {
		return itemClass;
	}

	
	public void setItemClass(String itemClass) {
		this.itemClass = itemClass;
	}

	/**
	 * @return the allocationData
	 */
	public List<AllocationDetailsData> getAllocationData() {
		return allocationData;
	}

	public void setAllocationData(List<AllocationDetailsData> allocationData) {
		this.allocationData = allocationData;
	}

	/**
	 * @return the discountMasterDatas
	 */
	public List<DiscountMasterData> getDiscountMasterDatas() {
		return discountMasterDatas;
	}

	public void setDiscountMasterDatas(List<DiscountMasterData> discountMasterDatas) {
		this.discountMasterDatas = discountMasterDatas;
	}

	/**
	 * @return the discountId
	 */
	public Long getDiscountId() {
		return discountId;
	}

	/**
	 * @param discountId the discountId to set
	 */
	public void setDiscountId(Long discountId) {
		this.discountId = discountId;
	}

	/**
	 * @return the oneTimeSaleData
	 */
	public List<OneTimeSaleData> getOneTimeSaleData() {
		return oneTimeSaleData;
	}

	public void setOneTimeSaleData(List<OneTimeSaleData> oneTimeSaleData) {
		this.oneTimeSaleData = oneTimeSaleData;
	}

	/**
	 * @return the eventOrdersData
	 */
	public List<EventOrderData> getEventOrdersData() {
		return eventOrdersData;
	}


	public void setEventOrdersData(List<EventOrderData> eventOrdersData) {
		this.eventOrdersData = eventOrdersData;
	}

	/**
	 * @return the serialNo
	 */
	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	/**
	 * @return the officesData
	 */
	public Collection<OfficeData> getOfficesData() {
		return officesData;
	}
	
	public void setOfficesData(Collection<OfficeData> officesData) {
		this.officesData = officesData;
	}

	/**
	 * @return the taxInclusive
	 */
	public Integer getTaxInclusive() {
		return taxInclusive;
	}

	public void setTaxInclusive(Integer taxInclusive) {
		this.taxInclusive = taxInclusive;
	}

	/**
	 * @return the chargeType
	 */
	public String getChargeType() {
		return chargeType;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @return the unitPrice
	 */
	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	
}
