#include "suip.h"
#include "hsmapi.h"
/*-------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000803 : 產生MAC DATA欄位資料                                */
/*-------------------------------------------------------------------------------*/
/*
DES_LIB("FN000803"，key_identify，Input_data_1，""，Return_data_1，""，Return_Code)

Input Parameters:
1.	 
key_identify =1T3MAC ATM   "+ATM_ID
Input_data_1="0016"+Random Number(H16)
Input_data_2="0016"+MAC(H16)
*/


typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    keytype ;
	char    key[48] ;
	char    data[32] ;
}	fn803i_t, *pfn803i_t ;

typedef struct	{
	int 	rc ;
	char	mac[16] ;
}	fn803o_t, *pfn803o_t ;

int fn803( char* out, char* in ) 
{
   int  n, mklen ;
   char cmds[512], mackey[50], atmid[10] ; 
   pfn803i_t i803 = (pfn803i_t) in ;
   pfn803o_t o803 = (pfn803o_t) out ;
   CMDSE_t *tse = (CMDSE_t *) cmds ;
   CMDSE *se = (CMDSE *) cmds ;
   CMDSF *sf = (CMDSF *) cmds ;

       
   memset( atmid, 0, sizeof(atmid) ) ;
   memset( mackey, 0, sizeof(mackey) ) ;

   if( i803->keytype == 0x01 ) 
	   memcpy( mackey, i803->key, 16 ) ;
   else
   if( i803->keytype == 0x02 ){
	   memcpy( mackey, "U", 1 ) ;
	   memcpy( mackey+1, i803->key, 32 ) ;
   }
   else
   if( i803->keytype == 0x03 ){
	   memcpy( mackey, "T", 1 ) ;
	   memcpy( mackey+1, i803->key, 48 ) ;
   }

   mklen = strlen(mackey) ;
   n = ldrint( i803->data, 4 ) ; 

   strcpy( cmds, "SE" ) ;
   memcpy( cmds+2, "14", 2 ) ;
   memcpy( cmds+4, mackey, mklen ) ;
   memcpy( cmds+4+mklen, "0000000000000000", 16 ) ;
   memcpy( cmds+20+mklen, i803->data+4, n );

   HSMLogMSG( "FN000803;", cmds, NULL ) ;
   if( o803->rc = hsmio(cmds, 0) ){
   HSMLogMSG( "FN000803;", cmds, NULL ) ;
      return( o803->rc ) ;
   }
   memcpy( o803->mac, sf->mab, 16 ) ;
   HSMLogMSG( "FN000803;", cmds, NULL ) ;
   return( o803->rc ) ;
}

