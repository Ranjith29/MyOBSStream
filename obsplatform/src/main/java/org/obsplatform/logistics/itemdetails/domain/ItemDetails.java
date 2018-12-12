package org.obsplatform.logistics.itemdetails.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "b_item_detail", uniqueConstraints = @UniqueConstraint(name = "serial_no_constraint", columnNames = { "serial_no" }))
public class ItemDetails extends AbstractAuditableCustom<AppUser, Long>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2876090423570296480L;

	@Column(name="item_master_id", nullable=false, length=20)
	private Long itemMasterId;
	
	@Column(name="serial_no", nullable=false, length=100)
	private String serialNumber;
	
	@Column(name="grn_id", nullable=false, length=20)
	private Long grnId;
	
	@Column(name="provisioning_serialno",nullable=true,length=100)
	private String provisioningSerialNumber;
	
	@Column(name="quality",nullable=true,length=20)
	private String quality;
	
	@Column(name="status",nullable=true,length=20)
	private String status;
	
	@Column(name="office_id",nullable=false, length=20)
	private Long officeId;
	
	@Column(name="client_id",nullable=false,length=20)
	private Long clientId;
	
	@Column(name="warranty",nullable=true,length=20)
	private Long warranty;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="warranty_date",nullable=true,length=100)
	private Date warrantyDate;
	
	@Column(name="remarks",nullable=true,length=100)
	private String remarks;
	
	@Column(name="item_model",nullable=true,length=60)
	private String itemModel;
	
	@Column(name="location_id",nullable=false, length=20)
	private Long locationId;
	
	@Column(name = "is_deleted")
	private char isDeleted;
	
	@Column(name="received_quantity")
	private Long receivedQuantity;
	
	public ItemDetails(){}
	
	
	public ItemDetails(Long itemMasterId,String serialNumber,Long grnId,String provisioningSerialNumber,String quality,
			String status,Long warranty,String remarks,String itemModel, Boolean isSerialRequired, Long receivedQuantity){
		this.itemMasterId=itemMasterId;
		this.receivedQuantity = receivedQuantity;
		if(isSerialRequired){
			this.serialNumber=serialNumber;
			this.provisioningSerialNumber=provisioningSerialNumber;
			this.receivedQuantity = Long.valueOf(1);
		}
		this.grnId=grnId;
		this.quality=quality;
		this.status=getStatusOnQaulity(quality);
		this.warranty=warranty;
		this.remarks=remarks;
		this.itemModel=itemModel;
		this.isDeleted='N';
	}
	
	private String getStatusOnQaulity(String quality) {
		String status=null;
		if("Good".equalsIgnoreCase(quality) ||"Refurbished".equalsIgnoreCase(quality)){
			status="Available";
		}else{
			status="UnAvailable";
		}
		return status;
	}


	public ItemDetails(Long itemMasterId,String serialNumber,Long grnId,String provisioningSerialNumber,String quality,
			String status,Long officeId,Long clientId,Long warranty,String remarks){
		this.itemMasterId=itemMasterId;
		this.serialNumber=serialNumber;
		this.grnId=grnId;
		this.provisioningSerialNumber=provisioningSerialNumber;
		this.quality=quality;
		this.status=status;
		this.officeId=officeId;
		this.clientId=clientId;
		this.warranty=warranty;
		this.remarks=remarks;
		this.isDeleted='N';
	}
	
	
	public ItemDetails(Long id) {
		this.itemMasterId = id;
	}
	
	public ItemDetails(final Long id,final Long clientId) {
		this.itemMasterId = id;
		this.clientId = clientId;
	}



	public Long getItemMasterId() {
		return itemMasterId;
	}

	public void setItemMasterId(Long itemMasterId) {
		this.itemMasterId = itemMasterId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Long getGrnId() {
		return grnId;
	}

	public void setGrnId(Long grnId) {
		this.grnId = grnId;
	}

	public String getProvisioningSerialNumber() {
		return provisioningSerialNumber;
	}

	public void setProvisioningSerialNumber(String provisioningSerialNumber) {
		this.provisioningSerialNumber = provisioningSerialNumber;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public void setOfficeId(Long officeId) {
		this.officeId = officeId;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getWarranty() {
		return warranty;
	}

	public void setWarranty(Long warranty) {
		this.warranty = warranty;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	public char getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}


	public Map<String, Object> update(JsonCommand command) {
		
		 final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		  final String quality = "quality";
	        if (command.isChangeInStringParameterNamed(quality, this.quality)) {
	            final String newValue = command.stringValueOfParameterNamed(quality);
	            actualChanges.put(quality, newValue);
	            this.quality = StringUtils.defaultIfEmpty(newValue, null);
	            this.status=getStatusOnQaulity(this.quality);
	        }
	        
	        final String provisionSerialNum = "provisioningSerialNumber";
	        if (command.isChangeInStringParameterNamed(provisionSerialNum, this.provisioningSerialNumber)) {
	            final String newValue = command.stringValueOfParameterNamed(provisionSerialNum);
	            actualChanges.put(provisionSerialNum, newValue);
	            this.provisioningSerialNumber = StringUtils.defaultIfEmpty(newValue, null);
	        }
	        
	        final String serialNumber = "serialNumber";
	        if (command.isChangeInStringParameterNamed(serialNumber, this.serialNumber)) {
	            final String newValue = command.stringValueOfParameterNamed(serialNumber);
	            actualChanges.put(serialNumber, newValue);
	            this.serialNumber = StringUtils.defaultIfEmpty(newValue, null);
	        }
	        
	        final String quantity = "quantity";
	        if(command.isChangeInLongParameterNamed(quantity, this.receivedQuantity)){
	        	final Long newValue = command.longValueOfParameterNamed("quantity");
	        	actualChanges.put(quantity, newValue);
	        	this.receivedQuantity = newValue;
	        }
	        			
	        return actualChanges;

	}
	public static ItemDetails fromJson(JsonCommand command, FromJsonHelper fromJsonHelper, Boolean isSerialRequired){
		
		//final JsonElement element = fromJsonHelper.parse(command.toString());
		Integer item = command.integerValueSansLocaleOfParameterNamed("itemMasterId");
		Long itemMasterId = item.longValue();
		
		//Long warranty = w.longValue();
		String remarks = command.stringValueOfParameterNamed("remarks");
		Long grnId = command.longValueOfParameterNamed("grnId");
		String serialNumber = command.stringValueOfParameterNamed("serialNumber");
		String provisioningSerialNumber = command.stringValueOfParameterNamed("provisioningSerialNumber");
		String  quality = command.stringValueOfParameterNamed("quality");
		String status = command.stringValueOfParameterNamed("status");
		String itemModel = command.stringValueOfParameterNamed("itemModel");
		Long receivedQuantity = command.longValueOfParameterNamed("quantity");
		return new ItemDetails(itemMasterId,serialNumber,grnId,provisioningSerialNumber,quality,status,null,remarks,itemModel, isSerialRequired, receivedQuantity);
	}


	public void setAvailable(String entityName) {
		
		this.clientId=null;
		if(entityName.equalsIgnoreCase("HARDWARESWAPPING")){
		  this.status="UnAvailable";
		  this.quality="Faulty";
		}else this.status="Available";
	}
	
	public void itemDelete() {
	
		this.isDeleted='Y';
		this.serialNumber=this.serialNumber+"_"+this.getId();
		
	}


	public void setLocationId(Long locationId) {
		this.locationId=locationId;
		
	}
	
	public Date getWarrantyDate() {
		return warrantyDate;
	}


	public void setWarrantyDate(LocalDate warrabtyEndDate) {
		this.warrantyDate = warrabtyEndDate.toDate();
	}


	public Long getReceivedQuantity() {
		return receivedQuantity;
	}


	public void setReceivedQuantity(Long receivedQuantity) {
		this.receivedQuantity = receivedQuantity;
	}
	
	
}
