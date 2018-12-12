/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.client.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.billing.selfcare.domain.SelfCare;
import org.obsplatform.billing.selfcare.domain.SelfcareRepositoryWrapper;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfiguration;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayConfigurationRepositoryWrapper;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.address.data.AddressData;
import org.obsplatform.organisation.address.service.AddressReadPlatformService;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.service.OfficeReadPlatformService;
import org.obsplatform.portfolio.allocation.service.AllocationReadPlatformService;
import org.obsplatform.portfolio.client.data.ClientAdditionalData;
import org.obsplatform.portfolio.client.data.ClientData;
import org.obsplatform.portfolio.client.service.ClientCategoryData;
import org.obsplatform.portfolio.client.service.ClientReadPlatformService;
import org.obsplatform.portfolio.client.service.GroupData;
import org.obsplatform.portfolio.group.service.SearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients")
@Component
@Scope("singleton")
public class ClientsApiResource {

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final ToApiJsonSerializer<ClientData> toApiJsonSerializer;
    private final ToApiJsonSerializer<ClientAdditionalData> jsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final AddressReadPlatformService addressReadPlatformService;
    private final AllocationReadPlatformService allocationReadPlatformService;
    private final ConfigurationRepository configurationRepository;
    private final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper;
    private final SelfcareRepositoryWrapper selfCareRepository;
    private final MCodeReadPlatformService codeReadPlatformService;

