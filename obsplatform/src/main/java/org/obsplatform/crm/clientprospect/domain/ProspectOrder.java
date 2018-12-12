package org.obsplatform.crm.clientprospect.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@SuppressWarnings("serial")
@Entity
@Table(name = "b_prospect_orders")
public class ProspectOrder extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "prospect_id")
	private Long prospectId;
	
	@Column(name = "plan_id")
	private Long planId;

	@Column(name = "contract_period")
	private Long contarctPeriod;
	
	@Column(name = "payterm_code")
	private String paytermCode;
	
	@Column(name = "no_of_connections")
	private Long noOfConnections;
	
	@Column(name = "price")
	private BigDecimal price;


	ProspectOrder() {
	}


	public ProspectOrder(Long prospectId, Long planCode, Long contractPeriod,
			String paytermCode, Long noOfConnections, BigDecimal price) {
		
		this.prospectId = prospectId;
		this.planId = planCode;
		this.contarctPeriod = contractPeriod;
		this.paytermCode = paytermCode;
		this.noOfConnections = noOfConnections;
		this.price = price;
	}
	
	public Map<String, Object> update(final JsonCommand command) {
		Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		final String planCode = "planCode";
		final String contractPeriod = "contractPeriod";
		final String paytermCode = "paytermCode";
		final String noOfConnections = "noOfConnections";
		final String totalPrice = "totalPrice";
		
		if (command.isChangeInLongParameterNamed(planCode, Long.valueOf(this.planId))) {	
			final Long newValue = command.longValueOfParameterNamed("planCode");
			actualChanges.put(planCode, newValue);
			this.planId = newValue;
		}
		
		if (command.isChangeInLongParameterNamed(contractPeriod, Long.valueOf(this.contarctPeriod))) {	
			final Long newValue = command.longValueOfParameterNamed("contractPeriod");
			actualChanges.put(contractPeriod, newValue);
			this.contarctPeriod = newValue;
		}
		
		if (command.isChangeInStringParameterNamed(paytermCode, this.paytermCode)) {
			final String newValue = command.stringValueOfParameterNamed("paytermCode");
			actualChanges.put(paytermCode, newValue);
			this.paytermCode = newValue;
		}
		
		if (command.isChangeInLongParameterNamed(noOfConnections, Long.valueOf(this.noOfConnections))) {	
			final Long newValue = command.longValueOfParameterNamed("noOfConnections");
			actualChanges.put(noOfConnections, newValue);
			this.noOfConnections = newValue;
		}
		
		if(command.isChangeInBigDecimalParameterNamed(totalPrice, this.price)){
			final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("totalPrice");
			actualChanges.put(totalPrice, newValue);
			this.price = newValue;
		}
		
		
		return actualChanges;
		
	}


	public Long getProspectId() {
		return prospectId;
	}


	public void setProspectId(Long prospectId) {
		this.prospectId = prospectId;
	}


	public Long getPlanId() {
		return planId;
	}


	public void setPlanId(Long planId) {
		this.planId = planId;
	}


	public Long getContarctPeriod() {
		return contarctPeriod;
	}


	public void setContarctPeriod(Long contarctPeriod) {
		this.contarctPeriod = contarctPeriod;
	}


	public String getPaytermCode() {
		return paytermCode;
	}


	public void setPaytermCode(String paytermCode) {
		this.paytermCode = paytermCode;
	}


	public Long getNoOfConnections() {
		return noOfConnections;
	}


	public void setNoOfConnections(Long noOfConnections) {
		this.noOfConnections = noOfConnections;
	}

	
	
	
}

