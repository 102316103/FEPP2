#include "suip.h"
#include "hsmapi.h"
/*---------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000134 :晶片金融卡交易認證碼製造－功能碼91               */
/*---------------------------------------------------------------------------*/
/*
DES_LIB("FN000134"，"1T3ICC C6    807     "，Input_data_1，Input_data_2，""，""，Return_Code)

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
Input_data_2="0048"+DivData(N48)
Output Parameters：N/A

*/
typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    keytype ;
	char    key[48] ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    divlen[4] ;
	char    DivData[48] ;
	char	datalen[4] ;		/* DATA Length            */
	char	data[32] ;			/* MAC DATA (OCCURS 4 TIMES) */
}	fn134i_t, *pfn134i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	tac[16] ;   	    /*  tac(H8)    */
}	fn134o_t, *pfn134o_t ;

int fn134( char* out, char* in ) 
{
   int n ;
   char cmds[512], ickey[50], data[512] ;
   pfn134i_t i134 = (pfn134i_t) in ;
   pfn134o_t o134 = (pfn134o_t) out ;

   CMDxTO *to = (CMDxTO *) cmds ;
   CMDxTP *tp = (CMDxTP *) cmds ;
   
   memset( ickey, 0, sizeof(ickey) ) ;
   memset( data, 0, sizeof(data) ) ;
 
   n = ldrint( i134->datalen, sizeof(i134->datalen) ) ;   

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( to->cmd, "TO", 2 ) ;
   to->tmkksz = 'T' ;
   memcpy(to->tmk, i134->key, 48 ) ;  /* C6 KEY */
   to->divlen= '3' ;
   memcpy( to->divdata, i134->DivData, 48 ) ;
   to->ttmksz = 'T' ;
   memcpy(to->ttmk, i134->key, 48 ) ;  /* C6 KEY */
   to->kschm ='Y' ;
   memcpy( to->icv, "4649534343415244", 16 ) ;
   UnpackCHARS( to->data, i134->data, n ) ;

   HSMLogMSG( "FN000134;", cmds, NULL ) ;

   if( o134->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000134;",cmds, NULL ) ;
	   return( o134->rc ) ;
   }
   memcpy( o134->tac, tp->mab, 16 );

   HSMLogMSG( "FN000134;", cmds, NULL ) ;
   return( o134->rc ) ;
}

