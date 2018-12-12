INSERT IGNORE INTO m_permission values(null,'client&orders','DELETENORECORD_ORDER','ORDER','DELETENORECORD',0);
INSERT IGNORE INTO m_permission values(null,'client&orders','DELETENORECORD_CLIENT','CLIENT','DELETENORECORD',0);

DROP PROCEDURE  IF EXISTS event_alter_columns;
DELIMITER //
CREATE PROCEDURE event_alter_columns() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_mod_detail'
      AND COLUMN_NAME ='is_deleted') THEN
ALTER TABLE b_mod_detail ADD column is_deleted char(1) DEFAULT 'N';
END IF;
END //
DELIMITER ;
call event_alter_columns();
DROP PROCEDURE  IF EXISTS event_alter_columns;
