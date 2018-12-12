package org.obsplatform.accounting.journalentry.service;

import org.obsplatform.accounting.journalentry.data.LoanDTO;

public interface AccountingProcessorForLoan {

    void createJournalEntriesForLoan(LoanDTO loanDTO);

}
