package org.obsplatform.finance.clientbalance.service;


import java.util.List;

import org.obsplatform.finance.clientbalance.data.ClientBalanceData;


public interface ClientBalanceReadPlatformService {

	
		ClientBalanceData retrieveBalance(Long clientid);

		List<ClientBalanceData> retrieveAllClientBalances(Long clientId);

	
	}

