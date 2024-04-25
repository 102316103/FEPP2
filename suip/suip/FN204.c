#include "suip.h"
#include "hsmapi.h"
/*------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000204 : PIN BLOCK轉換                                                              */
/*------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000204"，"2S1PPK ATM   ATM_ID  "+"PPK ATM   950     "，"0016"+ATM_PinBlock(H16)，""，Return_data_1，""，Return_Code)

Input Parameters:
1.ATM 傳來之 PIN BLOCK 轉換為 FISC 之 PIN BLOCK
key_identify ="2S1PPK ATM   ATM_ID  "+"PPK ATM   950     "
Input_data_1 ="0016"+ATM_PinBlock(H16)
2.ATM 傳來之 PIN BLOCK 轉換為信用卡之 PIN BLOCK
key_identify ="2S1PPK ATM   ATM_ID  "+"PPK ATM   80777831  "
Input_data_1 ="0016"+ATM_PinBlock(H16)
3.FISC 傳來之 PIN BLOCK 轉換為信用卡之 PIN BLOCK
key_identify ="2S1 PPK ATM   950     "+"PPK ATM   80777831  "
Input_data_1 ="0016"+FISC_PinBlock(H16)

Output Parameters:
1.Return_data_1= "0016"+FISC_PinBlock(B64)(H16)
2.Return_data_1= "0016"+SC_PinBlock(B64)(H16)
3.Return_data_1= "0016"+SC_PinBlock(B64)(H16)
*/
typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    keytype1 ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char	key1[48] ;
	char    keytype2 ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char	key2[48] ;
	char    len[4] ;
	char    src_pinb[16] ;
}	fn204i_t, *pfn204i_t ;

typedef struct	{
	int 	rc ;
	char	des_pinb[16] ;
}	fn204o_t, *pfn204o_t ;

int fn204( char* out, char* in ) 
{
   int srcklen, desklen ;
   char cmds[512], SrcTPK[50], DesTPK[50] ; 
   pfn204i_t i204 = (pfn204i_t) in ;
   pfn204o_t o204 = (pfn204o_t) out ;

   CMDSJ *r = (CMDSJ *) cmds ;

   memset( SrcTPK, 0, sizeof(SrcTPK) ) ;
   memset( DesTPK, 0, sizeof(DesTPK) ) ;

   if( i204->keytype1 == 0x01 )
        memcpy( SrcTPK, i204->key1, 16 ) ;
   if( i204->keytype1 == 0x02 ) { 
        memcpy( SrcTPK, "U", 1 ) ;
        memcpy( SrcTPK+1, i204->key1, 32 ) ;
   }
   if( i204->keytype1 == 0x03 ) { 
        memcpy( SrcTPK, "T", 1 ) ;
        memcpy( SrcTPK+1, i204->key1, 48 ) ;
   }

   if( i204->keytype2 == 0x01 ) 
       memcpy( DesTPK, i204->key2, 16 ) ;
   if( i204->keytype2 == 0x02 ) { 
       memcpy( DesTPK, "U", 1 ) ;
       memcpy( DesTPK+1, i204->key2, 32 ) ;
   }
   if( i204->keytype2 == 0x03 ) { 
       memcpy( DesTPK, "T", 1 ) ;
       memcpy( DesTPK+1, i204->key2, 48 ) ;
   }

   srcklen = strlen( SrcTPK ) ;
   desklen = strlen( DesTPK ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   strcpy( cmds, "SI" ) ;
   memcpy( cmds+2, "14", 2 ) ;
   memcpy( cmds+4, SrcTPK, srcklen ) ;
   memcpy( cmds+4+srcklen, "14", 2 ) ;
   memcpy( cmds+6+srcklen, DesTPK, desklen ) ;
   memcpy( cmds+6+srcklen+desklen, i204->src_pinb, 16 ) ;

   HSMLogMSG( "FN000204;", cmds, NULL ) ;
   if( o204->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000204;", cmds, NULL ) ;
       return( o204->rc ) ;
  }
   memcpy( o204->des_pinb, r->pinbk, 16 ) ;
   HSMLogMSG( "FN000204;", cmds, NULL ) ;
   return( o204->rc ) ;

}
