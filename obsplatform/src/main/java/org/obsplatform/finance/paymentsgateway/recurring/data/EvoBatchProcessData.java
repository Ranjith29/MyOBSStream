package org.obsplatform.finance.paymentsgateway.recurring.data;

public class EvoBatchProcessData {
	
	private Long id;
	private String fileName;
	private String filepath;
	private String description;
	
	public EvoBatchProcessData(){
		
	}
	
	public EvoBatchProcessData(final Long id, final String fileName, final String filepath, final String description){
		
		this.id = id;
		this.fileName = fileName;
		this.filepath = filepath;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
