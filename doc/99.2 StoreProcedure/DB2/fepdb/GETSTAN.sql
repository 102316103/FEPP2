CREATE OR REPLACE PROCEDURE DBO.GETSTAN (
    OUT P_ID	INTEGER,
    OUT P_INTERVAL	INTEGER )
BEGIN
set p_id = 0;
--set p_interval = 0;

Update STAN set STAN=STAN + INTERVAL;
select STAN  , INTERVAL into p_id, p_interval from STAN ;	
	if(p_id>99999000) then
    set p_id = 1;
	  Update STAN set STAN=p_id, interval=p_interval;    
  end if;
END