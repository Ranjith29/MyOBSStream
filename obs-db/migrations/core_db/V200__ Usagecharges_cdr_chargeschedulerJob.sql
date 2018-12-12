SET SQL_SAFE_UPDATES=0;

CREATE TABLE IF NOT EXISTS `b_usage_raw` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(10) NOT NULL,
  `number` varchar(30) NOT NULL,
  `time` datetime NOT NULL,
  `destination_number` varchar(40) DEFAULT NULL,
  `destination_location` varchar(50) DEFAULT NULL,
  `duration` decimal(24,4) DEFAULT NULL,
  `cost` decimal(24,4) DEFAULT NULL,
  `usage_charge_id` int(20) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `b_usage_charge` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(10) NOT NULL,
  `number` varchar(50) NOT NULL,
  `charge_date` datetime NOT NULL,
  `total_duration` decimal(24,4) DEFAULT NULL,
  `total_cost` decimal(24,4) NOT NULL,
  `charge_id` int(20) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT IGNORE INTO m_permission VALUES(NULL,'billing', 'CREATE_CHARGES', 'CHARGES', 'CREATE', '0');

INSERT IGNORE INTO stretchy_report VALUES(NULL, 'UsageCharges', 'Table', '', 'Scheduling Job', 
'select client_id as clientId,number as number from b_usage_raw  where usage_charge_id is null  group by clientId,number', 
'Process Customer Usage Charges', '0', '0');


DROP procedure IF EXISTS jobUniqueName; 
DELIMITER //
CREATE procedure jobUniqueName() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME ='name' AND COLUMN_KEY='UNI'
     and TABLE_NAME ='job'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `job` ADD UNIQUE INDEX `name_unique` (`name` ASC);
END IF;
END //
DELIMITER ;
call jobUniqueName();
DROP procedure IF EXISTS jobUniqueName;

DELETE prv1 FROM job_parameters AS prv1,
     job_parameters AS prv2 
   WHERE prv1.id < prv2.id
    AND prv1.job_id = prv2.job_id   
AND prv1.param_name = prv2.param_name;

DROP procedure IF EXISTS jobParamUnique; 
DELIMITER //
CREATE procedure jobParamUnique() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() and TABLE_NAME ='job_parameters'
      and COLUMN_NAME ='param_name' and INDEX_NAME = 'job_id_param')THEN
ALTER TABLE `job_parameters` ADD UNIQUE INDEX `job_id_param` (`job_id` ASC, `param_name` ASC);
END IF;
END //
DELIMITER ;
call jobParamUnique();
DROP procedure IF EXISTS jobParamUnique;

INSERT IGNORE INTO job VALUES(NULL, 'USAGECHARGES', 'Usage Charges', '0 30 23 1/1 * ? *', 'Daily once at Midnight',
 now(), '5', NULL, now(), NULL, 'USAGECHARGESJobDetaildefault _ DEFAULT', NULL, '0', '0', '1', '0', '0', '1');

SET @ID=(select id from job where name='USAGECHARGES');
INSERT IGNORE INTO job_parameters VALUES(NULL, @ID, 'reportName', 'COMBO', NULL, 'UsageCharges', 'N', NULL);

SET @Id=(SELECT id FROM m_code WHERE code_name='Charge Type');
INSERT IGNORE INTO m_code_value VALUES(NULL,@Id,'UC', '2');

DROP procedure IF EXISTS serviceMapProvision; 
DELIMITER //
CREATE procedure serviceMapProvision() 
Begin
IF EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME ='provision_system' 
     AND TABLE_NAME ='b_prov_service_details'
     AND TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_prov_service_details` 
CHANGE COLUMN `provision_system` `provision_system` VARCHAR(1000) NULL DEFAULT NULL ;
END IF;
END //
DELIMITER ;
call serviceMapProvision();
DROP procedure IF EXISTS serviceMapProvision;


DROP procedure IF EXISTS serviceMapProvision; 
DELIMITER //
CREATE procedure serviceMapProvision() 
Begin
IF EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME ='provision_system' 
     AND TABLE_NAME ='b_prov_service_details'
     AND TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_prov_service_details` 
