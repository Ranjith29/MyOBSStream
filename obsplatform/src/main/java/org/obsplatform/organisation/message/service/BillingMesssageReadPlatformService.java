package org.obsplatform.organisation.message.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.obsplatform.organisation.message.data.BillingMessageDataForProcessing;
import org.obsplatform.organisation.message.data.BillingMessageTemplateData;
import org.obsplatform.template.domain.Template;

/**
 * 
 * @author ashokreddy
 *
 */
public interface BillingMesssageReadPlatformService {

	BillingMessageTemplateData retrieveMessageTemplate(Long messageTemplateId);

	List<BillingMessageTemplateData> retrieveAllMessageTemplateParams();

	List<BillingMessageTemplateData> retrieveMessageParams(Long entityId);

	List<BillingMessageTemplateData> retrieveData(Long command, String json,
			BillingMessageTemplateData templateData,
			List<BillingMessageTemplateData> messageparam,
			BillingMesssageReadPlatformService billingMesssageReadPlatformService);

	List<BillingMessageDataForProcessing> retrieveMessageDataForProcessing(Long id);

	BillingMessageTemplateData retrieveTemplate();

	Long retrieveClientId(String hardwareId) throws IOException;

	List<List<Map<String, Object>>> retrieveMessageQuery(String query,Template template);

	
}
