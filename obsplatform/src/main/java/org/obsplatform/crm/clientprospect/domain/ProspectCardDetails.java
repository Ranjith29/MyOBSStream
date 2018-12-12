package org.obsplatform.crm.clientprospect.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@SuppressWarnings("serial")
@Entity
@Table(name = "b_prospect_card_details")
public class ProspectCardDetails extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "prospect_id")
	private Long prospectId;

	@Column(name = "prospect_orders_id")
	private Long prospectOrdersId;

	@Column(name = "name")
	private String name;

	@Column(name = "card_number")
	private String cardNumber;

	@Column(name = "card_type")
	private String cardType;
	
	@Column(name = "cvv_number")
	private String cvvNumber;
	
	@Column(name = "card_expiry_date")
	private String cardExpiryDate;
	
	@Column(name = "type")
	private String type;

	ProspectCardDetails() {
	}

	public ProspectCardDetails(final Long prospectId, final Long prospectOrdersId, final String cardNumber, 
			 final String cardType, final String cvvNumber,
			final String cardExpiryDate, final String name, final String type) {
		
		this.prospectId = prospectId;
		this.prospectOrdersId = prospectOrdersId;
		this.name = name;
		this.cardNumber = cardNumber;
		this.cardType = cardType;
		this.cvvNumber = cvvNumber;
		this.cardExpiryDate = cardExpiryDate;
		this.type = type;
	}
	
	public Map<String, Object> update(final JsonCommand command) {
		Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		final String cardNumberParamName="cardNumber";
		if (command.isChangeInStringParameterNamed(cardNumberParamName,this.cardNumber)){
			final String newValue = command.stringValueOfParameterNamed(cardNumberParamName);
			actualChanges.put(cardNumberParamName, newValue);
			this.cardNumber = StringUtils.defaultIfEmpty(newValue, null);
		}
		final String cardTypeParamName="cardType";
		if (command.isChangeInStringParameterNamed(cardTypeParamName,this.cardType)){
			final String newValue = command.stringValueOfParameterNamed(cardTypeParamName);
			actualChanges.put(cardTypeParamName, newValue);
			this.cardType = StringUtils.defaultIfEmpty(newValue, null);
		}
		final String cvvNumberParamName="cvvNumber";
		if (command.isChangeInStringParameterNamed(cvvNumberParamName,this.cvvNumber)){
			final String newValue = command.stringValueOfParameterNamed(cvvNumberParamName);
			actualChanges.put(cvvNumberParamName, newValue);
			this.cvvNumber = StringUtils.defaultIfEmpty(newValue, null);
		}
		final String expiryDateParamName="cardExpiryDate";
		if (command.isChangeInStringParameterNamed(expiryDateParamName,this.cardExpiryDate)){
			final String expiryDate = command.stringValueOfParameterNamed(expiryDateParamName);
			final String[] expiryDateParts = expiryDate.split("/");
			final String newValue = expiryDateParts[1] + expiryDateParts[0];
			actualChanges.put(expiryDateParamName, newValue);
			this.cardExpiryDate = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		return actualChanges;
	}

	
	public Long getProspectId() {
		return prospectId;
	}

	public void setProspectId(Long prospectId) {
		this.prospectId = prospectId;
	}

	public Long getProspectOrdersId() {
		return prospectOrdersId;
	}

	public void setProspectOrdersId(Long prospectOrdersId) {
		this.prospectOrdersId = prospectOrdersId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getCvvNumber() {
		return cvvNumber;
	}

	public void setCvvNumber(String cvvNumber) {
		this.cvvNumber = cvvNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCardExpiryDate() {
		return cardExpiryDate;
	}

	public void setCardExpiryDate(String cardExpiryDate) {
		this.cardExpiryDate = cardExpiryDate;
	}

}
