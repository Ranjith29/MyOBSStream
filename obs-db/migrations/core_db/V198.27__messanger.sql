Drop procedure IF EXISTS addRequestType;
DELIMITER //
create procedure addRequestType() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'request_type'
     and TABLE_NAME = 'b_userchat'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_userchat add column request_type VARCHAR(25) DEFAULT NULL  AFTER createdby_user;

END IF;
END //
DELIMITER ;
call addRequestType();
Drop procedure IF EXISTS addRequestType;


INSERT ignore INTO `c_configuration` (`id`, `name`, `enabled`, `value`) VALUES (null, 'hardware_sales_limit', '1', '4');

