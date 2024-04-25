#include "suip.h"
#include "hsmapi.h"
/*--------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000805 : ATM要求換TAK MAC key，由DES隨機產生新TAK key並更新Key-File, 使用FN313押 MAC  */
/*--------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000805"，"1T3MAC ATM   "+ATM_ID+"  "，""，""，Return_data_1，""，Return_Code) 
Input Parameters:
ATM_ID = ATM的ID後面補空白到共八位

Output Parameters:
Return_data_1="0064"+MACKEY(H48)+KCV(H16)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char	atmid[8] ;		/*  atmid */
	char    keytype ;
	char    key[48] ;
}	fn805i_t, *pfn805i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	key_lmk[48] ;	/*  MAC KEY encrypted under lmk */
	char	key[48] ;		/*  MAC KEY encrypted under CDK */
	char	kcv[16] ;		/*  KCV */
}	fn805o_t, *pfn805o_t ;

int fn805( char* out, char* in ) 
{
   int kid ;
   char cmds[512], AtmID[10], ctmk[50], lmkey[50], icv[20], data[20];
   pfn805i_t i805 = (pfn805i_t) in ;
   pfn805o_t o805 = (pfn805o_t) out ;

   CMDIA *hc = (CMDIA *) cmds ;
   CMDIB *hd = (CMDIB *) cmds ;

   memset( AtmID, 0, sizeof(AtmID) ) ;
   memset( ctmk, 0, sizeof(ctmk) ) ;
   memset( lmkey, 0, sizeof(lmkey) ) ;
   memset( icv, 0, sizeof(icv) ) ;
   memset( data, 0, sizeof(data) ) ;

   memcpy( AtmID, i805->atmid, 8 ) ;

   kid = i805->keytype ;

   memset( cmds, 0, sizeof(cmds) ) ;
   if( kid == 0x01 ){
       memcpy( hc->cmd, "HA", 2 ) ;
       memcpy( hc->kek, i805->key, 16 ) ;
       HSMLogMSG( "FN000805;", cmds, NULL ) ;
       if( o805->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000805;", cmds, NULL ) ;
	       return( o805->rc ) ;
	   }
       memcpy( lmkey, hd->kekey+16, 16 ) ;
       memcpy( o805->key_lmk, hd->kekey+16, 16 ) ;
       memcpy( o805->key_lmk+16, hd->kekey, 16 ) ;
   }
   if( kid == 0x02 ){
       memcpy( hc->cmd, "HA", 2 ) ;
       memcpy( hc->kek, "U", 1 ) ;
       memcpy( hc->kek+1, i805->key, 32 ) ;
       memcpy( hc->kek+33, ";XU0", 4 ) ;
       HSMLogMSG( "FN000805;", cmds, NULL ) ;
       if( o805->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000805;", cmds, NULL ) ;
	       return( o805->rc ) ;
	   }
       memcpy( lmkey, hd->kekey+34, 32 ) ;
       memcpy( o805->key_lmk, hd->kekey+34, 32 ) ;
       memcpy( o805->key_lmk+32, hd->kekey+1, 32 ) ;
   }
   if( kid == 0x03 ){
       memcpy( hc->cmd, "HA", 2 ) ;
       memcpy( hc->kek, "T", 1 ) ;
       memcpy( hc->kek+1, i805->key, 48 ) ;
       memcpy( hc->kek+49, ";YT0", 4 ) ;
       HSMLogMSG( "FN000805;", cmds, NULL ) ;
       if( o805->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000805;", cmds, NULL ) ;
	       return( o805->rc ) ;
	   }
       memcpy( lmkey, hd->kekey+50, 48 ) ;
       memcpy( o805->key_lmk, hd->kekey+50, 48 ) ;
       memcpy( o805->key, hd->kekey+1, 48 ) ;
   }

   memset( icv, '0', 16 ) ;
   memset( data, '0', 16 ) ;

   memset( cmds, 0, sizeof(cmds) ) ; /* Generate MAC KEY SYNC */ 
   strcpy( cmds, "SE" ) ;
   if( kid == 0x01 )   strcat( cmds, "16" ) ;
   if( kid == 0x02 )   strcat( cmds, "16U" ) ;
   if( kid == 0x03 )   strcat( cmds, "16T" ) ;
   strcat( cmds, lmkey ) ;
   strcat( cmds, icv ) ;
   strcat( cmds, data ) ;
   HSMLogMSG( "FN000805;", cmds, NULL ) ;
   if( o805->rc = hsmio(cmds, 0) ){
	   return( o805->rc ) ;
   }

   if( kid == 0x01 )
       memcpy( o805->key_lmk+32, cmds+4, 16) ;
   if( kid == 0x02 )
       memcpy( o805->key_lmk+64, cmds+4, 16) ;
   if( kid == 0x03 )
       memcpy( o805->kcv, cmds+4, 16) ;

/*   UpdTCPKey( lmkey, AtmID, 0x03 ) ; */

   HSMLogMSG( "FN000805;", cmds, NULL ) ;
   return( o805->rc ) ;
}

