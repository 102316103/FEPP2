#include "suip.h"
#include "hsmapi.h"
/*------------------------------------------------------------------------*/
/*  FUNCTION CODE FN00801 :  由DES隨機產生新CD Key, CDK_KCV, MAC Key, MAC_KCV  */
/*------------------------------------------------------------------------*/
/*

DES_LIB("FN000801"，"1S1PPK ATM   "+ATM_ID"，"詳: Input_data_1"，"N/A"，Return_data_1，Return_data_2，Return_Code)
Input Parameters:
key_identify =1T3PPK ATM   "+ATM_ID

Input_data_2 =N/A

Output Parameters:
(3_DES 3_length)
Return_data_1="0064"+CD-key(H48)+KeyCheckValue(H16)
Return_data_2="0064"+MAC-key(H48)+KeyCheckValue(H16)
*/


typedef struct	{
	char	f ;			/* FUNCTION CODE X'08'  */
	char	atmid[8] ;		/*  atmid */
	char    keytype ;               /* 0x00 - Master Key, 0x01 - MAC KEY */
	char    key[32] ;               /* Single Des TMK */
}	fn801i_t, *pfn801i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char    key_lmk[16] ;           /* key encrypted under LMK */
	char	key[16] ;		/*  key encrypted under TMK    */
	char	kcv[16] ;		/*  key kcv    */
}	fn801o_t, *pfn801o_t ;


int fn801( char* out, char* in ) 
{
   int kid  ;
   char cmds[512], lmkey[50], mack[50], icv[20], data[20] ;
   pfn801i_t i801 = (pfn801i_t) in ;
   pfn801o_t o801 = (pfn801o_t) out ;

   CMDIA *hc = (CMDIA *) cmds ;
   CMDIB *hd = (CMDIB *) cmds ;

   memset( lmkey, 0, sizeof(lmkey) ) ;
   memset( mack, 0, sizeof(mack) ) ;
   memset( icv, 0, sizeof(icv) ) ;
   memset( data, 0, sizeof(data) ) ;

   kid = i801->keytype ;

   if( kid != 0x00 && kid != 0x01 ) return( 15 ) ;

   memset( cmds, 0, sizeof(cmds) ) ; 
   if( kid == 0x00 ) {                              /* Generate Master Key */
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, "U", 1 ) ; 
       memcpy( hc->kek+1, i801->key, 32 ) ;
       memcpy( hc->kek+33, ";XU0", 4 ) ;
   }
   else{
       memcpy( hc->cmd, "HA", 2 ) ;
       memcpy( hc->kek, i801->key, 16 ) ;
   }
   HSMLogMSG( "FN000801;", cmds, NULL ) ;
   if( o801->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000801;", cmds, NULL ) ;
       return( o801->rc ) ;
   }
   HSMLogMSG( "FN000801;", cmds, NULL ) ;
   if( kid == 0x00 ) {                              /* Generate Master Key */
       memcpy( lmkey, hd->kekey+33, 33 ) ;
       memcpy( o801->key_lmk, hd->kekey+34, 32 ) ;
       memcpy( o801->key+16, hd->kekey+1, 32 ) ;
   }
   else{
       memcpy( lmkey, hd->kekey+16, 16 ) ;           /*  the key encrypted under LMK */ 
       memcpy( o801->key_lmk, hd->kekey+16, 16 ) ;   /*  the key encrypted under LMK */ 
       memcpy( o801->key, hd->kekey, 16) ;
   }

   memset( icv, '0', 16 ) ;
   memset( data, '0', 16 ) ;

   memset( cmds, 0, sizeof(cmds) ) ;            
   memcpy( cmds, "SE", 2 ) ;
   if( kid == 0x00 )   strcat( cmds, "14" ) ;    /* Generate PPK KEY SYNC */ 
   if( kid == 0x01 )   strcat( cmds, "16" ) ;    /* Generate MAC KEY SYNC */
   strcat( cmds, lmkey ) ;
   strcat( cmds, icv ) ;
   strcat( cmds, data ) ;
   HSMLogMSG( "FN000801;", cmds, NULL ) ;
   if( o801->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000801;",cmds, NULL ) ;
       return( o801->rc ) ;
   }

   if( kid == 0x00 )                               /* Generate Master Key */
       memcpy( o801->kcv+32, cmds+4, 16) ;
   else
       memcpy( o801->kcv, cmds+4, 16) ;

   HSMLogMSG( "FN000801;", cmds, NULL ) ;
   return( o801->rc ) ;
}

