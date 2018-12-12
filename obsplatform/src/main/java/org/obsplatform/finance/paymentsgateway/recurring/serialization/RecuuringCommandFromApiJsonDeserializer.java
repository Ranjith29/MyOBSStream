package org.obsplatform.finance.paymentsgateway.recurring.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.echeck.BankAccountType;
import net.authorize.data.xml.BankAccount;
import net.authorize.data.xml.Payment;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.InvalidJsonException;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author ashokreddy
 *
 */
@Component
public class RecuuringCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> authorizeParameters = new HashSet<String>(Arrays.asList("total_amount", "clientId",
			"emailId", "transactionId", "otherData", "device", "currency", "dateFormat", "locale", "paytermCode",
			"planCode", "contractPeriod", "value", "verificationCode", "screenName", "renewalPeriod", "description",
			"cardType", "cardNumber", "status", "error", "bankName", "accountNumber", "bankAccountType",
			"routingNumber", "accountName", "cardNumber", "month", "year", "payTermCode", "paymentType",
			"recurringName", "actionName", "orderId", "subscriberId","depositamount"));

	private final FromJsonHelper fromApiJsonHelper;

	private final Set<String> authorizeNotifyParameters = new HashSet<String>(Arrays.asList(
			"x_response_subcode", "x_response_code", "tenantIdentifier", "username", "password",
			"x_invoice_num", "x_trans_id", "x_avs_code", "x_auth_code", "x_response_reason_text", "x_response_reason_code", 
			"x_last_name", "x_first_name", "x_cust_id", "x_type", "x_method", "x_amount", "x_description",
			"x_email", "x_fax", "x_phone", "x_country", "x_zip", "x_state", "x_city", "x_address", "x_company",
			"x_ship_to_city", "x_ship_to_address", "x_ship_to_company", "x_ship_to_last_name", "x_ship_to_first_name", 
			"x_freight", "x_duty", "x_tax", "x_ship_to_country", "x_ship_to_zip", "x_ship_to_state", "x_subscription_paynum", 
			"x_test_request", "x_cavv_response", "x_MD5_Hash", "x_po_num", "x_tax_exempt", "x_subscription_id"));
	
	

	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList(
			"clientId", "orderId", "gatewayName"));

	@Autowired
	public RecuuringCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForAuthorizeRecurringProfile(final String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, authorizeParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("paymentgateway");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String actionName = fromApiJsonHelper.extractStringNamed("actionName", element);
	
		if (actionName.equalsIgnoreCase(ConfigurationConstants.CREATE)) {

			final String paymentType = fromApiJsonHelper.extractStringNamed("paymentType", element);
			baseDataValidator.reset().parameter("paymentType").value(paymentType).notBlank().notExceedingLengthOf(30);

			final String payTermCode = fromApiJsonHelper.extractStringNamed("payTermCode", element);
			baseDataValidator.reset().parameter("payTermCode").value(payTermCode).notBlank().notExceedingLengthOf(30);

			final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("total_amount", element);
			baseDataValidator.reset().parameter("total_amount").value(amount).notBlank();
			
			throwExceptionIfValidationWarningsExist(dataValidationErrors);
			
			if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_CREDIT_CARD_TYPE)) {
				final String cardNumber = fromApiJsonHelper.extractStringNamed("cardNumber", element);
				baseDataValidator.reset().parameter("cardNumber").value(cardNumber).notBlank().notExceedingLengthOf(16);
				final String month = fromApiJsonHelper.extractStringNamed("month", element);
				baseDataValidator.reset().parameter("month").value(month).notBlank().notExceedingLengthOf(2);
				final String year = fromApiJsonHelper.extractStringNamed("year", element);
				baseDataValidator.reset().parameter("year").value(year).notBlank().notExceedingLengthOf(4);				
			} else if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_BANK_ACCOUNT)) {
				final String bankName = fromApiJsonHelper.extractStringNamed("bankName", element);
				baseDataValidator.reset().parameter("bankName").value(bankName).notBlank().notExceedingLengthOf(50);
				final String accountNumber = fromApiJsonHelper.extractStringNamed("accountNumber", element);
				baseDataValidator.reset().parameter("accountNumber").value(accountNumber).notBlank();
				final String routingNumber = fromApiJsonHelper.extractStringNamed("routingNumber", element);
				baseDataValidator.reset().parameter("routingNumber").value(routingNumber).notBlank().notExceedingLengthOf(9);
				final String accountName = fromApiJsonHelper.extractStringNamed("accountName", element);
				baseDataValidator.reset().parameter("accountName").value(accountName).notBlank().notExceedingLengthOf(30);
				final String bankAccountType = fromApiJsonHelper.extractStringNamed("bankAccountType", element);
				baseDataValidator.reset().parameter("bankAccountType").value(bankAccountType).notBlank().notExceedingLengthOf(20);
			} else {
				baseDataValidator.reset().parameter("paymentType").value(paymentType).isOneOfTheseStringValues(
						ConfigurationConstants.RECC_CREDIT_CARD_TYPE, ConfigurationConstants.RECC_BANK_ACCOUNT);
			}
			
		} else if (actionName.equalsIgnoreCase(ConfigurationConstants.CANCEL)) {
			
			if(fromApiJsonHelper.parameterExists("orderId", element)) {
				final Long orderId = fromApiJsonHelper.extractLongNamed("orderId", element);
				baseDataValidator.reset().parameter("orderId").value(orderId).notBlank().notExceedingLengthOf(20);
			} else if (fromApiJsonHelper.parameterExists("subscriberId", element)) {
				final String subscriberId = fromApiJsonHelper.extractStringNamed("subscriberId", element);
				baseDataValidator.reset().parameter("subscriberId").value(subscriberId).notBlank().notExceedingLengthOf(50);
			} else {
				baseDataValidator.reset().parameter("orderId/subscriberId").value(null).notNull();
			}
			
		} else if (actionName.equalsIgnoreCase(ConfigurationConstants.UPDATE)) {
			
			final Long orderId = fromApiJsonHelper.extractLongNamed("orderId", element);
			baseDataValidator.reset().parameter("orderId").value(orderId).notBlank().notExceedingLengthOf(20);
			
			final String paymentType = fromApiJsonHelper.extractStringNamed("paymentType", element);
			baseDataValidator.reset().parameter("paymentType").value(paymentType).notBlank().notExceedingLengthOf(30);
			
			throwExceptionIfValidationWarningsExist(dataValidationErrors);
						
			if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_UPDATE_CREDIT_CARD_TYPE)) {	
				final String cardNumber = fromApiJsonHelper.extractStringNamed("cardNumber", element);
				baseDataValidator.reset().parameter("cardNumber").value(cardNumber).notBlank().notExceedingLengthOf(16);
				final String month = fromApiJsonHelper.extractStringNamed("month", element);
				baseDataValidator.reset().parameter("month").value(month).notBlank().notExceedingLengthOf(2);
				final String year = fromApiJsonHelper.extractStringNamed("year", element);
				baseDataValidator.reset().parameter("year").value(year).notBlank().notExceedingLengthOf(4);
				
			} else if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_UPDATE_BANK_ACCOUNT)) {
				final String bankName = fromApiJsonHelper.extractStringNamed("bankName", element);
				baseDataValidator.reset().parameter("bankName").value(bankName).notBlank().notExceedingLengthOf(50);
				final String accountNumber = fromApiJsonHelper.extractStringNamed("accountNumber", element);
				baseDataValidator.reset().parameter("accountNumber").value(accountNumber).notBlank().notExceedingLengthOf(30);
				final String routingNumber = fromApiJsonHelper.extractStringNamed("routingNumber", element);
				baseDataValidator.reset().parameter("routingNumber").value(routingNumber).notBlank().notExceedingLengthOf(9);
				final String accountName = fromApiJsonHelper.extractStringNamed("accountName", element);
				baseDataValidator.reset().parameter("accountName").value(accountName).notBlank().notExceedingLengthOf(30);
				final String bankAccountType = fromApiJsonHelper.extractStringNamed("bankAccountType", element);
				baseDataValidator.reset().parameter("bankAccountType").value(bankAccountType).notBlank().notExceedingLengthOf(20);
			
			} else if (paymentType.equalsIgnoreCase(ConfigurationConstants.RECC_UPDATE_RECURRING_AMOUNT)) {
				final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("total_amount", element);
				baseDataValidator.reset().parameter("total_amount").value(amount).notBlank();	
			
			} else {
				baseDataValidator.reset().parameter("paymentType").value(paymentType).isOneOfTheseStringValues(
						ConfigurationConstants.RECC_UPDATE_CREDIT_CARD_TYPE, ConfigurationConstants.RECC_UPDATE_BANK_ACCOUNT,
						ConfigurationConstants.RECC_UPDATE_RECURRING_AMOUNT);
			}	
		}
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	/**
	 * @param json
	 */
	public void validateForProfileProcess(final String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, authorizeParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
				.resource("paymentgateway");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank().notExceedingLengthOf(30);

		final String actionName = fromApiJsonHelper.extractStringNamed("actionName", element);
		baseDataValidator.reset().parameter("actionName").value(actionName).notBlank().notExceedingLengthOf(6)
				.isOneOfTheseStringValues(ConfigurationConstants.CREATE,
						ConfigurationConstants.UPDATE, ConfigurationConstants.GET,
						ConfigurationConstants.CANCEL);

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForUpdateRecurringBillingProfile(final String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, authorizeParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
				.resource("paymentgateway");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final Long orderId = fromApiJsonHelper.extractLongNamed("orderId", element);
		baseDataValidator.reset().parameter("orderId").value(orderId).notBlank().notExceedingLengthOf(30);

		final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank().notExceedingLengthOf(30);

		final String subscriberId = fromApiJsonHelper.extractStringNamed("subscriberId", element);
		baseDataValidator.reset().parameter("subscriberId").value(subscriberId).notBlank().notExceedingLengthOf(20);

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	
	public void validateForNotifyAuthorizeRequest(String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, authorizeNotifyParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
				.resource("paymentgateway");
		
		final JsonElement element = fromApiJsonHelper.parse(json);

		final String responseSubcode = fromApiJsonHelper.extractStringNamed("x_response_subcode", element);
		baseDataValidator.reset().parameter("x_response_subcode").value(responseSubcode).notBlank().notExceedingLengthOf(50);
		
		final String responseCode = fromApiJsonHelper.extractStringNamed("x_response_code", element);
		baseDataValidator.reset().parameter("x_response_code").value(responseCode).notBlank().notExceedingLengthOf(50);
		
		final String transactionId = fromApiJsonHelper.extractStringNamed("x_trans_id", element);
		baseDataValidator.reset().parameter("x_trans_id").value(transactionId).notBlank().notExceedingLengthOf(30);
		
		final String responseReasonText = fromApiJsonHelper.extractStringNamed("x_response_reason_text", element);
		baseDataValidator.reset().parameter("x_response_reason_text").value(responseReasonText).notBlank();
		
		final String responseReasonCode = fromApiJsonHelper.extractStringNamed("x_response_reason_code", element);
		baseDataValidator.reset().parameter("x_response_reason_code").value(responseReasonCode).notBlank().notExceedingLengthOf(20);
		
		final String firstName = fromApiJsonHelper.extractStringNamed("x_first_name", element);
		baseDataValidator.reset().parameter("x_first_name").value(firstName).notBlank().notExceedingLengthOf(50);
		
		final String lastName = fromApiJsonHelper.extractStringNamed("x_last_name", element);
		baseDataValidator.reset().parameter("x_last_name").value(lastName).notBlank().notExceedingLengthOf(50);
		
		if(fromApiJsonHelper.parameterExists("x_subscription_id", element)) {
			final String subscriptionId = fromApiJsonHelper.extractStringNamed("x_subscription_id", element);
			baseDataValidator.reset().parameter("x_subscription_id").value(subscriptionId).notBlank().notExceedingLengthOf(50);
		}
		
		if(fromApiJsonHelper.parameterExists("x_subscription_paynum", element)) {
			
			final String subscriptionPayNum = fromApiJsonHelper.extractStringNamed("x_subscription_paynum", element);
			baseDataValidator.reset().parameter("x_subscription_paynum").value(subscriptionPayNum).notBlank().notExceedingLengthOf(50);
			
			final String amountInString = fromApiJsonHelper.extractStringNamed("x_amount", element);
			final BigDecimal amount = BigDecimal.valueOf(Double.valueOf(amountInString));
			baseDataValidator.reset().parameter("x_amount").value(amount).notBlank().notLessThanMin(BigDecimal.ONE);
			
		}

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForCreate(String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
				.resource("recurring");
		
		final JsonElement element = fromApiJsonHelper.parse(json);

		final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank().notExceedingLengthOf(20);
		
		final Long orderId = fromApiJsonHelper.extractLongNamed("orderId", element);
		baseDataValidator.reset().parameter("orderId").value(orderId).notBlank().notExceedingLengthOf(20);
		
		final String gatewayName = fromApiJsonHelper.extractStringNamed("gatewayName", element);
		baseDataValidator.reset().parameter("gatewayName").value(gatewayName).notBlank().notExceedingLengthOf(100);
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}
}
