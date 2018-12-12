package org.obsplatform.organisation.address.data;



public class AddressLocationDetails {
	
  private final String countryCode;
  private final String countryName;
  private final String cityCode;
  private final String cityName;
  private final String stateCode;
  private final String stateName;
  private final Long cityId;
  private final Long countryId;
  private final Long stateId;
	
	public AddressLocationDetails(final String countryCode,final String countryName,final String cityCode,final String cityName, 
				final String stateCode,final String stateName,final Long countryId,final Long stateId,final Long cityId){
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.cityCode = cityCode;
		this.cityName = cityName;
		this.stateCode = stateCode;
		this.stateName = stateName;
		this.cityId=cityId;
		this.stateId=stateId;
		this.countryId=countryId;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getCountryName() {
		return countryName;
	}

	public String getCityCode() {
		return cityCode;
	}

	public String getCityName() {
		return cityName;
	}

	public String getStateCode() {
		return stateCode;
	}

	public String getStateName() {
		return stateName;
	}

	public Long getCityId() {
		return cityId;
	}

	public Long getCountryId() {
		return countryId;
	}

	public Long getStateId() {
		return stateId;
	}

	
	
}
