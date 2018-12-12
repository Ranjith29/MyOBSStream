INSERT IGNORE INTO `c_paymentgateway_conf` (`id`, `name`, `enabled`, `value`, `description`) VALUES (null, 'regularcheck', '0', '', 'when client want to pay by cheque we use this');


DROP PROCEDURE  IF EXISTS department_alter_columns;
DELIMITER //
CREATE PROCEDURE department_alter_columns() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_office_department'
      AND COLUMN_NAME ='is_allocated') THEN
ALTER TABLE b_office_department ADD COLUMN `is_allocated` varchar(10) Default 'No' AFTER is_deleted;
END IF;
 
END //
DELIMITER ;
call department_alter_columns();
DROP PROCEDURE  IF EXISTS department_alter_columns;
