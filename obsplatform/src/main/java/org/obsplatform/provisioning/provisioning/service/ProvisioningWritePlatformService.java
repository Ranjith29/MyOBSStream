package org.obsplatform.provisioning.provisioning.service;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.provisioning.provisioning.data.ProvisionAdapter;

public interface ProvisioningWritePlatformService {

	CommandProcessingResult createProvisioning(JsonCommand command);

	CommandProcessingResult updateProvisioning(JsonCommand command);

	CommandProcessingResult deleteProvisioningSystem(JsonCommand command);

	CommandProcessingResult createNewProvisioningSystem(JsonCommand command,Long entityId);

	CommandProcessingResult updateProvisioningDetails(Long entityId);

	void updateHardwareDetails(Long clientId, String serialNumber,String oldSerialnumber, String provSerilaNum, String oldHardware);

	CommandProcessingResult postOrderDetailsForProvisioning(Order order, String planName,String reqType, Long prepareId, 
			String groupName, String serialNo,Long orderId,String provisioningSys,Long addonId, String phoneNumber);

	CommandProcessingResult updateIpDetails(Long entityId, JsonCommand command);

	CommandProcessingResult confirmProvisioningDetails(Long entityId);

	String runAdapterCommands(String apiRequestBodyAsJson);

	List<ProvisionAdapter> gettingLogInformation(String apiRequestBodyAsJson);

	CommandProcessingResult postDetailsForProvisioning(Long clientId,Long resourceId, String requestType, String provisioningSystem, String hardwareId);

	CommandProcessingResult postPsswordForProvisioning(Order order, String planCode, String requestType, Long orderId, Date startDate, 
			String phoneNumber, String hwSerialNo, String contractPeriod, String provisioningSystem);


}
