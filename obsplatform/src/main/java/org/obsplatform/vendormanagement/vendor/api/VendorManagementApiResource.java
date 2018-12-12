/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.vendormanagement.vendor.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.cms.eventmaster.domain.EventMaster;
import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.address.data.CountryDetails;
import org.obsplatform.organisation.address.service.AddressReadPlatformService;
import org.obsplatform.organisation.monetary.data.CurrencyData;
import org.obsplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.obsplatform.useradministration.data.AppUserData;
import org.obsplatform.vendoragreement.exception.VendorNotFoundException;
import org.obsplatform.vendormanagement.vendor.data.VendorManagementData;
import org.obsplatform.vendormanagement.vendor.service.VendorManagementReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Class to Create, Update and Delete {@link VendorManagement}
 * @author Rakesh
 *
 */
@Path("/vendormanagement")
@Component
@Scope("singleton")
public class VendorManagementApiResource {

    /**
     * The set of parameters that are supported in response for
     * {@link AppUserData}.
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id"));

    private static final String RESOURCENAMEFORPERMISSIONS = "VENDORMANAGEMENT";
    private final PlatformSecurityContext context;
    private final VendorManagementReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<VendorManagementData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final AddressReadPlatformService addressReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;

    @Autowired
    public VendorManagementApiResource(final PlatformSecurityContext context, final VendorManagementReadPlatformService readPlatformService,
    		final DefaultToApiJsonSerializer<VendorManagementData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final AddressReadPlatformService addressReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.addressReadPlatformService = addressReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
    }
    
    
    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String vendorManagementTemplateDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
        VendorManagementData vendor = handleTemplateData();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, vendor, RESPONSE_DATA_PARAMETERS);
    }
    
    private VendorManagementData handleTemplateData() {
		
        final List<CountryDetails> countryData = this.addressReadPlatformService.retrieveCountries();
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllPlatformCurrencies();
		 
		return new VendorManagementData(countryData, currencyOptions);
			
	}
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createVendor(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createVendorManagement() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveVendors(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);

        final List<VendorManagementData> vendor = this.readPlatformService.retrieveAllVendorManagements();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, vendor, RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
    @Path("{vendorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveSingleVendor(@PathParam("vendorId") final Long vendorId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        VendorManagementData vendor = this.readPlatformService.retrieveSigleVendorManagement(vendorId);
        if(vendor == null){
        	throw new VendorNotFoundException(vendorId.toString());
        }
        
        if (settings.isTemplate()) {
        	final List<CountryDetails> countryData = this.addressReadPlatformService.retrieveCountries();
            final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllPlatformCurrencies();
            vendor.setCountryData(countryData);
            vendor.setCurrencyOptions(currencyOptions);
        }

        return this.toApiJsonSerializer.serialize(settings, vendor, RESPONSE_DATA_PARAMETERS);
    }
    
    @PUT
    @Path("{vendorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateVendor(@PathParam("vendorId") final Long vendorId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateVendorManagement(vendorId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
    @DELETE
    @Path("{vendorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteVendor(@PathParam("vendorId") final Long vendorId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteVendorManagement(vendorId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
