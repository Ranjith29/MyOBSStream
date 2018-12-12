DROP PROCEDURE  IF EXISTS recurring_alter_columns;
DELIMITER //
CREATE PROCEDURE recurring_alter_columns() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_recurring'
      AND COLUMN_NAME ='createdby_id') THEN
ALTER TABLE b_recurring ADD COLUMN createdby_id BIGINT(20) DEFAULT NULL AFTER  is_deleted;
END IF;
 IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_recurring'
      AND COLUMN_NAME ='created_date') THEN
ALTER TABLE b_recurring ADD COLUMN created_date DATETIME DEFAULT NULL AFTER  createdby_id;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_recurring'
      AND COLUMN_NAME ='lastmodifiedby_id') THEN
ALTER TABLE b_recurring ADD COLUMN lastmodifiedby_id BIGINT(20) DEFAULT NULL AFTER  created_date;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_recurring'
      AND COLUMN_NAME ='lastmodified_date') THEN
ALTER TABLE b_recurring ADD COLUMN lastmodified_date DATETIME DEFAULT NULL AFTER  lastmodifiedby_id;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_clientuser'
      AND COLUMN_NAME ='is_auto_billing') THEN
ALTER TABLE b_clientuser ADD COLUMN is_auto_billing  char(1) DEFAULT 'N' AFTER  is_deleted;
END IF;

END //
DELIMITER ;
call recurring_alter_columns();
DROP PROCEDURE  IF EXISTS recurring_alter_columns;
