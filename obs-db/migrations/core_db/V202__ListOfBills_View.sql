CREATE OR REPLACE
VIEW `bill_list_vw` AS
    select 
        `bm`.`id` AS `BILL ID`,
        date_format(`bm`.`Bill_date`, '%d-%m-%Y') AS `BILL DATE`,
        `c`.`account_no` AS `ACCOUNT NO`,
        `c`.`display_name` AS `CLIENT NAME`,
        `ca`.`city` AS `CITY`,
        `ca`.`state` AS `STATE`,
        `ca`.`country` AS `COUNTRY`,
        cast(truncate(max(if((`bd`.`Transaction_type` = 'SERVICE_CHARGES'),
                    `bd`.`Amount`,
                    0)),
                2)
            as char charset utf8) AS `SERVICE CHARGES`,
        cast(truncate(max(if((`bd`.`Transaction_type` = 'TAXES'),
                    `bd`.`Amount`,
                    0)),
                2)
            as char charset utf8) AS `TAXES`,
        cast(truncate(max(if((`bd`.`Transaction_type` = 'ADJUSTMENT'),
                    `bd`.`Amount`,
                    0)),
                2)
            as char charset utf8) AS `ADJUSTMENT`,
        cast(truncate(max(if((`bd`.`Transaction_type` like convert( concat('%', 'PAYMENT', '%') using utf8)),
                    `bd`.`Amount`,
                    0)),
                2)
            as char charset utf8) AS `PAYMENT`,
        cast(truncate(max(if((`bd`.`Transaction_type` = 'ONETIME_CHARGES'),
                    `bd`.`Amount`,
                    0)),
                2)
            as char charset utf8) AS `ONETIME CHARGES`,

cast(truncate(max(if((`bd`.`Transaction_type` = 'DEPOSIT&REFUND'),
                    `bd`.`Amount`,
                    0)),
                2)
            as char charset utf8) AS `DEPOSIT & REFUND`,
        cast(truncate(`bm`.`Due_amount`, 2) as char charset utf8) AS `BILL AMOUNT`
    from
        (((`m_client` `c`
        join `b_client_address` `ca` ON ((`c`.`id` = `ca`.`client_id`)))
        join `b_bill_master` `bm` ON (((`c`.`id` = `bm`.`Client_id`)
            and (`bm`.`is_deleted` = 'N'))))
        join `b_bill_details` `bd` ON ((`bm`.`id` = `bd`.`Bill_id`)))
    group by `bd`.`Bill_id`
