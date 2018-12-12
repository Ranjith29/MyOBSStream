package org.obsplatform.portfolio.client.service;

import java.util.List;

public class ClientCategoryData {
	
	private final Long id;
	private final String categoryType;
	private final String billMode;
	private final String accountNo;
	private final String displayName;
	private final String displayLabel;
	private Boolean count;
	private List<ClientCategoryData> parentClientData;

	public ClientCategoryData(final Long id,final String categoryType,final String billMode,final String accountNo,final String displayName,
			final List<ClientCategoryData> parentClientData,final Boolean count) {
           this.id=id;
           this.categoryType=categoryType;
           this.billMode = billMode;
           this.accountNo = accountNo;
           this.displayName = displayName;
           if(displayName!=null){
           this.displayLabel = generateLabelName();
           }else{
        	   this.displayLabel = null; 
           }
           this.count=count;
           this.setParentClientData(parentClientData);
	}

	private String generateLabelName() {
		 StringBuilder builder = new StringBuilder(this.displayName).append('[').append(this.accountNo).append(']');
		 //builder.append('[').append(this.accountNo).append(']');
		 return builder.toString();
	}

	public Long getId() {
		return id;
	}

	public String getCategoryType() {
		return categoryType;
	}

	public String getBillMode() {
		return billMode;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public Boolean getCount() {
		return count;
	}
    
	public Boolean setCount(Boolean count){
		return this.count=count;
	}

	public List<ClientCategoryData> getParentClientData() {
		return parentClientData;
	}
	
	public void setParentClientData(List<ClientCategoryData> parentClientData) {
		this.parentClientData = parentClientData;
	}
	
}
