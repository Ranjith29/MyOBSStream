SET SQL_SAFE_UPDATES=0;


INSERT IGNORE INTO c_configuration VALUES(NULL, 'is-discounts-apply-on-changeplan', '0', NULL, 'Billing', 'If this flag is enable we will apply old order discounts/promotions to new order based discount expiry date');


UPDATE stretchy_report SET report_sql = 'SELECT
              clnt.account_no as `CUST.NO`,
			  cast(clnt.display_name as char charset utf8) as `CUST.NAME`,   
              DATE_FORMAT(inv.invoice_date, ''%d-%m-%Y'') as `INVOICE DATE`,
			 (CASE WHEN charge.priceline_id = 0 AND charge.charge_type = ''NRC'' THEN  btm.item_description 
			  WHEN charge.priceline_id = -1  AND charge.charge_type=''NRC'' THEN ''Change of address fee''
              WHEN charge.priceline_id = -2  AND charge.charge_type=''NRC'' THEN ''Registration fee''
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



Drop procedure IF EXISTS feeMasterEnable; 
DELIMITER //
create procedure feeMasterEnable() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'enabled'
     and TABLE_NAME = 'b_fee_master'
     and TABLE_SCHEMA = DATABASE())THEN
alter table `b_fee_master` add column `enabled` tinyint(1) NOT NULL DEFAULT '0';
END IF;
END //
DELIMITER ;
call feeMasterEnable();
Drop procedure IF EXISTS feeMasterEnable;


