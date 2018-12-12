package org.obsplatform.provisioning.entitlements.service;

import java.util.List;

import org.obsplatform.provisioning.entitlements.data.ClientEntitlementData;
import org.obsplatform.provisioning.entitlements.data.EntitlementsData;
import org.obsplatform.provisioning.entitlements.data.StakerData;


public interface EntitlementReadPlatformService {
	

	List<EntitlementsData> getProcessingData(Long id, String provisioningSystem, String serviceType);

	ClientEntitlementData getClientData(Long clientId);

	StakerData getData(String mac);

	List<EntitlementsData> getBeeniusProcessingData(Long no, String provisioningSystem);

	List<EntitlementsData> getZebraOTTProcessingData(Long no, String provisioningSystem);

	List<EntitlementsData> getCubiWareProcessingData(Long no, String provisioningSystem);

	//List<EntitlementsData> getProcessingData(Long id,String provisioningSys,String serviceType, Long clientId);
	
}
