#include "suip.h"
#include "hsmapi.h"
/*---------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000309 : 檢核MAC_DATA，與產生MAC_DATA欄位資料_ATM                    */
/*---------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000309"，"N/A"，"詳: Input_data_1"，"N/A"，Return_data_1，Return_data_2，Return_Code)

Input Parameters:
key_identify =N/A
Input_data_1：
input_length_1(N4)=2(2個Function依序執行，檢核錯誤時，即回傳錯誤代碼)
input_string_1_1(X327)=值，請參考FN000302
input_string_1_2(X327)=值，請參考FN000301
Input_data_2 =N/A
Output Parameters：
Return_data_1="0008"+MAC_Data(H8)
*/


typedef	struct	{
	char    f  ;
	char    keytype1 ;
	char    key1[48] ;
	char    keytype2 ;
	char    key2[48] ;
	char    len[4] ;
	char    mac[8] ;
	char    input[256] ;
}	fn309i_t, *pfn309i_t ;

typedef	struct	{
	int  	rc ;
	char	mac[8] ;
}	fn309o_t, *pfn309o_t ;


int fn309( char* out, char* in ) 
{
   int  ksz1, ksz2, datlen1, datlen2 ;
   char *bptr, cmds[512], mackey1[50], mackey2[50], icv[16], icv1[16] ;
   char input1[128], input2[128] ;

   CMDSG *sg = (CMDSG *) cmds ;
   CMDSH *sh = (CMDSH *) cmds ;
   
   pfn309i_t i309 = (pfn309i_t) in ;
   pfn309o_t o309 = (pfn309o_t) out ;

   memset( mackey1, 0, sizeof(mackey1) ) ;
   memset( mackey2, 0, sizeof(mackey2) ) ;
   memset( input1, 0, sizeof(input1) ) ;
   memset( input2, 0, sizeof(input2) ) ;
   memset( icv, 0, sizeof(icv) ) ;
   memset( icv1, 0, sizeof(icv1) ) ;

   datlen1 = ldrint(i309->input, 4) ;
   memcpy( input1, i309->input+4, datlen1 ) ;
   datlen2 = ldrint(i309->input+datlen1+4, 4) ;
   memcpy( input2, i309->input+datlen1+8, datlen2 ) ;

  if( i309->keytype1 == 0x01 )   ksz1 = 8 ;
  if( i309->keytype1 == 0x02 )   ksz1 = 16 ;
  if( i309->keytype1 == 0x03 )   ksz1 = 24 ;
  hexncpy( mackey1, i309->key1, ksz1 ) ;
  hexncpy( icv, input1, 3 ) ;

  if( i309->keytype2 == 0x01 )   ksz2 = 8 ;
  if( i309->keytype2 == 0x02 )   ksz2 = 16 ;
  if( i309->keytype2 == 0x03 )   ksz2 = 24 ;
  hexncpy( mackey2, i309->key2, ksz1 ) ;
  hexncpy( icv1, input2, 3 ) ;

  memset( cmds, 0, sizeof(cmds) ) ;
  memcpy( sg->cmd, "SG", 2 ) ;
  bptr = sgCmdTXT( cmds+2, mackey1, ksz1, 14, icv, input1+6 ) ;
  *bptr++ = ';' ;
  sgCmdTXT( bptr, mackey2, ksz2, 14, icv1, input2+6 ) ;

   HSMLogMSG( "FN000309;", cmds, NULL ) ;
   if( o309->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000309;", cmds, NULL ) ;
	   return( o309->rc ) ;
   }

   HSMLogMSG( "FN000309;", cmds, NULL ) ;
   if( memcmp( i309->mac, sh->mab1, 8 ) ) {
        o309->rc = 101  ;
       return( o309->rc ) ;
   }
   memcpy( o309->mac, sh->mab2, 8 ) ;
   return( o309->rc ) ;
}
