package org.obsplatform.finance.billingmaster.service;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.finance.billingmaster.domain.BillDetail;
import org.obsplatform.finance.billingmaster.domain.BillMaster;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface BillWritePlatformService {
	
	CommandProcessingResult updateBillMaster(List<BillDetail> billDetails,BillMaster billMaster, BigDecimal previousBal);

	String generateStatementPdf(Long billId);

	String generateInovicePdf(Long billId);
	
	String generatePaymentPdf(Long paymentId);

	void sendPdfToEmail(String printFileName,Long clientId,String templateName);


}
