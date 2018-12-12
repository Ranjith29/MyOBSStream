/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.client.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.obsplatform.billing.selfcare.domain.SelfCare;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfiguration;
import org.obsplatform.infrastructure.codes.data.CodeValueData;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.organisation.address.data.AddressData;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.portfolio.client.service.ClientCategoryData;
import org.obsplatform.portfolio.client.service.GroupData;
import org.obsplatform.portfolio.group.data.GroupGeneralData;

/**
 * Immutable data object representing client data.
 */
final public class ClientData implements Comparable<ClientData> {

    private final Long id;
    private String accountNo;
    private final String externalId;

    private final EnumOptionData status;
    private final Boolean active;
    private final LocalDate activationDate;

    private final String firstname;
    private final String middlename;
    private final String lastname;
    private final String fullname;
    private String displayName;

    private final Long officeId;
    private final String officeName;

    private final String imageKey;
    @SuppressWarnings("unused")
    private final Boolean imagePresent;
    private final String email;
    private final String phone;
    private final String homePhoneNumber;
    private final String addressNo;
    private final String street;
    private final String city;
    private final String state;
    private final String country;
    private final String zip;
    private final BigDecimal balanceAmount;
    private final BigDecimal walletAmount;
    private final String hwSerialNumber;
    private final String taxExemption;
    private final String groupName;
    // associations
    private final Collection<GroupGeneralData> groups;

    // template
    private final Collection<OfficeData> officeOptions;
    private final Collection<ClientCategoryData> clientCategoryDatas;
	private final String categoryType;
	private AddressData addressTemplateData;
    private final List<String> hardwareDetails;
    private PaymentGatewayConfiguration configurationProperty;
    private Configuration loginConfigurationProperty;
    private PaymentGatewayConfiguration configurationPropertyforIos;
	private  final String currency;

	private final Collection<GroupData> groupNameDatas;
    private final Collection<CodeValueData> closureReasons;
    private Boolean balanceCheck;
    private final String  entryType;
    private SelfCare selfcare;
    private final String userName;
    private final String clientPassword;
    private final String title;
    private final String parentId;
	private ClientAdditionalData clientAdditionalData;
	private LocalDate date;
	private String registrationFee;
	private String plan;
	private String billingFrequency;
	private String contractPeriod;
	private String connectionType;
   

    public static ClientData template(final Long officeId, final LocalDate joinedDate, final Collection<OfficeData> officeOptions,
    		Collection<ClientCategoryData> categoryDatas,Collection<GroupData> groupDatas,List<CodeValueData> closureReasons) {
        return new ClientData(null, null,null, officeId, null, null, null, null, null, null, null, null, joinedDate, null, officeOptions, null,
        		categoryDatas,null,null,null, null, null, null, null, null, null, null,null,null,null,null,groupDatas,closureReasons,null,null,null,null,
        		 null,null,null,null,null);
        }
	
    public static ClientData template(final Long officeId, final LocalDate joinedDate, final Collection<OfficeData> officeOptions, Collection<ClientCategoryData> categoryDatas,
    		List<CodeValueData> closureReasons) {
        return new ClientData(null, null,null, officeId, null, null, null, null, null, null, null, null, joinedDate, null, officeOptions, null,
        		categoryDatas,null,null,null, null, null, null, null, null, null, null,null,null,null,null,null,closureReasons,null,null,null,null,null,null,null,null,null);
    }

    public static ClientData templateOnTop(final ClientData clientData, final List<OfficeData> allowedOffices, Collection<ClientCategoryData> categoryDatas,
    		Collection<GroupData> groupDatas, List<String> allocationDetailsDatas, String balanceCheck) {


        return new ClientData(clientData.accountNo,clientData.groupName, clientData.status, clientData.officeId, clientData.officeName, clientData.id,
                clientData.firstname, clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName,
                clientData.externalId, clientData.activationDate, clientData.imageKey, allowedOffices, clientData.groups,
                categoryDatas,clientData.categoryType,clientData.email,clientData.phone,clientData.homePhoneNumber,clientData.addressNo,clientData.street,
                clientData.city,clientData.state,clientData.country,clientData.zip,clientData.balanceAmount,allocationDetailsDatas,clientData.hwSerialNumber,
                clientData.currency, groupDatas,null,balanceCheck,clientData.taxExemption,clientData.entryType,clientData.walletAmount,null,null,null,clientData.title,clientData.registrationFee);
    }

    public static ClientData setParentGroups(final ClientData clientData, final Collection<GroupGeneralData> parentGroups) {
        return new ClientData(clientData.accountNo,clientData.groupName, clientData.status, clientData.officeId, clientData.officeName, clientData.id,

                clientData.firstname, clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName,
                clientData.externalId, clientData.activationDate, clientData.imageKey, clientData.officeOptions, parentGroups,
                clientData.clientCategoryDatas,clientData.categoryType,clientData.email,clientData.phone,clientData.homePhoneNumber,
                clientData.addressNo,clientData.street,clientData.city,clientData.state,clientData.country,clientData.zip,clientData.balanceAmount,
                clientData.hardwareDetails,clientData.hwSerialNumber,clientData.currency, clientData.groupNameDatas,null,null,clientData.taxExemption,clientData.entryType,
                clientData.walletAmount,null,null,null,clientData.title,clientData.registrationFee);

    }

