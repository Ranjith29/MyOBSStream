package org.obsplatform.cms.eventorder.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.obsplatform.cms.eventmaster.domain.EventDetails;
import org.obsplatform.cms.eventmaster.domain.EventDetailsRepository;
import org.obsplatform.cms.eventmaster.domain.EventMaster;
import org.obsplatform.cms.eventmaster.domain.EventMasterRepository;
import org.obsplatform.cms.eventorder.domain.EventOrder;
import org.obsplatform.cms.eventorder.domain.EventOrderRepository;
import org.obsplatform.cms.eventorder.domain.EventOrderdetials;
import org.obsplatform.cms.eventorder.exception.InsufficientAmountException;
import org.obsplatform.cms.eventprice.domain.EventPrice;
import org.obsplatform.cms.eventprice.domain.EventPriceRepository;
import org.obsplatform.cms.journalvoucher.domain.JournalVoucher;
import org.obsplatform.cms.journalvoucher.domain.JournalvoucherRepository;
import org.obsplatform.cms.media.domain.MediaAsset;
import org.obsplatform.cms.media.exceptions.NoEventMasterFoundException;
import org.obsplatform.cms.media.exceptions.NoMoviesFoundException;
import org.obsplatform.cms.mediadetails.domain.MediaAssetRepository;
import org.obsplatform.cms.mediadetails.domain.MediaassetLocation;
import org.obsplatform.cms.mediadetails.exception.NoMediaDeviceFoundException;
import org.obsplatform.cms.mediadevice.data.MediaDeviceData;
import org.obsplatform.cms.mediadevice.service.MediaDeviceReadPlatformService;
import org.obsplatform.finance.clientbalance.domain.ClientBalance;
import org.obsplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.onetimesale.data.OneTimeSaleData;
import org.obsplatform.logistics.onetimesale.serialization.EventOrderCommandFromApiJsonDeserializer;
import org.obsplatform.logistics.onetimesale.service.InvoiceOneTimeSale;
import org.obsplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.obsplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.obsplatform.workflow.eventvalidation.service.EventValidationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventOrderWriteplatformServiceImpl implements EventOrderWriteplatformService {

	private final PlatformSecurityContext context;
	private final ClientRepository clientRepository;
	private final InvoiceOneTimeSale invoiceOneTimeSale;
	private final MediaAssetRepository mediaAssetRepository;
	private final EventOrderRepository eventOrderRepository;
	private final EventMasterRepository eventMasterRepository;
	private final EventDetailsRepository eventDetailsRepository;
	private final EventValidationReadPlatformService eventValidationReadPlatformService;
	private final EventPriceRepository eventPricingRepository;
	private final JournalvoucherRepository journalvoucherRepository;
	private final ClientBalanceRepository clientBalanceRepository;
	private final ConfigurationRepository configurationRepository; 
	private final MediaDeviceReadPlatformService deviceReadPlatformService;
	private final EventOrderReadplatformServie eventOrderReadplatformServie;
	private final EventOrderCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;

	@Autowired
	public EventOrderWriteplatformServiceImpl(final PlatformSecurityContext context,final EventOrderRepository eventOrderRepository,
			final EventOrderCommandFromApiJsonDeserializer apiJsonDeserializer,final EventOrderReadplatformServie eventOrderReadplatformServie,
			final InvoiceOneTimeSale invoiceOneTimeSale,final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final EventMasterRepository eventMasterRepository,final MediaAssetRepository mediaAssetRepository,final EventPriceRepository eventPricingRepository,
			final MediaDeviceReadPlatformService deviceReadPlatformService,final EventDetailsRepository eventDetailsRepository,
			final ConfigurationRepository configurationRepository,final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
			final ActiondetailsWritePlatformService actiondetailsWritePlatformService,final ClientBalanceRepository clientBalanceRepository,
			final ClientRepository clientRepository,final EventValidationReadPlatformService eventValidationReadPlatformService,
			final JournalvoucherRepository journalvoucherRepository) {
		
		this.context = context;
		this.configurationRepository=configurationRepository;
		this.deviceReadPlatformService = deviceReadPlatformService;
		this.eventValidationReadPlatformService=eventValidationReadPlatformService;
		this.journalvoucherRepository=journalvoucherRepository;
		this.eventOrderReadplatformServie = eventOrderReadplatformServie;
		this.clientRepository=clientRepository;
		this.invoiceOneTimeSale = invoiceOneTimeSale;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.mediaAssetRepository = mediaAssetRepository;
		this.eventOrderRepository = eventOrderRepository;
		this.clientBalanceRepository=clientBalanceRepository;
		this.eventDetailsRepository=eventDetailsRepository;
		this.eventMasterRepository = eventMasterRepository;
		this.eventPricingRepository = eventPricingRepository;
		this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
		this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;

	}

	private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		// TODO Auto-generated method stub

	}

	@Transactional
	@Override
	public CommandProcessingResult createEventOrder(JsonCommand command) {
		
		try {
			
			this.context.authenticatedUser();
			Long clientId = command.longValueOfParameterNamed("clientId");
			
			//Check Client Custome Validation
			this.eventValidationReadPlatformService.checkForCustomValidations(clientId,EventActionConstants.EVENT_EVENT_ORDER, command.json(),getUserId());
			EventOrder eventOrder=assembleEventOrderDetails(command,clientId);
			Configuration walletConfiguration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_WALLET_ENABLE);
			boolean isBalanceAvailable=this.checkClientBalance(eventOrder.getBookedPrice(), clientId,walletConfiguration.isEnabled());
			 if (!isBalanceAvailable) {
					throw new InsufficientAmountException("bookevent");
				}
			this.eventOrderRepository.save(eventOrder);
			
			List<OneTimeSaleData> oneTimeSaleDatas = eventOrderReadplatformServie.retrieveEventOrderData(eventOrder.getClientId());
			  for (OneTimeSaleData oneTimeSaleData : oneTimeSaleDatas) {
				  CommandProcessingResult commandProcessingResult=this.invoiceOneTimeSale.invoiceOneTimeSale(eventOrder.getClientId(), oneTimeSaleData,walletConfiguration.isEnabled());
				  this.updateOneTimeSale(oneTimeSaleData);
				  if(walletConfiguration.isEnabled()){
					  JournalVoucher journalVoucher=new JournalVoucher(commandProcessingResult.resourceId(),DateUtils.getDateOfTenant(),"Event Sale",null,
							  eventOrder.getBookedPrice(),eventOrder.getClientId());
						this.journalvoucherRepository.save(journalVoucher);
				  }
			}

			  //Add New Action 
			List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_EVENT_ORDER);
			if(!actionDetaislDatas.isEmpty()){
				this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,clientId,eventOrder.getId().toString(),null);
			}		
			return new CommandProcessingResult(eventOrder.getEventOrderdetials().get(0).getMovieLink(),eventOrder.getClientId());
			
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

