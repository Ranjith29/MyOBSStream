package org.obsplatform.billing.promotioncodes.service;

import java.util.List;

import org.obsplatform.billing.promotioncodes.data.PromotionCodeData;

public interface PromotionCodeReadPlatformService {

	List<PromotionCodeData> retrieveAllPromotionCodes();

	PromotionCodeData retriveSinglePromotionCodeDetails(Long id);

}
