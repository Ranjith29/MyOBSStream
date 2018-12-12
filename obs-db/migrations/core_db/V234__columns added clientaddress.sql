Drop procedure IF EXISTS addcolumnsclientaddress; 
DELIMITER //
create procedure addcolumnsclientaddress() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'createdby_id'
     and TABLE_NAME = 'b_client_address'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_client_address  ADD COLUMN `createdby_id` BIGINT(20) NULL DEFAULT NULL AFTER `phone_num`;
END IF;

IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'created_date'
     and TABLE_NAME = 'b_client_address'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_client_address  ADD COLUMN `created_date` DATETIME NULL DEFAULT NULL AFTER `createdby_id`;
END IF;

  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'lastmodifiedby_id'
     and TABLE_NAME = 'b_client_address'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_client_address  ADD COLUMN `lastmodifiedby_id` BIGINT(20) NULL DEFAULT NULL AFTER `created_date`;
END IF;

IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'lastmodified_date'
     and TABLE_NAME = 'b_client_address'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_client_address  ADD COLUMN `lastmodified_date` DATETIME NULL DEFAULT NULL AFTER `lastmodifiedby_id`;
END IF;

IF NOT EXISTS (
     SELECT * FROM information_schema. KEY_COLUMN_USAGE
     WHERE TABLE_SCHEMA = DATABASE() and TABLE_NAME ='b_client_address'
     and COLUMN_NAME ='createdby_id' and CONSTRAINT_NAME = 'fk_bca_user')THEN
ALTER TABLE `b_client_address` ADD CONSTRAINT `fk_bca_user` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION ;
END IF;
END //

DELIMITER ;
call addcolumnsclientaddress();
Drop procedure IF EXISTS addcolumnsclientaddress;