    public static ClientData clientIdentifier(final Long id, final String accountNo, final EnumOptionData status, final String firstname,
            final String middlename, final String lastname, final String fullname, final String displayName, final Long officeId,
            final String officeName) {

        return new ClientData(accountNo,null, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName, null,
                null, null, null, null,null,null,null,null, null,null, null,null, null, null,null,null,null,null,null, null,null,null,null,null,null,null,null,null,null,null);
    }

    public static ClientData lookup(final Long id, final String displayName, final Long officeId, final String officeName) {
        return new ClientData(null,null, null, officeId, officeName, id, null, null, null, null, displayName, null, null, null, null, null,null,null,null,null,
        		null,null,null, null,null,null,null,null,null,null,null, null,null,null,null,null,null,null,null,null,null,null);

    }
    
    public static ClientData walletAmount(final Long id, final String accountNo, final BigDecimal walletAmount,final String hwSerialNumber) {
    	return new ClientData(accountNo,null, null, null, null, id, null, null, null, null, null, null, null, null, null, null,null,null,null,null,
    			null,null,null, null,null,null,null,null,null,hwSerialNumber,null, null,null,null,null,null,walletAmount,null,null,null,null,null);
    	
    }

    public static ClientData instance(final String accountNo, final String groupName, final EnumOptionData status, final Long officeId, final String officeName,final Long id, 
    		final String firstname, final String middlename, final String lastname, final String fullname,final String displayName, final String externalId,
    		final LocalDate activationDate, final String imageKey,final String categoryType,final String email,final String phone,final String homePhoneNumber,final String addrNo,final String street,
    		final String city,final String state,final String country,final String zip,final BigDecimal balanceAmount,final String hwSerialNumber,final String currency,final String taxExemption,
    		String entryType,final BigDecimal walletAmount,final String userName,final String clientPassword,final String parentId, String title, String registrationFee) {
    	
        return new ClientData(accountNo,groupName, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName,
                externalId, activationDate, imageKey, null, null,null,categoryType,email,phone,homePhoneNumber,addrNo,street,city,state,country,zip,
                balanceAmount,null,hwSerialNumber,currency, null,null,null,taxExemption,entryType,walletAmount,userName,clientPassword,parentId,title,registrationFee);

    }
    
    public static ClientData template(final Collection<OfficeData> officeOptions, Collection<ClientCategoryData> categoryDatas) {
        return new ClientData(null, null,null, null, null, null, null, null, null, null, null, null, null, null, officeOptions, null,
        		categoryDatas,null,null,null, null, null, null, null, null, null, null,null,null,null,null,null,null,null,null,null,null,
        		 null,null,null,null,null);
        }
    
    public static ClientData convertedClient(final Long id, final String firstname,final String email,final String address, final String city, 
			final String state, final String country) {
		
		return new ClientData(null, null,null, null, null, id, firstname, null, null, null, null, null, null, null, null, null,
        		null,null,email,null, null, address, null, city, state, country, null,null,null,null,null,null,null,null,null,null,null,
        		 null,null,null,null,null);
	}

    private ClientData(final String accountNo,final String groupName, final EnumOptionData status, final Long officeId, final String officeName, final Long id,final String firstname,
    		final String middlename, final String lastname, final String fullname, final String displayName,final String externalId, final LocalDate activationDate, 
    		final String imageKey, final Collection<OfficeData> allowedOffices,final Collection<GroupGeneralData> groups, Collection<ClientCategoryData> clientCategoryDatas,
    		final String categoryType,final String email,final String phone,final String homePhoneNumber,final String addrNo,final String street,final String city,final String state,
    		final String country,final String zip, BigDecimal balanceAmount,final List<String> hardwareDetails,final String hwSerialNumber,final String currency, Collection<GroupData> groupNameDatas, 
    		List<CodeValueData> closureReasons, String balanceCheck,final String taxExemption, String entryType,final BigDecimal walletAmount,final String userName,final String clientPassword,
    		final String parentId,final String title, final String registrationFee) {

        this.accountNo = accountNo;
        this.groupName=groupName;
        this.status = status;
        if (status != null) {
            active = status.getId().equals(300L);
        } else {
            active = null;
        }
        this.officeId = officeId;
        this.officeName = officeName;
        this.id = id;
        this.firstname = StringUtils.defaultIfEmpty(firstname, null);
        this.middlename = StringUtils.defaultIfEmpty(middlename, null);
        this.lastname = StringUtils.defaultIfEmpty(lastname, null);
        this.fullname = StringUtils.defaultIfEmpty(fullname, null);
        this.displayName = StringUtils.defaultIfEmpty(displayName, null);
        this.externalId = StringUtils.defaultIfEmpty(externalId, null);
        this.activationDate = activationDate;
        this.walletAmount=walletAmount;
        this.imageKey = imageKey;
        this.title = title;
        if (imageKey != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }
        this.closureReasons=closureReasons;

        // associations
        this.groups = groups;

        // template
        this.officeOptions = allowedOffices;
        this.clientCategoryDatas=clientCategoryDatas;
        this.groupNameDatas = groupNameDatas;
        this.categoryType=categoryType;
        this.email=email;
        this.phone=phone;
        this.homePhoneNumber=homePhoneNumber;
        this.addressNo= StringUtils.defaultIfEmpty(addrNo, null);
        this.street= StringUtils.defaultIfEmpty(street, null);
        this.city= StringUtils.defaultIfEmpty(city, null);
        this.state= StringUtils.defaultIfEmpty(state, null);
        this.country= StringUtils.defaultIfEmpty(country, null);
        this.zip= StringUtils.defaultIfEmpty(zip, null);
        if(balanceAmount==null){
        	balanceAmount=BigDecimal.ZERO;
		}
        this.balanceAmount=balanceAmount!=null?balanceAmount:BigDecimal.ZERO;
        this.hardwareDetails=hardwareDetails;
        this.hwSerialNumber=hwSerialNumber;
        this.currency=currency;
        this.taxExemption=taxExemption;
        this.entryType=entryType;
        if(balanceCheck !=null && balanceCheck.equalsIgnoreCase("Y")){
    	   this.setBalanceCheck(true);
        }
        else{
    	   this.setBalanceCheck(false);
        }
        this.userName = userName;
        this.clientPassword = clientPassword;
        this.parentId = parentId;
        this.registrationFee = registrationFee;

    }
      
