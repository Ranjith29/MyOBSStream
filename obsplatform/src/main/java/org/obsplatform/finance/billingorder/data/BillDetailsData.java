package org.obsplatform.finance.billingorder.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class BillDetailsData {

	private Long id;
	private Long clientId;
	private String billPeriod;
	private BigDecimal previousBalance;
	private BigDecimal chargeAmount;
	private BigDecimal adjustmentAmount;
	private BigDecimal taxAmount;
	private BigDecimal paidAmount;
	private BigDecimal dueAmount;
	private LocalDate billDate;
	private LocalDate dueDate;
	private String promotionalMessage;
	private String billNo;
	private String date;
	private String transaction;
	private BigDecimal amount;
	private String payments;
	private String isPaid;
	private BigDecimal invoiceAmount;

	public BillDetailsData(final Long id, final Long clientId,final LocalDate dueDate, final String transactionType,
			final BigDecimal dueAmount, final BigDecimal amount, final LocalDate transDate) {

		this.id = id;
		this.dueDate = dueDate;
		this.transaction = transactionType;
		this.dueAmount = dueAmount;
		this.amount = amount;
		this.clientId = clientId;

	}

	public BillDetailsData(final Long id, final LocalDate billDate, final LocalDate dueDate,final BigDecimal amount, 
			final String isPaid, final BigDecimal invoiceAmount,final BigDecimal adjustmentAmount) {

		this.id = id;
		this.billDate = billDate;
		this.dueDate = dueDate;
		this.amount = amount;
		this.isPaid = isPaid;
		this.invoiceAmount=invoiceAmount;
		this.adjustmentAmount = adjustmentAmount;
	}

	public Long getId() {
		return id;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getBillPeriod() {
		return billPeriod;
	}

	public BigDecimal getPreviousBalance() {
		return previousBalance;
	}

	public String getPromotionalMessage() {
		return promotionalMessage;
	}

	public String getBillNo() {
		return billNo;
	}

	public String getDate() {
		return date;
	}

	public String getTransaction() {
		return transaction;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getPayments() {
		return payments;
	}

	public BigDecimal getChargeAmount() {
		return chargeAmount;
	}

	public BigDecimal getAdjustmentAmount() {
		return adjustmentAmount;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	public BigDecimal getDueAmount() {
		return dueAmount;
	}

	public LocalDate getBillDate() {
		return billDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public String getMessage() {
		return promotionalMessage;
	}

	public String getIsPaid() {
		return isPaid;
	}

	public BigDecimal getInvoiceAmount() {
		return invoiceAmount;
	}
}

