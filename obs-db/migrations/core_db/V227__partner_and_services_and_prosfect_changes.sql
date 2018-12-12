Drop procedure IF EXISTS addedOfficeColoumn;
DELIMITER //
create procedure addedOfficeColoumn() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='m_office' 
     And  COLUMN_NAME in( 'partner_type_id')) then
     ALTER TABLE `m_office` 
     ADD COLUMN `partner_type_id` INT(10) NULL DEFAULT NULL AFTER `office_type`;
END IF;
END //
DELIMITER ;
call addedOfficeColoumn();
Drop procedure IF EXISTS addedOfficeColoumn;


SET @ID=(select id from m_code where code_name = 'Partner Type');
INSERT IGNORE INTO m_code_value values(null, @ID, 'Service Provider',3);


SET @ID=(select id from m_code_value where code_value = 'Partner');
DELETE FROM `m_code_value` WHERE `id`= @ID;


INSERT IGNORE INTO m_permission VALUES (NULL, 'organization', 'CREATE_SERVICEPARTNERMAPPING', 'SERVICEPARTNERMAPPING', 'CREATE', '0');
INSERT IGNORE INTO m_permission VALUES (NULL, 'organization', 'UPDATE_SERVICEPARTNERMAPPING', 'SERVICEPARTNERMAPPING', 'UPDATE', '0');
INSERT IGNORE INTO m_permission VALUES (NULL, 'organization', 'READ_SERVICEPARTNERMAPPING', 'SERVICEPARTNERMAPPING', 'READ', '0');
INSERT IGNORE INTO m_permission VALUES (NULL, 'organization', 'DELETE_SERVICEPARTNERMAPPING', 'SERVICEPARTNERMAPPING', 'DELETE', '0');


CREATE TABLE IF NOT EXISTS `b_service_partner_mapping` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `partner_name` varchar(50) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_code_uq` (`service_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;


Drop procedure IF EXISTS addedOfficeAgreementColoumn;
DELIMITER //
create procedure addedOfficeAgreementColoumn() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='m_office_agreement_detail' 
     And  COLUMN_NAME in( 'service_id')) then
     ALTER TABLE `m_office_agreement_detail` 
     CHANGE COLUMN `source` `source` BIGINT(30) NULL DEFAULT NULL ,
     ADD COLUMN `service_id` BIGINT(20) NULL DEFAULT NULL AFTER `source`;
END IF;
END //
DELIMITER ;
call addedOfficeAgreementColoumn();
Drop procedure IF EXISTS addedOfficeAgreementColoumn;


Drop procedure IF EXISTS addServiceIdToUniqueKey;
DELIMITER //
create procedure addServiceIdToUniqueKey() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema. KEY_COLUMN_USAGE
     WHERE TABLE_SCHEMA = DATABASE() and TABLE_NAME ='m_office_agreement_detail'
     AND CONSTRAINT_NAME = 'serviceid_agreementid_uq')THEN
     ALTER TABLE `m_office_agreement_detail` 
     ADD UNIQUE INDEX `serviceid_agreementid_uq` (`agreement_id` ASC, `service_id` ASC);
END IF;
END //
DELIMITER ;
call addServiceIdToUniqueKey();
Drop procedure IF EXISTS addServiceIdToUniqueKey;


Drop procedure IF EXISTS addedProspectColoumn;
DELIMITER //
create procedure addedProspectColoumn() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_prospect' 
     And  COLUMN_NAME in( 'filename')) then
     ALTER TABLE `b_prospect` 
     ADD COLUMN `filename` VARCHAR(200) NULL DEFAULT NULL AFTER `status_remark`;
END IF;
END //
DELIMITER ;
call addedProspectColoumn();
Drop procedure IF EXISTS addedProspectColoumn;


Drop procedure IF EXISTS addedClientColoumn;
DELIMITER //
create procedure addedClientColoumn() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='m_client' 
     And  COLUMN_NAME in( 'filename')) then
     ALTER TABLE `m_client` 
     ADD COLUMN `filename` VARCHAR(200) NULL DEFAULT NULL AFTER `registration_fee`;
END IF;
END //
DELIMITER ;
call addedClientColoumn();
Drop procedure IF EXISTS addedClientColoumn;


INSERT IGNORE INTO `b_eventaction_mapping` (`event_name`, `action_name`, `process`, `is_deleted`, `is_synchronous`) VALUES ('Create Client', 'Client Quotation', 'workflow_events', 'N', 'N');


INSERT IGNORE INTO `b_eventaction_mapping` (`event_name`, `action_name`, `process`, `is_deleted`, `is_synchronous`) VALUES ('Prospect Creation', 'Prospect Quotation', 'workflow_events', 'N', 'N');


INSERT IGNORE INTO m_permission VALUES (NULL, 'client&orders', 'DELETE_ORDERADDONS', 'ORDERADDONS', 'DELETE', '0');
INSERT IGNORE INTO m_permission VALUES (NULL, 'client&orders', 'CANCEL_ORDERADDONS', 'ORDERADDONS', 'CANCEL', '0');
