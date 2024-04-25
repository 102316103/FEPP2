#include "suip.h"
#include "hsmapi.h"
/*------------------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000122 : 檢核本行與參加單位(匯出行)間之通匯押碼同步查核欄是否同步; 並做新key確認及變更Key-File  */
/*------------------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000122"，"1S1MAC RMR   "+Bank_ID+"   "，"0008"+KeySyncCheckValue(H8)，""，""，""，Return_Code)

Input Parameters:
key_identify ="1S1MAC RMR   "+Bank_ID+"   "
(Bank_ID為他行的代號後面補空白到共六位)
Input_data_1 ="0008"++KeySyncCheckValue(H8)

Output Parameters：N/A
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'B8'  */
	char    keytype ;
	char    key[48] ;
	char    keysynclen[4] ;
	char	keysync[8] ;	/* KeySyncCheckValue */
	char    synclen[4] ;
	char    syncdata[6] ;
}	fn122i_t, *pfn122i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE          */
}	fn122o_t, *pfn122o_t ;

int fn122( char* out, char* in ) 
{
   int mklen ;
   char cmds[512], syncdata[32], mackey[50];
   pfn122i_t i122 = (pfn122i_t) in ;
   pfn122o_t o122 = (pfn122o_t) out ;
   CMDSF *sf = (CMDSF *) cmds ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memset( mackey, 0, sizeof(mackey) ) ;
   memset( syncdata, '0', sizeof(syncdata) ) ;

   if( i122->keytype == 0x01 )
       memcpy( mackey, i122->key, 16 ) ;
   else
   if( i122->keytype == 0x02 ){
       memcpy( mackey, "U", 1 ) ;
       memcpy( mackey+1, i122->key, 32 ) ;
   }
   else
   if( i122->keytype == 0x03 ){
       memcpy( mackey, "T", 1 ) ;
 	   memcpy( mackey+1, i122->key, 48 ) ;
   }


   memcpy( syncdata, i122->syncdata+3, 3 ) ;
   memcpy( syncdata+3, i122->syncdata, 3 ) ;

   mklen = strlen( mackey ) ;

   strcpy( cmds, "SE14" ) ;
   memcpy( cmds+4, mackey, mklen ) ;
   memcpy( cmds+4+mklen, "0000000000000000", 16 ) ;
   memcpy( cmds+20+mklen, syncdata, 16 ) ;
   HSMLogMSG( "FN000122;", cmds, NULL ) ;
   if( o122->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000122;", cmds, NULL ) ;
       return( o122->rc ) ;
   }
   HSMLogMSG( "FN000122;", cmds, NULL ) ;
   if ( memcmp( i122->keysync, sf->mab, 8 ) ){
	   o122->rc = 101 ;
       HSMLogMSG( "FN000122;", ";sync error", NULL ) ;
       return( o122->rc ) ;
   }
   return( o122->rc ) ;
}

