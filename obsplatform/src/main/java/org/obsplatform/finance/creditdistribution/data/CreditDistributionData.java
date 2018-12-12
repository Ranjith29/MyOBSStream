package org.obsplatform.finance.creditdistribution.data;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.invoice.data.InvoiceData;
import org.obsplatform.finance.payments.data.PaymentData;

public class CreditDistributionData {
	
	private  List<InvoiceData> invoiceDatas;
	private  List<PaymentData> paymentDatas;
	private  List<PaymentData> depositDatas;
	private  Long id;
	private LocalDate distributionDate;
	private Long paymentId;
	private Long invoiceId;
	private BigDecimal amount;

	public CreditDistributionData(List<InvoiceData> invoiceDatas,List<PaymentData> paymentDatas,List<PaymentData> depositDatas) {
		
		this.invoiceDatas=invoiceDatas;
		this.paymentDatas=paymentDatas;
		this.depositDatas=depositDatas;
	}

	public CreditDistributionData(Long id, LocalDate distributionDate,Long paymentId, Long invoiceId, BigDecimal amount) {
         
		this.id=id;
		this.distributionDate=distributionDate;
		this.paymentId=paymentId;
		this.invoiceId=invoiceId;
		this.amount=amount;
	
	}

	public List<InvoiceData> getInvoiceDatas() {
		return invoiceDatas;
	}

	public List<PaymentData> getPaymentDatas() {
		return paymentDatas;
	}

	public List<PaymentData> getDepositDatas() {
		return depositDatas;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getDistributionDate() {
		return distributionDate;
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

	
	
}
