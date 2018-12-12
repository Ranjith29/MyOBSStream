package org.obsplatform.finance.depositandrefund.service;



import org.obsplatform.organisation.feemaster.data.FeeMasterData;


/**
 * @author hugo
 * 
 */
public interface DepositeReadPlatformService {

	FeeMasterData retrieveDepositDetails(Long feeId, Long clientId);
	
	

}
