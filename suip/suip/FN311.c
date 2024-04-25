#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"

/*-------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000311 : 產生MAC DATA欄位資料                                */
/*-------------------------------------------------------------------------------*/
/*
DES_LIB("FN000301"，key_identify，Input_data_1，""，Return_data_1，""，Return_Code)

Input Parameters:
1.	 
key_identify ="1S1MAC OPC   950     "
Input_data_1="0022"+ICV0(N6)+交易啟動BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)
2.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0022"+ICV0(N6)+交易啟動BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)
3.	
key_identify ="1S1MAC RMF   950     "
Input_data_1="0022"+ICV0(N6)+交易啟動BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)
4.	
key_identify ="1S1MAC OPC   950     "
Input_data_1="0030"+ICV0(N6)+交易啟動行BANK_ID(N3)+
  STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
KEY_ID(N2)+"000000"
5.	
key_identify ="1S1MAC OPC   950     "
Input_data_1="0030"+ICV0(N6)+交易啟動行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
NOTICE_ID(N4)+"0000"
6.	
key_identify ="1S1MAC OPC   950     "
Input_data_1="0030"+ICV0(N6)+交易啟動行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
AP_ID(N4)+"0000"
7.	
key_identify ="1S1MAC OPC   950     "
Input_data_1="0030"+ICV0(N6)+交易啟動行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
AP_ID(N4)+M_BANK_ID(N3)+"0"
8.	
key_identify ="1S1MAC RMF   950     "
Input_data_1="0036"+ICV0(N6)+交易啟動行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
撥轉金額(N14) 
9.	
key_identify ="1S1MAC RMF   950     "
Input_data_1="0038"+ICV0(N6)+電文序號(N7)+
匯款金額(N13)+匯款單位代號(N3)+"000000000"
10.	
key_identify ="1S1MAC RMF   950     "
Input_data_1="0038"+ICV0(N6)+參加單位電文序號(N7)+
交易金額(N13)+發信單位代號(N3)+"000000000"
11.	
key_identify ="1S1MAC RMS   807     "
Input_data_1="0054"+ICV0(N6)+通匯序號(N7)+
匯款金額(N13)+收款人帳號N(14)+匯款單位代號(N3)+
解款單位代號(N3)+"00000000"
12.	
key_identify ="1S1MAC RMF   950     "
Input_data_1="0038"+ICV0(N6)+原通知訊息電文序號(N7)+
匯款金額(N13)+解款單位代號(N3)+回應代號(N4)+
"00000"
13.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
交易金額 (N13)+"000"
14.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
可用餘額 (N13)+"000"
15.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
 PinBlock(H16)
16.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
轉入帳號 (N16)
17.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0054"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
PinBlock(H16)+轉入帳號(N16)
18.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
原交易序號(N10)+"000000"
19.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
  STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
原交易日期(N6)+原交易序號(N10).
20.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0054"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
原交易日期(N6)+原交易序號(N10)+處理結果(N2)+
"00000000000000"
21.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0054"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
原交易日期(N6)+原交易序號(N10)+沖正指示(N3)+
"0000000000000"
22.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0054"+ICV0(N6)+ICV1代理行BANK_ID(N3)+
STAN_Number(N7)+Message_Type(N2)+Response_Code(N4)+
原交易日期(N6)+原交易序號(N10)+沖正指示(N3)+
處理結果(N2)+"00000000000"
23.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0022"+ICV0(N6)+ICV1發信單位代號(N3)+
追蹤序號(N7)+message function(N2)+response code(N4)
24.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0(N6)+ICV1發信單位代號(N3)+
追蹤序號(N7)+message function(N2)+response code(N4)+
預先授權交易金額/交易金額(N13)+"000"
25.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0(N6)+ICV1發信單位代號(N3)+
追蹤序號(N7)+message function(N2)+response code(N4)+
可用餘額(N13)+"000"
26.	
key_identify ="1S1MAC ATM   950     "
Input_data_1="0038"+ICV0 (N6)+ICV1發信單位代號(N3)+
追蹤序號(N7)+message function(N2)+response code(N4)+
轉入帳號(N16)
Output Parameters:
Return_data_1="0008"+MAC_Data(H8)
Retrun_data_1="0016"+MAC_Data(H16) (第27項)
*/
typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;      /* 0x01= 16, 0x02 = 32, 0x03= 48 */
	char	icvlen[4] ;    /* INPUT1 DATA */
	char	icv[16] ;      /* INPUT1 DATA */
	char	datlen[4] ;	   
	char	data[32] ;	   
}	fn311i_t, *pfn311i_t ;


typedef struct	{
	int 	rc ;
	char	mac[8] ;
}	fn311o_t, *pfn311o_t ;

int fn311( char* out, char* in ) 
{
   int  n, mklen ;
   char cmds[512], mackey[50] ; 
   pfn311i_t i311 = (pfn311i_t) in ;
   pfn311o_t o311 = (pfn311o_t) out ;

   CMDSE_t *tse = (CMDSE_t *) cmds ;
   CMDSE *se = (CMDSE *) cmds ;
   CMDSF *sf = (CMDSF *) cmds ;
   
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( mackey, 0, sizeof(mackey) ) ;

   if( i311->keytype == 0x01 )
       memcpy( mackey, i311->key, 16 ) ;
   else
   if( i311->keytype == 0x02 ){
       memcpy( mackey, "U", 1 ) ;
       memcpy( mackey+1, i311->key, 32 ) ;
   }
   else
   if( i311->keytype == 0x03 ){
       memcpy( mackey, "T", 1 ) ;
 	   memcpy( mackey+1, i311->key, 48 ) ;
   }
   
   mklen = strlen( mackey ) ;
   n = ldrint( i311->datlen, 4 ) ;   


   strcpy( cmds, "SE" ) ;
   memcpy( cmds+2, "14", 2 ) ;
   memcpy( cmds+4, mackey, mklen ) ;
   memcpy( cmds+4+mklen, i311->icv, 16 ) ;
   memcpy( cmds+20+mklen, i311->data, n );

   HSMLogMSG( "FN000311;", cmds, NULL ) ;
   if( o311->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000311;", cmds, NULL ) ;
       return( o311->rc ) ;
   }
   memcpy( o311->mac, sf->mab, 16 ) ;
   HSMLogMSG( "FN000311;", cmds, NULL ) ;
   return( o311->rc ) ;
}

