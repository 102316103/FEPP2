Create or REPLACE Procedure GETEJ
(
OUT p_id int,
OUT p_interval int
)
BEGIN
set p_id = 0;
--set p_interval = 0;

Update EJFNO set EJFNO=EJFNO + INTERVAL;
select EJFNO  , INTERVAL into p_id, p_interval from EJFNO ;	
	if(p_id>99999000) then
    set p_id = 1;
	  Update EJFNO set EJFNO=p_id, interval=p_interval;    
  end if;
END