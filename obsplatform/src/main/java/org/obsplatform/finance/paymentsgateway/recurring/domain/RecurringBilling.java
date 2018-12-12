package org.obsplatform.finance.paymentsgateway.recurring.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Store the data of Recurring profiles. map with OrderId and clientId.
 * 
 * @param gatewayName--
 *            is used to Identify the PaymentGateway for DML
 *            Operations(Update,Delete/Cancel,Get). If OBS Uses Multiple
 *            PaymentGateways for Recurring Billing. ( @link
 *            RecurringPaymentTransactionTypeConstants @see GATEWAY_NAME_PAYPAL)
 * 
 * 
 * @author ashokreddy
 *
 */

@Entity
@Table(name = "b_recurring")
public class RecurringBilling extends AbstractAuditableCustom<AppUser, Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5664089589174812105L;

	@Column(name = "client_id", nullable = false)
	private Long clientId;

	@Column(name = "subscriber_id", nullable = false)
	private String subscriberId;

	@Column(name = "order_id", nullable = true)
	private Long orderId;

	@Column(name = "gateway_name", nullable = false)
	private String gatewayName;

	@Column(name = "is_deleted", nullable = false)
	private char deleted = 'N';

	public RecurringBilling() {

	}

	public RecurringBilling(final Long clientId, final String subscriberId, final String gatewayName) {

		this.clientId = clientId;
		this.subscriberId = subscriberId;
		this.gatewayName = gatewayName;
	}

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
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

	public char getDeleted() {
		return deleted;
	}

	public void updateStatus() {
		this.deleted = 'Y';
	}

	public String getGatewayName() {
		return gatewayName;
	}

}