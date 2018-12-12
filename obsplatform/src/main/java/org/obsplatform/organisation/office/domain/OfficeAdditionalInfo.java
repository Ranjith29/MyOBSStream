package org.obsplatform.organisation.office.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_office_additional_info")
public class OfficeAdditionalInfo extends AbstractPersistable<Long> {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	@Column(name = "credit_limit")
	private BigDecimal creditLimit;
	
	@Column(name = "partner_currency")
	private String partnerCurrency;
	
	@Column(name = "contact_name")
	private String contactName;
	
	@Column(name = "is_collective", nullable = false, length = 100)
	private char isCollective;

	@OneToOne
	@JoinColumn(name = "office_id", insertable = true, updatable = true, nullable = true, unique = true)
	private Office office;

	public OfficeAdditionalInfo(){
		
	}
	
	public OfficeAdditionalInfo(final Office office, final BigDecimal creditLimit,final String currency,
			   final boolean isCollective,final String contactName) {
		
		this.office = office;
		this.creditLimit = creditLimit;
		this.partnerCurrency = currency;
		this.isCollective = isCollective?'Y':'N';
		this.contactName = contactName;
	}

	public BigDecimal getCreditLimit() {
		return creditLimit;
	}

	public String getPartnerCurrency() {
		return partnerCurrency;
	}

	public Office getOffice() {
		return office;
	}
	

	public String getContactName() {
		return contactName;
	}

	public void setOffice(Office office) {
		this.office = office;
	}


	public boolean getIsCollective() {
		boolean collective = false;
		
		if(this.isCollective == 'Y'){
			collective = true;		
		    return collective;
		
		}else{
			return collective;
		}
		
	}

	public Map<String, Object> update(final JsonCommand command) {
		final Map<String, Object> actualChanges = new ConcurrentHashMap<String, Object>(1);
		final String creditlimitParamName = "creditlimit";
		if (command.isChangeInBigDecimalParameterNamed(creditlimitParamName,this.creditLimit)) {
			final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(creditlimitParamName);
			actualChanges.put(creditlimitParamName, newValue);
			this.creditLimit = newValue;
		}

		final String currencyParamName = "currency";
		if (command.isChangeInStringParameterNamed(currencyParamName,this.partnerCurrency)) {
			final String newValue = command.stringValueOfParameterNamed(currencyParamName);
			actualChanges.put(currencyParamName, newValue);
			this.partnerCurrency = StringUtils.defaultIfEmpty(newValue,null);
		}
		
		final String contactNameParamName = "contactName";
		if (command.isChangeInStringParameterNamed(contactNameParamName,this.contactName)) {
			final String newValue = command.stringValueOfParameterNamed(contactNameParamName);
			actualChanges.put(contactNameParamName, newValue);
			this.contactName = StringUtils.defaultIfEmpty(newValue,null);
		}
		
		

		final char isCollectiveParamName = command.booleanPrimitiveValueOfParameterNamed("isCollective")?'Y':'N';
		
		if(this.isCollective != isCollectiveParamName){
			//actualChanges.put(isCollectiveParamName, isCollectiveParamName);
			this.isCollective = isCollectiveParamName;
		}
		

		return actualChanges;

	}

		
}
