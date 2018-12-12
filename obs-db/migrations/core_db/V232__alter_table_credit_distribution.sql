INSERT ignore INTO `m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES (null, 'billing&finance', 'CANCEL_CREDITDISTRIBUTION', 'CREDITDISTRIBUTION', 'CANCEL', '0');


DROP PROCEDURE  IF EXISTS credit_distribution_alter_columns;
DELIMITER //
CREATE PROCEDURE credit_distribution_alter_columns() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_credit_distribution'
      AND COLUMN_NAME ='is_deleted') THEN
ALTER TABLE b_credit_distribution ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' AFTER `amount`;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_credit_distribution'
      AND COLUMN_NAME ='cancel_remark') THEN
ALTER TABLE b_credit_distribution ADD COLUMN `cancel_remark` VARCHAR(100) NULL DEFAULT NULL AFTER `lastmodifiedby_id`;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_credit_distribution'
      AND COLUMN_NAME ='ref_id') THEN
ALTER TABLE b_credit_distribution ADD COLUMN `ref_id` VARCHAR(100) NULL DEFAULT NULL AFTER `cancel_remark`;
END IF;
END //
DELIMITER ;
call credit_distribution_alter_columns();
DROP PROCEDURE  IF EXISTS credit_distribution_alter_columns;

