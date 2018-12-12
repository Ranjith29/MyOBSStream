package org.obsplatform.finance.adjustment.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.finance.adjustment.data.AdjustmentCodeData;
import org.obsplatform.finance.adjustment.data.AdjustmentData;
import org.obsplatform.finance.adjustment.service.AdjustmentReadPlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/adjustments")
@Component
@Scope("singleton")
public class AdjustmentApiResource {
	
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("discountCode", "discountOptions"));
	private final static String RESOURCENAMEFORPERMISSIONS = "ADJUSTMENT";
	private final PlatformSecurityContext context;
    private final AdjustmentReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<AdjustmentCodeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    

    @Autowired
    public AdjustmentApiResource(final PlatformSecurityContext context, final AdjustmentReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<AdjustmentCodeData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }
    
    /**
     * this method is used for creating adjustment
     * @param clientId
     * @param apiRequestBodyAsJson
     * @return
     */
    @POST
    @Path("{clientId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String addNewAdjustment(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().createAdjustment(clientId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
    
    /**
     * this method is used for getting template data
     * @param uriInfo
     * @return
     */
    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveTempleteInfo(@Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
    	
        final List<AdjustmentData> data=this.readPlatformService.retrieveAllAdjustmentsCodes();
        final AdjustmentCodeData datas=new AdjustmentCodeData(data);
        datas.setDate(DateUtils.getLocalDateOfTenantForClient());
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, datas, RESPONSE_DATA_PARAMETERS);
    }
    

}
