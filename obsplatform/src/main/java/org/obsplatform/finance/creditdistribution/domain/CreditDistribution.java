package org.obsplatform.finance.creditdistribution.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.obsplatform.finance.payments.domain.Payment;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.useradministration.domain.AppUser;

import com.google.gson.JsonElement;

@SuppressWarnings("serial")
@Entity
@Table(name = "b_credit_distribution")
public class CreditDistribution extends AbstractAuditableCustom<AppUser, Long>{

	
	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "payment_id")
	private Long paymentId;

	@Column(name = "invoice_id")
	private Long invoiceId;
	
	@Column(name = "amount")
	private BigDecimal amount;

	@Column(name = "distribution_date")
	private Date distributionDate;
	
	@Column(name = "is_deleted", nullable = false)
	private boolean deleted = false;
	
	@Column(name = "cancel_remark")
	private String cancelRemark;
	
	@Column(name = "ref_id", nullable = true)
	private Long refernceId;

	
	public CreditDistribution() {
		// TODO Auto-generated constructor stub
	}


	public CreditDistribution(Long paymentId, Long clientId, Long invoiceId,BigDecimal amount, Date distributionDate) {
		
		this.paymentId=paymentId;
		this.clientId=clientId;
		this.invoiceId=invoiceId;
		this.distributionDate=distributionDate;
		this.amount=amount;
		
	}
	
	public CreditDistribution(Long paymentId, Long clientId, Long invoiceId,BigDecimal amount, LocalDate distributionDate,Long refernceId ) {
		
		this.paymentId=paymentId;
		this.clientId=clientId;
		this.invoiceId=invoiceId;
		this.distributionDate=distributionDate.toDate();
		this.amount=amount.negate();
		this.refernceId = refernceId;
		
	}


	public static CreditDistribution fromJson(JsonElement j,FromJsonHelper fromJsonHelper) {
	
	final Long paymentId = fromJsonHelper.extractLongNamed("paymentId",j);
	final Long clientId = fromJsonHelper.extractLongNamed("clientId", j);
	final Long invoiceId = fromJsonHelper.extractLongNamed("invoiceId", j);
	final BigDecimal amount = fromJsonHelper.extractBigDecimalWithLocaleNamed("amount", j);
	final Date distributionDate = DateUtils.getDateOfTenant();
	return new CreditDistribution(paymentId,clientId,invoiceId,amount,distributionDate);
	
	}


	public Long getClientId() {
		return clientId;
	}


	public Long getPaymentId() {
		return paymentId;
	}


	public Long getInvoiceId() {
		return invoiceId;
	}


	public BigDecimal getAmount() {
		return amount;
	}


	public Date getDistributionDate() {
		return distributionDate;
	}


	public static CreditDistribution cancelCreditDistributionRequest(CreditDistribution creditDistribution) {
		return new CreditDistribution(creditDistribution.getPaymentId(),creditDistribution.getClientId(),creditDistribution.getInvoiceId(),
				creditDistribution.getAmount(),DateUtils.getLocalDateOfTenant(),creditDistribution.getId());
	}
	
	public static Payment cancelPaymentRequest(final Payment payment) {
		return new Payment(payment.getClientId(), null, null, payment.getAmountPaid(), null, DateUtils.getLocalDateOfTenant(),payment.getRemarks(), 
				   payment.getPaymodeId(),null,payment.getReceiptNo(),null,payment.isWalletPayment(),payment.getIsSubscriptionPayment(), payment.getId());   
	}


	public void cancelDistribution(final JsonCommand command) {
		final String cancelRemarks = command.stringValueOfParameterNamed("cancelRemark");
		this.cancelRemark = cancelRemarks;
		this.deleted = true;
	}

	
}