package org.obsplatform.organisation.officeadjustments.service;

import java.util.List;

import org.obsplatform.finance.officebalance.data.OfficeBalanceData;

public interface OfficeAdjustmentsReadPaltformService {

	List<OfficeBalanceData> retrieveOfficeBalance(Long entityId);

}