package org.obsplatform.crm.clientprospect.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@SuppressWarnings("serial")
@Entity
@Table(name = "b_prospect_payments")
public class ProspectPayment extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "client_id")
	private Long clientId;
	
	@Column(name = "json")
	private String json;
	
	@Column(name = "error_data")
	private String errorData;
	
	@Column(name = "is_processed_pg")
	private char isProcessedPg = 'N';
	
	@Column(name = "is_processed_obs")
	private char isProcessedObs = 'N';
	
	ProspectPayment() {
	}

	public ProspectPayment(Long clientId, String json, char isProcessedPg, String errorData) {
		
		this.clientId = clientId;
		this.json = json;
		this.isProcessedPg = isProcessedPg;
		this.errorData = errorData;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public char getIsProcessedPg() {
		return isProcessedPg;
	}

	public void setIsProcessedPg(char isProcessedPg) {
		this.isProcessedPg = isProcessedPg;
	}

	public char getIsProcessedObs() {
		return isProcessedObs;
	}

	public void setIsProcessedObs(char isProcessedObs) {
		this.isProcessedObs = isProcessedObs;
	}
	
	

}


