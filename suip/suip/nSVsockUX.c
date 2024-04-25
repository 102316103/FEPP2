#include <r_connect.h>
#include <signal.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <time.h>
#include <errno.h>
#include <netinet/in.h>
#include "nsvsocknt.h"
#include "nsvsockcmn.h"
#include "socketio.h"

static SOCKET InitSOCKwLPORT( char* hname, ushort port, ushort lport ) ;
static SOCKET InitSVsock( char* locaddr, int port ) ;
static void IsValidSocketFD( void ) ;
static void SETmaskFD( pSTRipPORT_t ip, int n, fd_set* mask ) ;
static void SOCconnOK( pSTRipPORT_t ip ) ;
static void SOCreadOK( fd_set* mask ) ;
static void SOCerrMSG( char* msg ) ;
static void TimeOutThread( void ) ;

static pCtxtKEY_t pCntx ;
static int  MaxCntx, MaxSelFD, debugf ;
extern int  rc_errno ; /* defined in r_connect.c */
int    SOCtmoSECS    = -1 ; /* in secs, <= 0 : no time out */
int    _xSVapALIVE_  = 1 ;

void SVsocketIO( pSTRipPORT_t ip, int n, long ms, void (*f)(void), int debug )
{
   int    i ;
   struct timeval tm, *tv = &tm ;
   time_t lastva, currva ;
   fd_set rmask ;

   debugf = debug ;
   if ( n < 0 )
      n = -n ;
   else
      if ( fork() ) exit( 1 ) ; /* set deamon process */
   pCntx = (pCtxtKEY_t) ALCmemTEXT() ;
   GETcntxTAB( &MaxCntx ) ;
   for ( i = 0; i < n; i++ ) {
#ifdef _SVCLIENT_
       if ( (ip+i)->client == 0 ) {
          (ip+i)->sock = InitSVsock((ip+i)->ipaddr, (ip+i)->port ) ;
          if ( (ip+i)->sock == INVALID_SOCKET ) return ;
       }
       else
	  SVclientCON( ip+i ) ;
#else
       (ip+i)->sock = InitSVsock( (ip+i)->ipaddr, (ip+i)->port ) ;
       if ( (ip+i)->sock == INVALID_SOCKET ) return ;
#endif
   }
   InitProcTASK(ip) ;
   lastva = time( NULL ) ;
   while ( 1 ) {
      if ( SOCtmoSECS > 0 ) TimeOutThread() ;
      if ( !_xSVapALIVE_ ) {
	 f() ;
         lastva = time( NULL ) ;
      }
      else {
         if ( ms > 0 && *f != NULL ) {
	    currva = time( NULL ) ;
	    if ( (currva - lastva) >= (ms / 1000) ) {
	       f() ;
	       lastva = currva ;
	    }
         }
      }
      SETmaskFD( ip, n, &rmask ) ;
      if ( !_xSVapALIVE_ ) {
	 tv = &tm ;
	 memset( (void*) tv, 0, sizeof(*tv) ) ;
	 tv->tv_sec  = 1 ;
      }
      else {
	 if ( ms <= 0 && SOCtmoSECS <= 0 )
	    tv = NULL ;
	 else {
	    tv = &tm ;
	    memset( (void*) tv, 0, sizeof(*tv) ) ;
	    if ( ms > 0 ) {
	       tv->tv_sec  = ms / 1000 ;
	       tv->tv_usec = (ms % 1000) * 1000 ;
	    }
	    else
	       tv->tv_sec  = SOCtmoSECS ;
	    if ( SOCtmoSECS > 0 && tv->tv_sec > SOCtmoSECS )
	       tv->tv_sec  = SOCtmoSECS ;
         }
      }
      
      if ( (i=select(MaxSelFD+1, &rmask, NULL, NULL, tv)) <= 0 ) {
	 if ( errno == EINTR ) continue ;
	 IsValidSocketFD() ;
	 continue ;
      }
      for ( i = 0; i < n; i++ ) {
	  if ( (ip+i)->client == 0 ) {
             if ( FD_ISSET((ip+i)->sock, &rmask) )
	        SOCconnOK( ip+i ) ;
	  }
      }
      SOCreadOK( &rmask ) ;
   }
}

