SET SQL_SAFE_UPDATES=0;

DROP PROCEDURE  IF EXISTS orderConnectionType;
DELIMITER //
CREATE PROCEDURE orderConnectionType() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_orders'
      AND COLUMN_NAME ='connection_type') THEN
ALTER TABLE b_orders ADD COLUMN `connection_type` VARCHAR(100) DEFAULT NULL AFTER `user_action`;
END IF;
END //
DELIMITER ;
call orderConnectionType();
DROP PROCEDURE  IF EXISTS orderConnectionType;

UPDATE b_orders SET connection_type='REGULAR' WHERE connection_type IS NULL and is_deleted<>'y';
