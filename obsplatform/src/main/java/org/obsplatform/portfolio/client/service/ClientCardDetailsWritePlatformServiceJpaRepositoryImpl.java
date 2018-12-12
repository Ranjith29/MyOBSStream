package org.obsplatform.portfolio.client.service;

import java.util.Map;

import org.json.JSONException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientCardDetails;
import org.obsplatform.portfolio.client.domain.ClientCardDetailsRepository;
import org.obsplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.obsplatform.portfolio.client.serialization.ClientCardDetailsCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientCardDetailsWritePlatformServiceJpaRepositoryImpl implements ClientCardDetailsWritePlatformService {

	    private final PlatformSecurityContext context;
	    private final ClientRepositoryWrapper clientRepository;
	    private final ClientCardDetailsRepository clientCardDetailsRepository;	  
	    private final ClientCardDetailsCommandFromApiJsonDeserializer clientCardDetailsCommandFromApiJsonDeserializer;

	    @Autowired
	    public ClientCardDetailsWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
	            final ClientRepositoryWrapper clientRepository, final ClientCardDetailsRepository clientCardDetailsRepository,
	            final ClientCardDetailsCommandFromApiJsonDeserializer clientCardDetailsCommandFromApiJsonDeserializer) {
	    	
	        this.context = context;
	        this.clientRepository = clientRepository;
	        this.clientCardDetailsRepository = clientCardDetailsRepository;
	        this.clientCardDetailsCommandFromApiJsonDeserializer = clientCardDetailsCommandFromApiJsonDeserializer;
	    }
	
	@Override
	public CommandProcessingResult addClientCardDetails(Long clientId,JsonCommand command) {

		this.context.authenticatedUser();
		this.clientCardDetailsCommandFromApiJsonDeserializer.validateForCreate(command.json());

		ClientCardDetails clientCardDetails;
		try {

			clientCardDetails = ClientCardDetails.fromJson(command);

			final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
			ClientCardDetails oldCardDetails = this.clientCardDetailsRepository.findOneByClientObfuscatedCard(client);

			if (oldCardDetails != null) {
				//oldCardDetails.setIsDeleted('Y');
				//this.clientCardDetailsRepository.save(oldCardDetails);
				this.clientCardDetailsRepository.delete(oldCardDetails);
				
				clientCardDetails.setClient(client);
				this.clientCardDetailsRepository.save(clientCardDetails);
			} else {
				clientCardDetails.setClient(client);
				this.clientCardDetailsRepository.save(clientCardDetails);
			}

			return new CommandProcessingResultBuilder() //
					.withCommandId(command.commandId()) //
					.withOfficeId(client.officeId()) //
					.withClientId(clientId) //
					.withEntityId(clientCardDetails.getId()) //
					.build();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public CommandProcessingResult updateClientCardDetails(JsonCommand command) {
		this.context.authenticatedUser();
		this.clientCardDetailsCommandFromApiJsonDeserializer.validateForCreate(command.json());
		ClientCardDetails clientCardDetails = this.clientCardDetailsRepository.findOne(command.subentityId());
		final Client client = this.clientRepository.findOneWithNotFoundDetection(command.entityId());	
		final Map<String, Object> changes = clientCardDetails.update(command);
		clientCardDetails.setClient(client);		
        this.clientCardDetailsRepository.save(clientCardDetails);
        
		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withOfficeId(client.officeId()) //
				.withClientId(command.entityId()) //
				.withEntityId(clientCardDetails.getId()) //
				.build();
	}

	@Override
	public CommandProcessingResult deleteClientCardDetails(JsonCommand command) {
		this.context.authenticatedUser();
		ClientCardDetails clientCardDetails = this.clientCardDetailsRepository.findOne(command.subentityId());
		//clientCardDetails.setIsDeleted('Y');
		this.clientCardDetailsRepository.delete(clientCardDetails);
		//this.clientCardDetailsRepository.save(clientCardDetails);
		return new CommandProcessingResultBuilder()
		.withEntityId(clientCardDetails.getId()) //
		.build();
	}

}
