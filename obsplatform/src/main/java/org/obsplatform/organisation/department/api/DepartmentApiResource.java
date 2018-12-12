package org.obsplatform.organisation.department.api;

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

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.department.data.DepartmentData;
import org.obsplatform.organisation.department.service.DepartmentReadPlatformService;
import org.obsplatform.organisation.office.data.OfficeData;
import org.obsplatform.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/departments")
@Component
public class DepartmentApiResource {
	
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id","departmentname","description","officeId"));
	
	private final String resourceNameForPermissions = "DEPARTMENT";
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<DepartmentData> toApiJsonSerializer;
	private final PlatformSecurityContext context;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DepartmentReadPlatformService departmentReadPlatformService;
	private final OfficeReadPlatformService officeReadPlatformService;
	
	@Autowired
	public DepartmentApiResource(final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final DefaultToApiJsonSerializer<DepartmentData> toApiJsonSerializer,
			final PlatformSecurityContext context,final ApiRequestParameterHelper apiRequestParameterHelper,
			final DepartmentReadPlatformService departmentReadPlatformService,
			final OfficeReadPlatformService officeReadPlatformService) {
		
		        this.toApiJsonSerializer = toApiJsonSerializer;
		        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		        this.context = context;
		        this.apiRequestParameterHelper = apiRequestParameterHelper;
		        this.departmentReadPlatformService = departmentReadPlatformService;
		        this.officeReadPlatformService = officeReadPlatformService;
		    }
	
	/**
	 * this method is using for create Department
	 * Storing these details in b_office_department.
	 *  
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String createDepartments(final String jsonRequestBody) {
		    final CommandWrapper commandRequest = new CommandWrapperBuilder().createDepartment().withJson(jsonRequestBody).build();
	        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	        return this.toApiJsonSerializer.serialize(result);
	}
	/**
	 * this method is using for get the Department By Id
	 */
	@GET
	@Path("{deptid}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveDepartmentData(@PathParam("deptid") final Long deptId,@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final DepartmentData departmentDatas = this.departmentReadPlatformService.retrieveDepartmentData(deptId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if (settings.isTemplate()) {
		final DepartmentData departmentOfficeData = handleTemplateData();
		departmentDatas.setOfficeData(departmentOfficeData.getOfficeData());
		}
		return this.toApiJsonSerializer.serialize(settings, departmentDatas,RESPONSE_DATA_PARAMETERS);
	}
	/**
	 * this method is using for get all Departments
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDepartmentData(@Context final UriInfo uriInfo) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
	    final List<DepartmentData> departmentdatas = this.departmentReadPlatformService.retrieveAllDepartmentData();
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, departmentdatas,RESPONSE_DATA_PARAMETERS);
	}
	@GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String departmentTemplateDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        DepartmentData department = handleTemplateData();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, department, RESPONSE_DATA_PARAMETERS);
    }
    private DepartmentData handleTemplateData() {
        final Collection<OfficeData> officedata=this.officeReadPlatformService.retrieveOfficeTypeData();
		return new DepartmentData(officedata);
	}
	/**
	 * this method is using for edit and update Department
	 * Storing these details in b_office_department.
	 */
	@PUT
	@Path("{deptid}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String updateDepartments(@PathParam("deptid") final Long deptId, final String apiRequestBodyAsJson){
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDepartment(deptId).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		 return this.toApiJsonSerializer.serialize(result);

	}
	/**
	 * this method is using for delete Department
	 * Storing these details in b_office_department.
	 */
	@DELETE
	@Path("{deptid}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteDepartment(@PathParam("deptid") final Long deptId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDepartment(deptId).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
}