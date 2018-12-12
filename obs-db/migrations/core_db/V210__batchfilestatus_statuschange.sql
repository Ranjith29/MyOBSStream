alter table b_evo_batchprocess 
change column is_uploaded is_uploaded CHAR(1) NULL DEFAULT 'N',
change column is_downloaded is_downloaded CHAR(1) NULL DEFAULT 'N',
change column status status CHAR(1) NULL DEFAULT 'W';
