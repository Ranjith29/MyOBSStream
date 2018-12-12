package org.obsplatform.finance.paymentsgateway.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

	/**
	 * 
	 * @author raghu
	 *
	 */
	@Entity
	@Table(name = "b_evo_notify")
	public class EvoNotify extends AbstractPersistable<Long> {
		
		@Column(name = "blowfish_data")
		private String blowfishData;

		@Column(name="length")
		private Long length;
		
		@Column(name = "tenant")
		private String tenant;
		
		@Column(name = "client_id")
		private Long clientId;
		
		@Column(name = "status")
		private String status;
		
		
		public EvoNotify(){
			
		}
		
		public EvoNotify(final String blowfishData, final Long length) {
			
			this.blowfishData=blowfishData;
			this.length=length;
		}

		public Long getClientId() {
			return clientId;
		}

		public void setClientId(Long clientId) {
			this.clientId = clientId;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
		
		

}
