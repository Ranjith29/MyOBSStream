SET SQL_SAFE_UPDATES=0;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `b_chargevariant`;
CREATE TABLE IF NOT EXISTS `b_chargevariant` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `chargevariant_code` varchar(20) NOT NULL,
  `status` varchar(15) NOT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `is_delete` char(1) DEFAULT 'N',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `chargevariantcode` (`chargevariant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `b_chargevariant_detail`;
CREATE TABLE  IF NOT EXISTS `b_chargevariant_detail` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `chargevariant_id` int(10) NOT NULL,
  `variant_type` varchar(10) NOT NULL,
  `from_range` int(10) DEFAULT NULL,
  `to_range` int(10) DEFAULT NULL,
  `amount_type` varchar(10) NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'n',
  PRIMARY KEY (`id`),
  KEY `CHD_FK1` (`chargevariant_id`),
  CONSTRAINT `CHD_FK1` FOREIGN KEY (`chargevariant_id`) REFERENCES `b_chargevariant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


insert ignore into m_permission values(null,'billing','CREATE_CHARGEVARIANT','CHARGEVARIANT','CREATE',0);
insert ignore into m_permission values(null,'billing','UPDATE_CHARGEVARIANT','CHARGEVARIANT','UPDATE',0);
insert ignore into m_permission values(null,'billing','DELETE_CHARGEVARIANT','CHARGEVARIANT','DELETE',0);

INSERT IGNORE INTO m_code VALUES (null,'Variant Type',0,'Define Variant Type');
SET @id = (select id from m_code where code_name='Variant Type');

INSERT IGNORE INTO m_code_value VALUES (null,@id,'ANY',0);
INSERT IGNORE INTO m_code_value VALUES (null,@id,'Range',1);

INSERT IGNORE INTO `b_chargevariant` VALUES(0,'None','ACTIVE',NOW(),NOW()+INTERVAL 1000 DAY,'N',1,NOW(),1,NOW());
UPDATE b_chargevariant SET id=0 WHERE chargevariant_code='None';
UPDATE b_plan_pricing SET charging_variant=0 WHERE charging_variant=2;
