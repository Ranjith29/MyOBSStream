package org.obsplatform.portfolio.plan.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.api.JsonCommand;

/**
 * @author hugo
 *
 */
@Entity
@Table(name = "b_plan_detail")
public class PlanDetails {

	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;

	@ManyToOne
    @JoinColumn(name="plan_id")
    private Plan plan;

	@Column(name ="service_code", length=50)
    private String serviceCode;


	@Column(name = "is_deleted", nullable = false)
	private char isDeleted = 'n';


	public PlanDetails()
	{
		  // This constructor is intentionally empty. Nothing special is needed here.
	}
	public PlanDetails(final String serviceCode)
	{

		this.serviceCode=serviceCode;
		this.plan=null;

	}
	
	public Long getId() {
		return id;
	}
	public char getIsDeleted() {
		return isDeleted;
	}
	public String getServiceCode() {
		return serviceCode;
	}


	public char isIsDeleted() {
		return isDeleted;
	}



	public Plan getPlan() {
		return plan;
	}

	public void update(final Plan plan)
	{
		this.plan=plan;
	}
	public void delete() {
		this.isDeleted='y';

	}
	public static PlanDetails fromJson(final JsonCommand command) {
		
		    final String serviceCode = command.stringValueOfParameterNamed("serviceCode");
		    return new PlanDetails(serviceCode);
		
	}
}