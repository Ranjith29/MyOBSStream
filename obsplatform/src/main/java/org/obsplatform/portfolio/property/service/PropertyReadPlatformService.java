package org.obsplatform.portfolio.property.service;

import java.util.List;

import org.obsplatform.billing.servicetransfer.data.ClientPropertyData;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.portfolio.property.data.PropertyDefinationData;
import org.obsplatform.portfolio.property.data.PropertyDeviceMappingData;

public interface PropertyReadPlatformService {

	Page<PropertyDefinationData> retrieveAllProperties(SearchSqlQuery searchPropertyDetails);

	List<PropertyDefinationData> retrieveAllPropertiesForSearch(String propertyCode);

	PropertyDefinationData retrievePropertyDetails(Long propertyId);

	Page<PropertyDefinationData> retrievePropertyHistory(SearchSqlQuery searchPropertyDetails);

	ClientPropertyData retrieveClientPropertyDetails(Long clientId,String propertyCode);

	List<PropertyDefinationData> retrieveAllProperties();

	Page<PropertyDefinationData> retrieveAllPropertyMasterData(SearchSqlQuery searchPropertyDetails);

	List<PropertyDefinationData> retrievPropertyType(String propertyType,String code,String paramLength);

	PropertyDefinationData retrieveSinglePropertyMaster(Long codeId);

	Boolean retrievePropertyMasterCount(String code, String propertyCodeType);
	
	List<PropertyDeviceMappingData> retrievePropertyDeviceMappingData(Long clienId);

	List<String> retrieveclientProperties(Long clientId);
	
	List<PropertyDeviceMappingData> retrievePropertyDeviceMappingData(String serialNumber);

	
}
