package org.obsplatform.organisation.officefinancialtransaction.service;

import java.util.Collection;

import org.obsplatform.finance.financialtransaction.data.FinancialTransactionsData;

public interface OfficeFinancialTransactionReadPlatformService {

	Collection<FinancialTransactionsData> retreiveOfficeFinancialTransactionsData(Long officeId);
}
