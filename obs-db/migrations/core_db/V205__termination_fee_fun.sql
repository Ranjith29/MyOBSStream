INSERT IGNORE INTO `b_eventaction_mapping` (`event_name`, `action_name`, `process`, `is_deleted`, `is_synchronous`) VALUES ('Termination_Fee', 'Termination_Fee', 'workflow_events', 'N', 'N');

INSERT IGNORE INTO `m_code_value` (code_id,code_value,order_position) VALUES ((SELECT id FROM m_code mc WHERE mc.code_name='Transaction Type'),
'Termination Fee',(SELECT (MAX(order_position)+1) FROM m_code_value m 
WHERE m.code_id = (SELECT id FROM m_code mc WHERE mc.code_name='Transaction Type')));

INSERT IGNORE INTO `b_fee_master` (`fee_code`, `fee_description`, `transaction_type`, `charge_code`, `default_fee_amount`) VALUES ('TF', 'Termination Fee', 'Termination Fee', 'OTC', '0.000000');

