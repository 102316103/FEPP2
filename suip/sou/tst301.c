#include "suip.h"
#include "socketio.h"
#include <time.h>
#include <sys/timeb.h>
#include <errno.h>

typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    keytype ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char    key[48] ;      /* 0x01= 16, 0x02 = 32, 0x03= 48 */
	char	datalen[4] ;   /* INPUT1 DATA */
	char	data[32] ;	   
}	fn313i_t, *pfn313i_t ;


typedef struct	{
	int 	rc ;
	char	mac[8] ;
}	fn313o_t, *pfn313o_t ;

int Send2SUIP( char* cmd, int sz )
{
   int    rc ;
   SOCKET sock = INVALID_SOCKET ;

   sock = CMNInitSOCKET( "127.0.0.1", 13931 ) ;
   /*sock = CMNInitSOCKET("10.0.10.4",1500);*/
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
   pfn313i_t iB1 = (pfn313i_t) s->PktCTX ;
   pfn313o_t oB1 = (pfn313o_t) (s->PktCTX) ;

   memset( buf, 0, sizeof(buf) ) ;
   strint( 1, s->PktNoREQs, sizeof(s->PktNoREQs) ) ;
   iB1->f = 0x31 ;
   iB1->keytype = 0x02 ;

   memcpy( iB1->key, "76B4B2FA1A147326A5D6AFD13D48A2140000000000000000", 32 ) ;
   memcpy( iB1->datalen, "0018" , 4 ) ;
   memcpy( iB1->data, "465043230215093716" ,18) ;

   /*memcpy( iB1->data,"1234SE14U76B4B2F1A147326A5D6AFD13D48A2140000000000000000FPC230215093716", 72);*/
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
      printf( "tstfB1 : oB1->mac = %s\n", oB1->mac ) ;

}



