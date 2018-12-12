package org.obsplatform.finance.paymentsgateway.recurring.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

/**
 * This History class is used to store the History of the Request for Recurring
 * Payments.
 * 
 * @author ashokreddy
 *
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "b_recurring_history")
public class RecurringBillingHistory extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "transaction_id")
	private String transactionId;

	@Column(name = "transaction_category")
	private String transactionCategory;

	@Column(name = "source")
	private String source;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "transaction_date")
	private Date transactionDate;

	@Column(name = "transaction_data")
	private String transactionData;

	@Column(name = "transaction_status")
	private String transactionStatus;

	@Column(name = "obs_status")
	private String obsStatus;

	@Column(name = "obs_description")
	private String obsDescription;

	public RecurringBillingHistory() {

	}

	public RecurringBillingHistory(final Long clientId, final Long orderId, final String transactionId,
			final String transactionCategory, final String source, final Date transactionDate,
			final String transactionData, final String transactionStatus, final String obsStatus,
			final String obsDescription) {

		this.clientId = clientId;
		this.orderId = orderId;
		this.transactionId = transactionId;
		this.transactionCategory = transactionCategory;
		this.source = source;
		this.transactionDate = transactionDate;
		this.transactionData = transactionData;
		this.transactionStatus = transactionStatus;
		this.obsStatus = obsStatus;
		this.obsDescription = obsDescription;
	}
	
	public static RecurringBillingHistory createHistory(final Long clientId, final String transactionCategory, final String source,
			final Date transactionDate, final String transactionStatus) {
		return new RecurringBillingHistory(clientId, null, null, transactionCategory, source, 
				transactionDate, null, transactionStatus, null, null);
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getTransactionCategory() {
		return transactionCategory;
	}

	public void setTransactionCategory(String transactionCategory) {
		this.transactionCategory = transactionCategory;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getTransactionData() {
		return transactionData;
	}

	public void setTransactionData(String transactionData) {
		this.transactionData = transactionData;
	}

	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public String getObsStatus() {
		return obsStatus;
	}

	public void setObsStatus(String obsStatus) {
		this.obsStatus = obsStatus;
	}

	public String getObsDescription() {
		return obsDescription;
	}

	public void setObsDescription(String obsDescription) {
		this.obsDescription = obsDescription;
	}
}
