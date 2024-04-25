#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000103 : 與財金或銀行間換key成功時，異動key欄位           */
/*----------------------------------------------------------------------------*/
/*
DES_LIB("FN000103"，"1T2MAC OPC   950   1 "，""，""，""，""，Return_Code) ;
key_identify ="1S1MAC OPC   950   1 "
*/
typedef struct	{
	char	f ;				/* FUNCTION CODE X'B8'  */
	int 	KeyID ;			/* KEY ID X'60' , X'70' */
	char    keytype1 ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char	bnkid[3] ;		/* BACNK CODE  */
}	fn103i_t, *pfn103i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE          */
}	fn103o_t, *pfn103o_t ;

int fn103( char* out, char* in ) 
{
   char bankid[10] ;
   pfn103i_t i103 = (pfn103i_t) in ;
   pfn103o_t o103 = (pfn103o_t) out ;

   memset(bankid, 0, sizeof(bankid) ) ;

   memcpy( bankid, i103->bnkid, 3 ) ;
/*
   HSMLogMSG( "FN000103;", bankid, NULL ) ;
   
   if( i103->keytype1 == 0x01 ){
	   if( !memcmp( bankid, "950", 3 ) ){
           o103->rc = ReplaceNewKEY( "888", i103->KeyID ) ;
	   }
	   else{
           o103->rc = ReplaceNewKEY( bankid, i103->KeyID ) ;
	   }
   }

   o103->rc = ReplaceNewKEY( bankid, i103->KeyID ) ;
   HSMLogMSG( "FN000103;", bankid, NULL ) ;


   DesErr( des_return_code, rc ) ;
   sprintf( logtxt, "%s,%s,%s,%s,%s,%s,%s,%s ", "FN000103",key_identify,input1,input2,"",return1,return2, des_return_code ) ;
   WriteLOG( ErrMSG, ' ', logtxt, strlen(logtxt) ) ;
*/
   return( o103->rc = 0 ) ;
}

