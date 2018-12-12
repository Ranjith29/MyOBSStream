CREATE TABLE IF NOT EXISTS `b_office_employee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_name` varchar(100) DEFAULT NULL,
  `employee_loginname` varchar(100) DEFAULT NULL,
  `employee_password` varchar(100) DEFAULT NULL,
  `employee_phone` bigint(20) DEFAULT NULL,
  `employee_email` varchar(100) DEFAULT NULL,
  `department_id` bigint(20) NOT NULL,
  `employee_isprimary` char(1) DEFAULT NULL,
  `is_deleted` char(1) DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `FK_dprttId` (`department_id`),
  CONSTRAINT `FK_dprttId` FOREIGN KEY (`department_id`) REFERENCES `b_office_department` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=latin1;

INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES (null, 'organisation', 'CREATE_EMPLOYEE', 'EMPLOYEE', 'CREATE','0');

INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES (null, 'organisation', 'UPDATE_EMPLOYEE', 'EMPLOYEE', 'UPDATE', '0');

INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
VALUES (null, 'organisation', 'DELETE_EMPLOYEE', 'EMPLOYEE', 'DELETE', '0');

INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES (null, 'organisation', 'READ_RECURRING', 'RECURRING', 'READ', '0');

INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES (null, 'organisation', 'CREATEAUTHORIZE_RECURRING', 'RECURRING', 'CREATEAUTHORIZE', '0');

INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES (null, 'organisation', 'AUTHORIZE_RECURRING', 'RECURRING', 'AUTHORIZE', '0');

SET @pid = (select id from m_permission where code='READ_RECURRING');
SET @rid = (select id from m_role where name= 'selfcare');
INSERT IGNORE INTO `m_role_permission` (`role_id`, `permission_id`) VALUES (@rid, @pid);

SET @pid = (select id from m_permission where code='CREATEAUTHORIZE_RECURRING');
SET @rid = (select id from m_role where name= 'selfcare');
INSERT IGNORE INTO `m_role_permission` (`role_id`, `permission_id`) VALUES (@rid, @pid);

SET @pid = (select id from m_permission where code='AUTHORIZE_RECURRING');
SET @rid = (select id from m_role where name= 'selfcare');
INSERT IGNORE INTO `m_role_permission` (`role_id`, `permission_id`) VALUES (@rid, @pid);

DELETE FROM `c_configuration` WHERE `name`='book-order';
DELETE FROM `c_configuration` WHERE `name`='change-plan';
DELETE FROM `c_configuration` WHERE `name`='renewal-order';
INSERT INTO `c_configuration` (`id`, `name`, `enabled`, `value`, `module`) VALUES (null, 'is_pay_onOrder_actions', 1, '', '');



