package org.obsplatform.finance.paymentsgateway.domain;

import org.obsplatform.finance.paymentsgateway.data.PaymentEnum;
import org.obsplatform.infrastructure.core.data.MediaEnumoptionData;

public class PaymentEnumClass {
	
	public static MediaEnumoptionData enumPaymentData(final int id) {
		return enumPaymentData(PaymentEnum.fromInt(id));
	}
	
	public static MediaEnumoptionData enumPaymentData(final PaymentEnum paymentEnum) {

		MediaEnumoptionData optionData;
		
		switch (paymentEnum) {
		case FINISHED:
			optionData = new MediaEnumoptionData(PaymentEnum.FINISHED.getValue(), PaymentEnum.FINISHED.getCode(), "FINISHED");
			break;

		default:
			optionData = new MediaEnumoptionData(PaymentEnum.INVALID.getValue(), PaymentEnum.INVALID.getCode(), "INVALID");
			break;
		}

		return optionData;

	}

}
