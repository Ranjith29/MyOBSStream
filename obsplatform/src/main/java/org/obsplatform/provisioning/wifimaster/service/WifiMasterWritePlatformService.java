package org.obsplatform.provisioning.wifimaster.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Service;

@Service
public interface WifiMasterWritePlatformService {

	public CommandProcessingResult createWifi(final JsonCommand command);
	
	public CommandProcessingResult updateWifi(final JsonCommand command, final Long id);
	
	public CommandProcessingResult deleteWifi(Long id);
	
	//public CommandProcessingResult UpdateWifiDetailsByOrderId(Long orderId,Long clientId,String ssid,String wifiPassword);
	
	public CommandProcessingResult UpdateWifiByOrderId(final JsonCommand command,Long orderId,Long clientId);
	
	
}
