#include "suip.h"
#include "hsmapi.h"
/*---------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000104 : 他行要求與本行換RM之MAC key時，將新key及KeySyncCheckValue存入pending 欄位     */
/*---------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000104"，"1T2CDK RMR   "+Bank_ID+"  "，
"0016"+Ekcd(kMAC)(H16)，"0006"+發信單位代號(N3)+收信單位代號(N3)，""，""，Return_Code)

(single DES)
Input_data_1 ="0024"+Ekcd(kMAC)(H16)+KeySyncCheckValue(H8)
(3_DES 2_length)
Input_data_1 ="0040"+Ekcd(kMAC)(H32)+KeySyncCheckValue (H8)
(3_DES 3_length)
Input_data_1 ="0056"+Ekcd(kMAC)(H48)+KeySyncCheckValue (H8)
Input_data_2 ="0006"+發信單位代號(N3)+收信單位代號(N3) 
*/
typedef struct	{
	char	f ;				/* FUNCTION CODE   */
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    zmk[48] ;
	char    len1[4] ;
	char	src_bnkid[3] ;	/* SOURCE BANK */
	char	des_bnkid[3] ;	/* DESTINATION BANK */
	char    len2[4] ;
	char	Ekcd[48] ;		/* NEW MAC KEY ENCIPHER BY C.D. KEY */
}	fn104i_t, *pfn104i_t ;

typedef struct	{
	int 	rc ;				/* RETURN CODE                      */
	char    lmkey[48] ;
}	fn104o_t, *pfn104o_t ;

int fn104( char* out, char* in ) 
{
   char cmds[512], sync_r[10] ;
   int len1, len2 ;
   pfn104i_t i104 = (pfn104i_t) in ;
   pfn104o_t o104 = (pfn104o_t) out ;

   CMDSC_t *tsc = (CMDSC_t *) cmds ;
   CMDSC *sc = (CMDSC *) cmds ;
   CMDSD *sd = (CMDSD *) cmds ;


   len1 = ldrint(i104->len1, 4) ;
   if( len1 != 6 ) return( o104->rc = 81 ) ;
   len2 = ldrint(i104->len2, 4) ;
   if( len2 > 40 ) return( o104->rc = 81 ) ;

   memset( sync_r, 0, sizeof(sync_r) ) ;
   memset( cmds, 0, sizeof(cmds) ) ;
   if( i104->keytype == 0x01 ){
       memcpy( sc->cmd, "SC", 2 ) ;
       memcpy( sc->lmkpair, "14", 2 ) ;
       memcpy( sc->zmk, i104->zmk, 16 ) ;
       memcpy( sc->zmkey, i104->Ekcd, 16 ) ;
       memcpy( sync_r, i104->Ekcd+16, 8 ) ;
       memset( sc->numn, '0', 48 ) ;
       memcpy( sc->data1, i104->src_bnkid, 6 ) ;
       HSMLogMSG( "FN000104;", cmds, NULL ) ;
       o104->rc = hsmio(cmds, 0) ;
       if(  o104->rc != 0 && o104->rc != 11 ) {
           HSMLogMSG( "FN000104;", cmds, NULL ) ;
	       return( o104->rc ) ;
	   }
       HSMLogMSG( "FN000104;", cmds, NULL ) ;
       memcpy( o104->lmkey, sd->lmkey, 16 ) ;
       memcpy( o104->lmkey+16, sd->lmkey+48, 16 ) ;
       if( memcmp( sd->lmkey+48, sync_r, 8 ) )
	       return( o104->rc = 103 ) ;
   }
   if( i104->keytype == 0x02 ){
       memcpy( tsc->cmd, "SC", 2 ) ;
       memcpy( tsc->lmkpair, "14", 2 ) ;
       tsc->zmksz = 'U' ;
       memcpy( tsc->zmk, i104->zmk, 32 ) ;
       tsc->zmkeysz = 'X' ;
       memcpy( tsc->zmkey, i104->Ekcd, 32 ) ;
       memcpy( sync_r, i104->Ekcd+32, 8 ) ;
       memset( tsc->numn, '0', 48 ) ;
       memcpy( tsc->data1, i104->src_bnkid, 6 ) ;
       HSMLogMSG( "FN000104;", cmds, NULL ) ;
       o104->rc = hsmio(cmds, 0) ;
       if(  o104->rc != 0 && o104->rc != 11 ) {
           HSMLogMSG( "FN000104;", cmds, NULL ) ;
	       return( o104->rc ) ;
	   }
       HSMLogMSG( "FN000104;", cmds, NULL ) ;
       memcpy( o104->lmkey, sd->lmkey+1, 32 ) ;
       memcpy( o104->lmkey+32, sd->lmkey+65, 16 ) ;
       if( memcmp( sd->lmkey+65, sync_r, 8 ) )
	       return( o104->rc = 103 ) ;
   }
   return( o104->rc = 0 ) ;
}
