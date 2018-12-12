package org.obsplatform.billing.currency.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.billing.currency.data.CountryCurrencyData;

public interface CountryCurrencyReadPlatformService {

	List<CountryCurrencyData> getCountryCurrencyDetailsByName(String string);

	Collection<CountryCurrencyData> retrieveAllCurrencyConfigurationDetails();

	CountryCurrencyData retrieveSingleCurrencyConfigurationDetails(Long id);

}
