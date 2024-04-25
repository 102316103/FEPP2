#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"

/*-------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000313 : ����ANSI X9.9/X9.19 MAC DATA�����                     */
/*-------------------------------------------------------------------------------*/
/*
DES_LIB("FN000313"�Akey_identify�AInput_data_1�A""�AReturn_data_1�A""�AReturn_Code)

Input Parameters:

*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char	mode ;				/* 0x00 = x9.9 MAC, 0x01 = x9.19 MAC */
	char    keytype ;                       /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;                       /* 0x01= 16, 0x02 = 32, 0x03= 48 */
	char	datalen[4] ;                    /* INPUT1 DATA */
	char	data[32] ;	   
}	fn313i_t, *pfn313i_t ;


typedef struct	{
	int 	rc ;
	char	mac[16] ;
}	fn313o_t, *pfn313o_t ;

int fn313( char* out, char* in ) 
{
   int  n, mklen ;
   char cmds[1024], mackey[50], hexdec[5] ; 
   pfn313i_t i313 = (pfn313i_t) in ;
   pfn313o_t o313 = (pfn313o_t) out ;

   CMDSE_t *tse = (CMDSE_t *) cmds ;
   CMDSE *se = (CMDSE *) cmds ;
   CMDSF *sf = (CMDSF *) cmds ;
   
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( mackey, 0, sizeof(mackey) ) ;

   if( i313->keytype == 0x01 )
       memcpy( mackey, i313->key, 16 ) ;
   if( i313->keytype == 0x02 ){
       memcpy( mackey, "U", 1 ) ;
       memcpy( mackey+1, i313->key, 32 ) ;
   }
   if( i313->keytype == 0x03 ){
       memcpy( mackey, "T", 1 ) ;
       memcpy( mackey+1, i313->key, 48 ) ;
   }

   mklen = strlen( mackey ) ;
   n = ldrint( i313->datalen, sizeof(i313->datalen) ) ;   

   memset( hexdec, 0, sizeof(hexdec) ) ;
   if( i313->mode != 0x01 ) 
       memcpy( cmds, "M601111", 7 ) ;    /* 0 - single block( ICV default '0'), 0 - binary, 1 - MAC size 16byte, 1 - X9.9, 1 - Padding with 0x00 */
   else
       memcpy( cmds, "M601131", 7 ) ;    /* 0 - single block( ICV default '0'), 0 - binary, 1 - MAC size 16byte, 3 - X9.19, 1 - Padding with 0x00 */
   memcpy( cmds+7, "003", 3 ) ;              /* 003 - TAK, 008 - ZAK */
   memcpy( cmds+10, mackey, mklen ) ;
   sprintf( hexdec, "%04X", n ) ;
   memcpy( cmds+10+mklen, hexdec, 4 ) ;
   memcpy( cmds+14+mklen, i313->data, n );

   HSMLogMSG( "FN000313;", cmds, NULL ) ;
   if( o313->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000313;", cmds, NULL ) ;
       return( o313->rc ) ;
   }
   memcpy( o313->mac, sf->mab, 16 ) ;
   *(o313->mac+16) = '\0' ;
   HSMLogMSG( "FN000313;", cmds, NULL ) ;
   return( o313->rc ) ;
}

