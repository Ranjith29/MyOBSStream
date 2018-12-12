

Drop procedure IF EXISTS addDurationToUniqueKey;
DELIMITER //
create procedure addDurationToUniqueKey() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema. KEY_COLUMN_USAGE
     WHERE TABLE_SCHEMA = DATABASE() and TABLE_NAME ='b_wifi_details'
     AND CONSTRAINT_NAME = 'uq_wd_orderId')THEN
     ALTER TABLE `b_wifi_details` 
     ADD UNIQUE INDEX `uq_wd_orderId` (`order_id` ASC);
END IF;
END //
DELIMITER ;
call addDurationToUniqueKey();
Drop procedure IF EXISTS addDurationToUniqueKey;

