package org.obsplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.finance.billingorder.commands.BillingOrderCommand;
import org.obsplatform.finance.billingorder.domain.BillingOrder;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.clientbalance.domain.ClientBalance;
import org.obsplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.organisation.office.domain.Office;
import org.obsplatform.organisation.office.domain.OfficeAdditionalInfo;
import org.obsplatform.organisation.office.domain.OfficeAdditionalInfoRepository;
import org.obsplatform.organisation.office.domain.OfficeCommision;
import org.obsplatform.organisation.office.domain.OfficeCommisionRepository;
import org.obsplatform.organisation.partner.domain.OfficeControlBalance;
import org.obsplatform.organisation.partner.domain.PartnerBalanceRepository;
import org.obsplatform.organisation.partner.data.AgreementData;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderPrice;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillingOrderWritePlatformServiceImplementation implements BillingOrderWritePlatformService {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BillingOrderWritePlatformServiceImplementation.class);

	private final OrderRepository orderRepository;
	private final ClientBalanceRepository clientBalanceRepository;
	private final ClientRepository clientRepository;
	private final PartnerBalanceRepository partnerBalanceRepository;
	private final OfficeAdditionalInfoRepository infoRepository;
	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final OfficeCommisionRepository officeCommisionRepository;
	
	@Autowired
	public BillingOrderWritePlatformServiceImplementation(final OrderRepository orderRepository,
			final ClientBalanceRepository clientBalanceRepository,
			final ClientRepository clientRepository,
			final PartnerBalanceRepository partnerBalanceRepository,
			final OfficeAdditionalInfoRepository infoRepository,
			final BillingOrderReadPlatformService billingOrderReadPlatformService,
			final OfficeCommisionRepository officeCommisionRepository){

		this.orderRepository = orderRepository;
		this.clientBalanceRepository = clientBalanceRepository;
		this.clientRepository = clientRepository;
		this.partnerBalanceRepository = partnerBalanceRepository;
		this.infoRepository = infoRepository;
		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.officeCommisionRepository = officeCommisionRepository;
	}
	
	
	@Override
	public CommandProcessingResult updateBillingOrder(List<BillingOrderCommand> commands) {
		Order clientOrder = null;
		
		for (BillingOrderCommand billingOrderCommand : commands) {
			
			clientOrder = this.orderRepository.findOne(billingOrderCommand.getClientOrderId());
				if (clientOrder != null ) {
					
						clientOrder.setNextBillableDay(billingOrderCommand.getNextBillableDate());
						List<OrderPrice> orderPrices = clientOrder.getPrice();
						
						for (OrderPrice orderPriceData : orderPrices) {
							
						    if(billingOrderCommand.getOrderPriceId().equals(orderPriceData.getId())){
						    	
							orderPriceData.setInvoiceTillDate(billingOrderCommand.getInvoiceTillDate());
							orderPriceData.setNextBillableDay(billingOrderCommand.getNextBillableDate());
						
						}
					}
				}
				this.orderRepository.saveAndFlush(clientOrder);
		}
	
		return new CommandProcessingResult(Long.valueOf(clientOrder.getId()));
	}


	@Override
	public void updateClientVoucherBalance(BigDecimal amount,Long clientId,boolean isWalletEnable) {

		
		BigDecimal balance=null;
		ClientBalance clientBalance = this.clientBalanceRepository.findByClientId(clientId);
		
		if(clientBalance == null){
			clientBalance =new ClientBalance(clientId,amount,isWalletEnable?'Y':'N');
		}else{
			if(isWalletEnable){
				balance=clientBalance.getWalletAmount().add(amount);
				clientBalance.setWalletAmount(balance);
				
			}else{
				balance=clientBalance.getBalanceAmount().add(amount);
				clientBalance.setBalanceAmount(balance);
			}

		}

		this.clientBalanceRepository.saveAndFlush(clientBalance);
		
	}
	
	
	@Override
	public void updateClientBalance(BigDecimal amount, Long clientId,boolean isWalletEnable) {
		
		BigDecimal balance=null;
		
		ClientBalance clientBalance = this.clientBalanceRepository.findByClientId(clientId);
		
		if(clientBalance == null){
			clientBalance =new ClientBalance(clientId, amount, isWalletEnable?'Y':'N');
		}else{
			if(isWalletEnable){
				balance=clientBalance.getWalletAmount().add(amount);
				clientBalance.setWalletAmount(balance);
				
			}else{
				balance=clientBalance.getBalanceAmount().add(amount);
				clientBalance.setBalanceAmount(balance);
			}
			
		}

		this.clientBalanceRepository.saveAndFlush(clientBalance);
		
		final Client client = this.clientRepository.findOne(clientId);
		final OfficeAdditionalInfo officeAdditionalInfo = this.infoRepository.findoneByoffice(client.getOffice());
		if (officeAdditionalInfo != null) {
			if (officeAdditionalInfo.getIsCollective()) {
				this.updatePartnerBalance(client.getOffice(), amount);

			}
		}

	}

	private void updatePartnerBalance(final Office office,final BigDecimal amount) {

		final String accountType = "INVOICE";
		OfficeControlBalance partnerControlBalance = this.partnerBalanceRepository.findOneWithPartnerAccount(office.getId(), accountType);
		if (partnerControlBalance != null) {
			partnerControlBalance.update(amount, office.getId());

		} else {
			partnerControlBalance = OfficeControlBalance.create(amount, accountType,office.getId());

		}

		this.partnerBalanceRepository.save(partnerControlBalance);
	}

	@Override
	public void UpdateOfficeCommision(Invoice invoice, Long agreementId) {

		List<BillingOrder> charges = invoice.getCharges();

		for (BillingOrder charge : charges) {
      
			AgreementData data = this.billingOrderReadPlatformService.retrieveOfficeChargesCommission(charge.getId());
			if (data != null) {
				OfficeCommision commisionData = OfficeCommision.fromJson(data);
				this.officeCommisionRepository.save(commisionData);
			}else{}
           
		}
	}


}
