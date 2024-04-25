#include "suip.h"
#include "hsmapi.h"
/*------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000213 : 檢查金融卡之新密碼是否正確_磁條卡                                          */
/*------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000213"，"2S1PVK ATM   807     "+"PPK ATM   ATM_ID   "，"0052"+卡號(N16)+"0000"+帳號(N12)+OldPinBlock(H16)+OFFSET(N4)，
"0016"+NewPinBlock(H16)，""，Return_data_2，Return_Code)

Input Parameters:
key_identify ="2S1PVK ATM   807     "+" PPK ATM   ATM_ID  " 
Input_data_1 ="0052"+卡號(N16)+"0000"+帳號(N12)+OldPinBlock(H16)+OFFSET(N4)
Input_data_2 ="0016"+NewPinBlock(H16)
Output Parameters:
Return_data_2="0004"+NewOFFSET(H4)
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    pvk[16] ;
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;
	char    len[4] ;
	char    NPINB[16] ;
	char    input1[56] ;
}	fn213i_t, *pfn213i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	NewOffset[4] ;	/* offset */
}	fn213o_t, *pfn213o_t ;

int fn213( char* out, char* in ) 
{
   int pvklen, tpklen ;
   char cmds[512], TPK[50], PVK[50] ;
   pfn213i_t i213 = (pfn213i_t) in ;
   pfn213o_t o213 = (pfn213o_t) out ;

   CMDSN *r = (CMDSN *) cmds ;

   memset( PVK, 0, sizeof(PVK) ) ;
   memset( TPK, 0, sizeof(TPK) ) ;

   memcpy( PVK, i213->pvk, pvklen = 16 ) ;

   if( i213->keytype == 0x01 )
        memcpy( TPK, i213->key, 16 ) ;
   if( i213->keytype == 0x02 ){
        memcpy( TPK, "U", 1 ) ;
        memcpy( TPK+1, i213->key, 32 ) ;
   }
   if( i213->keytype == 0x03 ){
       memcpy( TPK, "T", 1 ) ;
       memcpy( TPK+1, i213->key, 48 ) ;
   }

   tpklen = strlen(TPK) ;

   memset(cmds, 0, sizeof(cmds) ) ;

   strcpy( cmds, "SM3" ) ;
   memcpy( cmds+3, TPK, tpklen ) ;
   memcpy( cmds+3+tpklen, PVK, pvklen  );         /* PVK */
   memcpy( cmds+3+tpklen+pvklen, i213->input1+36, 16 ) ;  /*  OldPinBlock */
   memcpy( cmds+19+tpklen+pvklen, i213->NPINB, 16 ) ;
   memcpy( cmds+35+tpklen+pvklen, i213->input1+24, 12 ) ;       /* 12byte */
   memcpy( cmds+47+tpklen+pvklen, i213->input1+4, 16 );       /* 16byte */
   memcpy( cmds+63+tpklen+pvklen, i213->input1+52, 4 ) ;       /* 4byte */

   HSMLogMSG( "FN000213;", cmds, NULL ) ;
   if( o213->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000213;", cmds, NULL ) ;
       return(o213->rc) ;
   }
   memcpy( o213->NewOffset, r->ofs, 4 ) ;

   HSMLogMSG( "FN000213;",cmds, NULL ) ;
   return(o213->rc) ;
}

