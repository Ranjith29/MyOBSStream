SET SQL_SAFE_UPDATES=0;

-- HW ALLOCATION --
CREATE OR REPLACE VIEW `hw_alloc_vw` AS 
select distinct
        `mo`.`name` AS `BRANCH`,
        `c`.`id` AS `CLIENT ID`,
        `c`.`display_name` AS `CLIENT NAME`,
        `im`.`item_code` AS `ITEM CODE`,
        `im`.`item_description` AS `DESCRIPTION`,
		`id`.`serial_no` AS `SERIAL NO`,
		`id`.`provisioning_serialno` AS `PROVISION NO`,
         date_format(`a`.`allocation_date`, '%d-%m-%Y') AS `ALLOCATION DATE`,
		 date_format(`bos`.`sale_date`, '%d-%m-%Y') AS `SALE DATE`,
        truncate(`bos`.`total_price`, 2) AS `SALE PRICE`,
		`id`.`status` AS `STATUS`,
	    `im`.`warranty` AS `WARRANTY`,
         date_format(`id`.`warranty_date`, '%d-%m-%Y') AS `EXPIRYDATE`
    from
        ((((((`m_office` `mo` INNER JOIN 
        `m_client` `c` ON (`mo`.`id` = `c`.`office_id`))
         INNER JOIN `b_client_address` `ca` ON (`c`.`id` = `ca`.`client_id`) AND (`ca`.`address_key` = 'PRIMARY'))
         JOIN `b_allocation` `a` ON (`ca`.`client_id` = `a`.`client_id`) AND (`a`.`is_deleted` = 'N'))
		 JOIN `b_item_detail` `id` ON (( `a`.`serial_no` = `id`.`serial_no`) AND (`c`.`id` = `id`.`client_id`)))
         JOIN `b_onetime_sale` `bos` ON ((`id`.`client_id`= `bos`.`client_id`)  AND (`a`.`order_id` =`bos`.`id`)
         AND (`bos`.`is_deleted` = 'N')))
		 JOIN `b_item_master` `im` ON  (`id`.`item_master_id` = `im`.`id`))         
 GROUP BY `a`.`serial_no` ORDER BY `bos`.`sale_date` DESC;

CREATE OR REPLACE
VIEW `stock_available_vw` AS
    select 
        `o`.`name` AS `BRANCH`,
        `s`.`supplier_description` AS `SUPPLIER`,
        `im`.`item_code` AS `ITEM CODE`,
        `im`.`item_description` AS `ITEM NAME`,
        `id`.`serial_no` AS `SERIAL NO`,
        `id`.`provisioning_serialno` AS `PROV NO`,
        `id`.`quality` AS `QUALITY`,
        date_format(`g`.`purchase_date`, '%d-%m-%Y') AS `PURCHASE DATE`
    from
        (((((`b_grn` `g`
        join `b_supplier` `s` ON ((`g`.`supplier_id` = `s`.`id`)))
        join `b_item_master` `im` ON ((`g`.`item_master_id` = `im`.`id`)))
        join `b_item_detail` `id` ON (((`im`.`id` = `id`.`item_master_id`)
            and (`id`.`status` = 'Available') AND `id`.`client_id` IS NULL)))
        left join `m_office` `o` ON ((`id`.`office_id` = `o`.`id`))))
        -- left join `m_code_value` `mcv` ON (((`mcv`.`code_id` = 46)
        --    and (`mcv`.`id` = `o`.`office_type`))))
    group by `id`.`serial_no`;

INSERT IGNORE INTO stretchy_parameter VALUES(NULL, 'ItemIdSelectOne', 'itemId', 'Item', 'select', 'number', '0', NULL, 'Y', 'Y', 
'select id,item_description from b_item_master where is_deleted=''N'' order by id asc', NULL, 'Report');
SET @ID=(SELECT id FROM stretchy_report WHERE report_name='Stock Item Details');
SET @HID=(SELECT id FROM stretchy_report WHERE report_name='List of HardWare Allocations');

DELETE FROM `stretchy_report_parameter` WHERE `report_id`=@ID AND report_parameter_name <> 'Office';
SET @PID=(SELECT id FROM stretchy_parameter WHERE parameter_label='Item');

INSERT INTO `stretchy_report_parameter` VALUES (NULL,@ID, @PID,'Item');
INSERT INTO `stretchy_report_parameter` VALUES (NULL,@HID, @PID,'Item');


UPDATE stretchy_report SET report_sql = 'select * from stock_available_vw sv where 
((select o.id from m_office o where o.name=sv.BRANCH) = ''${officeId}'' or -1 = ''${officeId}'' 
and (select i.id from b_item_master i where i.item_code=sv.`ITEM CODE`) = ''${itemId}'' or -1 = ''${itemId}'')'
 WHERE report_name='Stock Item Details';

UPDATE stretchy_report SET report_sql = 'select * from hw_alloc_vw  hw where
((select o.id from m_office o where o.name=hw.BRANCH) = ''${officeId}'' or -1 = ''${officeId}'') 
and  date_format(str_to_date(`hw`.`SALE DATE`,''%d-%m-%Y''),''%Y-%m-%d'')  
between ''${startDate}'' and ''${endDate}'' 
and ((select i.id from b_item_master i where i.item_code=`hw`.`ITEM CODE`) = ''${itemId}'' or -1 = ''${itemId}'')'
, report_name='List of Hardware Allocations' WHERE report_name='List of HardWare Allocations';

DROP PROCEDURE  IF EXISTS addDiscountEndDate;
DELIMITER //
CREATE PROCEDURE addDiscountEndDate() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_discount_master'
      AND COLUMN_NAME ='end_date') THEN
ALTER TABLE b_discount_master  ADD COLUMN `end_date` datetime DEFAULT NULL AFTER `start_date`,
ADD COLUMN  `createdby_id` bigint(20) DEFAULT NULL AFTER `is_delete`,
ADD COLUMN  `created_date` datetime DEFAULT NULL AFTER `createdby_id`,
ADD COLUMN  `lastmodifiedby_id` bigint(20) DEFAULT NULL AFTER `created_date`,
ADD COLUMN  `lastmodified_date` datetime DEFAULT NULL AFTER `lastmodifiedby_id`;
END IF;
END //
DELIMITER ;
call addDiscountEndDate();
DROP PROCEDURE  IF EXISTS addDiscountEndDate;
