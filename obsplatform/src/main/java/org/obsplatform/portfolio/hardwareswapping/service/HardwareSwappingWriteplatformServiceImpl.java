package org.obsplatform.portfolio.hardwareswapping.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.logistics.item.domain.ItemMaster;
import org.obsplatform.logistics.item.domain.ItemRepository;
import org.obsplatform.logistics.itemdetails.domain.ItemDetails;
import org.obsplatform.logistics.itemdetails.domain.ItemDetailsAllocation;
import org.obsplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.obsplatform.logistics.itemdetails.exception.SerialNumberNotFoundException;
import org.obsplatform.logistics.itemdetails.service.ItemDetailsWritePlatformService;
import org.obsplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.obsplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.obsplatform.logistics.ownedhardware.domain.OwnedHardware;
import org.obsplatform.logistics.ownedhardware.domain.OwnedHardwareJpaRepository;
import org.obsplatform.organisation.hardwareplanmapping.data.HardwarePlanData;
import org.obsplatform.organisation.hardwareplanmapping.service.HardwarePlanReadPlatformService;
import org.obsplatform.portfolio.allocation.service.AllocationReadPlatformService;
import org.obsplatform.portfolio.association.data.AssociationData;
import org.obsplatform.portfolio.association.data.HardwareAssociationData;
import org.obsplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.obsplatform.portfolio.association.service.HardwareAssociationWriteplatformService;
import org.obsplatform.portfolio.hardwareswapping.exception.WarrantyEndDateExpireException;
import org.obsplatform.portfolio.hardwareswapping.serialization.HardwareSwappingCommandFromApiJsonDeserializer;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderHistory;
import org.obsplatform.portfolio.order.domain.OrderHistoryRepository;
import org.obsplatform.portfolio.order.domain.OrderLine;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.domain.StatusTypeEnum;
import org.obsplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.obsplatform.portfolio.plan.domain.Plan;
import org.obsplatform.portfolio.plan.domain.PlanRepository;
import org.obsplatform.portfolio.planmapping.domain.PlanMapping;
import org.obsplatform.portfolio.planmapping.domain.PlanMappingRepository;
import org.obsplatform.portfolio.property.data.PropertyDeviceMappingData;
import org.obsplatform.portfolio.property.domain.PropertyDeviceMapping;
import org.obsplatform.portfolio.property.domain.PropertyDeviceMappingRepository;
import org.obsplatform.portfolio.property.domain.PropertyHistoryRepository;
import org.obsplatform.portfolio.property.domain.PropertyMaster;
import org.obsplatform.portfolio.property.domain.PropertyMasterRepository;
import org.obsplatform.portfolio.property.domain.PropertyTransactionHistory;
import org.obsplatform.portfolio.property.service.PropertyReadPlatformService;
import org.obsplatform.portfolio.service.domain.ServiceMaster;
import org.obsplatform.portfolio.service.domain.ServiceMasterRepository;
import org.obsplatform.portfolio.servicemapping.data.ServiceMappingData;
import org.obsplatform.portfolio.servicemapping.domain.ServiceMapping;
import org.obsplatform.portfolio.servicemapping.domain.ServiceMappingRepository;
import org.obsplatform.portfolio.servicemapping.service.ServiceMappingReadPlatformService;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hugo
 *
 */
