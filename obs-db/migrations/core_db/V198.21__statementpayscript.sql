Drop procedure IF EXISTS addisPay;
DELIMITER //
create procedure addisPay() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'is_pay'
     and TABLE_NAME = 'b_bill_master'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE b_bill_master ADD COLUMN `is_pay` char(2) DEFAULT 'N' AFTER Parent_id;

END IF;
END //
DELIMITER ;
call addisPay();
Drop procedure IF EXISTS addisPay;

-- read device permissions for selfcare
SET SQL_SAFE_UPDATES=0;
SET @PID=(SELECT id FROM m_permission WHERE code='READ_ONETIMESALE');
SET @RID=(SELECT id FROM m_role WHERE name='selfcare');
INSERT IGNORE INTO m_role_permission VALUES(@RID,@PID);

SET @PID=(SELECT id FROM m_permission WHERE code='READ_OWNEDHARDWARE');
SET @RID=(SELECT id FROM m_role WHERE name='selfcare');
INSERT IGNORE INTO m_role_permission VALUES(@RID,@PID);
SET SQL_SAFE_UPDATES=1;
