package org.obsplatform.finance.paymentsgateway.service;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.finance.paymentsgateway.data.PaymentGatewayData;
import org.obsplatform.finance.paymentsgateway.data.PaymentGatewayDownloadData;
import org.obsplatform.infrastructure.core.data.MediaEnumoptionData;
import org.obsplatform.infrastructure.core.service.Page;

/**
 * 
 * @author ashokreddy
 *
 */
public interface PaymentGatewayReadPlatformService {
	
	Long retrieveClientIdForProvisioning(String serialNum);

	Page<PaymentGatewayData> retrievePaymentGatewayData(SearchSqlQuery searchItemDetails, String type, String source);


	List<MediaEnumoptionData> retrieveTemplateData();

	PaymentGatewayData retrievePaymentGatewayIdData(Long id);

	String findReceiptNo(String receiptNo);

	Long getReceiptNoId(String receipt);

	List<PaymentGatewayDownloadData> retriveDataForDownload(String source,
			String startDate, String endDate, String status);

	List<PaymentGatewayData> retrievePendingDetails();

	Response retrieveDownloadedData(String source, String status, Long fromDate, Long toDate) throws IOException;
	
	

}
