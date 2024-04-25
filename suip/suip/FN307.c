#include "suip.h"
#include "hsmapi.h"
/*-------------------------------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000307 : 檢核MAC_DATA，與檢查金融卡之OFFSET與密碼是否正確_磁條卡(本行卡， FISC送來)，與產生MAC_DATA欄位資料  */
/*-------------------------------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000203"，"2S1PVK ATM   807     "+"PPK ATM   950     "，"0036"+卡號(N16)+"0000"+帳號(N12)+OFFSET(N4)，
"0016"+PinBlock(H16)，""，""，Return_Code)

PIN Verify and Generate Two MAC
DES_LIB("FN000307"，"N/A"，"詳: Input_data_1"，"N/A"，Return_data_1，Return_data_2，Return_Code)

Input Parameters:
key_identify =N/A
Input_data_1：
input_length_1(N4)=3(3個Function依序執行，檢核錯誤時，即回傳錯誤代碼)
input_string_1_1(X327)=值，請參考FN000302
input_string_1_2(X327)=值，請參考FN000203
input_string_1_3(X327)=值，請參考FN000301
Input_data_2 =N/A
Output Parameters:
Return_data_1="0008"+MAC_Data(H8)
Return_Code=依序為MAC=101，OFFSET=102
*/

typedef struct	{
	char	f ;				
	char    pvk[16] ;         
	char    keytype ;
	char	TPK[48] ;
	char    pinblen[4] ;
	char	PINB[16] ;
	char    maktype1 ;
	char	mak1[48] ;
	char    maktype2 ;
	char	mak2[48] ;
	char    maclen[4] ;
	char    mac[8] ;
    char    input[256] ;     
}	fn307i_t, *pfn307i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	mac[8] ;
}	fn307o_t, *pfn307o_t ;

int fn307( char* out, char* in ) 
{
   int ksz, ksz1, ksz2, datlen1, datlen2, datlen3  ;
   char  *bptr, cmds[512], PVK[50], TPK[50], mackey1[50], mackey2[50] ;
   char input1[128], input2[128], input3[128], icv[16], icv1[16] ;

   pfn307i_t i307 = (pfn307i_t) in ;
   pfn307o_t o307 = (pfn307o_t) out ;

   CMDfSS *fis = (CMDfSS *) cmds ;
   CMDxSS *atm = (CMDxSS *) cmds ;
   CMDST *r = (CMDST *) cmds ;

   memset( PVK, 0, sizeof(PVK) ) ;
   memset( TPK, 0, sizeof(TPK) ) ;
   memset( mackey1, 0, sizeof(mackey1) ) ;
   memset( mackey2, 0, sizeof(mackey2) ) ;
   memset( input1, 0, sizeof(input1) ) ;
   memset( input2, 0, sizeof(input2) ) ;
   memset( input3, 0, sizeof(input3) ) ;
   memset( icv, 0, sizeof(icv) ) ;
   memset( icv1, 0, sizeof(icv1) ) ;

   datlen1 = ldrint(i307->input, 4) ;
   memcpy( input1, i307->input+4, datlen1 ) ;
   datlen2 = ldrint(i307->input+datlen1+4, 4) ;
   memcpy( input2, i307->input+datlen1+8, datlen2 ) ;
   datlen3 = ldrint(i307->input+datlen1+8+datlen2, 4) ;
   memcpy( input3, i307->input+datlen1+12+datlen2, datlen3 ) ;


   hexncpy( PVK, i307->pvk, 8 ) ;
   if( i307->keytype == 0x01 )   ksz = 8 ;
   if( i307->keytype == 0x02 )   ksz = 16 ;
   if( i307->keytype == 0x03 )   ksz = 24 ;

   hexncpy( TPK, i307->TPK, ksz ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( fis->cmd, "SS", 2 ) ;
   bptr = SSCmdTXT( cmds+2, 3, TPK, ksz, PVK, i307->PINB, input2+20, input2, input2+32, NULL ) ;

   if( i307->maktype1 == 0x01 )       ksz1 = 8 ;
   if( i307->maktype1 == 0x02 )       ksz1 = 16 ;
   if( i307->maktype1 == 0x03 )       ksz1 = 24 ;
   hexncpy( mackey1, i307->mak1, ksz1 ) ;
   hexncpy( icv, input1, 3 ) ;
   

   if( i307->maktype2 == 0x01 )       ksz2 = 8 ;
   if( i307->maktype2 == 0x02 )       ksz2 = 16 ;
   if( i307->maktype1 == 0x03 )       ksz2 = 24 ;
   hexncpy( mackey2, i307->mak2, ksz2 ) ;
   hexncpy( icv1, input3, 3 ) ;

   bptr = sgCmdTXT( bptr, mackey1, ksz1, 14, icv, input1+6 ) ;
   *bptr++ = ';' ;
   sgCmdTXT( bptr, mackey2, ksz2, 14, icv1, input3+6 ) ;

   HSMLogMSG( "FN000307;", cmds, NULL ) ;
   if( o307->rc = hsmio(cmds, 0) ){
   HSMLogMSG( "FN000307;", cmds, NULL ) ;
      return( o307->rc ) ;
   }
   HSMLogMSG( "FN000307;", cmds, NULL ) ;
   if( memcmp( i307->mac, r->mab1, 8 ) ) {
        o307->rc = 101  ;
       return( o307->rc ) ;
   }
   memcpy( o307->mac, r->mab2, 8 ) ;

   return( o307->rc ) ;

}



