package org.obsplatform.portfolio.transactionhistory.service;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.portfolio.transactionhistory.data.TransactionHistoryData;

public interface TransactionHistoryReadPlatformService {

	
	public Page<TransactionHistoryData> retriveTransactionHistoryClientId(SearchSqlQuery searchTransactionHistory, Long clientId);

	public Page<TransactionHistoryData> retriveTransactionHistoryById(SearchSqlQuery searchTransactionHistory, Long clientId);
}
