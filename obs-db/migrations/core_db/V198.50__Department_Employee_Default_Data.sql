INSERT IGNORE INTO `b_office_department` (`department_name`, `department_description`, `office_id`, `is_deleted`, `is_allocated`) VALUES ('support', 'support', '1', 'N', 'yes');

INSERT IGNORE INTO m_appuser VALUES(NULL, '0', '1', NULL, 'support', 'support', 'support', '8bbe00729fb619fe49d4e55c2f97ab575147aa33b79ee00f197da99a81fa11a7', 'snr.min@gmail.com', '0', '1', '1', '1', '1');

INSERT IGNORE INTO `b_office_employee` (`employee_name`, `employee_loginname`, `employee_password`, `employee_phone`, `employee_email`, `department_id`, `employee_isprimary`, `is_deleted`, `user_id`) VALUES ('support', 'support', 'support', '123456789', 'snr.min@gmail.com ', (select id from b_office_department bd where bd.department_name = 'support'), '1', 'n', (select id from m_appuser ma where ma.username = 'support'));

INSERT IGNORE INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) 
VALUES ('portfolio', 'READ_PseudoCard', 'PseudoCard', 'READ', '0');

