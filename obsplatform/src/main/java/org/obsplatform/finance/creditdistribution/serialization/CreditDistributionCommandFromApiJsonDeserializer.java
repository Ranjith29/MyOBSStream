package org.obsplatform.finance.creditdistribution.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.data.ApiParameterError;
import org.obsplatform.infrastructure.core.data.DataValidatorBuilder;
import org.obsplatform.infrastructure.core.exception.InvalidJsonException;
import org.obsplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
@Component
public class CreditDistributionCommandFromApiJsonDeserializer {

	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("creditdistributions","paymentId","clientId","invoiceId","locale","dateFormat",
			"amount","distributionDate","avialableAmount","paymentType","depositId","cancelRemark"));
	
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	CreditDistributionCommandFromApiJsonDeserializer(final FromJsonHelper formApiJsonHelper){
		this.fromApiJsonHelper = formApiJsonHelper;
		
	}
	
	  public void validateForCreate(final String json) {
		  if(StringUtils.isBlank(json)){
			  throw new InvalidJsonException();
		  }	
		  
		   final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
	       fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

	        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
	        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("creditDistribution");
	        final JsonElement element = fromApiJsonHelper.parse(json);
	        
	        String paymentType = fromApiJsonHelper.extractStringNamed("paymentType", element);
	        JsonArray allocationData = fromApiJsonHelper.extractJsonArrayNamed("creditdistributions", element);
	        
	        
	        int i=1;
	        for(JsonElement j:allocationData){
	        	final Long paymentId = fromApiJsonHelper.extractLongNamed("paymentId", j);
	        	baseDataValidator.reset().parameter("paymentId").value(paymentId).notNull();
	        	final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", j);
	        	baseDataValidator.reset().parameter("clientId").value(clientId).notNull().notBlank();
	        	if("Invoice".equalsIgnoreCase(paymentType)){
	        	final Long invoiceId = fromApiJsonHelper.extractLongNamed("invoiceId", j);
	        	baseDataValidator.reset().parameter("invoiceId").value(invoiceId).notBlank();
	        	}
	        	if("Deposit".equalsIgnoreCase(paymentType)){
	        	final Long depositId = fromApiJsonHelper.extractLongNamed("depositId", j);
	        	baseDataValidator.reset().parameter("depositId").value(depositId).notBlank();
	        	}
	        	final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", j);
	        	baseDataValidator.reset().parameter("amount").value(amount).notBlankFoSerialNumber();
		        i++;
	        }
	        
	          
			throwExceptionIfValidationWarningsExist(dataValidationErrors);
	  }
	
	  private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
	        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
	                "Validation errors exist.", dataValidationErrors); }
	    }

	public void validateForCancel(String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("creditDistribution");

		final JsonElement element = fromApiJsonHelper.parse(json);
		final String cancelRemark = fromApiJsonHelper.extractStringNamed("cancelRemark", element);
		baseDataValidator.reset().parameter("cancelRemark").value(cancelRemark).notBlank().notExceedingLengthOf(50);

		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}
}
