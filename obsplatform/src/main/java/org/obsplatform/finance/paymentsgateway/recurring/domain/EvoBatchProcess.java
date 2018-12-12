package org.obsplatform.finance.paymentsgateway.recurring.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

/**
 * Store the data of evo batch process file upload and download.
 * 
 *
 * @author krishnareddy
 *
 */
@Entity
@Table(name = "b_evo_batchprocess")
public class EvoBatchProcess extends AbstractAuditableCustom<AppUser,Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name = "input_filename")
	private String inputFileName;
	
	@Column(name = "path")
	private String path;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "is_uploaded")
	private char uploaded = 'N';
	
	@Column(name = "is_downloaded")
	private char downloaded = 'N';
	
	@Column(name = "status")
	private char status = 'W';
	
	public EvoBatchProcess(){
		
	}
	
	public EvoBatchProcess(final String inputFileName, final String path, final String description){
		this.inputFileName = inputFileName;
		this.description = description;
		this.path = path;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void isuploaded() {
		this.uploaded = 'Y';
	}
	
	public void isdownloaded() {
		this.downloaded = 'Y';
	}
	
	public void status(){
		this.status = 'P';
	}

}
