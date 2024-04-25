#include "suip.h"
#include "hsmapi.h"
/*------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000206 : 檢查銀聯卡之OFFSET與密碼是否正確 (本行卡，本行ATM/財金送來)          */
/*------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000206"，"2T2CPVK ATM   807     "+"PPK ATM   950     "，"0036"+PinBlock(H16)+卡號(N16)+"0000"+帳號(N12)+OFFSET(N4)，""，""，""，Return_Code)

Input Parameters:
key_identify ="2T2CPVK ATM   807     "+"PPK ATM   950     " (本行卡他行ATM/財金送來)
key_identify ="2T2CPVK ATM   807     "+"PPK ATM   ATM_ID  " (本行卡本行ATM)
Input_data_1 ="0036"+PinBlock(H16)+卡號(N16)+"0000"+帳號(N12)+OFFSET(N4)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    pvk[32] ;
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;
	char    len1[4] ;
	char	PinB[16] ;
	char	AcctNo[16] ;	/*  Account Number  */
	char	Valdata[16] ;	/*  Account Number  */
	char	offset[12] ;		/*  offset  */
}	fn206i_t, *pfn206i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
}	fn206o_t, *pfn206o_t ;


int fn206( char* out, char* in ) 
{
   int ksz ;
   char  cmds[512], PVK[50], TPK[50], offset[16] ;
   pfn206i_t i206 = (pfn206i_t) in ;
   pfn206o_t o206 = (pfn206o_t) out ;

   CMDDG *dg = (CMDDG *) cmds ;
   CMDDH *dh = (CMDDH *) cmds ;

   memset( PVK, 0, sizeof(PVK) ) ;
   memset( TPK, 0, sizeof(TPK) ) ;
   memset( offset, 0, sizeof(offset) ) ;
 
   if( i206->keytype == 0x01 ) ksz = 16 ;  
   if( i206->keytype == 0x02 ) ksz = 32 ;  
   if( i206->keytype == 0x03 ) ksz = 48 ;  

   memcpy( PVK, i206->pvk, 32 ) ;
   memcpy( TPK, i206->key, ksz ) ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( dg->cmd, "BK", 2 ) ;          /* Generate IBM 3624 PIN PVV */  
   memcpy( dg->cmd+2, "002", 3 ) ;       /*  001 - ZPK, 002 - TPK */

   if( i206->keytype == 0x02 ) {
       memcpy( dg->cmd+5, "U", 1  ) ;
       memcpy( dg->cmd+6, TPK, 32  ) ;
       memcpy( dg->cmd+38, "U", 1  ) ;
       memcpy( dg->cmd+39, PVK, 32  ) ;
       memcpy( dg->cmd+71, i206->PinB, 16  ) ;
       memcpy( dg->cmd+87, "01", 2  ) ;    /*  PIN BLOCK FORMAT */ 
       memcpy( dg->cmd+89, "06", 2  ) ;    /*  The minimum PIN length. */
       memcpy( dg->cmd+91, i206->AcctNo+4, 12  ) ;   /*  CardNo */
       memcpy( dg->cmd+103, "0123456789012345", 16  ) ;       /*  decimalisation table */
       memcpy( dg->cmd+119, "P", 1  ) ;        
       memcpy( dg->cmd+120, i206->Valdata, 16  ) ;        /*  PIN Validation DATA */
   }

   if( i206->keytype == 0x03 ) {
       memcpy( dg->cmd+5, "T", 1  ) ;
       memcpy( dg->cmd+6, TPK, 48  ) ;
       memcpy( dg->cmd+54, "U", 1  ) ;
       memcpy( dg->cmd+55, PVK, 32  ) ;
       memcpy( dg->cmd+87, i206->PinB, 16  ) ;
       memcpy( dg->cmd+103, "01", 2  ) ;    /*  PIN BLOCK FORMAT */ 
       memcpy( dg->cmd+105, "06", 2  ) ;    /*  Check Length */
       memcpy( dg->cmd+107, i206->AcctNo+4, 12  ) ;   /*  CardNo */
       memcpy( dg->cmd+119, "0123456789012345", 16  ) ;       /*  decimalisation table */
       memcpy( dg->cmd+135, "P", 1  ) ;        
       memcpy( dg->cmd+136, i206->Valdata, 16  ) ;        /*  PIN Validation DATA */
   }

   HSMLogMSG( "FN000206;", cmds, NULL ) ;
   o206->rc = hsmio(cmds,0) ;
   if( o206->rc != 0 && o206->rc != 2 ){ 
       HSMLogMSG( "FN000206;", cmds, NULL ) ;
       return( o206->rc ) ;
  }

  HSMLogMSG( "FN000206;", cmds, NULL ) ;
  memcpy (offset, cmds+4, 12 ) ;
  shuffer(offset, 12 ) ;

  if( strncmp( offset, i206->offset, 12 ) )
	   return( o206->rc = 1 ) ;
  return( o206->rc = 0 ) ;


}

