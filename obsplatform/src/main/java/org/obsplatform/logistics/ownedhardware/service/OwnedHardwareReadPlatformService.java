package org.obsplatform.logistics.ownedhardware.service;

import java.util.List;

import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.ownedhardware.data.OwnedHardwareData;

public interface OwnedHardwareReadPlatformService {

	List<OwnedHardwareData> retriveOwnedHardwareData(Long clientId);

	List<ItemData> retriveTemplate();

	List<String> retriveSerialNumbers();

	List<OwnedHardwareData> retriveSingleOwnedHardwareData(Long id);

	int retrieveClientActiveDevices(Long clientId);

	int retrieveNoOfActiveUsers(Long clientId);

}
