package org.obsplatform.organisation.message.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.message.domain.BillingMessageTemplate;

/**
 * 
 * @author ashokreddy
 *
 */
public interface BillingMessageTemplateWritePlatformService {
	
CommandProcessingResult addMessageTemplate(final JsonCommand json);

CommandProcessingResult updateMessageTemplate(final JsonCommand command);

CommandProcessingResult deleteMessageTemplate(final JsonCommand command);

void processEmailNotification(Long clientId, Long orderId, String exceptionReason, String messageTemplateName);

BillingMessageTemplate getMessageTemplate(String messageTemplateName);

}
