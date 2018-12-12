package org.obsplatform.organisation.partner.service;

import java.util.List;

import org.obsplatform.organisation.partner.data.AgreementData;

public interface PartnersAgreementReadPlatformService {

	AgreementData retrieveAgreementData(Long partnerId);

	Long checkAgreement(Long officeId);

	List<AgreementData> retrieveAgreementDetails(Long agreementId);

	List<AgreementData> retrievePartnerOfficeData(Long partnerId);


}
