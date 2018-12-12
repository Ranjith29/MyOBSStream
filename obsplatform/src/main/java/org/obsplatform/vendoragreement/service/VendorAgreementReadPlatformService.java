/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.vendoragreement.service;

import java.util.List;

import org.obsplatform.portfolio.plan.data.PlanCodeData;
import org.obsplatform.portfolio.plan.data.PlanData;
import org.obsplatform.portfolio.plan.data.ServiceData;
import org.obsplatform.vendoragreement.data.VendorAgreementData;

public interface VendorAgreementReadPlatformService {

	List<VendorAgreementData> retrieveAllVendorAgreements();

	VendorAgreementData retrieveVendorAgreement(Long vendorAgreementId);

	List<VendorAgreementData> retrieveVendorAgreementDetails(Long vendorAgreementId);

	List<VendorAgreementData> retrieveRespectiveAgreementData(Long vendorId);


	List<PlanData> retrievePlans(Long agId);

	List<ServiceData> retrieveServices(Long agId);
	
	List<VendorAgreementData> retrievePlanDurationData(Long planId);

}