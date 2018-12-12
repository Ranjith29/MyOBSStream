Drop procedure IF EXISTS discountCode; 
DELIMITER //
create procedure discountCode() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'discount_code'
     and TABLE_NAME = 'b_order_discount'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_order_discount` ADD COLUMN `discount_code` VARCHAR(20) default NULL AFTER `discount_id`;
END IF;
END //
DELIMITER ;
call discountCode();
Drop procedure IF EXISTS discountCode;
