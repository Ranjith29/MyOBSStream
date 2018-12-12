package org.obsplatform.finance.depositandrefund.service;

import java.math.BigDecimal;

import org.obsplatform.finance.clientbalance.domain.ClientBalance;
import org.obsplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.obsplatform.finance.depositandrefund.domain.DepositAndRefund;
import org.obsplatform.finance.depositandrefund.domain.DepositAndRefundRepository;
import org.obsplatform.finance.depositandrefund.exception.ItemQualityAndStatusException;
import org.obsplatform.finance.depositandrefund.serialization.DepositeCommandFromApiJsonDeserializer;
import org.obsplatform.finance.payments.domain.PaypalEnquireyRepository;
import org.obsplatform.finance.payments.exception.ReceiptNoDuplicateException;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.obsplatform.organisation.partner.domain.PartnerBalanceRepository;
import org.obsplatform.portfolio.order.service.OrderReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundWritePlatformServiceImpl implements RefundWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(RefundWritePlatformServiceImpl.class);
  
	private final PlatformSecurityContext context;
	private final ClientBalanceRepository clientBalanceRepository;
	private final PartnerBalanceRepository partnerBalanceRepository;
	private final DepositAndRefundRepository depositAndRefundRepository;
	private final OrderReadPlatformService orderReadPlatformService;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final DepositeCommandFromApiJsonDeserializer fromApiJsonDeserializer;


	@Autowired
	public RefundWritePlatformServiceImpl(final PlatformSecurityContext context,final ClientBalanceRepository clientBalanceRepository,
			final ConfigurationRepository globalConfigurationRepository,final PaypalEnquireyRepository paypalEnquireyRepository,
			final PartnerBalanceRepository partnerBalanceRepository, final DepositAndRefundRepository depositAndRefundRepository, 
			final OrderReadPlatformService orderReadPlatformService, 
			final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final DepositeCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
		
		this.context = context;
		this.clientBalanceRepository=clientBalanceRepository;
		this.partnerBalanceRepository = partnerBalanceRepository;
		this.depositAndRefundRepository = depositAndRefundRepository;
		this.orderReadPlatformService = orderReadPlatformService;
		this.oneTimeSaleReadPlatformService = oneTimeSaleReadPlatformService;
		this.fromApiJsonDeserializer =  fromApiJsonDeserializer;
		
	}

	@Transactional
	@Override
	public CommandProcessingResult createRefund(final JsonCommand command, Long depositId) {
		
		try {
			
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validaForCreateRefund(command.json());
			final BigDecimal refundAmount = command.bigDecimalValueOfParameterNamed("refundAmount");
			final Long refundMode = command.longValueOfParameterNamed("refundMode");
			DepositAndRefund deposit = this.depositAndRefundRepository.findOne(depositId);
			Long clientId = deposit.getClientId();
			Long itemId = deposit.getItemId();
			Long saleId = deposit.getRefId();
			BigDecimal refundBalance = deposit.getDebitAmount();
			
			
			ClientBalance clientBalance = clientBalanceRepository.findByClientId(clientId);
			BigDecimal clientBalanceAmount = clientBalance.getBalanceAmount();
			
			/*final List<AllocationDetailsData> allocationData = this.oneTimeSaleReadPlatformService.retrieveUnAllocationDetails(saleId, clientId);
			if(!allocationData.isEmpty()){
				
				String serialNumber = allocationData.get(0).getSerialNo();
				String itemQuality = allocationData.get(0).getQuality();
				String hardwareStatus = allocationData.get(0).getHardwareStatus();
				
				final Long activeorders=this.orderReadPlatformService.retrieveClientActiveOrderDetails(clientId,serialNumber);
	    	   	if(activeorders!= 0){
	    	   		throw new ActivePlansFoundException();
	    	   	}
	    	   	if(!itemQuality.equalsIgnoreCase("Good")){
	    	   		throw new ItemQualityAndStatusException(itemQuality);
	    	   	}
	    	   	if(!hardwareStatus.equalsIgnoreCase("unallocated")){
	    	   		throw new ItemQualityAndStatusException();
	    	   	}
			}*/
    	   	
			if(clientBalanceAmount.intValue() == 0){
				processDepositAndRefund(clientId, itemId, refundAmount, "Refund", "Credit", depositId,null);
				//processDepositAndRefund(clientId, itemId, refundAmount, "Payment Towards Refund Entry", "Debit", depositId,refundMode);
				
				// Update Client Balance
				clientBalance.updateBalance("CREDIT",refundBalance,'N');
				this.clientBalanceRepository.saveAndFlush(clientBalance);
				

				deposit.setIsRefund('Y');
				this.depositAndRefundRepository.saveAndFlush(deposit);
				
				
				
				
			}else if(clientBalanceAmount.intValue() <= refundBalance.intValue()){
				//MathContext mc = new MathContext(4);
				BigDecimal amountValue = null;
				if(clientBalanceAmount.intValue() > 0){
					amountValue= refundBalance.subtract(clientBalanceAmount);
					processDepositAndRefund(clientId, itemId, amountValue, "Refund", "Credit", depositId,null);
					//processDepositAndRefund(clientId, itemId, amountValue, "Payment Towards Refund Entry", "Debit", depositId,refundMode);
					processDepositAndRefund(clientId, itemId, clientBalanceAmount, "Refund Adjustment towards Service Balance", "Credit", depositId,null);
					// Update Client Balance
					/*clientBalance.updateBalance("CREDIT",clientBalanceAmount,'N');
					this.clientBalanceRepository.saveAndFlush(clientBalance);*/
				}else{
					
					processDepositAndRefund(clientId, itemId, refundAmount, "Refund", "Credit", depositId,null);
					//processDepositAndRefund(clientId, itemId, refundAmount, "Payment Towards Refund Entry", "Debit", depositId,refundMode);
				}
				
				clientBalance.updateBalance("CREDIT",refundBalance,'N');
				this.clientBalanceRepository.saveAndFlush(clientBalance);
				
				deposit.setIsRefund('Y');
				this.depositAndRefundRepository.saveAndFlush(deposit);
				/*clientBalance.setBalanceAmount(BigDecimal.valueOf(0));
				this.clientBalanceRepository.saveAndFlush(clientBalance);*/
				
			}else{
				throw new ItemQualityAndStatusException(clientBalanceAmount);
			}
			
			
			
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId((long)1).withClientId(command.entityId()).build();

		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
			}
		}

	private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause(); 
		if(realCause.getMessage().contains("receipt_no")){
		          throw new ReceiptNoDuplicateException(command.stringValueOfParameterNamed("receiptNo"));
		}
		
		logger.error(dve.getMessage(), dve);
	}
	
	private void processDepositAndRefund(Long clientId, Long itemId, BigDecimal refundAmount,
				String description, String transType, Long depositId,Long refundMode){
		DepositAndRefund refund = DepositAndRefund.fromJson(clientId, itemId, refundAmount, description, transType);
		refund.setRefId(depositId);
		refund.setRefundMode(refundMode);
		this.depositAndRefundRepository.saveAndFlush(refund);
	}

}




