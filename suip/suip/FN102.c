#include "suip.h"
#include "hsmapi.h"
/*-------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000102 : 與他行換key前本行隨機產生新key及KeySyncCheckValue               */
/*-------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000102"，"2T2MAC RMS   807   1 CDK RMS   807   1 "，"0006"+發信單位代號(N3)+收信單位代號(N3)，""，""，""，Return_Code)

key_identify ="2S1MAC RMS   807   1 CDK RMS   807   1 "
Input_data_1 ="0006"+發信單位代號(N3)+收信單位代號(N3)

(single DES)
Return_data_1="0024"+新key(H16)+新KeySyncCheckValue(H8)
(3_DES 2_length)
Return_data_1="0040"+新key(H32)+新KeySyncCheckValue(H8)
(3_DES 3_length)
Return_data_1="0056"+新key(H48)+新KeySyncCheckValue(H8)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE                */
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;
	char    len[4] ;
	char    data[32] ;
}	fn102i_t, *pfn102i_t ;

typedef struct	{
	int 	rc ;				/* RETURN CODE                  */
    char    dbkey[72] ;
}	fn102o_t, *pfn102o_t ;

int fn102( char* out, char* in ) 
{
   char cmds[512];
   int len ;
   pfn102i_t i102 = (pfn102i_t) in ;
   pfn102o_t o102 = (pfn102o_t) out ;

   CMDSA *sa = (CMDSA *) cmds ;
   CMDSB *sb = (CMDSB *) cmds ;
   CMDSA_t *tsa = (CMDSA_t *) cmds ;
   CMDSB_t *tsb = (CMDSB_t *) cmds ;


   len = ldrint(i102->len, 4) ;
   if( len != 6 ) return( o102->rc = 81 ) ;
   memset( cmds, 0, sizeof(cmds) ) ;
   if( i102->keytype == 0x01 ){
       memcpy( sa->cmd, "SA", 2 ) ;
       memcpy( sa->lmkpair, "14", 2 ) ;
       memcpy( sa->zmk, i102->key, 16 ) ;
       memset( sa->data1, '0', 32 ) ;
       memcpy( sa->data1, i102->data, 6 ) ;
       HSMLogMSG( "FN000102;", cmds, NULL ) ;
      if( o102->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000102;", cmds, NULL ) ;
	       return( o102->rc ) ;
	   }
       memcpy( o102->dbkey, tsb->lmkey, 16 ) ;
       memcpy( o102->dbkey+16, tsb->zmkey, 16 ) ;
       memcpy( o102->dbkey+32, tsb->mab1, 8 ) ;
   }
   if( i102->keytype == 0x02 ){
       memcpy( tsa->cmd, "SA", 2 ) ;
       memcpy( tsa->lmkpair, "14", 2 ) ;
	   tsa->zmksz = 'U' ;
       memcpy( tsa->zmk, i102->key, 32 ) ;
       memset( tsa->data1, '0', 32 ) ;
       memcpy( tsa->data1, i102->data, 6 ) ;
	   tsa->delm = ';' ;
       tsa->kekschm = 'X' ;
	   tsa->lmkschm = 'U' ;
	   tsa->kcvmeth = '0' ;
       HSMLogMSG( "FN000102;", cmds, NULL ) ;
      if( o102->rc = hsmio(cmds, 0) ){
           HSMLogMSG( "FN000102;", cmds, NULL ) ;
	       return( o102->rc ) ;
	   }
       memcpy( o102->dbkey, tsb->lmkey, 32 ) ;
       memcpy( o102->dbkey+32, tsb->zmkey, 32 ) ;
       memcpy( o102->dbkey+64, tsb->mab1, 8 ) ;
   }

   HSMLogMSG( "FN000102;",  cmds, NULL ) ;
   return( o102->rc ) ;
}

