/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.office.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.finance.financialtransaction.data.FinancialTransactionsData;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.data.OfficeTransactionData;

public interface OfficeReadPlatformService {

    Collection<OfficeData> retrieveAllOffices();

    Collection<OfficeData> retrieveAllOfficesForDropdown();

    OfficeData retrieveOffice(Long officeId);

    OfficeData retrieveNewOfficeTemplate();

    Collection<OfficeData> retrieveAllowedParents(Long officeId);

    Collection<OfficeTransactionData> retrieveAllOfficeTransactions();

    OfficeTransactionData retrieveNewOfficeTransactionDetails();

	List<OfficeData> retrieveAgentTypeData();

	Collection<FinancialTransactionsData> retreiveOfficeFinancialTransactionsData(Long officeId);
	
	Collection<OfficeData> retrieveOfficeTypeData();

	Collection<OfficeData> retrieveAllParentOffices();
}