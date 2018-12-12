package org.obsplatform.billing.selfcare.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.obsplatform.portfolio.client.api.ClientApiConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_clientuser", uniqueConstraints = @UniqueConstraint(name = "username", columnNames = { "username", "unique_reference" }) )
public class SelfCare extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "username")
	private String userName;

	@Column(name = "password")
	private String password;

	@Column(name = "unique_reference")
	private String uniqueReference;

	@Column(name = "status")
	private String status;

	@Column(name = "auth_pin")
	private String authPin;

	@Column(name = "korta_token")
	private String token;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "zebra_subscriber_id")
	private Long zebraSubscriberId;

	@Column(name = "is_auto_billing")
	private char isAutoBilling = 'N';
	
	@Column(name = "is_worldpay_billing")
	private char isWorldpayBilling = 'N';
	
	@Column(name = "is_enable_marketing_mails")
	private char isEnableMarketingMails = 'Y';
	
	@Column(name = "is_deleted")
	private Boolean isDeleted = false;

	public SelfCare() {

	}

	public SelfCare(final Long clientId, final String userName, final String password, final String uniqueReference, Boolean isDeleted, final String device) {
		
		this.clientId = clientId;
		this.userName = userName;
		this.password =  (password.isEmpty() | password == null ) ? new RandomPasswordGenerator(8).generate().toString() : password ;
		this.uniqueReference = uniqueReference;
		this.isDeleted = isDeleted;
		this.status = "INACTIVE";
		this.deviceId = device;
	}

	public static SelfCare fromJson(final JsonCommand command) {
		
		final String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
		final String password = command.stringValueOfParameterNamed("password");
		final String device = command.stringValueOfParameterNamed("device");
		final Long clientId = command.longValueOfParameterNamed("clientId");
		
		return new SelfCare(clientId, uniqueReference, password, uniqueReference, false, device);
	}

	public static SelfCare fromJsonODP(final JsonCommand command) {
		
		final String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
		final String password = command.stringValueOfParameterNamed("password");
		SelfCare selfCare = new SelfCare();
		selfCare.setUserName(uniqueReference);
		selfCare.setUniqueReference(uniqueReference);
		selfCare.setPassword(password);
		selfCare.setIsDeleted(false);
		selfCare.setStatus("ACTIVE");
		return selfCare;

	}
	
	public Map<String, Object> update(final JsonCommand command) {
		
		 final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);

		if (command.isChangeInStringParameterNamed(ClientApiConstants.userNameParamName, this.userName)) {
			final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.userNameParamName);
			actualChanges.put(ClientApiConstants.userNameParamName, newValue);
			this.userName = newValue; // StringUtils.defaultIfEmpty(newValue,null);
		}

		if (command.parameterExists(ClientApiConstants.uniqueReferenceParamName)) {
			if (command.isChangeInStringParameterNamed(ClientApiConstants.uniqueReferenceParamName,this.uniqueReference)) {
				final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.uniqueReferenceParamName);
				actualChanges.put(ClientApiConstants.uniqueReferenceParamName, newValue);
				this.uniqueReference = newValue; // StringUtils.defaultIfEmpty(newValue,null);
			}
		}

		if (command.parameterExists(ClientApiConstants.emailParamName)) {
			if (command.isChangeInStringParameterNamed(ClientApiConstants.emailParamName, this.uniqueReference)) {
				final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.emailParamName);
				actualChanges.put(ClientApiConstants.uniqueReferenceParamName, newValue);
				this.uniqueReference = newValue;// StringUtils.defaultIfEmpty(newValue,null);
			}
		}

		if (command.isChangeInStringParameterNamed(ClientApiConstants.passwordParamName, this.password)) {
			final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.passwordParamName);
			actualChanges.put(ClientApiConstants.passwordParamName, newValue);
			this.password = newValue; // StringUtils.defaultIfEmpty(newValue,null);
		}

		return actualChanges;
	}


	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUniqueReference() {
		return uniqueReference;
	}

	public void setUniqueReference(String uniqueReference) {
		this.uniqueReference = uniqueReference;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
		this.uniqueReference = "DEL_" + getId() + "_" + this.uniqueReference;
		this.userName = "DEL_" + getId() + "_" + this.userName;

	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAuthPin() {
		return authPin;
	}

	public void setAuthPin(String authPin) {
		this.authPin = authPin;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getZebraSubscriberId() {
		return zebraSubscriberId;
	}

	public void setZebraSubscriberId(Long zebraSubscriberId) {
		this.zebraSubscriberId = zebraSubscriberId;
	}

	public char getIsAutoBilling() {
		return isAutoBilling;
	}

	public void setIsAutoBilling(boolean isAutoBilling) {
		this.isAutoBilling = isAutoBilling == true ? 'Y' : 'N'; 	
	}
	
	public char getIsWorldpayBilling() {
		return isWorldpayBilling;
	}

	public void setIsWorldpayBilling(boolean isWorldpayBilling) {
		this.isWorldpayBilling = isWorldpayBilling == true ? 'Y' : 'N';
	}

	public char getIsEnableMarketingMails() {
		return isEnableMarketingMails;
	}

	public void setIsEnableMarketingMails(boolean isEnableMarketingMails) {
		this.isEnableMarketingMails = isEnableMarketingMails == true ? 'Y' : 'N';
	}
	


}
