#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifndef WIN32
#include <unistd.h>
#endif
#include <comfcns.h>
#include <socketio.h>


#ifdef WIN32
int StartupCalled = 0 ;
#endif

static int NameORaddr( char* name ) ;
/* Error handler */

static int FatalError( SOCKET soc, char *s )
{
/*  fprintf(stderr, "%s\n", s); */
    if ( soc != INVALID_SOCKET ) closesocket( soc ) ;
    return( -1 );
}


SOCKET CMNInitSOCKET( char* hname, short port )
{
    int    err;
    SOCKET soc = INVALID_SOCKET ;
    struct sockaddr_in  sin;
    struct hostent *hp ;

#ifdef WIN32
    WSADATA WsaData;
    if ( StartupCalled == 0 ) {
       err = WSAStartup(0x0101, &WsaData);
       if (err == SOCKET_ERROR) {
	   FatalError( soc, "WSAStartup Failed" ) ;
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
	   FatalError( soc, "gethostbyname" ) ;
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
	FatalError( soc, "socket() failed" ) ;
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
	FatalError( soc, "connect() failed" );
	return( INVALID_SOCKET ) ;
    }
    return( soc ) ;
}

#ifndef _CONNECT_ONLY_

int CMNsocketIO( SOCKET soc, char* response, char* request, int sz )
{
   int  rc ;

   if ( send(soc, request, sz, 0) < 0 )
      return( FatalError(soc, "send() failed!!") ) ;

   if ( (rc = recv(soc, response, 1024, 0)) < 0 )
      return( FatalError(soc, "recv() failed!!") ) ;
   return ( rc ) ;
}

int CMNsocketSND( SOCKET soc, char* req, int sz )
{
   int  rc ;

   if ( (rc = send(soc, req, sz, 0)) < 0 )
      return( FatalError(soc, "send() failed!!") ) ;
   return( rc < 0 ? rc : 0 ) ;
}

int CMNsocketRCV( SOCKET soc, char* res, int sz, int tm )
{
   int  rc ;
   fd_set rmask ;
   struct timeval tmo, *tmp = &tmo ;

   FD_ZERO( &rmask ) ;
   FD_SET( soc, &rmask ) ;
   if ( tm >= 0 ) {
      memset( (char*) &tmo, 0, sizeof(tmo) ) ;
      tmo.tv_sec = (long) tm ;
   }
   else
      tmp = NULL ;
   while ( 1 ) {
      if ( (rc = select(soc+1, &rmask, NULL, NULL, tmp)) < 0 )
	 continue ;
      if ( rc == 0 ) return( 0 ) ;
      if ( FD_ISSET(soc, &rmask) ) break ;
   }
/*
   switch( select(soc+1, &rmask, NULL, NULL, tmp) ) {
	case 0 :
	   return( 0 ) ;
	case SOCKET_ERROR :
	   return( FatalError(soc, "recv() failed!!") ) ;
	default :
	   break ;
   }
*/

   if ( (rc = recv(soc, res, sz, 0)) < 0 )
      return( FatalError(soc, "recv() failed!!") ) ;
   return ( rc ) ;
}
#endif /* _CONNECT_ONLY_ */

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

