CREATE TABLE IF NOT EXISTS `b_batch_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `transaction_date` datetime DEFAULT NULL,
  `transaction_type` varchar(45) DEFAULT NULL,
  `count_value` varchar(45) DEFAULT NULL,
  `batch_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;


Drop procedure IF EXISTS addBatchId;
DELIMITER //
create procedure addBatchId() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'batch_id'
     and TABLE_NAME = 'b_bill_master'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_bill_master add column batch_id varchar(100) DEFAULT NULL after is_deleted;

END IF;
END //
DELIMITER ;
call addBatchId();
Drop procedure IF EXISTS addBatchId;




