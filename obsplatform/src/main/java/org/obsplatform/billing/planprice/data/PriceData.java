package org.obsplatform.billing.planprice.data;

import java.math.BigDecimal;

public class PriceData {

	final private Long id;
	final private String chargeCode;
	final private String serviceCode;
	final private String chargeVariant;
	final private BigDecimal price;
	final private String chagreType;
	final private String chargeDuration;
	final private String durationType;
	final private Long serviceId;
	final private Long discountId;
	final private boolean taxInclusive;
	final private Long clientStateId;
	final private Long regionStateId;
	final private Long priceRegionCountry;
	final private Long clientCountry;
	
	public PriceData(final Long id,final String serviceCode,final String chargeCode,final String chargVariant,final BigDecimal price,
			final String chrgeType,final String chargeDuration,final String durationType,final Long serviceId, Long discountId, 
			boolean taxinclusive,Long stateId, Long countryId, Long regionState, Long regionCountryId)
	
	{

		this.id=id;
		this.chargeCode=chargeCode;
		this.serviceCode=serviceCode;
		this.chargeVariant=chargVariant;
		this.price=price;
		this.chagreType=chrgeType;
		this.chargeDuration=chargeDuration;
		this.durationType=durationType;
		this.serviceId=serviceId;
		this.discountId=discountId;
		this.taxInclusive=taxinclusive;
		this.clientStateId=stateId;
	    this.clientCountry=countryId;
	    this.regionStateId=regionState;
	    this.priceRegionCountry=regionCountryId;
	    
	}
	public Long getId() {
		return id;
	}
	public String getChargeCode() {
		return chargeCode;
	}
	public String getServiceCode() {
		return serviceCode;
	}
	public String getChargingVariant() {
		return chargeVariant;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public String getChagreType() {
		return chagreType;
	}
	public String getChargeDuration() {
		return chargeDuration;
	}
	public String getDurationType() {
		return durationType;
	}
	public Long getServiceId() {
		return serviceId;
	}
	
	
	public Long getClientStateId() {
		return clientStateId;
	}
	public Long getRegionStateId() {
		return regionStateId;
	}
	
	
	public Long getPriceRegionCountry() {
		return priceRegionCountry;
	}
	public Long getClientCountry() {
		return clientCountry;
	}
	public Long getDiscountId() {
		return discountId;
	}
	/**
	 * @return the taxInclusive
	 */
	public boolean isTaxInclusive() {
		return taxInclusive;
	}

	
}
