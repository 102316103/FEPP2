#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000205 : 產生磁條金融卡密碼                               */
/*----------------------------------------------------------------------------*/
/*
DES_LIB("FN000205"，"1S1PVK ATM   807     "，"0048"+PinBlock(H16)+卡號(N16)+"0000"+帳號(N12)，""，Return_data_1，""，Return_Code)

Input Parameters:
key_identify ="1S1PVK ATM   807     "
Input_data_1 ="0048"+PinBlock(H16)+卡號(N16)+"0000"+帳號(N12)
Output Parameters:
Return_data_1=OFFSET(H12)
*/
typedef struct	{
	char	f ;					/* FUNCTION CODE */
	char    pvk[32] ;
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;
	char    len1[4] ;
	char	PinB[16] ;
	char	AcctNo[16] ;
	char	Valdata[16] ;		 /* Account Number */ 
}	fn205i_t, *pfn205i_t ;

typedef struct	{
	int 	rc ;			    /* RETURN CODE  */
	char	offset[12] ;		/* offset */
}	fn205o_t, *pfn205o_t ;

int fn205( char* out, char* in ) 
{
   int ksz ;
   char  cmds[512], PVK[50], TPK[50], offset[16] ;
   pfn205i_t i205 = (pfn205i_t) in ;
   pfn205o_t o205 = (pfn205o_t) out ;

   CMDDG *dg = (CMDDG *) cmds ;
   CMDDH *dh = (CMDDH *) cmds ;
   
   memset( PVK, 0, sizeof(PVK) ) ;
   memset( TPK, 0, sizeof(TPK) ) ;
   memset( offset, 0, sizeof(offset) ) ;

   if( i205->keytype == 0x01 ) ksz = 16 ;  
   if( i205->keytype == 0x02 ) ksz = 32 ;  
   if( i205->keytype == 0x03 ) ksz = 48 ;  

   memcpy( PVK, i205->pvk, 32 ) ;
   memcpy( TPK, i205->key, ksz ) ;
  
   memset( cmds, 0, sizeof( cmds ) ) ;   
   memcpy( dg->cmd, "BK", 2 ) ;          /* Generate IBM 3624 PIN PVV */  
   memcpy( dg->cmd+2, "002", 3 ) ;       /*  TPK */

   if( i205->keytype == 0x02 ) {
       memcpy( dg->cmd+5, "U", 1  ) ;
       memcpy( dg->cmd+6, TPK, 32  ) ;
       memcpy( dg->cmd+38, "U", 1  ) ;
       memcpy( dg->cmd+39, PVK, 32  ) ;
       memcpy( dg->cmd+71, i205->PinB, 16  ) ;
       memcpy( dg->cmd+87, "01", 2  ) ;    /*  PIN BLOCK FORMAT */ 
       memcpy( dg->cmd+89, "06", 2  ) ;    /*  Check Length */
       memcpy( dg->cmd+91, i205->AcctNo+4, 12  ) ;   /*  CardNo */
       memcpy( dg->cmd+103, "0123456789012345", 16  ) ;       /*  decimalisation table */
       memcpy( dg->cmd+119, "P", 1  ) ;        
       memcpy( dg->cmd+120, i205->Valdata, 16  ) ;        /*  PIN Validation DATA */
   }
   if( i205->keytype == 0x03 ) {
       memcpy( dg->cmd+5, "T", 1  ) ;
       memcpy( dg->cmd+6, TPK, 48  ) ;
       memcpy( dg->cmd+54, "U", 1  ) ;
       memcpy( dg->cmd+55, PVK, 32  ) ;
       memcpy( dg->cmd+87, i205->PinB, 16  ) ;
       memcpy( dg->cmd+103, "01", 2  ) ;    /*  PIN BLOCK FORMAT */ 
       memcpy( dg->cmd+105, "06", 2  ) ;    /*  Check Length */
       memcpy( dg->cmd+107, i205->AcctNo+4, 12  ) ;   /*  CardNo */
       memcpy( dg->cmd+119, "0123456789012345", 16  ) ;       /*  decimalisation table */
       memcpy( dg->cmd+135, "P", 1  ) ;        
       memcpy( dg->cmd+136, i205->Valdata, 16  ) ;        /*  PIN Validation DATA */
   }

   HSMLogMSG( "FN000205;", cmds, NULL ) ;
   o205->rc = hsmio(cmds,0) ;
   if( o205->rc != 0 && o205->rc != 2 ){ 
       HSMLogMSG( "FN00025;", cmds, NULL ) ;
       return( o205->rc ) ;
  }
   memcpy (offset, cmds+4, 12 ) ;
   shuffer(offset, 12 ) ;

   memcpy( o205->offset, offset, 12) ;  /*  offset */ 
   HSMLogMSG( "FN000205;", cmds, NULL ) ;
   return( o205->rc = 0 ) ;
}
