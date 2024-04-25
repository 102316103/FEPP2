#include "suip.h"
#include "hsmapi.h"
/*-----------------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000208 : UE command 檢查金融卡之12 byte OFFSET與密碼是否正確_磁條卡(本行卡，本行ATM/財金送來)  */
/*-----------------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000208"，"2S1PVK ATM   807     "+"PPK ATM   950     "，"0044"+卡號(N16)+"0000"+帳號(N12)+OFFSET(12)，
"0020"+E(OFFSET)(H4)+PinBlock(H16)，""，""，Return_Code)

Input Parameters:
key_identify ="2S1PVK ATM   807     "+"PPK ATM   950     " (本行卡他行ATM/財金送來)
key_identify ="2S1PVK ATM   807     "+"PPK ATM   ATM_ID  " (本行卡本行ATM)
Input_data_1 ="0044"+卡號(N16)+"0000"+帳號(N12)+OFFSET(12)
Input_data_2 ="0016"+PinBlock(H16)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    meth ;          /* 0x00 - Verify offset, 0x01 - Generate offset */
	char    pvk[16] ;
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;
	char    len1[4] ;
	char	Valdata[16] ;		 /* Account Number */ 
	char	AcctNo[16] ;
	char	offset[12] ;		/* offset */ 
	char    len2[4] ;
	char	PinB[16] ;
}	fn208i_t, *pfn208i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char    newoffset[12] ;
}	fn208o_t, *pfn208o_t ;

int fn208( char* out, char* in ) 
{
   int ksz, meth ;
   char  cmds[512], PVK[50], TPK[50], offset[16] ;
   pfn208i_t i208 = (pfn208i_t) in ;
   pfn208o_t o208 = (pfn208o_t) out ;

   CMDSS *ss = (CMDSS *) cmds ;
   CMDST *r = (CMDST *) cmds ;

   memset( PVK, 0, sizeof(PVK) ) ;
   memset( TPK, 0, sizeof(TPK) ) ;
   memset( offset, 0, sizeof(offset) ) ;

   meth = i208->meth ;
   if( i208->keytype == 0x01 ) ksz = 16 ;  
   if( i208->keytype == 0x02 ) ksz = 32 ;  
   if( i208->keytype == 0x03 ) ksz = 48 ;  

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( ss->cmd, "UE8", 3 ) ; 
   if ( ksz == 16 )
      *ss->tpk = 'Z' ;
   else
      *ss->tpk = (ksz == 32 ? 'U' : 'T') ;
   memcpy( ss->tpk+1, i208->key, ksz ) ;
   memcpy( ss->tpk+(ksz+1), i208->pvk, 16 ) ;
   memcpy( ss->tpk+(ksz+17), i208->PinB, 16 ) ;
   memcpy( ss->tpk+(ksz+33), i208->AcctNo+4, 12 ) ;
   memcpy( ss->tpk+(ksz+45), i208->Valdata, 16 ) ;
   memcpy( ss->tpk+(ksz+61), "0000FFFFFFFF", 12  ) ;

  HSMLogMSG( "FN000208;", cmds, NULL ) ;
  o208->rc = hsmio(cmds, 0) ;
  if( o208->rc != 1  ){ 
       HSMLogMSG( "FN000208;", cmds, NULL ) ;
       return( o208->rc ) ;
  }
  HSMLogMSG( "FN000208;", cmds, NULL ) ;
  memcpy (offset, cmds+4, 12 ) ;
  shuffer(offset, 12 ) ;

  if( meth == 0 ){
      if( !strncmp( offset, i208->offset, 12 ) )
          return( o208->rc = 0 ) ;
	  else
          return( o208->rc = 1 ) ;
  }
  if( meth == 1 ){
	    memcpy (o208->newoffset, offset, 12 ) ;
		return( o208->rc = 0 ) ;
  }
}
