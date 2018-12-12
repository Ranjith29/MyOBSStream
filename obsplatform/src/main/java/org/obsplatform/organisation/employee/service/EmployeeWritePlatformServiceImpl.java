package org.obsplatform.organisation.employee.service;

import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.department.data.DepartmentData;
import org.obsplatform.organisation.department.domain.Department;
import org.obsplatform.organisation.department.domain.DepartmentRepository;
import org.obsplatform.organisation.department.service.DepartmentReadPlatformService;
import org.obsplatform.organisation.department.service.DepartmentWritePlatformService;
import org.obsplatform.organisation.employee.domain.Employee;
import org.obsplatform.organisation.employee.domain.EmployeeRepository;
import org.obsplatform.organisation.employee.exception.EmployeeNotFoundException;
import org.obsplatform.organisation.employee.serialization.EmployeeCommandFromApiJsonDeserializer;
import org.obsplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.obsplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.obsplatform.provisioning.provsionactions.domain.ProvisionActions;
import org.obsplatform.provisioning.provsionactions.domain.ProvisioningActionsRepository;
import org.obsplatform.useradministration.api.UsersApiResource;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.useradministration.domain.AppUserRepository;
import org.obsplatform.useradministration.domain.Role;
import org.obsplatform.useradministration.domain.RoleRepository;
import org.obsplatform.useradministration.exception.RoleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class EmployeeWritePlatformServiceImpl implements
		EmployeeWritePlatformService {

	private final static Logger LOGGER = (Logger) LoggerFactory
			.getLogger(EmployeeWritePlatformServiceImpl.class);

	private final PlatformSecurityContext context;
	private final EmployeeRepository employeeRepository;
	private final EmployeeCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final FromJsonHelper fromApiJsonHelper;
	private final DepartmentRepository deptRepository;
	private final DepartmentReadPlatformService departmentReadPlatformService; 
	private final RoleRepository roleRepository;
    private final UsersApiResource userApiResource;
    private final AppUserRepository userRepository;
    private final ProvisioningActionsRepository provisioningActionsRepository;
    private final ProvisioningWritePlatformService provisioningWritePlatformService;
    private final DepartmentWritePlatformService departmentWritePlatformService;
	@Autowired
	public EmployeeWritePlatformServiceImpl(PlatformSecurityContext context,
			EmployeeRepository employeeRepository,
			EmployeeCommandFromApiJsonDeserializer apiJsonDeserializer,
			FromJsonHelper fromApiJsonHelper,DepartmentRepository deptRepository,
			final DepartmentReadPlatformService departmentReadPlatformService,
			final RoleRepository roleRepository,final UsersApiResource userApiResource,
			final AppUserRepository userRepository,
			final ProvisioningActionsRepository provisioningActionsRepository,
			final ProvisioningWritePlatformService provisioningWritePlatformService,
			final DepartmentWritePlatformService departmentWritePlatformService) {
		super();
		this.context = context;
		this.employeeRepository = employeeRepository;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.deptRepository = deptRepository;
		this.departmentReadPlatformService = departmentReadPlatformService;
		this.roleRepository = roleRepository;
		this.userApiResource = userApiResource;
		this.userRepository = userRepository;
		this.provisioningActionsRepository = provisioningActionsRepository;
		this.provisioningWritePlatformService = provisioningWritePlatformService;
		this.departmentWritePlatformService = departmentWritePlatformService;
		
	}

	@Override
	public CommandProcessingResult createEmployee(JsonCommand command) {
		Employee employee = null;
		try {
			context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			final JsonElement element = fromApiJsonHelper.parse(command.json());
			final Long departmentId = this.fromApiJsonHelper.extractLongNamed("departmentId", element);
			employee = Employee.fromJson(command);
			if(employee.isIsprimary()) {
				Department dept = this.deptRepository.findOne(departmentId);
				dept.allocated();
				this.deptRepository.saveAndFlush(dept);
			}
			/*
			 * code for create user when create employee 
			 */
			 //this departmentReadPlatformService used to get office id and office name of employee
			DepartmentData departmentdata = this.departmentReadPlatformService.retrieveDepartmentData(departmentId);
			
			final Long officeId = departmentdata.getOfficeId();
			final String officeName = departmentdata.getOfficeName();
			final String Name = command.stringValueOfParameterNamed("name");
			final String loginName = command.stringValueOfParameterNamed("loginname");
		    final String password = command.stringValueOfParameterNamed("password");
		    final String repeatPassword = command.stringValueOfParameterNamed("repeatpassword");
		    final String email = command.stringValueOfParameterNamed("email");
		    final String roleName = command.stringValueOfParameterNamed("roleName");
		    final String[]  roles= arrayOfRole(roleName);
		    JSONObject json = new JSONObject();
		    json.put("username", loginName);
		    json.put("password", password);
		    json.put("repeatPassword", repeatPassword);
		    json.put("firstname",Name);
		    json.put("lastname", Name);
		    json.put("sendPasswordToEmail",Boolean.FALSE);
		    json.put("email",email);
		    json.put("officeId", officeId);
		    json.put("roles", new JSONArray(roles));
	        final String result=this.userApiResource.createUser(json.toString());
	        JSONObject resultJson = new JSONObject(result);
	        
	        final Long userId = resultJson.getLong("resourceId");
	        
	        employee.setUserId(userId);
	        this.employeeRepository.save(employee);	        
           
	        ProvisionActions provisionActions=this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_CREATE_AGENT);
			
            if(provisionActions != null && provisionActions.isEnable() == 'Y'){
				
				this.provisioningWritePlatformService.postDetailsForProvisioning(Long.valueOf(0),officeId,ProvisioningApiConstants.REQUEST_CREATE_AGENT,
						               provisionActions.getProvisioningSystem(),null);
			}
			
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					  .withEntityId(employee.getId()).build();
		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1L));
		}catch (JSONException e) {
			e.printStackTrace();
			return new CommandProcessingResult(Long.valueOf(-1l));
		}
	}

	private String[] arrayOfRole(final String name) {
		
		  final Role role = this.roleRepository.findOneByName(name);
      if (role == null) { throw new RoleNotFoundException(Long.valueOf(name)); }
      String[] roles={role.getId().toString()};
      return roles;
	
	}
	@Override
	public CommandProcessingResult updateEmployee(JsonCommand command,Long employeeId) {
		Employee employee=null;
		try {
			context.authenticatedUser();
			this.apiJsonDeserializer.validateForUpdate(command.json());
			employee = retrieveEmployeeById(employeeId);
			final Map<String, Object> changes = employee.update(command);
			
			//update user
			final Long userId = employee.getUserId();
			final String name = command.stringValueOfParameterNamed("name");
			final String loginName = command.stringValueOfParameterNamed("loginname");
			final String email = command.stringValueOfParameterNamed("email");
			final String password = command.stringValueOfParameterNamed("password");
			
			JsonObject updateUserJson = new JsonObject();
			updateUserJson.addProperty("password", password);
			updateUserJson.addProperty("repeatPassword", password);
			updateUserJson.addProperty("username", loginName);
			updateUserJson.addProperty("email", email);
			updateUserJson.addProperty("firstname", name);
			updateUserJson.addProperty("lastname", name);
			final String result = this.userApiResource.updateUser(userId, updateUserJson.toString());
			if(changes.containsKey("isprimary")){
				if(employee.isIsprimary()) {
					Department dept = this.deptRepository.findOne(employee.getDepartmentId());
					dept.allocated();
					this.deptRepository.saveAndFlush(dept);
				}
				else{
					Department dept = this.deptRepository.findOne(employee.getDepartmentId());
					dept.setAllocated("No");
					this.deptRepository.saveAndFlush(dept);
				}
			}
				
			if (!changes.isEmpty()) {
				employeeRepository.saveAndFlush(employee);
			}
			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId())
					.withEntityId(employee.getId()).with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			if (dve.getCause() instanceof ConstraintViolationException) {
				handleCodeDataIntegrityIssues(command, dve);
			}
			return new CommandProcessingResult(Long.valueOf(-1L));
		}
	}
	private Employee retrieveEmployeeById(final Long employeeId) {
		
		final Employee employee = this.employeeRepository.findOne(employeeId);
		if (employee == null) {
			throw new EmployeeNotFoundException(employeeId.toString());
		}
		return employee;
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		
		final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("email")) {
            final String email = command.stringValueOfParameterNamed("email");
            throw new PlatformDataIntegrityException("error.msg.employee.email.duplicate.", "A Employee with email '" + email + "' already exists", "email");
        }
	        LOGGER.error(dve.getMessage(), dve);
	        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource: " + realCause.getMessage());
	}
	
	@Override
	public CommandProcessingResult deleteEmployee(JsonCommand command,
			Long employeeId) {
		try {

			this.context.authenticatedUser();
			Employee employee = retrieveEmployeeById(employeeId);
			final Long userId = employee.getUserId();
			final AppUser user = this.userRepository.findOne(userId);
			user.delete();
			if(employee.isIsprimary()){
				final Long deptId = employee.getDepartmentId();
				final Department department = this.deptRepository.findOne(deptId);
				department.setAllocated("No");
			}
			employee.delete();
			return new CommandProcessingResult(employeeId);
		} catch (final DataIntegrityViolationException dve) {
			
			throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
					"Unknown data integrity issue with resource: "+ dve.getMessage());
		}
	}

}