@Service
public class HardwareSwappingWriteplatformServiceImpl implements HardwareSwappingWriteplatformService {
	
	
	private final static Logger LOGGER = LoggerFactory.getLogger(HardwareSwappingWriteplatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final HardwareAssociationWriteplatformService associationWriteplatformService;
	private final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService;
	private final OrderRepository orderRepository;
	private final PlanRepository  planRepository;
	private final HardwareSwappingCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final OrderHistoryRepository orderHistoryRepository;
	private final ConfigurationRepository globalConfigurationRepository;
	private final OwnedHardwareJpaRepository hardwareJpaRepository;
	private final HardwareAssociationReadplatformService associationReadplatformService;
	private final ItemRepository itemRepository;
	private final ItemDetailsRepository itemDetailsRepository;
	private final PropertyDeviceMappingRepository propertyDeviceMappingRepository;
	private final HardwareSwappingReadplatformService hardwareSwappingReadplatformService;
	private final PropertyHistoryRepository propertyHistoryRepository;
	private final ProcessRequestRepository processRequestRepository;
	private final PlanMappingRepository planMappingRepository;
	private final ServiceMappingRepository serviceMappingRepository;
	private final ServiceMasterRepository serviceMasterRepository;
	private final AllocationReadPlatformService allocationReadPlatformService;
	private final PropertyReadPlatformService propertyReadPlatformService;
	private final PropertyMasterRepository propertyMasterRepository;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final HardwarePlanReadPlatformService hardwarePlanReadPlatformService;
	private final ServiceMappingReadPlatformService serviceMappingReadPlatformService;
	  
	@Autowired
	public HardwareSwappingWriteplatformServiceImpl(final PlatformSecurityContext context,final HardwareAssociationWriteplatformService associationWriteplatformService,
			final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService,final OrderRepository orderRepository,final PlanRepository planRepository,
			final HardwareSwappingCommandFromApiJsonDeserializer apiJsonDeserializer,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,final OrderHistoryRepository orderHistoryRepository,
			final ConfigurationRepository configurationRepository,final OwnedHardwareJpaRepository hardwareJpaRepository,
			final HardwareAssociationReadplatformService associationReadplatformService,final ItemRepository itemRepository,
			final ItemDetailsRepository itemDetailsRepository,final PropertyDeviceMappingRepository propertyDeviceMappingRepository,
			final HardwareSwappingReadplatformService hardwareSwappingReadplatformService,final PropertyHistoryRepository propertyHistoryRepository,
			final ProcessRequestRepository processRequestRepository,final PlanMappingRepository planMappingRepository,
			final ServiceMappingRepository serviceMappingRepository,final ServiceMasterRepository serviceMasterRepository,
			final AllocationReadPlatformService allocationReadPlatformService,final PropertyReadPlatformService propertyReadPlatformService,
			final PropertyMasterRepository propertyMasterRepository,
			final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final HardwarePlanReadPlatformService hardwarePlanReadPlatformService ,
			final ServiceMappingReadPlatformService serviceMappingReadPlatformService) {
 
		this.context=context;
		this.associationWriteplatformService=associationWriteplatformService;
		this.inventoryItemDetailsWritePlatformService=inventoryItemDetailsWritePlatformService;
		this.orderRepository=orderRepository;
		this.planRepository=planRepository;
		this.propertyDeviceMappingRepository = propertyDeviceMappingRepository;
		this.fromApiJsonDeserializer=apiJsonDeserializer;
		this.commandSourceWritePlatformService=commandSourceWritePlatformService;
		this.orderHistoryRepository=orderHistoryRepository;
		this.globalConfigurationRepository=configurationRepository;
		this.hardwareJpaRepository=hardwareJpaRepository;
		this.associationReadplatformService=associationReadplatformService;
		this.itemRepository=itemRepository;
		this.itemDetailsRepository = itemDetailsRepository;
		this.hardwareSwappingReadplatformService = hardwareSwappingReadplatformService;
		this.propertyHistoryRepository = propertyHistoryRepository;
		this.processRequestRepository = processRequestRepository;
		this.planMappingRepository = planMappingRepository;
		this.serviceMappingRepository = serviceMappingRepository;
		this.serviceMasterRepository = serviceMasterRepository;
		this.allocationReadPlatformService = allocationReadPlatformService;
		this.propertyReadPlatformService = propertyReadPlatformService;
		this.propertyMasterRepository = propertyMasterRepository;
		this.oneTimeSaleReadPlatformService=oneTimeSaleReadPlatformService;
		this.hardwarePlanReadPlatformService=hardwarePlanReadPlatformService;
		this.serviceMappingReadPlatformService=serviceMappingReadPlatformService;

	}
	
	
	
/* (non-Javadoc)
 * @see #doHardWareSwapping(java.lang.Long, JsonCommand)
 */
@Transactional
@Override
public CommandProcessingResult doHardWareSwapping(final Long entityId,final JsonCommand command) {
		
	try{
		final Long userId=this.context.authenticatedUser().getId();
		this.fromApiJsonDeserializer.validateForCreate(command.json());
		final String serialNo=command.stringValueOfParameterNamed("serialNo");
		final String deviceAgrementType=command.stringValueOfParameterNamed("deviceAgrementType");
		final Long saleId=command.longValueOfParameterNamed("saleId");
		final String provisionNum=command.stringValueOfParameterNamed("provisionNum");
		
     
		
		if(this.hardwareSwappingReadplatformService.retrieveingDisconnectionOrders(serialNo)){
			throw new PlatformDataIntegrityException("error.msg.serialNumber.unpaire.already.discon.orders", 
					"Disconnection Orders already Associated with this serialNumber `" + serialNo
                    + "` Please unpaire first", "Disconnection Orders Already Associated with this serialNumber", serialNo);
		}
		
		if(this.hardwareSwappingReadplatformService.retrieveingPendingOrders(serialNo)){
			throw new PlatformDataIntegrityException("error.msg.serialNumber.pend.orders.request.alreadysent", 
					"Please wait until pending state orders to be active", "Provisioning Request was sent to activation ", serialNo);
		}
		
		//getting new serial number item details data old data by sno
		ItemDetails newSerailNoItemData = this.itemDetailsRepository.getInventoryItemDetailBySerialNum(provisionNum);//old 
		
		if(newSerailNoItemData == null){
			throw new SerialNumberNotFoundException(provisionNum);
		 }
		
		
		//getting old serial number item details data 
		ItemDetails oldSerailNoItemData = this.itemDetailsRepository.getInventoryItemDetailBySerialNum(serialNo);
		
		//check type mapping ....
		final ItemData OlditemCategoryTypeData = this.oneTimeSaleReadPlatformService.retrieveItemByCategoryTypeByItemId(oldSerailNoItemData.getItemMasterId());
		final ItemData newitemCategoryTypeData = this.oneTimeSaleReadPlatformService.retrieveItemByCategoryTypeByItemId(newSerailNoItemData.getItemMasterId());
		
		
		
		//get plan id and asspiaction details of old device -------plancode
		List<AssociationData> associationData = this.hardwareSwappingReadplatformService.retrievingAllAssociations(entityId,serialNo,Long.valueOf(0)); 
		
		
		if(!associationData.isEmpty()){
			
			
			LOGGER.info("service code ......."+associationData.get(0).getServiceId());
			LOGGER.info("plan code......."+associationData.get(0).getPlanId());
			
			//new item code ----------newitemCategoryTypeData.getItemCode()
			final Plan planData  = this.planRepository.findOne(associationData.get(0).getPlanId());
			
			//get hardware mapping details by itemcode----------list map[ping ---plan+item 
			List<HardwarePlanData> retrieveItems =this.hardwarePlanReadPlatformService.
					retrieveRulebyPlanCodeandItemCode(newitemCategoryTypeData.getItemCode(),planData.getPlanCode());
			
			if(retrieveItems.size()== 0){
				LOGGER.info("if hardware config not fount for device code + paln code ");
				Configuration configurationProperty=this.globalConfigurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SERVICE_DEVICE_MAPPING);
				if(configurationProperty !=null && configurationProperty.isEnabled()){
					LOGGER.info("serice ...... id "+associationData.get(0).getServiceId());
					LOGGER.info(" old serin master id "+oldSerailNoItemData.getItemMasterId());
					LOGGER.info(" new serin master id "+newSerailNoItemData.getItemMasterId());
					LOGGER.info("enter config enable ");
					List<ServiceMappingData> OldconfigData=this.serviceMappingReadPlatformService.retrieveRuleServicewithMultipleItem(associationData.get(0).getServiceId(),oldSerailNoItemData.getItemMasterId());
					LOGGER.info(" old config count record size check"+OldconfigData.size());
					
					List<ServiceMappingData> NewconfigData=this.serviceMappingReadPlatformService.retrieveRuleServicewithMultipleItem(associationData.get(0).getServiceId(),newSerailNoItemData.getItemMasterId());
					
					LOGGER.info("device item check 1 st......"+OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId()));
					LOGGER.info(" new config count record size check"+NewconfigData.size());
					
					if(NewconfigData.isEmpty()){
						LOGGER.info(" config error error ");
						throw new PlatformDataIntegrityException("error.msg.device.types.are.not.same", 
								"Please cofigure in Service mapping to device", "Please cofigure in Service mapping to device", "serialNumber");
						} else if (newSerailNoItemData != null && newSerailNoItemData.getClientId() != null) {
							throw new SerialNumberNotFoundException();
						}
					
				 }else {
					  throw new PlatformDataIntegrityException("error.config.not.enable", "Please Enable config ", "Enable config", "serialNumber");
				   }
				
			}else{
				
				if(OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId())){
					LOGGER.info(" catagory type checked sucessfully ....."+OlditemCategoryTypeData.getItemCategoryId()+"......."+newitemCategoryTypeData.getItemCategoryId());
				}else{
					LOGGER.info(" Not mached  ....."+OlditemCategoryTypeData.getItemCategoryId()+"......."+newitemCategoryTypeData.getItemCategoryId());
					throw new PlatformDataIntegrityException("error.device.catagory.not.same", "Please choose same catagory type", "Please choose same catagory type", "serialNumber");
				}
				
				LOGGER.info(" ......Config Enable in Hard ware.....");
			}
			LOGGER.info(" size is 2.......... "+retrieveItems.size());
			LOGGER.info("1......"+!OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId()));
			LOGGER.info(" size is 2.......... "+(retrieveItems.size() == 0));
			
