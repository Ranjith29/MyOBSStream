package org.obsplatform.portfolio.property.data;

public class PropertyDeviceMappingData {

	private Long id;
	private String serialNumber;
	private String propertycode;
	private boolean serialNumberFlag;

	public PropertyDeviceMappingData(Long id, String serialNumber,
			String propertyCode, boolean serialNumberFlag) {

		this.id = id;
		this.serialNumber = serialNumber;
		this.propertycode = propertyCode;
		this.serialNumberFlag = serialNumberFlag;
	}

	public boolean isSerialNumberFlag() {
		return serialNumberFlag;
	}

	public Long getId() {
		return id;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getPropertycode() {
		return propertycode;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public void setPropertycode(String propertycode) {
		this.propertycode = propertycode;
	}

}
