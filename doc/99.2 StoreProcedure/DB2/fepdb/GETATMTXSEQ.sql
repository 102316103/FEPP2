CREATE OR REPLACE PROCEDURE DBO.GETATMTXSEQ (
    IN  IN_ATMNO	varchar(5),
    OUT OUT_NEXTID	INTEGER )
BEGIN

UPDATE ATMSTAT
    SET ATMSTAT_TX_SEQ = ATMSTAT_TX_SEQ + 1        
	WHERE ATMSTAT_ATMNO=IN_ATMNO;

select ATMSTAT_TX_SEQ into OUT_NEXTID from ATMSTAT WHERE ATMSTAT_ATMNO=IN_ATMNO;	
  if(OUT_NEXTID=100000) then
    --第二階段不需要從90000開始取號
    SET OUT_NEXTID = 1;
    UPDATE ATMSTAT
	     SET ATMSTAT_TX_SEQ = OUT_NEXTID 
	  WHERE ATMSTAT_ATMNO=IN_ATMNO;
  end if;
END