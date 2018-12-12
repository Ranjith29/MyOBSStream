package org.obsplatform.organisation.mcodevalues.service;

import java.util.Collection;

import org.obsplatform.organisation.mcodevalues.data.MCodeData;

public interface MCodeReadPlatformService {

	public Collection<MCodeData> getCodeValue(final String codeName);
	
	public Collection<MCodeData> getCodeValue(final String codeName,final String orderPosition);

}
