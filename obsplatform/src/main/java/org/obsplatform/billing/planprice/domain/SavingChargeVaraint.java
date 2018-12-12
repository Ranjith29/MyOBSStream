package org.obsplatform.billing.planprice.domain;

import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.portfolio.pricing.domain.ChargeVariant;


public class SavingChargeVaraint {

	public static EnumOptionData interestCompoundingPeriodType(final int id) {
		return interestCompoundingPeriodType(ChargeVariant.fromInt(id));
	}

	public static EnumOptionData interestCompoundingPeriodType(final ChargeVariant type) {
		final String codePrefix = "deposit.interest.compounding.period.";
		EnumOptionData optionData = null;
		switch (type) {
		case SUBSCRIBER_TYPE:
			optionData = new EnumOptionData(ChargeVariant.SUBSCRIBER_TYPE.getValue().longValue(), codePrefix + ChargeVariant.SUBSCRIBER_TYPE.getCode(), "SUBSCRIBER_TYPE");
			break;
		case AREAS:
			optionData = new EnumOptionData(ChargeVariant.AREAS.getValue().longValue(), codePrefix + ChargeVariant.AREAS.getCode(), "AREAS");
			break;
		case BASE:
			optionData = new EnumOptionData(ChargeVariant.BASE.getValue().longValue(), codePrefix + ChargeVariant.BASE.getCode(), "BASE");
			break;

		default:
			optionData = new EnumOptionData(ChargeVariant.INVALID.getValue().longValue(), ChargeVariant.INVALID.getCode(), "Invalid");
			break;
		}
		return optionData;
	}

}
