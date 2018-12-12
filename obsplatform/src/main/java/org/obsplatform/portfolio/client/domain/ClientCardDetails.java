package org.obsplatform.portfolio.client.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@SuppressWarnings("serial")
@Entity
@Table(name = "m_client_card_details")
public class ClientCardDetails extends AbstractAuditableCustom<AppUser, Long> {

	 private final static String CREDIT_CARD = "CreditCard";
	 private final static String ACH_CARD = "ACH";
	 private final static String PSEUDO_CARD = "PseudoCard";
	 private final static String OBFUSCATED_CARD = "ObfuscatedCard";
	 
	 @ManyToOne
	 @JoinColumn(name = "client_id", nullable = false)
	 private Client client;

	/* @Column(name = "unique_identifier", nullable = true)
	 private String uniqueIdentifier;*/
	 
	 @Column(name = "name", nullable = false)
	 private String cardName;
	 
	 @Column(name = "card_number", nullable = false)
	 private String cardNumber;
	 
	 @Column(name = "aba_routing_number", nullable = false)
	 private String routingNumber;
	 
	 @Column(name = "bank_account_number", nullable = true)
	 private String bankAccountNumber;
	 
	 @Column(name = "bank_name", nullable = true)
	 private String bankName;
	 
	 @Column(name = "account_type", nullable = true)
	 private String accountType;

	 @Column(name = "card_expiry_date", nullable = true)
	 private String expiryDate;
	 
	 @Column(name = "type", nullable = false)
	 private String type;
	 
	 @Column(name = "card_type", nullable = false)
	 private String cardType;
	 
	 @Column(name = "cvv_number", nullable = true)
	 private String cvvNumber;
	 
	 @Column(name = "rtf_type", nullable = true)
	 private String rtftype;
	 
	 @Column(name = "is_deleted", nullable = false)
	 private char isDeleted;
	 
	 @Column(name = "w_token", nullable = true)
	 private String w_token;
	 
	 @Column(name = "r_type", nullable = true)
	 private String r_type;
	 

	 public ClientCardDetails(){
		 
	 }
	
	
	public ClientCardDetails(final String cardName, final String cardNumber,
			final String cardExpiryDate, final String cardType, final String type, 
			final Long data, final String cvvNumber,final String rtftype) {
		
		this.cardName=cardName;
		this.cardNumber=cardNumber;
		this.expiryDate=cardExpiryDate;
		this.cardType=cardType;
		this.type=type;
		this.isDeleted='N';
		this.cvvNumber = cvvNumber;
		this.rtftype = rtftype;
	}



	public ClientCardDetails(final String cardName, final String routingNumber,
			final String bankAccountNumber, final String bankName, final String accountType, 
			final String type) {
		
		this.cardName=cardName;
		this.routingNumber=routingNumber;
		this.bankAccountNumber=bankAccountNumber;
		this.bankName=bankName;
		this.accountType=accountType;
		this.type=type;
		this.isDeleted='N';
       //	this.uniqueIdentifier = uniqueIdentifier;
	 }
	
	public ClientCardDetails(final String cardName, final String cardNumber,
			final String cardExpiryDate, final String cardType, final String type, 
			String w_token,String r_type,final Long data) {
		
		this.cardName=cardName;
		this.cardNumber=cardNumber;
		this.expiryDate=cardExpiryDate;
		this.cardType=cardType;
		this.type=type;
		this.isDeleted='N';
		this.w_token=w_token;
		this.r_type=r_type;
	}



	public static ClientCardDetails fromJson(JsonCommand command) throws JSONException {

		String type = command.stringValueOfParameterNamed("type");
		String cardName = command.stringValueOfParameterNamed("name");

		ClientCardDetails clientCardDetails = null;
		if (type.equalsIgnoreCase(CREDIT_CARD) || type.equalsIgnoreCase(PSEUDO_CARD)) {
			Long data = new Long(1);
			String cardNumber = command.stringValueOfParameterNamed("cardNumber");
			String cardExpiryDate = command.stringValueOfParameterNamed("cardExpiryDate");
			String cardType = command.stringValueOfParameterNamed("cardType");
			String cvvNumber = command.stringValueOfParameterNamed("cvvNumber");
			String rtftype = command.stringValueOfParameterNamed("rtftype");
			clientCardDetails = new ClientCardDetails(cardName, cardNumber, cardExpiryDate, cardType, type, data, cvvNumber, rtftype);

		} else if (type.equalsIgnoreCase(ACH_CARD)) {

			final String routingNumber = command.stringValueOfParameterNamed("routingNumber");
			final String bankAccountNumber = command.stringValueOfParameterNamed("bankAccountNumber");
			final String bankName = command.stringValueOfParameterNamed("bankName");
			final String accountType = command.stringValueOfParameterNamed("accountType");
			clientCardDetails = new ClientCardDetails(cardName, routingNumber, bankAccountNumber, bankName, accountType, type);
		} else if (type.equalsIgnoreCase(OBFUSCATED_CARD)) {

			Long data = new Long(1);
			String w_token = command.stringValueOfParameterNamed("token");
			String r_type = command.stringValueOfParameterNamed("r_type");
			//String paymentMethod = command.stringValueOfParameterNamed("paymentMethod");
			JSONObject json = new JSONObject(command.json());
			//json.getJSONObject("paymentMethod");
			
			JSONObject jsonObj= json.getJSONObject("paymentMethod");
			String cardNumber = jsonObj.getString("maskedCardNumber");
			Long expiryYear = jsonObj.getLong("expiryYear");
			Long expiryMonth = jsonObj.getLong("expiryMonth");
			String cardType = jsonObj.getString("cardType");
			String expireMonth = expiryMonth.toString();
			if(expiryMonth<10){
				expireMonth="0"+expiryMonth;
			}
			String cardExpiryDate=expiryYear.toString()+expireMonth;
			
			clientCardDetails = new ClientCardDetails(cardName,cardNumber,cardExpiryDate,cardType,type,w_token,r_type,data);
		}

		return clientCardDetails;
	}

