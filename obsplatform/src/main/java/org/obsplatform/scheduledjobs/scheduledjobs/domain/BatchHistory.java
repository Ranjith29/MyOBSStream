package org.obsplatform.scheduledjobs.scheduledjobs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_batch_history")
public class BatchHistory extends AbstractPersistable<Long>{


	@Column(name ="transaction_date")
    private Date transactionDate;
	
	@Column(name ="transaction_type")
    private String transactionType;
	
	@Column(name ="count_value")
    private String countValue;
	
	@Column(name ="batch_id")
    private String batchId;
	
	public BatchHistory(){
		
	}
	
	public BatchHistory(Date transactionDate, String transactionType, 
			String countValue, String batchId){
		this.transactionDate = transactionDate;
		this.transactionType = transactionType;
		this.countValue = countValue;
		this.batchId = batchId;
		
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

	public String getCountValue() {
		return countValue;
	}

	public void setCountValue(String countValue) {
		this.countValue = countValue;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
}
