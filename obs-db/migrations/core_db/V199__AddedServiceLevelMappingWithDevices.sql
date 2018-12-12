
SET SQL_SAFE_UPDATES=0;

INSERT IGNORE INTO c_configuration VALUES(NULL, 'service-device-mapping', '0', NULL, 'Order', 'If this flag is enable we will perform service level mapping with hardware instead of plan and hardware mapping');

DROP procedure IF EXISTS provServiceDetails;
DELIMITER //
CREATE procedure provServiceDetails() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'is_hw_req'
     AND TABLE_NAME = 'b_prov_service_details'
     AND TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE b_prov_service_details ADD COLUMN is_hw_req CHAR(2) DEFAULT 'N' AFTER provision_system,
ADD COLUMN item_id BIGINT(15) DEFAULT NULL AFTER is_hw_req;
END IF;
END //
DELIMITER ;
call provServiceDetails();
DROP procedure IF EXISTS provServiceDetails;
DELIMITER //

DELIMITER ;
DELETE prv1 FROM b_prov_service_details prv1, b_prov_service_details prv2 WHERE prv1.id < prv2.id AND prv1.service_id = prv2.service_id;

DROP procedure IF EXISTS provServiceDetailsUniques;
DELIMITER //
CREATE procedure provServiceDetailsUniques() 
Begin
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE
`TABLE_CATALOG` = 'def' AND `TABLE_SCHEMA` = DATABASE() AND
`TABLE_NAME` = 'b_prov_service_details' AND `INDEX_NAME` = 'service_id')THEN
ALTER TABLE b_prov_service_details DROP INDEX service_id;
END IF;
IF NOT EXISTS ( SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE
`TABLE_CATALOG` = 'def' AND `TABLE_SCHEMA` = DATABASE() AND
`TABLE_NAME` = 'b_prov_service_details' AND `INDEX_NAME` = 'serviceCode' )THEN
ALTER TABLE b_prov_service_details ADD UNIQUE  `serviceCode` (`service_id` ASC),
ADD UNIQUE INDEX `service_identification_uq` (`service_id` ASC, `service_identification` ASC);
END IF;
END //
DELIMITER ;
call provServiceDetailsUniques();
DROP procedure IF EXISTS provServiceDetailsUniques;


DROP procedure IF EXISTS association;
DELIMITER //
CREATE procedure association() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'service_id'
     AND TABLE_NAME = 'b_association'
     AND TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE b_association ADD COLUMN service_id BIGINT(20) DEFAULT NULL AFTER `allocation_type`;
END IF;
END //
DELIMITER ;
call association();
DROP procedure IF EXISTS association;