	public Map<String, Object> update(JsonCommand command) {
		
		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		final String cardName = "name";
		if (command.isChangeInStringParameterNamed(cardName,this.cardName)) {
			final String newValue = command.stringValueOfParameterNamed("name");
			actualChanges.put(cardName, newValue);
			this.cardName = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		final String type = "type";
		if (command.isChangeInStringParameterNamed(type,this.type)) {
			final String newValue = command.stringValueOfParameterNamed("type");
			actualChanges.put(type, newValue);
			this.type = StringUtils.defaultIfEmpty(newValue, null);
		}
		
	    if(this.type.equalsIgnoreCase(CREDIT_CARD) || this.type.equalsIgnoreCase(PSEUDO_CARD)){	    
	    	
	    	final String cardNumber = "cardNumber";
			if (command.isChangeInStringParameterNamed(cardNumber,this.cardNumber)) {
				final String newValue = command.stringValueOfParameterNamed("cardNumber");
				actualChanges.put(cardNumber, newValue);
				this.cardNumber = StringUtils.defaultIfEmpty(newValue, null);
			}
			
			final String cardType = "cardType";
			if (command.isChangeInStringParameterNamed(cardType,this.cardType)) {
				final String newValue = command.stringValueOfParameterNamed("cardType");
				actualChanges.put(cardType, newValue);
				this.cardType = StringUtils.defaultIfEmpty(newValue, null);
			}
			
			final String cardExpiryDate = "cardExpiryDate";
			if (command.isChangeInStringParameterNamed(cardExpiryDate,this.expiryDate)) {
				final String newValue = command.stringValueOfParameterNamed("cardExpiryDate");
				actualChanges.put(cardExpiryDate, newValue);
				this.expiryDate = StringUtils.defaultIfEmpty(newValue, null);
			}
			
			final String cvvNumber = "cvvNumber";
			if (command.isChangeInStringParameterNamed(cvvNumber,this.cvvNumber)) {
				final String newValue = command.stringValueOfParameterNamed("cvvNumber");
				actualChanges.put(cvvNumber, newValue);
				this.cvvNumber = StringUtils.defaultIfEmpty(newValue, null);
			}
			
			final String rtftype = "rtftype";
			if (command.isChangeInStringParameterNamed(rtftype,this.rtftype)) {
				final String newValue = command.stringValueOfParameterNamed("rtftype");
				actualChanges.put(rtftype, newValue);
				this.rtftype = StringUtils.defaultIfEmpty(newValue, null);
			}
			
		}else if(this.type.equalsIgnoreCase(ACH_CARD)){					
			
			final String routingNumber = "routingNumber";
			if (command.isChangeInStringParameterNamed(routingNumber,this.routingNumber)) {
				final String newValue = command.stringValueOfParameterNamed("routingNumber");
				actualChanges.put(routingNumber, newValue);
				this.routingNumber = StringUtils.defaultIfEmpty(newValue, null);
			}
			
			final String bankAccountNumber = "bankAccountNumber";
			if (command.isChangeInStringParameterNamed(bankAccountNumber,this.bankAccountNumber)) {
				final String newValue = command.stringValueOfParameterNamed("bankAccountNumber");
				actualChanges.put(bankAccountNumber, newValue);
				this.bankAccountNumber = StringUtils.defaultIfEmpty(newValue, null);
			}
			
			final String bankName = "bankName";
			if (command.isChangeInStringParameterNamed(bankName,this.bankName)) {
				final String newValue = command.stringValueOfParameterNamed("bankName");
				actualChanges.put(bankName, newValue);
				this.bankName = StringUtils.defaultIfEmpty(newValue, null);
			}
			
			final String accountType = "accountType";
			if (command.isChangeInStringParameterNamed(accountType,this.accountType)) {
				final String newValue = command.stringValueOfParameterNamed("accountType");
				actualChanges.put(accountType, newValue);
				this.accountType = StringUtils.defaultIfEmpty(newValue, null);
			}
			
			/*final String uniqueIdentifier = "uniqueIdentifier";
			if (command.isChangeInStringParameterNamed(uniqueIdentifier,this.uniqueIdentifier)) {
				final String newValue = command.stringValueOfParameterNamed("uniqueIdentifier");
				actualChanges.put(uniqueIdentifier, newValue);
				this.uniqueIdentifier = StringUtils.defaultIfEmpty(newValue, null);
			}
			*/
			
		}
		return actualChanges;
	}

	
	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}


	public char getIsDeleted() {
		return isDeleted;
	}


	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}


	public String getRtftype() {
		return rtftype;
	}


	public void setRtftype(String rtftype) {
		this.rtftype = rtftype;
	}


	public String getW_token() {
		return w_token;
	}


	public void setW_token(String w_token) {
		this.w_token = w_token;
	}


	public String getR_type() {
		return r_type;
	}


	public void setR_type(String r_type) {
		this.r_type = r_type;
	}

	
	
	
	

}
