package org.obsplatform.finance.payments.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface PaymentWritePlatformService {

	CommandProcessingResult createPayment(JsonCommand command);

	CommandProcessingResult cancelPayment(JsonCommand command,Long entityId);

	CommandProcessingResult paypalEnquirey(JsonCommand command);

	CommandProcessingResult refundPayment(JsonCommand command, Long entityId);

}
