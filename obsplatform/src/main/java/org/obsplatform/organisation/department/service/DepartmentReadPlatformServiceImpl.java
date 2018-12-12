package org.obsplatform.organisation.department.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.department.data.DepartmentData;
import org.obsplatform.organisation.department.exception.DepartmentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DepartmentReadPlatformServiceImpl implements DepartmentReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public DepartmentReadPlatformServiceImpl(final PlatformSecurityContext context,final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public DepartmentData retrieveDepartmentData(final Long deptId) {

		try {
			context.authenticatedUser();
			final DepartmentMapper mapper = new DepartmentMapper();
			final String sql = "select " + mapper.schema() + " and d.id=?";

			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[] { deptId });

		} catch (EmptyResultDataAccessException accessException) {
			throw new DepartmentNotFoundException(deptId);
		}
	}

	private static final class DepartmentMapper implements RowMapper<DepartmentData> {

		public String schema() {
			return " d.id as id,d.department_name as deptname,d.department_description as description,d.is_allocated as allocated,d.office_id as officeId,mo.name as officename"
					+ " from m_office mo join b_office_department d on mo.id = d.office_id where d.is_deleted ='N' ";
		}

		@Override
		public DepartmentData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String deptname = rs.getString("deptname");
			final String description = rs.getString("description");
			final Long officeId = rs.getLong("officeId");
			final String officeName = rs.getString("officeName");
			final String allocatedAsPrimary = rs.getString("allocated");

			return new DepartmentData(id, deptname, description, officeId, officeName, allocatedAsPrimary);
		}
	}

	@Override
	public List<DepartmentData> retrieveAllDepartmentData() {
		try {
			context.authenticatedUser();
			final DepartmentMapper mapper = new DepartmentMapper();
			final String sql = "select " + mapper.schema();

			return this.jdbcTemplate.query(sql, mapper, new Object[] {});

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	@Override
	public DepartmentData retrieveDepartmentId(final String deptName) {


		try {

			final DepartmentIdMapper mapper = new DepartmentIdMapper();
			final String sql = "select " + mapper.schema()+ " and d.department_name=?";
			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[] { deptName });

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	
	}
	private static final class DepartmentIdMapper implements RowMapper<DepartmentData> {

		public String schema() {
			return " d.id as id,d.department_name as deptname,d.department_description as description,d.is_allocated as allocated,d.office_id as officeId,mo.name as officename"
					+ " from m_office mo join b_office_department d on mo.id = d.office_id where d.is_deleted ='N' ";
		}

		@Override
		public DepartmentData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String deptname = rs.getString("deptname");
			final String description = rs.getString("description");
			final Long officeId = rs.getLong("officeId");
			final String officeName = rs.getString("officeName");
			final String allocatedAsPrimary = rs.getString("allocated");
			
			return new DepartmentData(id, deptname, description, officeId, officeName, allocatedAsPrimary);
		}
	}
}