			// For Plan And HardWare Association 
			// global config for service level mapping 
		    // Configuration configurationProperty=this.globalConfigurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_IMPLICIT_ASSOCIATION);
			// start code --------------
			/*Configuration configurationProperty=this.globalConfigurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SERVICE_DEVICE_MAPPING);
			
			if(configurationProperty !=null && configurationProperty.isEnabled()){
				
				System.out.println("enter config enable ");
				List<ServiceMappingData> OldconfigData=this.serviceMappingReadPlatformService.retrieveRuleServicewithMultipleItem(associationData.get(0).getServiceId(),oldSerailNoItemData.getItemMasterId());
				System.out.println(" old config count record size check"+OldconfigData.size());
				
				List<ServiceMappingData> NewconfigData=this.serviceMappingReadPlatformService.retrieveRuleServicewithMultipleItem(associationData.get(0).getServiceId(),newSerailNoItemData.getItemMasterId());
				
				System.out.println("device item check 1 st......"+OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId()));
				System.out.println(" new config count record size check"+NewconfigData.size());
				
				if(NewconfigData.isEmpty()){
					System.out.println(" config error error ");
					throw new PlatformDataIntegrityException("error.msg.device.types.are.not.same", 
							"Please cofigure in Service mapping to device", "Please cofigure in Service mapping to device", "serialNumber");
					} else if (newSerailNoItemData != null && newSerailNoItemData.getClientId() != null) {
						throw new SerialNumberNotFoundException();
					}
				
			   }else {
				   
				   if(!OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId()) || retrieveItems.size() == 0){
						System.out.println(" config off error ");
						throw new PlatformDataIntegrityException("error.msg.device.types.are.not.same", 
								"Please choose same category devices", "Device swap not possiable-Please choose same category devices", "serialNumber");
						
						} else if (newSerailNoItemData != null && newSerailNoItemData.getClientId() != null) {
							throw new SerialNumberNotFoundException();
						}
				   
				   
			   }*/
			//end code------------
			
			
			/** old one for hw type check
			 *  
			 * if(!OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId()) || retrieveItems.size() == 0){
				System.out.println(" error ");
				throw new PlatformDataIntegrityException("error.msg.device.types.are.not.same", 
						"Please choose same category devices", "Device swap not possiable-Please choose same category devices", "serialNumber");
				
				} else if (newSerailNoItemData != null && newSerailNoItemData.getClientId() != null) {
					throw new SerialNumberNotFoundException();
				}*/
		}
		
		
		/*System.out.println(" plan code......."+associationData.get(0).getPlanId());
		//new item code ----------newitemCategoryTypeData.getItemCode()
		final Plan planData  = this.planRepository.findOne(associationData.get(0).getPlanId());
		
		//get hardware mapping details by itemcode----------list map[ping ---plan+item 
		List<HardwarePlanData> retrieveItems =this.hardwarePlanReadPlatformService.
				retrieveRulebyPlanCodeandItemCode(newitemCategoryTypeData.getItemCode(),planData.getPlanCode());
		System.out.println(" size is 2.......... "+retrieveItems.size());
		
		
		System.out.println("1......"+!OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId()));
		System.out.println(" size is 2.......... "+(retrieveItems.size() == 0));
		
		if(!OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId()) || retrieveItems.size() == 0){
			System.out.println(" error ");
			throw new PlatformDataIntegrityException("error.msg.device.types.are.not.same", 
					"Please choose same category devices", "Device swap not possiable-Please choose same category devices", "serialNumber");
			
			} else if (newSerailNoItemData != null && newSerailNoItemData.getClientId() != null) {
				throw new SerialNumberNotFoundException();
			}*/
		
		
		/*if(!OlditemCategoryTypeData.getItemCategoryId().equals(newitemCategoryTypeData.getItemCategoryId())){
		
		if(!newSerailNoItemData.getItemMasterId().equals(oldSerailNoItemData.getItemMasterId())){
			throw new PlatformDataIntegrityException("error.msg.device.types.are.not.same", 
					"Please choose same category devices", "Device swap not possiable-Please choose same category devices", "serialNumber");
			} else if (newSerailNoItemData != null && newSerailNoItemData.getClientId() != null) {
				throw new SerialNumberNotFoundException();
			}
		
		}else{
			
			throw new PlatformDataIntegrityException("error.msg.device.types.are.not.same", 
					"It can not swap", "Device swap not possiable", "serialNumber");
		}*/
		
	//	List<AssociationData> associationData = this.hardwareSwappingReadplatformService.retrievingAllAssociations(entityId,serialNo,Long.valueOf(0));
		LinkedHashSet<Long> associationOrderList = new  LinkedHashSet<Long>();
		
		
		List<AssociationData> associationDataRemove = new ArrayList<AssociationData>();
		//DeAssociate Hardware
		for(AssociationData association : associationData){
			
			this.associationWriteplatformService.deAssociationHardware(association.getId());
			Order order=this.orderRepository.findOne(association.getOrderId());
			if(order.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())){
			    associationOrderList.add(association.getOrderId());
			}else{
				associationDataRemove.add(association);
				System.out.println("size"+associationDataRemove.size());
				//associationData.remove(association);//remove all orders which are not active status
			}
			
		}
		associationDataRemove.removeAll(associationDataRemove);
	    
	    LocalDate newWarrantyDate = new LocalDate(newSerailNoItemData.getWarrantyDate());
		
		if(deviceAgrementType.equalsIgnoreCase(ConfigurationConstants.CONFIR_PROPERTY_OWN)){
			
			OwnedHardware ownedHardware=this.hardwareJpaRepository.findBySerialNumber(serialNo);
			ownedHardware.updateSerialNumbers(provisionNum);
			this.hardwareJpaRepository.saveAndFlush(ownedHardware);
			
			final ItemMaster itemMaster=this.itemRepository.findOne(Long.valueOf(ownedHardware.getItemType()));
	        List<HardwareAssociationData> allocationDetailsDatas=this.associationReadplatformService.retrieveClientAllocatedPlan(ownedHardware.getClientId(),itemMaster.getItemCode());
	        if(!allocationDetailsDatas.isEmpty()){
	    				this.associationWriteplatformService.createNewHardwareAssociation(ownedHardware.getClientId(),allocationDetailsDatas.get(0).getPlanId(),
	    						ownedHardware.getSerialNumber(),allocationDetailsDatas.get(0).getorderId(),"ALLOT",null);
	        }
	        
	   }else{
		
		//DeAllocate HardWare
		ItemDetailsAllocation inventoryItemDetailsAllocation=this.inventoryItemDetailsWritePlatformService.deAllocateHardware(serialNo, entityId, command.entityName());
		
		 JSONObject allocation = new JSONObject();
		 JSONObject allocation1 = new JSONObject();
		 JSONArray  serialNumber=new JSONArray();
		  
		// allocation.put("itemMasterId",inventoryItemDetailsAllocation.getItemMasterId());
		 allocation.put("itemMasterId",newSerailNoItemData.getItemMasterId());
		 allocation.put("clientId",entityId);
		 allocation.put("orderId",saleId);
		 allocation.put("serialNumber",provisionNum);
		 allocation.put("status","allocated");
		 allocation.put("isNewHw","N");
		 
		 serialNumber.put(allocation);
		 allocation1.put("quantity",1);
		// allocation1.put("itemId",inventoryItemDetailsAllocation.getItemMasterId()); 
		// update item id in assoiaction by new item id while device swap 
		 allocation1.put("itemId",newSerailNoItemData.getItemMasterId());
		 allocation1.put("serialNumber",serialNumber);
		 
		//ReAllocate HardWare
			CommandWrapper commandWrapper = new CommandWrapperBuilder().allocateHardware().withJson(allocation1.toString()).build();
			this.commandSourceWritePlatformService.logCommandSource(commandWrapper);
		}
		Long resouceId=Long.valueOf(0);
		String provisionSystem="None";
		
			JSONObject jsonObj = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			 
			//for Reassociation With New SerialNumber
			for(AssociationData association : associationData){
				
				List<AssociationData> existingAssociations = this.hardwareSwappingReadplatformService.retrievingAllAssociations(entityId,provisionNum,association.getOrderId());
				if(existingAssociations.isEmpty()){		
					this.associationWriteplatformService.createNewHardwareAssociation(entityId,association.getPlanId(),
						provisionNum,association.getOrderId(),"ALLOT",association.getServiceId());
				}
			}
			
			//PrePare Provisioning Request
			for(Long orderId : associationOrderList){
				final Order order=this.orderRepository.findOne(orderId);
				
				String orderNo = order.getOrderNo();
				if(orderNo != null){
					orderNo = orderNo.replaceFirst("^0*", "");
				}
				//For Order History
				OrderHistory orderHistory=new OrderHistory(Long.valueOf(orderNo),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),resouceId,"DEVICE SWAP",userId,null);
				this.orderHistoryRepository.saveAndFlush(orderHistory);
				
				 final Plan plan  = this.planRepository.findOne(order.getPlanId());
				if(plan.isHardwareReq() == 'Y' && !plan.getProvisionSystem().equalsIgnoreCase("None")){
					provisionSystem = plan.getProvisionSystem();
					List<AllocationDetailsData> detailsData=this.allocationReadPlatformService.getTheHardwareItemDetails(orderId,provisionNum);
					PlanMapping planMapping= this.planMappingRepository.findOneByPlanId(order.getPlanId());
					List<OrderLine> orderLineData=order.getServices();

					JSONObject innerJsonObj = new JSONObject();
					JSONArray innerJsonArray = new JSONArray();
					if(planMapping != null){
						 innerJsonObj.put("planIdentification", planMapping.getPlanIdentification());
						 
					 }
					if(!detailsData.isEmpty()&&1==detailsData.size()&&detailsData.get(0).getServiceId()==null){ //plan level map
						
						 for(OrderLine orderLine:orderLineData){
							 
							 List<ServiceMapping> serviceMappingDetails=this.serviceMappingRepository.findOneByServiceId(orderLine.getServiceId());
						 		ServiceMaster service=this.serviceMasterRepository.findOne(orderLine.getServiceId());
						 		JSONObject subjson = new JSONObject();
						 		subjson.put("serviceName", service !=null ?service.getServiceCode():" ");
							 if(!serviceMappingDetails.isEmpty()){
								 subjson.put("serviceIdentification", serviceMappingDetails.get(0).getServiceIdentification());
							 }
							 innerJsonArray.put(subjson);
						 }
						
					}else if(!detailsData.isEmpty()){
						for(OrderLine orderLine:orderLineData){
							
							for (AllocationDetailsData detail : detailsData) {
								if (detail.getServiceId().equals(orderLine.getServiceId())) {
									List<ServiceMapping> serviceMappingDetails=this.serviceMappingRepository.findOneByServiceId(orderLine.getServiceId());
									ServiceMaster service=this.serviceMasterRepository.findOne(orderLine.getServiceId());
									JSONObject subjson = new JSONObject();
									subjson.put("serviceName", service !=null ?service.getServiceCode():" ");
									if(!serviceMappingDetails.isEmpty()){
										subjson.put("serviceIdentification", serviceMappingDetails.get(0).getServiceIdentification());
									}
									innerJsonArray.put(subjson);
									break;
								}
							}
						}
					}
					innerJsonObj.put("services" ,innerJsonArray);
					jsonArray.put(innerJsonObj);
					 
				}
			}
		
			   LocalDate oldWarrantyDate = new LocalDate(oldSerailNoItemData.getWarrantyDate());
				oldSerailNoItemData.setWarrantyDate(newWarrantyDate);
				newSerailNoItemData.setWarrantyDate(oldWarrantyDate);
				
			 this.itemDetailsRepository.save(oldSerailNoItemData);
			 this.itemDetailsRepository.save(newSerailNoItemData);
			 
			 Configuration globalConfiguration=this.globalConfigurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_PROPERTY_MASTER);
			 
			 if(globalConfiguration.isEnabled()){
				 
				 List<PropertyDeviceMappingData> deviceMapping = this.propertyReadPlatformService.retrievePropertyDeviceMappingData(serialNo);
				 for (PropertyDeviceMappingData mappingdata : deviceMapping){
					 PropertyDeviceMapping propertyDeviceMapping = this.propertyDeviceMappingRepository.findOne(mappingdata.getId());
					 PropertyMaster propertyMaster = this.propertyMasterRepository.findoneByPropertyCode(mappingdata.getPropertycode());
					 propertyDeviceMapping.setSerialNumber(provisionNum);
					 this.propertyDeviceMappingRepository.save(propertyDeviceMapping);
					 PropertyTransactionHistory propertyHistory = new PropertyTransactionHistory(DateUtils.getLocalDateOfTenant(),propertyMaster.getId(),"DEVICE SWAP",entityId,propertyDeviceMapping.getPropertyCode());
					 this.propertyHistoryRepository.save(propertyHistory);
				 }
			 }
			 
			 jsonObj.put("clientId", entityId);
			 jsonObj.put("OldHWId", oldSerailNoItemData.getProvisioningSerialNumber());
			 jsonObj.put("NewHWId", newSerailNoItemData.getProvisioningSerialNumber());
			 jsonObj.put("OldHWSerialNumber", oldSerailNoItemData.getSerialNumber());
			 jsonObj.put("NewHWSerialNumber", newSerailNoItemData.getSerialNumber());
			 SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy hh:mm aa");
			 jsonObj.put("TransactionDate", dateFormat.format(new Date()));
			 jsonObj.put("plans", jsonArray);
			 
			 ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0),entityId,Long.valueOf(0), provisionSystem,UserActionStatusTypeEnum.DEVICE_SWAP.toString(),'N','N');
			   ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(Long.valueOf(0),Long.valueOf(0),jsonObj.toString(),"Recieved",
			     provisionNum,DateUtils.getDateOfTenant(),DateUtils.getDateOfTenant(),null,null,'N',UserActionStatusTypeEnum.DEVICE_SWAP.toString(),null);
			   processRequest.add(processRequestDetails);
			   this.processRequestRepository.save(processRequest);
 			
				
		return new CommandProcessingResult(entityId);	
		
	   }catch(final WarrantyEndDateExpireException e){
		   Object[] obj = e.getDefaultUserMessageArgs();
		   throw new WarrantyEndDateExpireException(obj[0].toString());
	  }catch(final JSONException e){
		   	e.printStackTrace();
		   	return new CommandProcessingResult(Long.valueOf(-1));
	  }catch(final DataIntegrityViolationException e){
		  handleDataIntegrityIssues(command,e);
		return new CommandProcessingResult(Long.valueOf(-1));
	  }
	
	}

	private void handleDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {

		LOGGER.error(dve.getMessage(), dve);
		final Throwable realCause = dve.getCause();
		throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "+ realCause.getMessage());
	}

}