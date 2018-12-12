package org.obsplatform.portfolio.plan.data;

import java.math.BigDecimal;

public class ServiceData {

	private Long id;
	private String serviceCode;
	private String planDescription;
	private String planCode;
	private Long discountId;
	private BigDecimal price;
	private String chargeCode;
	private Long chargeVariant;
	private Long planId;
	private String serviceDescription;
	private String priceregion;
	private Long contractId;
	private String duration;
	private String billingFrequency;
	private String isPrepaid;
	private String serviceType;
	private String chargeDescription;
	private String image;
	private String variantCode;

	public ServiceData(final Long id, final String planCode,final  String serviceCode,final String planDescription,final  String chargeCode,
			final Long chargingVariant,final BigDecimal price,final String priceregion,final Long contractId,final String duration,
			final String billingFrequency, final Long discountId, String isPrepaid,final String variantCode) {

		this.id = id;
		this.serviceCode = serviceCode;
		this.planDescription = planDescription;
		this.planCode = planCode;
		this.chargeCode = chargeCode;
		this.chargeVariant = chargingVariant;
		this.price = price;
		this.priceregion=priceregion;
		this.contractId=contractId;
		this.duration=duration;
		this.billingFrequency=billingFrequency;
		this.discountId = discountId;
		this.isPrepaid=isPrepaid;
		this.variantCode=variantCode;
		
	}

	public ServiceData(final Long id,final Long planId,final String planCode,final String chargeCode,final  String serviceCode,
			final String serviceDescription,final String chargeDescription, final String priceRegion,final String serviceType,final String isPrepaid) {
		
		this.id = id;
		this.planId = planId;
		this.serviceCode = serviceCode;
		this.planCode = planCode;
		this.chargeCode = chargeCode;
		this.chargeDescription=chargeDescription;
		this.serviceDescription = serviceDescription;
		this.priceregion=priceRegion;
		this.serviceType=serviceType;
		this.isPrepaid=isPrepaid;
	

	}
	
	public ServiceData(final Long id,final  String serviceCode,final String serviceDescription,final String image) {
		
		this.id = id;
		this.serviceCode = serviceCode;
		this.serviceDescription = serviceDescription;
		this.image=image;

	}

	public ServiceData(Long id, String billingFrequency, Long contractId,
			BigDecimal price) {
		
		this.id = id;
		this.billingFrequency = billingFrequency;
		this.contractId = contractId;
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public String getServiceDescription() {
		return planDescription;
	}

	public Long getDiscountId() {
		return discountId;
	}

	public String getPlanCode() {
		return planCode;
	}

	public String getPriceregion() {
		return priceregion;
	}

	public Long getPlanId() {
		return planId;
	}

	public String getPlanDescription() {
		return planDescription;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public Long getChargeVariant() {
		return chargeVariant;
	}

	public Long getContractId() {
		return contractId;
	}

	public String getDuration() {
		return duration;
	}

	public String getBillingFrequency() {
		return billingFrequency;
	}

	public String getIsPrepaid() {
		return isPrepaid;
	}

	public String getChargeDescription() {
		return chargeDescription;
	}

	public String getServiceType() {
		return serviceType;
	}

	public String getImage() {
		return image;
	}

	public String getVariantCode() {
		return variantCode;
	}

}
