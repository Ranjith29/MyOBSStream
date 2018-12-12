package org.obsplatform.finance.officebalance.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;


@Entity
@Table(name = "m_office_balance")
public class OfficeBalance extends AbstractAuditableCustom<AppUser,Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "office_id", nullable = false, length = 20)
	private Long officeId;

	@Column(name = "balance_amount", nullable = false, length = 20)
	private BigDecimal balanceAmount;

	public static OfficeBalance create(final Long officeId,final BigDecimal balanceAmount) {

		return new OfficeBalance(officeId, balanceAmount);
	}

	public OfficeBalance(Long officeId, BigDecimal balanceAmount) {

		this.officeId = officeId;
		this.balanceAmount = balanceAmount;

	}

	public OfficeBalance() {

	}

	public Long getofficeId() {
		return officeId;
	}

	public void setofficeId(Long officeId) {
		this.officeId = officeId;
	}

	public BigDecimal getBalanceAmount() {
		return balanceAmount;
	}

	public void setBalanceAmount(BigDecimal balanceAmount) {

		this.balanceAmount = balanceAmount;

	}

	public void updateBalance(String paymentType, BigDecimal amountPaid) {

		if ("CREDIT".equalsIgnoreCase(paymentType)) {

			this.balanceAmount = this.balanceAmount.subtract(amountPaid);
		} else {

			this.balanceAmount = this.balanceAmount.add(amountPaid);
		}

	}
}


