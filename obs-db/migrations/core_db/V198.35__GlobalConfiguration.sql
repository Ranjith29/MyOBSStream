INSERT IGNORE INTO `c_configuration` (`id`, `name`, `enabled`, `value`) VALUES (null, 'renewal-order', '0', '');

INSERT IGNORE INTO `c_configuration` (`id`, `name`, `enabled`, `value`) VALUES (null, 'change-plan', '0', '');

INSERT IGNORE INTO `c_configuration` (`id`, `name`, `enabled`, `value`) VALUES (null, 'book-order', '0', '');

insert ignore into c_configuration values(null,'prospect-payment',0, NULL,'Prospects','If this flag is enable we will perform online payment for customer orders');

INSERT IGNORE INTO m_permission (`id`, `grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('NULL', 'PROSPECT', 'PAYMENT_PROSPECT', 'PROSPECT', 'PAYMENT', '0');
