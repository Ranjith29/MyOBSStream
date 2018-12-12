package org.obsplatform.provisioning.provisioning.data;

public class ProvisioningCommandParameterData {

	private Long id;
	private String commandParam;
	private String paramType;
	
	public ProvisioningCommandParameterData(final Long id, String commandParam,
			final String paramType){
		this.id=id;
		this.commandParam=commandParam;
		this.paramType=paramType;
		
	}

	public String getCommandParam() {
		return commandParam;
	}

	public String getParamType() {
		return paramType;
	}

	public Long getId() {
		return id;
	}
	
	
	
	
}
