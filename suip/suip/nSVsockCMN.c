#include <fcntl.h>
#ifdef WIN32
#include <windows.h>
#include <io.h>
#else
#define  O_BINARY	0
#endif
#include <malloc.h>
#include <errno.h>
#include <time.h>
#include "comfcns.h"
#include "nsvsocknt.h"
#include "nsvsockcmn.h"

static void mmddhhmmss_x( char* str, time_t xt ) ;
static pCtxtKEY_t pOvKEY = NULL ;
static int        MyCurrTAB = -1 ;
int    _MemTabOvFLAG_  = 0, _MyMaxTAB_EXT_ = -1 ;

void* ALCmemTEXT( void )
{
   int  i, max ;

   if ( pOvKEY != NULL && MyCurrTAB > 0 )
      return( (void*) pOvKEY ) ;
#ifdef _TABNOLOWERLIMIT_
   if ( _MyMaxTAB_EXT_ <= 0 )
      MyCurrTAB = MAXCONNS ;
   else
      MyCurrTAB = _MyMaxTAB_EXT_ ;
#else
   if ( _MyMaxTAB_EXT_ < MAXCONNS )
      MyCurrTAB = MAXCONNS ;
   else
      MyCurrTAB = _MyMaxTAB_EXT_ ;
#endif
   if ( MyCurrTAB > 1000 ) MyCurrTAB = 1000 ;
   pOvKEY = (pCtxtKEY_t) malloc( max = sizeof(*pOvKEY)*MyCurrTAB ) ;
   memset( (void*) pOvKEY, 0, max ) ;
   for ( i = 0; i < MyCurrTAB; i++ ) {
       (pOvKEY+i)->sock  = INVALID_SOCKET ;
   }
   return( (void*) pOvKEY ) ;
}

void* REPmemTAB( unsigned long ipaddr, unsigned short port )
{
   int  i ;

   if ( ipaddr == 0 ) return( GETmemTAB() ) ;

   for ( i = 0; i < MyCurrTAB; i++ ) {
       if ( (pOvKEY+i)->sock == INVALID_SOCKET )
	  continue ;
       if ( (pOvKEY+i)->ipaddr == ipaddr && (pOvKEY+i)->port == port ) {
	  closesocket( (pOvKEY+i)->sock ) ;
	  return( (void*) (pOvKEY+i) ) ;
       }
   }
   return( GETmemTAB() ) ;
}

