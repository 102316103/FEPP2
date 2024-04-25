#include "suip.h"
#include "hsmapi.h"

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000101 : FISC CHANGE MAC KEY                              */
/*----------------------------------------------------------------------------*/

/*
DES_LIB("FN000101"¡A"2S1MAC OPC   950   1 CDK FISC  950   1 "¡A
"0032"+¢Ókcd(key)(H16)+Ekcd(N)(H16)¡A"0016"+"9500000000000000"¡AReturn_data_1¡AReturn_data_2¡AReturn_Code)

key_identify ="2S1MAC OPC   950   1 CDK FISC  950   1 "
(single_DES)
Input_data_1 ="0032"+Ekcd(key)(H16)+Ekcd(N)(H16) 
(3_DES 2_length)
Input_data_1 ="0048"+Ekcd(key)(H32)+Ekcd(N)(H16) 
(3_DES 3_length)
Input_data_1 ="0064"+Ekcd(key)(H48)+Ekcd(N)(H16)
Input_data_2 ="0016"+"9500000000000000"

Return_data_1="0032"+Ekcd(N+1)(H16)+Ekcd(N+2)(H16)
Return_data_2="0016"+KeySync1(H8)+KeySync2(H8)
*/
typedef struct	{
	char	f ;				/* FUNCTION CODE X'01' */
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char	key[48] ;		
	char    len[4] ;
	char    sync1[8] ;
	char    sync2[8] ;
	char	datalen[4] ;   /* DATA Length */
	char	data[32] ;	   /* MAC DATA (OCCURS 4 TIMES) */
}	fn101i_t, *pfn101i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE                          */
	char	N1[16] ;		/* (N+1) ENCIPHER BY NEW MAC KEY        */
	char	N2[16] ;		/* (N+2) ENCIPHER BY NEW MAC KEY        */
	char    sync[16] ;
	char    KEY[48] ;       /* for unpdate DB */
}	fn101o_t, *pfn101o_t ;

int fn101( char* out, char* in ) 
{
   int len, datlen ;
   char cmds[512], zmk[50], lmkey[50] ;
   pfn101i_t i101 = (pfn101i_t) in ;
   pfn101o_t o101 = (pfn101o_t) out ;

   CMDSC_t *tsc = (CMDSC_t *) cmds ;
   CMDSC *sc = (CMDSC *) cmds ;
   CMDSD *sd = (CMDSD *) cmds ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memset( zmk, 0, sizeof(zmk) ) ;
   memset( lmkey, 0, sizeof(lmkey) ) ;

   len = ldrint(i101->len, 4) ;
   if( len != 16 ) return( o101->rc = 81 ) ;
   datlen = ldrint(i101->datalen, 4) ;
   if( datlen > 48 ) return( o101->rc = 81 ) ;

   if( i101->keytype == 0x01 ){
       memcpy( sc->cmd, "SC", 2 ) ;
       memcpy( sc->lmkpair, "14", 2 ) ;
       memcpy( sc->zmk, i101->key, 16 ) ;
       memcpy( sc->zmkey, i101->data, 16 ) ;
       memset( sc->numn, '0', 48 ) ;
       memcpy( sc->numn, i101->data+16, 16) ;
       memcpy( sc->data1, i101->sync1, 8 ) ;
       memcpy( sc->data2, i101->sync2, 8 ) ;
       HSMLogMSG( "FN000101;", cmds, NULL ) ;
       o101->rc = hsmio(cmds, 0) ;
       if(  o101->rc != 0 && o101->rc != 11 ) {
           HSMLogMSG( "FN000101;",cmds, NULL ) ;
	       return( o101->rc ) ;
	   }
       memcpy( o101->KEY, sd->lmkey, 16 ) ;
       memcpy( o101->N1, sd->numn1, 16 ) ;
       memcpy( o101->N2, sd->numn2, 16 ) ;
       memcpy( o101->sync, sd->mab1, 8 ) ;
       memcpy( o101->sync+8, sd->mab2, 8 ) ;
   }
   if( i101->keytype == 0x02 ){
       memcpy( sc->cmd, "SC", 2 ) ;
       memcpy( sc->lmkpair, "14", 2 ) ;
       tsc->zmksz = 'U' ;
       memcpy( tsc->zmk, i101->key, 32 ) ;
       tsc->zmkeysz = 'X' ;
       memcpy( tsc->zmkey, i101->data, 32) ;
       memset( tsc->numn, '0', 48 ) ;
       memcpy( tsc->numn, i101->data+32, 16) ;
       memcpy( tsc->data1, i101->sync1, 8 ) ;
       memcpy( tsc->data2, i101->sync2, 8 ) ;

       HSMLogMSG( "FN000101;", cmds, NULL ) ;
       o101->rc = hsmio(cmds, 0) ;
       if(  o101->rc != 0 && o101->rc != 11 ) {
           HSMLogMSG( "FN000101;",cmds, NULL ) ;
	       return( o101->rc ) ;
	   }
       memcpy( o101->KEY, sd->lmkey+1, 32 ) ;
       memcpy( o101->N1, sd->numn1+17, 16 ) ;
       memcpy( o101->N2, sd->numn2+17, 16 ) ;
       memcpy( o101->sync, sd->mab1+17, 8 ) ;
       memcpy( o101->sync+8, sd->mab2+17, 8 ) ;
   }

   HSMLogMSG( "FN000101;", cmds, NULL ) ;
   return( o101->rc ) ;
}
