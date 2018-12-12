SET SQL_SAFE_UPDATES=0;
CREATE 
   OR REPLACE
VIEW `vndr_revenuepack_vw` AS
    select 
        `nav`.`year_mon` AS `Month`,
        `bvm`.`id` AS `VendorId`,
        `bvm`.`vendor_name` AS `VendorName`,
        `nav`.`plan` AS `Package`,
        `nav`.`fdm` AS `startDate`,
        `nav`.`ldm` AS `endDate`,
        sum(((((`nav`.`new` + `nav`.`rec`) + `nav`.`ren`) + `nav`.`Op_Pending_add`) + `nav`.`Pending_add`)) AS `New Subscription`,
        sum(`nav`.`Op_Active`) AS `Open Subscriptions`,
        (sum(`nav`.`Del`) + sum(`nav`.`Pending_del`)) AS `Expiring Subscriptions`,
        `bad`.`content_cost` AS `Monthly Cost Price`,
        sum((if((`bvm`.`vendor_code` = 'OSN'),
            `nav`.`Cl_Active`,
            `nav`.`new`) * `bad`.`content_cost`)) AS `Monthly Cost`,
        (sum((if((`bvm`.`vendor_code` = 'OSN'),
            `nav`.`Cl_Active`,
            `nav`.`new`) * `bad`.`content_sellprice`)) - sum((if((`bvm`.`vendor_code` = 'OSN'),
            `nav`.`Cl_Active`,
            `nav`.`new`) * `bad`.`content_cost`))) AS `NetMarkup`,
        sum((if((`bvm`.`vendor_code` = 'OSN'),
            `nav`.`Cl_Active`,
            `nav`.`new`) * `bad`.`content_sellprice`)) AS `Subscription Amount`,
        truncate(((sum((if((`bvm`.`vendor_code` = 'OSN'),
                `nav`.`Cl_Active`,
                `nav`.`new`) * `bad`.`content_cost`)) * `bad`.`loyalty_share`) / 100),
            2) AS `UDC Commission`,
        truncate((sum((if((`bvm`.`vendor_code` = 'OSN'),
                `nav`.`Cl_Active`,
                `nav`.`new`) * `bad`.`content_sellprice`)) - ((sum((if((`bvm`.`vendor_code` = 'OSN'),
                `nav`.`Cl_Active`,
                `nav`.`new`) * `bad`.`content_sellprice`)) - sum((if((`bvm`.`vendor_code` = 'OSN'),
                `nav`.`Cl_Active`,
                `nav`.`new`) * `bad`.`content_cost`))) + ((sum((if((`bvm`.`vendor_code` = 'OSN'),
                `nav`.`Cl_Active`,
                `nav`.`new`) * `bad`.`content_cost`)) * `bad`.`loyalty_share`) / 100))),
            2) AS `Net Payable Amount`,
        truncate(((sum((if((`bvm`.`vendor_code` = 'OSN'),
                `nav`.`Cl_Active`,
                `nav`.`new`) * `bad`.`content_sellprice`)) - sum((if((`bvm`.`vendor_code` = 'OSN'),
                `nav`.`Cl_Active`,
                `nav`.`new`) * `bad`.`content_cost`))) + ((sum((if((`bvm`.`vendor_code` = 'OSN'),
                `nav`.`Cl_Active`,
                `nav`.`new`) * `bad`.`content_cost`)) * `bad`.`loyalty_share`) / 100)),
            2) AS `UDC Profit`
    from
        ((((`net_activedtls_vw` `nav`
        join `b_vendor_agmt_detail` `bad` ON (((`nav`.`plan_id` = `bad`.`content_code`)
            and (`bad`.`is_deleted` = 'N')
            and (`bad`.`content_cost` is not null))))
        join `b_order_price` `bop` ON ((`nav`.`order_id` = `bop`.`order_id`)))
        join `b_vendor_agreement` `bva` ON (((`bva`.`id` = `bad`.`vendor_agmt_id`)
            -- and (`bva`.`vendor_agmt_startdate` between `nav`.`fdm` and `nav`.`ldm`)
)))
        join `b_vendor_management` `bvm` ON ((`bva`.`vendor_id` = `bvm`.`id`)))
    group by `nav`.`year_mon` , `nav`.`plan_id` , `nav`.`plan` , `bvm`.`vendor_code`
    having ((`New Subscription` > 0)
        or (`Open Subscriptions` > 0)
        or (`Expiring Subscriptions` > 0))
    order by `nav`.`month_number`;


