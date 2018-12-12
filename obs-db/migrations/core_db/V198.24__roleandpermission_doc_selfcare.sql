SET @PID=(SELECT id FROM m_permission WHERE code='READ_DOCUMENT');
SET @RID=(SELECT id FROM m_role WHERE name='selfcare');
INSERT IGNORE INTO m_role_permission VALUES(@RID,@PID);
