#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"
/*---------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000409 : Diversify Session Key Generate HMAC Message     */
/*---------------------------------------------------------------------------*/
/*
DES_LIB("FN000409"£¬"1T3ICC C6    807     "£¬Input_data_1£¬Input_data_2£¬""£¬""£¬Return_Code)

Input Parameters:
Output Parameters£ºN/A
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'4F' */
	char    meth ;          /* 0x00 - sha1, 0x01 - sha244, 0x02 - sha256, 0x03 - sha384, 0x04 - sha512 */
	char    keytype ;
	char    key[48] ;
	char    inlen[4] ;
	char    divdata[64] ;
	char    msg_len[4] ;
	char    msg[2048] ;
}	fn409i_t, *pfn409i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	tac[256] ;   	    /*  tac(H8)    */
}	fn409o_t, *pfn409o_t ;

static const char basis_64[] =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static int Base64encode(char *encoded, const char *string, int len)
{
    int i;
    char *p;

    p = encoded;
    for (i = 0; i < len - 2; i += 3) {
    *p++ = basis_64[(string[i] >> 2) & 0x3F];
    *p++ = basis_64[((string[i] & 0x3) << 4) |
                    ((int) (string[i + 1] & 0xF0) >> 4)];
    *p++ = basis_64[((string[i + 1] & 0xF) << 2) |
                    ((int) (string[i + 2] & 0xC0) >> 6)];
    *p++ = basis_64[string[i + 2] & 0x3F];
    }
    if (i < len) {
    *p++ = basis_64[(string[i] >> 2) & 0x3F];
    if (i == (len - 1)) {
        *p++ = basis_64[((string[i] & 0x3) << 4)];
       /*  *p++ = '='; */
    }
    else {
        *p++ = basis_64[((string[i] & 0x3) << 4) |
                        ((int) (string[i + 1] & 0xF0) >> 4)];
        *p++ = basis_64[((string[i + 1] & 0xF) << 2)];
    }
    /* *p++ = '='; */
    }

    *p++ = '\0';
    return p - encoded - 1; /*  delete '='  for FISC hmac base64encode */
}

void RelpString(char *str)
{
     int i, count = 0;

    for (i = 0; str[i]; i++){
        if (str[i] == '+') str[i] = '-' ;
        if (str[i] == '/') str[i] = '_' ;
            str[count++] = str[i]; /*  here count is */
    }
    str[count] = '\0';
}


int fn409( char* out, char* in ) 
{
   int dsz, divlen, n, count, iksz, meth ;
   char cmds[2048], xcmds[2048], divkey[128], msg[2048], ekey[128], mac[256] ;
   pfn409i_t i409 = (pfn409i_t) in ;
   pfn409o_t o409 = (pfn409o_t) out ;

   CMDSE_t *tse = (CMDSE_t *) cmds ;
   CMDSE *se = (CMDSE *) cmds ;
   CMDSF *sf = (CMDSF *) cmds ;

   meth = i409->meth ;
   divlen = ldrint(i409->inlen, 4) ;         /*  divdata length */
      dsz = ldrint(i409->msg_len, 4) ;       /*  macvdata length */

   count = divlen/16 ;
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( xcmds, 0, sizeof(xcmds) ) ;
   memset( divkey, 0, sizeof(divkey) ) ;
   memset( ekey, 0, sizeof(ekey) ) ;
   memset( msg, 0, sizeof(msg) ) ;
   memset( mac, 0, sizeof(mac) ) ;

   memcpy(cmds, "SE14", 4 ) ;
   if( i409->keytype == 0x02 ){
       memcpy(cmds+4, "U", 1 ) ;
       memcpy(cmds+5, i409->key,  iksz = 32 ) ;  /* MAC seed KEY */
   }
   if( i409->keytype == 0x03 ){
       memcpy(cmds+4, "T", 1 ) ;
       memcpy(cmds+5, i409->key,  iksz = 48 ) ;  /* MAC seed KEY */
   }
   memcpy( cmds+5+iksz, "0000000000000000", 16 ) ;
   for( n = 0; n < count; n++ ){
        memcpy( cmds+21+iksz, i409->divdata+(n*16), 16 ) ;
        memcpy( xcmds, cmds, strlen(cmds) ) ;
        HSMLogMSG( "FN000409;", xcmds, NULL ) ;
        if( o409->rc = hsmio(xcmds, 0) ){
            HSMLogMSG( "FN000409;", xcmds, NULL ) ;
            return( o409->rc ) ;
        }
        memcpy( divkey+(n*16), xcmds+4, 16 ) ;
  }   
  hexncpy( ekey, divkey, iksz = divlen/2 ) ;
  memcpy( msg, i409->msg, dsz ) ;

  if( meth  == 0 ){
	   hmac(0, msg, dsz, ekey, iksz, mac);
	   dsz = 20 ;
  }
  if( meth  == 1 ){
	  hmac(1, msg, dsz, ekey, iksz, mac);
	  dsz = 28 ;
  }
  if( meth  == 2 ){
	  hmac(2, msg, dsz, ekey, iksz, mac);
	  dsz = 32 ;
  }
  if( meth  == 3 ){
	  hmac(3, msg, dsz, ekey, iksz, mac);
	  dsz = 48 ;
  }
  if( meth  == 4 ){
	  hmac(4, msg, dsz, ekey, iksz, mac);
	  dsz = 64 ;
  }
   n = Base64encode( o409->tac+4, mac, dsz );
   RelpString( o409->tac+4 ) ;
   strint(n, o409->tac, 4 ) ;
   *(o409->tac+4+ n) = 0 ;
   return( o409->rc ) ;
}
