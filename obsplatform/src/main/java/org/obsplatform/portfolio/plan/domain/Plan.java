package org.obsplatform.portfolio.plan.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.api.JsonCommand;

@Entity
@Table(name = "b_plan_master", uniqueConstraints = {@UniqueConstraint(name = "uplan_code_key", columnNames = { "plan_code" }),@UniqueConstraint(name = "plan_description", columnNames = { "plan_description" })})
public class Plan{


	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;

	@Column(name = "plan_code", length = 65536)
	private String planCode;

	@Column(name = "plan_description")
	private String description;

	@Column(name = "plan_status")
	private Long status;
	

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "bill_rule")
	private Long billRule;

	
	@Column(name = "is_deleted", nullable = false)
	private char deleted='N';
	
	@Column(name = "is_prepaid", nullable = false)
	private char isPrepaid='N';
	
	@Column(name = "allow_topup", nullable = false)
	private char allowTopup='N';
	
	@Column(name = "is_hw_req", nullable = false)
	private char isHwReq;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "plan", orphanRemoval = true)
	private Set<PlanDetails> planDetails = new HashSet<PlanDetails>();
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "plan", orphanRemoval = true)
	private Set<PlanEvent> planEvents = new HashSet<PlanEvent>();
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "plan", orphanRemoval = true)
	private Set<PlanCategoryDetails> planCategoryDetails = new HashSet<PlanCategoryDetails>();
	
	/*//@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "plan", orphanRemoval = true)
	private Set<PlanQualifier> planQualifier = new HashSet<PlanQualifier>();*/
	
	@Column(name = "provision_sys")
	private String provisionSystem;
	
	@Column(name="plan_Notes")
	private String planNotes;
	
	@Column(name="trial_days")
	private Long trialDays;
	
	
	public Plan() {
		// TODO Auto-generated constructor stub
	}

	public Plan(final String code, final String description,final LocalDate start_date, final LocalDate endDate,
			final Long bill_rule, final Long status,final List<PlanDetails> details,final String provisioingSystem,
			final boolean isPrepaid,final boolean allowTopup,final boolean isHwReq,final String planNotes,final Long trialDays) {
			
				this.planCode = code;
				this.description = description;
				if (endDate != null)
				  this.endDate = endDate.toDate();
				this.startDate = start_date.toDate();
				this.status = status;
				this.billRule = bill_rule;
				this.provisionSystem=provisioingSystem;
				this.isPrepaid=isPrepaid?'Y':'N';
				this.allowTopup=allowTopup?'Y':'N';
				this.isHwReq=isHwReq?'Y':'N';
				if(planNotes!="")
				  this.planNotes=planNotes;
				this.trialDays=trialDays;
		}

	public Set<PlanDetails> getDetails() {
			return planDetails;
		}

		

		public String getCode() {
		return planCode;
		}

		public String getDescription() {
			return description;
		}
		public Long getStatus() {
			return status;
		}
		
		public Long getId() {
			return id;
		}

		public String getPlanCode() {
			return planCode;
		}

		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public Long getBillRule() {
			return billRule;
		}

		public char isDeleted() {
			return deleted;
		}

		public void addServieDetails(final Set<PlanDetails> selectedServices) {
			this.planDetails.clear();
			for(PlanDetails planDetails:selectedServices){
				planDetails.update(this);
				this.planDetails.add(planDetails);
			}
		}
		
		public void addEvents(final Set<PlanEvent> selectedEvents) {
			this.planEvents.clear();
			for(PlanEvent planEvents:selectedEvents){
				planEvents.update(this);
				this.planEvents.add(planEvents);
			}
		}
		
		public void addCategoryDetails(final Set<PlanCategoryDetails> selectedClientCategorys) {
			this.planCategoryDetails.clear();
			for(PlanCategoryDetails planCategoryDetails:selectedClientCategorys){
				planCategoryDetails.update(this);
				this.planCategoryDetails.add(planCategoryDetails);
			}
		}
		
		public void clearEvents(){
			this.planEvents.clear();
		}

		public char isHardwareReq() {
			return isHwReq;
		}

		public char getDeleted() {
			return deleted;
		}

		public String getProvisionSystem() {
			return provisionSystem;
		}

		public void delete() {
				this.deleted = 'Y';
				this.planCode=this.planCode+"_"+this.getId()+"_DELETED";
				for(PlanDetails planDetails:this.planDetails){
					planDetails.delete();
				}
				for(PlanCategoryDetails planCategoryDetails:this.planCategoryDetails){
					planCategoryDetails.delete();
				}
		}

	public Map<String, Object> update(final JsonCommand command) {
		 
		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		final String firstnameParamName = "planCode";
			if (command.isChangeInStringParameterNamed(firstnameParamName, this.planCode)) {
				final String newValue = command.stringValueOfParameterNamed(firstnameParamName);
	            actualChanges.put(firstnameParamName, newValue);
	            this.planCode = StringUtils.defaultIfEmpty(newValue, null);
	        }
	    final String descriptionParamName = "planDescription";
	        	if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
	        		final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
	        		actualChanges.put(firstnameParamName, newValue);
	        		this.description = StringUtils.defaultIfEmpty(newValue, null);
	        	}
	    final String provisioingSystem = "provisioingSystem";
	        if (command.isChangeInStringParameterNamed(provisioingSystem, this.provisionSystem)) {
	            final String newValue = command.stringValueOfParameterNamed(provisioingSystem);
	            actualChanges.put(provisioingSystem, newValue);
	            this.provisionSystem = StringUtils.defaultIfEmpty(newValue, null);
	        }
	        
	        final String servicesParamName = "services";
	        if (command.isChangeInArrayParameterNamed(servicesParamName, getServicesAsIdStringArray())) {
	            final String[] newValue = command.arrayValueOfParameterNamed(servicesParamName);
	            actualChanges.put(servicesParamName, newValue);
	        }
	        
	        final String eventsParamName = "events";
	        if (command.isChangeInArrayParameterNamed(eventsParamName, getEventsAsIdStringArray())) {
	            final String[] newValue = command.arrayValueOfParameterNamed(eventsParamName);
	            actualChanges.put(eventsParamName, newValue);
	        }
	        

	        final String clientCategorysParamName = "clientCategorys";
	        if (command.isChangeInArrayParameterNamed(clientCategorysParamName, getlientCategorysAsIdStringArray())) {
	            final String[] newValue = command.arrayValueOfParameterNamed(clientCategorysParamName);
	            actualChanges.put(clientCategorysParamName, newValue);
	        }
	        
	    final String startDateParamName = "startDate";
			if (command.isChangeInLocalDateParameterNamed(startDateParamName,
					new LocalDate(this.startDate))) {
				final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
				actualChanges.put(startDateParamName, newValue);
				this.startDate=newValue.toDate();
			}
			
		final String endDateParamName = "endDate";
				if (command.isChangeInLocalDateParameterNamed(endDateParamName,new LocalDate(this.endDate))) {
					final LocalDate newValue = command.localDateValueOfParameterNamed(endDateParamName);
					actualChanges.put(endDateParamName, newValue);
					if(newValue!=null)
						this.endDate=newValue.toDate();
					else
						this.endDate=null;
				}
	    final String billRuleParamName = "billRule";
				if (command.isChangeInLongParameterNamed(billRuleParamName,this.billRule)) {
					final Long newValue = command.longValueOfParameterNamed(billRuleParamName);
					actualChanges.put(billRuleParamName, newValue);
					this.billRule=newValue;
				}
	    final String statusParamName = "status";
				if (command.isChangeInLongParameterNamed(statusParamName,this.status)) {
					final Long newValue = command.longValueOfParameterNamed(statusParamName);
					actualChanges.put(statusParamName, newValue);
					this.status=newValue;
				}
	   final String planNotesParamName = "planNotes";
			   if (command.isChangeInStringParameterNamed(planNotesParamName, this.planNotes)) {
						final String newValue = command.stringValueOfParameterNamed(planNotesParamName);
			            actualChanges.put(planNotesParamName, newValue);
			            this.planNotes = StringUtils.defaultIfEmpty(newValue, null);
			   }
			   
	   final String trialDaysParamName = "trialDays";
			 if (command.isChangeInLongParameterNamed(trialDaysParamName,this.trialDays)) {
					final Long newValue = command.longValueOfParameterNamed(trialDaysParamName);
					actualChanges.put(trialDaysParamName, newValue);
					this.trialDays=newValue;
			 }
		final boolean isPrepaid=command.booleanPrimitiveValueOfParameterNamed("isPrepaid");
				final char isPrepaidParamName =isPrepaid?'Y':'N';
				this.isPrepaid=isPrepaidParamName;
						
	
		final boolean allowTopupParamName =command.booleanPrimitiveValueOfParameterNamed("allowTopup");
				this.allowTopup=allowTopupParamName?'Y':'N';
	          
	    
	    final boolean isHwReqParamName =command.booleanPrimitiveValueOfParameterNamed("isHwReq");
				this.isHwReq=isHwReqParamName?'Y':'N';
	            return actualChanges;
	            
	}


	private String[] getServicesAsIdStringArray() {
		 	
		 final List<String> roleIds = new ArrayList<>();
        	for (final PlanDetails details : this.planDetails) {
        		roleIds.add(details.getId().toString());
        	}
        	return roleIds.toArray(new String[roleIds.size()]);
	 }
	 
	 private String[] getEventsAsIdStringArray() {
		 	
		 final List<String> roleIds = new ArrayList<>();
        	for (final PlanEvent events : this.planEvents) {
        		roleIds.add(events.getId().toString());
        	}
        	return roleIds.toArray(new String[roleIds.size()]);
	 }
	 
	 private String[] getlientCategorysAsIdStringArray() {
		 	
		 final List<String> roleIds = new ArrayList<>();
        	for (final PlanCategoryDetails planCategoryDetails : this.planCategoryDetails) {
        		roleIds.add(planCategoryDetails.getId().toString());
        	}
        	return roleIds.toArray(new String[roleIds.size()]);
	 }

	public static Plan fromJson(JsonCommand command) {
		 
		final String planCode = command.stringValueOfParameterNamed("planCode");
		final String planDescription = command.stringValueOfParameterNamed("planDescription");
		final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
		final LocalDate endDate = command.localDateValueOfParameterNamed("endDate");
		final Long status = command.longValueOfParameterNamed("status");
		final Long billRule = command.longValueOfParameterNamed("billRule");
		final String provisioingSystem=command.stringValueOfParameterNamed("provisioingSystem");
		final boolean isPrepaid=command.booleanPrimitiveValueOfParameterNamed("isPrepaid");
		final boolean allowTopup=command.booleanPrimitiveValueOfParameterNamed("allowTopup");
		final boolean isHwReq=command.booleanPrimitiveValueOfParameterNamed("isHwReq");
		final String planNotes=command.stringValueOfParameterNamed("planNotes");
		final Long trialDays = command.longValueOfParameterNamed("trialDays");
		   
		return new Plan(planCode,planDescription,startDate,endDate,billRule,status,null,provisioingSystem,isPrepaid,allowTopup,isHwReq,planNotes,trialDays);
	}

	/**
	 * @return the isPrepaid
	 */
	public char isPrepaid() {
		return isPrepaid;
	}

	/**
	 * @return the allowTopup
	 */
	public char getAllowTopup() {
		return allowTopup;
	}

	public char getIsPrepaid() {
		return isPrepaid;
	}

	public void setIsPrepaid(char isPrepaid) {
		this.isPrepaid = isPrepaid;
	}

	public String getPlanNotes() {
		return planNotes;
	}

	public void setPlanNotes(String planNotes) {
		this.planNotes = planNotes;
	}
	
	public Long getTrialDays() {
		return trialDays;
	}

	public void setTrialDays(Long trialDays) {
		this.trialDays = trialDays;
	}


/*	public void addPlanQualifierDetails(PlanQualifier planQualifier) {
		planQualifier.update(this);
		this.planQualifier.add(planQualifier);
		
	}*/
		
	}

	

	

