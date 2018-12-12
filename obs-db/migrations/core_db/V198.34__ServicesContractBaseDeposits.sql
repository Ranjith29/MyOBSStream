SET SQL_SAFE_UPDATES=0;

DROP PROCEDURE  IF EXISTS depositFeeOnSubscriptions;
DELIMITER //
CREATE PROCEDURE depositFeeOnSubscriptions() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_fee_detail'
      AND COLUMN_NAME ='plan_id') THEN
ALTER TABLE b_fee_detail ADD COLUMN `plan_id` BIGINT(15) DEFAULT NULL AFTER amount;
END IF;

IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_fee_detail'
      AND COLUMN_NAME ='contract_period') THEN
ALTER TABLE b_fee_detail ADD COLUMN `contract_period` VARCHAR(50) DEFAULT NULL AFTER plan_id;
END IF;

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE
TABLE_CATALOG = 'def' AND TABLE_SCHEMA = DATABASE() AND
TABLE_NAME = 'b_fee_detail' AND INDEX_NAME = 'feeid_with_region_uniquekey')THEN
ALTER TABLE b_fee_detail DROP INDEX feeid_with_region_uniquekey;
ALTER TABLE b_fee_detail ADD CONSTRAINT feeid_with_region_uniquekey UNIQUE (fee_id,region_id,plan_id,contract_period);
END IF;
END //
DELIMITER ;
call depositFeeOnSubscriptions();
DROP PROCEDURE  IF EXISTS depositFeeOnSubscriptions;

INSERT IGNORE INTO b_eventaction_mapping (event_name,action_name,process,is_deleted,is_synchronous) values ('Order Booking','Subscription Deposit','workflow_events','Y','N');


