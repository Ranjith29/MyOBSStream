package org.obsplatform.billing.selfcare.service;

import org.obsplatform.billing.selfcare.data.SelfCareData;

public interface SelfCareReadPlatformService {
	
	public Long getClientId(String uniqueReference);

	public String getEmail(Long clientId);
	
	public SelfCareData login(String userName, String password);
/*=======
	public Long login(String userName, String password);
	
>>>>>>> obsplatform-1.01*/
}
