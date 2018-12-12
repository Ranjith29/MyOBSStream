
CREATE TABLE IF NOT EXISTS `oauth_client_details` (
  `client_id` varchar(128) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` BIT(1) NULL DEFAULT NULL,
  PRIMARY KEY (`client_id`)
);

INSERT IGNORE INTO `oauth_client_details` (`client_id`, `client_secret`, `scope`, `authorized_grant_types`) 
VALUES ('app', '123', 'all', 'password,refresh_token');


CREATE TABLE IF NOT EXISTS `oauth_access_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(256) DEFAULT NULL
);


CREATE TABLE IF NOT EXISTS `oauth_refresh_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication` blob
);

Drop procedure IF EXISTS roleDisable; 
DELIMITER //
create procedure roleDisable() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'is_disabled'
     and TABLE_NAME = 'm_role'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE m_role ADD COLUMN `is_disabled` tinyint(1) NOT NULL DEFAULT '0';
END IF;
END //
DELIMITER ;
call roleDisable();
Drop procedure IF EXISTS roleDisable;
