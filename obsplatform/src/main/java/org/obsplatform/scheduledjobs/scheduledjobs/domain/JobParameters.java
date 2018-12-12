package org.obsplatform.scheduledjobs.scheduledjobs.domain;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.jobs.api.SchedulerJobApiConstants;
import org.obsplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "job_parameters")
public class JobParameters extends AbstractPersistable<Long>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
    @JoinColumn(name="job_id")
    private ScheduledJobDetail jobDetail;

	@Column(name ="param_name")
    private String paramName;
	
	@Column(name ="param_type")
    private String paramType;
	
	@Column(name ="param_default_value")
    private String paramDefaultValue;
	
	@Column(name ="param_value")
    private String paramValue;
	
	@Column(name ="query_values")
    private String queryValues;


	@Column(name = "is_dynamic")
	private String isDynamic;


	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMMM yyyy", new Locale("en"));
	
	public JobParameters()
	{}


	public JobParameters(Long id, Long jobId, String paramName,String paramType, String defaultValue,
			String paramValue,String isDynamic, String queryValue) {
		
		this.paramName=paramName;
		this.paramType=paramType;
		this.paramDefaultValue=defaultValue;
		this.paramValue=paramValue;
		this.isDynamic=isDynamic;
		this.queryValues=queryValue;
		
		
	}


	public ScheduledJobDetail getJobDetail() {
		return jobDetail;
	}


	public String getParamName() {
		return paramName;
	}


	public String getParamType() {
		return paramType;
	}


	public String getParamDefaultValue() {
		return paramDefaultValue;
	}


	public String getParamValue() {
		return paramValue;
	}


	public String getQueryValues() {
		return queryValues;
	}


	public String isDynamic() {
		return isDynamic;
	}


	public void update(JsonCommand command) {

		final String processDateParam = "processDate";
		final LocalDate processDate = command.localDateValueOfParameterNamed(processDateParam);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobProcessdate) && processDate != null) {
			this.paramValue = simpleDateFormat.format(processDate.toDate());;
		}

		final String descriptionParamName = "messageTemplate";
		final String messagetemplate = command.stringValueOfParameterNamed(descriptionParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobMessageTemplate) && messagetemplate != null) {
			this.paramValue = messagetemplate;
		}

		final String duedateParamName = "dueDate";
		final String isDynamicParam = "isDynamic";
		final boolean isDynamicDate = command.booleanPrimitiveValueOfParameterNamed(isDynamicParam);
		final LocalDate duedate = command.localDateValueOfParameterNamed(duedateParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobDueDate) && duedate != null) {
			this.paramValue = simpleDateFormat.format(duedate.toDate());
			this.isDynamic = isDynamicDate ? "Y" : "N";
		}

		final String reportNameParamName = "reportName";
		final String isDynamicParamName = "isDynamic";
		final String reportName = command.stringValueOfParameterNamed(reportNameParamName);
		final boolean isDynamic = command.booleanPrimitiveValueOfParameterNamed(isDynamicParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobReportName) && reportName != null) {
			this.paramValue = reportName;
			this.isDynamic = isDynamic ? "Y" : "N";	
		}

		final String isAutoRenewalParamName = "isAutoRenewal";
		final boolean isAutoRenewal = command.booleanPrimitiveValueOfParameterNamed(isAutoRenewalParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobisAutoRenewal)) {
			this.isDynamic = this.paramValue = isAutoRenewal ? "Y" : "N";	
			/*if (isAutoRenewal) {
				this.isDynamic = "Y";
				this.paramValue = "Y";
			} else
				this.isDynamic = "N";
			this.paramValue = "N";*/
		}

		final String addonExipiryParamName = "addonExipiry";
		final boolean addonExipiry = command.booleanPrimitiveValueOfParameterNamed(addonExipiryParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobisAddonExipiry)) {
			this.paramValue = addonExipiry ? "Y" : "N";	
		}
		
		final String messageParamName = "promotionalMessage";
		final String promotionalMessage = command.stringValueOfParameterNamed(messageParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobPromotionalMessage) && promotionalMessage != null) {
			this.paramValue = promotionalMessage;
		}

		final String exipiryDateParamName = "exipiryDate";
		final LocalDate exipiryDate = command.localDateValueOfParameterNamed(exipiryDateParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobExipiryDate) && exipiryDate != null) {
			this.paramValue = simpleDateFormat.format(exipiryDate.toDate());;
		}

		final String ReportEmailParamName = "emailId";
		final String ReportEmail = command.stringValueOfParameterNamed(ReportEmailParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_EmailId) && ReportEmail != null) {
			this.paramValue = ReportEmail;
		}

		final String ProvisioningParamName = "system";
		final String Provisioning = command.stringValueOfParameterNamed(ProvisioningParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_ProvSystem) && Provisioning != null) {
			this.paramValue = Provisioning;
		}

		final String UrlParamName = "URL";
		final String url = command.stringValueOfParameterNamed(UrlParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_URL) && url != null) {
			this.paramValue = url;
		}

		final String UsernameParamName = "Username";
		final String Username = command.stringValueOfParameterNamed(UsernameParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_Username) && Username != null) {
			this.paramValue = Username;
		}

		final String PasswordParamName = "Password";
		final String Password = command.stringValueOfParameterNamed(PasswordParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_Password) && Password != null) {
			this.paramValue = Password;
		}

		final String createticket = "isCreateTicket";
		final boolean ticket = command.booleanPrimitiveValueOfParameterNamed(createticket);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobTicket)) {
			this.isDynamic = ticket ? "Y" : "N";
		}

		final String updateStatus = "isUpdateStatus";
		final boolean status = command.booleanPrimitiveValueOfParameterNamed(updateStatus);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.statusParamName)) {
			this.isDynamic = status ? "Y" : "N";
		}
		
		final String valueParamName = "value";
		final String value = command.stringValueOfParameterNamed(valueParamName);
		if (this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_VALUE) && value != null) {
			this.paramValue = value;
		}
	}
}