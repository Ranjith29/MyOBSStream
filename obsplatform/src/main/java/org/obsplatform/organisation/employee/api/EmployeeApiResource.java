package org.obsplatform.organisation.employee.api;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.obsplatform.commands.domain.CommandWrapper;
import org.obsplatform.commands.service.CommandWrapperBuilder;
import org.obsplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.obsplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.department.data.DepartmentData;
import org.obsplatform.organisation.department.service.DepartmentReadPlatformService;
import org.obsplatform.organisation.employee.data.EmployeeData;
import org.obsplatform.organisation.employee.service.EmployeeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/employee")
@Component
@Scope("singleton")
public class EmployeeApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "loginname","password", "phone","email","departmentId","isprimary"));
	
	private final static String resourceNameForPermissions = "EMPLOYEE";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<EmployeeData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final EmployeeReadPlatformService employeeReadPlatformService;
	private final DepartmentReadPlatformService departmentReadPlatformService;
	
	
	@Autowired
	public EmployeeApiResource(	PlatformSecurityContext context,
			DefaultToApiJsonSerializer<EmployeeData> toApiJsonSerializer,
			ApiRequestParameterHelper apiRequestParameterHelper,
			PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			EmployeeReadPlatformService employeeReadPlatformService,
			DepartmentReadPlatformService departmentReadPlatformService) {
		
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.employeeReadPlatformService = employeeReadPlatformService;
		this.departmentReadPlatformService = departmentReadPlatformService;
	}
	
	
	/**
	 * this method is using for get all Employee
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllEmployeeData(@Context final UriInfo uriInfo, 
			@QueryParam("sqlSearch") final String sqlSearch, 
			@QueryParam("limit") final Integer limit, 
			@QueryParam("offset") final Integer offset) {
		
		/*this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
	    final List<EmployeeData> employeedatas = this.employeeReadPlatformService.retrieveAllEmployeeData();
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, employeedatas,RESPONSE_DATA_PARAMETERS);*/
        this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final SearchSqlQuery searchEmployee =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		Page<EmployeeData> employeedatas=this.employeeReadPlatformService.retrieveAllEmployeeData(searchEmployee);
		return this.toApiJsonSerializer.serialize(employeedatas);
	}
	
	/**
	 * this method is using for get the Employee By Id
	 */
	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveEmployeeData(@PathParam("id") final Long id,@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		EmployeeData employeeDatas = this.employeeReadPlatformService.retrieveEmployeeData(id);
		EmployeeData employeeDatas12 = handleTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if (settings.isTemplate()) {
			final EmployeeData employeeDepartmentData = handleTemplateData();
			employeeDatas.setDepartmentdata(employeeDepartmentData.getDepartmentdata());
			}
		return this.toApiJsonSerializer.serialize(settings, employeeDatas,RESPONSE_DATA_PARAMETERS);
	}
	
	
	@GET
	@Path("deprt/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveAllEmployeeByDepartment(@PathParam("id") final Long id,@Context final UriInfo uriInfo){
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<EmployeeData> employeedatas = this.employeeReadPlatformService.retrieveAllEmployeeDataByDeptId(id);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, employeedatas,RESPONSE_DATA_PARAMETERS);
	}
	
	/**
	 * this method is using for get departentdata in dropdown
	 */
	@GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String employeeTemplateDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        EmployeeData employee = handleTemplateData();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, employee, RESPONSE_DATA_PARAMETERS);
    }
    
    private EmployeeData handleTemplateData() {
		
        final Collection<DepartmentData> departmentdata=this.departmentReadPlatformService.retrieveAllDepartmentData();//
		 
		return new EmployeeData(departmentdata);
			
	}
    
    /**
	 * this method is using for create Employee
	 * Storing these details in b_office_employee.
	 *  
	 */
    @POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewEmployee(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createEmployee().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
    
    /**
	 * this method is using for edit/update a single employee record
	 * Storing these details in b_office_employee.
	 */
    @PUT
	@Path("{employeeId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateEmployeeData(@PathParam("employeeId") final Long employeeId,final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateEmployee(employeeId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

    /**
	 * this method is using for delete employee (means update is_deleted as true)
	 * Storing these details in b_office_employee.
	 */
    @DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteEmployee(@PathParam("id") final Long id) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteEmployee(id).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
}
