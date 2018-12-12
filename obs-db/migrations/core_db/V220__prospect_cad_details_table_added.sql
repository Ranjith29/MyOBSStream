CREATE TABLE IF NOT EXISTS `b_prospect_card_details` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `prospect_id` int(11) NOT NULL,
  `prospect_orders_id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `card_number` varchar(100) NOT NULL,
  `card_type` varchar(20) NOT NULL,
  `cvv_number` varchar(20) NOT NULL,
  `card_expiry_date` varchar(50) NOT NULL,
  `type` varchar(50) NOT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'N',
  `createdby_id` bigint(20) DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pcd_prospectId_idx` (`prospect_id`),
  KEY `fk_pcd_prospect_orderId_idx` (`prospect_orders_id`),
  CONSTRAINT `fk_pcd_prospectId` FOREIGN KEY (`prospect_id`) REFERENCES `b_prospect` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_pcd_prospect_orderId` FOREIGN KEY (`prospect_orders_id`) REFERENCES `b_prospect_orders` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=latin1;


INSERT IGNORE INTO `b_eventaction_mapping` (`event_name`, `action_name`, `process`, `is_deleted`, `is_synchronous`) VALUES ('Appointment Ticket', 'Send Email', 'workflow_events', 'Y', 'N');


INSERT IGNORE INTO job VALUES(NULL, 'LOG_FILES_REMOVE', 'Log Files Remove', '0 0 7 ? * WED *', 'Weekly Once at 07:00 AM', '2017-04-05 15:36:06', '5', NULL, '2017-04-05 18:50:47', '2017-04-12 07:00:00', 'LOG_FILES_REMOVEJobDetaildefault _ DEFAULT', NULL, '0', '0', '1', '0', '0', '1');

