#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000105 : ATM要求換PPK key，由DES隨機產生新PPK key並更新Key-File             */
/*----------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000105"，"1T3PPK ATM   "+ATM_ID+"  "，""，""，Return_data_1，""，Return_Code) 
Input Parameters:
ATM_ID = ATM的ID後面補空白到共八位

Output Parameters:
Return_data_1="0016"+E(PPK)(H16)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'05'  */
	char	atmid[8] ;		/*  atmid */
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;
}	fn105i_t, *pfn105i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	ppk[48] ;		/*  PPK encrypted under LMK    */
	char	kek[48] ;		/*  kek encrypted under TMK    */
}	fn105o_t, *pfn105o_t ;

int fn105( char* out, char* in ) 
{
   char cmds[512], AtmID[10];
   pfn105i_t i105 = (pfn105i_t) in ;
   pfn105o_t o105 = (pfn105o_t) out ;

   CMDIA *hc = (CMDIA *) cmds ;
   CMDIB *hd = (CMDIB *) cmds ;

   memset( AtmID, 0, sizeof(AtmID) ) ;
   memcpy( AtmID, i105->atmid, 8 ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   if( i105->keytype == 0x01 ){
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, i105->key, 16 ) ;
       HSMLogMSG( "FN000105;", cmds, NULL ) ;
       if( o105->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000105;", cmds, NULL ) ;
	       return( o105->rc ) ;
	   }
       memcpy( o105->ppk, hd->kekey+16, 16 ) ;
       memcpy( o105->kek, hd->kekey, 16 ) ;
   }

   if( i105->keytype == 0x02 ){
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, "U", 1 ) ;
       memcpy( hc->kek+1, i105->key, 32 ) ;
       memcpy( hc->kek+33, ";XU0", 4 ) ;
       HSMLogMSG( "FN000105;", cmds, NULL ) ;
       if( o105->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000105;", cmds, NULL ) ;
	       return( o105->rc ) ;
	   }
       memcpy( o105->ppk, hd->kekey+34, 32 ) ;
       memcpy( o105->kek, hd->kekey+1, 32 ) ;
   }

   if( i105->keytype == 0x03 ){
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, "T", 1 ) ;
       memcpy( hc->kek+1, i105->key, 48 ) ;
       memcpy( hc->kek+49, ";YT0", 4 ) ;
       HSMLogMSG( "FN000105;", cmds, NULL ) ;
       if( o105->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000105;", cmds, NULL ) ;
	       return( o105->rc ) ;
	   }
       memcpy( o105->ppk, hd->kekey+50, 48 ) ;
       memcpy( o105->kek, hd->kekey+1, 48 ) ;
   }
   HSMLogMSG( "FN000105;", cmds, NULL ) ;
   return( o105->rc ) ;
}

