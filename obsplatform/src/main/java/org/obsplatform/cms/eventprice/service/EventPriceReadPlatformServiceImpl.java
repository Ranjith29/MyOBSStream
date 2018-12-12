/**
 * 
 */
package org.obsplatform.cms.eventprice.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obsplatform.cms.eventprice.data.ClientTypeData;
import org.obsplatform.cms.eventprice.data.EventPriceData;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * {@link Service} Class for {@link EventPricing} Read Service
 * implements {@link EventPriceWritePlatformService}
 * 
 * @author pavani
 *
 */
@Service
public class EventPriceReadPlatformServiceImpl implements 
		EventPriceReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public EventPriceReadPlatformServiceImpl(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<EventPriceData> retrieventPriceData(final Long eventId) {
		try {
			final EventPricingMapper eventPricingMapper = new EventPricingMapper();
			final String sql = "SELECT " + eventPricingMapper.eventPricingSchema() + " where event_id = ? and is_deleted = 'n'";
			return jdbcTemplate.query(sql, eventPricingMapper,new Object[] {eventId});
		} catch (EmptyResultDataAccessException e) {
		return null;
		}
	}
	
	@Override
	public EventPriceData retrieventPriceDetails(final Long eventPriceId) {
		try {
			final EventPricingMapper eventPricingMapper = new EventPricingMapper();
			final String sql = "SELECT " + eventPricingMapper.eventPricingSchema() + " where ep.id = ? and is_deleted = 'n'";
			return jdbcTemplate.queryForObject(sql, eventPricingMapper,new Object[] {eventPriceId});
		} catch (EmptyResultDataAccessException e) {
		return null;
		}
	}
	
	private static final class EventPricingMapper implements RowMapper<EventPriceData>  {
		
		public String eventPricingSchema() {
			return " ep.id AS id,em.id AS eventId,em.event_name AS eventName,ep.format_type AS formatType,ep.opt_type AS optType, ep.client_typeid as clientTypeId,"
				  +" d.discount_description AS discount,ep.price AS price,mc.code_value as clientType,ep.discount_id as discountId " +
				   " from b_mod_pricing ep " +
				   " inner join m_code_value mc on  ep.client_typeid=mc.id"
				  +"  inner join b_discount_master d ON d.id = ep.discount_id " +
				   " inner join b_mod_master em ON em.id = ep.event_id";
		}
		@Override
		public EventPriceData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
			
			final Long id = resultSet.getLong("id");
			final Long eventId = resultSet.getLong("eventId");
			final String eventName  = resultSet.getString("eventName");
			final String formatType = resultSet.getString("formatType");
			final String optType = resultSet.getString("optType");
			final Long clientTypeId = resultSet.getLong("clientTypeId");
			final Long discountId = resultSet.getLong("discountId");
			final String discount = resultSet.getString("discount");
			final BigDecimal price = resultSet.getBigDecimal("price");
			final String clientType = resultSet.getString("clientType");
			
			return new EventPriceData(id, eventName, formatType, optType, clientTypeId, discount, price,eventId,clientType,discountId);
		}
		
	}

	@Override
	public List<ClientTypeData> clientType() {
		try {
			final ClientTypeMapper clientTypeMapper = new ClientTypeMapper();
			final String sql = "SELECT " + clientTypeMapper.clientTypeSchema() + " where mc.code_name='Client Category'";
			return jdbcTemplate.query(sql, clientTypeMapper,new Object[] {});
		} catch (EmptyResultDataAccessException e) {
		return null;
		}
	}
	
private static final class ClientTypeMapper implements RowMapper<ClientTypeData>  {
		
		public String clientTypeSchema() {
			return " mcv.id as id , mcv.code_value as type from m_code_value mcv "
					+ " inner join m_code mc on mc.id = mcv.code_id " ;
		}
		@Override
		public ClientTypeData mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
			
			final Long id = resultSet.getLong("id");
			final String type= resultSet.getString("type");
			return new ClientTypeData(id,type);
		}
		
	}
	

}
