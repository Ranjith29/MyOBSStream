package org.obsplatform.billing.selfcare.data;

import java.util.List;

import org.obsplatform.crm.ticketmaster.data.TicketMasterData;
import org.obsplatform.finance.billingorder.data.BillDetailsData;
import org.obsplatform.finance.clientbalance.data.ClientBalanceData;
import org.obsplatform.finance.payments.data.PaymentData;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfiguration;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.organisation.address.data.AddressData;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.order.data.OrderData;


public class SelfCareData {

private Long clientId;
	
	private String userName;
	private String password;
	private String uniqueReference;
	private Boolean isDeleted;
	private String email;
	private ClientData clientData;
	private ClientBalanceData clientBalanceData;
	private List<AddressData> addressData;
	private List<OrderData> clientOrdersData;
	private Page<BillDetailsData> statementsData;
	private List<PaymentData> paymentsData;
	private List<TicketMasterData> ticketMastersData;
	private PaymentGatewayConfiguration paypalConfigData;
	private String authPin;
	private PaymentGatewayConfiguration paypalConfigDataForIos;
	private Long loginHistoryId; 

	public SelfCareData(Long clientId, String email) {
		this.clientId = clientId;
		this.email = email;
	}

	public SelfCareData() {
		// TODO Auto-generated constructor stub
	}



	public SelfCareData(String authPin, Long clientId, String password) {
		this.authPin = authPin;
		this.clientId = clientId;
		this.password = password;

	}

	public Long getClientId() {
		return clientId;
	}


	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getUniqueReference() {
		return uniqueReference;
	}


	public void setUniqueReference(String uniqueReference) {
		this.uniqueReference = uniqueReference;
	}


	public Boolean getIsDeleted() {
		return isDeleted;
	}


	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public void setDetails(ClientData clientsData,
			ClientBalanceData balanceData, List<AddressData> addressData,
			List<OrderData> clientOrdersData,
			Page<BillDetailsData> statementsData,
			List<PaymentData> paymentsData,
			List<TicketMasterData> ticketMastersData,
			Long loginHistoryId) {
		
		
		this.clientData = clientsData;
		this.clientBalanceData = balanceData;
		this.addressData = addressData;
		this.clientOrdersData = clientOrdersData;
		this.statementsData = statementsData;
		this.paymentsData = paymentsData;
		this.ticketMastersData = ticketMastersData;
		this.loginHistoryId=loginHistoryId;
		
	}



	public PaymentGatewayConfiguration getPaypalConfigData() {
		return paypalConfigData;
	}



	public void setPaypalConfigData(PaymentGatewayConfiguration paypalConfigData) {
		this.paypalConfigData = paypalConfigData;
	}

	public void setPaypalConfigDataForIos(PaymentGatewayConfiguration paypalConfigDataForIos) {
		this.paypalConfigDataForIos =paypalConfigDataForIos;
		
	}
	
	

}
