INSERT IGNORE INTO `job` (`name`, `display_name`, `cron_expression`, `cron_description`, `create_time`, `task_priority`,
`previous_run_start_time`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `user_id`) 
VALUES ('EVO_BATCH_PROCESS', 'EvoBatchProcess Upload', '0 0 0 1/1 * ? *', 'Daily once at Midnight', now(), 5, 
now(), 'EVO_BATCH_PROCESSJobDetaildefault _ DEFAULT', 0, 0, 1, 0, 0, 1);


SET @id=(SELECT id from job WHERE name='EVO_BATCH_PROCESS');
INSERT IGNORE INTO `job_parameters` (`job_id`, `param_name`,`param_type`,`param_default_value`,`param_value`,`is_dynamic`) 
VALUES(@id,'value','COMBO',NULL,'{\"merchantId\":\"pg_57966t\",\"port\":\"22\",\"host\": \"ftp.computop-paygate.com\",
\"username\":\"pg_57966_batch\",\"SFTPWORKINGDIR\":\"/paygate/pg_57966/\",\"passphrase\":\"ruZNfuwpyfXACIHtl4eB\",
\"privateKey\":\"/home/obs/.ssh/id_rsa\"}','N');


INSERT IGNORE INTO `job` (`name`, `display_name`, `cron_expression`, `cron_description`, `create_time`, `task_priority`,
`previous_run_start_time`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `user_id`) 
VALUES ('EVO_BATCH_PROCESS_DOWNLOAD', 'EvoBatchProcess Download', '0 0 5 1/1 * ? *', 'Daily once at earlymorning 5`o clock', now(), 5, 
now(), 'EVO_BATCH_PROCESS_DOWNLOADJobDetaildefault _ DEFAULT', 0, 0, 1, 0, 0, 1);


CREATE TABLE IF NOT EXISTS `b_evo_batchprocess` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `input_filename` varchar(100) NOT NULL,
  `path` varchar(200) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `is_uploaded` char(3) DEFAULT 'N',
  `is_downloaded` char(3) DEFAULT 'N',
  `status` char(3) DEFAULT 'T',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;


-- Department --

DROP PROCEDURE  IF EXISTS department_alter_columns;
DELIMITER //
CREATE PROCEDURE department_alter_columns() 
BEGIN
 IF NOT EXISTS (
     SELECT * FROM information_schema.KEY_COLUMN_USAGE
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_office_department'
      AND COLUMN_NAME ='department_name' and CONSTRAINT_NAME = 'department_name_UNIQUE') THEN
ALTER TABLE `b_office_department` ADD UNIQUE INDEX `department_name_UNIQUE` (`department_name` ASC) ;
END IF;
END //
DELIMITER ;
call department_alter_columns();
DROP PROCEDURE  IF EXISTS department_alter_columns;

CREATE TABLE IF NOT EXISTS `b_ticket_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ticket_id` bigint(20) NOT NULL,
  `assigned_to` bigint(20) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `Assign_from` varchar(200) DEFAULT NULL,
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_tcktid` (`ticket_id`),
  KEY `fk_tdl_user` (`createdby_id`),
  CONSTRAINT `FK_tcktid` FOREIGN KEY (`ticket_id`) REFERENCES `b_ticket_master` (`id`),
  CONSTRAINT `fk_tdl_user` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;
