package org.obsplatform.billing.planprice.service;

import java.util.List;

import org.obsplatform.billing.planprice.data.PricingData;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.organisation.voucher.data.VoucherData;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.plan.data.ServiceData;

public interface PriceReadPlatformService {

	// List<ChargesData> retrieveChargeCode();

	List<EnumOptionData> retrieveChargeVariantData();

	// List<DiscountMasterData> retrieveDiscountDetails();

	List<SubscriptionData> retrievePaytermData();

	List<ServiceData> retrieveServiceDetails(Long planId);

	List<ServiceData> retrieveServiceCodeDetails(Long planCode);

	PricingData retrieveSinglePriceDetails(String priceId);

	List<PricingData> retrievePlanAndPriceDetails(String region);

	List<ServiceData> retrievePriceDetails(Long planId, String region);
	
	List<VoucherData> retrieveVoucherDatas(Long priceId);
	
	Long retrieveEventsPrice(Long planId);

}
