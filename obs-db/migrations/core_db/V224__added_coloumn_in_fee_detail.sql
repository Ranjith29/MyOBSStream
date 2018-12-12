Drop procedure IF EXISTS addedColoumnInFeeDetail; 
DELIMITER //
create procedure addedColoumnInFeeDetail() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'category_id'
     and TABLE_NAME = 'b_fee_detail'
     and TABLE_SCHEMA = DATABASE())THEN
     ALTER TABLE `b_fee_detail` 
     ADD COLUMN `category_id` INT(10) NULL DEFAULT NULL AFTER `contract_period`;
END IF;
END //
DELIMITER ;
call addedColoumnInFeeDetail();
Drop procedure IF EXISTS addedColoumnInFeeDetail;


Drop procedure IF EXISTS changeColoumnInFeeMaster; 
DELIMITER //
create procedure changeColoumnInFeeMaster() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'fee_code'
     and TABLE_NAME = 'b_fee_master'
     and TABLE_SCHEMA = DATABASE())THEN
     ALTER TABLE `b_fee_master` 
     CHANGE COLUMN `fee_code` `fee_code` VARCHAR(20) NULL DEFAULT NULL ;
END IF;
END //
DELIMITER ;
call changeColoumnInFeeMaster();
Drop procedure IF EXISTS changeColoumnInFeeMaster;



UPDATE `b_fee_detail` SET `category_id`='20' WHERE `id`='2';
UPDATE `b_fee_detail` SET `category_id`='20' WHERE `id`='3';
UPDATE `b_fee_detail` SET `category_id`='20' WHERE `id`='4';
UPDATE `b_fee_detail` SET `category_id`='20' WHERE `id`='5';

INSERT IGNORE INTO b_fee_detail VALUES (NULL, '2', '1', '40.000000', NULL, NULL,'21', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '2', '1', '40.000000', NULL, NULL,'22', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '2', '1', '40.000000', NULL, NULL,'227', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '2', '1', '40.000000', NULL, NULL,'239', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '2', '1', '40.000000', NULL, NULL,'232', 'N');

INSERT IGNORE INTO b_fee_detail VALUES (NULL, '3', '1', '30.000000', NULL, NULL,'21', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '3', '1', '30.000000', NULL, NULL,'22', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '3', '1', '30.000000', NULL, NULL,'227', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '3', '1', '30.000000', NULL, NULL,'239', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '3', '1', '30.000000', NULL, NULL,'232', 'N');

INSERT IGNORE INTO b_fee_detail VALUES (NULL, '5', '1', '40.000000', NULL, NULL,'21', 'N');

INSERT IGNORE INTO b_fee_detail VALUES (NULL, '6', '1', '90.000000', NULL,NULL,'21', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '6', '1', '90.000000', NULL,NULL,'22', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '6', '1', '90.000000', NULL,NULL,'227', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '6', '1', '90.000000', NULL,NULL,'239', 'N');
INSERT IGNORE INTO b_fee_detail VALUES (NULL, '6', '1', '90.000000', NULL,NULL,'232', 'N');