INSERT IGNORE INTO stretchy_parameter VALUES(null,'vendorSelectAll', 'vendorId', 'Vendor Name', 'select', 'number', '-1', NULL, NULL, 'Y', 'select id, vendor_name as vendorName from b_vendor_management where is_deleted=''N''', NULL, 'Report');
INSERT IGNORE INTO stretchy_report VALUES(null,'Vendor Packagewise Revenue', 'Table', NULL, 'Client', 'SELECT `Package`, `New Subscription`,`Open Subscriptions`,`Expiring Subscriptions` ,
`Monthly Cost Price`,`Monthly Cost`, `NetMarkup`,`Subscription Amount`,`UDC Commission`,`Net Payable Amount`,`UDC Profit`
FROM vndr_revenuepack_vw vndr WHERE (vndr.VendorId = ''${vendorId}'' or -1 = ''${vendorId}'') AND  
vndr.activeDate between ''${startDate}'' and ''${endDate}'''
, 'Vendor Packagewise Revenue', '0', '1');

SET @ID=(SELECT id FROM stretchy_report where report_name='Vendor Packagewise Revenue');
SET @startDate=(SELECT id FROM stretchy_parameter where parameter_label='startDate');
SET @endDate=(SELECT id FROM stretchy_parameter where parameter_label='endDate');
SET @vendor=(SELECT id FROM stretchy_parameter where parameter_label='Vendor Name');
INSERT IGNORE INTO stretchy_report_parameter(report_id,parameter_id,report_parameter_name)VALUES (@ID,@startDate,'startDate');
INSERT IGNORE INTO stretchy_report_parameter(report_id,parameter_id,report_parameter_name)VALUES (@ID,@endDate,'endDate');
INSERT IGNORE INTO stretchy_report_parameter(report_id,parameter_id,report_parameter_name)VALUES (@ID,@vendor,'Vendor Name');

-- Monthly Net Activations --
DROP TABLE IF EXISTS `v_netact_dtls`;
CREATE 
OR REPLACE
VIEW `v_netact_dtls` AS
    select 
        `dt`.`year4` AS `year4`,
        `dt`.`year_month_abbreviation` AS `year_mon`,
        `dt`.`month_number` AS `month_number`,
        `fdm`.`date_value` AS `fdm`,
        `ldm`.`date_value` AS `ldm`,
        `o`.`client_id` AS `client_id`,
        `o`.`id` AS `order_id`,
        `o`.`plan_id` AS `plan_id`,
        `pm`.`plan_description` AS `plan`,
        `t`.`actual_date` AS `actual_date`,
        `t`.`transaction_type` AS `transaction_type`,
        (case
            when
                (`op`.`transaction_type` in ('ACTIVATION' , 'RECONNECTION',
                    'RENEWAL',
                    'Renewal',
                    'RENEWAL BEFORE AUTOEXIPIRY',
                    'RENEWAL AFTER AUTOEXIPIRY',
                    'CHANGE_PLAN'))
            then
                1
            else 0
        end) AS `Op_Active`,
        if((`t`.`transaction_type` = 'ACTIVATION'),
            1,
            0) AS `new`,
        if(((`t`.`transaction_type` = 'RECONNECTION')
                and (`op`.`transaction_type` = 'DISCONNECTION')),
            1,
            0) AS `rec`,
        if((`t`.`transaction_type` = 'CHANGE_PLAN'),
            1,
            0) AS `chg`,
        if((`t`.`transaction_type` in ('Renewal' , 'RENEWAL', 'RENEWAL AFTER AUTOEXIPIRY')),
            1,
            0) AS `ren`,
        if(((`dt`.`month_number` = month(`o`.`active_date`))
                and (`o`.`order_status` = 4)
                and (`t`.`transaction_type` in ('ACTIVATION' , 'RECONNECTION',
                'RENEWAL',
                'Renewal',
                'RENEWAL BEFORE AUTOEXIPIRY',
                'RENEWAL AFTER AUTOEXIPIRY',
                'CHANGE_PLAN'))),
            1,
            0) AS `Pending_add`,
        if(((`dt`.`month_number` >= month(`o`.`active_date`))
                and (`o`.`order_status` = 4)
                and (`op`.`transaction_type` in ('ACTIVATION' , 'RECONNECTION',
                'RENEWAL',
                'Renewal',
                'RENEWAL BEFORE AUTOEXIPIRY',
                'RENEWAL AFTER AUTOEXIPIRY',
                'CHANGE_PLAN'))),
            1,
            0) AS `Op_Pending_add`,
        if(((`t`.`transaction_type` = 'DISCONNECTION')
                and (`op`.`transaction_type` in ('ACTIVATION' , 'RECONNECTION',
                'RENEWAL',
                'Renewal',
                'RENEWAL BEFORE AUTOEXIPIRY',
                'RENEWAL AFTER AUTOEXIPIRY',
                'CHANGE_PLAN'))),
            1,
            0) AS `Del`,
        if(((`dt`.`month_number` = month(`o`.`active_date`))
                and (`o`.`order_status` = 4)
                and (`t`.`transaction_type` = 'DISCONNECTION')),
            1,
            0) AS `Pending_del`,
        if(((`dt`.`month_number` >= month(`o`.`active_date`))
                and (`o`.`order_status` = 4)),
            1,
            0) AS `Cum_Pending`,
        (case
            when
                ((`cl`.`transaction_type` in ('ACTIVATION' , 'RECONNECTION',
                    'RENEWAL',
                    'Renewal',
                    'RENEWAL BEFORE AUTOEXIPIRY',
                    'RENEWAL AFTER AUTOEXIPIRY',
                    'CHANGE_PLAN'))
                    and if((`dt`.`month_number` = month(now())),
                    (`o`.`order_status` = 1),
                    1))
            then
                1
            else 0
        end) AS `Cl_Active`
    from
        ((((((((`m_client` `c`
        join `dim_date` `dt` ON (((`dt`.`month_number` <= month(now()))
            and (`dt`.`is_first_day_in_month` = 'Yes'))))
        join `dim_date` `fdm` ON (((`fdm`.`year_month_number` = `dt`.`year_month_number`)
            and (`fdm`.`is_first_day_in_month` = 'Yes'))))
        join `dim_date` `ldm` ON (((`fdm`.`year_month_number` = `ldm`.`year_month_number`)
            and (`ldm`.`is_last_day_in_month` = 'Yes'))))
        join `b_orders` `o` ON ((`c`.`id` = `o`.`client_id`)))
        join `b_plan_master` `pm` ON ((`o`.`plan_id` = `pm`.`id`)))
        left join `b_orders_history` `op` ON (((`o`.`id` = `op`.`order_id`)
            and (`op`.`id` = (select 
                max(`h3`.`id`)
            from
                `b_orders_history` `h3`
            where
                ((`h3`.`order_id` = `o`.`id`)
                    and (`h3`.`transaction_type` in ('ACTIVATION' , 'RECONNECTION', 'RENEWAL', 'DISCONNECTION', 'Renewal', 'RENEWAL BEFORE AUTOEXIPIRY', 'RENEWAL AFTER AUTOEXIPIRY', 'CHANGE_PLAN'))
                    and (cast(`h3`.`actual_date` as date) < `fdm`.`date_value`)))))))
        left join `b_orders_history` `t` ON (((`o`.`id` = `t`.`order_id`)
            and (`t`.`id` = (select 
                max(`h4`.`id`)
            from
                `b_orders_history` `h4`
            where
                ((`h4`.`order_id` = `o`.`id`)
                    and (`h4`.`transaction_type` in ('ACTIVATION' , 'RECONNECTION', 'RENEWAL', 'DISCONNECTION', 'Renewal', 'RENEWAL BEFORE AUTOEXIPIRY', 'RENEWAL AFTER AUTOEXIPIRY', 'CHANGE_PLAN'))
                    and (cast(`h4`.`actual_date` as date) between `fdm`.`date_value` and `ldm`.`date_value`)))))))
        left join `b_orders_history` `cl` ON (((`o`.`id` = `cl`.`order_id`)
            and (`cl`.`id` = (select 
                max(`h5`.`id`)
            from
                `b_orders_history` `h5`
            where
                ((`h5`.`order_id` = `o`.`id`)
                    and (`h5`.`transaction_type` in ('ACTIVATION' , 'RECONNECTION', 'RENEWAL', 'DISCONNECTION', 'Renewal', 'RENEWAL BEFORE AUTOEXIPIRY', 'RENEWAL AFTER AUTOEXIPIRY', 'CHANGE_PLAN'))
                    and (cast(`h5`.`actual_date` as date) <= `ldm`.`date_value`)))))))
    where
        (`dt`.`year4` = year(now()));

-- --
DROP TABLE IF EXISTS `v_netact_summary`;
CREATE 
OR REPLACE
VIEW `v_netact_summary` AS
    select 
        `v_netact_dtls`.`year_mon` AS `Month`,
        sum(`v_netact_dtls`.`Op_Active`) AS `OpenBal`,
        sum(`v_netact_dtls`.`new`) AS `New`,
        sum(`v_netact_dtls`.`rec`) AS `Reconn`,
        sum(`v_netact_dtls`.`ren`) AS `Renewal`,
        (sum(`v_netact_dtls`.`Op_Pending_add`) + sum(`v_netact_dtls`.`Pending_add`)) AS `Pending_add`,
        sum(((((`v_netact_dtls`.`new` + `v_netact_dtls`.`rec`) + `v_netact_dtls`.`ren`) + `v_netact_dtls`.`Op_Pending_add`) + `v_netact_dtls`.`Pending_add`)) AS `NetAdditions`,
        sum(((((`v_netact_dtls`.`Op_Active` + `v_netact_dtls`.`new`) + `v_netact_dtls`.`rec`) + `v_netact_dtls`.`Op_Pending_add`) + `v_netact_dtls`.`Pending_add`)) AS `Total`,
        sum(`v_netact_dtls`.`Del`) AS `Deletions`,
        sum(`v_netact_dtls`.`Pending_del`) AS `Pending_del`,
        sum(`v_netact_dtls`.`Cum_Pending`) AS `Cum_Pending`,
        (sum(`v_netact_dtls`.`Del`) + sum(`v_netact_dtls`.`Pending_del`)) AS `NetSub`,
        sum(((((((`v_netact_dtls`.`Op_Active` + `v_netact_dtls`.`new`) + `v_netact_dtls`.`rec`) + `v_netact_dtls`.`Op_Pending_add`) + `v_netact_dtls`.`Pending_add`) - `v_netact_dtls`.`Del`) - `v_netact_dtls`.`Pending_del`)) AS `NetBal`,
        sum(`v_netact_dtls`.`Cl_Active`) AS `ClosingBal`
    from
        `v_netact_dtls`
    group by `v_netact_dtls`.`year_mon`
    order by `v_netact_dtls`.`month_number`;

-- Monthly planwise Activations --
DROP TABLE IF EXISTS `v_netactpln_summary`;
CREATE 
   OR REPLACE
VIEW `v_netactpln_summary` AS
    select 
        `net_activedtls_vw`.`year_mon` AS `Month`,
        `net_activedtls_vw`.`plan` AS `Plan`,
        `net_activedtls_vw`.`plan_id` AS `PlanId`,
        sum(`net_activedtls_vw`.`Op_Active`) AS `OpenBal`,
        sum(`net_activedtls_vw`.`new`) AS `New`,
        sum(`net_activedtls_vw`.`rec`) AS `Reconnect`,
        sum(`net_activedtls_vw`.`ren`) AS `Renewals`,
        sum(`net_activedtls_vw`.`Op_Pending_add`) AS `Open_Pending_add`,
        sum(`net_activedtls_vw`.`Pending_add`) AS `Pending_add`,
        sum(((`net_activedtls_vw`.`new` + `net_activedtls_vw`.`rec`) + `net_activedtls_vw`.`ren`)) AS `NetAdditions`,
        sum(((`net_activedtls_vw`.`Op_Active` + `net_activedtls_vw`.`new`) + `net_activedtls_vw`.`rec`)) AS `Total`,
        sum(`net_activedtls_vw`.`Del`) AS `Deletions`,
        sum(`net_activedtls_vw`.`Pending_del`) AS `Pending_del`,
        sum(`net_activedtls_vw`.`Cum_Pending`) AS `Cum_Pending`,
        sum(`net_activedtls_vw`.`Del`) AS `NetSub`,
        sum((((((`net_activedtls_vw`.`Op_Active` + `net_activedtls_vw`.`new`) + `net_activedtls_vw`.`rec`) + `net_activedtls_vw`.`Op_Pending_add`) + `net_activedtls_vw`.`Pending_add`) - `net_activedtls_vw`.`Del`)) AS `NetBal`,
        sum(`net_activedtls_vw`.`Cl_Active`) AS `ClosingBal`
    from
        `net_activedtls_vw`
    group by `net_activedtls_vw`.`year_mon` , `net_activedtls_vw`.`plan`
    having ((`OpenBal` > 0) or (`New` > 0)
        or (`Reconnect` > 0)
        or (`Open_Pending_add` > 0)
        or (`Pending_add` > 0)
        or (`Deletions` > 0)
        or (`Pending_del` > 0))
    order by `net_activedtls_vw`.`month_number` , `net_activedtls_vw`.`plan_id`;

SET @ID=(SELECT id FROM stretchy_report where report_name='Monthly Planwise Net Activations');
SET @PLAN=(select id from stretchy_parameter where parameter_label='Plan Name');

UPDATE stretchy_report SET report_sql='select * from v_netactpln_summary where 
`planId` = ''${planId}'' or -1 = ''${planId}'''
WHERE id=@ID or  report_name='Monthly Planwise Net Activations';

INSERT IGNORE INTO stretchy_report_parameter VALUES(NULL, @ID, @PLAN, 'Plan Name');

-- FUNCTIONS --


DROP function IF EXISTS `fbal`;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `fbal`(cid int, dt date) RETURNS double(10,2)
BEGIN

declare v_bal double(10,2) Default 0 ;
declare i_bal decimal(10,2) Default 0 ;
declare p_bal decimal(10,2) Default 0 ; 

select ifnull(truncate(sum( `bicl`.`invoice_amount`),2),0) into i_bal from `b_invoice` `bicl` 
where ((`bicl`.`invoice_date` <=  dt)
and (`bicl`.`client_id` = cid))  ;

select ifnull(truncate(sum(`bpcl`.`amount_paid`),2),0) into p_bal from `b_payments` `bpcl` 
where ( (`bpcl`.`is_deleted`=0) and (`bpcl`.`client_id` = cid)
and (`bpcl`.`payment_date` <= dt));

SET v_bal = i_bal-p_bal;

return v_bal;
END$$

DELIMITER ;

DROP function IF EXISTS `fopbal`;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` FUNCTION `fopbal`(cid int, dt date) RETURNS double(10,2)
BEGIN

declare v_bal double(10,2) Default 0 ;
declare i_bal decimal(10,2) Default 0 ;
declare p_bal decimal(10,2) Default 0 ; 

select ifnull(truncate(sum( `bicl`.`invoice_amount`),2),0) into i_bal from `b_invoice` `bicl` 
where ((`bicl`.`invoice_date` <  dt)
and (`bicl`.`client_id` = cid))  ;

select ifnull(truncate(sum(`bpcl`.`amount_paid`),2),0) into p_bal from `b_payments` `bpcl` 
where ( (`bpcl`.`is_deleted`=0) and (`bpcl`.`client_id` = cid)
and (`bpcl`.`payment_date` < dt));

SET v_bal = i_bal-p_bal;

return v_bal;
END$$

DELIMITER ;

 -- REVENUE REPORT'S --
CREATE 
     OR REPLACE 
VIEW `netrevenuedtls_vw` AS
    select 
        `c`.`id` AS `client_id`,
        `dt`.`year4` AS `year`,
        `dt`.`month_number` AS `mon`,
        `dt`.`year_month_abbreviation` AS `year_mon`,
        `dt`.`date_value` AS `date`,
        cast(fopbal(`c`.`id`, `dt`.`date_value`) as char charset utf8) AS `opbal`,
        `bi`.`id` AS `inv_id`,
        date_format(`bi`.`invoice_date`, '%d-%m-%Y') AS `invoice_date`,
        cast(sum(truncate(`bi`.`invoice_amount`, 2)) as char charset utf8) AS `inv_amt`,
        `bp`.`id` AS `pmt_id`,
        date_format(`bp`.`payment_date`, '%d-%m-%Y') AS `payment_date`,
        sum(truncate(ifnull(`bp`.`amount_paid`, 0),
            2)) AS `pmt_amt`,
        `ldm`.`date_value` AS `month_end`,
        cast(fbal(`c`.`id`, `dt`.`date_value`) as char charset utf8) AS `clbal`,
        truncate(`cb`.`balance_amount`, 2) AS `current_balance`
    from
        (((((`m_client` `c`
        join `dim_date` `dt` ON (((`dt`.`year4` = year(now()))
            and (`dt`.`month_number` <= month(now())))))
        join `dim_date` `ldm` ON (((`dt`.`year_month_number` = `ldm`.`year_month_number`)
            and (`ldm`.`is_last_day_in_month` = 'Yes'))))
        join `b_client_balance` `cb` ON ((`c`.`id` = `cb`.`client_id`)))
        left join `b_invoice` `bi` ON (((`c`.`id` = `bi`.`client_id`)
            and (`bi`.`invoice_date` = `dt`.`date_value`))))
        left join `b_payments` `bp` ON (((`c`.`id` = `bp`.`client_id`)
            and (`bp`.`payment_date` = `dt`.`date_value`)
            and (`bp`.`is_deleted` = 0)
            and (`dt`.`year_month_number` = convert( concat(year(`bp`.`payment_date`), '-', month(`bp`.`payment_date`)) using utf8)))))
    where
        ((`bi`.`id` is not null)
            or (`bp`.`id` is not null))
    group by `bi`.`client_id` , `dt`.`date_value` , `bi`.`id` , `bp`.`id`
    order by `c`.`id` , `dt`.`year4` , `dt`.`date_value`;


INSERT IGNORE INTO stretchy_report VALUES(null,'Customer Revenue Details', 'Table', NULL, 'Client', 'SELECT 
* FROM netrevenuedtls_vw nrv WHERE  
nrv.date between ''${startDate}'' and ''${endDate}'''
, 'Customer Revenue Details', '0', '1');

SET @ID=(SELECT id FROM stretchy_report where report_name='Customer Revenue Details');
SET @startDate=(SELECT id FROM stretchy_parameter where parameter_label='startDate');
SET @endDate=(SELECT id FROM stretchy_parameter where parameter_label='endDate');
INSERT IGNORE INTO stretchy_report_parameter(report_id,parameter_id,report_parameter_name)VALUES (@ID,@startDate,'startDate');
INSERT IGNORE INTO stretchy_report_parameter(report_id,parameter_id,report_parameter_name)VALUES (@ID,@endDate,'endDate');




