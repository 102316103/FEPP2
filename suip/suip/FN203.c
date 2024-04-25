#include "suip.h"
#include "hsmapi.h"
/*------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000203 : 檢查金融卡之OFFSET與密碼是否正確_磁條卡(本行卡，本行ATM/財金送來)          */
/*------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000203"，"2S1PVK ATM   807     "+"PPK ATM   950     "，"0036"+卡號(N16)+"0000"+帳號(N12)+OFFSET(N4)，
"0020"+E(OFFSET)(H4)+PinBlock(H16)，""，""，Return_Code)

Input Parameters:
key_identify ="2S1PVK ATM   807     "+"PPK ATM   950     " (本行卡他行ATM/財金送來)
key_identify ="2S1PVK ATM   807     "+"PPK ATM   ATM_ID  " (本行卡本行ATM)
Input_data_1 ="0036"+卡號(N16)+"0000"+帳號(N12)+OFFSET(N4)
Input_data_2 ="0016"+PinBlock(H16)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    pvk[16] ;
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;
	char    len1[4] ;
	char	Valdata[16] ;		 /* Account Number */ 
	char	AcctNo[16] ;
	char	offset[4] ;		/* offset  */
	char    len2[4] ;
	char	PinB[16] ;
}	fn203i_t, *pfn203i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char    newoffset[4] ;
}	fn203o_t, *pfn203o_t ;

int fn203( char* out, char* in ) 
{
   int ksz ;
   char  cmds[512], PVK[50], TPK[50] ;
   pfn203i_t i203 = (pfn203i_t) in ;
   pfn203o_t o203 = (pfn203o_t) out ;

   CMDfSS *fis = (CMDfSS *) cmds ;
   CMDxSS *atm = (CMDxSS *) cmds ;
   CMDST *r = (CMDST *) cmds ;

   memset( PVK, 0, sizeof(PVK) ) ;
   memset( TPK, 0, sizeof(TPK) ) ;
 
   if( i203->keytype == 0x01 ) ksz = 8 ;  
   if( i203->keytype == 0x02 ) ksz = 16 ;  
   if( i203->keytype == 0x03 ) ksz = 24 ;  

   hexncpy( PVK, i203->pvk, 8 ) ;
   hexncpy( TPK, i203->key, ksz ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( fis->cmd, "SS", 2 ) ;
   SSCmdTXT( cmds+2, 3, TPK, ksz, PVK, i203->PinB, i203->AcctNo+4, i203->Valdata, i203->offset, NULL ) ;
   HSMLogMSG( "FN000203;", cmds, NULL ) ;
   o203->rc = hsmio(cmds, 0) ;
   if( o203->rc == 1 )  
	   memcpy( o203->newoffset, r->mab1, 4 ) ;
   else
       memset( o203->newoffset, 0, 4 ) ;
   HSMLogMSG( "FN000203;", cmds, NULL ) ;
   return( o203->rc ) ;
}
