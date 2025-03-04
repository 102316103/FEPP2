DELETE FROM SYSCONF WHERE SYSCONF_SUBSYSNO = 1 AND SYSCONF_NAME = 'TO_FISC_PROTOCOL';
DELETE FROM SYSCONF WHERE SYSCONF_SUBSYSNO = 1 AND SYSCONF_NAME = 'TO_FISC_HOST';
DELETE FROM SYSCONF WHERE SYSCONF_SUBSYSNO = 1 AND SYSCONF_NAME = 'TO_FISC_PORT';
DELETE FROM SYSCONF WHERE SYSCONF_SUBSYSNO = 2 AND SYSCONF_NAME = 'TO_FISC_PROTOCOL';
DELETE FROM SYSCONF WHERE SYSCONF_SUBSYSNO = 2 AND SYSCONF_NAME = 'TO_FISC_HOST';
DELETE FROM SYSCONF WHERE SYSCONF_SUBSYSNO = 2 AND SYSCONF_NAME = 'TO_FISC_PORT';

INSERT INTO SYSCONF
  (SYSCONF_SUBSYSNO,
   SYSCONF_NAME,
   SYSCONF_DATATYPE,
   SYSCONF_TYPE,
   SYSCONF_VALUE,
   SYSCONF_READONLY,
   SYSCONF_FREQ,
   SYSCONF_REMARK,
   UPDATE_USERID,
   UPDATE_TIME,
   SYSCONF_ENCRYPT)
VALUES
  ('1',
   'TO_FISC_PROTOCOL',
   'varchar',
   'Queue',
   'restful',
   '1',
   NULL,
   'AA Service送交易給財金用的Protocol, restful/socket',
   '0',
   CURRENT_TIMESTAMP,
   '0');

INSERT INTO SYSCONF
  (SYSCONF_SUBSYSNO,
   SYSCONF_NAME,
   SYSCONF_DATATYPE,
   SYSCONF_TYPE,
   SYSCONF_VALUE,
   SYSCONF_READONLY,
   SYSCONF_FREQ,
   SYSCONF_REMARK,
   UPDATE_USERID,
   UPDATE_TIME,
   SYSCONF_ENCRYPT)
VALUES
  ('1',
   'TO_FISC_HOST',
   'varchar',
   'Queue',
   'localhost',
   '1',
   NULL,
   'AA Service送交易給財金用的Host',
   '0',
   CURRENT_TIMESTAMP,
   '0');

INSERT INTO SYSCONF
  (SYSCONF_SUBSYSNO,
   SYSCONF_NAME,
   SYSCONF_DATATYPE,
   SYSCONF_TYPE,
   SYSCONF_VALUE,
   SYSCONF_READONLY,
   SYSCONF_FREQ,
   SYSCONF_REMARK,
   UPDATE_USERID,
   UPDATE_TIME,
   SYSCONF_ENCRYPT)
VALUES
  ('1',
   'TO_FISC_PORT',
   'int',
   'Queue',
   '18091',
   '1',
   NULL,
   'AA Service送交易給財金用的Port',
   '0',
   CURRENT_TIMESTAMP,
   '0');

INSERT INTO SYSCONF
  (SYSCONF_SUBSYSNO,
   SYSCONF_NAME,
   SYSCONF_DATATYPE,
   SYSCONF_TYPE,
   SYSCONF_VALUE,
   SYSCONF_READONLY,
   SYSCONF_FREQ,
   SYSCONF_REMARK,
   UPDATE_USERID,
   UPDATE_TIME,
   SYSCONF_ENCRYPT)
VALUES
  ('2',
   'TO_FISC_PROTOCOL',
   'varchar',
   'Queue',
   'restful',
   '1',
   NULL,
   'AA Service送交易給財金用的Protocol, restful/socket',
   '0',
   CURRENT_TIMESTAMP,
   '0');

INSERT INTO SYSCONF
  (SYSCONF_SUBSYSNO,
   SYSCONF_NAME,
   SYSCONF_DATATYPE,
   SYSCONF_TYPE,
   SYSCONF_VALUE,
   SYSCONF_READONLY,
   SYSCONF_FREQ,
   SYSCONF_REMARK,
   UPDATE_USERID,
   UPDATE_TIME,
   SYSCONF_ENCRYPT)
VALUES
  ('2',
   'TO_FISC_HOST',
   'varchar',
   'Queue',
   'localhost',
   '1',
   NULL,
   'AA Service送交易給財金用的Host',
   '0',
   CURRENT_TIMESTAMP,
   '0');

INSERT INTO SYSCONF
  (SYSCONF_SUBSYSNO,
   SYSCONF_NAME,
   SYSCONF_DATATYPE,
   SYSCONF_TYPE,
   SYSCONF_VALUE,
   SYSCONF_READONLY,
   SYSCONF_FREQ,
   SYSCONF_REMARK,
   UPDATE_USERID,
   UPDATE_TIME,
   SYSCONF_ENCRYPT)
VALUES
  ('2',
   'TO_FISC_PORT',
   'int',
   'Queue',
   '18091',
   '1',
   NULL,
   'AA Service送交易給財金用的Port',
   '0',
   CURRENT_TIMESTAMP,
   '0');

