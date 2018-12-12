package org.obsplatform.scheduledjobs.scheduledjobs.data;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;

public class WorldpayBatchData {

	private Long chargeId;
	private Long invoiceId;
	private Long orderId;
	private Long clientId;
	private BigDecimal amount;
	private Date inVoiceDate;
	private String w_token;
	private String r_type;
	private String type;
	private String  name;
	
	public WorldpayBatchData(final Long chargeId, final Long clientId, final BigDecimal amount, LocalDate inVoiceDate,
			String w_token,String r_type,String type,String name) {
		
		this.chargeId = chargeId;
		this.clientId = clientId;
		this.amount = amount;
		this.inVoiceDate = inVoiceDate.toDate();
		this.w_token=w_token;
		this.r_type=r_type;
		this.type=type;
		this.name=name;
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

	public String getW_token() {
		return w_token;
	}

	public String getR_type() {
		return r_type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
	

}
