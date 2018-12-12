package org.obsplatform.scheduledjobs.scheduledjobs.data;

import java.util.Date;

public class BatchHistoryData {

	private Long id;
	private Date transactionDate;
	private String transactionType;
	private String countValue;
	private String batchId;

	public BatchHistoryData(final Long id, final Date transactionDate,final String transactionType, final String countValue, final String batchId) {
		
		this.id = id;
		this.transactionDate = transactionDate;
		this.transactionType = transactionType;
		this.countValue = countValue;
		this.batchId = batchId;
	}

	public Long getId() {
		return id;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public String getCountValue() {
		return countValue;
	}

	public String getBatchId() {
		return batchId;
	}

}
