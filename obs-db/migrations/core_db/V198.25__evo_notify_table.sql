CREATE TABLE IF NOT EXISTS `b_evo_notify` (
  `id` int(50) NOT NULL AUTO_INCREMENT,
  `blowfish_data` longtext,
  `length` bigint(20) DEFAULT NULL,
  `tenant` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1
