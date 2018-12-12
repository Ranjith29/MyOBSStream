SET @id=(SELECT id FROM m_code WHERE code_name='Problem Code');
INSERT IGNORE INTO m_code_value VALUES(null, @id,'Hardware Sale','4');

SET @id=(SELECT id FROM m_code WHERE code_name='Problem Code');
INSERT IGNORE INTO m_code_value VALUES(null, @id,'Payment','5');
