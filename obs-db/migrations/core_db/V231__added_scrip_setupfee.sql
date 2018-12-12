
INSERT IGNORE INTO `m_permission` 
(`id`,`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`)
 VALUES (NULL,'client&orders', 'CREATE_SETUPFEE', 'SETUPFEE', 'CREATE', '0');


SET @ID=(select id from m_code where code_name='Transaction Type');

INSERT IGNORE INTO m_code_value values(null,@ID,'Setup Fee',6);



INSERT IGNORE  INTO `r_enum_value` 
(`enum_name`, `enum_id`, `enum_message_property`, `enum_value`) 
VALUES ('service_type', '4', 'Service', 'Service');


INSERT IGNORE INTO `stretchy_report` 
(`id`,`report_name`, `report_type`, `report_category`, `report_sql`, 
`description`, `core_report`, `use_report`) 
VALUES 
(NULL,'No of Clients for Plan Wise Addons', 'Table', 'Orders', 
'select a.plan_code AS PLANCODE, a.service_code as ADDONSERVICECODE , 
a.status as STATUS, count(a.client_id) AS NO_OF_CLIENTS FROM  (SELECT \n    bp.plan_code ,\n    bp.plan_description ,\n    boa.status ,\n    bs.service_code,\n    bs.service_description ,\n    bo.client_id\n    \nFROM\n    b_plan_master bp\n        join\n    b_orders bo ON bo.plan_id = bp.id\n        join\n    b_orders_addons boa ON boa.order_id = bo.id\n        join\n    b_service bs ON bs.id = boa.service_id\nwhere\n    boa.status = \'ACTIVE\'\n) as a\ngroup by a.plan_code, a.service_code', 
'No of Clients for Plan Wise Addons', '0', '1');

-- fin_trans_vw changed for add setupfee --


CREATE OR REPLACE VIEW `fin_trans_vw` AS
    SELECT DISTINCT
        `b_invoice`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_invoice`.`client_id` AS `client_id`,
        IF((`b_charge`.`charge_type` = 'NRC'),
            'Once',
            'Periodic') AS `tran_type`,
        CAST(`b_invoice`.`invoice_date` AS DATE) AS `transDate`,
        (CASE
            WHEN (`b_charge`.`transaction_type` = 'SERVICE_TRANSFER') THEN 'SERVICE TRANSFER'
            WHEN (`b_charge`.`transaction_type` = 'REGISTRATION_FEE') THEN 'REGISTRATION CHARGE'
            WHEN (`b_charge`.`transaction_type` = 'TERMINATION_FEE') THEN 'TERMINATION CHARGE'
            WHEN (`b_charge`.`transaction_type` = 'RECONNECTION FEE') THEN 'RECONNECTION CHARGE'
            WHEN (`b_charge`.`transaction_type` = 'REACTIVATION_FEE') THEN 'REACTIVATION CHARGE'
            WHEN (`b_charge`.`transaction_type` = 'SETUP_FEE') THEN 'SETUP FEE'
			
            ELSE 'INVOICE'
        END) AS `transType`,
        IF((`b_invoice`.`invoice_amount` > 0),
            `b_invoice`.`invoice_amount`,
            0) AS `dr_amt`,
        IF((`b_invoice`.`invoice_amount` < 0),
            ABS(`b_invoice`.`invoice_amount`),
            0) AS `cr_amt`,
        1 AS `flag`,
        0 AS `refundFlag`,
        `b_invoice`.`created_date` AS `transDateTime`
    FROM
        ((`b_invoice`
        JOIN `m_appuser`)
        JOIN `b_charge`)
    WHERE
        ((`b_invoice`.`createdby_id` = `m_appuser`.`id`)
            AND (`b_invoice`.`id` = `b_charge`.`invoice_id`)
            AND (`b_invoice`.`invoice_date` <= NOW()))
    GROUP BY `b_invoice`.`id` 
    UNION ALL SELECT DISTINCT
        `b_adjustments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_adjustments`.`client_id` AS `client_id`,
        (SELECT 
                `m_code_value`.`code_value`
            FROM
                `m_code_value`
            WHERE
                ((`m_code_value`.`code_id` = 12)
                    AND (`b_adjustments`.`adjustment_code` = `m_code_value`.`id`))) AS `tran_type`,
        CAST(`b_adjustments`.`adjustment_date` AS DATE) AS `transdate`,
        'ADJUSTMENT' AS `transType`,
        0 AS `dr_amt`,
        (CASE `b_adjustments`.`adjustment_type`
            WHEN 'CREDIT' THEN `b_adjustments`.`adjustment_amount`
        END) AS `cr_amount`,
        1 AS `flag`,
        0 AS `refundFlag`,
        `b_adjustments`.`created_date` AS `transDateTime`
    FROM
        (`b_adjustments`
        JOIN `m_appuser`)
    WHERE
        ((`b_adjustments`.`adjustment_date` <= NOW())
            AND (`b_adjustments`.`adjustment_type` = 'CREDIT')
            AND (`b_adjustments`.`createdby_id` = `m_appuser`.`id`)) 
    UNION ALL SELECT DISTINCT
        `b_adjustments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_adjustments`.`client_id` AS `client_id`,
        (SELECT 
                `m_code_value`.`code_value`
            FROM
                `m_code_value`
            WHERE
                ((`m_code_value`.`code_id` = 12)
                    AND (`b_adjustments`.`adjustment_code` = `m_code_value`.`id`))) AS `tran_type`,
        CAST(`b_adjustments`.`adjustment_date` AS DATE) AS `transdate`,
        'ADJUSTMENT' AS `transType`,
        (CASE `b_adjustments`.`adjustment_type`
            WHEN 'DEBIT' THEN `b_adjustments`.`adjustment_amount`
        END) AS `dr_amount`,
        0 AS `cr_amt`,
        1 AS `flag`,
        0 AS `refundFlag`,
        `b_adjustments`.`created_date` AS `transDateTime`
    FROM
        (`b_adjustments`
        JOIN `m_appuser`)
    WHERE
        ((`b_adjustments`.`adjustment_date` <= NOW())
            AND (`b_adjustments`.`adjustment_type` = 'DEBIT')
            AND (`b_adjustments`.`createdby_id` = `m_appuser`.`id`)) 
    UNION ALL SELECT DISTINCT
        `b_payments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_payments`.`client_id` AS `client_id`,
        (SELECT 
                `m_code_value`.`code_value`
            FROM
                `m_code_value`
            WHERE
                ((`m_code_value`.`code_id` = 11)
                    AND (`b_payments`.`paymode_id` = `m_code_value`.`id`))) AS `tran_type`,
        CAST(`b_payments`.`payment_date` AS DATE) AS `transDate`,
        'PAYMENT' AS `transType`,
        0 AS `dr_amt`,
        `b_payments`.`amount_paid` AS `cr_amount`,
        `b_payments`.`is_deleted` AS `flag`,
        0 AS `refundFlag`,
        `b_payments`.`created_date` AS `transDateTime`
    FROM
        (`b_payments`
        JOIN `m_appuser`)
    WHERE
        ((`b_payments`.`createdby_id` = `m_appuser`.`id`)
            AND ISNULL(`b_payments`.`ref_id`)
            AND (`b_payments`.`payment_date` <= NOW())) 
    UNION ALL SELECT DISTINCT
        `b_payments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_payments`.`client_id` AS `client_id`,
        (SELECT 
                `m_code_value`.`code_value`
            FROM
                `m_code_value`
            WHERE
                ((`m_code_value`.`code_id` = 11)
                    AND (`b_payments`.`paymode_id` = `m_code_value`.`id`))) AS `tran_type`,
        CAST(`b_payments`.`payment_date` AS DATE) AS `transDate`,
        'PAYMENT CANCELED' AS `transType`,
        ABS(`b_payments`.`amount_paid`) AS `dr_amt`,
        0 AS `cr_amount`,
        `b_payments`.`is_deleted` AS `flag`,
        1 AS `refundFlag`,
        `b_payments`.`created_date` AS `transDateTime`
    FROM
        (`b_payments`
        JOIN `m_appuser`)
    WHERE
        ((`b_payments`.`is_deleted` = 1)
            AND (`b_payments`.`ref_id` IS NOT NULL)
            AND (`b_payments`.`createdby_id` = `m_appuser`.`id`)
            AND (`b_payments`.`payment_date` <= NOW())) 
    UNION ALL SELECT DISTINCT
        `b_payments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_payments`.`client_id` AS `client_id`,
        (SELECT 
                `m_code_value`.`code_value`
            FROM
                `m_code_value`
            WHERE
                ((`m_code_value`.`code_id` = 11)
                    AND (`b_payments`.`paymode_id` = `m_code_value`.`id`))) AS `tran_type`,
        CAST(`b_payments`.`payment_date` AS DATE) AS `transDate`,
        'PAYMENT REFUND' AS `transType`,
        ABS(`b_payments`.`amount_paid`) AS `dr_amt`,
        0 AS `cr_amount`,
        `b_payments`.`is_deleted` AS `flag`,
        1 AS `refundFlag`,
        `b_payments`.`created_date` AS `transDateTime`
    FROM
        (`b_payments`
        JOIN `m_appuser`)
    WHERE
        ((`b_payments`.`is_deleted` = 0)
            AND (`b_payments`.`ref_id` IS NOT NULL)
            AND (`b_payments`.`createdby_id` = `m_appuser`.`id`)
            AND (`b_payments`.`payment_date` <= NOW())) 
    UNION ALL SELECT DISTINCT
        `bjt`.`id` AS `transId`,
        `ma`.`username` AS `username`,
        `bjt`.`client_id` AS `client_id`,
        'Event Journal' AS `tran_type`,
        CAST(`bjt`.`jv_date` AS DATE) AS `transDate`,
        'JOURNAL VOUCHER' AS `transType`,
        IFNULL(`bjt`.`debit_amount`, 0) AS `dr_amt`,
        IFNULL(`bjt`.`credit_amount`, 0) AS `cr_amount`,
        1 AS `flag`,
        0 AS `refundFlag`,
        `bjt`.`created_date` AS `transDateTime`
    FROM
        (`b_jv_transactions` `bjt`
        JOIN `m_appuser` `ma` ON (((`bjt`.`createdby_id` = `ma`.`id`)
            AND (`bjt`.`jv_date` <= NOW())))) 
    UNION ALL SELECT DISTINCT
        `bdr`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `bdr`.`client_id` AS `client_id`,
        `bdr`.`description` AS `tran_type`,
        CAST(`bdr`.`transaction_date` AS DATE) AS `transDate`,
        'DEPOSIT&REFUND' AS `transType`,
        IFNULL(`bdr`.`debit_amount`, 0) AS `dr_amt`,
        IFNULL(`bdr`.`credit_amount`, 0) AS `cr_amount`,
        IFNULL(`bdr`.`payment_id`, 0) AS `flag`,
        `bdr`.`is_refund` AS `refundFlag`,
        `bdr`.`created_date` AS `transDateTime`
    FROM
        (`b_deposit_refund` `bdr`
        JOIN `m_appuser`)
    WHERE
        ((`bdr`.`createdby_id` = `m_appuser`.`id`)
            AND (`bdr`.`transaction_date` <= NOW()))
    ORDER BY 1 , 2;












