package org.obsplatform.portfolio.order.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.useradministration.domain.AppUser;

import com.google.gson.JsonElement;

@Entity
@Table(name="b_orders_addons")
public class OrderAddons extends AbstractAuditableCustom<AppUser,Long>{

	private static final long serialVersionUID = 1L;

	@Column(name = "order_id")
	private Long orderId;
	
	@Column(name = "price_id")
	private Long priceId;

	@Column(name = "service_id")
	private Long serviceId;

	@Column(name = "contract_id")
	private Long contractId;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "provision_system")
	private String provisionSystem;
	
	@Column(name="associate_id")
	private Long associationId;
	
	@Column(name ="is_deleted")
	private char isDelete = 'N';
	
	public OrderAddons(){
		
	}

	public OrderAddons(final Long orderId, final Long serviceId, final Long contractId,final Date startDate) {
		
		this.orderId=orderId;
		this.serviceId=serviceId;
		this.contractId=contractId;
		this.startDate=startDate;
	}

	public static OrderAddons fromJson(final JsonElement element,final FromJsonHelper fromJsonHelper,final Long orderId, LocalDate startDate, Long contractId) {
		
		final Long serviceId=fromJsonHelper.extractLongNamed("serviceId", element);
		return new OrderAddons(orderId,serviceId,contractId,startDate.toDate());
	}

	public Long getOrderId() {
		return orderId;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public Long getContractId() {
		return contractId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Long getPriceId() {
		return priceId;
	}

	public String getStatus() {
		return status;
	}

	public String getProvisionSystem() {
		return provisionSystem;
	}

	public char getIsDelete() {
		return isDelete;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setProvisionSystem(String provisionSystem) {
		this.provisionSystem = provisionSystem;

	}

	public void setStatus(String status) {
		this.status = status;

	}

	public void setPriceId(Long priceId) {
		this.priceId = priceId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public void isDeleted() {

		this.isDelete = 'Y';
	}

	public void setAssociationId(Long associationId) {
		this.associationId = associationId;
	}

	public Long getAssociationId() {
		return associationId;
	}
	
	public void updateDisconnectionstate() {
		
		this.endDate =DateUtils.getDateOfTenant();
		this.isDelete='Y';
		this.status = "Disconnected";
	}

	public void updateTalkAddonstate() {
	
		this.endDate =DateUtils.getDateOfTenant();
		this.isDelete='Y';
		this.status = "Disconnected";
	}

}
