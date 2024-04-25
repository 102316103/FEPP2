#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"

/*-------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000601 : 產生MAC DATA欄位資料                                */
/*-------------------------------------------------------------------------------*/
/*
DES_LIB("FN000601"，key_identify，Input_data_1，""，Return_data_1，""，Return_Code)

Input Parameters:
key_identify =""1T2PPK ATM   950     " (本行聯銀卡財金送來)
key_identify ="1T3 PPK ATM   ATM_ID  " (本行卡本行ATM)
Input_data_1 ="00XX"+卡號(N12)+PIN(N4~12) PS.VISA/Master PIN 4~12位

Output Parameters:
Output_data = "0016"+PINDBlock(H16)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;      /* 0x01= 16, 0x02 = 32, 0x03= 48 */
	char	datalen[4] ;   /* INPUT1 DATA */
	char	data[32] ;	   
}	fn601i_t, *pfn601i_t ;


typedef struct	{
	int 	rc ;
	char	pinb[16] ;
}	fn601o_t, *pfn601o_t ;

int fn601( char* out, char* in ) 
{
   int  n, mklen, pinlen ;
   char cmds[512], mackey[50], PINB[16], EPINB[16] ; 
   pfn601i_t i601 = (pfn601i_t) in ;
   pfn601o_t o601 = (pfn601o_t) out ;

   CMDSE_t *tse = (CMDSE_t *) cmds ;
   CMDSE *se = (CMDSE *) cmds ;
   CMDSF *sf = (CMDSF *) cmds ;
   
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( mackey, 0, sizeof(mackey) ) ;

   if( i601->keytype == 0x01 )
       memcpy( mackey, i601->key, 16 ) ;
   else
   if( i601->keytype == 0x02 ){
       memcpy( mackey, "U", 1 ) ;
       memcpy( mackey+1, i601->key, 32 ) ;
   }
   else
   if( i601->keytype == 0x03 ){
       memcpy( mackey, "T", 1 ) ;
 	   memcpy( mackey+1, i601->key, 48 ) ;
   }
   else
       return( o601->rc = 04 ) ;
   mklen = strlen( mackey ) ;
   n = ldrint( i601->datalen, sizeof(i601->datalen) ) ;   

   pinlen = n - 12 ;

   memset(PINB, 'F', 16 ) ;
   strint( pinlen, PINB, 2 ) ;
   memcpy( PINB+2, i601->data+12, pinlen ) ;
   hexncpy( EPINB, PINB, 8 ) ;
   memset( PINB, 0, sizeof(PINB ) ) ;
   hexncpy( PINB+2, i601->data, 6 ) ;    /* ActNo */ 
   XorDATA( EPINB, PINB, EPINB, 8 ) ;
   memset( PINB, 0, sizeof(PINB ) ) ;
   UnpackCHARS( PINB, EPINB, 8 ) ;

   memcpy( cmds, "SE", 2 ) ;
   memcpy( cmds+2, "14", 2 ) ;
   memcpy( cmds+4, mackey, mklen ) ;
   memcpy( cmds+4+mklen, "0000000000000000", 16 ) ;
   memcpy( cmds+20+mklen, PINB, 16 );

   HSMLogMSG( "FN000601;", cmds, NULL ) ;
   if( o601->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000601;", cmds, NULL ) ;
       return( o601->rc ) ;
   }
   memcpy( o601->pinb, sf->mab, 16 ) ;
   HSMLogMSG( "FN000601;", cmds, NULL ) ;
   return( o601->rc ) ;
}

