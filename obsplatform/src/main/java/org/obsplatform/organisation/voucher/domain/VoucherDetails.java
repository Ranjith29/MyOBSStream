package org.obsplatform.organisation.voucher.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Entity class, Used to Store the 
 * VoucherPin details to b_pin_details table.
 * 
 * @author ashokreddy
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "b_pin_details")
public class VoucherDetails extends AbstractPersistable<Long> {
	
	@ManyToOne
    @JoinColumn(name="pin_id", nullable = false)
    private Voucher voucher;
	
	@Column(name = "pin_no")
	private String pinNo;
	
	@Column(name = "serial_no", nullable = false)
	private Long serialNo;
	
	@Column(name = "client_id", nullable = true)
	private Long clientId;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "sale_date")
	private Date saleDate;
	
	@Column(name = "is_deleted")
	private char isDeleted = 'N';
	
	@Column(name = "cancel_reason")
	private String cancelReason;
	
	/**
	 * Default/Zero-Parameterized Constructor
	 */
	public VoucherDetails(){
		
	}

	/**
	 * Constructor, Used to assign the values 
	 * To entity class to Store Record in Database
	 * @param voucherpin
	 * 			Randomly Generated Voucher Pin
	 * @param serialNo
	 * 			Serial Number of the Generated Voucher Pin. 
	 * 			To Identify the Record in a DB.
	 * @param voucher
	 * 			Voucher(b_pin_master) class object, 
	 * 			This Voucher Object id Act as foreign reference
	 */
	public VoucherDetails(final String voucherpin, final Long serialNo, final Voucher voucher) {
		super();
		this.pinNo = voucherpin;
		this.serialNo = serialNo;
		this.voucher = voucher;
		this.status = "NEW";
	}

	public String getPinNo() {
		return pinNo;
	}

	public Long getSerialNo() {
		return serialNo;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setPinNo(String pinNo) {
		this.pinNo = pinNo;
	}

	public void setSerialNo(Long serialNo) {
		this.serialNo = serialNo;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Voucher getVoucher() {
		return voucher;
	}

	public void setVoucher(Voucher voucher) {
		this.voucher = voucher;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getSaleDate() {
		return saleDate;
	}

	public void setSaleDate(Date saleDate) {
		this.saleDate = saleDate;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void update(String cancelReason) {
		this.cancelReason = cancelReason;
		this.clientId = null;
		this.saleDate = null;
		this.status = "NEW";
		
	}

		
	
	

}
