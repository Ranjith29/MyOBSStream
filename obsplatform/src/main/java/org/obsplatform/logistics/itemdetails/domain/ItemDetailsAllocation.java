package org.obsplatform.logistics.itemdetails.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.useradministration.domain.AppUser;

import com.google.gson.JsonElement;



@Entity
@Table(name="b_allocation")
public class ItemDetailsAllocation extends AbstractAuditableCustom<AppUser,Long>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*order_id is nothing but a ID of b_onetime_sale table*/
	
	@Column(name="order_id",nullable=false,length=20)
	private Long orderId;
	
	@Column(name="client_id",nullable=false,length=20)
	private Long clientId; 
	
	@Column(name="item_master_id",nullable=false,length=20)
	private Long itemMasterId;
	
	@Column(name="serial_no",nullable=false,length=100)
	private String serialNumber;
	
	@Column(name="allocation_date",nullable=true)
	private Date allocationDate;
	
	@Column(name="status",nullable=true)
	private String status;

	@Column(name="is_deleted")
	private String isDeleted="N";
	
	public ItemDetailsAllocation(){}

	public ItemDetailsAllocation(final Long orderId,final Long clientId,final Long itemMasterId,final String serialNumber,Date allocationDate,String status){
		this.orderId = orderId;
		this.clientId = clientId;
		this.itemMasterId = itemMasterId;
		this.serialNumber = serialNumber;
		this.allocationDate = allocationDate;
		this.status = status;
	}
	
	
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
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

	public Date getAllocationDate() {
		return allocationDate;
	}

	public void setAllocationDate(Date allocationDate) {
		this.allocationDate = allocationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static ItemDetailsAllocation fromJson(JsonElement j, FromJsonHelper fromJsonHelper) {
		 
		final Long orderId = fromJsonHelper.extractLongNamed("orderId",j);
		final Long clientId = fromJsonHelper.extractLongNamed("clientId", j);
		final Long itemMasterId = fromJsonHelper.extractLongNamed("itemMasterId", j);
		final String serialNumber = fromJsonHelper.extractStringNamed("serialNumber", j);
		final Date allocationDate = DateUtils.getDateOfTenant();
		final String status = fromJsonHelper.extractStringNamed("status", j);
		return new ItemDetailsAllocation(orderId,clientId,itemMasterId,serialNumber,allocationDate,status);
	}

	public void deAllocate() {
  
		  this.isDeleted="Y";
		  this.status="unallocated";
		
	}
	
	
}
