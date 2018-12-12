/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.office.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.financialtransaction.data.FinancialTransactionsData;
import org.obsplatform.infrastructure.codes.data.CodeValueData;
import org.obsplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.address.service.AddressReadPlatformService;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/offices")
@Component
@Scope("singleton")
public class OfficesApiResource {

    /**
     * The set of parameters that are supported in response for
     * {@link OfficeData}.
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "nameDecorated", "externalId",
            "openingDate", "hierarchy", "parentId", "parentName", "allowedParents","officeTypes"));

    private final String resourceNameForPermissions = "OFFICE";
    
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<OfficeData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<FinancialTransactionsData> toFinancialTransactionApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final AddressReadPlatformService addressReadPlatformService;
    public static final String OFFICE_TYPE="Office Type";

    @Autowired
    public OfficesApiResource(final PlatformSecurityContext context, final OfficeReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<OfficeData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final DefaultToApiJsonSerializer<FinancialTransactionsData> toFinancialTransactionApiJsonSerializer,
            final AddressReadPlatformService addressReadPlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.toFinancialTransactionApiJsonSerializer = toFinancialTransactionApiJsonSerializer;
        this.addressReadPlatformService = addressReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOffices(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        final Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, offices, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOfficeTemplate(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        OfficeData office = this.readPlatformService.retrieveNewOfficeTemplate();
        final Collection<OfficeData> allowedParents = this.readPlatformService.retrieveAllOfficesForDropdown();
        final Collection<CodeValueData> officeTypes=this.codeValueReadPlatformService.retrieveCodeValuesByCode(OFFICE_TYPE);
        office = OfficeData.appendedTemplate(office, allowedParents,officeTypes, DateUtils.getLocalDateOfTenantForClient());
        office.setCitiesData(this.addressReadPlatformService.retrieveCityDetails());

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, office, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createOffice(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createOffice() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{officeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveOffice(@PathParam("officeId") final Long officeId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        OfficeData office = this.readPlatformService.retrieveOffice(officeId);
        
        if (settings.isTemplate()) {
            final Collection<OfficeData> allowedParents = this.readPlatformService.retrieveAllowedParents(officeId);
            final Collection<CodeValueData> codeValueDatas=this.codeValueReadPlatformService.retrieveCodeValuesByCode(OFFICE_TYPE);
            
            office = OfficeData.appendedTemplate(office, allowedParents,codeValueDatas, DateUtils.getLocalDateOfTenantForClient());
            office.setCountryData(this.addressReadPlatformService.retrieveCountryDetails());
        	office.setCitiesData(this.addressReadPlatformService.retrieveCityDetails());
        	office.setStatesData(this.addressReadPlatformService.retrieveStateDetails());
        }

        return this.toApiJsonSerializer.serialize(settings, office, RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{officeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateOffice(@PathParam("officeId") final Long officeId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateOffice(officeId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
    /**
     * Office financial Transactions
     * */
    @GET
    @Path("financialtransactions/{officeId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveTransactionalData(@PathParam("officeId") final Long officeId, @Context final UriInfo uriInfo)	{
    	context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
    	final Collection<FinancialTransactionsData> transactionData = this.readPlatformService.retreiveOfficeFinancialTransactionsData(officeId);
    	final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toFinancialTransactionApiJsonSerializer.serialize(settings, transactionData, RESPONSE_DATA_PARAMETERS);
    }
}