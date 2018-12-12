package org.obsplatform.scheduledjobs.scheduledjobs.data;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;

public class EvoBatchData {

	private Long chargeId;
	private Long invoiceId;
	private Long orderId;
	private Long clientId;
	private BigDecimal amount;
	private Date inVoiceDate;
	
	public EvoBatchData(final Long chargeId, final Long clientId, final BigDecimal amount, LocalDate inVoiceDate) {
		
		this.chargeId = chargeId;
		this.clientId = clientId;
		this.amount = amount;
		this.inVoiceDate = inVoiceDate.toDate();
	}

	public Long getChargeId() {
		return chargeId;
	}

	public Long getInvoiceId() {
		return invoiceId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public Long getClientId() {
		return clientId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Date getInVoiceDate() {
		return inVoiceDate;
	}

}
