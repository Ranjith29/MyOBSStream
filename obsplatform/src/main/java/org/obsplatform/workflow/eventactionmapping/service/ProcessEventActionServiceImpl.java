package org.obsplatform.workflow.eventactionmapping.service;


import java.util.List;

import org.obsplatform.cms.eventmaster.domain.EventMaster;
import org.obsplatform.cms.eventmaster.domain.EventMasterRepository;
import org.obsplatform.finance.billingmaster.api.BillingMasterApiResourse;
import org.obsplatform.finance.billingorder.service.InvoiceClient;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.portfolio.association.data.HardwareAssociationData;
import org.obsplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.obsplatform.portfolio.order.service.OrderWritePlatformService;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.obsplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.obsplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.obsplatform.workflow.eventaction.domain.EventAction;
import org.obsplatform.workflow.eventaction.domain.EventActionRepository;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.obsplatform.workflow.eventaction.service.ProcessEventActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;


@Service
public class ProcessEventActionServiceImpl implements ProcessEventActionService {

	
	
	private final InvoiceClient invoiceClient;
	private final FromJsonHelper fromApiJsonHelper;
	private final EventActionRepository eventActionRepository;
	private final ProcessRequestRepository processRequestRepository;
    private final OrderWritePlatformService orderWritePlatformService;
    private final EventMasterRepository eventMasterRepository;
    private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
    private final BillingMasterApiResourse billingMasterApiResourse;
    
 

	@Autowired
	public ProcessEventActionServiceImpl(final EventActionRepository eventActionRepository,final FromJsonHelper fromJsonHelper,
			final OrderWritePlatformService orderWritePlatformService,final InvoiceClient invoiceClient,
			final ProcessRequestRepository processRequestRepository,final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,
			final BillingMasterApiResourse billingMasterApiResourse,final EventMasterRepository eventMasterRepository)
	{
		this.invoiceClient=invoiceClient;
        this.fromApiJsonHelper=fromJsonHelper;
        this.eventActionRepository=eventActionRepository;
        this.eventMasterRepository=eventMasterRepository;
        this.processRequestRepository=processRequestRepository;
        this.orderWritePlatformService=orderWritePlatformService;
        this.hardwareAssociationReadplatformService=hardwareAssociationReadplatformService;
        this.billingMasterApiResourse = billingMasterApiResourse;
        
	}
	
	@Override
	public void processEventActions(EventActionData eventActionData) {
		
		EventAction eventAction=this.eventActionRepository.findOne(eventActionData.getId());
		String jsonObject=eventActionData.getJsonData();
		 JsonCommand command=null;
		 JsonElement parsedCommand =null;
		try{
			switch (eventAction.getActionName()) {
			
			case EventActionConstants.ACTION_RENEWAL:
				
				 parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
	            command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"RenewalOrder",
						eventActionData.getClientId(), null,null,eventActionData.getClientId(), null, null, null,null, null, null,null);
			    	this.orderWritePlatformService.renewalClientOrder(command,eventActionData.getOrderId());
			    break;
			
			case EventActionConstants.ACTION_ACTIVE :
				this.orderWritePlatformService.reconnectOrder(eventActionData.getOrderId());
				break;
				
			case EventActionConstants.ACTION_DISCONNECT :
					
					 parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
					 command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"DissconnectOrder",eventActionData.getClientId(), null,
						null,eventActionData.getClientId(), null, null, null,null, null, null,null);
					 this.orderWritePlatformService.disconnectOrder(command,eventActionData.getOrderId());
				 break;
				
			case EventActionConstants.ACTION_NEW :

               parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
				command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"CreateOrder",eventActionData.getClientId(), null,
						null,eventActionData.getClientId(), null, null, null,null, null, null,null);
				this.orderWritePlatformService.createOrder(eventActionData.getClientId(), command);
				break;	
				
				
			case EventActionConstants.ACTION_CHNAGE_PLAN :

