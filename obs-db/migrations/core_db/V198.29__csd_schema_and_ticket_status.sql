-- osd scripts 
INSERT ignore INTO `stretchy_report` (`id`,`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,`description`,`core_report`,`use_report`) VALUES (null, 'OSD_Reminder_from_7days', 'Table', NULL, 'Scheduling Job', 'select ba.hw_serial_no as key_id, c.display_name as customerName, pm.plan_description as serviceName, DATE_FORMAT(o.end_date, \'%Y-%m-%d\') as disconnectionDate from b_orders o, m_client c, b_association ba, b_plan_master pm where o.order_status = 1 and o.client_id = c.id and o.plan_id = pm.id and o.end_date in (DATE_FORMAT(date_add(now(), interval 4 day),\'%Y-%m-%d\') , DATE_FORMAT(date_add(now(), interval 1 day),\'%Y-%m-%d\')) and o.id = ba.order_id and ba.is_deleted = \'N\'\n', 'Order reminder for 7 days osm', '0', '0');

INSERT ignore INTO `b_message_template` (`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) VALUES (null, 'OSD_FROM_7_DAYS', 'REMINDER', NULL, 'Dear <Customer Name>, Your Plan <Plan Name> will expire on <End Date>.', NULL, 'O', '1', '2016-01-11 16:43:09', '1', '2016-01-11 16:43:09', 'N');

INSERT ignore INTO b_command(`id`,`provisioning_system`,`command_name`,`status`,`is_deleted`)VALUES('1', 'Stalker', 'OSM', 'Y', 'N');

-- m_code value for status
SET @id=(SELECT id FROM m_code WHERE code_name='Ticket Status');
update m_code_value set code_value='Open' where code_id=@id and code_value='New Open';

DROP PROCEDURE  IF EXISTS ticket_detail_notes;
DELIMITER //
CREATE PROCEDURE ticket_detail_notes() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_details'
      AND COLUMN_NAME ='Assign_from') THEN
ALTER TABLE `b_ticket_details` ADD COLUMN `notes` LONGTEXT NULL DEFAULT NULL AFTER `username`;
END IF;
END //
DELIMITER ;
call ticket_detail_notes();
DROP PROCEDURE  IF EXISTS ticket_detail_notes;

-- document permission for selfcare 

set @pid=(select id from m_permission where code='CREATE_DOCUMENT');
set @rid=(select id from m_role where name='selfcare');
Insert Ignore into m_role_permission values(@rid,@pid);


