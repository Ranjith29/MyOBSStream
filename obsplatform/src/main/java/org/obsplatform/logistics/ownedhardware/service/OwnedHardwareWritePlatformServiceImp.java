package org.obsplatform.logistics.ownedhardware.service;

import java.util.List;
import java.util.Map;

import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.obsplatform.logistics.itemdetails.service.ItemDetailsReadPlatformService;
import org.obsplatform.logistics.ownedhardware.domain.OwnedHardware;
import org.obsplatform.logistics.ownedhardware.domain.OwnedHardwareJpaRepository;
import org.obsplatform.logistics.ownedhardware.exception.OwnHardwareNotFoundException;
import org.obsplatform.logistics.ownedhardware.serialization.OwnedHardwareFromApiJsonDeserializer;
import org.obsplatform.portfolio.allocation.domain.HardwareAssociationRepository;
import org.obsplatform.portfolio.association.data.HardwareAssociationData;
import org.obsplatform.portfolio.association.domain.HardwareAssociation;
import org.obsplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.obsplatform.portfolio.association.service.HardwareAssociationWriteplatformService;
import org.obsplatform.portfolio.order.service.OrderReadPlatformService;
import org.obsplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnedHardwareWritePlatformServiceImp implements OwnedHardwareWritePlatformService {

	private final static Logger logger = (Logger) LoggerFactory.getLogger(OwnedHardwareWritePlatformServiceImp.class);
	private final OwnedHardwareJpaRepository ownedHardwareJpaRepository;
	private final PlatformSecurityContext context;
	private final OwnedHardwareFromApiJsonDeserializer apiJsonDeserializer;
	private final OwnedHardwareReadPlatformService ownedHardwareReadPlatformService;
	private final ItemDetailsReadPlatformService inventoryItemDetailsReadPlatformService;
	private final ConfigurationRepository globalConfigurationRepository;
	private final HardwareAssociationWriteplatformService hardwareAssociationWriteplatformService;
	private final HardwareAssociationReadplatformService associationReadplatformService;
	private final OrderReadPlatformService orderReadPlatformService;
	private final HardwareAssociationRepository associationRepository;
	private final ProvisioningWritePlatformService provisioningWritePlatformService;

	@Autowired
	public OwnedHardwareWritePlatformServiceImp(final OwnedHardwareJpaRepository ownedHardwareJpaRepository,
			final PlatformSecurityContext context,
			final OwnedHardwareFromApiJsonDeserializer apiJsonDeserializer,
			final OwnedHardwareReadPlatformService ownedHardwareReadPlatformService,
			final ItemDetailsReadPlatformService inventoryItemDetailsReadPlatformService,
			final ConfigurationRepository globalConfigurationRepository,
			final HardwareAssociationWriteplatformService hardwareAssociationWriteplatformService,
			final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,
			final OrderReadPlatformService orderReadPlatformService,
			final HardwareAssociationRepository associationRepository,
			final ProvisioningWritePlatformService provisioningWritePlatformService) {

		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.associationRepository = associationRepository;
		this.orderReadPlatformService = orderReadPlatformService;
		this.ownedHardwareJpaRepository = ownedHardwareJpaRepository;
		this.globalConfigurationRepository = globalConfigurationRepository;
		this.provisioningWritePlatformService = provisioningWritePlatformService;
		this.ownedHardwareReadPlatformService = ownedHardwareReadPlatformService;
		this.associationReadplatformService = hardwareAssociationReadplatformService;
		this.inventoryItemDetailsReadPlatformService = inventoryItemDetailsReadPlatformService;
		this.hardwareAssociationWriteplatformService = hardwareAssociationWriteplatformService;

	}

	@Transactional
	@Override
	public CommandProcessingResult createOwnedHardware(final JsonCommand command,final Long clientId) {

		try {
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			final OwnedHardware ownedHardware = OwnedHardware.fromJson(command, clientId);
			List<String> inventorySerialNumbers = inventoryItemDetailsReadPlatformService.retriveSerialNumbers();
			List<String> ownedhardwareSerialNumbers = ownedHardwareReadPlatformService.retriveSerialNumbers();
			String ownedHardwareSerialNumber = ownedHardware.getSerialNumber();

			if (inventorySerialNumbers.contains(ownedHardwareSerialNumber) | ownedhardwareSerialNumbers.contains(ownedHardwareSerialNumber)) {
				throw new PlatformDataIntegrityException("validation.error.msg.ownedhardware.duplicate.serialNumber",
						"validation.error.msg.ownedhardware.duplicate.serialNumber","serialNumber", "");
			}
		
		this.ownedHardwareJpaRepository.save(ownedHardware);
		
		  //For Plan And HardWare Association
		Configuration configurationProperty=this.globalConfigurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_IMPLICIT_ASSOCIATION);
		
		if(configurationProperty.isEnabled()){
			
			
		           List<HardwareAssociationData> allocationDetailsDatas=this.associationReadplatformService.retrieveClientAllocatedPlan(ownedHardware.getClientId(),ownedHardware.getItemType());
		    
		        if(!allocationDetailsDatas.isEmpty()){
		    				this.hardwareAssociationWriteplatformService.createNewHardwareAssociation(ownedHardware.getClientId(),
		    						allocationDetailsDatas.get(0).getPlanId(),ownedHardware.getSerialNumber(),allocationDetailsDatas.get(0).getorderId(),"OWNED",null);
		       }
		    }
		
		return new CommandProcessingResultBuilder().withEntityId(ownedHardware.getId()).withClientId(clientId).build();
		
	}catch(DataIntegrityViolationException dve){
		handleDataIntegrityIssues(command, dve);
		return CommandProcessingResult.empty();

	}
}

	/*
	 * private boolean checkforClientActiveDevices(Long clientId) {
	 * 
	 * boolean isCheck=true;
	 * 
	 * if(configurationProperty.isEnabled()){ int
	 * clientDevices=this.ownedHardwareReadPlatformService
	 * .retrieveClientActiveDevices(clientId);
	 * 
	 * if(clientDevices >= Integer.parseInt(configurationProperty.getValue())){
	 * isCheck=false; } } return isCheck; }
	 */

	private void handleDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {

		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("serialNumber")) {
			final String name = command.stringValueOfParameterNamed("serialNumber");
			throw new PlatformDataIntegrityException("error.msg.serialnumber.duplicate.name",
					"serialnumber with name `" + name + "` already exists","serialnumber", name);
		}

		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "+ realCause.getMessage());
	}

	@Transactional
	@Override
	public CommandProcessingResult updateOwnedHardware(final JsonCommand command,final Long id) {
		
		try {
			
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			OwnedHardware ownedHardware = OwnedHardwareretrieveById(id);
			final String oldHardware = ownedHardware.getProvisioningSerialNumber();
			final String oldSerialnumber = ownedHardware.getSerialNumber();
			final Map<String, Object> changes = ownedHardware.update(command);
			
			List<String> inventorySerialNumbers = inventoryItemDetailsReadPlatformService.retriveSerialNumbers();
			List<String> ownedhardwareSerialNumbers = ownedHardwareReadPlatformService.retriveSerialNumbers();

			if (!oldSerialnumber.equalsIgnoreCase(ownedHardware.getSerialNumber())) {
				final String newSerialNumber = ownedHardware.getSerialNumber();
				if (inventorySerialNumbers.contains(newSerialNumber) | ownedhardwareSerialNumbers.contains(newSerialNumber)) {
					throw new PlatformDataIntegrityException(
							"validation.error.msg.ownedhardware.duplicate.serialNumber",
							"validation.error.msg.ownedhardware.duplicate.serialNumber",
							"serialNumber", "");
				}
			}
			
			if (!changes.isEmpty()) {
				this.ownedHardwareJpaRepository.save(ownedHardware);
			}

			if (!oldHardware.equalsIgnoreCase(ownedHardware.getProvisioningSerialNumber())) {

				this.provisioningWritePlatformService.updateHardwareDetails(ownedHardware.getClientId(),ownedHardware.getSerialNumber(), oldSerialnumber,
						ownedHardware.getProvisioningSerialNumber(),oldHardware);
			}

			return new CommandProcessingResult(id);

		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}
	}

	private OwnedHardware OwnedHardwareretrieveById(final Long id) {

		OwnedHardware ownedHardware = this.ownedHardwareJpaRepository.findOne(id);
		if (ownedHardware == null) {
			throw new OwnHardwareNotFoundException(id.toString());
		}
		return ownedHardware;
	}

	@Override
	public CommandProcessingResult deleteOwnedHardware(final Long id) {

		try {
			this.context.authenticatedUser();
			final OwnedHardware ownedHardware = this.OwnedHardwareretrieveById(id);
			// Check if Active plans are exist
			final Long activeorders = this.orderReadPlatformService.retrieveClientActiveOrderDetails(ownedHardware.getClientId(),ownedHardware.getSerialNumber());
			if (activeorders != 0) {
				throw new ActivePlansFoundException();
			}
			List<HardwareAssociation> hardwareAssociations = this.associationRepository.findOneByserialNo(ownedHardware.getSerialNumber());
			if (!hardwareAssociations.isEmpty()) {
				for (HardwareAssociation hardwareAssociation : hardwareAssociations) {
					hardwareAssociation.delete();
					this.associationRepository.save(hardwareAssociation);
				}
			}
			ownedHardware.delete();
			this.ownedHardwareJpaRepository.save(ownedHardware);

			return new CommandProcessingResult(id);

		} catch (DataIntegrityViolationException exception) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
}
