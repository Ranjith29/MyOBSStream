SET SQL_SAFE_UPDATES = 0;

Drop procedure IF EXISTS clientcarddetailes; 
DELIMITER //
create procedure clientcarddetailes() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'rtf_type'
     and TABLE_NAME = 'm_client_card_details'
     and TABLE_SCHEMA = DATABASE())THEN
alter table m_client_card_details add column rtf_type VARCHAR(10) DEFAULT NULL AFTER card_expiry_date;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'is_deleted'
     and TABLE_NAME = 'm_client_card_details'
     and TABLE_SCHEMA = DATABASE())THEN
alter table m_client_card_details change column is_deleted is_deleted CHAR(1) NOT NULL DEFAULT 'N' AFTER rtf_type;
END IF;
END //
DELIMITER ;
call clientcarddetailes();
Drop procedure IF EXISTS clientcarddetailes;

update m_client_card_details set rtf_type = 'I' where rtf_type IS NULL;


CREATE OR REPLACE
VIEW `hw_alloc_vw` AS
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
        cast(truncate(`bc`.`discount_amount`,2) as char charset utf8) AS `DISCOUNT AMOUNT`,
        truncate(`bos`.`total_price`, 2) AS `SALE PRICE`,
        `id`.`status` AS `STATUS`,
        `im`.`warranty` AS `WARRANTY`,
        date_format(`id`.`warranty_date`, '%d-%m-%Y') AS `EXPIRYDATE`
    from
        ((((((`m_office` `mo`
        join `m_client` `c` ON ((`mo`.`id` = `c`.`office_id`)))
        join `b_client_address` `ca` ON (((`c`.`id` = `ca`.`client_id`)
            and (`ca`.`address_key` = 'PRIMARY'))))
        join `b_allocation` `a` ON (((`ca`.`client_id` = `a`.`client_id`)
            and (`a`.`is_deleted` = 'N'))))
        join `b_item_detail` `id` ON (((`a`.`serial_no` = `id`.`serial_no`)
            and (`c`.`id` = `id`.`client_id`))))
        join `b_onetime_sale` `bos` ON (((`id`.`client_id` = `bos`.`client_id`)
            and (`a`.`order_id` = `bos`.`id`)
            and (`bos`.`is_deleted` = 'N'))))
        join `b_charge` `bc` on (`bos`.`invoice_id`=`bc`.`invoice_id`)
        join `b_item_master` `im` ON ((`id`.`item_master_id` = `im`.`id`)))
    group by `a`.`serial_no`
    order by `bos`.`sale_date` desc
