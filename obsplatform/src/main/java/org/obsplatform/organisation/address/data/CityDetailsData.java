package org.obsplatform.organisation.address.data;

public class CityDetailsData {

	private String cityName;
	private String cityCode;
	private String state;
	private String country;

	public CityDetailsData(final String cityName, final String cityCode) {

		this.cityCode = cityCode;
		this.cityName = cityName;
	}

	public CityDetailsData(final String cityName, final String cityCode,
			final String state, final String country) {

		this.cityCode = cityCode;
		this.cityName = cityName;
		this.state = state;
		this.country = country;
	}

	public String getCityName() {
		return cityName;
	}

	public String getCityCode() {
		return cityCode;
	}

	public String getState() {
		return state;
	}

	public String getCountry() {
		return country;
	}

}
