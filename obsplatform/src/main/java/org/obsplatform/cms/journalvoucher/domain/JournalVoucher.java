package org.obsplatform.cms.journalvoucher.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@Entity
@Table(name="b_jv_transactions")
public class JournalVoucher extends AbstractAuditableCustom<AppUser,Long>{
	
	@Column(name = "ref_id")
	private Long referenceId;
	
	@Column(name = "client_id")
	private Long clientId;
	
	@Column(name = "jv_date")
	private Date transactionDate;

	@Column(name = "jv_description")
	private String description;

	@Column(name = "credit_amount")
	private Double creditAmount;

	@Column(name = "debit_amount")
	private Double debitAmount;
	
	public JournalVoucher(){
		
	}

	public JournalVoucher(Long refId, Date transactionDate, String description, Double creditAmount, Double debitAmount, Long clientId) {
		
		this.referenceId=refId;
		this.transactionDate=transactionDate;
		this.description=description;
		this.creditAmount=creditAmount;
		this.debitAmount=debitAmount;
		this.clientId=clientId;
	}

	
}
