/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.documentmanagement.service;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.obsplatform.infrastructure.core.domain.JdbcSupport;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.documentmanagement.data.DocumentData;
import org.obsplatform.infrastructure.documentmanagement.data.FileData;
import org.obsplatform.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 *
 */
@Service
public class DocumentReadPlatformServiceImpl implements DocumentReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
  

    @Autowired
    public DocumentReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
      
    }

    @Override
    public Collection<DocumentData> retrieveAllDocuments(final String entityType, final Long entityId) {

        this.context.authenticatedUser();

        // TODO verify if the entities are valid and a user
        // has data
        // scope for the particular entities
        final DocumentMapper mapper = new DocumentMapper(true,true);
        final String sql = "select " + mapper.schema() + " and d.is_delete='N' order by d.id";
        return this.jdbcTemplate.query(sql, mapper, new Object[] { entityType, entityId });
    }

    @Override
    public DocumentData retrieveDocument(final String entityType, final Long entityId, final Long documentId) {

        try {
            this.context.authenticatedUser();

            // TODO verify if the entities are valid and a
            // user has data
            // scope for the particular entities
            final DocumentMapper mapper = new DocumentMapper(true,true);
            final String sql = "select " + mapper.schema() + " and d.id=? ";
            return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { entityType, entityId, documentId });
        } catch (final EmptyResultDataAccessException e) {
            throw new DocumentNotFoundException(entityType, entityId, documentId);
        }
    }
    
    
    @Override
    public FileData retrieveFileData(final String entityType, final Long entityId, final Long documentId) {
    try {
    final DocumentMapper mapper = new DocumentMapper(false,false);
    final DocumentData documentData = fetchDocumentDetails(entityType, entityId, documentId, mapper);
    //final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository(documentData.storageType());
    return this.fetchFile(documentData);
   
    } catch (final EmptyResultDataAccessException e) {
    throw new DocumentNotFoundException(entityType, entityId, documentId);
    }
    }

	private DocumentData fetchDocumentDetails(final String entityType, final Long entityId, final Long documentId,
    		final DocumentMapper mapper) {
    		final String sql = "select " + mapper.schema() + " and d.id=? ";
    		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { entityType, entityId, documentId });
    		}

    private static final class DocumentMapper implements RowMapper<DocumentData> {

        private final boolean hideLocation;
        private final boolean hideStorageType;

        public DocumentMapper(final boolean hideLocation,final boolean hideStorageType) {
            this.hideLocation = hideLocation;
            this.hideStorageType = hideStorageType;
        }

        public String schema() {
            return "d.id as id, d.parent_entity_type as parentEntityType, d.parent_entity_id as parentEntityId, d.name as name, "
                    + " d.file_name as fileName, d.size as fileSize, d.type as fileType, "
                    + " d.description as description, d.location as location,d.storage_type_enum as storageType"
                    + " from m_document d where d.parent_entity_type=? and d.parent_entity_id=?";
        }

        @Override
        public DocumentData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Long parentEntityId = JdbcSupport.getLong(rs, "parentEntityId");
            final Long fileSize = JdbcSupport.getLong(rs, "fileSize");
            final String parentEntityType = rs.getString("parentEntityType");
            final String name = rs.getString("name");
            final String fileName = rs.getString("fileName");
            final String fileType = rs.getString("fileType");
            final String description = rs.getString("description");
            String location = null;
            Integer storageType = null;
            if (!this.hideLocation) {
                location = rs.getString("location");
            }
            if (!this.hideStorageType) {
            	storageType = rs.getInt("storageType");
            	}

            return new DocumentData(id, parentEntityType, parentEntityId, name, fileName, fileSize, fileType, description, location,storageType);
        }
    }
    
	public FileData fetchFile(DocumentData documentData) {
		
		 final File file = new File(documentData.fileLocation());
		 return new FileData(file, documentData.fileName(), documentData.contentType());
	}
	
	@Override
	public String retrievePdfFile(Long clientId) {

		context.authenticatedUser();
		final GetFileNameMapper rowMapper = new GetFileNameMapper();
		final String sql = "select " + rowMapper.query()
				+ " where c.id = ?  ";
		return jdbcTemplate.queryForObject(sql, rowMapper, new Object[] { clientId });
	}

	public class GetFileNameMapper implements RowMapper<String> {

		public String query() {

			return  " c.filename as printFileName from m_client c ";
		}

		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {

			final String printFileName = rs.getString("printFileName");

			return printFileName ;
		}

	}
    
}