SET @id = (select id from m_code where code_name='command');
INSERT IGNORE INTO m_code_value VALUES (null,@id,'Reboot',0);
INSERT IGNORE INTO m_code_value VALUES (null,@id,'Reload',0);
