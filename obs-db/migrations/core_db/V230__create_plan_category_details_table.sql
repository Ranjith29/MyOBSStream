CREATE TABLE IF NOT EXISTS `b_plan_category_detail` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `plan_id` int(10) NOT NULL,
  `client_category_id` bigint(20) NOT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `fk_pcd_pmid` (`plan_id`),
  CONSTRAINT `fk_pcd_pmid` FOREIGN KEY (`plan_id`) REFERENCES `b_plan_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;



