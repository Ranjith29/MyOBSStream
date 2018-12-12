CREATE TABLE IF NOT EXISTS `b_plan_events` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `plan_id` int(10) NOT NULL,
  `event_name` varchar(25) NOT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'n',
  PRIMARY KEY (`id`),
  KEY `PD_EVENT_FK` (`plan_id`),
  CONSTRAINT `PD_EVENT_FK` FOREIGN KEY (`plan_id`) REFERENCES `b_plan_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;




CREATE TABLE IF NOT EXISTS `b_order_events` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `order_id` int(20) NOT NULL,
  `event_id` bigint(20) NOT NULL,
  `event_type` varchar(20) NOT NULL,
  `event_status` int(20) NOT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'n',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_event_cid` (`createdby_id`),
  KEY `fk_order_event_lid` (`lastmodifiedby_id`),
  KEY `fk_order_event_oid` (`order_id`),
  KEY `fk_order_event_eid` (`event_id`),
  CONSTRAINT `fk_order_event_oid` FOREIGN KEY (`order_id`) REFERENCES `b_orders` (`id`),
  CONSTRAINT `fk_order_event_eid` FOREIGN KEY (`event_id`) REFERENCES `b_mod_master` (`id`),
  CONSTRAINT `fk_order_event_cid` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `fk_order_event_lid` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
