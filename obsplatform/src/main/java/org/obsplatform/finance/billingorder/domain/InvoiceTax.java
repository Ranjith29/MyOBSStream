package org.obsplatform.finance.billingorder.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_charge_tax")
public class InvoiceTax extends AbstractPersistable<Long>{

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "charge_id", insertable = true, updatable = true, nullable = true, unique = true)
	private BillingOrder charge;

	@ManyToOne
	@JoinColumn(name = "invoice_id", insertable = true, updatable = true, nullable = true, unique = true)
	private Invoice invoice;

	@Column(name = "tax_code")
	private String taxCode;

	@Column(name = "tax_value")
	private Integer taxValue;

	@Column(name = "tax_percentage")
	private BigDecimal taxPercentage;

	@Column(name = "tax_amount")
	private BigDecimal taxAmount;
	
	@Column(name = "bill_id")
	private Long billId;
	
   public InvoiceTax() {
		
	  }

	public InvoiceTax(final Invoice invoice, final BillingOrder charge,
			final String taxCode, final Integer taxValue,
			final BigDecimal taxPercentage, final BigDecimal taxAmount) {

		this.charge = charge;
		this.invoice = invoice;
		this.taxCode = taxCode;
		this.taxValue = taxValue;
		this.taxPercentage = taxPercentage;
		this.taxAmount = taxAmount;
	}

	public BillingOrder getCharge() {
		return charge;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public Long getBillId() {
		return billId;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public Integer getTaxValue() {
		return taxValue;
	}

	public void setTaxValue(Integer taxValue) {
		this.taxValue = taxValue;
	}

	public BigDecimal getTaxPercentage() {
		return taxPercentage;
	}

	public void setTaxPercentage(BigDecimal taxPercentage) {
		this.taxPercentage = taxPercentage;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	public void updateBillId(Long billId) {
		this.billId=billId;
	}
	
	public void update(BillingOrder charge) {
		this.charge = charge;

	}

}
