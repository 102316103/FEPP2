#include <comfcns.h>
#ifndef WIN32
#include <unistd.h>
#else
#include <process.h>
#endif
#include <socketio.h>
#include <r_connect.h>


#ifdef WIN32
int    StartupCalled = 0 ;
static int ovInit = 0 ;
static OVERLAPPED  ovOut;
static DWORD   dwWritten ;
#endif

extern int  rc_errno ; /* defined in r_connect.c */
#ifdef _EXPORT_MY_PGM_
extern char* MyPGM ;
#endif

static int NameORaddr( char* name ) ;
static int MyBlockRCV( SOCKET soc, char* rcv, int sz ) ;
static int msleep( long ms ) ;
/* Error handler */

static int FatalError( SOCKET soc, char *s, int c, int rc )
{
#ifdef WIN32
    if ( rc <= 0 ) rc = (int) GetLastError() ;
#else
    if ( rc <= 0 ) rc = errno ;
#endif

#ifdef _SOC_ERR_MSG_
#ifdef _EXPORT_MY_PGM_
    fprintf(stderr, "%s(%d, %d) : %s rc = %d\n", MyPGM,
#else
    fprintf(stderr, "(%d, %d) : %s rc = %d\n",
#endif _EXPORT_MY_PGM_
            getpid(), soc, s, rc );
#endif _SOC_ERR_MSG_
    if ( (soc != INVALID_SOCKET) && c ) closesocket( soc ) ;
    return( c ? -1 : -rc );
}

static int NameORaddr( char* name )
{
   int sz = strlen(name) ;

   while ( sz-- ) {
      if ( *(name+sz) != ' ' ) break ;
      *(name+sz) = 0 ;
   }
   sz = strlen(name) ;
   while ( sz-- ) {
      if ( (*name >= '0' && *name <= '9') || *name == '.' ) {
	 name++ ;
	 continue ;
      }
      return( 'N' ) ;
   }
   return( 'A' ) ;
}

SOCKET  CMNInitSOCKwLPORT( char* hname, ushort port, ushort lport )
{
    char   verb[20] ;
    SOCKET soc = INVALID_SOCKET ;

#ifdef WIN32
    WSADATA WsaData;
    int    err ;

    if ( StartupCalled == 0 ) {
       err = WSAStartup(0x0101, &WsaData);
       if (err == SOCKET_ERROR) {
	   FatalError( soc, "WSAStartup Failed", 1, 0 ) ;
	   return( INVALID_SOCKET ) ;
       }
       StartupCalled = 1 ;
    }
#endif

    if ( (soc = r_connect(verb, hname, port, lport)) == INVALID_SOCKET )
       FatalError( soc, verb, 0, rc_errno ) ;
    return( soc ) ;
}

SOCKET  CMNInitSOCKET( char* hname, unsigned short port )
{

/* return( CMNInitSOCKwLPORT(hname, port, 0) ) ; */
    int    err;
    SOCKET soc = INVALID_SOCKET ;
    struct sockaddr_in  sin;
    struct hostent *hp ;

#ifdef WIN32
    WSADATA WsaData;
    if ( StartupCalled == 0 ) {
       err = WSAStartup(0x0101, &WsaData);
       if (err == SOCKET_ERROR) {
	   FatalError( soc, "WSAStartup Failed", 1, 0 ) ;
	   return( INVALID_SOCKET ) ;
       }
       StartupCalled = 1 ;
    }
#endif

    memset(&sin, 0, sizeof(sin));
    sin.sin_port   = htons(port);
    while( *hname == ' ' ) hname++ ;

    if ( NameORaddr(hname) == 'A' ) {
       sin.sin_addr.s_addr = inet_addr(hname) ;
       sin.sin_family = 2 ;
    }
    else {
       if ( (hp = gethostbyname(hname)) == 0 ) {
	   FatalError( soc, "gethostbyname", 1, 0 ) ;
	   return( INVALID_SOCKET ) ;
       }
       memcpy( &sin.sin_addr, hp->h_addr, hp->h_length ) ;
       sin.sin_family = hp->h_addrtype ;
    }

    /*
     * Open a TCP socket (an Internet stream socket)
     */

    soc = socket(AF_INET, SOCK_STREAM, 0);
    if ( soc == INVALID_SOCKET ) {
	FatalError( soc, "socket() failed", 1, 0 ) ;
	return( INVALID_SOCKET ) ;
    }

#ifndef WIN32
    alarm(1) ;
#endif
    err = connect( soc, (struct sockaddr *) &sin, sizeof(sin) ) ;
#ifndef WIN32
    alarm(0) ;
#endif
    if ( err < 0 ) {
	FatalError( soc, "connect() failed", 1, 0 );
	return( INVALID_SOCKET ) ;
    }
    return( soc ) ;
}

#ifndef _CONNECT_ONLY_
#ifdef WIN32
static void InitOV( void )
{
   
   if ( ovInit ) return ;
   memset( (void*) &ovOut, 0, sizeof(ovOut) ) ;
   ovOut.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
   ovOut.hEvent = (HANDLE)((DWORD)ovOut.hEvent | 0x1);
   ovInit = 1 ;
}
#endif

int  CMNsocketIO( SOCKET soc, char* response, char* request, int sz )
{
   int  rc = sz, rwbk = 0 ;
   char buf[2048] ;

   if ( sz < 0 ) {
      rwbk = 1 ;
      rc = (sz = -sz) ;
   }

   if ( rwbk ) {
      *((short*) buf) = htons((short) sz) ;
      rc += sizeof(short) ;
      memcpy( buf+sizeof(short), request, sz ) ;
   }
   else
      memcpy( buf, request, sz ) ;

   if ( send(soc, buf, rc, 0) < 0 )
      return( FatalError(soc, "send() failed!!", 1, 0) ) ;

   if ( rwbk ) {
      if ( (rc = MyBlockRCV(soc, buf, sizeof(buf))) < 0 )
         return( rc ) ;
   }
   else {
      if ( (rc = recv(soc, buf, sizeof(buf), 0)) < 0 )
         return( FatalError(soc, "recv() failed!!", 1, 0) ) ;
   }
   memcpy( response, buf, rc ) ;
   return ( rc ) ;
}

static int CMNsocketSNDv( SOCKET soc, char* req, int sz, int ov )
{
   int  rc = sz, rwbk = 0 ;
   char buf[2048] ;

   if ( sz < 0 ) {
      rwbk = 1 ;
      rc = (sz = -sz) ;
   }

   if ( rwbk ) {
      *((short*) buf) = htons((short) sz) ;
      rc += sizeof(short) ;
      memcpy( buf+sizeof(short), req, sz ) ;
   }
   else
      memcpy( buf, req, sz ) ;

#ifndef WIN32
   rc = send( soc, buf, rc, 0 ) ;
#else
   if ( ov ) {
      if ( ovInit == 0 ) InitOV() ;
      if ( !WriteFile((HANDLE) soc, buf, (DWORD) rc, &dwWritten, &ovOut) )
         rc = -1 ;
   }
   else
      rc = send( soc, buf, rc, 0 ) ;
#endif
#ifdef WIN32
   return( rc < 0 ? (int) GetLastError() : 0 ) ;
#else
   return( rc < 0 ? errno : 0 ) ;
#endif
}
int  CMNsocketSND( SOCKET soc, char* req, int sz )
{
   return( CMNsocketSNDv(soc, req, sz, 0) ) ;
}

int  CMNsocketSNDsw( SOCKET soc, char* req, int sz, int ov )
{
   int    rc, cnt = 0 ;
   fd_set wmask ;
   struct timeval t, *tp = NULL ;

   while ( cnt++ <= 5 ) {
      FD_ZERO( &wmask ) ;
      FD_SET( soc, &wmask ) ;
      if ( select(soc+1, NULL, &wmask, NULL, tp) < 0 ) {
#ifndef WIN32
         if ( errno == EINTR ) continue ;
#else
         if ( WSAGetLastError() == WSAEINTR ) continue ;
#endif
	 return( FatalError(soc, "select SNDsw() failed!!", 1, 0) ) ;
      }
      if ( !FD_ISSET(soc, &wmask) ) continue ;
      if ( (rc = CMNsocketSNDv(soc, req, sz, ov)) == 0 ) return ( 0 ) ;
#ifdef WIN32
      if ( rc == ERROR_NONPAGED_SYSTEM_RESOURCES || /* 1451  */
	   rc == ERROR_PAGED_SYSTEM_RESOURCES    || /* 1452  */
	   rc == ERROR_WORKING_SET_QUOTA         || /* 1453  */
	   rc == ERROR_PAGEFILE_QUOTA            || /* 1454  */
	   rc == ERROR_COMMITMENT_LIMIT   	 || /* 1455  */
	   rc == WSAENOBUFS ) {			    /* 10055 */
#else
      if ( rc == ENOBUFS || rc == ENOMEM || rc == EINTR ) {
#endif
	 tp = &t ;
	 memset( (char*) tp, 0, sizeof(*tp) ) ;
	 tp->tv_usec = 10000 ;
	 continue ;
      }
      break ;
   }
   FatalError( soc, "SNDsw() failed!!", 1, 0 ) ;
   return ( -1 ) ;
}

int  CMNsocketSNDbrk( SOCKET soc, char* req, int sz, int ov )
{
   return( CMNsocketSNDsw(soc, req, -sz, ov) ) ;
}

int  CMNsocketRCV( SOCKET soc, char* res, int sz, int tm )
{
   int  rc, rwbk = 0 ;
   char buf[2048] ;
   struct timeval tmo ;
   fd_set rmask ;

   if ( sz < 0 ) {
      rwbk = 1 ;
      sz = -sz ;
   }

   if ( tm >= 0 ) {
      FD_ZERO( &rmask ) ;
      FD_SET( soc, &rmask ) ;
      memset( (char*) &tmo, 0, sizeof(tmo) ) ;
      tmo.tv_sec = (long) tm ;
      while ( 1 ) {
	 if ( (rc = select(soc+1, &rmask, NULL, NULL, &tmo)) < 0 ) {
#ifdef WIN32
	    rc = WSAGetLastError() ;
#else
	    rc = errno ;
#endif
#ifdef _EXPORT_MY_PGM_
	    nCHARDUMP_f(MyPGM, "CMNsocketRCV select rc = ", rc ) ;
#else
	    nCHARDUMP_f("nsocketio", "CMNsocketRCV select rc = ", rc ) ;
#endif _EXPORT_MY_PGM_

#ifdef WIN32
	    if ( rc == WSAEINTR || rc == WSAEINPROGRESS ) continue ;
#else
	    if ( rc == EINTR ) continue ;
#endif
	    return( rc > 0 ? -rc : rc ) ;
         }
	 if ( rc == 0 ) return( 0 ) ;
	 if ( FD_ISSET(soc, &rmask) ) break ;
      }
   }
   if ( rwbk ) {
      if ( (rc = MyBlockRCV(soc, buf, sizeof(buf))) < 0 )
         return( rc ) ;
   }
   else {
      if ( (rc = recv(soc, buf, sizeof(buf), 0)) < 0 )
         return( FatalError(soc, "recv() whole block failed!!", 1, 0) ) ;
   }
   memcpy( res, buf, rc = (rc > sz ? sz : rc) ) ;
   return ( rc ) ;
}

int CMNsocketRCVbrk( SOCKET soc, char* res, int sz, int tm )
{
   return( CMNsocketRCV(soc, res, -sz, tm) ) ;
}

static int MyBlockRCV( SOCKET soc, char* rcv, int sz )
{
   int  idx = 0, rc, wrc ;

   if ( (rc = recv(soc, rcv, sizeof(short), 0)) < 0 )
      return( FatalError(soc, "recv() length failed!!", 1, 0) ) ;
   wrc = ntohs( *((short*) rcv) ) ;
   if ( wrc > sz ) wrc = sz ;
   while( 1 ) {
      if ( (rc = recv(soc, rcv+idx, wrc, 0)) < 0 ) {
	 if ( idx == 0 )
	    return( FatalError(soc, "recv() 1st block failed!!", 1, 0) ) ;
	 else
	    return( FatalError(soc, "recv() nth block failed!!", 1, 0) ) ;
      }
      idx += rc ;
      if ( rc >= wrc ) break ;
      wrc = wrc - rc ;
   }
   return( idx ) ;
}
#endif /* _CONNECT_ONLY_ */
