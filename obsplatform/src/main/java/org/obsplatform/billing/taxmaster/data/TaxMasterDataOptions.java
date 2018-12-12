package org.obsplatform.billing.taxmaster.data;

import java.util.Collection;

import org.obsplatform.infrastructure.core.data.EnumOptionData;

public class TaxMasterDataOptions {
	private Collection<EnumOptionData> enumOptionData;

	public TaxMasterDataOptions(Collection<EnumOptionData> enumOptionData)
	{
		this.enumOptionData=enumOptionData;
	}
}
