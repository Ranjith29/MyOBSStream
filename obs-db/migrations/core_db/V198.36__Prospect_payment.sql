CREATE TABLE IF NOT EXISTS `b_prospect_payments` (
  `id` bigint(10) NOT NULL AUTO_INCREMENT,
  `client_id` bigint(10) NOT NULL,
  `json` varchar(1000) DEFAULT NULL,
  `error_data` varchar(1000) DEFAULT NULL,
  `is_processed_pg` char(1) CHARACTER SET latin1 DEFAULT 'N',
  `is_processed_obs` char(1) CHARACTER SET latin1 DEFAULT 'N',
  `createdby_id` bigint(10) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(10) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=latin1


