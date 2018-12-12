package org.obsplatform.workflow.eventactionmapping.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_eventaction_mapping", uniqueConstraints = @UniqueConstraint(name = "event_action_name_code", 
  columnNames = { "event_name","action_name" }))
public class EventActionMapping extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "event_name")
	private String eventName;

	@Column(name = "action_name")
	private String actionName;

	@Column(name = "process")
	private String process;

	@Column(name = "is_deleted")
	private char isDeleted = 'N';

	public EventActionMapping() {

	}

	public EventActionMapping(final String eventName, final String actionName,final String process) {

		this.eventName = eventName;
		this.actionName = actionName;
		this.process = process;
	}

	public String getEventName() {
		return eventName;
	}

	public String getActionName() {
		return actionName;
	}

	public String getProcess() {
		return process;
	}

	public static EventActionMapping fromJson(final JsonCommand command) {

		final String eventName = command.stringValueOfParameterNamed("event");
		final String actionName = command.stringValueOfParameterNamed("action");
		final String process = command.stringValueOfParameterNamed("process");

		return new EventActionMapping(eventName, actionName, process);
	}

	public Map<String, Object> update(final JsonCommand command) {
		
		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		final String eventParamName = "event";
		if (command.isChangeInStringParameterNamed(eventParamName, this.eventName)) {
			final String newValue = command.stringValueOfParameterNamed(eventParamName);
			actualChanges.put(eventParamName, newValue);
			this.eventName = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String actionParamName = "action";
		if (command.isChangeInStringParameterNamed(actionParamName, this.actionName)) {
			final String newValue = command.stringValueOfParameterNamed(actionParamName);
			actualChanges.put(actionParamName, newValue);
			this.actionName = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String processParam = "process";
		if (command.isChangeInStringParameterNamed(processParam, this.process)) {
			final String newValue = command.stringValueOfParameterNamed(processParam);
			actualChanges.put(processParam, newValue);
			this.process = StringUtils.defaultIfEmpty(newValue, null);
		}

		return actualChanges;

	}

	public void delete() {

		if (this.isDeleted == 'N') {
			this.isDeleted = 'Y';
		} else {
			this.isDeleted = 'N';
		}

	}

}
