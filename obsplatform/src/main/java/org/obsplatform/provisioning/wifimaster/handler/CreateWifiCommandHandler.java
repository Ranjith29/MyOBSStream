package org.obsplatform.provisioning.wifimaster.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.provisioning.wifimaster.service.WifiMasterWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CreateWifiCommandHandler implements NewCommandSourceHandler {

	private final WifiMasterWritePlatformService  wifiMasterWritePlatformService;

	@Autowired
	CreateWifiCommandHandler(
			final WifiMasterWritePlatformService wifiMasterWritePlatformService) {
		this.wifiMasterWritePlatformService = wifiMasterWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return wifiMasterWritePlatformService.createWifi(command);
	}

}
