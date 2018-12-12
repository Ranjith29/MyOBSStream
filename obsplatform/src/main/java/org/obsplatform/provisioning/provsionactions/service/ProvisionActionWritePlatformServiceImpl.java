package org.obsplatform.provisioning.provsionactions.service;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.provisioning.provsionactions.domain.ProvisionActions;
import org.obsplatform.provisioning.provsionactions.domain.ProvisioningActionsRepository;
import org.obsplatform.provisioning.provsionactions.exception.ProvisionActionsNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProvisionActionWritePlatformServiceImpl implements ProvisionActionWritePlatformService{
	
	private final PlatformSecurityContext context;
	private final ProvisioningActionsRepository provisioningActionsRepository;
	
@Autowired
public ProvisionActionWritePlatformServiceImpl(final PlatformSecurityContext context,final ProvisioningActionsRepository actionsRepository){
	
	this.context=context;
	this.provisioningActionsRepository=actionsRepository;
}

@Transactional	
@Override
public CommandProcessingResult updateProvisionActionStatus(JsonCommand command) {
	try{
		this.context.authenticatedUser();
		final ProvisionActions provisionActions=retrieveObjectById(command.entityId());
		final boolean status=command.booleanPrimitiveValueOfParameterNamed("status");
		provisionActions.updateStatus(status);
		this.provisioningActionsRepository.save(provisionActions);
		return new CommandProcessingResult(command.entityId());

	}catch(DataIntegrityViolationException dve){
    	 handleCodeDataIntegrityIssues(command,dve);
    	 return new CommandProcessingResult(Long.valueOf(-1));
	}
}

private ProvisionActions retrieveObjectById(Long entityId) {
	
	final ProvisionActions provisionActions=this.provisioningActionsRepository.findOne(entityId);
	if(provisionActions == null){throw new ProvisionActionsNotFoundException(entityId.toString());}
	return provisionActions;
}



	private void handleCodeDataIntegrityIssues(JsonCommand command,
			DataIntegrityViolationException dve) {
		// TODO Auto-generated method stub
		
	}

}
