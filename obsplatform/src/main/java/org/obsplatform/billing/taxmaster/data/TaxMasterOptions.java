package org.obsplatform.billing.taxmaster.data;

import java.util.ArrayList;
import java.util.List;

import org.obsplatform.infrastructure.core.data.EnumOptionData;

public class TaxMasterOptions {
	private List<EnumOptionData> enumOptionData;

	public TaxMasterOptions() {
		enumOptionData = new ArrayList<EnumOptionData>();
		enumOptionData
				.add(new EnumOptionData(new Long(1), "Tax", "notrequired"));
		enumOptionData.add(new EnumOptionData(new Long(2), "Tax on Tax",
				"notrequired"));
	}
}
