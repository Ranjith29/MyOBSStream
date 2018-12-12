package org.obsplatform.provisioning.provisioning.data;

import java.util.Collection;
import java.util.List;

import org.obsplatform.finance.payments.data.McodeData;
import org.obsplatform.organisation.ippool.data.IpPoolData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.portfolio.client.service.GroupData;
import org.obsplatform.portfolio.order.data.OrderLineData;

public class ProvisioningData {

	private String provisioningSystem;
	private String commandName;
	private Long id;
	private String status;
	private List<McodeData> commands;
	private List<McodeData> provisioning;
	private List<ProvisioningCommandParameterData> commandParameters;
    private Collection<MCodeData> vlanDatas;
	private List<OrderLineData> services;
	private List<IpPoolData> ipPoolDatas;
	private List<ServiceParameterData> parameterDatas,serviceDatas;
	private Collection<GroupData> groupDatas;
	private List<ProvisionAdapter> provisionAdapterData;
	

	public ProvisioningData(final Long id,final String ProvisioningSystem,
			final String CommandName,final String Status ){
		this.id=id;
		this.provisioningSystem=ProvisioningSystem;
		this.commandName=CommandName;
		this.status=Status;
	}
	

	public ProvisioningData(final List<McodeData> provisioning, final List<McodeData> commands) {
		
		this.commands=commands;
		this.provisioning=provisioning;		
	}


	public ProvisioningData(final Collection<MCodeData> vlanDatas,
			final List<IpPoolData> ipPoolDatas, 
			final List<OrderLineData> services,final List<ServiceParameterData> serviceDatas, 
			final List<ServiceParameterData> parameterDatas, 
			final Collection<GroupData> groupDatas) {
		
		this.vlanDatas=vlanDatas;
		this.services=services;
		this.ipPoolDatas=ipPoolDatas;
		this.parameterDatas=parameterDatas;
		this.serviceDatas=serviceDatas;
		this.groupDatas=groupDatas;
	}


	public ProvisioningData() {
		// For ProvisioningAdapterApiResource.java
	}


	public String getProvisioningSystem() {
		return provisioningSystem;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public String getCommandName() {
		return commandName;
	}


	public Long getId() {
		return id;
	}


	public String getStatus() {
		return status;
	}


	public List<McodeData> getCommands() {
		return commands;
	}


	public List<McodeData> getProvisioning() {
		return provisioning;
	}


	public void setCommands(List<McodeData> commands) {
		this.commands = commands;
	}


	public void setProvisioning(List<McodeData> provisioning) {
		this.provisioning = provisioning;
	}
	
	public List<ProvisioningCommandParameterData> getCommandParameters() {
		return commandParameters;
	}


	public void setCommandParameters(
			List<ProvisioningCommandParameterData> commandParameters) {
		this.commandParameters = commandParameters;
	}


	public List<ProvisionAdapter> getProvisionAdapterData() {
		return provisionAdapterData;
	}


	public void setProvisionAdapterData(List<ProvisionAdapter> provisionAdapterData) {
		this.provisionAdapterData = provisionAdapterData;
	}

	
	
	
}
