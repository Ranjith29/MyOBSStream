package org.obsplatform.billing.invoice.data;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;

public class InvoiceData {

	private Long id;
	private BigDecimal amount;
	private BigDecimal dueAmount;
	private Date billDate;
	private Long billId;
	private Long clientId;
	private String customerName;
	private String email;
	private String planName;
	private LocalDate expiryDate;
	private Long orderId;

	public InvoiceData(final Long id, final BigDecimal amount, final BigDecimal dueAmount,final Date billDate, final Long billId) {

		this.id = id;
		this.amount = amount;
		this.dueAmount = dueAmount;
		this.billDate = billDate;
		this.billId = billId;
	}

	public InvoiceData(final Long clientId, final String customerName,final String email, 
			final Long orderId, final String planName,final LocalDate expiryDate) {

		this.clientId = clientId;
		this.customerName = customerName;
		this.email = email;
		this.orderId = orderId;
		this.planName = planName;
		this.expiryDate = expiryDate;

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getDiscountAmount() {
		return dueAmount;
	}

	public void setDiscountAmount(BigDecimal dueAmount) {
		this.dueAmount = dueAmount;
	}

	public Date getBillDate() {
		return billDate;
	}

	public void setBillDate(Date billDate) {
		this.billDate = billDate;
	}

	public Long getBillId() {
		return billId;
	}

	public void setBillId(Long billId) {
		this.billId = billId;
	}
	
	public BigDecimal getDueAmount() {
		return dueAmount;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public String getEmail() {
		return email;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getPlanName() {
		return planName;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}
	

}
