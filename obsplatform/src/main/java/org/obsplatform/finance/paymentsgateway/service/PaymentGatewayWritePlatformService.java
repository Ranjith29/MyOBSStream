package org.obsplatform.finance.paymentsgateway.service;

import java.io.IOException;
import java.math.BigDecimal;

import org.json.JSONException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface PaymentGatewayWritePlatformService {

	public CommandProcessingResult createPaymentGateway(JsonCommand command);

	public CommandProcessingResult updatePaymentGateway(JsonCommand command);

	public CommandProcessingResult onlinePaymentGateway(JsonCommand command);
	
	//public String payment(Long clientId, Long id, String txnId, String amount, String errorDesc) throws JSONException;
	
	//void emailSending(Long clientId, String result, String description,String txnId, String amount, String cardType, String cardNumber) throws JSONException;

	public String globalPayProcessing(String transactionId, String remarks) throws JSONException, IOException;

	public String createFingerPrint(BigDecimal amount);

	public String echeckProcess(String apiRequestBodyAsJson);

	public String onlinePaymentGateway(String commandJson);

	public CommandProcessingResult crediCardProcess(JsonCommand command);
	
    //for worldpay intigration
	public CommandProcessingResult createOrderWorldpay(JsonCommand command);
	
	public CommandProcessingResult createOrderWorldpayRecurring(Long client, String apiRequestBodyAsJson);
	
}