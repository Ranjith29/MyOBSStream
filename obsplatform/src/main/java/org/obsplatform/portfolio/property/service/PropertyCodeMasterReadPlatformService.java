package org.obsplatform.portfolio.property.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.portfolio.property.data.PropertyCodeMasterData;

public interface PropertyCodeMasterReadPlatformService {
	
	Page<PropertyCodeMasterData> retrieveAllPropertyCodeMasterData(SearchSqlQuery searchPropertyDetails);

	List<PropertyCodeMasterData> retrievPropertyType(String propertyType);

}
