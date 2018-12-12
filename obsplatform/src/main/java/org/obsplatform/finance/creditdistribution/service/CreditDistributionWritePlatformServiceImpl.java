package org.obsplatform.finance.creditdistribution.service;

import java.math.BigDecimal;

import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.billingorder.domain.InvoiceRepository;
import org.obsplatform.finance.creditdistribution.domain.CreditDistribution;
import org.obsplatform.finance.creditdistribution.domain.CreditDistributionRepository;
import org.obsplatform.finance.creditdistribution.serialization.CreditDistributionCommandFromApiJsonDeserializer;
import org.obsplatform.finance.depositandrefund.domain.DepositAndRefund;
import org.obsplatform.finance.depositandrefund.domain.DepositAndRefundRepository;
import org.obsplatform.finance.payments.domain.Payment;
import org.obsplatform.finance.payments.domain.PaymentRepository;
import org.obsplatform.finance.payments.exception.PaymentDetailsNotFoundException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.plan.service.PlanWritePlatformServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class CreditDistributionWritePlatformServiceImpl implements CreditDistributionWritePlatformService{

	private final static Logger logger = LoggerFactory.getLogger(PlanWritePlatformServiceImpl.class);
	
	private final PlatformSecurityContext context;
	private final CreditDistributionRepository creditDistributionRepository;
	private final CreditDistributionCommandFromApiJsonDeserializer commandFromApiJsonDeserializer; 
	private final FromJsonHelper fromJsonHelper;
	private final InvoiceRepository invoiceRepository;
	private final PaymentRepository paymentRepository;
	private final DepositAndRefundRepository depositAndRefundRepository;
	
	
	@Autowired
	public CreditDistributionWritePlatformServiceImpl(PlatformSecurityContext context,final CreditDistributionRepository creditDistributionRepository,
			final CreditDistributionCommandFromApiJsonDeserializer commandFromApiJsonDeserializer,final FromJsonHelper fromJsonHelper,
			final InvoiceRepository invoiceRepository,final PaymentRepository paymentRepository,
			final DepositAndRefundRepository depositAndRefundRepository){
		
		this.context=context;
		this.creditDistributionRepository=creditDistributionRepository;
		this.commandFromApiJsonDeserializer=commandFromApiJsonDeserializer;
		this.fromJsonHelper=fromJsonHelper;
		this.invoiceRepository=invoiceRepository;
		this.paymentRepository=paymentRepository;
		this.depositAndRefundRepository=depositAndRefundRepository;
	}
	
	
	@Transactional
	@Override
	public CommandProcessingResult createCreditDistribution(JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			commandFromApiJsonDeserializer.validateForCreate(command.json());
			 final JsonElement element = fromJsonHelper.parse(command.json());
			 final BigDecimal avialableAmount = fromJsonHelper.extractBigDecimalWithLocaleNamed("avialableAmount",element);
			 final Long paymentId=command.longValueOfParameterNamed("paymentId");
			 Long clientId = null;
			 String paymentType = null;
			 if(command.hasParameter("paymentType")){
					paymentType = command.stringValueOfParameterNamed("paymentType");
			 }
			 if("Deposit".equalsIgnoreCase(paymentType)){
				 JsonArray creditDistributions = fromJsonHelper.extractJsonArrayNamed("creditdistributions", element);
				 
				 for (JsonElement je : creditDistributions) {
						final Long depositId = fromJsonHelper.extractLongNamed("depositId", je);
						final BigDecimal amount = fromJsonHelper.extractBigDecimalWithLocaleNamed("amount", je);
						DepositAndRefund deopositAndRefund = this.depositAndRefundRepository.findOne(depositId);
						deopositAndRefund.setPaymentId(paymentId);
						this.depositAndRefundRepository.saveAndFlush(deopositAndRefund);
					}
				 
				 
			 }else if("Invoice".equalsIgnoreCase(paymentType)){
				 JsonArray creditDistributions = fromJsonHelper.extractJsonArrayNamed("creditdistributions", element);
			        
			        for(JsonElement j:creditDistributions)
			        {
			        	clientId=fromJsonHelper.extractLongNamed("clientId", j);
			        	CreditDistribution creditDistribution= CreditDistribution.fromJson(j,fromJsonHelper);
			        	this.creditDistributionRepository.save(creditDistribution);
			        	Invoice invoice=this.invoiceRepository.findOne(creditDistribution.getInvoiceId());
			        	invoice.updateAmount(creditDistribution.getAmount());
			        	this.invoiceRepository.save(invoice);
			        } 
			 }
		      if(avialableAmount.compareTo(BigDecimal.ZERO) !=1){
		    	  Payment payment=this.paymentRepository.findOne(paymentId);
		    	  payment.setInvoiceId(Long.valueOf(-1));
		    	  this.paymentRepository.save(payment);
		    	  
		      }
		    return new CommandProcessingResult(Long.valueOf(1l),clientId);
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve); 
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
		
	}
	
	@Override
	public CommandProcessingResult cancelCreditDistribution(final JsonCommand command,final Long distributionId){
		
		try{
			this.commandFromApiJsonDeserializer.validateForCancel(command.json());
			final CreditDistribution creditDistribution = this.creditDistributionRepository.findOne(distributionId);
			/*if(creditDistribution == null){
				throw new CreditDistributionDetailsNotFoundException(distributionId.toString());
			}*/
			 final CreditDistribution cancelCreditDistribution = CreditDistribution.cancelCreditDistributionRequest(creditDistribution);
			 cancelCreditDistribution.cancelDistribution(command);
			 this.creditDistributionRepository.save(cancelCreditDistribution);
			 creditDistribution.cancelDistribution(command);
			 this.creditDistributionRepository.save(creditDistribution);
			 
			 Long invoiceId = creditDistribution.getInvoiceId();
				BigDecimal amount = creditDistribution.getAmount();				
				Invoice invoiceData = this.invoiceRepository.findOne(invoiceId);
				BigDecimal dueAmount = invoiceData.getDueAmount();
				BigDecimal updateAmount = dueAmount.add(amount);
				invoiceData.setDueAmount(updateAmount);
				this.invoiceRepository.saveAndFlush(invoiceData);
				Payment payment=this.paymentRepository.findOne(creditDistribution.getPaymentId());
				if(payment.getInvoiceId() != null)
				payment.setInvoiceId(null);
				this.paymentRepository.save(payment);
			return new CommandProcessingResult(cancelCreditDistribution.getId(),creditDistribution.getClientId());
		}catch(DataIntegrityViolationException exception){
			handleDataIntegrityIssues(command, exception);
			return CommandProcessingResult.empty();
		}
		
	}


	private void handleDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		// TODO Auto-generated method stub
		
	}

}
