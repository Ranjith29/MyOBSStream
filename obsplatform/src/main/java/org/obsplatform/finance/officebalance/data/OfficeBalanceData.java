package org.obsplatform.finance.officebalance.data;

import java.math.BigDecimal;

public class OfficeBalanceData {

	private Long id;
	private Long officeId;
	private BigDecimal balanceAmount;

	public OfficeBalanceData(Long id, Long officeId, BigDecimal balanceAmount) {

		this.id = id;
		this.officeId = officeId;
		this.balanceAmount = balanceAmount;

	}

	public Long getId() {
		return id;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public BigDecimal getBalanceAmount() {
		return balanceAmount;
	}

}
