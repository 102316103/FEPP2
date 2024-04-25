#ifndef _SVSOCKNT_H
#define _SVSOCKNT_H
#include <time.h>
#include <stdcall.h>
#include <sockdefs.h>

#ifdef	__cplusplus
extern "C" {
#endif

#define SVSHMKEY	7168
#define MAXCONNS	100
#ifndef LINEMAX
#define LINEMAX		2048
#endif

struct ContextKey {
    SOCKET      sock;
    unsigned long ipaddr ;
    time_t      lastIoTIME ;
    char        InBuffer[LINEMAX];
    char        rwblk ; /* see ipc flag within STRipPORT_t */
    char        r1 ; /* & 0x01 - connection made by SVsocketIO */
    short	index ;
    unsigned short port ;
    char        reserved[38] ;
#ifdef WIN32
    OVERLAPPED  ovIn;
    OVERLAPPED  ovOut;
#else
#ifndef DWORD
#define DWORD	long
#endif
#endif
    DWORD       dwWritten;
    DWORD       dwLength;
    short	s1 ;
    char	v1 ;
    char	v2 ;        
} ;

typedef struct ContextKey CtxtKEY_t, *pCtxtKEY_t ;

typedef	struct	{
    char   *ipaddr ;
    unsigned short  port ; /* server port */
    char   ipc ; /* b1(0x01) - ipc port do'nt send KEEPALIVE,
		    b2(0x02) - rwblock,
		    b3(0x04) - one connection per ipaddr
		    b4(0x08) - close on exec UNIX ONLY
		    b5(0x10) - overlayable while table full
		 */
    char   client ;
    SOCKET sock ;
    unsigned short  lport ; /* local port */
    char   ip_r1 ;
    char   ip_r2 ;
    char   ip_dat[16] ;
}   STRipPORT_t, *pSTRipPORT_t ;

/* internal procedures can be called by application */

void SVsocketIO(pSTRipPORT_t ip, int no, long ms, void (*f)(void), int debug) ;
void ALLsockCLOSE( void ) ;
int  SVclientCON( pSTRipPORT_t ip ) ;
void *GETcntxTAB( int* idx ) ;

/* end of internal procedures can be called by application */


/* external procedures must be supplied by application */

void InitProcTASK(pSTRipPORT_t) ;

/*
   ProcSocMSG is an external procedure, it takes socket input msg,
   and process it, if it need to reply to socket, put the data in
   InBuffer & dwLength, then return nonzero, otherwise return 0.
   for WIN32 max is always 1, for UNIX, max is the total entries
   of the table, program should check dwLength greater than zero,
   before processing that entry
   
*/
int  ProcSocMSG( pCtxtKEY_t pCntx, int max ) ;


/*
   SVconIpaddrCHK is an external procedure, whenever a connection requested,
   server will call this external function with the client's ip address.
   the function should return 0, if server should accept the connection,
   otherwise return non_zero. ip is the respective entry(PORT), it try to
   connect, pCntx is a newly allocate table entry for this socket.
   SVconIpPortCHK works the same way as SVconIpaddrCHK except it take
   "(struct sockaddr *) cli" instead of "unsigned long ipadr" as parameter,
   cli contains client ipaddr & local port information(see KsvMsgRtnSV.c).
*/
#ifndef _CHK_ADDR_PORT_
int  SVconIpaddrCHK( pSTRipPORT_t ip, pCtxtKEY_t pCntx, unsigned long ipadr ) ;
#else
int  SVconIpPortCHK( pSTRipPORT_t ip, pCtxtKEY_t pCntx, struct sockaddr* cli ) ;
#endif

#ifdef _CLEANUP_FCN_
void    CleanUpFCN( pCtxtKEY_t pCntx ) ;
#else
#define CleanUpFCN(c)
#endif

/* end of external procedures must be supplied by application */

/*
examples of typical socket server program :

#include "comfcns.h"
#include "nsvsocknt.h"

void main( int argc, char* argv[] )
{
   char ipstr[sizeof(STRipPORT_t)] ;
   pSTRipPORT_t ip = (pSTRipPORT_t) ipstr ;

   memset( ipstr, 0, sizeof(ipstr) ) ;
   ip->port   = // server TCP port number
   ip->client = // 0 for socket server
   ip->ipc    = // assigned the value needed
   SVsocketIO( ip, 1, -1, NULL, 0 ) ;
}

void InitProcTASK( pSTRipPORT_t ip )
{
// initialize all process related stuff here(house keeping)
}

#ifndef _CHK_ADDR_PORT_
int SVconIpaddrCHK( pSTRipPORT_t ip, pCtxtKEY_t pCntx, unsigned long ipadr )
{
   struct in_addr in ;

   if ( pCntx == NULL ) return( -1 ) ;
   if ( ipadr ) {
      in.s_addr = ipadr ;
      fprintf( stderr, "connection req from ip %s\n", inet_ntoa(in) ) ;
   }
   return( 0 ) ; // 0 : accept connection 1: reject connection
}
#else
int  SVconIpPortCHK( pSTRipPORT_t ip, pCtxtKEY_t pCntx, struct sockaddr* cli )
{
   struct in_addr in ;
   char   wk[120] ;
   struct sockaddr_in  *sin ;

   if ( pCntx == NULL ) return( -1 ) ;
   if ( cli != NULL ) {
      sin = (struct sockaddr_in *) cli ;
      in.s_addr = sin->sin_addr.s_addr ;
      fprintf( stderr, "connection req from ip %s, port(%d, %d)\n", 
			inet_ntoa(in), ip->port, ntohs(sin->sin_port) ) ;
   }
   return( 0 ) ; // 0 : accept connection 1: reject connection
}
#endif

#ifdef _CLEANUP_FCN_
void  CleanUpFCN( pCtxtKEY_t pCntx )
{
// socket closed by client, clean up process stuff for a specific client
}
#endif

int ProcSocMSG( pCtxtKEY_t pCntx, int max )
{
// process client's transaction message
}
*/

#ifdef	__cplusplus
}
#endif
#endif /* _SVSOCKNT_H */

