Drop procedure IF EXISTS addedExDirectoryColoumn;
DELIMITER //
create procedure addedExDirectoryColoumn() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_exdirectory' 
     And  COLUMN_NAME in( 'is_umee_app')) then
     ALTER TABLE `b_exdirectory` 
     ADD COLUMN `is_umee_app` TINYINT(1) NOT NULL DEFAULT '0' AFTER `is_number_with_held`;
END IF;
END //
DELIMITER ;
call addedExDirectoryColoumn();
Drop procedure IF EXISTS addedExDirectoryColoumn;
