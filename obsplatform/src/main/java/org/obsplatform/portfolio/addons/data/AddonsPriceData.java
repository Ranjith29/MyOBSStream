package org.obsplatform.portfolio.addons.data;

import java.math.BigDecimal;

public class AddonsPriceData {

	private Long id;
	private Long serviceId;
	private Long chargeCodeId;

	private String serviceCode;
	private String chargecodeDescription;
	private String chargeCode;

	private BigDecimal price;

	public AddonsPriceData(final Long id, final Long serviceId, final String serviceCode,final BigDecimal price, final Long chargeCodeId, 
			final String chargecodeDescription,final String chargeCode) {

		this.id = id;
		this.serviceCode = serviceCode;
		this.serviceId = serviceId;
		this.price = price;
		this.chargeCodeId = chargeCodeId;
		this.chargecodeDescription = chargecodeDescription;
		this.chargeCode = chargeCode;
	}

	public Long getId() {
		return id;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public Long getChargeCodeId() {
		return chargeCodeId;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public String getChargecodeDescription() {
		return chargecodeDescription;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public BigDecimal getPrice() {
		return price;
	}

}
