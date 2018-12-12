package org.obsplatform.billing.currency.data;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.organisation.monetary.data.ApplicationCurrencyConfigurationData;

/**
 * @author hugo
 * 
 */
public class CountryCurrencyData {

	private Long id;
	private String country;
	private String currency;
	private String baseCurrency;
	private BigDecimal conversionRate;
	private String status;
	private String countryISD;
	private ApplicationCurrencyConfigurationData currencydata;
	private List<String> countryData;
	private List<EnumOptionData> statusData;

	public CountryCurrencyData(final Long id, final String country,
			final String currency, final String baseCurrency,
			final BigDecimal conversionRate, final String status, final String countryISD) {

		this.id = id;
		this.country = country;
		this.currency = currency;
		this.baseCurrency = baseCurrency;
		this.conversionRate = conversionRate;
		this.status = status;
		this.countryISD = countryISD;

	}

	public CountryCurrencyData(final CountryCurrencyData currencyData,
			final ApplicationCurrencyConfigurationData currency,
			final List<String> countryData,
			final List<EnumOptionData> statusData) {

		if (currencyData != null) {
			this.id = currencyData.getId();
			this.country = currencyData.getCountry();
			this.currency = currencyData.getCurrency();
			this.status = currencyData.getStatus();
			this.baseCurrency = currencyData.getBaseCurrency();
			this.conversionRate = currencyData.getConversionRate();
			this.countryISD = currencyData.getCountryISD();
		}

		this.currencydata = currency;
		this.countryData = countryData;
		this.statusData = statusData;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * @return the baseCurrency
	 */
	public String getBaseCurrency() {
		return baseCurrency;
	}

	/**
	 * @return the conversionRate
	 */
	public BigDecimal getConversionRate() {
		return conversionRate;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the currencydata
	 */
	public ApplicationCurrencyConfigurationData getCurrencydata() {
		return currencydata;
	}

	/**
	 * @return the countryData
	 */
	public List<String> getCountryData() {
		return countryData;
	}

	/**
	 * @return the statusData
	 */
	public List<EnumOptionData> getStatusData() {
		return statusData;
	}

	public String getCountryISD() {
		return countryISD;
	}
	
}