    @Autowired
    public ClientsApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final ToApiJsonSerializer<ClientData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,final AddressReadPlatformService addressReadPlatformService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,final AllocationReadPlatformService allocationReadPlatformService,
            final ConfigurationRepository configurationRepository, final PaymentGatewayConfigurationRepositoryWrapper paymentGatewayConfigurationRepositoryWrapper,
            final SelfcareRepositoryWrapper selfCareRepository,final MCodeReadPlatformService mCodeReadPlatformService,
            final ToApiJsonSerializer<ClientAdditionalData> jsonSerializer) {
        
    	this.context = context;
    	this.paymentGatewayConfigurationRepositoryWrapper=paymentGatewayConfigurationRepositoryWrapper;
        this.clientReadPlatformService = readPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.addressReadPlatformService=addressReadPlatformService;
        this.allocationReadPlatformService=allocationReadPlatformService;
        this.codeReadPlatformService = mCodeReadPlatformService;
        this.configurationRepository=configurationRepository;
        this.selfCareRepository = selfCareRepository;
        this.jsonSerializer = jsonSerializer;
    }
    
    /**
     * this method is using for getting template data to create a client
     */
    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo,  @QueryParam("commandParam") final String commandParam) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        ClientData clientData = this.clientReadPlatformService.retrieveTemplate();
        if (is(commandParam, "close")) {
        	
        clientData = this.clientReadPlatformService.retrieveAllClosureReasons(ClientApiConstants.CLIENT_CLOSURE_REASON);
        }
        final Configuration configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_LOGIN);
        clientData.setConfigurationProperty(configurationProperty);
        clientData=handleAddressTemplateData(clientData);
        clientData.setDate(DateUtils.getLocalDateOfTenantForClient());
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
    @Path("additionalinfo/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClientAdditionalInfoTemplate(@Context final UriInfo uriInfo,  @QueryParam("commandParam") final String commandParam) {

         	Collection<MCodeData> genderDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_GENDER);
         	Collection<MCodeData> nationalityDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_NATIONALITY);
         	Collection<MCodeData> customeridentificationDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_CUSTOMER_IDENTIFIER);
         	Collection<MCodeData> cummunitcationDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PREFER_COMMUNICATION);
         	Collection<MCodeData> languagesDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PREFER_LANG);
         	Collection<MCodeData> ageGroupDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_AGE_GROUP);
         	ClientAdditionalData  clientAdditionalData = new ClientAdditionalData(genderDatas,nationalityDatas,customeridentificationDatas,cummunitcationDatas,
         			languagesDatas,ageGroupDatas);
         	clientAdditionalData.setDate(DateUtils.getLocalDateOfTenantForClient());
         	
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, clientAdditionalData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    private ClientData handleAddressTemplateData(final ClientData clientData) {
    	
    	 final List<String> countryData = this.addressReadPlatformService.retrieveCountryDetails();
         final List<String> statesData = this.addressReadPlatformService.retrieveStateDetails();
         final List<String> citiesData = this.addressReadPlatformService.retrieveCityDetails();
         final List<EnumOptionData> enumOptionDatas = this.addressReadPlatformService.addressType();
         final AddressData data=new AddressData(null,countryData,statesData,citiesData,enumOptionDatas);
         clientData.setAddressTemplate(data);
         return clientData;
	}

	@GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@QueryParam("sqlSearch") final String sqlSearch, @QueryParam("officeId") final Long officeId,
            @QueryParam("externalId") final String externalId, @QueryParam("displayName") final String displayName,
            @QueryParam("firstName") final String firstname, @QueryParam("lastName") final String lastname,
            @QueryParam("underHierarchy") final String hierarchy, @QueryParam("offset") final Integer offset,
            @QueryParam("limit") final Integer limit, @QueryParam("orderBy") final String orderBy,
            @QueryParam("sortOrder") final String sortOrder,@QueryParam("groupName") final String groupName,
            @QueryParam("status") final String status,@QueryParam("category") final Long category) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final SearchParameters searchParameters = SearchParameters.forClients(sqlSearch, officeId, externalId, displayName, firstname,
                lastname, hierarchy, offset, limit, orderBy, sortOrder,groupName,status,category);
        final Page<ClientData> clientData = this.clientReadPlatformService.retrieveAll(searchParameters);
        return this.toApiJsonSerializer.serialize(clientData);
    }

	/**
     * this method is using for getting template data in editing a client
     */
	@GET
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
         Configuration configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_BALANCE_CHECK);
        ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);
        String balanceCheck="N";
        if(configurationProperty.isEnabled()){
        	balanceCheck="Y";
        }
         
        if (settings.isTemplate()) {
        	
            final List<OfficeData> allowedOffices = new ArrayList<OfficeData>(officeReadPlatformService.retrieveAllOfficesForDropdown());
            final Collection<ClientCategoryData> categoryDatas=this.clientReadPlatformService.retrieveClientCategories();
            final Collection<GroupData> groupDatas = this.clientReadPlatformService.retrieveGroupData();
            final List<String> allocationDetailsDatas=this.allocationReadPlatformService.retrieveHardWareDetails(clientId);
            clientData = ClientData.templateOnTop(clientData, allowedOffices,categoryDatas,groupDatas,allocationDetailsDatas,null);
        }else{
        	final List<String> allocationDetailsDatas=this.allocationReadPlatformService.retrieveHardWareDetails(clientId);
             clientData = ClientData.templateOnTop(clientData, null,null,null,allocationDetailsDatas,balanceCheck);
        }
        clientData.setDate(DateUtils.getLocalDateOfTenantForClient());
        final SelfCare selfcare = this.selfCareRepository.findOneByClientId(clientId);
        clientData.setSelfcare(selfcare);
        final PaymentGatewayConfiguration paypalconfigurationProperty=this.paymentGatewayConfigurationRepositoryWrapper.findOneByName(ConfigurationConstants.PAYMENTGATEWAY_IS_PAYPAL_CHECK);
        clientData.setConfigurationProperty(paypalconfigurationProperty);
        final PaymentGatewayConfiguration paypalconfigurationPropertyForIos=this.paymentGatewayConfigurationRepositoryWrapper.findOneByName(ConfigurationConstants.PAYMENTGATEWAY_IS_PAYPAL_CHECK_IOS);
        clientData.setConfigurationPropertyForIos(paypalconfigurationPropertyForIos);
        
        return this.toApiJsonSerializer.serialize(settings, clientData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }
    
    /**
     * this method is using for getting template data in editing a client
     */
	@GET
    @Path("additionalinfo/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClientAdditionalInfo(@PathParam("id") final Long id, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        ClientAdditionalData clientAdditionalData = this.clientReadPlatformService.retrieveClientAdditionalData(id);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
         
        if (settings.isTemplate()) {
        	
            	Collection<MCodeData> genderDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_GENDER);
            	Collection<MCodeData> nationalityDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_NATIONALITY);
            	Collection<MCodeData> customeridentificationDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_CUSTOMER_IDENTIFIER);
            	Collection<MCodeData> cummunitcationDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PREFER_COMMUNICATION);
            	Collection<MCodeData> languagesDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PREFER_LANG);
            	Collection<MCodeData> ageGroupDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_AGE_GROUP);
            	if(clientAdditionalData != null){
            	clientAdditionalData.setAgeGroupDatas(ageGroupDatas);
            	 clientAdditionalData.setGenderDatas(genderDatas);
            	 clientAdditionalData.setNationalityDatas(nationalityDatas);
            	 clientAdditionalData.setCustomeridentificationDatas(customeridentificationDatas);
            	 clientAdditionalData.setCummunitcationDatas(cummunitcationDatas);
            	 clientAdditionalData.setLanguagesDatas(languagesDatas);
            	 clientAdditionalData.setDate(DateUtils.getLocalDateOfTenantForClient());
            	}else{
            	  clientAdditionalData = new ClientAdditionalData(genderDatas,nationalityDatas,customeridentificationDatas,cummunitcationDatas,
            			languagesDatas,ageGroupDatas);
            	  clientAdditionalData.setDate(DateUtils.getLocalDateOfTenantForClient());
            	}
      	  }
        return this.jsonSerializer.serialize(settings, clientAdditionalData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }
    
    /**
     * this method is using for create a new client
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createClient().withJson(apiRequestBodyAsJson).build(); 
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
    
    @POST
    @Path("additionalinfo/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createClientAdditionalInfo(@PathParam("clientId") final Long clientId,final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientAdditional(clientId).withJson(apiRequestBodyAsJson).build(); 
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
    
    @PUT
    @Path("additionalinfo/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClientAdditionalInfo(@PathParam("clientId") final Long clientId,final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClientAdditional(clientId).withJson(apiRequestBodyAsJson).build(); 
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * this method is using for edit a client
     */
    @PUT
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClient(clientId).withJson(apiRequestBodyAsJson).build(); 
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("clientId") final Long clientId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClient(clientId).build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * this method is using for closing a client
     */
    @POST
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String activate(@PathParam("clientId") final Long clientId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.activateClient(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        
        }else  if (is(commandParam, "close")) {

        	 final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClient(clientId).withJson(apiRequestBodyAsJson).build();
             result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate" });
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
    
    @DELETE
	@Path("delete/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteCustomerWithNoRecord(@PathParam("clientId") final Long clientId) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCustomerNoRecord(clientId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
	}
    
    @GET
    @Path("params")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClientCatagory() {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final Collection<ClientCategoryData> clientCategoryData = this.clientReadPlatformService.retrieveClientCategories();
        final Collection<OfficeData> officeData = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        ClientData clientData = ClientData.template(officeData, clientCategoryData);
        return this.toApiJsonSerializer.serialize(clientData);
    }
    
    @GET
    @Path("convertedClient/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveConcvertedClients(@PathParam("clientId") final Long clientId) {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final Collection<ClientData> clientData = this.clientReadPlatformService.retrieveConcvertedClients(clientId);
        return this.toApiJsonSerializer.serialize(clientData);
    }
    
    @PUT
    @Path("requiredfields/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClientRequiredFields(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClientRequiredFields(clientId).withJson(apiRequestBodyAsJson).build(); 
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
    
}