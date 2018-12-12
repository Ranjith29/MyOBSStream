
Drop procedure IF EXISTS enable_marketing_mails; 
DELIMITER //
create procedure enable_marketing_mails() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'is_enable_marketing_mails'
     and TABLE_NAME = 'b_clientuser'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE b_clientuser ADD COLUMN is_enable_marketing_mails  CHAR(1) NULL DEFAULT 'Y' AFTER zebra_subscriber_id;
END IF;
END //
DELIMITER ;
call enable_marketing_mails();
Drop procedure IF EXISTS enable_marketing_mails;


