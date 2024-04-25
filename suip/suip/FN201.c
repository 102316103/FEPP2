#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000201 : 產生磁條金融卡密碼                               */
/*----------------------------------------------------------------------------*/
/*
DES_LIB("FN000201"，"1S1PVK ATM   807     "，"0016"+卡號(N16)，""，Return_data_1，""，Return_Code)

Input Parameters:
key_identify ="1S1PVK ATM   807     "
Input_data_1 ="0016"+卡號(N16)
(輸入的卡號可變動前面的長度，例如長度放0014，則後面要接十四位卡號)
Output Parameters:
Return_data_1="0008"+密碼(N4)+OFFSET(N4)
*/
typedef struct	{
	char	f ;					/* FUNCTION CODE */
	char    pvk[16] ;
	char    TPK[48] ;
	char    len[4] ;
	char	CardNo[16] ;		    /* Card Number */
}	fn201i_t, *pfn201i_t ;

typedef struct	{
	int 	rc ;			    /* RETURN CODE  */
	char	offset[8] ;			/* offset */
}	fn201o_t, *pfn201o_t ;

int fn201( char* out, char* in ) 
{
   char cmds[512], EPINB[20], PINB[20], AcctNo[20], PINVALD[20], CVV[10], TMK[50], PVK[20] ;
   pfn201i_t i201 = (pfn201i_t) in ;
   pfn201o_t o201 = (pfn201o_t) out ;

   CMDSO *so = (CMDSO *) cmds ;
   CMDSP *sp = (CMDSP *) cmds ;
   CMDSE *ng = (CMDSE *) cmds ;
   CMDSF *nh = (CMDSF *) cmds ;
   CMDSS *ss = (CMDSS *) cmds ;
   CMDST *st = (CMDST *) cmds ;

   memset( TMK, 0, sizeof( TMK ) ) ;
   memset( PVK, 0, sizeof( PVK ) ) ;
   memset( EPINB, 0, sizeof( EPINB ) ) ;
   memset( PINB, 0, sizeof( PINB ) ) ;
   memset( CVV, 0, sizeof( CVV ) ) ;
   memset( PINVALD, 0, sizeof( PINVALD ) ) ;
   memset( AcctNo, '0', sizeof( AcctNo ) ) ;

   memcpy( PINVALD, i201->CardNo, 16 ) ;
   memcpy( PVK, i201->pvk, 16 ) ;
   memcpy( TMK, i201->TPK, 48 ) ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( so->cmd, "SO", 2 ) ;           /* Generate Encrypted PIN & Offset */
   so->meth = '3' ;
   memcpy( so->pvk, PVK, 16 ) ; ; /* lmk pair 14 */
   memcpy( so->val, PINVALD, 16 ) ;
   memcpy( so->wei, "1111", 4 ) ;
   HSMLogMSG( "FN000201;", cmds, NULL ) ;
   if ( o201->rc = hsmio(cmds,0) ){ 
        return( o201->rc ) ;
   }
   memcpy( CVV, sp->ofs, 4 ) ;
   memcpy( EPINB, "04", 2 ) ;
   memcpy( EPINB+2, CVV, 4 ) ;
   memcpy( EPINB+6, "FFFFFFFFFF", 10 ) ;
   hexncpy( PINB, EPINB, 8 ) ;
   memset( EPINB, 0, sizeof(EPINB ) ) ;
   XorDATA( EPINB, PINB, EPINB, 8 ) ;
   memset( PINB, 0, sizeof(PINB ) ) ;
   UnpackCHARS( PINB, EPINB, 8 ) ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( ng->cmd, "SE", 2 ) ;             
   memcpy( ng->lmkpair, "14", 2 ) ;             
   memcpy( ng->key, "T", 1  ) ;
   memcpy( ng->key+1, TMK, 48  ) ;
   memcpy( ng->key+49, "0000000000000000", 16  ) ;
   memcpy( ng->key+65, PINB, 16  ) ;
   if ( o201->rc = hsmio(cmds,0) ){ 
        return( o201->rc ) ;
   }
   memset( EPINB, 0, sizeof( EPINB ) ) ;
   memcpy( EPINB, nh->mab, 16  ) ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( ss->cmd, "SS3", 3 ) ;              
   memcpy( ss->tpk, "T", 1 ) ;              
   memcpy( ss->tpk+1, TMK, 48 ) ;              
   memcpy( ss->tpk+49, PVK, 16  ) ;
   memcpy( ss->tpk+65, EPINB, 16  ) ;
   memcpy( ss->tpk+81, AcctNo, 12  ) ;
   memcpy( ss->tpk+93, PINVALD, 16  ) ;
   memcpy( ss->tpk+109, "0000", 4  ) ;

   HSMLogMSG( "FN000201;", cmds, NULL ) ;
   o201->rc = hsmio(cmds,0) ;
   if( o201->rc != 1 )
   { 
       HSMLogMSG( "FN000201;", cmds, NULL ) ;
       return( o201->rc ) ;
   }
   o201->rc = 0 ;
   memcpy( o201->offset, CVV, 4) ;
   memcpy( o201->offset+4, st->mab1, 4) ;

   HSMLogMSG( "FN000201;", cmds, NULL ) ;
   return( o201->rc ) ;

}

