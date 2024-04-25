#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"
/*---------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000414 :²£¥ÍDIVKEY, Translate Data Block                 */
/*---------------------------------------------------------------------------*/
/*
DES_LIB("FN000414"¡A"1T3ICC C6    807     "¡AInput_data_1¡AInput_data_2¡A""¡A""¡AReturn_Code)

*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    keytype ;
	char    key[48] ;
	char    inlen[4] ;
	char    icv[16] ;
	char    divdata[48] ;
	char    dst_keytype ;
	char    dst_key[48] ;
	char    dst_inlen[4] ;
	char    dst_icv[16] ;
	char    dst_divdata[48] ;
	char    msg_len[4] ;
	char    msg[128] ;
}	fn414i_t, *pfn414i_t ;

typedef struct	{
	int 	rc ;			    /* RETURN CODE  */
	char	encdata[128] ; 	    /* encrypt data (H8)    */
}	fn414o_t, *pfn414o_t ;

int fn414( char* out, char* in ) 
{
   int dsz, divlen, divlenx, n, count, iksz, oksz ;
   char cmds[512], xcmds[512], divkey[50], divkeyx[50], zmk[50], msg[128] ;
   char ikey[24], okey[24], icv[8], oicv[8], idat[128], odat[128] ;

   pfn414i_t i414 = (pfn414i_t) in ;
   pfn414o_t o414 = (pfn414o_t) out ;

   CMDSE_t *tse = (CMDSE_t *) cmds ;
   CMDSE *se = (CMDSE *) cmds ;
   CMDSF *sf = (CMDSF *) cmds ;
  
 
   divlen = ldrint(i414->inlen, 4) - 16 ;    /*  divdata length */
      dsz = ldrint(i414->msg_len, 4) ;       /*  macvdata length */

   if( dsz % 16 ) return( -81 ) ;
   count = divlen/16 ;
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( xcmds, 0, sizeof(xcmds) ) ;
   memset( divkey, 0, sizeof(divkey) ) ;
   memset( divkeyx, 0, sizeof(divkeyx) ) ;

   memcpy(cmds, "SE14", 4 ) ;

   if( i414->keytype == 0x02 ){
       memcpy(cmds+4, "U", 1 ) ;
       memcpy(cmds+5, i414->key, iksz = 32 ) ;  /* MAC seed KEY */
   }
   if( i414->keytype == 0x03 ){
       memcpy(cmds, "T", 1 ) ;
       memcpy(cmds+5, i414->key, iksz = 48 ) ;  /* MAC seed KEY */
   }
   memcpy( cmds+5+iksz, "0000000000000000", 16 ) ;
   for( n = 0; n < count; n++ ){
        memcpy( cmds+21+iksz, i414->divdata+(n*16), 16 ) ;
        memcpy( xcmds, cmds, strlen(cmds) ) ;
        if( o414->rc = hsmio(xcmds, 0) ){
            HSMLogMSG( "FN000414;", xcmds, NULL ) ;
            return( o414->rc ) ;
        }
        memcpy( divkey+(n*16), xcmds+4, 16 ) ;
  }

/* destination key */
   divlenx = ldrint(i414->dst_inlen, 4) - 16 ;  /*  divdata length */
   count = divlenx/16 ;
   memcpy(cmds, "SE14", 4 ) ;
   if( i414->dst_keytype == 0x02 ){
       memcpy(cmds+4, "U", 1 ) ;
       memcpy(cmds+5, i414->dst_key, oksz = 32 ) ;  /* MAC seed KEY */
   }
   if( i414->dst_keytype == 0x03 ){
       memcpy(cmds, "T", 1 ) ;
       memcpy(cmds+5, i414->dst_key, oksz = 48 ) ;  /* MAC seed KEY */
   }
   memcpy( cmds+5+oksz, "0000000000000000", 16 ) ;
   for( n = 0; n < count; n++ ){
        memcpy( cmds+21+oksz, i414->dst_divdata+(n*16), 16 ) ;
        memcpy( xcmds, cmds, strlen(cmds) ) ;
        if( o414->rc = hsmio(xcmds, 0) ){
            HSMLogMSG( "FN000414;", xcmds, NULL ) ;
            return( o414->rc ) ;
        }
        memcpy( divkeyx+(n*16), xcmds+4, 16 ) ;
  }

  hexncpy( ikey, divkey, iksz = divlen/2 ) ;
  hexncpy( okey, divkeyx, oksz = divlenx/2 ) ;
  hexncpy( icv, i414->icv, 8 ) ;
  hexncpy( oicv, i414->dst_icv, 8 ) ;
  hexncpy( idat, i414->msg, 16 ) ;
/* translate cardno 32bytes */
  TransLateData_CBC( odat, oicv, okey,  oksz,  idat, 16, icv, ikey, iksz ) ;


/* translate pin & cvv */
  count = (dsz-32)/16 ;
 for( n = 0; n < count; n++ ){
  hexncpy( idat, i414->msg+32+(16*n), 8 ) ;
  TransLateData_CBC( odat+16+(8*n), oicv, okey,  oksz,  idat, 8, icv, ikey, iksz ) ;
 }
/* translate pin & cvv */
  UnpackCHARS( o414->encdata, odat, 16+(8*n) );
  *(o414->encdata+32+(16*n) ) = 0 ;
  return( o414->rc = 0 ) ;
}
