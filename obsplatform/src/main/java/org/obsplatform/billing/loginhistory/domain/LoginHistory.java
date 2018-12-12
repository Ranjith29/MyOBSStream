package org.obsplatform.billing.loginhistory.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_login_history")
public class LoginHistory extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "username")
	private String userName;

	@Column(name = "session_id")
	private String sessionId;

	@Column(name = "login_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date loginTime;

	@Column(name = "logout_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date logoutTime;

	@Column(name = "session_lastupdate")
	private Date sessionLastupdate;

	@Column(name = "status")
	private String status;

	public LoginHistory() {

	}

	public LoginHistory(final String ipAddress, final String deviceId, final String sessionId,final Date loginTime, final Date logoutTime, 
			final String userName, final String status) {

		this.ipAddress = ipAddress;
		this.deviceId = deviceId;
		this.sessionId = sessionId;
		this.loginTime = loginTime;
		this.logoutTime = logoutTime;
		this.status = status;
		this.userName = userName;
	}

	public static LoginHistory fromJson(final JsonCommand command) {
		
		final String ipAddress = command.stringValueOfParameterNamed("ipAddress");
		final String deviceId = command.stringValueOfParameterNamed("deviceId");
		final String sessionId = command.stringValueOfParameterNamed("sessionId");
		final Date loginTime = command.DateValueOfParameterNamed("loginTime");
		// final Date logoutTime= command.DateValueOfParameterNamed("logoutTime");
		final String userName = command.stringValueOfParameterNamed("userName");
		final String status = command.stringValueOfParameterNamed("status");

		return new LoginHistory(ipAddress, deviceId, sessionId, loginTime,null, userName, status);
	}

	public Map<String, Object> update() {

		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		final String logoutTimeParamName = "logoutTime";
		this.logoutTime = DateUtils.getDateTimeOfTenant().toDate();
		actualChanges.put(logoutTimeParamName, this.logoutTime);
		final String statusParamName = "status";
		this.status = "INACTIVE";
		actualChanges.put(statusParamName, this.status);
		
		return actualChanges;

	}

	public void updateActiveTime() {
		this.sessionLastupdate = DateUtils.getDateOfTenant();
	}
}
