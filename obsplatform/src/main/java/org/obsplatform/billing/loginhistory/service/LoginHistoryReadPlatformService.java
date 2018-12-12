package org.obsplatform.billing.loginhistory.service;

import org.obsplatform.billing.loginhistory.data.LoginHistoryData;

public interface LoginHistoryReadPlatformService {

	LoginHistoryData retrieveSessionId(String id);

	int retrieveNumberOfUsers(String username);
	
}
