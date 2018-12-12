INSERT ignore INTO `m_permission`(`id`,`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`)
VALUES (null, 'organisation', 'CREATE_CREDITCARDPAYMENT', 'CREDITCARDPAYMENT', 'CREATE', '0');

INSERT ignore INTO `m_role` (`name`, `description`) VALUES ('employee', 'Employee');


DROP PROCEDURE  IF EXISTS employee_alter_columns;
DELIMITER //
CREATE PROCEDURE employee_alter_columns() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_office_employee'
      AND COLUMN_NAME ='user_id') THEN
ALTER TABLE b_office_employee ADD COLUMN `user_id` BIGINT(20) Default NULL AFTER  is_deleted;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_master'
      AND COLUMN_NAME ='dept_id') THEN
ALTER TABLE b_ticket_master ADD COLUMN `dept_id` BIGINT(20) Default NULL AFTER  lastmodified_date;
END IF;
END //
DELIMITER ;
call employee_alter_columns();
DROP PROCEDURE  IF EXISTS employee_alter_columns;
