package org.obsplatform.logistics.itemdetails.data;

import java.util.Collection;

import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.office.data.OfficeData;


public class ItemDetailsData {
	
	private final Collection<InventoryGrnData> inventoryGrnDatas;
	private final Collection<MCodeData> qualityDatas;
	private final Collection<MCodeData> statusDatas;
	private final Long id;
	private final Long itemMasterId; 
	private final String serialNumber;
	private final Long grnId;
	private final String provisioningSerialNumber;
	private final String quality;
	private final String status;
	private final Long officeId;
	private final Long clientId;
	private final Long warranty;
	private final String remarks;
	private final String itemDescription;
	private final String supplier;
	private final String officeName;
	private final String accountNumber;
	private Collection<OfficeData> officeData;
	private Collection<ItemData> itemMasterData;
	private String units;
	private Long quantity;
	
	public ItemDetailsData(Collection<InventoryGrnData> inventoryGrnData,Collection<MCodeData> qualityDatas,Collection<MCodeData> statusDatas,
			String serialNumber, String provisionSerialNumber) {
		
		this.inventoryGrnDatas=inventoryGrnData;
		this.qualityDatas=qualityDatas;
		this.statusDatas=statusDatas;
		this.officeId=null;
		this.id=null;
		this.itemMasterId=null;
		this.serialNumber=serialNumber;
		this.grnId=null;
		this.provisioningSerialNumber=provisionSerialNumber;
		this.quality=null;
		this.status=null;
		this.warranty=null;
		this.remarks=null;
		this.itemDescription = null;
		this.supplier = null;
		this.clientId = null;
		this.officeName = null;
		this.accountNumber = null;
		
		
	}

	public ItemDetailsData(final Long id,final  Long itemMasterId,final String serialNumber,final Long grnId,final  String provisioningSerialNumber,final  String quality,
			final String status,final Long warranty,final String remarks,final String itemDescription,final String supplier,final Long clientId,final String officeName, 
			final String accountNumber, String units, Long quantity) {
		
		this.id=id;
		this.itemMasterId=itemMasterId;
		this.serialNumber=serialNumber;
		this.grnId=grnId;
		this.provisioningSerialNumber=provisioningSerialNumber;
		this.quality=quality;
		this.status=status;
		this.warranty=warranty;
		this.remarks=remarks;
		this.itemDescription = itemDescription;
		this.officeId=null;
		this.supplier = supplier;
		this.clientId = clientId;
		this.officeName = officeName;
		this.accountNumber = accountNumber;
		this.inventoryGrnDatas=null;
		this.qualityDatas=null;
		this.statusDatas=null;
		this.units = units;
		this.quantity = quantity;
	}
	
	public ItemDetailsData(Collection<OfficeData> officeData, Collection<ItemData> itemMasterData,Collection<MCodeData> qualityDatas) {
		this.officeData = officeData;
		this.itemMasterData = itemMasterData;
		this.qualityDatas = qualityDatas;
		this.inventoryGrnDatas = null;
		this.statusDatas = null;
		this.id = null;
		this.itemMasterId = null;
		this.serialNumber = null;
		this.grnId = null;
		this.provisioningSerialNumber = null;
		this.quality = null;
		this.status = null;
		this.officeId = null;
		this.clientId = null;
		this.warranty = null;
		this.remarks = null;
		this.itemDescription = null;
		this.supplier = null;
		this.officeName = null;
		this.accountNumber = null;
	}

	public Collection<InventoryGrnData> getInventoryGrnDatas() {
		return inventoryGrnDatas;
	}

	public Collection<MCodeData> getQualityDatas() {
		return qualityDatas;
	}

	public Collection<MCodeData> getStatusDatas() {
		return statusDatas;
	}

	public Long getId() {
		return id;
	}

	public Long getItemMasterId() {
		return itemMasterId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public Long getGrnId() {
		return grnId;
	}

	public String getProvisioningSerialNumber() {
		return provisioningSerialNumber;
	}

	public String getQuality() {
		return quality;
	}

	public String getStatus() {
		return status;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getWarranty() {
		return warranty;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public String getSupplier() {
		return supplier;
	}

	public String getOfficeName() {
		return officeName;
	}

	public String getAccountNumber() {
		return accountNumber;
	}
	
	public Collection<OfficeData> getOfficeData() {
		return officeData;
	}

	public void setOfficeData(Collection<OfficeData> officeData) {
		this.officeData = officeData;
	}

	public Collection<ItemData> getItemMasterData() {
		return itemMasterData;
	}

	public void setItemMasterData(Collection<ItemData> itemMasterData) {
		this.itemMasterData = itemMasterData;
	}

	
	
}
