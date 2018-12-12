CREATE TABLE IF NOT EXISTS `b_ticketassign_rule` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `businessprocess_id` bigint(20) NOT NULL,
  `clientcategory_id` int(10) NOT NULL,
  `department_id` int(10) NOT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT '0',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `businessprocessid_with_categorytype_uniquekey` (`businessprocess_id`,`clientcategory_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=latin1;


INSERT IGNORE INTO `b_eventaction_mapping` VALUES(NULL, 'Order Booking', 'Ticket Creation', 'workflow_events', 'Y', 'N');
INSERT IGNORE INTO `b_eventaction_mapping` VALUES(NULL, 'Create Client', 'Ticket Creation', 'workflow_events', 'Y', 'N');
INSERT IGNORE INTO `b_eventaction_mapping` VALUES(NULL, 'Hardware Sale', 'Ticket Creation', 'workflow_events', 'Y', 'N');


INSERT IGNORE INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'CREATE_TICKETASSIGNRULE', 'TICKETASSIGNRULE', 'CREATE', '0');

INSERT IGNORE INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'UPDATE_TICKETASSIGNRULE', 'TICKETASSIGNRULE', 'UPDATE', '0');

INSERT IGNORE INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('organisation', 'DELETE_TICKETASSIGNRULE', 'TICKETASSIGNRULE', 'DELETE', '0');


SET @id=(select id from m_code_value where code_value='New Order');
UPDATE `m_code_value` SET `code_value`='Order Booking' WHERE `id`=@id;

SET @codeId=(select id from m_code where code_name='Problem Code');
INSERT IGNORE INTO `m_code_value` VALUES (NULL, @codeId, 'Create Client', '8');

SET @codeId=(select id from m_code where code_name='Problem Code');
INSERT IGNORE INTO `m_code_value` VALUES (NULL, @codeId, 'Hardware Sale', '9');

delete from c_configuration where name='is-create-Ticket';

INSERT IGNORE INTO m_permission VALUES (NULL, 'organisation', 'NEWPASSWORDREQUEST_ORDER', 'ORDER', 'NEWPASSWORDREQUEST', '0');
INSERT IGNORE INTO b_provisioning_actions VALUES (NULL, 'NEW_PASSWORD_VOIP', 'NEW_PASSWORD_VOIP', 'u-mee sync server', 'N', 'N');
INSERT IGNORE INTO m_permission VALUES (NULL, 'organisation', 'RESETPASSWORDREQUEST_ORDER', 'ORDER', 'RESETPASSWORDREQUEST', '0');
INSERT IGNORE INTO b_provisioning_actions VALUES (NULL, 'RESET_PASSWORD_VOIP', 'RESET_PASSWORD_VOIP', 'u-mee sync server', 'N', 'N');

INSERT IGNORE INTO m_code values(null,'Item Category',1,'Define Inventory item category');

SET @ID=(select id from m_code where code_name='Item Category');
INSERT IGNORE INTO m_code_value values(NULL,@ID,'None',1);
INSERT IGNORE INTO m_code_value values(NULL,@ID,'CPE',2);
INSERT IGNORE INTO m_code_value VALUES(NULL,@ID,'STB',3);
INSERT IGNORE INTO m_code_value values(NULL,@ID,'Telephone Number',4);

Drop procedure IF EXISTS item_category_type; 
DELIMITER //
create procedure item_category_type() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'item_category_id'
     and TABLE_NAME = 'b_item_master'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_item_master` ADD COLUMN `item_category_id` INT(5) NOT NULL AFTER `warranty`;
END IF;
END //
DELIMITER ;
call item_category_type();
Drop procedure IF EXISTS item_category_type;


