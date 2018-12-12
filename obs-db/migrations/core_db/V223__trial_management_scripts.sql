INSERT IGNORE INTO `c_configuration` VALUES (NULL,'Is-Freemium-Plan', '0', NULL, 'Order', 'If this flag is enable we will Change The Dates while Order Activation');

Drop procedure IF EXISTS trial_days_Process; 
DELIMITER //
create procedure trial_days_Process() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'trial_days'
     and TABLE_NAME = 'b_plan_master'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_plan_master` ADD COLUMN `trial_days` INT(5) NULL DEFAULT NULL AFTER `allow_topup`;
END IF;
END //
DELIMITER ;
call trial_days_Process();
Drop procedure IF EXISTS trial_days_Process;



Drop procedure IF EXISTS order_activation_date_Process; 
DELIMITER //
create procedure order_activation_date_Process() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'order_activation_date'
     and TABLE_NAME = 'b_orders'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_orders` ADD COLUMN `order_activation_date` DATETIME NULL DEFAULT NULL AFTER `auto_renew`;
END IF;
END //
DELIMITER ;
call order_activation_date_Process();
Drop procedure IF EXISTS order_activation_date_Process;



