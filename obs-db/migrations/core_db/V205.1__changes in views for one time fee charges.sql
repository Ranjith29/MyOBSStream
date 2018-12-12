SET SQL_SAFE_UPDATES=0;

DROP PROCEDURE  IF EXISTS chargeTypes;
DELIMITER //
CREATE PROCEDURE chargeTypes() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_charge'
     AND COLUMN_NAME ='transaction_type') THEN
ALTER TABLE b_charge ADD COLUMN `transaction_type` VARCHAR(300) NOT NULL AFTER `charge_end_date`;
END IF;
END //
DELIMITER ;
call chargeTypes();
DROP PROCEDURE  IF EXISTS chargeTypes;

-- fin_trans_vw --
CREATE OR REPLACE
VIEW `fin_trans_vw` AS
    select distinct
        `b_invoice`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_invoice`.`client_id` AS `client_id`,
        if((`b_charge`.`charge_type` = 'NRC'),
            'Once',
            'Periodic') AS `tran_type`,
        cast(`b_invoice`.`invoice_date` as date) AS `transDate`,
        (case
            when (`b_charge`.`transaction_type` = 'SERVICE_TRANSFER') then 'SERVICE TRANSFER'
            when (`b_charge`.`transaction_type` = 'REGISTRATION_FEE') then 'REGISTRATION CHARGE'
            when (`b_charge`.`transaction_type` = 'TERMINATION_FEE') then 'TERMINATION CHARGE'
            when (`b_charge`.`transaction_type` = 'RECONNECTION FEE') then 'RECONNECTION CHARGE'
            when (`b_charge`.`transaction_type` = 'REACTIVATION_FEE') then 'REACTIVATION CHARGE'
            else 'INVOICE'
        end) AS `transType`,
        if((`b_invoice`.`invoice_amount` > 0),
            `b_invoice`.`invoice_amount`,
            0) AS `dr_amt`,
        if((`b_invoice`.`invoice_amount` < 0),
            abs(`b_invoice`.`invoice_amount`),
            0) AS `cr_amt`,
        1 AS `flag`,
        0 AS `refundFlag`
    from
        ((`b_invoice`
        join `m_appuser`)
        join `b_charge`)
    where
        ((`b_invoice`.`createdby_id` = `m_appuser`.`id`)
            and (`b_invoice`.`id` = `b_charge`.`invoice_id`)
            and (`b_invoice`.`invoice_date` <= now()))
    group by `b_invoice`.`id` 
    union all select distinct
        `b_adjustments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_adjustments`.`client_id` AS `client_id`,
        (select 
                `m_code_value`.`code_value`
            from
                `m_code_value`
            where
                ((`m_code_value`.`code_id` = 12)
                    and (`b_adjustments`.`adjustment_code` = `m_code_value`.`id`))) AS `tran_type`,
        cast(`b_adjustments`.`adjustment_date` as date) AS `transdate`,
        'ADJUSTMENT' AS `transType`,
        0 AS `dr_amt`,
        (case `b_adjustments`.`adjustment_type`
            when 'CREDIT' then `b_adjustments`.`adjustment_amount`
        end) AS `cr_amount`,
        1 AS `flag`,
        0 AS `refundFlag`
    from
        (`b_adjustments`
        join `m_appuser`)
    where
        ((`b_adjustments`.`adjustment_date` <= now())
            and (`b_adjustments`.`adjustment_type` = 'CREDIT')
            and (`b_adjustments`.`createdby_id` = `m_appuser`.`id`)) 
    union all select distinct
        `b_adjustments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_adjustments`.`client_id` AS `client_id`,
        (select 
                `m_code_value`.`code_value`
            from
                `m_code_value`
            where
                ((`m_code_value`.`code_id` = 12)
                    and (`b_adjustments`.`adjustment_code` = `m_code_value`.`id`))) AS `tran_type`,
        cast(`b_adjustments`.`adjustment_date` as date) AS `transdate`,
        'ADJUSTMENT' AS `transType`,
        (case `b_adjustments`.`adjustment_type`
            when 'DEBIT' then `b_adjustments`.`adjustment_amount`
        end) AS `dr_amount`,
        0 AS `cr_amt`,
        1 AS `flag`,
        0 AS `refundFlag`
    from
        (`b_adjustments`
        join `m_appuser`)
    where
        ((`b_adjustments`.`adjustment_date` <= now())
            and (`b_adjustments`.`adjustment_type` = 'DEBIT')
            and (`b_adjustments`.`createdby_id` = `m_appuser`.`id`)) 
    union all select distinct
        `b_payments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_payments`.`client_id` AS `client_id`,
        (select 
                `m_code_value`.`code_value`
            from
                `m_code_value`
            where
                ((`m_code_value`.`code_id` = 11)
                    and (`b_payments`.`paymode_id` = `m_code_value`.`id`))) AS `tran_type`,
        cast(`b_payments`.`payment_date` as date) AS `transDate`,
        'PAYMENT' AS `transType`,
        0 AS `dr_amt`,
        `b_payments`.`amount_paid` AS `cr_amount`,
        `b_payments`.`is_deleted` AS `flag`,
        0 AS `refundFlag`
    from
        (`b_payments`
        join `m_appuser`)
    where
        ((`b_payments`.`createdby_id` = `m_appuser`.`id`)
            and isnull(`b_payments`.`ref_id`)
            and (`b_payments`.`payment_date` <= now())) 
    union all select distinct
        `b_payments`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `b_payments`.`client_id` AS `client_id`,
        (select 
                `m_code_value`.`code_value`
            from
                `m_code_value`
            where
                ((`m_code_value`.`code_id` = 11)
                    and (`b_payments`.`paymode_id` = `m_code_value`.`id`))) AS `tran_type`,
        cast(`b_payments`.`payment_date` as date) AS `transDate`,
        'PAYMENT CANCELED' AS `transType`,
        abs(`b_payments`.`amount_paid`) AS `dr_amt`,
        0 AS `cr_amount`,
        `b_payments`.`is_deleted` AS `flag`,
        1 AS `refundFlag`
    from
        (`b_payments`
        join `m_appuser`)
    where
        ((`b_payments`.`is_deleted` = 1)
            and (`b_payments`.`ref_id` is not null)
            and (`b_payments`.`createdby_id` = `m_appuser`.`id`)
            and (`b_payments`.`payment_date` <= now())) 
    union all select distinct
        `bjt`.`id` AS `transId`,
        `ma`.`username` AS `username`,
        `bjt`.`client_id` AS `client_id`,
        'Event Journal' AS `tran_type`,
        cast(`bjt`.`jv_date` as date) AS `transDate`,
        'JOURNAL VOUCHER' AS `transType`,
        ifnull(`bjt`.`debit_amount`, 0) AS `dr_amt`,
        ifnull(`bjt`.`credit_amount`, 0) AS `cr_amount`,
        1 AS `flag`,
        0 AS `refundFlag`
    from
        (`b_jv_transactions` `bjt`
        join `m_appuser` `ma` ON (((`bjt`.`createdby_id` = `ma`.`id`)
            and (`bjt`.`jv_date` <= now())))) 
    union all select distinct
        `bdr`.`id` AS `transId`,
        `m_appuser`.`username` AS `username`,
        `bdr`.`client_id` AS `client_id`,
        `bdr`.`description` AS `tran_type`,
        cast(`bdr`.`transaction_date` as date) AS `transDate`,
        'DEPOSIT&REFUND' AS `transType`,
        ifnull(`bdr`.`debit_amount`, 0) AS `dr_amt`,
        ifnull(`bdr`.`credit_amount`, 0) AS `cr_amount`,
        ifnull(`bdr`.`payment_id`, 0) AS `flag`,
        `bdr`.`is_refund` AS `refundFlag`
    from
        (`b_deposit_refund` `bdr`
        join `m_appuser`)
    where
        ((`bdr`.`createdby_id` = `m_appuser`.`id`)
            and (`bdr`.`transaction_date` <= now()))
    order by 1 , 2;

-- billdetails_view ---

CREATE OR REPLACE
VIEW `billdetails_v` AS
    select 
        `b`.`client_id` AS `client_id`,
        `a`.`id` AS `transId`,
        `b`.`invoice_date` AS `transDate`,
        'SERVICE_CHARGES' AS `transType`,
        `a`.`netcharge_amount` AS `amount`,
        concat(date_format(`a`.`charge_start_date`, '%Y-%m-%d'),
                ' to ',
                date_format(`a`.`charge_end_date`, '%Y-%m-%d')) AS `description`,
        `c`.`plan_id` AS `plan_code`,
        `a`.`charge_type` AS `service_code`
    from
        ((`b_charge` `a`
        join `b_invoice` `b`)
        join `b_orders` `c`)
    where
        ((`a`.`invoice_id` = `b`.`id`)
            and (`a`.`order_id` = `c`.`id`)
            and isnull(`a`.`bill_id`)
            and (`b`.`invoice_date` <= now())
            and (`a`.`priceline_id` >= 1)) 
    union all select 
        `b`.`client_id` AS `client_id`,
        `a`.`id` AS `transId`,
        date_format(`b`.`invoice_date`, '%Y-%m-%d') AS `transDate`,
        'TAXES' AS `transType`,
        `a`.`tax_amount` AS `amount`,
        `a`.`tax_code` AS `description`,
        NULL AS `plan_code`,
        `c`.`charge_type` AS `service_code`
    from
        ((`b_charge_tax` `a`
        join `b_invoice` `b`)
        join `b_charge` `c`)
    where
        ((`a`.`invoice_id` = `b`.`id`)
            and (`a`.`charge_id` = `c`.`id`)
            and isnull(`a`.`bill_id`)
            and (`b`.`invoice_date` <= now())) 
    union all select 
        `b_adjustments`.`client_id` AS `client_id`,
        `b_adjustments`.`id` AS `transId`,
        date_format(`b_adjustments`.`adjustment_date`,
                '%Y-%m-%d') AS `transDate`,
        'ADJUSTMENT' AS `transType`,
        (case `b_adjustments`.`adjustment_type`
            when 'DEBIT' then -(`b_adjustments`.`adjustment_amount`)
            when 'CREDIT' then `b_adjustments`.`adjustment_amount`
        end) AS `amount`,
        `b_adjustments`.`remarks` AS `remarks`,
        `b_adjustments`.`adjustment_type` AS `adjustment_type`,
        NULL AS `service_code`
    from
        `b_adjustments`
    where
        (isnull(`b_adjustments`.`bill_id`)
            and (`b_adjustments`.`adjustment_date` <= now())) 
    union all select 
        `pa`.`client_id` AS `client_id`,
        `pa`.`id` AS `transId`,
        date_format(`pa`.`payment_date`, '%Y-%m-%d') AS `transDate`,
        concat('PAYMENT', ' - ', `p`.`code_value`) AS `transType`,
        `pa`.`amount_paid` AS `invoiceAmount`,
        `pa`.`Remarks` AS `remarks`,
        `p`.`code_value` AS `code_value`,
        NULL AS `service_code`
    from
        (`b_payments` `pa`
        join `m_code_value` `p`)
    where
        (isnull(`pa`.`bill_id`)
            and (`pa`.`payment_date` <= now())
            and (`pa`.`paymode_id` = `p`.`id`)) 
    union all select 
        `bdr`.`client_id` AS `client_id`,
        `bdr`.`id` AS `transId`,
        date_format(`bdr`.`transaction_date`, '%Y-%m-%d') AS `transDate`,
        'DEPOSIT&REFUND' AS `transType`,
        (case
            when (`bdr`.`description` in ('Deposit' , 'Payment Towards Refund Entry')) then `bdr`.`debit_amount`
            when (`bdr`.`description` in ('Refund' , 'Refund Adjustment towards Service Balance')) then -(`bdr`.`credit_amount`)
        end) AS `amount`,
        `bdr`.`description` AS `remarks`,
        `bdr`.`transaction_type` AS `transaction`,
        NULL AS `service_code`
    from
        `b_deposit_refund` `bdr`
    where
        ((`bdr`.`transaction_date` <= now())
            and isnull(`bdr`.`bill_id`)) 
    union all select 
        `b`.`client_id` AS `client_id`,
        `a`.`id` AS `transId`,
        date_format(`c`.`sale_date`, '%Y-%m-%d') AS `transDate`,
        'ONETIME_CHARGES' AS `transType`,
        `a`.`netcharge_amount` AS `amount`,
        `c`.`charge_code` AS `charge_code`,
        `c`.`item_id` AS `item_id`,
        `a`.`charge_type` AS `service_code`
    from
        ((`b_charge` `a`
        join `b_invoice` `b`)
        join `b_onetime_sale` `c`)
    where
        ((`a`.`invoice_id` = `b`.`id`)
            and (`a`.`order_id` = `c`.`id`)
            and isnull(`a`.`bill_id`)
            and (`c`.`sale_date` <= now())
            and (`c`.`invoice_id` = `b`.`id`)
            and (`a`.`priceline_id` = 0)) 
    union all select 
        `b`.`client_id` AS `client_id`,
        `a`.`id` AS `transId`,
        `b`.`invoice_date` AS `transDate`,
        `a`.`transaction_type` AS `transType`,
        `a`.`netcharge_amount` AS `amount`,
        concat(date_format(`a`.`charge_start_date`, '%Y-%m-%d'),
                ' to ',
                date_format(`a`.`charge_end_date`, '%Y-%m-%d')) AS `description`,
        NULL AS `plan_code`,
        `a`.`charge_type` AS `service_code`
    from
        (`b_charge` `a`
        join `b_invoice` `b`)
    where
        ((`a`.`invoice_id` = `b`.`id`)
            and isnull(`a`.`bill_id`)
            and (`b`.`invoice_date` <= now())
            and (`a`.`priceline_id` < 0));

UPDATE b_charge SET transaction_type='INVOICE' WHERE priceline_id=0 and charge_type='NRC'; 
UPDATE b_charge SET transaction_type='INVOICE' WHERE priceline_id>0 and charge_type='RC'; 
UPDATE b_charge SET transaction_type='INVOICE' WHERE priceline_id>0 and charge_type='UC'; 
UPDATE b_charge SET transaction_type='INVOICE' WHERE priceline_id>0 and charge_type='DC'; 
UPDATE b_charge SET transaction_type='SERVICE_TRANSFER' WHERE priceline_id=-1 and charge_type='NRC'; 
UPDATE b_charge SET transaction_type='REGISTRATION_FEE' WHERE priceline_id=-2 and charge_type='NRC';	
UPDATE b_charge SET transaction_type='TERMINATION_FEE' WHERE priceline_id=-3 and charge_type='NRC';
UPDATE b_charge SET transaction_type='REACTIVATION_FEE' WHERE priceline_id=-4 and charge_type='NRC';
UPDATE b_charge SET transaction_type='RECONNECTION_FEE' WHERE priceline_id > 0 and charge_type='NRC';
UPDATE b_charge SET priceline_id= -1 WHERE priceline_id < 0;
UPDATE b_charge SET priceline_id= -1 WHERE transaction_type='RECONNECTION_FEE';


UPDATE stretchy_report SET report_sql = 'SELECT
              clnt.account_no as `CUST.NO`,
			  cast(clnt.display_name as char charset utf8) as `CUST.NAME`,   
              DATE_FORMAT(inv.invoice_date, ''%d-%m-%Y'') as `INVOICE DATE`,
			 (CASE WHEN charge.priceline_id = 0 AND charge.charge_type = ''NRC'' THEN  btm.item_description 
			  WHEN charge.transaction_type=''SERVICE_TRANSFER'' THEN ''Change of address fee''
              WHEN charge.transaction_type=''REGISTRATION_FEE'' THEN ''Registration fee''
              WHEN charge.transaction_type=''TERMINATION_FEE'' THEN ''Termination fee''
              WHEN charge.transaction_type=''REACTIVATION_FEE'' THEN ''Reactivation fee''
              WHEN charge.transaction_type=''RECONNECTION_FEE'' THEN ''Reconnection fee''
			  ELSE pm.plan_description END) as ''PLAN/ITEM'',
              charge.charge_type as `CHARGE TYPE`,      
              cast(TRUNCATE(sum(charge.charge_amount),2) as char charset utf8) as `CHARGE AMOUNT`,
              cast(TRUNCATE(sum(charge.discount_amount),2) as char charset utf8) as `DISCOUNT AMOUNT`,
			  cast(TRUNCATE(sum(ctx.tax_amount),2) as char charset utf8) as `TAX AMOUNT`,
              cast(inv.invoice_amount as char charset utf8) as `INVOICE AMOUNT`
            
FROM 
      m_office off
       JOIN
      m_client clnt ON off.id = clnt.office_id
       JOIN
      b_invoice inv  ON clnt.id = inv.client_id
       JOIN
      b_charge charge ON inv.id = charge.invoice_id AND charge.client_id = inv.client_id
       LEFT JOIN
      b_charge_tax ctx ON charge.invoice_id = ctx.invoice_id
      LEFT JOIN 
       b_onetime_sale bos  ON charge.order_id = bos.id
	  LEFT JOIN 
      b_item_master btm  ON btm.id = bos.item_id
      LEFT JOIN 
      b_orders o ON o.id=charge.order_id and charge.client_id=clnt.id
      LEFT JOIN 
      b_plan_master pm ON  o.plan_id = pm.id 
      WHERE (off.id = ''${officeId}'' OR -1 = ''${officeId}'')  AND inv.invoice_date  BETWEEN ''${startDate}'' AND ''${endDate}''
      GROUP BY charge.invoice_id,inv.invoice_date,ctx.invoice_id order by clnt.id,inv.invoice_date' 
      WHERE report_name='Invoice Date Wise Details';




