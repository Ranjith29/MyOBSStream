package org.obsplatform.portfolio.association.service;

import java.util.List;

import org.obsplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.obsplatform.portfolio.association.data.AssociationData;
import org.obsplatform.portfolio.association.data.HardwareAssociationData;


public interface HardwareAssociationReadplatformService {

	//List<HardwareAssociationData> retrieveClientHardwareDetails(Long clientId);

	List<HardwareAssociationData> retrieveClientAllocatedPlan(Long clientId, String itemCode);

	List<AssociationData> retrieveClientAssociationDetails(Long clientId, String serialNo);

	AssociationData retrieveSingleDetails(Long id);

	List<AssociationData> retrieveHardwareData(Long clientId);

	List<AssociationData> retrieveplanData(Long clientId);

	List<HardwareAssociationData> retrieveClientAllocatedHardwareDetails(Long clientId);

	List<AssociationData> retrieveCustomerHardwareAllocationData(Long clientId,Long orderId,Long itemId);

	List<AllocationDetailsData> retrieveClientAllocatedPlanByServiceMap(Long clientId, Long itemId);

	AssociationData retrieveAssociationsDetailsWithSerialNum(Long clientId,String serialNumber);

	List<AssociationData> retrieveClientAssociationDetailsForProperty(Long clientId, String serialNumber);
	
	String retrieveClientTalkSerialNo(Long clientId);
	
	String retrieveClientTalkgoSerialNo(Long clientId,Long orderId,Long planId);
	
	String retrieveClientTalkSerialNoFirstNo(Long clientId);
	
	
}
