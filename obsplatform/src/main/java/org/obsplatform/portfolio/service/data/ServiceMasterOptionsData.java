package org.obsplatform.portfolio.service.data;

public class ServiceMasterOptionsData {

private String serviceType;
private String categoryType;

public ServiceMasterOptionsData(final String serviceType,final String categoryType)
{
	this.serviceType=serviceType;
	this.categoryType=categoryType;
}

public String getServiceType() {
	return serviceType;
}

public void setServiceType(final String serviceType) {
	this.serviceType = serviceType;
}

public String getCategoryType() {
	return categoryType;
}

public void setCategoryType(final String categoryType) {
	this.categoryType = categoryType;
}

}
