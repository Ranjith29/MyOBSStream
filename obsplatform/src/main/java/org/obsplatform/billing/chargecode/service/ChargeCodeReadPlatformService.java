package org.obsplatform.billing.chargecode.service;

import java.util.List;

import org.obsplatform.billing.chargecode.data.BillFrequencyCodeData;
import org.obsplatform.billing.chargecode.data.ChargeCodeData;
import org.obsplatform.billing.chargecode.data.ChargeTypeData;
import org.obsplatform.billing.chargecode.data.DurationTypeData;

/**
 * @author hugo
 * 
 */
public interface ChargeCodeReadPlatformService {

	List<ChargeCodeData> retrieveAllChargeCodes();

	List<ChargeTypeData> getChargeType();

	List<DurationTypeData> getDurationType();

	List<BillFrequencyCodeData> getBillFrequency();

	ChargeCodeData retrieveSingleChargeCodeDetails(Long chargeCodeId);

	ChargeCodeData retrieveChargeCodeForRecurring(Long planId);
}
