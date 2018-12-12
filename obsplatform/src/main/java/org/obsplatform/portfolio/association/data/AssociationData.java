package org.obsplatform.portfolio.association.data;

import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.emun.data.EnumValuesData;
import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;

public class AssociationData {

	private Long orderId;
	private String planCode;
	private String itemCode;
	private String serialNum;
	private Long id;
	private Long planId;
	private List<AssociationData> hardwareData;
	private List<AssociationData> planData;
	private Long clientId;
	private String allocationType;
	private String provisionNumber;
	private Long saleId;
	private Long itemId;
	private String propertyCode;
	private Long serviceId;
	private Long officeId;
	private LocalDate warrantyDate;
	private Collection<EnumValuesData> enumValuesDatas;
	private Collection<MCodeData> itemCategory;
	
	private List<ItemData> itemData;

	public AssociationData(Long orderId, Long id, String planCode,String itemCode, String serialNum, Long planId) {

		this.orderId = orderId;
		this.planCode = planCode;
		this.itemCode = itemCode;
		this.serialNum = serialNum;
		this.planId = planId;
		this.id = id;

	}

	public AssociationData(List<AssociationData> hardwareDatas,List<AssociationData> planDatas) {

		this.hardwareData = hardwareDatas;
		this.planData = planDatas;
	}


	public AssociationData(Long orderId, String planCode, String provisionNumber,Long id, Long planId, Long clientId, 
			String serialNum, String itemCode, Long saleId, Long itemId,String allocationType) {
		
		this.orderId=orderId;
		this.planCode=planCode;
		this.serialNum=serialNum;
		this.allocationType=allocationType;
		this.id=id;
		this.planId=planId;
		this.clientId=clientId;
		this.provisionNumber=provisionNumber;
		this.itemCode=itemCode;
		this.saleId=saleId;
		this.itemId=itemId;
	}
    public AssociationData(Long officeId, String serialNum, Long saleId, Long itemId, LocalDate warrantyDate) {
		
		this.officeId = officeId;
		this.serialNum = serialNum;
		this.saleId = saleId;
		this.itemId = itemId;
		this.warrantyDate = warrantyDate;
	}

	public AssociationData(Long planId, String planCode, Long id) {

		this.planId = planId;
		this.planCode = planCode;
		this.orderId = id;
	}

	public AssociationData(String serialNum, String provisionNumber,String allocationType, 
			String propertyCode, Long orderId) {
		
		this.serialNum = serialNum;
		this.provisionNumber = provisionNumber;
		this.allocationType = allocationType;
		this.propertyCode = propertyCode;
		this.orderId = orderId;
		this.itemId = orderId;

	}

	public AssociationData(Long id, Long clientId, Long orderId, Long planId,String serialNum,
            String allocationType, Long serviceId) {

		this.id = id;
		this.clientId = clientId;
		this.orderId = orderId;
		this.planId = planId;
		this.serialNum = serialNum;
		this.allocationType = allocationType;
		this.serviceId = Long.valueOf(0).equals(serviceId) ? null : serviceId;
	}



	public void addHardwareDatas(List<AssociationData> hardwareDatas){
		this.hardwareData=hardwareDatas;

	}

	public void addPlanDatas(List<AssociationData> planDatas) {
		this.planData = planDatas;
	}

	public void addEnumValuesDatas(Collection<EnumValuesData> enumValuesDatas ,List<ItemData> itemData,Collection<MCodeData> itemCategory) {

		this.enumValuesDatas = enumValuesDatas;
		this.itemData=itemData;
		this.itemCategory=itemCategory;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getPlanCode() {
		return planCode;
	}

	public String getItemCode() {
		return itemCode;
	}

	public String getSerialNum() {
		return serialNum;
	}

	public Long getId() {
		return id;
	}

	public Long getPlanId() {
		return planId;
	}

	public String getProvisionNumber() {
		return provisionNumber;
	}

	public List<AssociationData> getHardwareData() {
		return hardwareData;
	}

	public List<AssociationData> getPlanData() {
		return planData;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getAllocationType() {
		return allocationType;
	}

	public Long getSaleId() {
		return saleId;
	}

	public Long getItemId() {
		return itemId;
	}

	public String getPropertyCode() {
		return propertyCode;
	}

	public void setPropertyCode(String propertyCode) {
		this.propertyCode = propertyCode;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	
	public Collection<EnumValuesData> getEnumValuesDatas() {
		return enumValuesDatas;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public void setOfficeId(Long officeId) {
		this.officeId = officeId;
	}

	public LocalDate getWarrantyDate() {
		return warrantyDate;
	}

	public void setWarrantyDate(LocalDate warrantyDate) {
		this.warrantyDate = warrantyDate;
	}
	


}