package org.obsplatform.organisation.ticketassignrule.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.ticketassignrule.data.TicketAssignRuleData;
import org.obsplatform.organisation.ticketassignrule.exception.TicketAssignRuleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class TicketAssignRuleReadPlatformServiceImpl implements TicketAssignRuleReadPlatformService{
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	
	@Autowired
	public TicketAssignRuleReadPlatformServiceImpl(final PlatformSecurityContext context,final RoutingDataSource dataSource){
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public TicketAssignRuleData retrieveCategoryDepartment(Long businessprocessId,Long clientCategoryId) {
		try {
			context.authenticatedUser();
			final TicketAssignRuleMapper mapper = new TicketAssignRuleMapper();
			final String sql = "select " + mapper.schema()+ " and btr.businessprocess_id=? and btr.clientcategory_id=?";

			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { businessprocessId, clientCategoryId });
		} catch (EmptyResultDataAccessException accessException) {
			//throw new TicketAssignRuleNotFoundException(businessprocessId,clientCategoryId);
			return null;
		}
	}
	
	private static final class TicketAssignRuleMapper implements RowMapper<TicketAssignRuleData> {

		public String schema() {
			return " btr.id as id,btr.clientcategory_id as clientcategoryId,btr.department_id as departmentId,btr.businessprocess_id as businessProcessId,"
					+ "mcv.code_value as clientcategoryType,mc.code_value as businessprocessName,bd.department_name as departmentName"
					+ " from b_ticketassign_rule btr join m_code_value mcv ON mcv.id = btr.clientcategory_id join "
					+ "m_code_value mc ON mc.id = btr.businessprocess_id join b_office_department bd ON bd.id = btr.department_id "
					+ "where btr.is_deleted = 'N' ";
		}
		
		@Override
		public TicketAssignRuleData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long clientCategoryId = rs.getLong("clientcategoryId");
			final Long departmentId = rs.getLong("departmentId");
			final Long businessprocessId = rs.getLong("businessProcessId");
			final String businessprocessName = rs.getString("businessprocessName");
			final String clientcategoryType = rs.getString("clientcategoryType");
			final String departmentName = rs.getString("departmentName");

			return new TicketAssignRuleData(id, clientCategoryId, departmentId, businessprocessId,businessprocessName,clientcategoryType,departmentName);
		}
	}

	@Override
	public TicketAssignRuleData retrieveTicketAssignRuleData(final Long ticketassignruleId) {
		try {
			context.authenticatedUser();
			final TicketAssignRuleMapper mapper = new TicketAssignRuleMapper();
			final String sql = "select " + mapper.schema() + " and btr.id=?";

			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[] { ticketassignruleId });

		} catch (EmptyResultDataAccessException accessException) {
			throw new TicketAssignRuleNotFoundException(ticketassignruleId);
		}
	}
	
	@Override
	public List<TicketAssignRuleData> retrieveAllTicketAssignRuleData() {
		try {
			context.authenticatedUser();
			final TicketAssignRuleMapper mapper = new TicketAssignRuleMapper();
			final String sql = "select " + mapper.schema();

			return this.jdbcTemplate.query(sql, mapper, new Object[] {});

		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

}
