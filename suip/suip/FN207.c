#include "suip.h"
#include "hsmapi.h"
/*------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000207 : UE command 產生磁條金融卡密碼 4-12PIN, 12bye offset      */
/*------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000207"，"1S1PVK ATM   807     "，"0016"+卡號(N16)，""，Return_data_1，""，Return_Code)

Input Parameters:
key_identify ="1S1PVK ATM   807     "
Input_data_1 ="0016"+卡號(N16)+"0000"+帳號(N12)
Output Parameters:
Return_data_1="0016"+密碼(N4)+OFFSET(12)
*/
typedef struct	{
	char	f ;					/* FUNCTION CODE */
	char    pvk[16] ;
	char    TPK[48] ;
	char    len[4] ;
	char	Valdata[16] ;		 
	char	AcctNo[16] ;       /*  Account Number */ 
}	fn207i_t, *pfn207i_t ;


typedef struct	{
	int 	rc ;			    /* RETURN CODE  */
	char	offset[16] ;			/* offset */
}	fn207o_t, *pfn207o_t ;

int fn207( char* out, char* in ) 
{
   char cmds[512], EPINB[20], PINB[20], AcctNo[20], PINVALD[20], CVV[10], TMK[50], PVK[20], offset[16] ;
   pfn207i_t i207 = (pfn207i_t) in ;
   pfn207o_t o207 = (pfn207o_t) out ;

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
   memset( AcctNo, 0, sizeof( AcctNo ) ) ;
   memset( offset, 0, sizeof(offset) ) ;

   memcpy( PINVALD, i207->Valdata, 16 ) ;
   memcpy( AcctNo, i207->AcctNo, 16 ) ;
   memcpy( PVK, i207->pvk, 16 ) ;
   memcpy( TMK, i207->TPK, 48 ) ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( so->cmd, "SO", 2 ) ;           /* Generate Encrypted PIN & Offset */
   so->meth = '3' ;
   memcpy( so->pvk, PVK, 16 ) ; ; /*  lmk pair 14 */
   memcpy( so->val, PINVALD, 16 ) ;
   memcpy( so->wei, "1111", 4 ) ;
   HSMLogMSG( "FN000207;", cmds, NULL ) ;
   if ( o207->rc = hsmio(cmds,0) ){ 
        return( o207->rc ) ;
   }
   memset( EPINB, 0xff, 8 ) ;
   memcpy( CVV, sp->ofs, 4 ) ;
   *EPINB = 4 ;
   hexncpy( EPINB+1, CVV, 2 ) ;
   hexncpy( PINB+2, AcctNo+4, 6 ) ;  
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
   if ( o207->rc = hsmio(cmds,0) ){ 
        return( o207->rc ) ;
   }
   memset( EPINB, 0, sizeof( EPINB ) ) ;
   memcpy( EPINB, nh->mab, 16  ) ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( ss->cmd, "UE8", 3 ) ;              
   memcpy( ss->tpk, "T", 1 ) ;              
   memcpy( ss->tpk+1, TMK, 48 ) ;              
   memcpy( ss->tpk+49, PVK, 16  ) ;
   memcpy( ss->tpk+65, EPINB, 16  ) ;
   memcpy( ss->tpk+81, AcctNo+4, 12  ) ;
   memcpy( ss->tpk+93, PINVALD, 16  ) ;
   memcpy( ss->tpk+109, "0000FFFFFFFF", 12  ) ;

   HSMLogMSG( "FN000207;", cmds, NULL ) ;
   o207->rc = hsmio(cmds,0) ;
   if( o207->rc != 1 )
   { 
       HSMLogMSG( "FN000207;", cmds, NULL ) ;
       return( o207->rc ) ;
   }
   HSMLogMSG( "FN000207;", cmds, NULL ) ;
   o207->rc = 0 ;
   memcpy( o207->offset, CVV, 4) ;
   memcpy (offset, st->mab1, 12) ;
   shuffer(offset, 12 ) ;
   memcpy( o207->offset+4, offset, 12) ;
   *(o207->offset+16) = 0 ;
   return( o207->rc ) ;

}

