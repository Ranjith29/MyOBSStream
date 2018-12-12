package org.obsplatform.provisioning.wifimaster.service;

import java.util.List;

import org.obsplatform.provisioning.wifimaster.data.WifiData;

/**
 * @author anil
 * 
 */
public interface WifiMasterReadPlatformService {

	List<WifiData> wifiAllDetailsData();

	WifiData retrievedSingleWifiData(Long id);
	
	WifiData getByOrderId(Long clientId,Long orderId);
	
	List<WifiData> WifiDataGetByClientId(Long clientId);

}
