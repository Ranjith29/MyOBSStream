Drop procedure IF EXISTS AddColumn_resolutiondate; 
DELIMITER //
create procedure AddColumn_resolutiondate() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'resolution_date'
     and TABLE_NAME = 'b_ticket_master'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_ticket_master` ADD COLUMN `resolution_date` DATETIME DEFAULT NULL AFTER `due_date` ;
END IF;
END //
DELIMITER ;
call AddColumn_resolutiondate();
Drop procedure IF EXISTS AddColumn_resolutiondate;


Drop procedure IF EXISTS addon_association; 
DELIMITER //
create procedure addon_association() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'associate_id'
     and TABLE_NAME = 'b_orders_addons'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE b_orders_addons ADD COLUMN associate_id BIGINT(10) NOT NULL AFTER provision_system;
END IF;
END //
DELIMITER ;
call addon_association();
Drop procedure IF EXISTS addon_association;
