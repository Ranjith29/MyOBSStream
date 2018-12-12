CREATE TABLE IF NOT EXISTS `b_exdirectory` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `client_id` int(20) NOT NULL,
  `order_id` int(20) NOT NULL,
  `plan_id` int(20) DEFAULT NULL,
  `service_id` int(20) DEFAULT NULL,
  `is_ex_directory` tinyint(1) NOT NULL DEFAULT '0',
  `is_number_with_held` tinyint(1) NOT NULL DEFAULT '0',
  `is_deleted` char(1) NOT NULL DEFAULT 'N',
  `createdby_id` bigint(20) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

INSERT IGNORE INTO b_provisioning_actions VALUES(NULL, 'Change ExDirectory', 'CHANGE EXDIRECTORY', 'u-mee sync server', 'N', 'N');


INSERT IGNORE INTO m_permission VALUES (NULL, 'organization', 'READ_ISEXDIRECTORY', 'ISEXDIRECTORY', 'READ', '0');
INSERT IGNORE INTO m_permission VALUES (NULL, 'organization', 'CREATE_ISEXDIRECTORY', 'ISEXDIRECTORY', 'CREATE', '0');
INSERT IGNORE INTO m_permission VALUES (NULL, 'organization', 'UPDATE_ISEXDIRECTORY', 'ISEXDIRECTORY', 'UPDATE', '0');
