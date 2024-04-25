#include "suip.h"
#include "hsmapi.h"

typedef struct	{
	char	f ;                     /* FUNCTION CODE X'08'  */
	char	atmid[8] ;              /*  atmid */
	char    keytype ;               /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char	tmk[48] ;               /*  TMK */
}	fn108i_t, *pfn108i_t ;

typedef struct	{
	int 	rc ;                    /* RETURN CODE  */
	char    ppks_lmk[16] ;          /*  ppks pair 14 encrypted under LMK */
	char	ppks[16] ;              /*  PPK encrypted under TMK    */
	char	ppkskcv[16] ;           /*  PPK kcv    */
	char    ppk3_lmk[48] ;          /*  PPk pair 14 encrypted under LMK */
	char	ppk3[48] ;              /*  PPK encrypted under TMK    */
	char	ppk3kcv[16] ;           /*  PPK kcv    */
	char    mack3_lmk[48] ;         /*  mack key pair 16 encrypted under LMK */
	char	mack3[48] ;             /*  mack  encrypted under TMK    */
	char	mack3kcv[16] ;          /*  mack kcv    */
}	fn108o_t, *pfn108o_t ;


int fn108( char* out, char* in )
{
   int kid ;
   char cmds[512], tmk[50], ctmk[50], lmkey[50], AtmID[10], ppks[20], ppk3[50], mack3[50], icv[20], data[20] ;
   pfn108i_t i108 = (pfn108i_t) in ;
   pfn108o_t o108 = (pfn108o_t) out ;

   CMDIA *hc = (CMDIA *) cmds ;
   CMDIB *hd = (CMDIB *) cmds ;

   memset( AtmID, 0, sizeof(AtmID) ) ;
   memset(tmk, 0, sizeof(tmk) ) ;
   memset( ctmk, 0, sizeof(ctmk) ) ;
   memset( lmkey, 0, sizeof(lmkey) ) ;
   memset( ppks, 0, sizeof(ppks) ) ;
   memset( ppk3, 0, sizeof(ppk3) ) ;
   memset( mack3, 0, sizeof(mack3) ) ;
   memset( icv, 0, sizeof(icv) ) ;
   memset( data, 0, sizeof(data) ) ;

   memcpy( AtmID, i108->atmid, 8 ) ;
   kid = i108->keytype ;

   if( kid == 0x01 )  memcpy( tmk, i108->tmk, 16 ) ;
   if( kid == 0x02 )  memcpy( tmk, i108->tmk, 32 ) ;
   if( kid == 0x03 )  memcpy( tmk, i108->tmk, 48 ) ;

   memset( cmds, 0, sizeof(cmds) ) ; /* Generate PPK */

   if( kid == 0x01 ){
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, tmk, 16 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
       }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( ppks, hd->kekey+16, 16 ) ;
       memcpy( o108->ppks_lmk, hd->kekey+16, 16 ) ;
       memcpy( o108->ppks, hd->kekey, 16 ) ;

       memset( cmds, 0, sizeof(cmds) ) ; /* Generate PPK */
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, tmk, 16 ) ;
       memcpy( hc->kek+16, ";YT0", 4 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
       }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( ppk3, hd->kekey+49, 49 ) ;
       memcpy( o108->ppk3_lmk, hd->kekey+50, 48 ) ;
       memcpy( o108->ppk3, hd->kekey+1, 48 ) ;

       memset( cmds, 0, sizeof(cmds) ) ; /* Generate TAK */
       memcpy( hc->cmd, "HA", 2 ) ;                   /* Generate TAK, Generate MAC use FN313 */
       memcpy( hc->kek, tmk, 16 ) ;
       memcpy( hc->kek+16, ";YT0", 4 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
       }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( mack3, hd->kekey+49, 49 ) ;
       memcpy( o108->mack3_lmk, hd->kekey+50, 48 ) ;
       memcpy( o108->mack3, hd->kekey+1, 48 ) ;
   }

   if( kid == 0x02 ){
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, "U", 1 ) ;
       memcpy( hc->kek+1, tmk, 32 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
       }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( ppks, hd->kekey+16, 16 ) ;
       memcpy( o108->ppks_lmk, hd->kekey+16, 16 ) ;
       memcpy( o108->ppks, hd->kekey, 16 ) ;

       memset( cmds, 0, sizeof(cmds) ) ; /* Generate PPK */
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, "U", 1 ) ;
       memcpy( hc->kek+1, tmk, 32 ) ;
       memcpy( hc->kek+33, ";YT0", 4 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
       }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( ppk3, hd->kekey+49, 49 ) ;
       memcpy( o108->ppk3_lmk, hd->kekey+50, 48 ) ;
       memcpy( o108->ppk3, hd->kekey+1, 48 ) ;

       memset( cmds, 0, sizeof(cmds) ) ; /* Generate TAK */
       memcpy( hc->cmd, "HA", 2 ) ;                   /* Generate TAK, Generate MAC use FN313 */
       memcpy( hc->kek, "U", 1 ) ;
       memcpy( hc->kek+1, tmk, 32 ) ;
       memcpy( hc->kek+33, ";YT0", 4 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
       }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( mack3, hd->kekey+49, 49 ) ;
       memcpy( o108->mack3_lmk, hd->kekey+50, 48 ) ;
       memcpy( o108->mack3, hd->kekey+1, 48 ) ;
   }
   if( kid == 0x03 ){
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, "T", 1 ) ;
       memcpy( hc->kek+1, tmk, 48 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
       }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( ppks, hd->kekey+16, 16 ) ;
       memcpy( o108->ppks_lmk, hd->kekey+16, 16 ) ;
       memcpy( o108->ppks, hd->kekey, 16 ) ;

       memset( cmds, 0, sizeof(cmds) ) ; /* Generate PPK */
       memcpy( hc->cmd, "HC", 2 ) ;
       memcpy( hc->kek, "T", 1 ) ;
       memcpy( hc->kek+1, tmk, 48 ) ;
       memcpy( hc->kek+49, ";YT0", 4 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
           return( o108->rc ) ;
       }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( ppk3, hd->kekey+49, 49 ) ;
       memcpy( o108->ppk3_lmk, hd->kekey+50, 48 ) ;
       memcpy( o108->ppk3, hd->kekey+1, 48 ) ;

       memset( cmds, 0, sizeof( cmds) ) ;   /* Generate TAK */
       memcpy( hc->cmd, "HA", 2 ) ;                       /* Generate TAK, Generate MAC use FN313 */
       memcpy( hc->kek, "T", 1 ) ;
       memcpy( hc->kek+1, tmk, 48 ) ;
       memcpy( hc->kek+49, ";YT0", 4 ) ;
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       if( o108->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000108;", cmds, NULL ) ;
	       return( o108->rc ) ;
	   }
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
       memcpy( mack3, hd->kekey+49, 49 ) ;
       memcpy( o108->mack3_lmk, hd->kekey+50, 48 ) ;
       memcpy( o108->mack3, hd->kekey+1, 48 ) ;
   }

