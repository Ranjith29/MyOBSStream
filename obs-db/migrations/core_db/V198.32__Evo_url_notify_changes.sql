DROP PROCEDURE  IF EXISTS evo_notify_status;
DELIMITER //
CREATE PROCEDURE evo_notify_status() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_evo_notify'
      AND COLUMN_NAME ='client_id') THEN
ALTER TABLE b_evo_notify ADD COLUMN client_id bigint(20) DEFAULT NULL;
END IF;
 IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_evo_notify'
      AND COLUMN_NAME ='status') THEN
ALTER TABLE b_evo_notify ADD COLUMN status VARCHAR(100) DEFAULT NULL;
END IF;
END //
DELIMITER ;
call evo_notify_status();
DROP PROCEDURE  IF EXISTS evo_notify_status;
