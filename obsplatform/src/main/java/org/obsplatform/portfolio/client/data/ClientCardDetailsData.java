package org.obsplatform.portfolio.client.data;

public class ClientCardDetailsData {

	private final Long id;
	private final Long clientId;
	private final String type;
	private final String name;	
	private final String cardNumber;
	private final String routingNumber;
	private final String bankName;
	private final String accountType;
	private final String cardExpiryDate;
	private final String bankAccountNumber;
	private final String cardType;
	private final String cvvNum;
	private final String rtftype;
	private  String r_type;
	//private final String uniqueIdentifier;
	
	
	public ClientCardDetailsData(final Long id, final Long clientId, final String name, final String cardNumber, 
			final String routingNumber, final String bankName, final String accountType, final String cardExpiryDate, 
			final String bankAccountNumber, final String cardType, final String type, final String cvvNum,final String rtftype) {
		
		this.id=id;
		this.clientId=clientId;
		this.type=type;
		this.name=name;
		this.cardNumber=cardNumber;
		this.routingNumber=routingNumber;
		this.bankName=bankName;
		this.accountType=accountType;
		this.cardExpiryDate=cardExpiryDate;
		this.bankAccountNumber=bankAccountNumber;
		this.cardType=cardType;
		this.cvvNum = cvvNum;
		this.rtftype = rtftype;
		//this.uniqueIdentifier = uniqueIdentifier;
	}


	public Long getId() {
		return id;
	}


	public Long getClientId() {
		return clientId;
	}


	public String getType() {
		return type;
	}


	public String getName() {
		return name;
	}


	public String getCardNumber() {
		return cardNumber;
	}


	public String getRoutingNumber() {
		return routingNumber;
	}


	public String getBankName() {
		return bankName;
	}


	public String getAccountType() {
		return accountType;
	}


	public String getCardExpiryDate() {
		return cardExpiryDate;
	}


	public String getBankAccountNumber() {
		return bankAccountNumber;
	}


	public String getCardType() {
		return cardType;
	}


	public String getCvvNum() {
		return cvvNum;
	}


	public String getRtftype() {
		return rtftype;
	}


	public String getR_type() {
		return r_type;
	}


	public void setR_type(String r_type) {
		this.r_type = r_type;
	}

	

}
