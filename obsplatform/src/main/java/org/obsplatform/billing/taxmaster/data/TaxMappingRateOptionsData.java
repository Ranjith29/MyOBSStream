package org.obsplatform.billing.taxmaster.data;

import java.util.Collection;

import org.obsplatform.billing.chargecode.data.ChargeCodeTemplateData;
import org.obsplatform.billing.discountmaster.data.DiscountValues;

public class TaxMappingRateOptionsData {
	private Collection<DiscountValues> datass;
	private Collection<TaxMasterData> taxMasterData;
	private Collection<ChargeCodeTemplateData> chargeCodeData;

	public TaxMappingRateOptionsData(Collection<ChargeCodeTemplateData> chargeCodeData,Collection<TaxMasterData> taxMasterData,Collection<DiscountValues> datass)
	{
		this.taxMasterData=taxMasterData;
		this.chargeCodeData=chargeCodeData;
		this.datass=datass;

	}
}