/*  For TCB Bank SYNC */
   memset( icv, '0', 16 ) ;
   memset( data, '0', 16 ) ;
/* 
   For Sinopack Bank SYNC 
   memcpy( data, AtmID+3, 5 ) ;
*/

   memset( cmds, 0, sizeof(cmds) ) ; /* Generate PPKS SYNC */
   strcpy( cmds, "SE" ) ;
   strcat( cmds, "14" ) ;
   strcat( cmds, ppks ) ;
   strcat( cmds, icv ) ;
   strcat( cmds, data ) ;
   HSMLogMSG( "FN000108;", cmds, NULL ) ;
   if( o108->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
   }
   memcpy( o108->ppkskcv, cmds+4, 16) ;

   memset( cmds, 0, sizeof(cmds) ) ; /* Generate PPK3 SYNC */
   strcpy( cmds, "SG" ) ;
   strcat( cmds, "14" ) ;
   strcat( cmds, ppk3 ) ;
   strcat( cmds, icv ) ;
   strcat( cmds, data ) ;
   strcat( cmds, ";" ) ;
   strcat( cmds, "16" ) ;           /* Generate MAC3 pair 16 KEY SYNC */
   strcat( cmds, mack3 ) ;
   strcat( cmds, icv ) ;
   strcat( cmds, data ) ;
   HSMLogMSG( "FN000108;", cmds, NULL ) ;
   if( o108->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000108;", cmds, NULL ) ;
	   return( o108->rc ) ;
   }
   memcpy( o108->ppk3kcv, cmds+4, 16) ;
   memcpy( o108->mack3kcv, cmds+20, 16) ;

   HSMLogMSG( "FN000108;", cmds, NULL ) ;
   return( o108->rc ) ;
}

