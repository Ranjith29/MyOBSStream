package org.obsplatform.organisation.priceregion.service;

import java.util.List;

import org.obsplatform.organisation.priceregion.data.PriceRegionData;

public interface RegionalPriceReadplatformService {

	List<PriceRegionData> getPriceRegionsDetails();

	PriceRegionData getTheClientRegionDetails(Long clientId);


}
