package org.obsplatform.portfolio.order.domain;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_orders_history")
public class OrderHistory extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "transaction_type")
	private String transactionType;

	@Column(name = "transaction_date")
	private Date transactionDate;

	@Column(name = "actual_date")
	private Date actualDate;

	@Column(name = "prepare_id")
	private Long prepareId;
	
	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "createdby_id")
	private Long createdbyId;
	
	@Column(name = "remarks")
	private String remarks;


	 public OrderHistory() {
			
	}

	public OrderHistory(final Long orderId, final LocalDate transactionDate, final LocalDate actualDate,
			final Long provisioningId, final String tranType, final Long userId, final String extensionReason) {
		
		this.orderId=orderId;
		this.transactionDate=transactionDate.toDate();
		this.actualDate=actualDate.toDate();
		this.prepareId=provisioningId;
		this.transactionType=tranType;
		this.createdbyId=userId;
		this.createdDate=DateUtils.getDateOfTenant();
		this.remarks=extensionReason;
	}
	
}
