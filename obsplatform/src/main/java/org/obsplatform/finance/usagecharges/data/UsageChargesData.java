package org.obsplatform.finance.usagecharges.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class UsageChargesData {

	private Long id;
	private Long clientId;
	private String number;
	private LocalDate chargeDate;
	private BigDecimal totalCost;
	private BigDecimal totalDuration;

	public UsageChargesData(Long clientId, String number) {

		this.clientId = clientId;
		this.number = number;

	}

	public UsageChargesData(Long id ,String number, LocalDate chargeDate, BigDecimal totalCost,BigDecimal totalDuration) {

		this.id = id;
		this.number = number;
		this.chargeDate = chargeDate;
		this.totalCost = totalCost;
		this.totalDuration = totalDuration;

	}

	public Long getId() {
		return id;
	}
	
	public Long getClientId() {
		return clientId;
	}

	public String getNumber() {
		return number;
	}
	
	public LocalDate getChargeDate() {
		return chargeDate;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public BigDecimal getTotalDuration() {
		return totalDuration;
	}

	
}