	               parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
					command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"ChangeOrder",eventActionData.getClientId(), null,
							null,eventActionData.getClientId(), null, null, null,null, null, null,null);
					this.orderWritePlatformService.changePlan(command, command.longValueOfParameterNamed("orderId"));
					break;
				
			case EventActionConstants.ACTION_INVOICE :
				try{
					CommandProcessingResult result = null;
					parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
					command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"CreateInvoice",eventActionData.getClientId(), null,
						null,eventActionData.getClientId(), null, null, null,null, null, null,null);
					result=this.invoiceClient.createInvoiceBill(command);
					if(result!=null){
						this.billingMasterApiResourse.printInvoice(result.resourceId(),eventActionData.getClientId(),true);
					}
					
				}catch(Exception exception){
					
				}
				break;
				
			case EventActionConstants.ACTION_SEND_PROVISION :
				try{
					final List<HardwareAssociationData> associationDatas= this.hardwareAssociationReadplatformService.retrieveClientAllocatedHardwareDetails(eventActionData.getClientId());
					if(!associationDatas.isEmpty()){
						Long none=Long.valueOf(0);
						final ProcessRequest processRequest=new ProcessRequest(none,eventActionData.getClientId(),none,ProvisioningApiConstants.PROV_BEENIUS,
													ProvisioningApiConstants.REQUEST_TERMINATE,'N','N');
						final ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(none,none,null,"success",associationDatas.get(0).getProvSerialNum(), 
																	DateUtils.getDateOfTenant(), null, DateUtils.getDateOfTenant(),null,'N', ProvisioningApiConstants.REQUEST_TERMINATE,null);
						processRequest.add(processRequestDetails);
						this.processRequestRepository.save(processRequest);
					}
				}catch(Exception exception){
					
				}
				break;
				
			case EventActionConstants.ACTION_ACTIVE_LIVE_EVENT : 
				
				EventMaster eventMaster=this.eventMasterRepository.findOne(eventActionData.getResourceId());
				eventMaster.setStatus(Integer.valueOf(1));
				this.eventMasterRepository.saveAndFlush(eventMaster);
				
				break;
				
			case EventActionConstants.ACTION_INACTIVE_LIVE_EVENT : 
				
				eventMaster=this.eventMasterRepository.findOne(eventActionData.getResourceId());
				eventMaster.setStatus(Integer.valueOf(2));
				this.eventMasterRepository.saveAndFlush(eventMaster);
				
				break;
				
			case EventActionConstants.ACTION_SEND_PAYMENT :
				try{
				   this.billingMasterApiResourse.printPayment(eventAction.getResourceId(), eventAction.getClientId(),true);
				   }	
				catch(Exception exception){
					
				}
				break;

			case EventActionConstants.ACTION_TOPUP_INVOICE_MAIL :

				try{
					 this.billingMasterApiResourse.printInvoice(eventActionData.getResourceId(),eventActionData.getClientId(),true);
					}	
				catch(Exception exception){
				}
			break;		
			
			case EventActionConstants.ACTION_ORDER_REACTIVATE :
				
				parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
				command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"ORDER",eventActionData.getResourceId(), null,
						null,null, null, null, null,null, null, null,"reactive/"+eventActionData.getOrderId());
				this.orderWritePlatformService.reactiveOrder(command, eventActionData.getOrderId());
				
			break;
			
            case EventActionConstants.ACTION_ORDER_RECONNECTION :
				
				this.orderWritePlatformService.reconnectOrder(eventActionData.getOrderId());
				
			break;

			
			default:
				break;
			}
	    	eventAction.updateStatus('Y');
	    	this.eventActionRepository.save(eventAction);
		}catch(DataIntegrityViolationException exception){
			eventAction.updateStatus('F');
	    	this.eventActionRepository.save(eventAction);
			exception.printStackTrace();
		}catch (Exception exception) {
	    	eventAction.updateStatus('F');
	    	this.eventActionRepository.save(eventAction);
		}
	}
}