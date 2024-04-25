#include "suip.h"
#include "hsmapi.h"
/*---------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000308 : 檢核MAC_DATA，與檢核交易驗證碼(TAC_DATA)              */
/*---------------------------------------------------------------------------------*/
/*
DES_LIB("FN000308"，"N/A"，"詳: Input_data_1"，"N/A"，Return_data_1，Return_data_2，Return_Code)

Input Parameters:
key_identify =N/A
Input_data_1：
input_length_1(N4)=2(2個Function依序執行，檢核錯誤時，即回傳錯誤代碼)
input_string_1_1(X327)=值，請參考FN000302
input_string_1_2(X327)=值，請參考FN000402
Input_data_2 =N/A
Output Parameters：N/A
Return_Code=依序為MAC=101，TAC=102

00021T3MAC ATM   80712121 1                                            
003899031612121784601000000012100300911372                                                                                          
000882F430C2                                                                                                                        
1T3ICC C6    807      1                                            
00880000051025200000000001000012121000784529522010031609295200044004001026840012100300911372                                        
00641210030091137201121003009113720000000000000000018065D4AD8E2E099D

Input_data_2="0080"+ICV(H16)+DivData(N48)+ATM_TAC_data(H16)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    keytype1 ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key1[48] ;      /* 0x01= 16, 0x02 = 32, 0x03= 48 */
	char    keytype2 ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key2[48] ;      /* 0x01= 16, 0x02 = 32, 0x03= 48 */
	char    len[4] ;
	char	mac[8] ;        /* mac */
	char	input[256] ;    /* DATA Length */
}	fn308i_t, *pfn308i_t ;


typedef struct	{
	int 	rc ;			/* RETURN CODE  */
}	fn308o_t, *pfn308o_t ;


int fn308( char* out, char* in ) 
{
   int  datlen1, datlen2, datlen3, mklen ;
   char  cmds[512], mackey[50] ;
   char input1[128], input2[128], input3[128], icv[16] ;

   pfn308i_t i308 = (pfn308i_t) in ;
   pfn308o_t o308 = (pfn308o_t) out ;
   CMDSF *sf = (CMDSF *) cmds ;
   CMDxTO *to = (CMDxTO *) cmds ;
   CMDxTP *tp = (CMDxTP *) cmds ;

   memset( mackey, 0, sizeof(mackey) ) ;
   memset( input1, 0, sizeof(input1) ) ;
   memset( input2, 0, sizeof(input2) ) ;
   memset( input3, 0, sizeof(input3) ) ;
   memset( icv, '0', sizeof(icv) ) ;

   datlen1 = ldrint(i308->input, 4) ;
   memcpy( input1, i308->input+4, datlen1 ) ;
   datlen2 = ldrint(i308->input+datlen1+4, 4) ;
   memcpy( input2, i308->input+datlen1+8, datlen2 ) ;
   datlen3 = ldrint(i308->input+datlen1+8+datlen2, 4) ;
   memcpy( input3, i308->input+datlen1+12+datlen2, datlen3 ) ;


   if( i308->keytype1 == 0x01 )
       memcpy( mackey, i308->key1, 16 ) ;
   else
   if( i308->keytype1 == 0x02 ){
       memcpy( mackey, "U", 1 ) ;
       memcpy( mackey+1, i308->key1, 32 ) ;
   }
   else
   if( i308->keytype1 == 0x03 ){
       memcpy( mackey, "T", 1 ) ;
 	   memcpy( mackey+1, i308->key1, 48 ) ;
   }

   mklen = strlen( mackey ) ;
   memset( icv, '0', 16 ) ;
   memcpy( icv, input1, 6 ) ;

   memset( cmds, 0, sizeof(cmds) ) ;

   strcpy( cmds, "SE" ) ;
   memcpy( cmds+2, "14", 2 ) ;
   memcpy( cmds+4, mackey, mklen ) ;
   memcpy( cmds+4+mklen, icv, 16 ) ;
   memcpy( cmds+20+mklen, input1+6, datlen1-6 );

   HSMLogMSG( "FN000308;", cmds, NULL ) ;
   if( o308->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000308;", cmds, NULL ) ;
       return( o308->rc ) ;
   }
   HSMLogMSG( "FN000308;", cmds, NULL ) ;
   if( memcmp( i308->mac, sf->mab, 8 ) ) {
        o308->rc = 101  ;
       return( o308->rc ) ;
   }

   memset( cmds, 0, sizeof(cmds) ) ;

   memcpy( to->cmd, "TO", 2 ) ;
   to->tmkksz = 'T' ;
   memcpy(to->tmk, i308->key2, 48 ) ;  /* C6 KEY */
   to->divlen= '3' ;
   memcpy( to->divdata, input3, 48 ) ;
   to->ttmksz = 'T' ;
   memcpy(to->ttmk, i308->key2, 48 ) ;  /* C6 KEY */
   to->kschm ='Y' ;
   memcpy( to->icv, "4649534343415244", 16 ) ;
   UnpackCHARS( to->data, input2, datlen2 ) ;
   HSMLogMSG( "FN000308;", cmds, NULL ) ;

   if( o308->rc = hsmio(cmds, 0) ){
   HSMLogMSG( "FN000308;", cmds, NULL ) ;
      return( o308->rc ) ;
   }
   HSMLogMSG( "FN000308;", cmds, NULL ) ;

   if( memcmp( input3+48,tp->mab, 8 ) ) {
        o308->rc = 102  ;
       return( o308->rc ) ;
   }
   return( o308->rc ) ;
}



