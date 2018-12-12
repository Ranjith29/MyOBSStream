package org.obsplatform.organisation.partner.service;

import java.util.Collection;

import org.obsplatform.organisation.partner.data.PartnersData;

public interface PartnersReadPlatformService {

	Collection<PartnersData> retrieveAllPartners();

	PartnersData retrieveSinglePartnerDetails(Long partnerId);

	PartnersData retrievePartnerImage(Long userId);

	PartnersData retrieveSinglePartnerTypeName(Long partnerId);


}
