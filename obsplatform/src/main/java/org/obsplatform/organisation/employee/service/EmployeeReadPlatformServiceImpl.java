package org.obsplatform.organisation.employee.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.chargevariant.data.ChargeVariantData;
import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.core.service.PaginationHelper;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.employee.data.EmployeeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class EmployeeReadPlatformServiceImpl implements
		EmployeeReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<EmployeeData> paginationHelper =new PaginationHelper<EmployeeData>();
	
	@Autowired
	public EmployeeReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
	
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	
	@Override
	public List<EmployeeData> retrieveAllEmployeeData() {
		try {
			final EmployeeMapper mapper = new EmployeeMapper();
			final String sql = "select " + mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}
	@Override
	public Page<EmployeeData> retrieveAllEmployeeData(SearchSqlQuery searchEmployee) {
		
		//SalesDataMapper mapper = new SalesDataMapper();
		final EmployeeMapper mapper = new EmployeeMapper();
		
		final StringBuilder sqlBuilder = new StringBuilder(200);
	    sqlBuilder.append("select SQL_CALC_FOUND_ROWS");
	    sqlBuilder.append(mapper.schema());
	   	    
	    String sqlSearch = searchEmployee.getSqlSearch();
	    String extraCriteria = "";
	    if (sqlSearch != null) {
	    	sqlSearch=sqlSearch.trim();
	    	extraCriteria = " and (e.employee_name like '%"+sqlSearch+"%' OR" 
	    			+ " e.employee_loginname like '%"+sqlSearch+"%' )";
	    			
	    }
	        sqlBuilder.append(extraCriteria);
	    
	    if (searchEmployee.isLimited()) {
	        sqlBuilder.append(" limit ").append(searchEmployee.getLimit());
	    }

	    if (searchEmployee.isOffset()) {
	        sqlBuilder.append(" offset ").append(searchEmployee.getOffset());
	    }
	    final String sqlCountRows = "SELECT FOUND_ROWS()";
		return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows,sqlBuilder.toString(),
	            new Object[] {}, mapper);
	}
	
	
	private static final class EmployeeMapper implements RowMapper<EmployeeData> {

		public String schema() {
			return " e.id as id,e.employee_name as name,e.employee_loginname as loginname,e.employee_password as password,e.employee_phone as phone,e.employee_email as email,e.employee_isprimary as isprimary,e.department_id as departmentId,d.department_name as departmentname,d.is_allocated as allocated,e.user_id as userId"
					+ " from b_office_department d join b_office_employee e on d.id=e.department_id where e.is_deleted ='N'";

		}
		@Override
		public EmployeeData mapRow(final ResultSet resultSet,final int rowNum) throws SQLException {

			final Long id = resultSet.getLong("id");
			final String name = resultSet.getString("name");
			final String loginname = resultSet.getString("loginname");
			final String password = resultSet.getString("password");
			final Long phone = resultSet.getLong("phone");
			final String email = resultSet.getString("email");
			final Boolean isprimary = resultSet.getBoolean("isprimary");
			final Long departmentId = resultSet.getLong("departmentId");
			final String departmentName = resultSet.getString("departmentname");
			final String allocated = resultSet.getString("allocated");
			final Long userId = resultSet.getLong("userId");
			
			
			return new EmployeeData(id,name,loginname,password,phone,email,isprimary,departmentId,departmentName,allocated,userId);

		}
	}

	
	@Override
	public EmployeeData retrieveEmployeeData(Long employeeId) {
		try {
			final EmployeeMapper mapper = new EmployeeMapper();
			final String sql = "select " + mapper.schema()+" and e.id=?";
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {employeeId});
			
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	@Override
	public List<EmployeeData> retrieveAllEmployeeDataByDeptId(Long departmentId) {
		try {
			final EmployeeMapper mapper = new EmployeeMapper();
			final String sql = "select " + mapper.schema()+" and d.id=?";
			return this.jdbcTemplate.query(sql, mapper, new Object[] {departmentId});
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}


}
