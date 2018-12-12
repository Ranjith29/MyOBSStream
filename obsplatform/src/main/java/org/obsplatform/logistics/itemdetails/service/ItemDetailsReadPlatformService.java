package org.obsplatform.logistics.itemdetails.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.itemdetails.data.AllocationHardwareData;
import org.obsplatform.logistics.itemdetails.data.ItemDetailsData;
import org.obsplatform.logistics.itemdetails.data.ItemMasterIdData;
import org.obsplatform.logistics.itemdetails.data.ItemSerialNumberData;
import org.obsplatform.logistics.itemdetails.data.QuantityData;

public interface ItemDetailsReadPlatformService {

	public ItemSerialNumberData retriveAllocationData(List<String> itemSerialNumbers,QuantityData quantityData, ItemMasterIdData itemMasterIdData);
	
	public AllocationHardwareData retriveInventoryItemDetail(String serialNumber);

	List<String> retriveSerialNumbers();

	public Page<ItemDetailsData> retriveAllItemDetails(SearchSqlQuery searchItemDetails,String officeName,String itemCode,String quality);

	public List<String> retriveSerialNumbersOnKeyStroke(Long oneTimeSaleId,String query, Long officeId);

	public List<ItemDetailsData> retriveSerialNumbersOnKeyStroke(String query);
	
	public ItemDetailsData retriveSingleItemDetail(Long itemId);

	public ItemData retriveItemDetailsDataBySerialNum(String query, Long clientId);
	
	public Long getItemMasterIdBySerialNo(String serialNumber);
	
	
}
