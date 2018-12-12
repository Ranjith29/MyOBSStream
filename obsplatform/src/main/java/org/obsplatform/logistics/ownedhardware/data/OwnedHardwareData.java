package org.obsplatform.logistics.ownedhardware.data;

import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.logistics.item.data.ItemData;


public class OwnedHardwareData {

	private Long id;
	private Long clientId;
	private String serialNumber;
	private String provisioningSerialNumber;
	private LocalDate allocationDate;
	private String status;
	private String itemType;
	private String propertyCode;
	private List<ItemData> itemDatas;
	private List<OwnedHardwareData> ownedHardwareDatas;
	private LocalDate date;
	
	public OwnedHardwareData() {
		
	}

	public OwnedHardwareData(final Long id, final Long clientId, final String serialNumber,
			final String provisioningSerialNumber, final LocalDate allocationDate,
			final String status, final String itemType, final String propertyCode) {

		this.id = id;
		this.clientId = clientId;
		this.serialNumber = serialNumber;
		this.provisioningSerialNumber = provisioningSerialNumber;
		this.allocationDate = allocationDate;
		this.status = status;
		this.itemType = itemType;
		this.propertyCode = propertyCode;
	}

	public OwnedHardwareData(final List<ItemData> itemCodes,final List<OwnedHardwareData> ownedHardwareDatas) {
		this.itemDatas = itemCodes;
		this.ownedHardwareDatas = ownedHardwareDatas;
	}

	public Long getId() {
		return id;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getProvisioningSerialNumber() {
		return provisioningSerialNumber;
	}

	public void setProvisioningSerialNumber(String provisioningSerialNumber) {
		this.provisioningSerialNumber = provisioningSerialNumber;
	}

	public LocalDate getAllocationDate() {
		return allocationDate;
	}

	public void setAllocationDate(LocalDate allocationDate) {
		this.allocationDate = allocationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getPropertyCode() {
		return propertyCode;
	}

	public List<ItemData> getItemDatas() {
		return itemDatas;
	}

	public List<OwnedHardwareData> getOwnedHardwareDatas() {
		return ownedHardwareDatas;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

}
