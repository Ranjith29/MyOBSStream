package org.obsplatform.finance.billingorder.data;

import java.math.BigDecimal;
import java.util.Date;

import org.obsplatform.finance.billingorder.domain.Invoice;

public class GenerateInvoiceData {
	
	private Long clientId;
	private Date nextBillableDay;
	private BigDecimal invoiceAmount;
	private Invoice invoice;
	private BigDecimal chargeAmount;
	private BigDecimal discountAmount;
	
	public GenerateInvoiceData( final Long clientId, final Date nextBillableDay,BigDecimal invoiceAmount,Invoice invoice) {
		this.clientId = clientId;
		this.nextBillableDay = nextBillableDay;
		this.invoiceAmount=invoiceAmount;
		this.invoice = invoice;
	}

	
	public GenerateInvoiceData(BigDecimal chargeAmount,BigDecimal discountAmount) {
		
		this.chargeAmount = chargeAmount;
		this.discountAmount = discountAmount;
	}

	public Long getClientId() {
		return clientId;
	}

	public Date getNextBillableDay() {
		return nextBillableDay;
	}

	public BigDecimal getInvoiceAmount() {
		return invoiceAmount;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public BigDecimal getChargeAmount() {
		return chargeAmount;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}
	
}
