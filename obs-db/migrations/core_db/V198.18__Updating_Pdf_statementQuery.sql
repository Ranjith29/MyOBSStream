SET SQL_SAFE_UPDATES=0;
UPDATE stretchy_report SET report_sql = 'SELECT DISTINCT id AS billId FROM b_bill_master WHERE filename=''invoice''
AND is_deleted=''N'' AND Due_date=DATE_FORMAT(NOW() + INTERVAL 7 DAY ,''%Y-%m-%d'')' WHERE report_name='PDF Statement';
UPDATE job SET cron_expression='0 10 0 1/1 * ? *' WHERE name='INVOICING';
SET @ID=(SELECT ID FROM job WHERE name='PDF');
UPDATE job_parameters SET  is_dynamic = 'Y' WHERE job_id=@ID AND param_name='reportName';
DROP TABLE IF EXISTS `b_ticket_history`;
CREATE TABLE `b_ticket_history` (`id` bigint(20) NOT NULL AUTO_INCREMENT,`ticket_id` bigint(20) NOT NULL,`assigned_to` bigint(20) DEFAULT NULL,`createdby_id` bigint(20) DEFAULT NULL,`created_date` datetime DEFAULT NULL,`Assign_from` varchar(200) DEFAULT NULL,`status` varchar(45) DEFAULT NULL, PRIMARY KEY (`id`),KEY `FK_tcktid` (`ticket_id`), KEY `fk_tdl_user` (`createdby_id`),CONSTRAINT `FK_tcktid` FOREIGN KEY (`ticket_id`) REFERENCES `b_ticket_master` (`id`), CONSTRAINT `fk_tdl_user` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

DROP PROCEDURE  IF EXISTS ticket_issue_assign;
DELIMITER //
CREATE PROCEDURE ticket_issue_assign() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_master'
      AND COLUMN_NAME ='issue') THEN
ALTER TABLE b_ticket_master CHANGE COLUMN `issue` `issue` LONGTEXT NULL DEFAULT NULL;
END IF;
END //
DELIMITER ;
call ticket_issue_assign();
DROP PROCEDURE  IF EXISTS ticket_issue_assign;
