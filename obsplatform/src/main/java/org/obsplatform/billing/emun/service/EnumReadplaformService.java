package org.obsplatform.billing.emun.service;

import java.util.Collection;

import org.obsplatform.billing.emun.data.EnumValuesData;

public interface EnumReadplaformService {

	public Collection<EnumValuesData> getEnumValues(final String codeName);

}
