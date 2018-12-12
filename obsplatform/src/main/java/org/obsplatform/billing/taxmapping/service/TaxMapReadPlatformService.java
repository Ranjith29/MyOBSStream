package org.obsplatform.billing.taxmapping.service;

import java.util.List;

import org.obsplatform.billing.chargecode.data.ChargeCodeData;
import org.obsplatform.billing.taxmapping.data.TaxMapData;

/**
 * @author hugo
 * 
 */
public interface TaxMapReadPlatformService {

	List<TaxMapData> retriveTaxMapData(String chargeCode);

	TaxMapData retrievedSingleTaxMapData(Long id);

	List<ChargeCodeData> retrivedChargeCodeTemplateData();

}
