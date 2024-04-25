#include "suip.h"
#include "hsmapi.h"
/*-------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000901 : Translate PPK key, 轉換PPK Key, 驗證PPKSync, 轉換MAC Key, 驗證MACSync  */
/*-------------------------------------------------------------------------------------------------*/
/*
 Translate PPK/MAC key,：

DES_LIB("FN000901"，"1S1PPK ATM   "+ATM_ID"，"詳: Input_data_1"，"N/A"，Return_data_1，Return_data_2，Return_Code)
Input Parameters:
key_identify =1T3PPK ATM   "+ATM_ID
Input_data_1：

(single DES)
Input_data_1="0032"+新PPKkey(H16)+新KeySyncCheckValue(H16)
Input_data_2="0032"+新MACkey(H16)+新KeySyncCheckValue(H16)
Return_data_2=N/A
(3_DES 2_length)
Input_data_1="0048"+新PPKkey(H32)+新KeySyncCheckValue(H16)
Input_data_2="0048"+新MACkey(H32)+新KeySyncCheckValue(H16)
(3_DES 3_length)
Input_data_1="0064"+新PPKkey(H48)+新KeySyncCheckValue(H16)
Input_data_2="0064"+新MACkey(H48)+新KeySyncCheckValue(H16)
*/


typedef struct	{
	char	f ;					/* FUNCTION CODE */
	char    cdktype ;                               /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    cdkey[48] ;                             /* ZCMK */
	char    tmktype ;                               /* atm keytype 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    atmkey[48] ;                            /* Receive from Host key encrypted under cdkey */
	char    ilen[4] ;                               /* 0016 */
	char    input[32] ;                             /* 0060000000000000 */
}	fn901i_t, *pfn901i_t ;

typedef struct	{
	int 	rc ;
	char    mak[48] ;
	char    mac[16] ;
}	fn901o_t, *pfn901o_t ;

int fn901( char* out, char* in ) 
{
   int len, cdksz, atmksz ;
   char cmds[512], temp[512], lmkey[50], pkcv[20], mkcv[20] ;
   pfn901i_t i901 = (pfn901i_t) in ;
   pfn901o_t o901 = (pfn901o_t) out ;

   CMDSC *sc = (CMDSC *) cmds ;
   CMDSD *sd = (CMDSD *) cmds ;

   len = ldrint( i901->ilen, 4 ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memset( temp, 0, sizeof(temp) ) ;

   memcpy( sc->cmd, "SC", 2 ) ;
   memcpy( sc->lmkpair, "14", 2 ) ;    /* pair 14 - TMK,TPK */

   if( i901->cdktype == 0x01 ){
       cdksz = 16 ;
       memcpy( sc->zmk, i901->cdkey, cdksz ) ;
   }
   if( i901->cdktype == 0x02 ){
       cdksz = 32 ;
       memcpy( sc->zmk, "U", 1 ) ;
       memcpy( sc->zmk+1, i901->cdkey, cdksz ) ;
   }
   if( i901->cdktype == 0x03 ){
       cdksz = 48 ;
       memcpy( sc->zmk, "T", 1 ) ;
       memcpy( sc->zmk+1, i901->cdkey, cdksz ) ;
   }

   if( i901->tmktype == 0x01 ){
       atmksz = 16 ;
       memcpy( sc->zmkey, i901->atmkey, atmksz ) ;
   }
   if( i901->tmktype == 0x02 ){
       atmksz = 32 ;
       memcpy( sc->zmk+1+cdksz, "X", 1 ) ;
       memcpy( sc->zmk+2+cdksz, i901->atmkey, atmksz ) ;
   }
   if( i901->tmktype == 0x03 ){
       atmksz = 48 ;
       memcpy( sc->zmk+1+cdksz, "Y", 1 ) ;
       memcpy( sc->zmk+2+cdksz, i901->atmkey, atmksz ) ;
   }

   if( i901->cdktype == 0x01 ){
       memset( sc->numn, '0', 48 ) ;
       memcpy( sc->numn+16, i901->input, 16 ) ;
       memcpy( sc->numn+32, i901->input, 16 ) ;
   }
   else{
       memset( sc->zmk+2+cdksz+atmksz, '0', 48 ) ;
       memcpy( sc->zmk+cdksz+atmksz+18, i901->input, 16 ) ;
       memcpy( sc->zmk+cdksz+atmksz+34, i901->input, 16 ) ;
   }

   memcpy( temp, cmds, strlen(cmds) ) ;
   memcpy( temp, "SC16", 4 ) ;                  /* change from pair 14 - TMK, TPK to pair 16 - TAK for FN313 M6 command */

   HSMLogMSG( "FN000901;", cmds, NULL ) ;
   o901->rc = hsmio(cmds, 0) ;
   if(  o901->rc != 0 && o901->rc != 11 ) {
        HSMLogMSG( "FN000901;",cmds, NULL ) ;
        return( o901->rc ) ;
   }
   HSMLogMSG( "FN000901;", cmds, NULL ) ;

   if( i901->cdktype == 0x01 )
       memcpy( o901->mak, sd->lmkey, atmksz ) ;
   else
       memcpy( o901->mak, sd->lmkey+1, atmksz ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( cmds, temp, strlen(temp) ) ;  /* pair 16 */

   HSMLogMSG( "FN000901;", cmds, NULL ) ;
   o901->rc = hsmio(cmds, 0) ;
   if(  o901->rc != 0 && o901->rc != 11 ) {
        HSMLogMSG( "FN000901;",cmds, NULL ) ;
        return( o901->rc ) ;
   }
   HSMLogMSG( "FN000901;", cmds, NULL ) ;

   if( i901->cdktype == 0x01 ){
       memcpy( o901->mak+atmksz, sd->lmkey, atmksz ) ;
       memcpy( o901->mak+atmksz+atmksz, sd->lmkey+atmksz+32, 16 ) ;
   }
   else{
       memcpy( o901->mak+atmksz, sd->lmkey+1, atmksz ) ;
       memcpy( o901->mak+atmksz+atmksz, sd->lmkey+atmksz+33, 16 ) ;
   }

   *( o901->mak+atmksz+atmksz+16 ) = '\0' ;

   return( o901->rc ) ;
}

