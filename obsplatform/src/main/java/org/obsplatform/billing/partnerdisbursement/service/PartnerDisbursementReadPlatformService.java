package org.obsplatform.billing.partnerdisbursement.service;

import java.util.List;

import org.obsplatform.billing.partnerdisbursement.data.PartnerDisbursementData;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;

public interface PartnerDisbursementReadPlatformService {

	Page<PartnerDisbursementData> getAllData(SearchSqlQuery searchVoucher, String souceType, String partnerType);

	List<PartnerDisbursementData> getPatnerData();

}

