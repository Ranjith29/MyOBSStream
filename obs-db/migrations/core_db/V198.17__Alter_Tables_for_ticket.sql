DROP PROCEDURE  IF EXISTS ticket_detail_assign;
DELIMITER //
CREATE PROCEDURE ticket_detail_assign() 
BEGIN
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_details'
      AND COLUMN_NAME ='Assign_from') THEN
ALTER TABLE b_ticket_details ADD COLUMN Assign_from VARCHAR(100) DEFAULT NULL AFTER  `assigned_to`;
END IF;
 IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_details'
      AND COLUMN_NAME ='status') THEN
ALTER TABLE b_ticket_details ADD COLUMN status VARCHAR(100) DEFAULT NULL AFTER  `Assign_from`;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_master'
      AND COLUMN_NAME ='issue') THEN
ALTER TABLE b_ticket_master ADD COLUMN issue VARCHAR(250) DEFAULT NULL AFTER  `assigned_to`;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME ='b_ticket_details'
      AND COLUMN_NAME ='username') THEN
ALTER TABLE b_ticket_details ADD COLUMN username VARCHAR(100) DEFAULT NULL AFTER  `status`;
END IF;
END //
DELIMITER ;
call ticket_detail_assign();
DROP PROCEDURE  IF EXISTS ticket_detail_assign;

update b_message_template set subject = 'TICKET',header = 'Dear<b> <customer> </b> As per your request Issue has been created in OBS follwing are the Issue Details<br/><br/>',body = '<b>IssueId:</b>&nbsp;<ticketId><br/><b>Date:</b>&nbsp;<ticketdate><br/><b>Status:</b>&nbsp;<ticketstatus><br/><b>Assigned To:</b>&nbsp;<ticketname><br/><b>Description:</b>&nbsp;<ticketdesc><br/><br/>', footer = '<b>Thanks</b>'WHERE template_description='TICKET_TEMPLATE';

SET @ID=(SELECT id FROM b_message_template WHERE template_description='TICKET_TEMPLATE');
INSERT Ignore INTO `b_message_params` VALUES(null,@ID,'<customer>',1),(null,@ID,'<ticketId>',2),(null,@ID,'<ticketdate>',3),(null,@ID,'<ticketstatus>',4),(null,@ID,'<ticketname>',5),(null,@ID,'<ticketdesc>',6);

INSERT Ignore INTO b_message_template VALUES (null,'EDIT TICKET','TICKET','Dear <b> <username> </b> <br/><br/> <b> Your IssueId :</b><ticketId></br>','<b> Assigned To: </b>  <ticketassign><br/>\n<b>Status is:  </b> <ticketstatus><br/>\n<b>Comment: </b><comment><br/><br/>','<b>Thanks</b>','E',1,'2015-11-25 15:30:44',1,'2015-11-25 17:56:28','N');

SET @ID=(SELECT id FROM b_message_template WHERE template_description='EDIT TICKET');
INSERT Ignore INTO `b_message_params` VALUES(null,@ID,'<username>',1),(null,@ID,'<ticketId>',2),(null,@ID,'<ticketassign>',3),(null,@ID,'<ticketstatus>',4),(null,@ID,'<comment>',5);

INSERT Ignore INTO `b_message_template` VALUES (null,'ClOSE TICKET','TICKET','Dear  <b><customer></b> Your Issue Id <b><ticketId></b> was closed  Following are the Issue Closing details <br/><br/>','<b>Description:</b> <ticketdesc><br/>\n<b>Resolution:</b><resolution>\n<br/><br/>','<b>Thanks</b>','E',1,'2015-11-25 17:02:47',1,'2015-11-25 17:58:13','N');

SET @ID=(SELECT id FROM b_message_template WHERE template_description='ClOSE TICKET');
INSERT Ignore INTO `b_message_params` VALUES(null,@ID,'<customer>',1),(null,@ID,'<ticketId>',2),(null,@ID,'<ticketdesc>',3),(null,@ID,'<resolution>',4),(null,@ID,'<date>',5);

