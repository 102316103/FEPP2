#include "suip.h"
#include "hsmapi.h"
/*-----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000127 : 端末機認證IC金融卡(Terminal_Authentication)       */
/*-----------------------------------------------------------------------------*/
/*
DES_LIB("FN000127"，"1T3ICC C3    807     "，"0064"+DivData(N48)+RandomNumber(H16)，""，Return_data_1，""，Return_Code)

Input Parameters:
Input_data_1="0064"+DivData(N48)+RandomNumber(H16)

Output Parameters:
Return_data_1="0016"+TerminalAuthCode(H16)
*/
typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    ickey[48] ;
    char    len[4] ;
	char    iccid[16] ;
	char    ran[16] ;
}	fn127i_t, *pfn127i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	authcode[16] ;		/*  TerminalAuthCode(H16)    */
}	fn127o_t, *pfn127o_t ;

int fn127( char* out, char* in ) 
{
   char cmds[512] ;
   pfn127i_t i127 = (pfn127i_t) in ;
   pfn127o_t o127 = (pfn127o_t) out ;

   CMDTQ *tq = (CMDTQ *) cmds ;
   CMDTR *tr = (CMDTR *) cmds ;


/* Generate Terminal Authentication Data for IC card */

   memset( cmds, 0, sizeof(cmds) ) ;
   if( i127->keytype == 0x01 ){
       memcpy( tq->cmd, "TQ", 2 ) ;
       memcpy( tq->cmd+2, i127->ickey, 16 ) ; 
       memcpy( tq->cmd+18, i127->iccid, 16 ) ;
       memcpy( tq->cmd+34, i127->ran, 16 ) ;
       HSMLogMSG( "FN000127;", cmds, NULL ) ;
       if( o127->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000127;", cmds, NULL ) ;
	       return(o127->rc) ;
	   }
   }
   if( i127->keytype == 0x02 ){
       memcpy( tq->cmd, "TQ", 2 ) ;
       tq->ksz ='U' ;
       memcpy( tq->tmk, i127->ickey, 32 ) ; 
       memcpy( tq->tmk+32, i127->iccid, 16 ) ;
       memcpy( tq->tmk+48, i127->ran, 16 ) ;
       HSMLogMSG( "FN000127;", cmds, NULL ) ;
       if( o127->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000127;", cmds, NULL ) ;
	       return(o127->rc) ;
	   }
   }
   if( i127->keytype == 0x03 ){
       memcpy( tq->cmd, "TQ", 2 ) ;
       tq->ksz ='T' ;
       memcpy( tq->tmk, i127->ickey, 48 ) ; 
       memcpy( tq->iccid, i127->iccid, 16 ) ;
       memcpy( tq->authdata, i127->ran, 16 ) ;
       HSMLogMSG( "FN000127;", cmds, NULL ) ;
       if( o127->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000127;", cmds, NULL ) ;
	       return(o127->rc) ;
	   }
   }
   memcpy( o127->authcode, tr->authcode, 16 ) ;

   HSMLogMSG( "FN000127;", cmds, NULL ) ;
   return( o127->rc) ;
}

