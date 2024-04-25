#include "suip.h"
#include "hsmapi.h"
/*-----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000130 :晶片金融卡個人化基碼產生－功能碼90                 */
/*-----------------------------------------------------------------------------*/
/*
DES_LIB("FN000130"，"2T3ICC C6    807   1 T3ICC C0    807 "，"0048"+DivData(H48)，""，Return_data_1，""，Return_Code)

Input Parameters:
Input_data_1="0048"+DivData(H48)

Output Parameters:
Return_data_1="0048"+晶片金融卡個人化基碼

 RG7000/RG8000  TO/TP 
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    key1[48] ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key2[48] ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    len[4] ;
	char    DivData[48] ;
}	fn130i_t, *pfn130i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	divktmk[48] ;   /*  divktmk(H24)    */
}	fn130o_t, *pfn130o_t ;


int fn130( char* out, char* in ) 
{
   char cmds[512] ;
   pfn130i_t i130 = (pfn130i_t) in ;
   pfn130o_t o130 = (pfn130o_t) out ;

   CMDxTO *to = (CMDxTO *) cmds ;
   CMDxTP *tp = (CMDxTP *) cmds ;
   
   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( to->cmd, "TO", 2 ) ;
   to->tmkksz = 'T' ;
   memcpy(to->tmk, i130->key1, 48 ) ;  /* seedkey */
   to->divlen= '3' ;
   memcpy( to->divdata, i130->DivData, 48 ) ;
   to->ttmksz = 'T' ;
   memcpy(to->ttmk, i130->key2, 48 ) ;  /* Transmission KEY */
   to->kschm ='Y' ;

   HSMLogMSG( "FN000130;", cmds, NULL ) ;
   if( o130->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000130;", cmds, NULL ) ;
	   return( o130->rc) ;
   }
   memcpy( o130->divktmk, tp->divktmk, 48 );
   HSMLogMSG( "FN000130;", cmds, NULL ) ;
   return( o130->rc ) ;
}

 