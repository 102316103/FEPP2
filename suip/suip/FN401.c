#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"
/*---------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000401 :產生交易驗證碼(TAC_DATA)                         */
/*---------------------------------------------------------------------------*/
/*
DES_LIB("FN000401"，"1T3ICC C6    807     "，Input_data_1，Input_data_2，""，""，Return_Code)

Input Parameters:
1.	
Input_data_1="0072"+交易序號(N8)+交易代號(N4)+
交易金額(NS14)+端末代號(N8)+端末查核碼(AN8)+
交易日期時間(N14)+轉出帳號(N16)
2.	
Input_data_1="0088"+交易序號(N8)+交易代號(N4)+
交易金額(NS14)+端末代號(N8)+端末查核碼(AN8)+
交易日期時間(N14)+轉入帳號(N16)+轉出帳號(N16)
3.	
Input_data_1="0040"+交易序號(N8)+交易代號(N4)+
端末查核碼(AN8)+轉出帳號(N16)
4.	
Input_data_1="0096"+交易序號(N8)+交易代號(N4)+
繳費金額(NS14)+端末代號(N8)+端末查核碼(AN8)+
交易日期時間(N14)+繳款類別(N5)+轉出帳號(N16)+
銷帳編號(N16)
Input_data_2="0064"+ICV(H16)+DivData(N48)
Output Parameters：N/A
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    keytype ;
	char    key[48] ;
	char    in2_len[4] ;
	char    icv[16] ;
	char    divdata[48] ;
	char    in1_len[4] ;
	char    input1[128] ;
}	fn401i_t, *pfn401i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	tac[16] ;   	    /*  tac(H8)    */
}	fn401o_t, *pfn401o_t ;



int fn401( char* out, char* in ) 
{
   int n ;
   char cmds[512] ;
   pfn401i_t i401 = (pfn401i_t) in ;
   pfn401o_t o401 = (pfn401o_t) out ;

   CMDxTO *to = (CMDxTO *) cmds ;
   CMDxTP *tp = (CMDxTP *) cmds ;
   
 
   n = ldrint(i401->in1_len, 4) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( to->cmd, "TO", 2 ) ;
   to->tmkksz = 'T' ;
   memcpy(to->tmk, i401->key, 48 ) ;  /* C6 KEY */
   to->divlen= '3' ;
   memcpy( to->divdata, i401->divdata, 48 ) ;
   to->ttmksz = 'T' ;
   memcpy(to->ttmk, i401->key, 48 ) ;  /* C6 KEY */
   to->kschm ='Y' ;
   memcpy( to->icv, i401->icv, 16 ) ;
   UnpackCHARS( to->data, i401->input1, n ) ;
   HSMLogMSG( "FN000401;", cmds, NULL ) ;
   if( o401->rc = hsmio(cmds, 0) ){
   HSMLogMSG( "FN000401;", cmds, NULL ) ;
	   return( o401->rc ) ;
   }
   memcpy( o401->tac, tp->mab, 16 );

   HSMLogMSG( "FN000401;", cmds, NULL ) ;
   return( o401->rc ) ;
}

