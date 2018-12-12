SET SQL_SAFE_UPDATES=0;
-- Device status present and back --
CREATE 
  OR REPLACE
VIEW `stock_history_vw` AS
    select 
        `o`.`name` AS `BRANCH`,
        `s`.`supplier_description` AS `SUPPLIER`,
        `im`.`item_code` AS `ITEM CODE`,
        `im`.`item_description` AS `ITEM NAME`,
        `id`.`serial_no` AS `SERIALNO`,
        `id`.`provisioning_serialno` AS `PROV-SER-NO`,
        `id`.`quality` AS `QUALITY`,
        date_format(`g`.`purchase_date`,'%d-%m-%Y') AS `PURCHASE DATE`,
        date_format(`bih`.`transaction_date` ,'%d-%m-%Y') AS `TRANSACTION DATE`,
        if((`bih`.`ref_type` = 'Allocation'),
            date_format(`bih`.`transaction_date`, '%d-%m-%Y'),
            NULL) AS `ALLOCATION DATE`,
        if((`dl`.`transaction_date` > `bih`.`transaction_date`),
            'Allocated',
            `id`.`status`) AS `STATUS`,
        if((`bih`.`ref_type` = 'De Allocation'),
            date_format(`bih`.`transaction_date`, '%d-%m-%Y'),
            NULL) AS `DEALLOCATION DATE`
    from
        (((((((`b_grn` `g`
        join `b_supplier` `s` ON ((`g`.`supplier_id` = `s`.`id`)))
        join `b_item_master` `im` ON ((`g`.`item_master_id` = `im`.`id`)))
        join `b_item_detail` `id` ON (((`im`.`id` = `id`.`item_master_id`)
            and (`id`.`grn_id` = `g`.`id`))))
        left join `b_item_history` `bih` ON (((`id`.`serial_no` = convert( `bih`.`serial_number` using utf8))
            and (`im`.`id` = `bih`.`item_master_id`))))
        left join `b_item_history` `dl` ON (((`id`.`serial_no` = convert( `dl`.`serial_number` using utf8))
            and (`dl`.`ref_type` = 'De Allocation')
            and (`id`.`office_id` = `dl`.`to_office`)
            and (`im`.`id` = `dl`.`item_master_id`))))
        left join `m_office` `o` ON ((`id`.`office_id` = `o`.`id`)))) order by `bih`.`transaction_date` desc;

INSERT IGNORE INTO `stretchy_parameter` (`parameter_name`, `parameter_variable`, `parameter_label`, `parameter_displayType`, `parameter_FormatType`, `parameter_default`, `type`) 
VALUES ('asOnDate', 'asOnDate', 'As On Date', 'date', 'date', 'today', 'Report');
SET @offId=(SELECT id FROM stretchy_parameter where parameter_label='Office');
SET @PID=(SELECT id FROM stretchy_parameter WHERE parameter_label='Item');
SET @ASDATE=(SELECT id FROM stretchy_parameter WHERE parameter_label='As On Date');

INSERT IGNORE INTO stretchy_report VALUES(NULL,'Stock History Details', 'Table',NULL,'Inventory',
'select * from stock_history_vw sh where 
((select o.id from m_office o where o.name=sh.BRANCH) = ''${officeId}'' or -1 = ''${officeId}'') 
and ((select i.id from b_item_master i where i.item_code=sh.`ITEM CODE`) = ''${itemId}'' or -1 = ''${itemId}'')
and  date_format(str_to_date(sh.`TRANSACTION DATE`,''%d-%m-%Y''),''%Y-%m-%d'') <=''${asOnDate}''',
'Stock History Details',0,1);

SET @ID=(SELECT id FROM stretchy_report where report_name='Stock History Details');

INSERT IGNORE INTO `stretchy_report_parameter` VALUES (NULL,@ID, @offId,'Office');
INSERT IGNORE INTO `stretchy_report_parameter` VALUES (NULL,@ID, @PID,'Item');
INSERT IGNORE INTO `stretchy_report_parameter` VALUES (NULL,@ID, @ASDATE,'As On Date');

CREATE TABLE IF NOT EXISTS `b_prospect_orders` (
  `id` bigint(10) NOT NULL AUTO_INCREMENT,
  `prospect_id` bigint(10) NOT NULL,
  `plan_id` int(20) NOT NULL,
  `contract_period` int(10) NOT NULL,
  `payterm_code` varchar(20) NOT NULL,
  `no_of_connections` int(20) NOT NULL,
  `price` decimal(22,4) NOT NULL,
  `createdby_id` bigint(10) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(10) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1
