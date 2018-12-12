package org.obsplatform.portfolio.plan.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Naresh
 * 
 */
@Entity
@Table(name = "b_plan_category_detail")
public class PlanCategoryDetails {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@ManyToOne
    @JoinColumn(name="plan_id")
    private Plan plan;
	
	@Column(name = "client_category_id", nullable = false)
	private Long clientCategoryId;

	@Column(name = "is_deleted", nullable = false)
	private char isDeleted = 'N';

	public PlanCategoryDetails() {

	}

	public PlanCategoryDetails(Long clientCategoryId) {
		
		this.clientCategoryId = clientCategoryId;
	}

	public Long getId() {
		return id;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public char isIsDeleted() {
		return isDeleted;
	}

	public Long getClientCategoryId() {
		return clientCategoryId;
	}

	public void setClientCategoryId(Long clientCategoryId) {
		this.clientCategoryId = clientCategoryId;
	}

	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public void delete() {
		this.isDeleted = 'Y';

	}

	public void update(Plan plan) {
		this.plan = plan;
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}
	
	
}