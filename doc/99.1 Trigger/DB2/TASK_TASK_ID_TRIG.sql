CREATE OR REPLACE TRIGGER "TASK_TASK_ID_TRIG"
  BEFORE INSERT ON "TASK"
  REFERENCING NEW AS "NEW"
  FOR EACH ROW
BEGIN
  DECLARE V_NEWVAL INT DEFAULT 0;
  DECLARE V_INCVAL INT DEFAULT 0;
  SELECT NEXTVAL FOR TASK_TASK_ID_SEQ INTO V_NEWVAL FROM SYSIBM.SYSDUMMY1;
  -- If this is the first time this table have been inserted into (sequence == 1)
  IF V_NEWVAL = 1 THEN
    --get the max indentity value from the table
    SELECT NVL(MAX(TASK_ID), 0) INTO V_NEWVAL FROM TASK;
    SET V_NEWVAL = V_NEWVAL + 1;
    --set the sequence to that value
    FETCH_LOOP :
    LOOP 
      IF V_INCVAL >= V_NEWVAL THEN
        LEAVE FETCH_LOOP;
      END IF;
      SELECT NEXTVAL FOR TASK_TASK_ID_SEQ INTO V_INCVAL FROM SYSIBM.SYSDUMMY1;
     END LOOP FETCH_LOOP;
  END IF;
  -- assign the value from the sequence to emulate the identity column
  SET NEW.TASK_ID = V_NEWVAL;
END;
