#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000121 : ���o����P�]���Υ���P��L�Ȧ涡��KeySyncCheckValue                */
/*----------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000121"�A"1S1MAC RMF   950     "�A"0006"+����N��(N3)+���N��(N3)"�A""�AReturn_data_1�A""�AReturn_Code)
Input Parameters:
key_identify ="1S1MAC RMF   950     "  (�P�]������MAC)
key_identify ="1S1MAC RMS   807     "  (�P��L�Ȧ涡)
Input_data_1 ="0006"+�o�H���N��(N3)+���H���N��(N3)"
Output Parameters:
Return_data_1="0008"+KeySyncCheckValue(H8)
*/
typedef struct	{
	char	f ;				/* FUNCTION CODE X'B8'  */
	char    keytype ;
	char	key[48] ;		/* db key          */
	char    len[4] ;
	char    syncdata[6] ;
}	fn121i_t, *pfn121i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE          */
	char	keysync[8] ;	/* KeySyncCheckValue */
}	fn121o_t, *pfn121o_t ;

int fn121( char* out, char* in ) 
{
   int mklen ;
   char cmds[512], syncdata[32], mackey[50] ;
   pfn121i_t i121 = (pfn121i_t) in ;
   pfn121o_t o121 = (pfn121o_t) out ;
   CMDSF *sf = (CMDSF *) cmds ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memset( mackey, 0, sizeof(mackey) ) ;
   memset( syncdata, '0', sizeof(syncdata) ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memset( mackey, 0, sizeof(mackey) ) ;

   if( i121->keytype == 0x01 )
       memcpy( mackey, i121->key, 16 ) ;
   else
   if( i121->keytype == 0x02 ){
       memcpy( mackey, "U", 1 ) ;
       memcpy( mackey+1, i121->key, 32 ) ;
   }
   else
   if( i121->keytype == 0x03 ){
       memcpy( mackey, "T", 1 ) ;
 	   memcpy( mackey+1, i121->key, 48 ) ;
   }
   
   mklen = strlen( mackey ) ;
   memcpy( syncdata, i121->syncdata, 6 ) ;

   strcpy( cmds, "SE14" ) ;
   memcpy( cmds+4, mackey, mklen ) ;
   memcpy( cmds+4+mklen, "0000000000000000", 16 ) ;
   memcpy( cmds+20+mklen, syncdata, 16 ) ;
   HSMLogMSG( "FN000121;", cmds, NULL ) ;
   if( o121->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000121;", cmds, NULL ) ;
       return( o121->rc ) ;
   }
   HSMLogMSG( "FN000121;", cmds, NULL ) ;
   memcpy( o121->keysync, sf->mab, 8 ) ;

   return( o121->rc ) ;
}

