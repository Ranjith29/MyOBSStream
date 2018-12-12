package org.obsplatform.logistics.onetimesale.service;

import java.util.List;

import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.obsplatform.logistics.onetimesale.data.OneTimeSaleData;

public interface OneTimeSaleReadPlatformService {

	List<ItemData> retrieveItemData();

	List<OneTimeSaleData> retrieveClientOneTimeSalesData(Long clientId);

	List<OneTimeSaleData> retrieveOnetimeSalesForInvoice(Long clientId);

	OneTimeSaleData retrieveSingleOneTimeSaleDetails(Long saleId);

	List<AllocationDetailsData> retrieveAllocationDetails(Long orderId);

	AllocationDetailsData retrieveAllocationDetailsBySerialNo(String serialNo);
	
	List<AllocationDetailsData> retrieveUnAllocationDetails(Long orderId, Long clientId);

	Long retrieveCustomerExistsSaleDevices(Long clientId, Long itemId);
	
	List<ItemData> retrieveItemByCategoryType(Long itemId);
	
	ItemData retrieveItemByCategoryTypeByItemId(Long itemId);
	

}
