package org.obsplatform.billing.chargevariant.service;

import java.util.List;

import org.obsplatform.billing.chargevariant.data.ChargeVariantData;
import org.obsplatform.billing.chargevariant.data.ChargeVariantDetailsData;

public interface ChargeVariantReadPlatformService {

	
	ChargeVariantData retrieveChargeVariantData(Long variantId);

	List<ChargeVariantData> retrieveAllChargeVariantData();

	List<ChargeVariantDetailsData> retrieveAllChargeVariantDetails(Long variantId);

}
