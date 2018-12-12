INSERT IGNORE INTO `c_configuration` (`name`, `enabled`, `value`) VALUES ('is-Note-for-Plan', '1', '');

INSERT IGNORE INTO `b_eventaction_mapping` (`event_name`, `action_name`, `process`, `is_deleted`, `is_synchronous`) 
VALUES ('Order Booking', 'Send ClientCreation Email', 'workflow_events', 'N', 'N');

Drop procedure IF EXISTS plan_notes_Process; 
DELIMITER //
create procedure plan_notes_Process() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'plan_Notes'
     and TABLE_NAME = 'b_plan_master'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_plan_master` ADD COLUMN `plan_Notes` TEXT DEFAULT NULL AFTER `allow_topup` ;
END IF;
END //
DELIMITER ;
call plan_notes_Process();
Drop procedure IF EXISTS plan_notes_Process;
