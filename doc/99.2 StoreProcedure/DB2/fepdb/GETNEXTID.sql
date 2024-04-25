CREATE OR REPLACE PROCEDURE DBO.GETNEXTID (
 IN  v_serialName VARCHAR,
 OUT v_nextId     BIGINT,
 OUT v_format     VARCHAR)
BEGIN
DECLARE c1 CURSOR FOR
      SELECT NEXTID,NUMBERFORMAT FROM SYSCOMSERIAL WHERE SERIALNAME = v_serialName;
BEGIN
  Open c1;
  FETCH c1 INTO v_nextId,v_format;
  If c1%NOTFOUND Then
     Begin
        INSERT INTO SYSCOMSERIAL (SERIALNAME) VALUES (v_serialName);
     End;
  End if;
  Begin
     UPDATE SYSCOMSERIAL
            SET NEXTID = NVL(NEXTID,0) + 1,
                NUMBERFORMAT=v_format
      WHERE SERIALNAME=v_serialName;
  End;
  Commit;
EXCEPTION
  WHEN OTHERS THEN
    dbms_output.put_line('Store Procedure:GETNEXTID' || ' - ' || SQLCODE);
End