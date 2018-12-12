package org.obsplatform.finance.billingmaster.service;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.finance.billingorder.data.BillDetailsData;
import org.obsplatform.finance.financialtransaction.data.FinancialTransactionsData;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.scheduledjobs.scheduledjobs.data.BatchHistoryData;

public interface BillMasterReadPlatformService {

	List<FinancialTransactionsData> retrieveFinancialData(Long clientId);

	Page<FinancialTransactionsData> retrieveInvoiceFinancialData(SearchSqlQuery searchFinancialTransaction, Long clientId);

	List<FinancialTransactionsData> getFinancialTransactionData(Long id);

	Page<BillDetailsData> retrieveStatments(SearchSqlQuery searchCodes, Long clientId);

	BigDecimal retrieveClientBalance(Long clientId);

	List<FinancialTransactionsData> retrieveSingleInvoiceData(Long invoiceId);
	
	List<BillDetailsData> retrieveStatementDetails(Long billId);

	Page<FinancialTransactionsData> retrieveSampleData(SearchSqlQuery searchFinancialTransaction, Long clientId,String type);

	List<FinancialTransactionsData> retriveDataForDownload(Long clientId,String fromDate, String toDate);
	
	List<BatchHistoryData> retrieveBatchProcessStatementsDetails();
	
	List<Long> retriveStatementsIdsByBatchId(String batchId);
	
	Long retrieveMaxStatementId(Long clientId);
}
