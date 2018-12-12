package org.obsplatform.provisioning.wifimaster.service;

import java.util.Map;

import javax.transaction.Transactional;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.provisioning.wifimaster.domain.WifiMaster;
import org.obsplatform.provisioning.wifimaster.domain.WifiMasterRepository;
import org.obsplatform.provisioning.wifimaster.serialization.WifiMasterCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * @author anil
 *
 */
@Service
public class WifiMasterWritePlatformServiceImp implements WifiMasterWritePlatformService{

	private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(WifiMasterWritePlatformService.class);
	
	private final PlatformSecurityContext context;
	private final WifiMasterCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final WifiMasterRepository wifiMasterRepository;
	private final OrderRepository orderRepository;
	
	@Autowired
	public WifiMasterWritePlatformServiceImp(
			final PlatformSecurityContext context,
			final WifiMasterCommandFromApiJsonDeserializer apiJsonDeserializer,
			final WifiMasterRepository wifiMasterRepository,
			final OrderRepository orderRepository){
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.wifiMasterRepository = wifiMasterRepository;
		this.orderRepository = orderRepository;
	}
	
	
	@Transactional
	@Override
	public CommandProcessingResult updateWifi(final JsonCommand command,final Long id){
		WifiMaster wifimaster = null;
		try{
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command);
			wifimaster = retrieveWifiById(id);
			final Map<String, Object> changes = wifimaster.update(command);
			
			if(!changes.isEmpty()){
				this.wifiMasterRepository.saveAndFlush(wifimaster);
			}
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(wifimaster.getId())
					.with(changes).build();
		}catch(final DataIntegrityViolationException dve){
			
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	
	}
	
	 private WifiMaster retrieveWifiById(Long id) {
		 
	        final WifiMaster wifimaster = this.wifiMasterRepository.findOne(id);
	        
	        return wifimaster;
	    }
	 /*private WifiMaster retrieveWifiByOrderId(Long orderId) {
		 
	        final WifiMaster wifimaster = this.wifiMasterRepository.findOneByOrderId(orderId);
	        return wifimaster;
	    }*/
	
	private void handleDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {

      final Throwable realCause = dve.getMostSpecificCause();
      
      LOGGER.error(dve.getMessage(), dve);  
      
	  	if (realCause.getMessage().contains("uq_wd_orderId")) {
	
			final Long orderId = command.longValueOfParameterNamed("orderId");
			throw new PlatformDataIntegrityException("error.msg.client.duplicate.orderId",
					"WIFI Details with orderId `" + orderId	+ "` already exists", "orderId", orderId);
		}
       
    	   throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
					"Unknown data integrity issue with resource: "+ dve.getMessage());
       }
	
	@Override
	public CommandProcessingResult createWifi(JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			
			this.apiJsonDeserializer.validateForCreate(command);
			WifiMaster wifimaster = WifiMaster.fromJson(command);
    		this.wifiMasterRepository.save(wifimaster);
			
    		return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(wifimaster.getId()).build();
    		
		}catch(final DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

	
	@Override
	public CommandProcessingResult deleteWifi(Long id) {
		
		WifiMaster wifiMaster = retrieveWifiById(id);
		
		wifiMaster.delete();
		
		return new CommandProcessingResultBuilder().withEntityId(id).build();		
	}


	@Override
	public CommandProcessingResult UpdateWifiByOrderId(JsonCommand command,Long clientId,Long orderId) {
		this.apiJsonDeserializer.validateForCreate(command);
		String ssid = command.stringValueOfParameterNamed("ssid");
		String wifiPassword = command.stringValueOfParameterNamed("wifiPassword");
		WifiMaster wifimaster = this.wifiMasterRepository.findOneByOrderId(clientId, orderId);
		Order order = this.orderRepository.findOne(orderId);
		if(order.getPlanId()==1 || order.getPlanId()==2 || order.getPlanId()==6 || order.getPlanId()==7){
			if (wifimaster != null) {
				wifimaster.setSsid(ssid);
				wifimaster.setWifiPassword(wifiPassword);
				this.wifiMasterRepository.saveAndFlush(wifimaster);
			} else {
				wifimaster = new WifiMaster(clientId, ssid, wifiPassword, null, orderId, null);
				this.wifiMasterRepository.save(wifimaster);
			}
		    return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(wifimaster.getId()).build();
		}else {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
}
