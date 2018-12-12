package org.obsplatform.finance.paymentsgateway.recurring.data;

import org.obsplatform.finance.paymentsgateway.recurring.domain.RecurringBilling;

public class RecurringData {

	private Long id;
	private Long clientId;
	private Long orderId;
	private String subscriberId;
	private String gatewayName;

	public RecurringData(final Long id, final Long clientId, final Long orderId, final String subscriberId,
			final String gatewayName) {

		this.id = id;
		this.clientId = clientId;
		this.orderId = orderId;
		this.subscriberId = subscriberId;
		this.gatewayName = gatewayName;
	}
	
	public RecurringData(final Long id, final String subscriberId, final Long orderId, final String pgName) {

		this.id = id;
		this.orderId = orderId;
		this.subscriberId = subscriberId;
		this.gatewayName = pgName;
	}

	public static RecurringData getRecurringData(final RecurringBilling billing) {
		return new RecurringData(billing.getId(), billing.getClientId(), billing.getOrderId(),
				billing.getSubscriberId(), billing.getGatewayName());
	}

	public Long getId() {
		return id;
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getSubscriberId() {
		return subscriberId;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

}
