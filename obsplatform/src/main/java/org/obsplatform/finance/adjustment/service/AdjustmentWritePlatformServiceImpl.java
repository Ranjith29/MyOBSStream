package org.obsplatform.finance.adjustment.service;

import java.math.BigDecimal;

import org.obsplatform.finance.adjustment.domain.Adjustment;
import org.obsplatform.finance.adjustment.domain.AdjustmentRepository;
import org.obsplatform.finance.adjustment.serializer.AdjustmentCommandFromApiJsonDeserializer;
import org.obsplatform.finance.clientbalance.domain.ClientBalance;
import org.obsplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.obsplatform.finance.clientbalance.service.ClientBalanceReadPlatformService;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.office.domain.Office;
import org.obsplatform.organisation.office.domain.OfficeAdditionalInfo;
import org.obsplatform.organisation.office.domain.OfficeAdditionalInfoRepository;
import org.obsplatform.organisation.partner.domain.OfficeControlBalance;
import org.obsplatform.organisation.partner.domain.PartnerBalanceRepository;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdjustmentWritePlatformServiceImpl implements AdjustmentWritePlatformService {

	private final PlatformSecurityContext context;
	private final AdjustmentRepository adjustmentRepository;
	private final ClientBalanceRepository clientBalanceRepository;
	private final AdjustmentCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ClientRepository clientRepository;
	private final PartnerBalanceRepository partnerBalanceRepository;
	private final OfficeAdditionalInfoRepository infoRepository;

	@Autowired
	public AdjustmentWritePlatformServiceImpl(final PlatformSecurityContext context,final AdjustmentRepository adjustmentRepository,
			final ClientBalanceRepository clientBalanceRepository,final AdjustmentCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final ClientBalanceReadPlatformService clientBalanceReadPlatformService,
			final AdjustmentReadPlatformService adjustmentReadPlatformService,final ClientRepository clientRepository,
			final PartnerBalanceRepository partnerBalanceRepository,final OfficeAdditionalInfoRepository infoRepository) {
		
		this.context = context;
		this.adjustmentRepository = adjustmentRepository;
		this.clientBalanceRepository = clientBalanceRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.clientRepository = clientRepository;
		this.partnerBalanceRepository = partnerBalanceRepository;
		this.infoRepository = infoRepository;
	}


	@Transactional
	@Override
	public CommandProcessingResult createAdjustment(final JsonCommand command) {

		try {
			
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			Adjustment adjustment = Adjustment.fromJson(command);
			ClientBalance clientBalance = null;
			BigDecimal balance=BigDecimal.ZERO;
			clientBalance = clientBalanceRepository.findByClientId(adjustment.getClientId());
			this.adjustmentRepository.saveAndFlush(adjustment);
			
			boolean isWalletPayment=command.booleanPrimitiveValueOfParameterNamed("isWalletPayment");
			if (clientBalance != null) {

				clientBalance.updateBalance(adjustment.getAdjustmentType(),adjustment.getAmountPaid(),isWalletPayment?'Y':'N');
			} else if (clientBalance == null) {
				
				   if("CREDIT".equalsIgnoreCase(adjustment.getAdjustmentType())){
				       balance=BigDecimal.ZERO.subtract(adjustment.getAmountPaid());
				   }else{
					    balance=BigDecimal.ZERO.add(adjustment.getAmountPaid());
				   }
					   
					clientBalance =ClientBalance.create(adjustment.getClientId(),balance,isWalletPayment?'Y':'N');
			}

			this.adjustmentRepository.saveAndFlush(adjustment);
			
			final Client client = this.clientRepository.findOne(adjustment.getClientId());
			final OfficeAdditionalInfo officeAdditionalInfo = this.infoRepository.findoneByoffice(client.getOffice());
			if (officeAdditionalInfo != null) {
				if (officeAdditionalInfo.getIsCollective()) {
					System.out.println(officeAdditionalInfo.getIsCollective());
					this.updatePartnerBalance(client.getOffice(), adjustment);
				}
			}
			return new CommandProcessingResult(adjustment.getId());
		} catch (DataIntegrityViolationException dve) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	
	
	private void updatePartnerBalance(final Office office,final Adjustment adjustment) {

		final String accountType = "ADJUSTMENTS";
		OfficeControlBalance partnerControlBalance = this.partnerBalanceRepository.findOneWithPartnerAccount(office.getId(), accountType);
		if (partnerControlBalance != null) {
			if(adjustment.getAdjustmentType().equalsIgnoreCase("CREDIT")){
				partnerControlBalance.update(adjustment.getAmountPaid().negate(), office.getId());
			}else{
				partnerControlBalance.update(adjustment.getAmountPaid(), office.getId());
			}

		} else {
		  if(adjustment.getAdjustmentType().equalsIgnoreCase("CREDIT")){
			  partnerControlBalance = OfficeControlBalance.create(adjustment.getAmountPaid().negate(), accountType,office.getId());
		  }else{
			  partnerControlBalance = OfficeControlBalance.create(adjustment.getAmountPaid(), accountType,office.getId());
		}
	}
		this.partnerBalanceRepository.save(partnerControlBalance);
	}
}
