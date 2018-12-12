package org.obsplatform.billing.chargevariant.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
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

/**
 * Deserializer for code JSON to validate API request.
 */
@Component
public final class ChargeVariantCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
	
	
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "chargeVariantCode", "dateFormat", "locale",
			"status", "startDate", "endDate", "variantType", "from", "to", "range", "amountType", "amount", "chargeVariantDetails"));
    
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ChargeVariantCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("chargevariant");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String chargeVariantCode = fromApiJsonHelper.extractStringNamed("chargeVariantCode", element);
        baseDataValidator.reset().parameter("chargeVariantCode").value(chargeVariantCode).notBlank().notExceedingLengthOf(10);
        final String status = fromApiJsonHelper.extractStringNamed("status", element);
        baseDataValidator.reset().parameter("status").value(status).notBlank();
        final LocalDate startDate = fromApiJsonHelper.extractLocalDateNamed("startDate", element);
        baseDataValidator.reset().parameter("startDate").value(startDate).notBlank();
        final LocalDate endDate = fromApiJsonHelper.extractLocalDateNamed("endDate", element);
        baseDataValidator.reset().parameter("endDate").value(endDate).notBlank();
        final JsonArray chargeVariantAttributesArray = fromApiJsonHelper.extractJsonArrayNamed("chargeVariantDetails", element);
        
        String[] chargeVariantDetails = null;
        chargeVariantDetails = new String[chargeVariantAttributesArray.size()];
	    final int attributeSize = chargeVariantAttributesArray.size();
	    baseDataValidator.reset().parameter("chargeVariantDetails").value(attributeSize).integerGreaterThanZero();
	    for(int i = 0; i < chargeVariantAttributesArray.size(); i++){
	    	chargeVariantDetails[i] = chargeVariantAttributesArray.get(i).toString();
	    }
	    	
        
	    for (final String chargeVariantDetail : chargeVariantDetails) {
	    	
	    	
	    	final JsonElement attributeElement = fromApiJsonHelper.parse(chargeVariantDetail);
	    	final String variantType = fromApiJsonHelper.extractStringNamed("variantType",attributeElement);
	    	baseDataValidator.reset().parameter("variantType").value(variantType).notBlank();
	    		
	    		if("Range".equalsIgnoreCase(variantType)){
	    			final Long from = fromApiJsonHelper.extractLongNamed("from", attributeElement);
	    			baseDataValidator.reset().parameter("from").value(from).notNull();

	    			final Long to = fromApiJsonHelper.extractLongNamed("to", attributeElement);
	    			baseDataValidator.reset().parameter("to").value(to).notNull();
	    		}
	    		
	    		final String amountType=fromApiJsonHelper.extractStringNamed("amountType", attributeElement);
		    	baseDataValidator.reset().parameter("amountType").value(amountType).notBlank();
		    	
	    	final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", attributeElement);
	    	baseDataValidator.reset().parameter("amount").value(amount).notNull();
	    	
	    	
		  }
	   
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        
    }

  

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}