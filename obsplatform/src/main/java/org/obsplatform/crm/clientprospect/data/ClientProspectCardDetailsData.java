package org.obsplatform.crm.clientprospect.data;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;

/**
 * @author Naresh
 * 
 */
public class ClientProspectCardDetailsData {

	private Long id;
	private String cardNumber;
	private String cardType;
	private String cvvNumber;
	private String cardExpiryDate;

	public ClientProspectCardDetailsData() {

	}

	public ClientProspectCardDetailsData(final Long id, final String cardNumber, final String cardType, 
			final String cvvNumber, final String cardExpiryDate) {
		
		this.id = id;
		this.cardNumber = cardNumber;
		this.cardType = cardType;
		this.cvvNumber = cvvNumber;
		this.cardExpiryDate = cardExpiryDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getCardExpiryDate() {
		return cardExpiryDate;
	}

	public void setCardExpiryDate(String cardExpiryDate) {
		this.cardExpiryDate = cardExpiryDate;
	}
	
}
