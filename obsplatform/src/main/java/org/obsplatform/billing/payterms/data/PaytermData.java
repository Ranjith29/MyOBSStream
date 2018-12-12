package org.obsplatform.billing.payterms.data;

import java.math.BigDecimal;

public class PaytermData {

	private Long id;
	private String paytermtype;
	private String duration;
	private String planType;
	private BigDecimal price;
	private BigDecimal secondaryPrice;
	private String chargeVariant;
	private Long activeOrders;
	private BigDecimal depositPrice;
	private Long contractPeriodId;

	public PaytermData(final Long id, final String paytermtype,final String duration, 
			final String planType, final BigDecimal price,final String chargeVariant, final Long contractPeriodId) {
		
		this.id = id;
		this.paytermtype = paytermtype;
		this.duration = duration;
		this.planType = planType;
		this.price = price;
		this.chargeVariant = chargeVariant;
		this.contractPeriodId = contractPeriodId;
	}
	
	public PaytermData(final BigDecimal price,final BigDecimal secondaryPrice, final Long activeOrders, final BigDecimal depositPrice){
		
		this.price = price;
		this.secondaryPrice = secondaryPrice;
		this.activeOrders = activeOrders;
		this.depositPrice = depositPrice;
	}

	public Long getId() {
		return id;
	}

	public String getPaytermtype() {
		return paytermtype;
	}

	public String getDuration() {
		return duration;
	}

	public String getPlanType() {
		return planType;
	}

	public BigDecimal getPrice() {
		return price;
	}
	
	public BigDecimal getSecondaryPrice() {
		return secondaryPrice;
	}

	public BigDecimal getDepositPrice() {
		return depositPrice;
	}

	public String getChargeVariant() {
		return chargeVariant;
	}

	public Long getActiveOrders() {
		return activeOrders;
	}

	public Long getContractPeriodId() {
		return contractPeriodId;
	}
	

}
