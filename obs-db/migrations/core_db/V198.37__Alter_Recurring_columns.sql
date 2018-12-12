 INSERT IGNORE INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'AUTHORIZE_RECURRING', 'RECURRING', 'AUTHORIZE', 0);

INSERT IGNORE INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('client&orders', 'UPDATEPROFILE_RECURRING', 'RECURRING', 'UPDATEPROFILE', 0);

INSERT IGNORE INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'READ_RECURRING', 'RECURRING', 'READ', 0);

Drop procedure IF EXISTS recurringProcess; 
DELIMITER //
create procedure recurringProcess() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'gateway_name'
     and TABLE_NAME = 'b_recurring'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_recurring` ADD COLUMN `gateway_name` VARCHAR(45) NOT NULL  AFTER `order_id` ;
END IF;
END //
DELIMITER ;
call recurringProcess();
Drop procedure IF EXISTS recurringProcess;

INSERT IGNORE INTO `c_paymentgateway_conf` (`id`, `name`, `enabled`, `value`) VALUES (null, 'is-paypal', '0','');

INSERT IGNORE INTO `c_paymentgateway_conf` (`id`, `name`, `enabled`, `value`) VALUES (null, 'is-paypal-for-ios', '0','');


INSERT IGNORE INTO `c_paymentgateway_conf` (`id`, `name`, `enabled`, `value`) VALUES (null, 'is-paypal', '0','');

INSERT IGNORE INTO `c_paymentgateway_conf` (`id`, `name`, `enabled`, `value`) VALUES (null, 'is-paypal-for-ios', '0','');

CREATE TABLE IF NOT EXISTS `b_recurring_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(20) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `transaction_id` varchar(50) DEFAULT NULL,
  `transaction_category` varchar(50) NOT NULL,
  `source` varchar(50) DEFAULT NULL,
  `transaction_date` datetime DEFAULT NULL,
  `transaction_data` longtext,
  `transaction_status` text,
  `obs_status` varchar(50) DEFAULT NULL,
  `obs_description` longtext,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1;


