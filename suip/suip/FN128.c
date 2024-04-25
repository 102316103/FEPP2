#include "suip.h"
#include "hsmapi.h"
/*-----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000128 : 下傳個人化基碼1－功能碼86                         */
/*-----------------------------------------------------------------------------*/
/*
DES_LIB("FN000128"，"2T3ICC C1    807   1 ICC D6    807"，"0048"+ICCID(H16)+RandomNumber1(H16)+RandomNumber2(H16)，""，Return_data_1，""，Return_Code)

Input Parameters:
Input_data_1="0048"+ICCID(H16)+R1（FROM TERMINAL）+ R2（FROM IC CARD）

Output Parameters:
Return_data_1="0016"+亂碼後之下傳基碼
*/
typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    key1[48] ;
	char    key2[48] ;
	char    len[4] ;
	char    iccid[16] ;
	char    ran1[16] ;
	char    ran2[16] ;
}	fn128i_t, *pfn128i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	divkey[48] ;   	/*  divkey(H24)    */
}	fn128o_t, *pfn128o_t ;

int fn128( char* out, char* in ) 
{
   char cmds[512], ickey[50], c1key[50] ;
   pfn128i_t i128 = (pfn128i_t) in ;
   pfn128o_t o128 = (pfn128o_t) out ;

   CMDxTS *ts = (CMDxTS *) cmds ;
   CMDTT *tt = (CMDTT *) cmds ;

   memset( c1key, 0, sizeof(c1key) ) ;
   memset( ickey, 0, sizeof(ickey) ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( ts->cmd, "TS", 2 ) ;
   ts->zmkksz ='T' ;
   memcpy( ts->zmk, i128->key1, 48 ) ;  /* C1KEY 04 pair */
   ts->tmkksz ='T' ;
   memcpy( ts->tmk, i128->key2, 48 ) ;  /* TMK, use C3 fro testing */
   memcpy( ts->iccid, i128->iccid, 16 ) ;
   memcpy( ts->hostdata, i128->ran1, 16 ) ;
   memcpy( ts->ifddata, i128->ran2, 16 ) ;
   HSMLogMSG( "FN000128;", cmds, NULL ) ;
   if( o128->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000128;", cmds, NULL ) ;
	   return(o128->rc) ;
   }
   memcpy( o128->divkey, tt->divkey, 48 ) ;
   HSMLogMSG( "FN000128;", cmds, NULL ) ;
   return( o128->rc ) ;
}

