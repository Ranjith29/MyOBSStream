CREATE TABLE IF NOT EXISTS `b_office_department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `department_name` varchar(100) NOT NULL,
  `department_description` varchar(200) DEFAULT NULL,
  `office_id` bigint(20) NOT NULL,
  `is_deleted` VARCHAR(3) DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `fk_oca_client` (`office_id`),
  CONSTRAINT `fk_oda_client` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;


INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES (null, 'organisation', 'CREATE_DEPARTMENT', 'DEPARTMENT', 'CREATE','0');

INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES (null, 'organisation', 'UPDATE_DEPARTMENT', 'DEPARTMENT', 'UPDATE', '0');

INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES (null, 'organisation', 'DELETE_DEPARTMENT', 'DEPARTMENT', 'DELETE', '0');
