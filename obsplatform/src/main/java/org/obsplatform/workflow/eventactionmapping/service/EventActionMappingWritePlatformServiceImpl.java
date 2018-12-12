package org.obsplatform.workflow.eventactionmapping.service;

import java.util.List;
import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.workflow.eventactionmapping.data.EventActionMappingData;
import org.obsplatform.workflow.eventactionmapping.domain.EventActionMapping;
import org.obsplatform.workflow.eventactionmapping.domain.EventActionMappingRepository;
import org.obsplatform.workflow.eventactionmapping.exception.EventActionMappingNotFoundException;
import org.obsplatform.workflow.eventactionmapping.exception.EventNameDuplicateException;
import org.obsplatform.workflow.eventactionmapping.serialization.EventActionMappingCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 *
 */
@Service
public class EventActionMappingWritePlatformServiceImpl implements EventActionMappingWritePlatformService {

	private final static Logger LOGGER = LoggerFactory.getLogger(EventActionMappingWritePlatformServiceImpl.class);
	
	private final PlatformSecurityContext context;
	private final EventActionMappingCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final EventActionMappingRepository eventActionMappingRepository;
	private final EventActionMappingReadPlatformService eventActionMappingReadPlatformService;

	@Autowired
	public EventActionMappingWritePlatformServiceImpl(final PlatformSecurityContext context,
			final EventActionMappingCommandFromApiJsonDeserializer apiJsonDeserializer,
			final EventActionMappingRepository eventActionMappingRepository,
			final EventActionMappingReadPlatformService eventActionMappingReadPlatformService) {
		
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.eventActionMappingRepository = eventActionMappingRepository;
		this.eventActionMappingReadPlatformService = eventActionMappingReadPlatformService;
	}

	@Override
	public CommandProcessingResult createEventActionMapping(JsonCommand command) {

		try {
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			final EventActionMapping eventActionMapping = EventActionMapping.fromJson(command);
			final List<EventActionMappingData> datas = this.eventActionMappingReadPlatformService.retrieveEvents(eventActionMapping.getEventName());
			for (EventActionMappingData data : datas) {
				if (data.getActionName().equalsIgnoreCase(eventActionMapping.getActionName())) {
					throw new EventNameDuplicateException(eventActionMapping.getActionName());
				}
			}

			this.eventActionMappingRepository.save(eventActionMapping);

			return new CommandProcessingResult(eventActionMapping.getId());

		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return null;
		}

	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		
		final Throwable realCause = dve.getMostSpecificCause();
		LOGGER.error(dve.getMessage(), dve);
		if("event_action_name_code".equalsIgnoreCase(dve.getMostSpecificCause().getMessage())){
			throw new EventNameDuplicateException(dve.getMessage());
		}
		
		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "+ realCause.getMessage());

	}

	@Override
	public CommandProcessingResult updateEventActionMapping(final Long id,final JsonCommand command) {
		
		try {

			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			final EventActionMapping eventAction = this.eventActionRetrieveById(id);
			final Map<String, Object> changes = eventAction.update(command);
			if (!changes.isEmpty()) {
				this.eventActionMappingRepository.saveAndFlush(eventAction);
			}
			return new CommandProcessingResult(id);

		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return null;
		}

	}

	private EventActionMapping eventActionRetrieveById(final Long id) {

		final EventActionMapping eventAction = this.eventActionMappingRepository.findOne(id);
		if (eventAction == null) {
			throw new EventActionMappingNotFoundException(id.toString());
		}
		return eventAction;

	}

	@Override
	public CommandProcessingResult deleteEventActionMapping(Long id) {

		try {
			this.context.authenticatedUser();
			final EventActionMapping event = this.eventActionRetrieveById(id);
			event.delete();
			this.eventActionMappingRepository.save(event);
			return new CommandProcessingResult(id);
		} catch (Exception exception) {
			return null;
		}

	}
}
