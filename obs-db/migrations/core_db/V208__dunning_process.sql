INSERT IGNORE INTO `job` (`name`, `display_name`, `cron_expression`, `cron_description`, `create_time`, `task_priority`,
`previous_run_start_time`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `user_id`) 
VALUES ('SUSPENSION_Of_SERVICE', 'SuspensionOfService', '0 0 3 22 1/1 ? *', 'Monthly Once at 03:00AM', now(), 5, 
now(), 'SUSPENSION_Of_SERVICEJobDetaildefault _ DEFAULT', 0, 0, 1, 0, 0, 1);

SET @jobId=(select id from job where name='SUSPENSION_Of_SERVICE');
INSERT IGNORE INTO `job_parameters` (`id`, `job_id`, `param_name`, `param_type`, `param_value`, `is_dynamic`) VALUES(NULL, @jobId, 'reportName', 'COMBO', 'SuspensionOfService', 'Y');

INSERT IGNORE INTO `job` (`name`, `display_name`, `cron_expression`, `cron_description`, `create_time`, `task_priority`,
`previous_run_start_time`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `user_id`) 
VALUES ('DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS', 'DisconnectionOfAllServices', '0 0 3 1/1 * ? *', 'Daily Once at 03:00AM', now(), 5, 
now(), 'DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERSJobDetaildefault _ DEFAULT', 0, 0, 1, 0, 0, 1);

SET @jobId=(select id from job where name='DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS');
INSERT IGNORE INTO `job_parameters` (`id`, `job_id`, `param_name`, `param_type`, `param_value`, `is_dynamic`) VALUES(NULL, @jobId, 'reportName', 'COMBO', 'DisconnectOfAllServices', 'Y');

-- eventaction mapping --
INSERT IGNORE INTO b_eventaction_mapping VALUES(NULL, 'Order Reactivate', 'Reactivate', 'workflow_events', 'N', 'N');
INSERT IGNORE INTO b_eventaction_mapping VALUES(NULL, 'Order Reactivation Fee', 'Reactivation_Fee', 'workflow_events', 'Y', 'N');

-- fee master --
SET @codeId=(select id from m_code where code_name='Transaction Type');
INSERT IGNORE INTO m_code_value VALUES(NULL, @codeId, 'Reactivation', '5');

INSERT IGNORE INTO b_fee_master(`id`, `fee_code`, `fee_description`, `transaction_type`, `charge_code`, `default_fee_amount`, `is_deleted`, `enabled`) 
VALUES (NULL, 'RAF', 'ReactivationFee', 'Reactivation', 'OTC', '0.000000', 'N', '0');
