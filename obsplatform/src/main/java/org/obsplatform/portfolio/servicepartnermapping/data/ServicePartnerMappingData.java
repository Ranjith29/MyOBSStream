package org.obsplatform.portfolio.servicepartnermapping.data;

import java.util.List;

/**
 * 
 * @author Naresh
 * 
 */
public class ServicePartnerMappingData {

	private Long id;
	private String name;
	private String serviceCode;
	private String serviceType;
	private String serviceDescription;
	private List<ServicePartnerMappingData> serviceDatas;
	private List<ServicePartnerMappingData> partnerNames;
	private Long serviceId;
	private String partnerName;
	private Long partnertypeid;
	private String codevalue;

	
	public ServicePartnerMappingData(final Long id, final String name) {
		this.id = id;
		this.name = name;
	}
	public ServicePartnerMappingData(final Long id, final String name, final Long partnertypeid, final String codevalue) {

		this.id = id;
		this.name = name;
		this.partnertypeid = partnertypeid;
		this.codevalue = codevalue;
	}

	public ServicePartnerMappingData(final Long id, final String serviceCode, final String serviceType, final String serviceDescription) {

		this.id = id;
		this.serviceCode = serviceCode;
		this.serviceType = serviceType;
		this.serviceDescription = serviceDescription;
	}

	public ServicePartnerMappingData(final List<ServicePartnerMappingData> serviceDatas, final List<ServicePartnerMappingData> partnerNames) {

		this.serviceDatas = serviceDatas;
		this.partnerNames = partnerNames;
	}

	public ServicePartnerMappingData(final Long id, final Long serviceId, final String partnerName) {

		this.id = id;
		this.serviceId = serviceId;
		this.partnerName = partnerName;
	}

	public ServicePartnerMappingData(final Long id, final Long serviceId, final String partnerName, final String serviceCode,
			final String serviceDescription, final String serviceType) {

		this.id = id;
		this.serviceId = serviceId;
		this.partnerName = partnerName;
		this.serviceCode = serviceCode;
		this.serviceDescription = serviceDescription;
		this.serviceType = serviceType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public List<ServicePartnerMappingData> getServiceDatas() {
		return serviceDatas;
	}

	public void setServiceDatas(List<ServicePartnerMappingData> serviceDatas) {
		this.serviceDatas = serviceDatas;
	}

	public List<ServicePartnerMappingData> getPartnerNames() {
		return partnerNames;
	}

	public void setPartnerNames(List<ServicePartnerMappingData> partnerNames) {
		this.partnerNames = partnerNames;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}

	public Long getPartnertypeid() {
		return partnertypeid;
	}

	public void setPartnertypeid(Long partnertypeid) {
		this.partnertypeid = partnertypeid;
	}

	public String getCodevalue() {
		return codevalue;
	}

	public void setCodevalue(String codevalue) {
		this.codevalue = codevalue;
	}
	

}
