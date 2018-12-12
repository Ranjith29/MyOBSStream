SET @id = (select id from m_code where code_name='Ticket Status');
INSERT IGNORE INTO m_code_value VALUES (null,@id,'Problems',1);


Drop procedure IF EXISTS addedAddressColumn;
DELIMITER //
create procedure addedAddressColumn() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_client_address' 
     And  COLUMN_NAME in( 'distribution_box')) then
     ALTER TABLE `b_client_address` 
     ADD COLUMN `distribution_box` VARCHAR(100) NULL DEFAULT NULL AFTER `state`;
END IF;
END //
DELIMITER ;
call addedAddressColumn();
Drop procedure IF EXISTS addedAddressColumn;
