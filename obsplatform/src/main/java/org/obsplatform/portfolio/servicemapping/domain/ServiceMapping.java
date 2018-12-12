package org.obsplatform.portfolio.servicemapping.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_prov_service_details",uniqueConstraints = {@UniqueConstraint(name = "service_identification_uq", columnNames = { "service_id", "service_identification","item_id"})})
public class ServiceMapping extends AbstractPersistable<Long> {
	
/*@Table(name = "b_prov_service_details", uniqueConstraints = {@UniqueConstraint(name = "serviceCode", columnNames = { "service_id" }),
		@UniqueConstraint(name = "service_identification_uq", columnNames = { "service_id", "service_identification"})})
public class ServiceMapping extends AbstractPersistable<Long> {*/
	
	private static final long serialVersionUID = 1L;

	@Column(name = "service_id")
	private Long serviceId;

	@Column(name = "service_identification", nullable = false, length = 100)
	private String serviceIdentification;

	@Column(name = "status", nullable = false, length = 100)
	private String status;

	@Column(name = "image", nullable = false, length = 100)
	private String image;

	@Column(name = "category")
	private String category;

	@Column(name = "sub_category")
	private String subCategory;
	
	@Column(name = "is_deleted")
	private String isDeleted = "n";
	
	@Column(name = "provision_system")
	private String provisionSystem;

	@Column(name = "sort_by")
	private Integer sortBy;
	
	@Column(name = "is_hw_req", nullable = false)
	private char isHwReq='N';
	
	@Column(name = "item_id",nullable = true)
	private Long itemId;


	public ServiceMapping() {
	}

	public ServiceMapping(final Long serviceId, final String serviceUrl,final String status, final String image, final String category,
			final String subCategory,final String provisionSystem,final Long itemId,final boolean isHwReq) {

		this.serviceId = serviceId;
		this.serviceIdentification = serviceUrl;
		this.status = status;
		this.image = image;
		this.category = category;
		this.subCategory = subCategory;
		this.provisionSystem=provisionSystem;
		this.itemId= itemId;
		this.isHwReq=isHwReq?'Y':'N';

	}

	public static ServiceMapping fromJson(JsonCommand command, boolean isServiceLevelMap) {
		
		Long itemId=null;
		boolean isHwReq=false;
		final Long serviceId = command.longValueOfParameterNamed("serviceId");
		final String serviceIdentification = command.stringValueOfParameterNamed("serviceIdentification");
		final String status = command.stringValueOfParameterNamed("status");
		final String image = command.stringValueOfParameterNamed("image");
		final String category = command.stringValueOfParameterNamed("category");
		final String subcategory = command.stringValueOfParameterNamed("subCategory");
		final String provisionSystem=command.stringValueOfParameterNamed("provisionSystem");
		if(isServiceLevelMap){
			isHwReq =command.booleanPrimitiveValueOfParameterNamed("isHwReq");
		  if(isHwReq&&command.hasParameter("itemId")){
			 itemId = command.longValueOfParameterNamed("itemId");
		   }
		}
		return new ServiceMapping(serviceId, serviceIdentification, status,image, category,subcategory,provisionSystem,itemId,isHwReq);

	}


	public Map<String, Object> update(JsonCommand command, boolean isServiceLevelMap) {

		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		final String serviceParamName = "serviceId";
		if (command.isChangeInLongParameterNamed(serviceParamName,this.serviceId)) {

			final Long newValue = command.longValueOfParameterNamed(serviceParamName);
			actualChanges.put(serviceParamName, newValue);
			this.serviceId = newValue;
		}

		final String serviceIdentificationParamName = "serviceIdentification";
		if (command.isChangeInStringParameterNamed(serviceIdentificationParamName, this.serviceIdentification)) {

			final String newValue = command.stringValueOfParameterNamed(serviceIdentificationParamName);
			actualChanges.put(serviceIdentificationParamName, newValue);
			this.serviceIdentification = StringUtils.defaultIfEmpty(newValue,null);
		}

		final String statusParamName = "status";
		if (command.isChangeInStringParameterNamed(statusParamName, this.status)) {

			final String newValue = command.stringValueOfParameterNamed(statusParamName);
			actualChanges.put(statusParamName, newValue);
			this.status = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String imageParamName = "image";
		if (command.isChangeInStringParameterNamed(imageParamName, this.image)) {
			
			final String newValue = command.stringValueOfParameterNamed(imageParamName);
			actualChanges.put(imageParamName, newValue);
			this.image = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String categoryParamName = "category";
		if (command.isChangeInStringParameterNamed(categoryParamName,this.category)) {
			
			final String newValue = command.stringValueOfParameterNamed(categoryParamName);
			actualChanges.put(categoryParamName, newValue);
			this.category = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String subCategoryParamName = "subCategory";
		if (command.isChangeInStringParameterNamed(subCategoryParamName,this.subCategory)) {
			
			final String newValue = command.stringValueOfParameterNamed(subCategoryParamName);
			actualChanges.put(subCategoryParamName, newValue);
			this.subCategory = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		final String provisionSystemParamName = "provisionSystem";
		if (command.isChangeInStringParameterNamed(provisionSystemParamName,this.provisionSystem)) {
			
			final String newValue = command.stringValueOfParameterNamed(provisionSystemParamName);
			actualChanges.put(provisionSystemParamName, newValue);
			this.provisionSystem = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		if(isServiceLevelMap){
			final boolean isHwReqParamName =command.booleanPrimitiveValueOfParameterNamed("isHwReq");
		     this.isHwReq=isHwReqParamName?'Y':'N';
			  if(isHwReqParamName&&command.hasParameter("itemId")){
					final String itemParamName = "itemId";
					if (command.isChangeInLongParameterNamed(itemParamName,this.itemId)) {
						final Long newValue = command.longValueOfParameterNamed(itemParamName);
						actualChanges.put(itemParamName, newValue);
						this.itemId = newValue;
					}
				     
			   }else{
			       this.itemId=null;
			   }
			}

		return actualChanges;

	}

	public Long getServiceId() {
		return serviceId;
	}

	public String getServiceIdentification() {
		return serviceIdentification;
	}

	public String getProvisionSystem() {
		return provisionSystem;
	}

	public String getStatus() {
		return status;
	}

	public String getCategory() {
		return category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public String getImage() {
		return image;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public Integer getSortBy() {
		return sortBy;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}

	public char getIsHwReq() {
		return isHwReq;
	}
	
	public Long getItemId() {
		return itemId;
	}


}
