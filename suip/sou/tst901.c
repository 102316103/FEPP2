#include "suip.h"
#include "socketio.h"
#include <time.h>
#include <sys/timeb.h>
#include <errno.h>

typedef struct  {
        char    f ;                                     /* FUNCTION CODE */
        char    cdktype ;                               /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
        char    cdkey[48] ;                             /* ZCMK */
        char    tmktype ;                               /* atm keytype 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
        char    atmkey[48] ;
        char    ilen[4] ;                               /* 0060000000000000 */
        char    input[32] ;                             /* 0060000000000000 */
}       fn901i_t, *pfn901i_t ;

typedef struct  {
        int     rc ;
        char    mak[48] ;
        char    mac[8] ;
}       fn901o_t, *pfn901o_t ;

int Send2SUIP( char* cmd, int sz )
{
   int    rc ;
   SOCKET sock = INVALID_SOCKET ;

   sock = CMNInitSOCKET( "127.0.0.1", 13931 ) ;
   if ( sock == INVALID_SOCKET ) {
      printf( "SUIP server not active!\n" ) ;
      exit( 1 ) ;
   }
   if ( (rc = CMNsocketIO(sock, cmd, cmd, sz)) <= 0 ) {
      printf( "SUIP server not active!\n" ) ;
      exit( 1 ) ;
   }
   closesocket( sock ) ;
   return( rc ) ;
}

void main( int argc, char* argv[] )
{
   int rc ;
   char buf[2048] ;
   pSuipPKT_t s = (pSuipPKT_t) buf ;
   pfn901i_t iB1 = (pfn901i_t) s->PktCTX ;
   pfn901o_t oB1 = (pfn901o_t) (s->PktCTX) ;

   memset( buf, 0, sizeof(buf) ) ;
   strint( 1, s->PktNoREQs, sizeof(s->PktNoREQs) ) ;
   iB1->f = 0x91 ;
   iB1->cdktype = 0x02 ;
   memcpy( iB1->cdkey, "F3B3126E4B4CD30B594AF9D07B6E01680000000000000000", 48 ) ;
   iB1->tmktype = 0x02 ;
   memcpy( iB1->atmkey, "EA34B451971B07D6436B2FE872C9C6E10000000000000000", 48 ) ;
   memcpy( iB1->ilen, "0016", 4 ) ;
   memcpy( iB1->input, "0060000000000000", 16 ) ;
/*
   HEXDUMP("buf = ", buf, 256 ) ;
*/
   rc = Send2SUIP( buf, sizeof(*s)+256 ) ;
/*
   HEXDUMP("buf = ", buf, 128 ) ;
*/
   if( oB1->rc != 0 )
      printf( "tstfB1 : oB1->rc = %d\n", oB1->rc ) ;
   else
      printf( "tstfB1 : oB1->mac = %s\n", oB1->mak ) ;

}