	public Long id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public Long officeId() {
        return this.officeId;
    }

    public String officeName() {
        return this.officeName;
    }

    public String imageKey() {
        return this.imageKey;
    }

    public boolean imageKeyDoesNotExist() {
        return !imageKeyExists();
    }
    
    /**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}
	
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return the addressNo
	 */
	public String getAddressNo() {
		return addressNo;
	}

	/**
	 * @return the zip
	 */
	public String getZip() {
		return zip;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	public Boolean isActive() {
		return active;
	}

	private boolean imageKeyExists() {
        return StringUtils.isNotBlank(this.imageKey);
    }

    @Override
    public int compareTo(final ClientData obj) {
        if (obj == null) { return -1; }
        return new CompareToBuilder() //
                .append(this.id, obj.id) //
                .append(this.displayName, obj.displayName) //
                .toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        ClientData rhs = (ClientData) obj;
        return new EqualsBuilder() //
                .append(this.id, rhs.id) //
                .append(this.displayName, rhs.displayName) //
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37) //
                .append(this.id) //
                .append(this.displayName) //
                .toHashCode();
    }

    // TODO - kw - look into removing usage of the getters below
    public String getExternalId() {
        return this.externalId;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public LocalDate getActivationDate() {
        return this.activationDate;
    }

	public void setAddressTemplate(AddressData data) {
		this.setAddressTemplateData(data);
		
	}

	public PaymentGatewayConfiguration getConfigurationProperty() {
		return configurationProperty;
	}

	public void setConfigurationProperty(PaymentGatewayConfiguration paypalconfigurationProperty) {
		this.configurationProperty = paypalconfigurationProperty;
	}
	public void setConfigurationPropertyForIos(PaymentGatewayConfiguration paypalconfigurationPropertyForIos) {
		this.setConfigurationPropertyforIos(paypalconfigurationPropertyForIos);
	}

	public void setBalanceCheck(boolean isEnabled) {

		this.balanceCheck=isEnabled;
	}

	public SelfCare getSelfcare() {
		return selfcare;
	}

	public void setSelfcare(SelfCare selfcare) {
		this.selfcare = selfcare;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public AddressData getAddressTemplateData() {
		return addressTemplateData;
	}

	public void setAddressTemplateData(AddressData addressTemplateData) {
		this.addressTemplateData = addressTemplateData;
	}

	public PaymentGatewayConfiguration getConfigurationPropertyforIos() {
		return configurationPropertyforIos;
	}

	public void setConfigurationPropertyforIos(
			PaymentGatewayConfiguration paypalconfigurationPropertyForIos) {
		this.configurationPropertyforIos = paypalconfigurationPropertyForIos;
	}

	public Collection<CodeValueData> getClosureReasons() {
		return closureReasons;
	}

	public Boolean getBalanceCheck() {
		return balanceCheck;
	}

	public void setBalanceCheck(Boolean balanceCheck) {
		this.balanceCheck = balanceCheck;
	}

	public void setConfigurationProperty(Configuration configurationProperty) {
		this.loginConfigurationProperty=configurationProperty;
		
	}

	public void setClientAdditionalData(ClientAdditionalData clientAdditionalData) {

		  this.clientAdditionalData = clientAdditionalData;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public String getCurrency() {
		return currency;
	}

	public String getRegistrationFee() {
		return registrationFee;
	}

	public void setRegistrationFee(String registrationFee) {
		this.registrationFee = registrationFee;
	}


}