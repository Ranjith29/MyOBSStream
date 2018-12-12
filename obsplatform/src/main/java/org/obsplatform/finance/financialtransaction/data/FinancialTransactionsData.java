package org.obsplatform.finance.financialtransaction.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;

public class FinancialTransactionsData {

	private Long id;
	private Long chargeId;
	private Long orderId;
	private Long officeId;
	private Long transactionId;
	
	private Date transactionDate;
	private LocalDate transDate;
	private LocalDate transactionalDate;
	private LocalDate billDate;
	private LocalDate dueDate;
	private LocalDate chargeStartDate;
	private LocalDate chargeEndDate;
	
	private String userName;
	private String description;
	private String planCode;
	private String transactionType;
	private String transaction;
	private String chargeType;
	private String chargeDescription;
	private String transactionCategory;
	
	private BigDecimal amount;
	private BigDecimal chargeAmount;
	private BigDecimal taxAmount;
	private BigDecimal creditAmount;
	private BigDecimal debitAmount;
	private BigDecimal discountAmount;
	private BigDecimal netChargeAmount;
	
	private boolean flag;
	private boolean refundFlag;
	
	private List<FinancialTransactionsData> transactionsDatas;
	
	public FinancialTransactionsData(final Long transactionId,final Date transactionDate, final String transactionType,final BigDecimal amount) {
		
		this.transactionId = transactionId;
		this.transactionDate = transactionDate;
		this.transactionType = transactionType;
		this.amount = amount;
	}

	public FinancialTransactionsData(final Long officeId, final Long transactionId,final LocalDate transDate, final String transactionType, 
			final BigDecimal amount,final BigDecimal creditAmount, final BigDecimal debitAmount, final String userName,final String transactionCategory, 
			final boolean flag, final String planCode, final String description,final boolean refundFlag) {

		this.officeId = officeId;
		this.transactionId = transactionId;
		this.transDate = transDate;
		this.transactionType = transactionType;
		this.amount = amount;
		this.transaction = "INVOICE";
		this.creditAmount = creditAmount;
		this.debitAmount = debitAmount;
		this.userName = userName;
		this.transactionCategory = transactionCategory;
		this.flag = flag;
		this.planCode = planCode;
		this.description = description;
		this.refundFlag = refundFlag;
	}

	public FinancialTransactionsData(final Long transctionId, final String transactionType,final LocalDate transactionDate, final BigDecimal amount) {
		
		this.transactionId = transctionId;
		this.transactionalDate = transactionDate;
		this.transactionType = transactionType;
		this.amount = amount;
	}

	public FinancialTransactionsData(final Long id, final LocalDate billDate,final LocalDate dueDate, final BigDecimal amount) {
		
		this.id = id;
		this.billDate = billDate;
		this.dueDate = dueDate;
		this.amount = amount;
	}

	public FinancialTransactionsData(final Long chargeId, final String chargeType,final String chargeDescription, final BigDecimal chargeAmount,
			final BigDecimal taxAmount, final BigDecimal discountAmount,final BigDecimal netChargeAmount, final LocalDate chargeStartDate,
			final LocalDate chargeEndDate, final Long orderId) {
		
		this.chargeId = chargeId;
		this.chargeType = chargeType;
		this.chargeDescription = chargeDescription;
		this.chargeAmount = chargeAmount;
		this.taxAmount = taxAmount;
		this.discountAmount = discountAmount;
		this.netChargeAmount = netChargeAmount;
		this.chargeStartDate = chargeStartDate;
		this.chargeEndDate = chargeEndDate;
		this.orderId = orderId;
	}

	public FinancialTransactionsData(List<FinancialTransactionsData> transactionData) {
		this.transactionsDatas = transactionData;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDate getTransDate() {
		return transDate;
	}

	public LocalDate getTransactionalDate() {
		return transactionalDate;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getBillDate() {
		return billDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public String getTransaction() {
		return transaction;
	}

	public String getChargeType() {
		return chargeType;
	}
	
	public String getChargeDescription() {
		return chargeDescription;
	}

	public BigDecimal getChargeAmount() {
		return chargeAmount;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public BigDecimal getdiscountAmount() {
		return discountAmount;
	}
	
	public BigDecimal getNetChargeAmount() {
		return netChargeAmount;
	}

	public LocalDate getChargeStartDate() {
		return chargeStartDate;
	}

	public LocalDate getChargeEndDate() {
		return chargeEndDate;
	}

	public List<FinancialTransactionsData> getTransactionsDatas() {
		return transactionsDatas;
	}
	
	public BigDecimal getCreditAmount() {
		return creditAmount;
	}

	public BigDecimal getDebitAmount() {
		return debitAmount;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public void setOfficeId(Long officeId) {
		this.officeId = officeId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlanCode() {
		return planCode;
	}

	public void setPlanCode(String planCode) {
		this.planCode = planCode;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public boolean isRefundFlag() {
		return refundFlag;
	}

	public void setTransDate(LocalDate transDate) {
		this.transDate = transDate;
	}

	public void setCreditAmount(BigDecimal creditAmount) {
		this.creditAmount = creditAmount;
	}

	public void setDebitAmount(BigDecimal debitAmount) {
		this.debitAmount = debitAmount;
	}

	public String getTransactionCategory() {
		return transactionCategory;
	}

	public void setTransactionCategory(String transactionCategory) {
		this.transactionCategory = transactionCategory;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public Long getChargeId() {
		return chargeId;
	}

	public Long getOrderId() {
		return orderId;
	}

}
