package org.obsplatform.finance.billingorder.service;

import java.util.List;

import org.joda.time.LocalDate;

import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.billing.taxmaster.data.TaxMappingRateData;
import org.obsplatform.finance.billingorder.data.BillingOrderData;
import org.obsplatform.finance.billingorder.data.GenerateInvoiceData;
import org.obsplatform.organisation.partner.data.AgreementData;

public interface BillingOrderReadPlatformService {

	List<BillingOrderData> retrieveOrderIds(Long clientId, LocalDate processDate);
	
	List<BillingOrderData> retrieveBillingOrderData(Long clientId,LocalDate localDate, Long planId);

	List<DiscountMasterData> retrieveDiscountOrders(Long orderId,Long orderPriceId);
	
	List<TaxMappingRateData> retrieveTaxMappingData(Long clientId, String chargeCode);

	List<TaxMappingRateData> retrieveDefaultTaxMappingData(Long clientId,String chargeCode);

	List<BillingOrderData> getReverseBillingOrderData(Long clientId,LocalDate disconnectionDate, Long orderId);

	AgreementData retriveClientOfficeDetails(Long clientId);

	AgreementData retrieveOfficeChargesCommission(Long id);

	List<Long> listOfInvoices(Long clientId, Long orderId);

	GenerateInvoiceData getAllChargesAmountsOnOrder(Long clientId, Long clientOrderId,LocalDate disconnectionDate);

}
