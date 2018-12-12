INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES (
null, 'organisation', 'READ_DEPARTMENT', 'DEPARTMENT', 'READ', '0');

SET @pid = (select id from m_permission where code='READ_DEPARTMENT');
SET @rid = (select id from m_role where name= 'selfcare');
INSERT IGNORE INTO `m_role_permission` (`role_id`, `permission_id`) VALUES (@rid, @pid);


INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES (
null, 'organisation', 'READ_EMPLOYEE', 'EMPLOYEE', 'READ', '0');

SET @pid = (select id from m_permission where code='READ_EMPLOYEE');
SET @rid = (select id from m_role where name= 'selfcare');
INSERT IGNORE INTO `m_role_permission` (`role_id`, `permission_id`) VALUES (@rid, @pid);



INSERT IGNORE INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES (
null, 'organisation', 'CREATE_CREDITCARDPAYMENT', 'CREDITCARDPAYMENT', 'CREATE', '0');

SET @pid = (select id from m_permission where code='CREATE_CREDITCARDPAYMENT');
SET @rid = (select id from m_role where name= 'selfcare');
INSERT IGNORE INTO `m_role_permission` (`role_id`, `permission_id`) VALUES (@rid, @pid);
