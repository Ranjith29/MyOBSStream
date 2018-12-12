package org.obsplatform.crm.clientprospect.data;

import java.math.BigDecimal;

public class ProspectPlanCodeData {

	private Long id;
	private String planDescription;
	private Long planId;
	private Long contractPeriod;
	private Long noOfConnections;
	private String payTermCode;
	private BigDecimal amount;
	private String isPrepaid;

	public ProspectPlanCodeData() {
	}

	public ProspectPlanCodeData(final Long id, final String planDescription, final String isPrepaid) {
		this.id = id;
		this.planDescription = planDescription;
		this .isPrepaid = isPrepaid;
	}

	public ProspectPlanCodeData(Long planId, Long contractPeriod,
			String payTermCode, Long noOfConnections, BigDecimal amount) {
		
		this.planId = planId;
		this.contractPeriod = contractPeriod;
		this.payTermCode = payTermCode;
		this.noOfConnections = noOfConnections;
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPlanDescription() {
		return planDescription;
	}

	public void setPlanDescription(String planDescription) {
		this.planDescription = planDescription;
	}

	public String getIsPrepaid() {
		return isPrepaid;
	}

	public void setIsPrepaid(String isPrepaid) {
		this.isPrepaid = isPrepaid;
	}
	
	
}
