
Drop procedure IF EXISTS Unique_Ref_ChangeProcedure; 
DELIMITER //
create procedure Unique_Ref_ChangeProcedure() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_NAME = 'service_identification_uq'
     and TABLE_NAME = 'b_prov_service_details'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_prov_service_details`
DROP INDEX `service_identification_uq` ,
ADD UNIQUE INDEX `service_identification_uq` (`service_id` ASC, `item_id` ASC);
END IF;
END //
DELIMITER ;
call Unique_Ref_ChangeProcedure();
Drop procedure IF EXISTS Unique_Ref_ChangeProcedure;


Drop procedure IF EXISTS Unique_Ref_ChangeProcedurenNew; 
DELIMITER //
create procedure Unique_Ref_ChangeProcedurenNew() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_NAME = 'serviceCode'
     and TABLE_NAME = 'b_prov_service_details'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_prov_service_details`
DROP INDEX `serviceCode`;
END IF;
END //
DELIMITER ;
call Unique_Ref_ChangeProcedurenNew();
Drop procedure IF EXISTS Unique_Ref_ChangeProcedurenNew;

