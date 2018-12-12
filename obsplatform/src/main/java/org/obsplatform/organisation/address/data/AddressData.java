package org.obsplatform.organisation.address.data;

import java.util.List;

import org.obsplatform.infrastructure.core.data.EnumOptionData;

public class AddressData {
	
	private Long id;
	private Long clientId;
	private String addressKey;
	private String addressNo;
	private String street;
	private String zip;
	private String city;
	private String state;
	private String country;
	private List<AddressData> datas;
	private List<String> countryData,stateData,cityData;
	private String data;
	private List<EnumOptionData> addressOptionsData; 
	private List<AddressData> addressData;
	private String addressType;
	private Long addressTypeId;
	private String distributionBox;
	
	public AddressData(final Long addressId, final Long clientId, final String addressKeyId, final String addressNo,
			final String street, final String distributionBox, final String zip, final String city, final String state,
			final String country, final String addressKey, final Long addressTypeId) {

		this.id = addressId;
		this.addressKey = addressKeyId;
		this.addressTypeId = addressTypeId;
		this.clientId = clientId;
		this.addressNo = addressNo;
		this.street = street;
		this.distributionBox = distributionBox;
		this.zip = zip;
		this.city = city;
		this.state = state;
		this.country = country;
		this.addressType = addressKey;
	}

	public AddressData(final List<AddressData> addressdata, final List<String> countryData, final List<String> statesData,
			final List<String> citiesData, final List<EnumOptionData> enumOptionDatas) {

		this.datas = addressdata;
		this.countryData = countryData;
		this.stateData = statesData;
		this.cityData = citiesData;
		this.addressOptionsData = enumOptionDatas;
	}

	public AddressData(final Long id, final String data) {

		this.id = id;
		this.data = data;
	}

	public AddressData(final String city, final String state, final String country) {
		
		this.city = city;
		this.state = state;
		this.country = country;
	}

	public AddressData(final List<AddressData> data) {
		
		this.addressData = data;
	}

	public Long getAddressTypeId() {
		return addressTypeId;
	}

	public Long getAddressId() {
		return id;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getAddressKey() {
		return addressKey;
	}

	public String getAddressNo() {
		return addressNo;
	}

	public Long getId() {
		return id;
	}

	public List<AddressData> getDatas() {
		return datas;
	}

	public List<String> getCountryData() {
		return countryData;
	}

	public List<String> getStateData() {
		return stateData;
	}

	public List<String> getCityData() {
		return cityData;
	}

	public String getData() {
		return data;
	}

	public List<EnumOptionData> getAddressOptionsData() {
		return addressOptionsData;
	}

	public List<AddressData> getAddressData() {
		return addressData;
	}

	public String getAddressType() {
		return addressType;
	}

	public String getStreet() {
		return street;
	}

	public String getZip() {
		return zip;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getCountry() {
		return country;
	}

	public String getDistributionBox() {
		return distributionBox;
	}

	public void setDistributionBox(String distributionBox) {
		this.distributionBox = distributionBox;
	}

}
