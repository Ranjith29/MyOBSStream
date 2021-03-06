package org.obsplatform.provisioning.processrequest.domain;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@SuppressWarnings("serial")
@Entity
@Table(name = "b_process_request")

public class ProcessRequest extends AbstractAuditableCustom<AppUser, Long> {
	
	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "is_processed")
	private char isProcessed='N';


	@Column(name = "provisioing_system")
	private String provisioingSystem;
	
	@Column(name="request_type")
	private String requestType;
	
	@Column(name = "prepareRequest_id")
	private Long prepareRequestId;

	@Column(name = "is_notify")
	private char isNotify='N';



	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processRequest", orphanRemoval = true)
	private List<ProcessRequestDetails> processRequestDetails = new ArrayList<ProcessRequestDetails>();

	

	 public ProcessRequest() {
		// TODO Auto-generated constructor stub
			
	}



	/*public ProcessRequest(Long clientId, Long orderId, 
			 String provisioningSystem, char isDeleted,String userName, String requestType, Long requestId) {
            this.clientId=clientId;
            this.orderId=orderId;
            this.provisioingSystem=provisioningSystem;
            this.requestType=requestType;
            this.prepareRequestId=requestId;
	
	
	}*/
	 public ProcessRequest(Long prepareRequestId, Long clientId,Long orderId, String provisioningSytem, String requestType, char isProcessed, char isNotify) {
		 	this.prepareRequestId = prepareRequestId;
		 	this.clientId = clientId;
		 	this.orderId = orderId;
		 	this.provisioingSystem = provisioningSytem;
		 	this.requestType = requestType;
		 	this.isProcessed=isProcessed;
		 	this.isNotify=isNotify;
	 }


	 public static ProcessRequest fromJson(JsonCommand command){

		 	final Long prepareRequestId= command.entityId();
		 	final Long clientId = command.longValueOfParameterNamed("clientId");
		 	final Long orderId = command.longValueOfParameterNamed("orderId");
		 	final String provisioningSytem = command.stringValueOfParameterNamed("provisioingSystem");
		 	final String requestType = command.stringValueOfParameterNamed("requestType");
		 	return new ProcessRequest(prepareRequestId,clientId,orderId,provisioningSytem,requestType,'N','N');

	 }
	


	public void add(ProcessRequestDetails processRequestDetails) {
	     processRequestDetails.update(this);
	     this.processRequestDetails.add(processRequestDetails);
		
		
	}



	public void setNotify() {
		if(this.isNotify =='N'){
			this.isNotify='Y';
		}
		
	}



	public void setProcessStatus(char status) {
		this.isProcessed=status;
		
	}
	



	public Long getClientId() {
		return clientId;
	}



	public Long getOrderId() {
		return orderId;
	}



	public char getIsProcessed() {
		return isProcessed;
	}



	public String getProvisioingSystem() {
		return provisioingSystem;
	}



	public String getRequestType() {
		return requestType;
	}



	public Long getPrepareRequestId() {
		return prepareRequestId;
	}



	public char getIsNotify() {
		return isNotify;
	}



	public List<ProcessRequestDetails> getProcessRequestDetails() {
		return processRequestDetails;
	}


	public void update() {
		
		this.isProcessed ='N';
		this.isNotify='N';
		
	}


	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	
	public void setNotifyAddOn() {
		if(this.isNotify=='N'){
			this.isNotify='C';
		}
		
	}



	public void setProcessedAddOn() {
		if(this.isProcessed=='N'){
			this.isProcessed='C';
		}
		
	}
	
	
	
	
	
 
	

}
