CREATE TABLE IF NOT EXISTS `b_wifi_details` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) NOT NULL,
  `wifi_password` varchar(45) DEFAULT NULL,
  `ssid` varchar(45) DEFAULT NULL,
  `service_type` varchar(45) DEFAULT NULL,
  `is_deleted` char(2) DEFAULT 'N',
  `order_id` int(20) NOT NULL,
  `service_id` int(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1;

INSERT IGNORE INTO `m_permission` (`id`,`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES (null,'organisation','READ_WIFIMASTER','WIFIMASTER','READ',0);
INSERT IGNORE INTO `m_permission` (`id`,`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES (null,'organisation','DELETE_WIFIMASTER','WIFIMASTER','DELETE',0);
INSERT IGNORE INTO `m_permission` (`id`,`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES (null,'organisation','CREATE_WIFIMASTER','WIFIMASTER','CREATE',0);
INSERT IGNORE INTO `m_permission` (`id`,`grouping`,`code`,`entity_name`,`action_name`,`can_maker_checker`) VALUES (null,'organisation','UPDATE_WIFIMASTER','WIFIMASTER','UPDATE',0);