CHANGE COLUMN `provision_system` `provision_system` VARCHAR(1000) NULL DEFAULT NULL ;
END IF;
END //
DELIMITER ;
call serviceMapProvision();
DROP procedure IF EXISTS serviceMapProvision;

ALTER TABLE `b_orders_addons` 
CHANGE COLUMN `provision_system` `provision_system` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `b_process_request` 
CHANGE COLUMN `provisioing_system` `provisioing_system` VARCHAR(1000) NULL DEFAULT NULL ;

DROP procedure IF EXISTS `obsplatform-tenants`.tenantLocale; 
DELIMITER //
CREATE procedure `obsplatform-tenants`.tenantLocale() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME ='locale_name'
     and TABLE_NAME ='tenants'
     and TABLE_SCHEMA = 'obsplatform-tenants')THEN
ALTER TABLE tenants ADD COLUMN locale_name VARCHAR(20) DEFAULT NULL AFTER license_key;
UPDATE tenants SET locale_name='en_IN';
END IF;
END //
DELIMITER ;
call `obsplatform-tenants`.tenantLocale();
DROP procedure IF EXISTS `obsplatform-tenants`.tenantLocale;


CREATE 
  OR REPLACE
VIEW `bmaster_vw` AS
    select distinct
        `bm`.`id` AS `billId`,
        `bm`.`Bill_No` AS `billNo`,
        `bm`.`Client_id` AS `clientId`,
        cast(`bm`.`Bill_date` as date) AS `billDate`,
        `bm`.`Due_date` AS `dueDate`,
        `bm`.`Previous_balance` AS `previousBalance`,
        `bm`.`Charges_amount` AS `chargesAmount`,
        `bm`.`Adjustment_amount` AS `adjustmentAmount`,
        round(`bm`.`Tax_amount`, 2) AS `taxAmount`,
        `bm`.`Paid_amount` AS `paidAmount`,
        `bm`.`deposit_refund_amount` AS `depositRefundAmount`,
        `bm`.`Due_amount` AS `dueAmount`,
        `bm`.`Promotion_description` AS `Description`,
        sum(`bc`.`discount_amount`) AS `discountAmount`,
        `ca`.`address_id` AS `addressId`,
        `ca`.`address_key` AS `addressKey`,
        `ca`.`address_no` AS `addressNo`,
        `ca`.`street` AS `street`,
        `ca`.`zip` AS `zip`,
        `ca`.`city` AS `city`,
        `ca`.`state` AS `state`,
        `ca`.`country` AS `country`,
        concat(`ca`.`address_no`,
                `ca`.`street`,
                ',',
                `ca`.`city`,
                ',',
                `ca`.`state`,
                ',',
                `ca`.`country`,
                '-',
                `ca`.`zip`) AS `address`,
        `c`.`account_no` AS `accountNo`,
        `c`.`external_id` AS `externalId`,
        `c`.`status_enum` AS `status`,
        `c`.`activation_date` AS `activationDate`,
        `c`.`office_id` AS `officeId`,
        `c`.`firstname` AS `firstname`,
        `c`.`middlename` AS `middlename`,
        `c`.`lastname` AS `lastname`,
        `c`.`display_name` AS `displayName`,
        `c`.`email` AS `emailId`,
        `c`.`image_key` AS `imageKey`,
        `c`.`category_type` AS `categoryType`,
        `mo`.`name` AS `officeName`
    from
        ((((`b_bill_master` `bm`
        join `m_client` `c` ON ((`bm`.`Client_id` = `c`.`id`)))
        join `m_office` `mo` ON ((`c`.`office_id` = `mo`.`id`)))
        left join `b_client_address` `ca` ON (((`ca`.`client_id` = `c`.`id`)
            and (`ca`.`address_key` = 'PRIMARY')
            and (`ca`.`is_deleted` = 'n'))))
        left join `b_charge` `bc` ON (((`bm`.`id` = `bc`.`bill_id`)
            and (`bc`.`discount_amount` > 0))))
    group by `bm`.`id`;


