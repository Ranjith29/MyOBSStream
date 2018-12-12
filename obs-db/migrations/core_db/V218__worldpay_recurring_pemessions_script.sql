INSERT IGNORE INTO `m_permission` (`id`,`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES (null,'portfolio','READ_ObfuscatedCard','ObfuscatedCard','READ',0);

INSERT IGNORE INTO `m_permission` (`id`,`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES (null,'organisation','CREATE_WORLDPAYPAYMENTGATEWAY','WORLDPAYPAYMENTGATEWAY','CREATE',0);

INSERT IGNORE INTO `c_paymentgateway_conf` (`id`,`name`,`enabled`,`value`,`description`) 
VALUES (null,'worldpay',0,'{\"url\":\"https://secure-test.worldpay.com/wcc/purchase\",\"instId\":\"1173217\",\"cartId\":\"OPENBILLINGSYSBGTESTM1\",\"service_key\":\"T_S_7c8f8038-667a-4659-8a5c-6c8c7cf728a0\",\"client_key\":\"T_C_527ad219-b537-47a1-a6f3-b51e29158dae\"}',NULL);

INSERT IGNORE INTO `job` (`name`, `display_name`, `cron_expression`, `cron_description`, `create_time`, `task_priority`,
`previous_run_start_time`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `user_id`) 
VALUES 
('WORLDPAY_RECURRING_BATCH_PROCESS', 'World Pay Batch Process', '0 0 3 1/1 * ? *', 'Daily Once at 01:00AM', now(), 5, 
now(), 'WORLDPAY_RECURRING_BATCH_PROCESSJobDetaildefault _ DEFAULT', 0, 0, 1, 0, 0, 1);


Drop procedure IF EXISTS addon_worldpay; 
DELIMITER //
create procedure addon_worldpay() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'is_worldpay_billing'
     and TABLE_NAME = 'b_clientuser'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE b_clientuser ADD COLUMN is_worldpay_billing  CHAR(1) NULL DEFAULT 'N' AFTER zebra_subscriber_id;
END IF;
END //
DELIMITER ;
call addon_worldpay();
Drop procedure IF EXISTS addon_worldpay;


Drop procedure IF EXISTS addon_token_worldpay; 
DELIMITER //
create procedure addon_token_worldpay() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'w_token'
     and TABLE_NAME = 'm_client_card_details'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE m_client_card_details ADD COLUMN w_token  VARCHAR(45) NULL DEFAULT NULL AFTER is_deleted;
END IF;
END //
DELIMITER ;
call addon_token_worldpay();
Drop procedure IF EXISTS addon_token_worldpay;


Drop procedure IF EXISTS addon_r_type; 
DELIMITER //
create procedure addon_r_type() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE  COLUMN_NAME = 'r_type'
     and TABLE_NAME = 'm_client_card_details'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE m_client_card_details ADD COLUMN r_type  VARCHAR(45) NULL DEFAULT NULL AFTER w_token;
END IF;
END //
DELIMITER ;
call addon_r_type();
Drop procedure IF EXISTS addon_r_type;