int  SVclientCON( pSTRipPORT_t ip )
{
   pCtxtKEY_t pKey;

   ip->sock = InitSOCKwLPORT ( ip->ipaddr, ip->port, ip->lport ) ;
   if ( ip->sock == INVALID_SOCKET ) return( -1 ) ;
   if ( (pKey = (pCtxtKEY_t) GETmemTAB()) == NULL ) {
       closesocket( ip->sock ) ;
       ip->sock = INVALID_SOCKET ;
       return( -1 ) ;
   }
   pKey->sock  = ip->sock ;
   pKey->rwblk = ip->ipc ;
   pKey->r1    = 1 ;
   pKey->lastIoTIME = time( (void*) 0 ) ;
#ifndef _CHK_ADDR_PORT_
   if ( SVconIpaddrCHK(ip, pKey, 0) ) {
#else
   if ( SVconIpPortCHK(ip, pKey, NULL) ) {
#endif
      FREEmemTAB( pKey );
      ip->sock = INVALID_SOCKET ;
      return( -1 ) ;
   }
   return( 0 ) ;
}

static void IsValidSocketFD( void )
{
   int  i, sz ;
   char name[256] ;
   struct timeval tm ;
   fd_set rmask, wmask ;

   for ( i = 0; i < MaxCntx; i++ ) {
       if ( (pCntx+i)->sock == INVALID_SOCKET ) continue ;
       sz = sizeof(name) ;
       if ( getpeername((pCntx+i)->sock, (void*) name, &sz) ) {
	  FREEmemTAB( pCntx+i );
	  continue ;
       }
       FD_ZERO( &rmask ) ;
       FD_SET( (pCntx+i)->sock, &rmask ) ;
       FD_SET( (pCntx+i)->sock, &wmask ) ;
       memset( (void*) &tm, 0, sizeof(tm) ) ;
       if ( select((pCntx+i)->sock+1, &rmask, &wmask, NULL, &tm) < 0 ) {
	  if ( errno != EINTR ) FREEmemTAB( pCntx+i ) ;
       }
   }
}
/*
static void IsValidSocketFD( void )
{
   int  i, sz ;
   char name[256] ;

   for ( i = 0; i < MaxCntx; i++ ) {
       if ( (pCntx+i)->sock == INVALID_SOCKET ) continue ;
       sz = sizeof(name) ;
       if ( getpeername((pCntx+i)->sock, (void*) name, &sz) )
	  FREEmemTAB( pCntx+i ) ;
   }
}
*/

static void SETmaskFD( pSTRipPORT_t ip, int n, fd_set* mask )
{
   int  i, sz ;
   char name[256] ;

   FD_ZERO( mask ) ;
   MaxSelFD = -1 ;
   for ( i = 0; i < n; i++ ) {
       if ( (ip+i)->sock == INVALID_SOCKET ) continue ;
#ifdef _SVCLIENT_
       if ( (ip+i)->client ) continue ;
#endif
       sz = sizeof(name) ;
       if ( getpeername((ip+i)->sock, (void*) name, &sz) ) {
	  if ( errno == EBADF || errno == ENOTSOCK ) {
	     closesocket( (ip+i)->sock ) ;
             (ip+i)->sock = INVALID_SOCKET ;
	     continue ;
	  }
       }
       FD_SET( (ip+i)->sock, mask ) ;
       if ( (ip+i)->sock > MaxSelFD )
          MaxSelFD = (ip+i)->sock ;
   }
   for ( i = 0; i < MaxCntx; i++ ) {
       if ( (pCntx+i)->sock == INVALID_SOCKET ) continue ;
       sz = sizeof(name) ;
       if ( getpeername((pCntx+i)->sock, (void*) name, &sz) ) {
	  FREEmemTAB( pCntx+i );
	  continue ;
       }
       FD_SET( (pCntx+i)->sock, mask ) ;
       if ( (pCntx+i)->sock > MaxSelFD )
          MaxSelFD = (pCntx+i)->sock ;
   }
}

static void SOCconnOK( pSTRipPORT_t ip )
{
   int  cc, ms, reu = 1 ;
   unsigned long ipaddr = 0 ;
   pCtxtKEY_t pKey;
   struct sockaddr_in clientAddress;
   int     clientAddressLength;

   memset( (void *)&clientAddress, 0, sizeof(clientAddress) ) ;
   clientAddressLength = sizeof(clientAddress);
   ms = accept( ip->sock,
                (struct sockaddr *)&clientAddress,
                &clientAddressLength
	);
   if ( ms < 0 ) {
      SOCerrMSG( "accept() Failed" ) ;
      return ;
   }
   if ( ip->ipc & 0x04 ) /* one connection per IP */
      ipaddr = clientAddress.sin_addr.s_addr ;

   if ( (pKey = (pCtxtKEY_t) REPmemTAB(ipaddr, ip->port)) != NULL ) {
      pKey->ipaddr = clientAddress.sin_addr.s_addr ;
      pKey->sock = ms ;
   }
#ifndef _CHK_ADDR_PORT_
   cc = SVconIpaddrCHK( ip, pKey, clientAddress.sin_addr.s_addr ) ;
#else
   cc = SVconIpPortCHK( ip, pKey, (struct sockaddr *) &clientAddress ) ;
#endif
   if ( cc || pKey == NULL ) {
      close(ms) ;
      if ( pKey != NULL ) FREEmemTAB( pKey ) ;
      return ;
   }
   pKey->rwblk  = ip->ipc ;
   pKey->port   = ip->port ;
   pKey->lastIoTIME = time( (void*) 0 ) ;
   if ( (ip->ipc & 0x01) == 0 )
      setsockopt( ms, SOL_SOCKET, SO_KEEPALIVE, &reu, sizeof(int) ) ;
   if ( ip->ipc & 0x08  ) fcntl( ms, F_SETFD, 1 ) ;
}

static int SvBlockREAD( pCtxtKEY_t p )
{
   int  rc ;

   if ( read(p->sock, p->InBuffer, sizeof(short)) <= 0 ) {
      FREEmemTAB( p ) ;
      return( 0 ) ;
   }
   if ( (p->dwLength = ntohs(*((short*) p->InBuffer))) == 0 )
      return( 0 ) ;
   if ( p->dwLength > sizeof(p->InBuffer) ) {
      FREEmemTAB( p ) ;
      return( 0 ) ;
   }
   p->index = 0 ;
   memset( p->InBuffer, 0, sizeof(p->InBuffer) ) ;
   while( 1 ) {
      if ( (rc = read(p->sock, p->InBuffer+p->index, p->dwLength)) <= 0 ) {
          FREEmemTAB( p ) ;
	  return( 0 ) ;
      }
      p->index += rc ;
      if ( rc >= p->dwLength ) {
	 p->lastIoTIME = time( (void*) 0 ) ;
	 return( p->index ) ;
      }
      p->dwLength = p->dwLength - rc ;
   }
}

static void SOCreadOK( fd_set* mask )
{
   int  i, rc ;
   pCtxtKEY_t p;

   for ( i = 0; i < MaxCntx; i++ ) {
       p = pCntx+i ;
       p->dwLength = 0 ;
       if ( p->sock == INVALID_SOCKET )
          continue ;
       if ( FD_ISSET(p->sock, mask) == 0 ) continue ;
       if (  p->rwblk & 0x02 ) {
	  p->dwLength = SvBlockREAD(p) ;
       }
       else {
	 memset( p->InBuffer, 0, sizeof(p->InBuffer) ) ;
	 if ( (p->dwLength = read(p->sock, p->InBuffer, LINEMAX)) <= 0 )
             FREEmemTAB( p ) ;
         else
	     p->lastIoTIME = time( (void*) 0 ) ;
       }
   }
   ProcSocMSG(pCntx, MaxCntx) ;
}
static void SOCerrMSG( char* cmd )
{
   fprintf( stderr, "process %d : '%s' %s\n", getpid(), cmd, strerror(errno) ) ;
}

static SOCKET InitSVsock( char* locaddr, int port )
{
    SOCKET  listener;
    struct sockaddr_in serverAddress;
    int     reu = 1, err = 0 ;


    /*
     * Open a TCP socket connection to the server
     * By default, a socket is always opened
     * for overlapped I/O.  Do NOT attach this
     * socket (listener) to the I/O completion
     * port!
     */
    if ( (listener = socket(AF_INET, SOCK_STREAM, 0)) < 0 ) {
       SOCerrMSG( "socket" ) ;
       return( INVALID_SOCKET ) ;
    }
    setsockopt( listener, SOL_SOCKET, SO_REUSEADDR, &reu, sizeof(int) ) ;

    /*
     * Bind our local address
     */
    memset(&serverAddress, 0, sizeof(serverAddress));
    serverAddress.sin_family      = AF_INET ;
    if ( locaddr == (char*) 0 )
       serverAddress.sin_addr.s_addr = htonl(INADDR_ANY);
    else
       serverAddress.sin_addr.s_addr = inet_addr(locaddr);
    serverAddress.sin_port        = htons((short)port);

    err = bind( listener,
                (struct sockaddr *)&serverAddress,
                sizeof(serverAddress)
              );
    if ( err < 0 ) {
       SOCerrMSG( "bind" ) ;
       closesocket( listener ) ;
       return( INVALID_SOCKET ) ;
    }
    fcntl( listener, F_SETFD, 1 ) ;
    listen(listener, SOMAXCONN);
    return( listener ) ;
}

static SOCKET InitSOCKwLPORT( char* hname, ushort port, ushort lport )
{
    char   verb[20] ;
    SOCKET soc ;

    soc = r_connect( verb, hname, port, lport ) ;
    if ( debugf && soc == INVALID_SOCKET )
        fprintf( stderr, "%s rc = %d\n", verb, rc_errno ) ;
    return( soc ) ;
}

#include "nSVsockCMN.c"

static void TimeOutThread( void )
{
   int    i, wva ;
   time_t ctm ;

   ctm = time( (void*) 0 ) ;
   for ( i = 0; i < MyCurrTAB; i++ ) {
       if ( (pOvKEY+i)->sock == INVALID_SOCKET ) continue ;
       if ( ((int) (ctm - (pOvKEY+i)->lastIoTIME)) < SOCtmoSECS )
	   continue ;
       closesocket((pOvKEY+i)->sock ) ;
       memset( (char*) (pOvKEY+i), 0, sizeof(*pOvKEY) ) ;
       (pOvKEY+i)->sock = INVALID_SOCKET ;
   }
}
