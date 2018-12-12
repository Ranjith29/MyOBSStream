SET FOREIGN_KEY_CHECKS=0;
SET SQL_SAFE_UPDATES=0;
DROP PROCEDURE  IF EXISTS itempriceWithChargeVariant;
DELIMITER //
CREATE PROCEDURE itempriceWithChargeVariant() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_item_price'
      AND COLUMN_NAME ='charge_variant') THEN
ALTER TABLE b_item_price ADD COLUMN `charge_variant` int(25) DEFAULT NULL AFTER `price`,
ADD CONSTRAINT `fk_itemprice_charge_variant` 
FOREIGN KEY (`charge_variant`) REFERENCES `b_chargevariant` (`id`);
END IF;
END //
DELIMITER ;
call itempriceWithChargeVariant();
DROP PROCEDURE  IF EXISTS itempriceWithChargeVariant;

UPDATE b_item_price SET charge_variant = 0 WHERE charge_variant IS NULL; 

