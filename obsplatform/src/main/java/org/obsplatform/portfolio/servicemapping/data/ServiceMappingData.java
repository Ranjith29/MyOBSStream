package org.obsplatform.portfolio.servicemapping.data;

import java.util.Collection;
import java.util.List;

import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.dataqueries.data.ReportParameterData;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;

public class ServiceMappingData {

	
	private Long id;
	private Long serviceId;
	private String serviceCode;
	private String serviceIdentification;
	private String status;
	private String image;
	private String category;
	private String subCategory;
	private List<ServiceMappingData> serviceMappingData;
	private List<ServiceCodeData> serviceCodeData;
	private List<EnumOptionData> statusData;
	private Collection<ReportParameterData> serviceParameters;
	private Collection<McodeData> categories;
	private Collection<McodeData> subCategories;
	private Collection<MCodeData> provisionSysData;
	private String provisionSystem;
	private String sortBy;
	private List<ItemData> itemsData;
	private Long itemId;
	private String itemDescription;
	private boolean isHwReq;

	public ServiceMappingData(final Long id, final String serviceCode,final String serviceIndentification, final String status,
			final String image, final String category, final String subCategory,final String sortBy, String provisionSystem,
			final Long itemId, final String isHwReq,final String itemDescription) {
		
		this.id=id;
		this.serviceCode=serviceCode;
		this.serviceIdentification=serviceIndentification;
		this.status=status;
		this.image=image;
		this.category=category;
		this.provisionSystem=provisionSystem;
		this.subCategory=subCategory;
		this.sortBy = sortBy;
		this.itemId = itemId;
		this.itemDescription = itemDescription;
		this.isHwReq = ("Y".equalsIgnoreCase(isHwReq) ? true : false);
	}

	public ServiceMappingData( final List<ServiceMappingData> serviceMappingData,final List<ServiceCodeData> serviceCodeData, 
			final List<EnumOptionData> status,final Collection<ReportParameterData> serviceParameters, 
			final Collection<McodeData> categories, final Collection<McodeData> subCategories,
			final Collection<MCodeData> provisionSysData,final  List<ItemData> itemsData) {

		this.serviceMappingData=serviceMappingData;
		this.serviceCodeData=serviceCodeData;
		this.statusData=status;
		this.serviceParameters=serviceParameters;
		this.categories=categories;
		this.provisionSysData=provisionSysData;
		this.subCategories=subCategories;
		this.itemsData = itemsData;
	}
	
	public Collection<McodeData> getCategories() {
		return categories;
	}

	public void setCategories(Collection<McodeData> categories) {
		this.categories = categories;
	}

	public Collection<McodeData> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(Collection<McodeData> subCategories) {
		this.subCategories = subCategories;
	}

	public List<ServiceCodeData> getServiceCodeData() {
		return serviceCodeData;
	}

	public void setServiceCodeData(List<ServiceCodeData> serviceCodeData) {
		this.serviceCodeData = serviceCodeData;
	}

	public Long getId() {
		return id;
	}

	public List<ServiceMappingData> getServiceMappingData() {
		return serviceMappingData;
	}

	public void setServiceMappingData(List<ServiceMappingData> serviceMappingData) {
		this.serviceMappingData = serviceMappingData;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getServiceIndentification() {
		return serviceIdentification;
	}

	public void setServiceIndentification(String serviceIndentification) {
		this.serviceIdentification = serviceIndentification;
	}

	public String getImage() {
		return image;
	}

	public void setStatusData(List<EnumOptionData> status) {
		this.statusData = status;

	}
	
	public Long getItemId() {
		return itemId;
	}

	public boolean isHwReq() {
		return isHwReq;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public List<EnumOptionData> getStatusData() {
		return statusData;
	}

	public Collection<MCodeData> getProvisionSysData() {
		return provisionSysData;
	}

	public void setProvisionSysData(Collection<MCodeData> provisionSysData) {
		this.provisionSysData = provisionSysData;

	}

	public String getServiceIdentification() {
		return serviceIdentification;
	}

	public String getCategory() {
		return category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public Collection<ReportParameterData> getServiceParameters() {
		return serviceParameters;
	}

	public String getProvisionSystem() {
		return provisionSystem;
	}

	public String getSortBy() {
		return sortBy;
	}
	

	public List<ItemData> getItemsData() {
		return itemsData;
	}

	public void setItemsData(List<ItemData> itemsData) {
		this.itemsData = itemsData;
	}

}