private EventOrder assembleEventOrderDetails(JsonCommand command, Long clientId) {
	
	Configuration configuration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIR_PROPERTY_REGISTRATION_DEVICE);
	this.apiJsonDeserializer.validateForCreate(command.json(),configuration.isEnabled());
	final Long eventId = command.longValueOfParameterNamed("eventId");
	final String deviceId = command.stringValueOfParameterNamed("deviceId");
	//final Long mediaId = command.longValueOfParameterNamed("mediaId");

	Long clientType=Long.valueOf(0);
	
	if(configuration != null && configuration.isEnabled()){
		MediaDeviceData deviceData = this.deviceReadPlatformService.retrieveDeviceDetails(deviceId);
			if (deviceData == null) {
				throw new NoMediaDeviceFoundException();
			}
			clientId=deviceData.getClientId();
			clientType=deviceData.getClientTypeId();
			
	}else if(clientId != null){
		Client client=this.clientRepository.findOne(clientId);
		clientType=client.getCategoryType();
	}
	
	final String formatType = command.stringValueOfParameterNamed("formatType");
	final String optType=command.stringValueOfParameterNamed("optType");
	EventMaster eventMaster = this.eventMasterRepository.findOne(eventId);
	if(eventMaster == null){
			throw new NoEventMasterFoundException();
	}
	List<EventDetails> eventDetails=eventMaster.getEventDetails();
	EventOrder eventOrder = EventOrder.fromJson(command,eventMaster,clientType);
	for(EventDetails detail:eventDetails){
		EventDetails eventDetail=this.eventDetailsRepository.findOne(detail.getId());
		//if(eventDetail.getMediaId().equals(mediaId)){
			MediaAsset mediaAsset = this.mediaAssetRepository.findOne(eventDetail.getMediaId());
			List<MediaassetLocation> mediaassetLocations = mediaAsset.getMediaassetLocations();
			String movieLink = "";
			for (MediaassetLocation location : mediaassetLocations) {
				if (location.getFormatType().equalsIgnoreCase(formatType)) {
					movieLink = location.getLocation();
				}
			}
			EventOrderdetials eventOrderdetials=new EventOrderdetials(eventDetail,movieLink,formatType,optType);
			eventOrder.addEventOrderDetails(eventOrderdetials);
				if (movieLink.isEmpty()) {
					throw new NoMoviesFoundException();
				}
			//}
		}
	
		return eventOrder;
}

private Long getUserId() {
	Long userId=null;
	SecurityContext context = SecurityContextHolder.getContext();
	if(context.getAuthentication() != null){
		AppUser appUser=this.context.authenticatedUser();
		userId=appUser.getId();
	}else {
		userId=new Long(0);
	}
	
	return userId;
}
	
	@Override
	public CommandProcessingResult updateEventOrderPrice(JsonCommand command) {
		
			Long id = eventOrderReadplatformServie.getCurrentRow(command.stringValueOfParameterNamed("formatType"), 
						command.stringValueOfParameterNamed("optType"), command.longValueOfParameterNamed("clientId"));
			EventPrice eventPricing = eventPricingRepository.findOne(id);
			eventPricing.setPrice(Double.valueOf(command.stringValueOfParameterNamed("price")));
			eventPricingRepository.save(eventPricing);
			return new CommandProcessingResultBuilder().withResourceIdAsString(eventPricing.getPrice().toString()).build();
	}
	
	@Override
	public boolean checkClientBalance(Double bookedPrice, Long clientId, boolean isWalletEnable) {
		
		  boolean isBalanceAvailable = false;
		  ClientBalance clientBalance=this.clientBalanceRepository.findByClientId(clientId);
		
		  BigDecimal eventPrice=new BigDecimal(bookedPrice);
		  if(clientBalance!=null){
			  if(isWalletEnable){
				  BigDecimal resultantBalance = clientBalance.getWalletAmount().add(eventPrice);
				  	if(resultantBalance.compareTo(BigDecimal.ZERO) == -1 || resultantBalance.compareTo(BigDecimal.ZERO) == 0){
			  			isBalanceAvailable = true;   
				  	}else {
				  			isBalanceAvailable = false;
				  	}
			  }else{
			     isBalanceAvailable = true;
			  }
		  }
		 
		  return isBalanceAvailable;
	
	}
	public void updateOneTimeSale(OneTimeSaleData oneTimeSaleData) {
		EventOrder oneTimeSale = eventOrderRepository.findOne(oneTimeSaleData.getId());
		oneTimeSale.setInvoiced();
		eventOrderRepository.save(oneTimeSale);

	}
}