#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"
/*------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000416 : CBC mode Diversify Session Key Encrypt/Decrypt Message   */
/*------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000416"£¬"1T3ICC C6    807     "£¬Input_data_1£¬Input_data_2£¬""£¬""£¬Return_Code)

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
	char    icv2[16] ;
	char    msg[256] ;
}	fn416i_t, *pfn416i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	tac[256] ;   	    /*  tac(H8)    */
}	fn416o_t, *pfn416o_t ;

int fn416( char* out, char* in ) 
{
   int dsz, divlen, n, count, iksz, meth ;
   char cmds[512], xcmds[512], divkey[50], msg[512], tmp[1024], ekey[24], iv[8], mac[8], divdata[64], xicv[16], temp[16], block[16], work[16];
   pfn416i_t i416 = (pfn416i_t) in ;
   pfn416o_t o416 = (pfn416o_t) out ;

   CMDSE_t *tse = (CMDSE_t *) cmds ;
   CMDSE *se = (CMDSE *) cmds ;
   CMDSF *sf = (CMDSF *) cmds ;
  
   divlen = ldrint(i416->inlen, 4) - 16 ;    /*  divdata length */
      dsz = ldrint(i416->msg_len, 4) - 16 ;       /*  macvdata length */
     meth = i416->meth ;

   count = divlen/16 ;
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( xcmds, 0, sizeof(xcmds) ) ;
   memset( divkey, 0, sizeof(divkey) ) ;
   memset( ekey, 0, sizeof(ekey) ) ;

   hexncpy( iv, i416->icv2, 8 ) ;

   memcpy(cmds, "SE14", 4 ) ;
   if( i416->keytype == 0x02 ){
       memcpy(cmds+4, "U", 1 ) ;
       memcpy(cmds+5, i416->key,  iksz = 32 ) ;  /* MAC seed KEY */
   }
   if( i416->keytype == 0x03 ){
       memcpy(cmds+4, "T", 1 ) ;
       memcpy(cmds+5, i416->key,  iksz = 48 ) ;  /* MAC seed KEY */
   }
   memcpy( cmds+5+iksz, "0000000000000000", 16 ) ;
   hexncpy( xicv, i416->icv, 8 ) ;

   for( n = 0; n < count; n++ ){
	    hexncpy(block, i416->divdata+(n*16), 8);
        XorDATA( temp, block, xicv, 8 );
		UnpackCHARS( cmds+21+iksz, temp, 8 ) ;
        memcpy( xcmds, cmds, strlen(cmds) ) ;
        if( o416->rc = hsmio(xcmds, 0) ){
            HSMLogMSG( "FN000416;", xcmds, NULL ) ;
            return( o416->rc ) ;
        }
		hexncpy( xicv, xcmds+4, 8 ) ;
        memcpy( divkey+(n*16), xcmds+4, 16 ) ;
  }   

  hexncpy( ekey, divkey, 24 ) ;   /*  triple length DES DivKEY */

  dsz = ( dsz % 16 ? dsz/16 + 1 : dsz/16 ) ;
  memset( tmp, '0', sizeof(tmp) ) ;
  memcpy( tmp, i416->msg, dsz = dsz * 16 ) ;
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
   strint(dsz = dsz*2, o416->tac, 4 ) ;
   if( meth == 4 )
       UnpackCHARS( o416->tac+4, mac, dsz );
   else
	   UnpackCHARS( o416->tac+4, tmp, dsz );
   *(o416->tac+4+dsz) = 0 ;
   return( o416->rc ) ;
}
