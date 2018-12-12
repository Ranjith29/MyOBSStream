SET SQL_SAFE_UPDATES=0;
INSERT IGNORE INTO stretchy_report VALUES(NULL, 'OrderReminderFor1And7Days', 'Table', '', 'Scheduling Job','select 
    c.email as email,
    c.display_name as customerName,
    pm.plan_description as serviceName,
    DATE_FORMAT(o.end_date,''%Y-%m-%d'') as disconnectionDate,
    c.id as clientId,
    o.id as orderId
from
 b_orders o,
    m_client c,
    b_plan_master pm
where
    o.order_status = 1
 and o.client_id = c.id
        and o.plan_id = pm.id
        and o.end_date in (DATE_FORMAT(date_add(now(), interval 7 day),
            ''%Y-%m-%d'') , DATE_FORMAT(date_add(now(), interval  1 day),
                ''%Y-%m-%d''))', 'Order Reminder every 10 and 20 days for disconnection', '0', '0');

INSERT IGNORE INTO b_message_template VALUES(NULL, 'REMINDER_FOR_1AND7DAYS', 'Reminder For Service Expiry', 'Dear <Customer Name> ,<br/><br/>', 'Your service with <b> <Service name> </b>will Expire on <Disconnection Date>.<br/>Please call us or do the renew or Top-up through your selfcare portal before your Service Expires. <br/> <b>Customer Details<b/>: <br/> ClientId: <clientId>,<br/> OrderId: <orderId>.  <br/><br/>', 'Thanks&Regards <br/>', 'E', '1', now(), '1', now(), 'N');
SET @ID=(SELECT id FROM b_message_template WHERE template_description='REMINDER_FOR_1AND7DAYS');
INSERT IGNORE INTO b_message_params VALUES(NULL,  @ID, '<Customer Name>', '1'),
(NULL,  @ID, '<Service name>', '2'),
(NULL,  @ID, '<Disconnection Date>', '3'),
(NULL,  @ID, '<clientId>', '4'),
(NULL,  @ID, '<orderId>', '5');

SET @jobId=(SELECT id FROM job WHERE name='MERGE_MESSAGE');
UPDATE job SET cron_expression='0 0 06 1/1 * ? *',cron_description='Daily Once at 06:00AM' WHERE name='MERGE_MESSAGE';
UPDATE job_parameters SET param_value='REMINDER_FOR_1AND7DAYS' WHERE param_name='messageTemplate' AND job_id=@jobId;
UPDATE job_parameters SET param_value='OrderReminderFor1And7Days' WHERE param_name='reportName' AND job_id=@jobId;


