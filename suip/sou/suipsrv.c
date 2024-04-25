#ifdef	WIN32
#include <windows.h>
#include <io.h>
#else
#define  O_BINARY	0
#include <signal.h>
#endif
#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include "comfcns.h"
#include "usleep.h"
#include "ntgetopt.h"
#include "hsmio.h"
#include "svsocknt.h"
#include "socketio.h"
#include "suipfp.h"


static int  ProcessOneMSG( pCtxtKEY_t pCntx ) ;
static int SVRportPRM( void ) ;

extern int SOCtmoSECS ;
extern int CltHeaderSZ, f_err_code ;
extern unsigned long cnipadr[MaxCONN] ;
static int debug, logfl ;
static int sport = 0, cport = 0, bport = 0 ;
static char *MyPGM, ipstr[sizeof(STRipPORT_t)*3] ;
static pSTRipPORT_t ip = (pSTRipPORT_t) ipstr ;
unsigned char StartDate[20] ;

void main( int argc, char* argv[] )
{
   int  n, refresh = 10000 ;   /* default 1 sec refresh wakeup_handler */ 
   char bol[12], *ipaddr = (char*) 0 ;

   MyPGM = argv[0] ;
   memset( ipstr, 0, sizeof(ipstr) ) ;
   for ( n = 0; n < 3; n++ ) {
       (ip+n)->ipaddr = ipaddr ;
       (ip+n)->sock   = INVALID_SOCKET ;
   }

   n = SVRportPRM() ;
   nt_getopt(argc, argv, "datrpel", bol) ;
   debug = bol[0] ;
   logfl = bol[6] ;
   if ( bol[1] > 0 ) ipaddr = argv[bol[1]] ;
   if ( bol[2] > 0 ) { /* time out value, in min */
      SOCtmoSECS = atoi(argv[bol[2]]) * 60 ; /* in secs */
   }
   if ( bol[3] > 0 ) { /* time to refresh parameter table, in secs */
      refresh = atoi(argv[bol[3]]) * 1000 ; /* in minisecs */
   }
   if ( bol[4] > 0 ) bport = atoi(argv[bol[4]]) ;
   if ( bol[5] > 0 ) f_err_code = atoi(argv[bol[5]]) ;
   if ( n == 0 && bport <= 0 ) {
      fprintf( stderr, "%s : server port not defined\n", MyPGM ) ;
      exit( 1 ) ;
   }
   if ( bport > 0 ) {
      (ip+n)->port  = bport ;
      (ip+n++)->ipc = 0x02 ;
   }
   AscDateTime(StartDate) ;

   /*2012/5/15, add log when starting service*/
   HSMLogMSG( "hsmsuip;", "hsmsuip start", NULL ) ;
   SVsocketIO( ip, n, refresh, wakeup_handler, debug ) ;
}

#ifndef WIN32
static void InitProcSIG( int s )
{
   if ( s == SIGUSR1 ) debug = 1 ; /* LINUX SIGUSR1 10 */
   if ( s == SIGUSR2 ) logfl = 1 ; /* LINUX SIGUSR2 12 */
   if ( s == SIGSEGV ) exit( 1 ) ;
   if ( debug ) printf( "signal catched s = %d\n", s ) ;
   signal( s, InitProcSIG ) ;
}

void InitProcTASK( pSTRipPORT_t ip )
{
   int  s ;

   for ( s = 1; s < NSIG; s++ ) {
       if ( s == SIGKILL || s == SIGINT )
	  continue ;
    signal( s, InitProcSIG ) ;
   }
   hsmio(NULL, 0) ;
}
#else
void InitProcTASK( pSTRipPORT_t ip )
{
   hsmio(NULL, 0) ;
   return ;
}
#endif

int SVconIpaddrCHK( pSTRipPORT_t xp, pCtxtKEY_t pCntx, unsigned long ipadr )
{
   int  i ;

   for ( i = 0; i < MaxCONN; i++ ) {
      if ( cnipadr[i] == INADDR_NONE ) continue ;
      if ( cnipadr[i] == ipadr ) break ;
      if ( cnipadr[i] != ipadr ) 
      {
           HSMLogMSG("illegal client ip ",  inet_ntoa(ipadr) ,NULL);
           return( -90 ) ; /* reject connection */
      }
      if ( i >= MaxCONN ){
           HSMLogMSG("over max connection client ip ",  inet_ntoa(ipadr) ,NULL);
           return( -90 ) ; /* reject connection */
      } 
   }
   return( 0 ) ; /* accept connection */
}

int ProcSocMSG( pCtxtKEY_t pCntx, int max )
{
#ifdef WIN32
   ProcessOneMSG(pCntx) ;
#else
   int  i ;

   for ( i = 0; i < max; i++ ) {
       if ( (pCntx+i)->dwLength <= 0 || (pCntx+i)->sock == INVALID_SOCKET )
	  continue ;
       ProcessOneMSG(pCntx+i) ;
   }
#endif
   return( 0 ) ;
}

static int ProcessOneMSG( pCtxtKEY_t pCntx )
{
   int  rc, cc, j, n, cmd ;
   unsigned char *ptr ;
   pSuipPKT_t s ;

/*  Add by David Tai on 2012-05-16 */
   char outbuf[4096] ;
   char *ibuf, *obuf ;

   cDump(outbuf, pCntx->InBuffer,pCntx->dwLength) ;
   HSMLogMSG( "suip read : ", outbuf, NULL ) ;

   if ( debug ) 
        HEXDUMP("pCntx->InBuffer = ", pCntx->InBuffer, pCntx->dwLength ) ;

   s = (pSuipPKT_t) pCntx->InBuffer ;
   n = ldrint( s->PktNoREQs, sizeof(s->PktNoREQs) ) ;
   memcpy( s->PktType, "02", 2 ) ;
   ptr = (unsigned char*) s->PktCTX  ;

   SetFuncCnts( *ptr ) ;

   if ( rc = hsmcmd( ptr, ptr ) ) {
        memcpy( s->PktRC, "0001", 4 ) ;
        strint( rc, s->PktSubRC, 4 ) ;
   }
   else{
        memcpy( s->PktRC, "0001", 4 ) ;
        strint( rc, s->PktSubRC, 4 ) ;
   }
   memset( s->PktRC, '0', 8 ) ;


/*  Add by David Tai on 2012-05-16 */
   cDump( outbuf, (char*) s,pCntx->dwLength);
   HSMLogMSG( "suip send : ", outbuf, NULL ) ;

   if (debug)
       HEXDUMP("suip send : ",(char*) s,pCntx->dwLength);
 
/*
   CMNsocketSNDsw( pCntx->sock,  pCntx->InBuffer, pCntx->dwLength, 1 ) ;
   return( rc ) ;
*/
   CMNsocketSND( pCntx->sock, pCntx->InBuffer, pCntx->dwLength ) ;
   return( 1 ) ;
}

static int SVRportPRM( void )
{
   int  fd, n = 0 ;
   char prmbuf[sizeof(Parameter_t)] ;
   pParameter_t pm = (pParameter_t) prmbuf ;

   if ( (fd = open(NamePrm3K, O_RDONLY|O_BINARY)) < 0 ) {
      fprintf( stderr, "%s : can not open %s(%d)\n", MyPGM, NamePrm3K, errno ) ;
      exit( 1 ) ;
   }
   read( fd, prmbuf, sizeof(prmbuf) ) ;
   close( fd ) ;

   /* Jim, 2012/5/23, modify port=13931 */ 
   (ip+n++)->port = (cport = 13931) ;
   return( n ) ;
}