void* GETmemTAB( void )
{
   int  i, mx = -1 ;
   int  r, rsz = sizeof(int) ;
   SOCKET  sock ;
   time_t  mt = time((void*) 0) ;

   for ( i = 0; i < MyCurrTAB; i++ ) {
       (pOvKEY+i)->s1 = i ;
       if ( (sock = (pOvKEY+i)->sock) == INVALID_SOCKET ) {
          memset( (void*) (pOvKEY+i), 0, sizeof(*pOvKEY) ) ;
#ifdef WIN32
          (pOvKEY+i)->ovOut.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
          (pOvKEY+i)->ovOut.hEvent =
	                (HANDLE)((DWORD)(pOvKEY+i)->ovOut.hEvent | 0x1);
#endif
          return( (void*) (pOvKEY+i) ) ;
       }
       if ( getsockopt(sock, SOL_SOCKET, SO_KEEPALIVE, (void*) &r, &rsz) ) {
#ifdef WIN32
	  if ( WSAGetLastError() == WSAENOTSOCK ) {
#else
	  if ( errno == ENOTSOCK || errno == EBADF ) {
#endif
	     closesocket( sock ) ;
	     return( (void*) (pOvKEY+i) ) ;
	  }
       }
       if ( ((pOvKEY+i)->rwblk & 0x10) == 0 ) continue ;
       if ( (((pOvKEY+i)->r1 & 0x01) == 0) && (pOvKEY+i)->lastIoTIME < mt ) {
	  mt = (pOvKEY+i)->lastIoTIME ;
	  mx = i ;
       }
   }
   if ( _MemTabOvFLAG_ == 0 || mx < 0 ) return( NULL ) ;
   closesocket( (pOvKEY+mx)->sock ) ;
   return( (void*) (pOvKEY+mx) ) ;
}

void FREEmemTAB( void *pVoid )
{
   int    i ;
   pCtxtKEY_t pCntx = (pCtxtKEY_t) pVoid ;

   for ( i = 0; i < MyCurrTAB; i++ )
       if ( (pOvKEY+i) == pCntx ) break ;
   if ( i >= MyCurrTAB ) return ;
   if ( pCntx->sock != INVALID_SOCKET )
      closesocket( pCntx->sock ) ;
   CleanUpFCN( pCntx ) ;
#ifdef WIN32
   CloseHandle( pCntx->ovOut.hEvent ) ;
#endif
   memset( (char*) pCntx, 0, sizeof(*pCntx) ) ;
   pCntx->sock = INVALID_SOCKET ;
}

int  GETusageTAB( void )
{
   int  i, cnt = 0 ;
   int  r, rsz = sizeof(int) ;
   SOCKET  sock ;

   for ( i = 0; i < MyCurrTAB; i++ ) {
       if ( (sock=(pOvKEY+i)->sock) == INVALID_SOCKET ) continue ;
       if ( getsockopt(sock, SOL_SOCKET, SO_KEEPALIVE, (void*) &r, &rsz) ) {
#ifdef WIN32
	  if ( WSAGetLastError() == WSAENOTSOCK ) {
	     CloseHandle( (pOvKEY+i)->ovOut.hEvent ) ;
#else
	  if ( errno == ENOTSOCK || errno == EBADF ) {
#endif
	     closesocket( sock ) ;
	     memset( (char*) (pOvKEY+i), 0, sizeof(*pOvKEY) ) ;
	     (pOvKEY+i)->sock = INVALID_SOCKET ;
	     continue ;
	  }
       }
       cnt++ ;
   }
   return( cnt ) ;
}

void* GETcntxTAB( int* idx )
{
   if ( idx != (int*) 0 )
      *idx = MyCurrTAB ;
   return( (void*) pOvKEY ) ;
}

void ALLsockCLOSE( void )
{
   int    i ;

   for ( i = 0; i < MyCurrTAB; i++ ) {
       if ( (pOvKEY+i)->sock != INVALID_SOCKET ) {
	  closesocket((pOvKEY+i)->sock ) ;
#ifdef WIN32
	  CloseHandle( (pOvKEY+i)->ovOut.hEvent ) ;
#endif
          (pOvKEY+i)->sock = INVALID_SOCKET ;
       }
   }
}

typedef struct {
	char	ipaddr[16] ;
	char	port[6] ;
	char	filler1[2] ;
	char	sock[sizeof(SOCKET)*2] ;
	char	filler2[2] ;
	char	lastIoTIME[10] ;
}  OVtabREC_t, *pOVtabREC_t ;

void cmnDmpSesTAB( char* fname )
{
   int  i, fd ;
   char rbuf[sizeof(OVtabREC_t)+2] ;
   struct in_addr in ;
   pOVtabREC_t t = (pOVtabREC_t) rbuf ;

   if ( fname == (char*) 0 ) return ;

   if ( (fd=open(fname, O_BINARY|O_CREAT|O_RDWR|O_TRUNC, 0666)) < 0 )
      return ;
   for ( i = 0; i < MyCurrTAB; i++ ) {
       memset( rbuf, ' ', sizeof(rbuf) ) ;
       UnpackCHARS((void*) t->sock, (void*) &(pOvKEY+i)->sock, sizeof(SOCKET));
       if ( (pOvKEY+i)->sock != INVALID_SOCKET )
          mmddhhmmss_x( t->lastIoTIME, (pOvKEY+i)->lastIoTIME ) ;
       else
          memset( t->lastIoTIME, '0', sizeof(t->lastIoTIME) ) ;
       in.s_addr = (pOvKEY+i)->ipaddr ;
       strint( (pOvKEY+i)->port, t->port, sizeof(t->port) ) ;
       stchars( inet_ntoa(in), t->ipaddr, sizeof(t->ipaddr) ) ;
       memcpy( rbuf+sizeof(*t), "\r\n", 2 ) ;
       write( fd, rbuf, sizeof(rbuf) ) ;
   }
   close( fd ) ;
}

static void mmddhhmmss_x( char* str, time_t xt )
{
   int    wtm ;
   struct tm *ptr ;

   ptr = localtime(&xt);
   wtm  = (ptr->tm_mon+1) * 100 + ptr->tm_mday ;
   strint( wtm, str, 4 ) ;
   wtm  = ptr->tm_hour * 10000 + ptr->tm_min * 100 + ptr->tm_sec ;
   strint( wtm, str+4, 6 ) ;
}
