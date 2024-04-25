#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"
/*---------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000415 : Diversify Session Key Encrypt/Decrypt Message   */
/*---------------------------------------------------------------------------*/
/*
DES_LIB("FN000415"£¬"1T3ICC C6    807     "£¬Input_data_1£¬Input_data_2£¬""£¬""£¬Return_Code)

Input Parameters:
Output Parameters£ºN/A
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'4F' */
	char    meth ;          /* 0x00 - ECB_ENC, 0x01 - ECB_DEC, 0x02 - CBC_ENC, 0x03 - CBC_DEC, 0x04 - MAC */
	char    keytype ;
	char    key[48] ;
	char    inlen[4] ;
	char    icv[16] ;
	char    divdata[48] ;
	char    msg_len[4] ;
	char    msg[256] ;
}	fn415i_t, *pfn415i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	tac[256] ;   	    /*  tac(H8)    */
}	fn415o_t, *pfn415o_t ;

int fn415( char* out, char* in ) 
{
   int dsz, divlen, n, count, iksz, meth ;
   char cmds[512], xcmds[512], divkey[50], msg[512], tmp[1024], ekey[24], iv[8], mac[8] ;
   pfn415i_t i415 = (pfn415i_t) in ;
   pfn415o_t o415 = (pfn415o_t) out ;

   CMDSE_t *tse = (CMDSE_t *) cmds ;
   CMDSE *se = (CMDSE *) cmds ;
   CMDSF *sf = (CMDSF *) cmds ;
  
   divlen = ldrint(i415->inlen, 4) - 16 ;    /*  divdata length */
      dsz = ldrint(i415->msg_len, 4) ;       /*  macvdata length */
     meth = i415->meth ;

   count = divlen/16 ;
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( xcmds, 0, sizeof(xcmds) ) ;
   memset( divkey, 0, sizeof(divkey) ) ;
   memset( ekey, 0, sizeof(ekey) ) ;

   hexncpy( iv, i415->icv, 8 ) ;

   memcpy(cmds, "SE14", 4 ) ;
   if( i415->keytype == 0x02 ){
       memcpy(cmds+4, "U", 1 ) ;
       memcpy(cmds+5, i415->key,  iksz = 32 ) ;  /* MAC seed KEY */
   }
   if( i415->keytype == 0x03 ){
       memcpy(cmds+4, "T", 1 ) ;
       memcpy(cmds+5, i415->key,  iksz = 48 ) ;  /* MAC seed KEY */
   }
   memcpy( cmds+5+iksz, "0000000000000000", 16 ) ;
   for( n = 0; n < count; n++ ){
        memcpy( cmds+21+iksz, i415->divdata+(n*16), 16 ) ;
        memcpy( xcmds, cmds, strlen(cmds) ) ;
        if( o415->rc = hsmio(xcmds, 0) ){
            HSMLogMSG( "FN000415;", xcmds, NULL ) ;
            return( o415->rc ) ;
        }
        memcpy( divkey+(n*16), xcmds+4, 16 ) ;
  }   

  hexncpy( ekey, divkey, 24 ) ;

  dsz = ( dsz % 16 ? dsz/16 + 1 : dsz/16 ) ;
  memset( tmp, '0', sizeof(tmp) ) ;
  memcpy( tmp, i415->msg, dsz = dsz * 16 ) ;
  hexncpy( msg, tmp, dsz = dsz/2) ;

  if( meth == 0 ){
      p_ENCRYPT3_ECB(tmp, msg, dsz, ekey ) ;
  }
  if( meth ==  1 ){
      p_DECRYPT3_ECB(tmp, msg, dsz, ekey ) ;
  }
  if( meth == 2 ){
      p_ENCRYPT3_CBC(tmp, msg, dsz, iv, ekey ) ;
  }
  if( meth == 3 ){
      p_DECRYPT3_CBC(tmp, msg, dsz, iv, ekey ) ;
  }
  if( meth == 4 ){
      p_ENCRYPT3_CBC(tmp, msg, dsz, iv, ekey ) ;
	  memcpy( mac, tmp+dsz-8,  8 ) ;
	  dsz = 8 ;
  }
   strint(dsz = dsz*2, o415->tac, 4 ) ;
   if( meth == 4 )
       UnpackCHARS( o415->tac+4, mac, dsz );
   else
	   UnpackCHARS( o415->tac+4, tmp, dsz );
   *(o415->tac+4+dsz) = 0 ;
   return( o415->rc ) ;
}
