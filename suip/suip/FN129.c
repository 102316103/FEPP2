#include "suip.h"
#include "hsmapi.h"
/*-----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000129 : 下傳個人化基碼2－功能碼87                         */
/*-----------------------------------------------------------------------------*/
/*
DES_LIB("FN000129"，"2T3ICC C1    807   1 T3ICC C6    807 "，"0064"+ICCID(H16)+RandomNumber1(H16)+RandomNumber2(H16)+DivData(H48)，""，Return_data_1，""，Return_Code)

Input Parameters:
Input_data_1="0064"+ICCID(H16)+R1（FROM TERMINAL）+ R2（FROM IC CARD）+ DivData(H48)

Output Parameters:
Return_data_1="0016"+亂碼後之下傳基碼

 RG7000/RG8000  TU/TV command  
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    key1[48] ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key2[48] ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    len[4] ;
	char    iccid[16] ;
	char    ran1[16] ;
	char    ran2[16] ;
	char    DivData[48] ;
}	fn129i_t, *pfn129i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	divkses[48] ;   	/*  divkses(H24)    */
}	fn129o_t, *pfn129o_t ;

int fn129( char* out, char* in ) 
{
   char cmds[512] ;
   pfn129i_t i129 = (pfn129i_t) in ;
   pfn129o_t o129 = (pfn129o_t) out ;

   CMDxTU *tu = (CMDxTU *) cmds ;
   CMDTV *tv = (CMDTV *) cmds ;
   

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( tu->cmd, "TU", 2 ) ;
   tu->zmkksz ='T' ;
   memcpy( tu->zmk, i129->key1, 48 ) ;  /* C1KEY just for generate session key */
   tu->tmkksz ='T' ;
   memcpy( tu->tmk, i129->key2, 48 ) ;  /* TMK, use C6 for testing for generate div key */
   memcpy( tu->iccid, i129->iccid, 16 ) ;
   memcpy( tu->hostdata, i129->ran1, 16 ) ;
   memcpy( tu->ifddata, i129->ran2, 16 ) ;
   tu->divlen ='3' ;
   memcpy( tu->divdata, i129->DivData, 48 ) ;
   tu->ttmksz = 'T' ;
   memcpy( tu->ttmk, i129->key2, 48 ) ;  /* TMK, use C6 for testing for trasmission */
   tu->kschm ='Y' ;

   HSMLogMSG( "FN000129;", cmds, NULL ) ;
   if( o129->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000129;", cmds, NULL ) ;
	   return( o129->rc ) ;
   }
   memcpy( o129->divkses, tv->divkses+2, 48 ) ;

   HSMLogMSG( "FN000129;",cmds, NULL ) ;
   return( o129->rc ) ;
}

