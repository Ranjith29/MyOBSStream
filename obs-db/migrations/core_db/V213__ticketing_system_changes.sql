SET @codeId=(select id from m_code where code_name='Ticket Status');
INSERT IGNORE INTO m_code_value VALUES (NULL, @codeId, 'FollowUp', '1');
SET @codeId=(select id from m_code where code_name='Ticket Status');
INSERT IGNORE INTO m_code_value VALUES (NULL, @codeId, 'Closed', '1');

SET @codeId=(select id from m_code where code_name='Problem Code');
INSERT IGNORE INTO m_code_value VALUES (NULL, @codeId, 'Installation Appointment', '6');
SET @codeId=(select id from m_code where code_name='Problem Code');
INSERT IGNORE INTO m_code_value VALUES (NULL, @codeId, 'New Order', '7');

SET @id=(select id from m_code_value where code_value='Testing');
UPDATE `m_code_value` SET `code_value`='Appointment' WHERE `id`=@id;

SET @id=(select id from b_eventaction_mapping where event_name='Add Comment');
UPDATE `b_eventaction_mapping` SET `event_name`='Edit Ticket' WHERE `id`=@id;

INSERT IGNORE INTO `c_configuration` (`name`, `enabled`) VALUES ('is-create-Ticket', '0');

INSERT IGNORE INTO c_configuration VALUES(NULL, 'Is-Addon-Pair-with-NewDevices', '0', NULL, 'Order', 'If this flag is enable we will pair addon service with new device');


INSERT IGNORE INTO `job` (`name`, `display_name`, `cron_expression`, `cron_description`, `create_time`, `task_priority`, `previous_run_start_time`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `user_id`) VALUES ('FOLLOWUP_TICKET', 'FollowUp Tickets', '0 30 10 1/1 * ? *', 'Daily Once at 10:30AM', now(), '5', now(), 'FOLLOWUP_TICKETJobDetaildefault _ DEFAULT', '0', '0', '1', '0', '0', '1');

SET @jobId=(select id from job where name='FOLLOWUP_TICKET');
INSERT IGNORE INTO `job_parameters` (`id`, `job_id`, `param_name`, `param_type`, `param_value`, `is_dynamic`) VALUES (NULL, @jobId, 'reportName', 'COMBO', 'FollowUp Tickets', 'Y');


INSERT IGNORE INTO `stretchy_report` (`report_name`, `report_type`, `report_subtype`, `report_category`, `report_sql`, `description`, `core_report`, `use_report`) VALUES ('FollowUp Tickets', 'Table', '', 'Scheduling Job', 'select btm.id as ticketId,btm.client_id as clientId from b_ticket_master btm \nwhere DATE_FORMAT((btm.ticket_date),\'%y-%m-%d\') = DATE_FORMAT((NOW() - INTERVAL 3 DAY ),\'%y-%m-%d\') and status=\'open\'\ngroup by ticketId;', 'FollowUp Tickets', '0', '0');



DROP PROCEDURE  IF EXISTS ticket_master_columns;
DELIMITER //
CREATE PROCEDURE ticket_master_columns() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_master'
      AND COLUMN_NAME ='appointment_date') THEN
ALTER TABLE b_ticket_master ADD COLUMN appointment_date datetime AFTER closed_date;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_master'
      AND COLUMN_NAME ='appointment_time') THEN
ALTER TABLE b_ticket_master ADD COLUMN appointment_time time AFTER appointment_date;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_master'
      AND COLUMN_NAME ='followup_date') THEN
ALTER TABLE b_ticket_master ADD COLUMN followup_date datetime AFTER appointment_time;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_master'
      AND COLUMN_NAME ='followup_time') THEN
ALTER TABLE b_ticket_master ADD COLUMN followup_time time AFTER followup_date;
END IF;
END //
DELIMITER ;
call ticket_master_columns();
DROP PROCEDURE  IF EXISTS ticket_master_columns;


DROP PROCEDURE  IF EXISTS office_additional_info;
DELIMITER //
CREATE PROCEDURE office_additional_info() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='m_office_additional_info'
      AND COLUMN_NAME ='balance_amount') THEN
ALTER TABLE m_office_additional_info ADD COLUMN balance_amount DOUBLE(24,6) DEFAULT NULL;
END IF;
END //
DELIMITER ;
call office_additional_info();
DROP PROCEDURE  IF EXISTS office_additional_info;

