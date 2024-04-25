#include "r_connect.h"
#ifdef WIN32
#define r_EWOULDBLOCK WSAEWOULDBLOCK
#define r_EINPROGRESS WSAEINPROGRESS
#define r_ENOBUFS  WSAENOBUFS
#define r_ENOMEM   10106
#define r_sleep(c) Sleep( c*1000 )
#define r_errno    GetLastError()
#define r_ioctl(a, b, c)  ioctlsocket(a, b, c)
#else
#include <sys/ioctl.h>
#define r_EWOULDBLOCK EWOULDBLOCK
#define r_EINPROGRESS EINPROGRESS
#define r_ENOBUFS  ENOBUFS
#define r_ENOMEM   ENOMEM
#define r_sleep(c) sleep( c )
#define r_errno    errno
#define r_ioctl(a, b, c)  ioctl(a, b, c)
#endif

struct linger opt_linger = { 0,0 };
static int NameORaddr( char* name ) ;

int    rc_errno ;

SOCKET r_connect( char* verb, char* hname, ushort port, ushort lport )
{
    int    rc, cnt ;
    SOCKET soc = INVALID_SOCKET ;
    struct sockaddr_in  sin, lsin ;
    struct hostent *hp ;
#ifdef WIN32
    u_long nblk = 1 ;
#else
    int    nblk = 1 ;
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
	   if ( verb != (char*) 0 ) strcpy( verb, "gethostbyname" ) ;
	   return( INVALID_SOCKET ) ;
       }
       memcpy( &sin.sin_addr, hp->h_addr, hp->h_length ) ;
       sin.sin_family = hp->h_addrtype ;
    }

    /*
     * Open a TCP socket (an Internet stream socket)
     */

    for ( cnt = 0; cnt <= 5; cnt++ ) {
       soc = socket(AF_INET, SOCK_STREAM, 0);
       if ( soc != INVALID_SOCKET ) break ;
       rc = r_errno ;
       if ( rc == r_ENOBUFS || rc == r_ENOMEM ) {
	  r_sleep( 5 ) ;
	  continue ;
       }
       break ;
    }
    if ( soc == INVALID_SOCKET ) {
       rc_errno = r_errno ;
       if ( verb != (char*) 0 ) strcpy( verb, "socket" ) ;
       return( INVALID_SOCKET ) ;
    }
    cnt = 1 ;
    setsockopt( soc, SOL_SOCKET, SO_REUSEADDR, (void*) &cnt, sizeof(int) ) ;
#ifdef SO_REUSEPORT
    setsockopt( soc, SOL_SOCKET, SO_REUSEPORT, (void*) &cnt, sizeof(int) ) ;
#endif
    if ( lport != 0 ) {
       lsin.sin_family      = AF_INET;
       lsin.sin_addr.s_addr = htonl(INADDR_ANY);
       lsin.sin_port        = htons(lport) ;
       rc = bind( soc, (struct sockaddr *) &lsin, sizeof(lsin));
       if ( rc < 0 ) {
	  rc_errno = r_errno ;
          if ( verb != (char*) 0 ) strcpy( verb, "bind" ) ;
	  closesocket( soc ) ;
          return( INVALID_SOCKET ) ;
       }
    }
    r_ioctl(soc, FIONBIO, &nblk) ;
    for ( cnt = 0; cnt <= 5; cnt++ ) {
       rc = connect( soc, (struct sockaddr *) &sin, sizeof(sin) ) ;
       rc_errno = (rc < 0 ? r_errno : 0) ;
       if ( (rc_errno == 0) ||
	  (rc_errno != r_ENOBUFS && rc_errno != r_ENOMEM) ) break ;
       r_sleep( 10 ) ;
    }
    if ( rc_errno == r_EWOULDBLOCK || rc_errno == r_EINPROGRESS ) {
       struct timeval tmo ;
       fd_set wmask ;

       FD_ZERO( &wmask ) ;
       FD_SET( soc, &wmask ) ;
       memset( (char*) &tmo, 0, sizeof(tmo) ) ;
       tmo.tv_sec = 2 ;
       if ( select(soc+1, NULL, &wmask, NULL, &tmo) > 0 ) {
          if ( FD_ISSET(soc, &wmask) ) rc_errno = 0 ;
       }
    }
/* #ifdef SO_LINGER */ 
opt_linger.l_onoff = 1;
opt_linger.l_linger = 3;
if (setsockopt(soc, SOL_SOCKET, SO_LINGER, (const char *)&opt_linger, sizeof(opt_linger)) == -1) {
    return (-1);
}

    if ( rc_errno ) {
       if ( verb != (char*) 0 ) strcpy( verb, "connect" ) ;
       closesocket( soc ) ;
       return( INVALID_SOCKET ) ;
    }
    nblk = 0 ;
    r_ioctl(soc, FIONBIO, &nblk) ;
    return( soc ) ;
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
