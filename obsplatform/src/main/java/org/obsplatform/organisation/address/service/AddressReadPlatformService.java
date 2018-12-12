package org.obsplatform.organisation.address.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.organisation.address.data.AddressData;
import org.obsplatform.organisation.address.data.AddressLocationDetails;
import org.obsplatform.organisation.address.data.CityDetailsData;
import org.obsplatform.organisation.address.data.CountryDetails;

public interface AddressReadPlatformService {


	List<AddressData> retrieveSelectedAddressDetails(String selectedname);

	List<AddressData> retrieveAddressDetailsBy(Long clientId, String addressType);

	List<AddressData> retrieveAddressDetails();
	
	List<String> retrieveCountryDetails();

	List<String> retrieveStateDetails();

	List<String> retrieveCityDetails();

	List<AddressData> retrieveCityDetails(String selectedname);

	List<EnumOptionData> addressType();

	AddressData retrieveAdressBy(String cityName);

	List<CountryDetails> retrieveCountries();
	
	List<AddressData> retrieveClientAddressDetails(Long clientId);
	
	Page<AddressLocationDetails> retrieveAllAddressLocations(SearchSqlQuery searchAddresses);

	List<CityDetailsData> retrieveCitywithCodeDetails();

	List<CityDetailsData> retrieveAddressDetailsByCityName(String cityName);
	

}

