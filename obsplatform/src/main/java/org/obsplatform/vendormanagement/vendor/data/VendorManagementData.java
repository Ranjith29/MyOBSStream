/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.vendormanagement.vendor.data;

import java.util.Collection;
import java.util.List;

import org.obsplatform.organisation.address.data.CountryDetails;
import org.obsplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for application user data.
 */

public class VendorManagementData {
	
	private Long id;
    private String vendorCode;
    private String vendorName;
    private String vendorEmailId;
    private String contactName;
    private String vendormobileNo;
    
    private String vendorLandlineNo;
    private String vendorAddress;
    private String vendorCountryName;
    private String vendorCurrency;
    
    private Long vendorCountryId;
    private Long vendorCurrencyId;
    private List<CountryDetails> countryData;
	private Collection<CurrencyData> currencyOptions;
    
    
    public VendorManagementData(List<CountryDetails> countryData,
			Collection<CurrencyData> currencyOptions) {
		
    	this.countryData = countryData;
    	this.currencyOptions = currencyOptions;
	}


	public VendorManagementData(Long id, String vendorCode,
			String vendorName, String vendorEmailId, String contactName,
			String vendormobileNo, String vendorLandlineNo,
			String vendorAddress, String vendorCountryName,
			Long vendorCountryId, String vendorCurrency) {
		
		this.id = id;
		this.vendorCode = vendorCode;
		this.vendorName = vendorName;
		this.vendorEmailId = vendorEmailId;
		this.contactName = contactName;
		this.vendormobileNo = vendormobileNo;
		this.vendorLandlineNo = vendorLandlineNo;
		this.vendorAddress = vendorAddress;
		this.vendorCountryName = vendorCountryName;
		this.vendorCountryId = vendorCountryId;
		this.vendorCurrency = vendorCurrency;
		
	}

	public List<CountryDetails> getCountryData() {
		return countryData;
	}

	public Collection<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}

	public void setCountryData(List<CountryDetails> countryData) {
		this.countryData = countryData;
	}

	public void setCurrencyOptions(Collection<CurrencyData> currencyOptions) {
		this.currencyOptions = currencyOptions;
	}


	public String getVendorCode() {
		return vendorCode;
	}


	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}


	public String getVendorName() {
		return vendorName;
	}


	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}


	public String getVendorEmailId() {
		return vendorEmailId;
	}


	public void setVendorEmailId(String vendorEmailId) {
		this.vendorEmailId = vendorEmailId;
	}


	public String getContactName() {
		return contactName;
	}


	public void setContactName(String contactName) {
		this.contactName = contactName;
	}


	public String getVendormobileNo() {
		return vendormobileNo;
	}


	public void setVendormobileNo(String vendormobileNo) {
		this.vendormobileNo = vendormobileNo;
	}


	public String getVendorLandlineNo() {
		return vendorLandlineNo;
	}


	public void setVendorLandlineNo(String vendorLandlineNo) {
		this.vendorLandlineNo = vendorLandlineNo;
	}


	public String getVendorAddress() {
		return vendorAddress;
	}


	public void setVendorAddress(String vendorAddress) {
		this.vendorAddress = vendorAddress;
	}


	public String getVendorCountryName() {
		return vendorCountryName;
	}


	public void setVendorCountryName(String vendorCountryName) {
		this.vendorCountryName = vendorCountryName;
	}


	public String getVendorCurrency() {
		return vendorCurrency;
	}


	public void setVendorCurrency(String vendorCurrency) {
		this.vendorCurrency = vendorCurrency;
	}


	public Long getVendorCountryId() {
		return vendorCountryId;
	}


	public void setVendorCountryId(Long vendorCountryId) {
		this.vendorCountryId = vendorCountryId;
	}


	public Long getVendorCurrencyId() {
		return vendorCurrencyId;
	}


	public void setVendorCurrencyId(Long vendorCurrencyId) {
		this.vendorCurrencyId = vendorCurrencyId;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}
	
	
	
}

