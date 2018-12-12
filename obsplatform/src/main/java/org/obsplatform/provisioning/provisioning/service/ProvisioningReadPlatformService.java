package org.obsplatform.provisioning.provisioning.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.provisioning.provisioning.data.ProcessRequestData;
import org.obsplatform.provisioning.provisioning.data.ProvisioningCommandParameterData;
import org.obsplatform.provisioning.provisioning.data.ProvisioningData;
import org.obsplatform.provisioning.provisioning.data.ServiceParameterData;

public interface ProvisioningReadPlatformService {

	List<ProvisioningData> getProvisioningData();

	List<McodeData> retrieveProvisioningCategory();

	List<McodeData> retrievecommands();

	ProvisioningData retrieveIdData(Long id);

	List<ProvisioningCommandParameterData> retrieveCommandParams(Long id);

	List<ServiceParameterData> getSerivceParameters(Long orderId);

	List<ServiceParameterData> getProvisionedSerivceParameters(Long orderId);

	//Long getHardwareDetails(String oldHardWare, Long clientId, String name);

	List<ProcessRequestData> getProcessRequestData(String orderNo);
	
	List<ProcessRequestData> getProcessRequestClientData(Long clientId);

	ProcessRequestData getProcessRequestIDData(Long id);
	
	//ProcessRequestData getProcessRequestClientData(Long clientId);

	Collection<MCodeData> retrieveVlanDetails(String string);
}
