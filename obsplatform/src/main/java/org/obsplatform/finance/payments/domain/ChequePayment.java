package org.obsplatform.finance.payments.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@SuppressWarnings("serial")
@Entity
@Table(name = "b_cheque")
public class ChequePayment extends AbstractAuditableCustom<AppUser,Long> {

	@Column(name = "payment_id", nullable = false)
	private Long paymentId;

	@Column(name = "cheque_no",nullable = false)
	private String chequeNo;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "cheque_date", nullable = false)
	private Date chequeDate;

	@Column(name = "bank_name", nullable = false)
	private String bankName;

	@Column(name = "branch_name", nullable=false)
	private String branchName;

	 
	
	public ChequePayment() {
		// TODO Auto-generated constructor stub
	}
	
	public ChequePayment(final Long paymentId, final String chequeNo, final Date chequeDate, final String bankName, final String branchName){
		this.paymentId = paymentId;
		this.chequeNo = chequeNo;
		this.chequeDate = chequeDate;
		this.bankName = bankName;
		this.branchName = branchName;
	}
	
	
	
	public static ChequePayment fromJson(final JsonCommand command) {
		final Long paymentId = command.longValueOfParameterNamed("paymentId");
		final String chequeNo = command.stringValueOfParameterNamed("chequeNo");
		final Date chequeDate = command.DateValueOfParameterNamed("chequeDate");
		final String bankName = command.stringValueOfParameterNamed("bankName");
		final String branchName = command.stringValueOfParameterNamed("branchName");
		return new ChequePayment(paymentId,chequeNo, chequeDate, bankName, branchName);
	}

	public Long getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(final Long paymentId) {
		this.paymentId = paymentId;
	}

	public String getChequeNo() {
		return chequeNo;
	}

	public void setChequeNo(final String chequeNo) {
		this.chequeNo = chequeNo;
	}

	public Date getChequeDate() {
		return chequeDate;
	}

	public void setChequeDate(final Date chequeDate) {
		this.chequeDate = chequeDate;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(final String bankName) {
		this.bankName = bankName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(final String branchName) {
		this.branchName = branchName;
	}

	
}
	